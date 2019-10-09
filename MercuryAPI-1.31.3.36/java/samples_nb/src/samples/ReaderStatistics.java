/**
 * Sample program that gets and prints the reader stats
 */
// Import the API
package samples;

import com.thingmagic.*;
import java.util.ArrayList;
import java.util.List;

public class ReaderStatistics
{

    static void usage()
    {
        System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "ReaderStatistics [-v] [reader-uri] [--ant n[,n...]] \n" +
                  "-v  Verbose: Turn on transport listener\n" +
                  "reader-uri  Reader URI: e.g., \"tmr:///COM1\", \"tmr://astra-2100d3\"\n"
                + "--ant  Antenna List: e.g., \"--ant 1\", \"--ant 1,2\"\n" 
                + "e.g: tmr:///com1 --ant 1,2 ; tmr://10.11.115.32 --ant 1,2\n ");
        System.exit(1);
    }

    public static void setTrace(Reader r, String args[])
    {
        if (args[0].toLowerCase().equals("on"))
        {
           r.addTransportListener(r.simpleTransportListener);
        }
    }
  

    public static void main(String argv[])
    {
        // Program setup
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

        // Create Reader object, connecting to physical device
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
            
            String model = r.paramGet("/reader/version/model").toString();
            if (r.isAntDetectEnabled(antennaList))
            {
                System.out.println("Module doesn't has antenna detection support, please provide antenna list");
                r.destroy();
                usage();
            }
            
            if((model.equalsIgnoreCase("M6e Micro") || (model.equalsIgnoreCase("M6e Micro USB"))|| (model.equalsIgnoreCase("M6e Micro USBPro"))))
            {
                r.paramSet(TMConstants.TMR_PARAM_ANTENNA_CHECKPORT, true); 
            }
            
            SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
            r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);

            SerialReader.ReaderStatsFlag[] READER_STATISTIC_FLAGS = {SerialReader.ReaderStatsFlag.ALL};

            r.paramSet(TMConstants.TMR_PARAM_READER_STATS_ENABLE, READER_STATISTIC_FLAGS);
            SerialReader.ReaderStatsFlag[] getReaderStatisticFlag = (SerialReader.ReaderStatsFlag[]) r.paramGet(TMConstants.TMR_PARAM_READER_STATS_ENABLE);
            if (READER_STATISTIC_FLAGS.equals(getReaderStatisticFlag))
            {
                System.out.println("GetReaderStatsEnable--pass");
            }
            else
            {
                System.out.println("GetReaderStatsEnable--Fail");
            }

            TagReadData[] tagReads;
            tagReads = r.read(500);
            // Print tag reads
            List<String> epcList = new ArrayList<String>();
            for (TagReadData tr : tagReads)
            {
                String epcString = tr.getTag().epcString();
                System.out.println(tr.toString());
                if (!epcList.contains(epcString))
                {
                    epcList.add(epcString);
                }
            }

            SerialReader.ReaderStats readerStats = (SerialReader.ReaderStats) r.paramGet(TMConstants.TMR_PARAM_READER_STATS);

            System.out.println("Frequency   :  " + readerStats.frequency + " kHz");
            System.out.println("Temperature :  " + readerStats.temperature + " C");
            System.out.println("Protocol    :  " + readerStats.protocol);
            System.out.println("Connected antenna port : " + readerStats.antenna);
            try
            {
                int portList[] = (int[])r.paramGet("/reader/antenna/portList");
                int[] connectedAntennaPorts = readerStats.connectedAntennaPorts;
                List<Integer> list = new ArrayList<Integer>();
                for(int index = 0; index < connectedAntennaPorts.length; index++)
                {
                    list.add(connectedAntennaPorts[index]);
                }
                    
                for (int i = 1; i <= portList.length; i++) 
                {
                    if (list.contains(i)) 
                    {
                        System.out.println("Antenna " + (i) + " is : Connected");
                    } else 
                    {
                        System.out.println("Antenna " + (i) + " is : Disconnected");
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            
            /* Get the antenna return loss value, this parameter is not the part of reader stats */
            int[][] returnLoss=(int[][]) r.paramGet(TMConstants.TMR_PARAM_ANTENNA_RETURNLOSS);
            for (int[] rl : returnLoss)
            {
              System.out.println("Antenna ["+rl[0] +"] returnloss :"+ rl[1]);
            }

            int[] rfontimes = readerStats.rfOnTime;
            for (int antenna = 0; antenna < rfontimes.length; antenna++)
            {
              System.out.println("RF_ON_TIME for antenna [" + (antenna + 1) + "] is : " + rfontimes[antenna] +" ms");
            }

            byte[] noiseFloorTxOn = readerStats.noiseFloorTxOn;
            for (int antenna = 0; antenna < noiseFloorTxOn.length; antenna++)
            {
              System.out.println("NOISE_FLOOR_TX_ON for antenna [" + (antenna + 1) + "] is : " + noiseFloorTxOn[antenna] +" db");
            }
            r.destroy();
        }
        catch (ReaderException re)
        {
            System.out.println("Reader Exception : " + re.getMessage());
        } 
        catch (Exception re)
        {
            System.out.println("Exception : " + re.getMessage());
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
