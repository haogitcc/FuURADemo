/*
 * Simple program to test the SL900A Get Sensor Value function in the Mercury API
 */
package samples;

// Import the API
import com.thingmagic.*;


public class SL900AGetSensorValue
{
  static void usage()
  { 
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "SL900AGetSensorValue [-v] [reader-uri] <sensor> [--ant n[,n...]] \n" +
                  "-v  Verbose: Turn on transport listener\n" +
                  "reader-uri  Reader URI: e.g., \"tmr:///COM1\", \"tmr://astra-2100d3\"\n"
                + "sensor: TEMP, EXT1, EXT2, BATT  "+
                  "--ant  Antenna List: e.g., \"--ant 1\", \"--ant 1,2\"\n"
                + "e.g: tmr:///com1 TEMP --ant 1,2 ; tmr://10.11.115.32 EXT1 --ant 1,2\n ");
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
      if(argv.length < 3)
      {
         usage();
      }
      trace = true;
      nextarg++;
    }
    else if(argv.length < 2)
    {
       usage();
    }
    
    // Create Reader object, connecting to physical device
    try
    {
        String readerURI = argv[nextarg];
        nextarg++;
        String mode = argv[nextarg];
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
        
        Gen2.IDS.SL900A.GetSensorValue tagop = null;
        
        if (mode.equalsIgnoreCase("TEMP"))
                {
            //Create a tag op to retrieve the TEMP sensor value
            tagop = new Gen2.IDS.SL900A.GetSensorValue(Gen2.IDS.SL900A.Sensor.TEMP);
        }
        else if (mode.equalsIgnoreCase("EXT1"))
        {
            //Create a tag op to retrieve the EXT1 sensor value
            tagop = new Gen2.IDS.SL900A.GetSensorValue(Gen2.IDS.SL900A.Sensor.EXT1);
        }
        else if (mode.equalsIgnoreCase("EXT2"))
        {
            //Create a tag op to retrieve the EXT2 sensor value
            tagop = new Gen2.IDS.SL900A.GetSensorValue(Gen2.IDS.SL900A.Sensor.EXT2);
        }
        else if (mode.equalsIgnoreCase("BATTV"))
        {
            //Create a tag op to retrieve the BATTV sensor value
            tagop = new Gen2.IDS.SL900A.GetSensorValue(Gen2.IDS.SL900A.Sensor.BATTV);
        }
        else
        {
            //Print that an invalid input was detected
            System.out.println(String.format("{%s} is not a valid sensor", mode));
            //Exit the program
            usage();
        }

        //Execute the tagop
         Gen2.IDS.SL900A.SensorReading sensorReading = (Gen2.IDS.SL900A.SensorReading)r.executeTagOp(tagop, null);
         
         //Print the raw sensor value info
         System.out.println(String.format("ADError:{%s} Value:{%d} RangeLimit:{%d} Raw: {%d}", 
                 sensorReading.getADError(), sensorReading.getValue(),
                 sensorReading.getRangeLimit(), sensorReading.getRaw()));
         
                //Print the converted sensor value
                if (mode.equalsIgnoreCase("TEMP"))
                {
                    //Get the code value
                    short value = sensorReading.getValue();
                    //Convert the code to a Temp (Using default config function)
                    double temp = ((double)value)*0.18-89.3;
                    System.out.println(String.format("Temp: {%f} C", temp));
                }
                else if (mode.equalsIgnoreCase("EXT1") || mode.equalsIgnoreCase("EXT2"))
                {
                    //Get the code value
                    short value = sensorReading.getValue();
                    //Convert the code to a Voltage (V) (Using default config function)
                    double voltage = ((double)value) * .310 / 1024 + .310;
                    System.out.println(String.format("Voltage: {%f} V", voltage));
                }
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
