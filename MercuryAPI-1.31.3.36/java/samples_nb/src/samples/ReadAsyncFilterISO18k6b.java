/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samples;

import com.thingmagic.Iso180006b;
import com.thingmagic.ReadListener;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagData;
import com.thingmagic.TagFilter;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;
import com.thingmagic.TransportListener;
import java.util.ArrayList;

/**
 *
 * @author qvantel
 */
public class ReadAsyncFilterISO18k6b
{

    static SerialPrinter serialPrinter;
    static StringPrinter stringPrinter;
    static TransportListener currentListener;
    public static ArrayList<String> tagData = new ArrayList<String>();

    static void usage()
    {
        System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "ReadAsyncFilterISO18k6b [-v] [reader-uri] [--ant n[,n...]] \n" +
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
                {
                    System.out.printf("\n         ");
                }
                System.out.printf(" %02x", data[i]);
            }
            System.out.printf("\n");
        }
    }

    static class StringPrinter implements TransportListener
    {
        public void message(boolean tx, byte[] data, int timeout)
        {
            System.out.println((tx ? "Sending:\n" : "Receiving:\n")
                    + new String(data));
        }
    }

    public static void main(String argv[]) throws ReaderException
    {

        Reader r = null;
        int nextarg = 0;
        int[] antennaList = null;
        boolean ISO180006BTagOps = false;
        boolean trace = false;
	
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
                setTrace(r, new String[]{"on"});
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
            // Create and add tag listener
            ReadListener rl = new PrintListener();
            r.addReadListener(rl);
            r.paramSet(TMConstants.TMR_PARAM_ISO180006B_DELIMITER, Iso180006b.Delimiter.DELIMITER1);
            Iso180006b.Select filt = new Iso180006b.Select(false, Iso180006b.SelectOp.NOTEQUALS, (byte) 0, (byte) 0xFF, new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });

            SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.ISO180006B, filt, null, 1000);
            r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
             
            // search for tags in the background
            r.startReading();
            System.out.println("Do other work here");
            Thread.sleep(1000);
            System.out.println("Do other work here");
            Thread.sleep(500);
            r.stopReading();
            
            if(ISO180006BTagOps)
            {
                if(!tagData.isEmpty())
                {
                String tag = tagData.get(0); 
                System.out.println("****************ISO180006b Tag Operation********************");
                
                //Use first antenna for operation
                if (antennaList != null)
                r.paramSet("/reader/tagop/antenna", antennaList[0]);

                TagFilter filterToUse;
                filterToUse = new TagData(tag);
                              
                byte address = 0x28;
                byte num_bytes_to_read = 4, readDataWord[];
                byte writedata[] = new byte[]{(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44};
                
                // write data to a particular address location of tag
                Iso180006b.WriteData writeOp = new Iso180006b.WriteData(address, writedata);
                r.executeTagOp(writeOp, filterToUse);
                System.out.println("Write Data Successful");

                //read  data from a specified memory location works only with tag data filter
                Iso180006b.ReadData readOp = new Iso180006b.ReadData(address, num_bytes_to_read);
                readDataWord = (byte[]) r.executeTagOp(readOp, filterToUse);
                System.out.println("Read Data:");
                for (byte data : readDataWord) {
                            System.out.printf("%02x\n", data);
                        }

                // Lock the tag at the specified address
                // Uncomment Below Code to Perfrom Lock Operation
                //Iso180006b.Lock lockOp = new Iso180006b.Lock(address);
                //r.executeTagOp(lockOp, filterToUse);
                //System.out.println("Lock Tag Successful"); 
                }
                else
                {
                     throw new Exception("No ISO180006B Tags found.");
                }
            }
            
            r.removeReadListener(rl);

        }
        catch (ReaderException re)
        {
            System.out.println("ReaderException: " + re.getMessage());
        }
        catch (Exception re)
        {
            System.out.println("ReaderException: " + re.getMessage());
        }
        finally
        {
            // Shut down reader
            r.destroy();
        }
    }

    static class PrintListener implements ReadListener
    {
        public void tagRead(Reader r, TagReadData tr)
        {
            System.out.println("Background read: " + tr.toString());
            tagData.add(tr.getTag().toString().replace("EPC:", ""));
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