/**
 * Sample program that writes an EPC to a tag and 
 * demonstrates the functionality of read after write.
 */

// Import the API
package samples;
import com.thingmagic.*;

public class writetag
{
  static SerialPrinter serialPrinter;
  static StringPrinter stringPrinter;
  static TransportListener currentListener;
  private static int[] antennaList = null;
  private static Reader r = null;

  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as: "
                + "writetag [-v] reader-uri [--ant n][--rw][--filter] \n"
                + "-v Verbose: Turn on transport listener \n"
                + "reader-uri Reader URI: e.g., 'tmr:///COM1' or 'tmr://astra-2100d3' or 'tmr:///dev/ttyS0/'\n"
                + "--ant Antenna List : e.g., '--ant 1' \n"
                + "--rw Enables ReadAfterWrite functionality \n"
                + "--filter Enables filtering \n"
                + " Example: 'tmr:///COM1 --ant 1' or 'tmr:///COM1 --ant 1 --rw' or 'tmr:///COM1 --ant 1 --rw --filter'");
    System.exit(1);
  }

   public static void setTrace(Reader r, String args[])
  {
    if (args[0].toLowerCase().equals("on"))
    {
        r.addTransportListener(Reader.simpleTransportListener);
        currentListener = Reader.simpleTransportListener;
    }
    else if (currentListener != null)
    {
        r.removeTransportListener(Reader.simpleTransportListener);
    }
  }

   static class SerialPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.print(tx ? "Sending: " : "Received:");
      for (int i = 0; i < data.length; i++)
      {
        if (i > 0 && (i & 15) == 0)
          System.out.printf("\n         ");
        System.out.printf(" %02x", data[i]);
      }
      System.out.printf("\n");
    }
  }

  static class StringPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                         new String(data));
    }
  }
  public static void main(String argv[])
  {
    // Program setup
    TagFilter target = null;
    int nextarg = 0;
    boolean trace = false;
    boolean enableReadAfterWrite = false;
    boolean enableFilter = false;
    boolean enableEmbeddedReadAfterWrite = false;

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
                if(arg.equalsIgnoreCase("--rw"))
                {
                    // Enables read after write functionality
                    enableReadAfterWrite = true;
                    
                }

                else if(argv[nextarg].equalsIgnoreCase("--filter"))
                {
                    // Enables filtering 
                    enableFilter = true;
                }
                else
                {
                    System.out.println("Argument " + argv[nextarg] + " is not recognised");
                    usage();
                }
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
        
        if(enableFilter)
        {
            // This select filter matches all Gen2 tags where bits 32-48 of the EPC are 0x0123 
            target = new Gen2.Select(false, Gen2.Bank.EPC, 32, 16, new byte[] {(byte)0x01,(byte)0x23});
        }
        
        Gen2.TagData epc = new Gen2.TagData(new byte[]
           {(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xAB,
            (byte)0xCD, (byte)0xEF, (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67,
            });
        Gen2.WriteTag tagop = new Gen2.WriteTag(epc);
        r.executeTagOp(tagop, target);
        
        // Reads data from a tag memory bank after writing data to the requested memory bank without powering down of tag
        
        if(enableReadAfterWrite)
        {
            //create a tagopList with write tagop followed by read tagop
            TagOpList tagopList = new TagOpList();
            short[] readData;
            byte wordCount;
            
            //Write one word of data to USER memory and read back 8 words from EPC memory
            {

                // write data
                short writeData[] =
                {
                     (short) 0x1234
                };
                wordCount = 8;
                Gen2.WriteData wData = new Gen2.WriteData(Gen2.Bank.USER, 2, writeData);
                Gen2.ReadData rData = new Gen2.ReadData(Gen2.Bank.EPC, 0, wordCount);

                // assemble tagops into list
                tagopList.list.add(wData);
                tagopList.list.add(rData);

                // call executeTagOp with list of tagops
                readData = (short[])r.executeTagOp(tagopList, target);
                System.out.println("ReadData: ");
                for (short dt : readData)
                {
                    System.out.printf("%02x \t", dt);
                }
                System.out.println("\n");

                // enable flag enableEmbeddedReadAfterWrite to execute embedded read after write 
                if(enableEmbeddedReadAfterWrite)
                {
                    performEmbeddedOperation(target, tagopList);
                }
            }

            //clearing the list for next operation
            tagopList.list.clear();

            //Write 12 bytes(6 words) of EPC and read back 8 words from EPC memory 
            {
                Gen2.TagData epc1 = new Gen2.TagData(new byte[]
                   {(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66,
                    (byte)0x77, (byte)0x88, (byte)0x99, (byte)0xaa, (byte)0xbb, (byte)0xcc,
                    });

                wordCount = 8;

                Gen2.WriteTag wtag = new Gen2.WriteTag(epc1);
                Gen2.ReadData rData = new Gen2.ReadData(Gen2.Bank.EPC, 0, wordCount);

                // assemble tagops into list
                tagopList.list.add(wtag);
                tagopList.list.add(rData);

                // call executeTagOp with list of tagops
                readData = (short[])r.executeTagOp(tagopList, target);
                System.out.println("ReadData: ");
                for (short dt : readData)
                {
                    System.out.printf("%02x \t", dt);
                }
                System.out.println("\n");

                // enable flag enableEmbeddedReadAfterWrite to execute embedded read after write
                if(enableEmbeddedReadAfterWrite)
                {
                    performEmbeddedOperation(target, tagopList);
                }
            }
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

    public static void performEmbeddedOperation(TagFilter filter, TagOp op) throws Exception
    {
        TagReadData[] tagReads = null;
        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, filter, op, 1000);
        r.paramSet("/reader/read/plan", plan);
        tagReads = r.read(1000);
        for (TagReadData tr : tagReads)
        {
            for (byte b : tr.getData())
            {
                System.out.printf("%02x", b);
                System.out.printf("\n");
            }
        }
    }
}
