/**
 * Sample program that reads tags for a fixed period of time (500ms)
 * and prints the tags found.
 */

// Import the API
package samples;
import com.thingmagic.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class read
{  
  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "read [-v] [reader-uri] [--ant n[,n...]] \n" +
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
    boolean printTagMetaData = false;

    if (argv.length < 1)
      usage();

    if (argv[nextarg].equals("-v"))
    {
      trace = true;
      nextarg++;
    }
    
    // Create Reader object, connecting to physical device
    try
    { 
     
        TagReadData[] tagReads;
        String readerURI = argv[nextarg];
        nextarg++;

        for (; nextarg < argv.length; nextarg++)
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
                System.out.println("Argument " + argv[nextarg] + " is not recognised");
                usage();
            }
        }

        r = Reader.create(readerURI);
        if (trace)
        {
            setTrace(r, new String[] {"on"});
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

        if (r.isAntDetectEnabled(antennaList))
        {
            System.out.println("Module doesn't has antenna detection support, please provide antenna list");
            r.destroy();
            usage();
        }
        
        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
    
        if(r.getClass().getName()== ("com.thingmagic.SerialReader"))
        {
//        Set<TagReadData.TagMetadataFlag> setMetaDataFlags = EnumSet.of(TagReadData.TagMetadataFlag.ANTENNAID , TagReadData.TagMetadataFlag.FREQUENCY);
        Set<TagReadData.TagMetadataFlag> setMetaDataFlags = EnumSet.of( TagReadData.TagMetadataFlag.ALL);
        r.paramSet(TMConstants.TMR_PARAM_READER_METADATA,setMetaDataFlags);
        }
        else
        {
            // Configurable metadata param is not supported for llrp readers.
            printTagMetaData = false;
        }
        // Read tags
        tagReads = r.read(500);
        // Print tag reads
        for (TagReadData tr : tagReads)
        {
            System.out.println("EPC: " + tr.epcString());
            
            // Enable printTagMetaData to print meta data
            if (printTagMetaData) 
            {
                for (TagReadData.TagMetadataFlag metaData : TagReadData.TagMetadataFlag.values()) 
                {
                    if (tr.metadataFlags.contains(metaData))
                    {
                        switch(metaData)
                        {
                            case ANTENNAID:
                                System.out.println("Antenna ID: " + tr.getAntenna());
                                break;
                            case DATA:
                                // User should initialize Read Data
                                System.out.print("Data: ");
                                for (byte b : tr.getData()) {
                                    System.out.printf("%02x ", b);
                                }
                                System.out.printf("\n");
                                break;
                            case FREQUENCY:
                                System.out.println("Frequency: " + tr.getFrequency());
                                break;
                            case GPIO_STATUS:
                                Reader.GpioPin[] state = tr.getGpio();
                                System.out.printf("GPO Status: \n");
                                for (Reader.GpioPin gp : state) {
                                    System.out.printf("Pin %d: %s\n", gp.id, gp.high ? "High" : "Low");
                                }
                                System.out.printf("GPI Status: \n");
                                 for (Reader.GpioPin gp : state) {
                                    System.out.printf("Pin %d: %s\n", gp.id, gp.output ? "High" : "Low");
                                }
                                break;
                            case PHASE:
                                System.out.println("Phase: " + tr.getPhase());
                                break;
                            case PROTOCOL:
                                System.out.println("Protocol: " + tr.getTag().getProtocol());
                                break;
                            case READCOUNT:
                                System.out.println("ReadCount: " + tr.getReadCount());
                                break;
                            case RSSI:
                                System.out.println("RSSI: " + tr.getRssi());
                                break;
                            case TIMESTAMP:
                                System.out.println("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date(tr.getTime())));
                                break;
                            default:
                                break;
                        }
                        if (TagProtocol.GEN2 == tr.getTag().getProtocol()) 
                        {
                            Gen2.TagReadData gen2 = (Gen2.TagReadData) (tr.prd);
                            switch (metaData) 
                            {
                                case GEN2_Q:
                                    System.out.println("Gen2Q: " + gen2.getGen2Q());
                                    break;
                                case GEN2_LF:
                                    System.out.println("Gen2LinkFrequency: " + gen2.getGen2LF());
                                    break;
                                case GEN2_TARGET:
                                    System.out.println("Gen2Target: " + gen2.getGen2Target());
                                    break;
                            }
                        }
                    }
                }
            }
        }

        // Shut down reader
        r.destroy();
    } 
    catch (ReaderException re)
    {
      System.out.println("Reader Exception: " + re.getMessage());
    }
    catch (Exception re)
    {
        System.out.println("Exception: " + re.getMessage());
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
