/*
 * Sample program that demonstrates how to read all gen2 memory banks data
 */
package samples;

import com.thingmagic.*;
import java.util.EnumSet;

public class Gen2ReadAllMemoryBanks
{
    
  private static Reader r = null;
  private static int[] antennaList = null;
  
  static void usage()
  {
    System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "Gen2ReadAllMemoryBanks [-v] [reader-uri] [--ant n[,n...]] \n" +
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

        byte length;
        String model = r.paramGet("/reader/version/model").toString();
        if (r.isAntDetectEnabled(antennaList))
        {
            System.out.println("Module doesn't has antenna detection support, please provide antenna list");
            r.destroy();
            usage();
        }
  
        if ("M6e".equalsIgnoreCase(model)
                || "M6e PRC".equalsIgnoreCase(model)
                || "M6e JIC".equalsIgnoreCase(model)
                || "M6e Micro".equalsIgnoreCase(model)
                || "Mercury6".equalsIgnoreCase(model)
                || "Sargas".equalsIgnoreCase(model)
                || "Astra-EX".equalsIgnoreCase(model))
        {
            // Specifying the readLength = 0 will return full TID for any tag read in case of M6e varients, M6, Astra-EX and Sargas readers.
            length = 0;
        }
        else
        {
            length = 2;
        }
        
        Gen2ReadAllMemoryBanks program =new Gen2ReadAllMemoryBanks();
        program.performWriteOperation();
        
        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
        
        // Read tags
        tagReads = r.read(500);
        if(tagReads.length == 0)
        {
            System.out.println("No tags found"); 
        }
        
        TagFilter filter = new TagData(tagReads[0].epcString());
        System.out.println("Perform embedded and standalone tag operation - read only user memory without filter");
        TagOp op = new Gen2.ReadData(Gen2.Bank.USER, 0, length);
        program.performReadAllMemOperation(filter, op);
       
        EnumSet<Gen2.Bank> memBanks = EnumSet.of
        (
                Gen2.Bank.USER,
                Gen2.Bank.GEN2BANKUSERENABLED, Gen2.Bank.GEN2BANKRESERVEDENABLED,
                Gen2.Bank.GEN2BANKEPCENABLED, Gen2.Bank.GEN2BANKTIDENABLED
        );
      
       
        System.out.println("Perform embedded and standalone tag operation - read user memory, reserved memory, tid memory and epc memory without filter");
        op = null;
        op = new Gen2.ReadData(memBanks, 0, length);
        program.performReadAllMemOperation(null, op);

        System.out.println("Perform embedded and standalone tag operation - read only user memory with filter");
        op = null;
        op = new Gen2.ReadData(Gen2.Bank.USER, 0, length);
        program.performReadAllMemOperation(filter, op);

        memBanks = EnumSet.of(
                Gen2.Bank.USER,
                Gen2.Bank.GEN2BANKUSERENABLED, Gen2.Bank.GEN2BANKRESERVEDENABLED
        );
        System.out.println("Perform embedded and standalone tag operation - read user memory, reserved memory with filter");
        op = null;
        op = new Gen2.ReadData(memBanks, 0, length);
        program.performReadAllMemOperation(filter, op);

        System.out.println("Perform embedded and standalone tag operation - read user memory, reserved memory and tid memory with filter");
        memBanks = EnumSet.of(
                Gen2.Bank.USER,
                Gen2.Bank.GEN2BANKUSERENABLED, Gen2.Bank.GEN2BANKRESERVEDENABLED,
                Gen2.Bank.GEN2BANKTIDENABLED
        );
        op = null;
        op = new Gen2.ReadData(memBanks, 0, length);
        program.performReadAllMemOperation(filter, op);
        
        System.out.println("Perform embedded and standalone tag operation - read user memory, reserved memory, tid memory and epc memory with filter");
        memBanks = EnumSet.of(
                Gen2.Bank.USER,
                Gen2.Bank.GEN2BANKUSERENABLED, Gen2.Bank.GEN2BANKRESERVEDENABLED,
                Gen2.Bank.GEN2BANKEPCENABLED, Gen2.Bank.GEN2BANKTIDENABLED
        );

        op = null;
        op = new Gen2.ReadData(memBanks, 0, length);
        program.performReadAllMemOperation(filter, op);
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
  
 private void performReadAllMemOperation(TagFilter filter, TagOp op) throws ReaderException
    {
        TagReadData[] tagReads = null;
        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, filter, op, 1000);
        r.paramSet("/reader/read/plan", plan);
        System.out.println("Embedded tag operation - ");
        // Read tags
        tagReads = r.read(500);
        if(tagReads.length == 0)
        {
            System.out.println("No tags found"); 
        }
        else
        {
            for (TagReadData tr : tagReads)
            {
                System.out.println(tr.toString());
                if (0 < tr.getData().length)
                {
                    System.out.println(" Embedded read data: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                    System.out.println(" User memory: " + ReaderUtil.byteArrayToHexString(tr.getUserMemData()));
                    System.out.println(" Reserved memory: " + ReaderUtil.byteArrayToHexString(tr.getReservedMemData()));
                    System.out.println(" Tid memory: " + ReaderUtil.byteArrayToHexString(tr.getTIDMemData()));
                    System.out.println(" EPC memory: " + ReaderUtil.byteArrayToHexString(tr.getEPCMemData()));
                }
                System.out.println(" Embedded read data length:" + tr.getData().length);
            }

            System.out.println("Standalone tag operation - ");
            //Use first antenna for operation
            if (antennaList != null)
            {
                r.paramSet("/reader/tagop/antenna", antennaList[0]);
            }

            short[] data = (short[]) r.executeTagOp(op, filter);
            // Print tag reads
            if (0 < data.length)
            {
                System.out.println(" Standalone read data:"
                        + ReaderUtil.byteArrayToHexString(ReaderUtil.convertShortArraytoByteArray(data)));
                System.out.println(" Standalone read data length:" + data.length);
            }
            data = null;
        }
    }
 
  private void performWriteOperation() throws ReaderException
  {
      //Use first antenna for operation
      if (antennaList != null)
      {
          r.paramSet("/reader/tagop/antenna", antennaList[0]);
      }

      Gen2.TagData epc = new Gen2.TagData(new byte[]
      {
          (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xAB,
          (byte) 0xCD, (byte) 0xEF, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67,
      });
     
      System.out.println("Write on epc mem: " + epc.epcString());
      Gen2.WriteTag tagop = new Gen2.WriteTag(epc);
      r.executeTagOp(tagop, null);

      short[] data = new short[] { 0x1234, 0x5678 };
      
      System.out.println("Write on reserved mem: " + 
              ReaderUtil.byteArrayToHexString(ReaderUtil.convertShortArraytoByteArray(data)));
      Gen2.BlockWrite blockwrite = new Gen2.BlockWrite(Gen2.Bank.RESERVED, 0, (byte) data.length, data);
      r.executeTagOp(blockwrite, null);

      data = null;
      data = new short[] {(short) 0xFFF1, (short) 0x1122};
      
      System.out.println("Write on user mem: " + 
              ReaderUtil.byteArrayToHexString(ReaderUtil.convertShortArraytoByteArray(data)));
      blockwrite = new Gen2.BlockWrite(Gen2.Bank.USER, 0, (byte) data.length, data);
      r.executeTagOp(blockwrite, null);

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
