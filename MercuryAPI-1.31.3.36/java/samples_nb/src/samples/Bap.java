package samples;
import com.thingmagic.*;

public class Bap
{
  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "Bap [-v] [reader-uri] [--ant n[,n...]] \n" +
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
        int[] antennaList = null;
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
        if (Reader.Region.UNSPEC == (Reader.Region)r.paramGet("/reader/region/id"))
        {
            Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
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

        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2);
        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
        Gen2.Bap bap;
        System.out.println("case 1: read by using the default bap parameter values \n");
        System.out.println("Get Bap default parameters");
        bap = (Gen2.Bap) r.paramGet(TMConstants.TMR_PARAM_GEN2_BAP);
        System.out.println("powerupdelay : "+bap.powerUpDelayUs +" freqHopOfftimeUs : "+ bap.freqHopOfftimeUs);
        //  Read tags
        tagReads = r.read(500);
        for (TagReadData tagReadData : tagReads)
        {
              System.out.println("EPC :"+tagReadData.epcString());
        }

        System.out.println("case 2: read by setting the bap parameters  \n");
        bap= new Gen2.Bap(10000, 20000);
        //set the parameters to the module
        r.paramSet(TMConstants.TMR_PARAM_GEN2_BAP, bap);
        System.out.println("Get Bap  parameters");
        bap = (Gen2.Bap) r.paramGet(TMConstants.TMR_PARAM_GEN2_BAP);
        System.out.println("powerupdelay : "+bap.powerUpDelayUs +" freqHopOfftimeUs : "+ bap.freqHopOfftimeUs);
        //  Read tags
        tagReads = r.read(500);
        for (TagReadData tagReadData : tagReads)
        {
              System.out.println("EPC :"+tagReadData.epcString());
        }

        System.out.println("case 3: read by disbling the bap option  \n");
        //initialize the with -1
        //set the parameters to the module
        r.paramSet(TMConstants.TMR_PARAM_GEN2_BAP, null);
        System.out.println("Get Bap  parameters");
        bap = (Gen2.Bap) r.paramGet(TMConstants.TMR_PARAM_GEN2_BAP);
        System.out.println("powerupdelay : "+bap.powerUpDelayUs +" freqHopOfftimeUs : "+ bap.freqHopOfftimeUs);
        //  Read tags
        tagReads = r.read(500);
        for (TagReadData tagReadData : tagReads)
        {
              System.out.println("EPC :"+tagReadData.epcString());
        }

        // Shut down reader
        r.destroy();
    }
    catch (ReaderException re)
    {
      re.printStackTrace();
      System.out.println("Reader Exception : " + re.getMessage());
    }
    catch (Exception re)
    {
        re.printStackTrace();
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
