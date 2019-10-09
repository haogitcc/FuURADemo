/*
 * Simple program that configure the gpi trigger and autonomous operation.
 */
package samples;

import com.thingmagic.Gen2;
import com.thingmagic.GpiPinTrigger;
import com.thingmagic.ReadListener;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SerialReader;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagOp;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;


public class SavedReadplanConfig
{
    static void usage()
    {
        System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "SavedReadplanConfig [-v] [reader-uri] [--ant n[,n...]] \n" +
                  "-v  Verbose: Turn on transport listener\n" +
                  "reader-uri  Reader URI: e.g., \"tmr:///COM1\", \"tmr://astra-2100d3\"\n"
                + "e.g: tmr:///com1 --ant 1,2 ; tmr://10.11.115.32 --ant 1,2\n ");
        System.exit(1);
    }

    public static void setTrace(Reader r, String args[])
    {
        if (args[0].toLowerCase().equals("on"))
        {
            r.addTransportListener(Reader.simpleTransportListener);
        }      
    }
    
    public static void main(String argv[]) throws ReaderException
    {
        Reader r = null;
        int nextarg = 0;
        boolean trace = false;
        int[] antennaList = null;

        if (argv.length < 1)
        {
            usage();
        }
        if (argv[nextarg].equals("-v"))
        {
            trace = true;
            nextarg++;
        }

        try
        {
            String readerURI = argv[nextarg];
            nextarg++;
            
            for ( ; nextarg < argv.length; nextarg++)
            {
                String arg = argv[nextarg];
                if (arg.equalsIgnoreCase("--ant"))
                {
                    if (antennaList != null)
                    {
                        System.out.println("Duplicate argument: --ant specified more than once");
                        usage();
                    }
                    antennaList = parseAntennaList(argv, nextarg);
                    nextarg++;
                }
                else
                {
                    System.out.println("Argument "+argv[nextarg] +" is not recognised");
                    usage();
                }
            }
            
            r = Reader.create(readerURI);
            if (trace)
            {
                setTrace(r, new String[]{"on"});
            }
            r.connect();
            String model = (String) r.paramGet("/reader/version/model");
            if (false
                    || model.equalsIgnoreCase("M6e")
                    || model.equalsIgnoreCase("M6e PRC")
                    || model.equalsIgnoreCase("M6e JIC")
                    || model.equalsIgnoreCase("M6e Micro")
                    || model.equalsIgnoreCase("M6e Nano")
                    || model.equalsIgnoreCase("M6e Micro USBPro"))
            {
                if (Reader.Region.UNSPEC == (Reader.Region) r.paramGet("/reader/region/id"))
                {
                    Reader.Region[] supportedRegions = (Reader.Region[]) r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
                    if (supportedRegions.length < 1)
                    {
                        throw new Exception("Reader doesn't support any regions");
                    }
                    else
                    {
                        r.paramSet("/reader/region/id", supportedRegions[0]);
                    }
                }
                
                if ((model.equalsIgnoreCase("M6e Micro") || model.equalsIgnoreCase("M6e Nano")) && antennaList == null)
                {
                    System.out.println("Module doesn't has antenna detection support, please provide antenna list");
                    r.destroy();
                    usage();
                }

                //Uncomment the following line to revert the module settings to factory defaluts
                //r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.CLEAR));
                
                /* Uncomment the following line if want to enable embedded read with autonomous operation.
                 * Add tagop object op in simple read plan constructor.
                 * Add filter object epcFilter in simple read plan constructor.
                 */
//               TagOp op = new Gen2.ReadData(Gen2.Bank.RESERVED, 2, (byte)2);
//               Gen2.TagData epcFilter = new Gen2.TagData("112233445566778899009999");
               
                GpiPinTrigger gpiTrigger = new GpiPinTrigger();
                //Gpi trigger option not there for M6e Micro USB
                if(!model.equalsIgnoreCase("M6e Micro USB"))
                {
                    gpiTrigger.enable = true;
                    //set the gpi pin to read on
                    r.paramSet("/reader/read/trigger/gpi", new int[]{1});
                }
              
                SimpleReadPlan srp = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
                if(!model.equalsIgnoreCase("M6e Micro USB"))
                {
                  srp.triggerRead = gpiTrigger;
                }
                //To disable autonomous read make enableAutonomousRead flag to false and do SAVEWITHRREADPLAN 
                srp.enableAutonomousRead = true;
                SerialReader.ReaderStatsFlag[] READER_STATISTIC_FLAGS = {SerialReader.ReaderStatsFlag.TEMPERATURE};
                //Uncomment the line if reader stats need to be included as part of autonomous operation
                //r.paramSet("/reader/stats/enable",READER_STATISTIC_FLAGS);
              
                r.paramSet("/reader/read/plan", srp);
               
                ReadListener readListener = new PrintListener();
                r.addReadListener(readListener);
                
                r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.SAVEWITHREADPLAN));
                System.out.println("User profile set option:save all configuration with read plan");
                
                r.paramSet("/reader/userConfig", new SerialReader.UserConfigOp(SerialReader.SetUserProfileOption.RESTORE));
                System.out.println("User profile set option:restore all configuration");

                r.receiveAutonomousReading();
                Thread.sleep(6000);
                
                r.removeReadListener(readListener);
                r.destroy();
            }
            else
            {
                System.out.println("Error: This codelet works only on M6e variants");
            }
        }
        catch (ReaderException re)
        {
            System.out.println("ReaderException: " + re.getMessage());
        }
        catch (Exception re)
        {
            System.out.println("Exception: " + re.getMessage());
        }
    }
  static class PrintListener implements ReadListener
  {
    public void tagRead(Reader r, TagReadData tr)
    {
      System.out.println("Background read: " + tr.toString());
    }
  }
  
  static  int[] parseAntennaList(String[] args,int argPosition)
    {
        int[] antennaList = null;
        try
        {
            String argument = args[argPosition + 1];
            String[] antennas = argument.split(",");
            int i = 0;
            antennaList = new int[antennas.length];
            for (String ant : antennas)
            {
                antennaList[i] = Integer.parseInt(ant);
                i++;
            }
        }
        catch (IndexOutOfBoundsException ex)
        {
            System.out.println("Missing argument after " + args[argPosition]);
            usage();
        }
        catch (Exception ex)
        {
            System.out.println("Invalid argument at position " + (argPosition + 1) + ". " + ex.getMessage());
            usage();
        }
        return antennaList;
    }
}
