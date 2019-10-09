/*
 * Simple program to test the SL900A Get Calibration Value function in the Mercury API
 */
package samples;

// Import the API
import com.thingmagic.*;


public class SL900AGetCalibrationData
{
    
  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "SL900AGetCalibrationData [-v] [reader-uri] [--ant n[,n...]] \n" +
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
      usage();

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
        
        //Use first antenna for tag operation
        if (antennaList != null)
            r.paramSet("/reader/tagop/antenna", antennaList[0]);
        
        //Set the session to session 0
        r.paramSet(TMConstants.TMR_PARAM_GEN2_SESSION, Gen2.Session.S0);

        //Get the region
        Reader.Region region = (Reader.Region) r.paramGet(TMConstants.TMR_PARAM_REGION_ID);
        System.out.println("The current region is " + region);

        //Get the session
        Gen2.Session session = (Gen2.Session) r.paramGet(TMConstants.TMR_PARAM_GEN2_SESSION);
        System.out.println("The current session is " + session);
        
        //Get the read plan
        ReadPlan rp = (ReadPlan) r.paramGet(TMConstants.TMR_PARAM_READ_PLAN);
        System.out.println("The current Read Plan is: " + rp);
        
        //Create the Get Calibration Data tag operation
        Gen2.IDS.SL900A.GetCalibrationData tagOp = new Gen2.IDS.SL900A.GetCalibrationData();
        
        //Use the Get Calibration Data (and SFE Parameters) tag op
        Gen2.IDS.SL900A.CalSfe calSfe = (Gen2.IDS.SL900A.CalSfe)r.executeTagOp(tagOp, null);
        
        //Display the Calibration (and SFE Parameters) Data
        System.out.println(""+ calSfe);
        
        //Display the specific Calibration data gnd_switch
        System.out.println("gnd_switch: " + calSfe.calibrationData.getGndSwitch());

        //Display the specific SFE Parameter Verify Sensor ID
        System.out.println("Verify Sensor ID: " + calSfe.sfeParameter.getVerifySensorID());
    } 
    catch (ReaderException re)
    {
      System.out.println("Reader Exception : " + re.getMessage());
    }
    catch (Exception re)
    {
        System.out.println("Exception : " + re.getMessage());
    }
    finally
    {
        // Shut down reader
        r.destroy();
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
