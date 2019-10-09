/**
 * Sample program that used for Standalone and Embedded
 * DenatronIAVCustomTagOpearations and prints the data length.
 */
// Import the API
package samples;

import com.thingmagic.*;

public class DenatranIAVCustomTagOps
{

    static void usage()
    {
        System.out.printf("Usage: Please provide valid arguments, such as:\n"
                + "DenatranIAVCustomTagops [-v] [reader-uri] [--ant n[,n...]] \n" +
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

    public static void main(String argv[])
    {
        // Program setup
        Reader r = null;
        int nextarg = 0;
        boolean trace = false;
        TagFilter filter;
        Gen2.Select selectfilter;
        int[] antenna = null;
        byte payload = (byte) 0x80;
        byte GOPAOBUAuth = (byte) 0x80;
        byte GOPAOBUAuthFullPass = (byte) 0x81;
        byte GOPAOBUAuthFullPass1 = (byte) 0x82;
        SimpleReadPlan rp;
        TagReadData[] tagReads;
        byte readPtr = (byte) 0xFFFF;
        byte wordAddress = (byte) 0xFFFF;
        byte word = (byte) 0xFFFF;
        byte readSecPtr = (byte) 0x0000;

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
                    if (antenna != null)
                    {
                        System.out.println("Duplicate argument: --ant specified more than once");
                        usage();
                    }
                    antenna = parseAntennaList(argv, nextarg);
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
            
            if (r.isAntDetectEnabled(antenna))
            {
                System.out.println("Module doesn't has antenna detection support, please provide antenna list");
                r.destroy();
                usage();
            }
            
            rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, null, 1000);
            r.paramSet("/reader/read/plan", rp);
            
             //Use first antenna for tag operation
            if (antenna != null)
                r.paramSet("/reader/tagop/antenna", antenna[0]);
            
            /* get the protocol extension*/
            System.out.println("Get Protocol Extension: " + (Gen2.ProtocolExtension)r.paramGet(TMConstants.TMR_PARAM_GEN2_PROTOCOLEXTENSION));

            /* set the tag encoding to FM0 */
            System.out.println("setting the tagencoding to FM0");
            r.paramSet(TMConstants.TMR_PARAM_GEN2_TAGENCODING, Gen2.TagEncoding.FM0);

            /*set the BLF value to 320KHZ */
            System.out.println("setting the BLF to 320KHZ");
            r.paramSet(TMConstants.TMR_PARAM_GEN2_BLF, Gen2.LinkFrequency.LINK320KHZ);

            /* set the session to S0 */
            System.out.println("setting the session to S0");
            r.paramSet(TMConstants.TMR_PARAM_GEN2_SESSION, Gen2.Session.S0);

            /* set the target to AB */
            System.out.println("setting the target to AB");
            r.paramSet(TMConstants.TMR_PARAM_GEN2_TARGET, Gen2.Target.AB);

            /* set the tari to 6.25us */
            System.out.println("setting the tari to 6.25");
            r.paramSet(TMConstants.TMR_PARAM_GEN2_TARI, Gen2.Tari.TARI_6_25US);

            /* set the Q to be static */
            System.out.println("setting Q as static");
            r.paramSet(TMConstants.TMR_PARAM_GEN2_Q, new Gen2.StaticQ(0));
            
            
            /* filter settings */
            tagReads = r.read(1000);
            if(tagReads.length == 0)
            {
                System.out.println("Error: No tags found");
            }
            else
            {
                for (TagReadData tr : tagReads)
                {
                    System.out.println("EPC: " + tr.epcString());
                }
                
                /* Tag Data filter*/
                filter = new TagData(tagReads[0].epcString());
                
                /*Select filter */
                byte[] mask = ReaderUtil.hexStringToByteArray(tagReads[0].epcString());
                selectfilter = new Gen2.Select(false, Gen2.Bank.EPC, 32, (tagReads[0].epcString().length())*4, mask);

                /** Set the token */
                byte[] token = {(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF,(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF};
                /** Set the writeCredentials */
                byte[] writeCredentials = {(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF,(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF,(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF,(byte)0xDE,(byte)0xAD,(byte)0xBE,(byte)0xEF};
                byte[] tagId = {(byte)0x80,(byte)0x10,(byte)0x00,(byte)0x12,(byte)0x34,(byte)0xAD,(byte)0xBD,(byte)0xC0};
                byte[] writeSecData = {(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x66};
                byte[] writeSecCredentials = {(byte)0x35 ,(byte)0x49,(byte)0x87,(byte)0xbd,(byte)0xb2,(byte)0xab,(byte)0xd2,(byte)0x7c,(byte)0x2e,(byte)0x34,(byte)0x78,(byte)0x8b,(byte)0xf2,(byte)0xf7,(byte)0x0b,(byte)0xa2};

                /* Standalonetagoperations */
                try//scenario1-Gen2ActivateSecureMode-with out filter
                {
                    System.out.println("Standalone Tagop :- Authenticate Secure Mode with out filter");
                    Gen2.Denatran.IAV.ActivateSecureMode securemode = new Gen2.Denatran.IAV.ActivateSecureMode(payload);
                    byte[] activateSecureModedata = (byte[]) r.executeTagOp(securemode, null);
                    System.out.println("Activate secure mode response data : " + ReaderUtil.byteArrayToHexString(activateSecureModedata));
                    System.out.println("Data length : " + activateSecureModedata.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario2-Gen2ActivateSecureMode-with Tagdata filter
                {
                    System.out.println("Standalone Tagop :- Authenticate Secure Mode with Tagdata filter");
                    Gen2.Denatran.IAV.ActivateSecureMode securemode = new Gen2.Denatran.IAV.ActivateSecureMode(payload);
                    byte[] activateSecureModedata = (byte[]) r.executeTagOp(securemode, filter);
                    System.out.println("Activate secure mode response data : " + ReaderUtil.byteArrayToHexString(activateSecureModedata));
                    System.out.println("Data length : " + activateSecureModedata.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario3-Gen2ActivateSecureMode-with Gen2select filter
                {
                    System.out.println("Standalone Tagop :- Authenticate Secure Mode with Gen2select filter");
                    Gen2.Denatran.IAV.ActivateSecureMode securemode = new Gen2.Denatran.IAV.ActivateSecureMode(payload);
                    byte[] activateSecureModedata = (byte[]) r.executeTagOp(securemode, selectfilter);
                    System.out.println("Activate secure mode response data : " + ReaderUtil.byteArrayToHexString(activateSecureModedata));
                    System.out.println("Data length : " + activateSecureModedata.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-Gen2AuthenticateOBU-With out filter
                {
                    System.out.println("Standalone Tagop :- Authenticate OBU with out filter");
                    Gen2.Denatran.IAV.AuthenticateOBU authenticateOBU = new Gen2.Denatran.IAV.AuthenticateOBU(payload);
                    byte[] AuthenticateOBU = (byte[]) r.executeTagOp(authenticateOBU, null);
                    System.out.println("Authenticate OBU response data : " + ReaderUtil.byteArrayToHexString(AuthenticateOBU));
                    System.out.println("Data length : " + AuthenticateOBU.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario2-Gen2AuthenticateOBU-With TagData filter
                {
                    System.out.println("Standalone Tagop :- Authenticate OBU with TagData filter");
                    Gen2.Denatran.IAV.AuthenticateOBU authenticateOBU = new Gen2.Denatran.IAV.AuthenticateOBU(payload);
                    byte[] AuthenticateOBU = (byte[]) r.executeTagOp(authenticateOBU, filter);
                    System.out.println("Authenticate OBU response data : " + ReaderUtil.byteArrayToHexString(AuthenticateOBU));
                    System.out.println("Data length : " + AuthenticateOBU.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario3-Gen2AuthenticateOBU-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- Authenticate OBU with Gen2select filter");
                    Gen2.Denatran.IAV.AuthenticateOBU authenticateOBU = new Gen2.Denatran.IAV.AuthenticateOBU(payload);
                    byte[] AuthenticateOBU = (byte[]) r.executeTagOp(authenticateOBU, selectfilter);
                    System.out.println("Authenticate OBU response data : " + ReaderUtil.byteArrayToHexString(AuthenticateOBU));
                    System.out.println("Data length : " + AuthenticateOBU.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-Gen2ActivateSiniavMode-With out filter
                {
                    System.out.println("Standalone Tagop :- Activate Siniav Mode With out filter");
                    Gen2.Denatran.IAV.ActivateSiniavMode siniavMode = new Gen2.Denatran.IAV.ActivateSiniavMode(payload, token);
                    byte[] ActivateSiniavMode = (byte[]) r.executeTagOp(siniavMode, null);
                    System.out.println("Activate Siniav mode response data : " + ReaderUtil.byteArrayToHexString(ActivateSiniavMode));
                    System.out.println("Data length : " + ActivateSiniavMode.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario2-Gen2ActivateSiniavMode-With TagData filter
                {
                    System.out.println("Standalone Tagop :- Activate Siniav Mode With TagData filter");
                    Gen2.Denatran.IAV.ActivateSiniavMode siniavMode = new Gen2.Denatran.IAV.ActivateSiniavMode(payload, token);
                    byte[] ActivateSiniavMode = (byte[]) r.executeTagOp(siniavMode, filter);
                    System.out.println("Activate Siniav mode response data : " + ReaderUtil.byteArrayToHexString(ActivateSiniavMode));
                    System.out.println("Data length : " + ActivateSiniavMode.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario3-Gen2ActivateSiniavMode-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- Activate Siniav Mode With Gen2select filter");
                    Gen2.Denatran.IAV.ActivateSiniavMode siniavMode = new Gen2.Denatran.IAV.ActivateSiniavMode(payload, token);
                    byte[] ActivateSiniavMode = (byte[]) r.executeTagOp(siniavMode, selectfilter);
                    System.out.println("Activate Siniav mode response data : " + ReaderUtil.byteArrayToHexString(ActivateSiniavMode));
                    System.out.println("Data length : " + ActivateSiniavMode.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUAuthID-With out filter
                {
                    System.out.println("Standalone Tagop :- OBU AuthID With out filter");
                    Gen2.Denatran.IAV.OBUAuthID obuAuthID = new Gen2.Denatran.IAV.OBUAuthID(payload);
                    byte[] OBUAuthID = (byte[]) r.executeTagOp(obuAuthID, null);
                    System.out.println("OBU AuthID response data : " + ReaderUtil.byteArrayToHexString(OBUAuthID));
                    System.out.println("Data length : " + OBUAuthID.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-OBUAuthID-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU AuthID With TagData filter");
                    Gen2.Denatran.IAV.OBUAuthID obuAuthID = new Gen2.Denatran.IAV.OBUAuthID(payload);
                    byte[] OBUAuthID = (byte[]) r.executeTagOp(obuAuthID, filter);
                    System.out.println("OBU AuthID response data : " + ReaderUtil.byteArrayToHexString(OBUAuthID));
                    System.out.println("Data length : " + OBUAuthID.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-OBUAuthID-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU AuthID With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthID obuAuthID = new Gen2.Denatran.IAV.OBUAuthID(payload);
                    byte[] OBUAuthID = (byte[]) r.executeTagOp(obuAuthID, selectfilter);
                    System.out.println("OBU AuthID response data : " + ReaderUtil.byteArrayToHexString(OBUAuthID));
                    System.out.println("Data length : " + OBUAuthID.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUAuthFullPass1-With out filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass1 With out filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass1 authFullPass1 = new Gen2.Denatran.IAV.OBUAuthFullPass1(payload);
                    byte[] OBUAuthFullPass1 = (byte[]) r.executeTagOp(authFullPass1, null);
                    System.out.println("OBU Auth FullPass1 response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass1));
                    System.out.println("Data length : " + OBUAuthFullPass1.length);
                }

                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-OBUAuthFullPass1-With TagData Filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass1 With TagData Filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass1 authFullPass1 = new Gen2.Denatran.IAV.OBUAuthFullPass1(payload);
                    byte[] OBUAuthFullPass1 = (byte[]) r.executeTagOp(authFullPass1, filter);
                    System.out.println("OBU Auth FullPass1 response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass1));
                    System.out.println("Data length : " + OBUAuthFullPass1.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-OBUAuthFullPass1-With Gen2select Filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass1 With Gen2select Filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass1 authFullPass1 = new Gen2.Denatran.IAV.OBUAuthFullPass1(payload);
                    byte[] OBUAuthFullPass1 = (byte[]) r.executeTagOp(authFullPass1, selectfilter);
                    System.out.println("OBU Auth FullPass1 response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass1));
                    System.out.println("Data length : " + OBUAuthFullPass1.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUAuthFullPass2-With out filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass2 With out filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass2 authFullPass2 = new Gen2.Denatran.IAV.OBUAuthFullPass2(payload);
                    byte[] OBUAuthFullPass2 = (byte[]) r.executeTagOp(authFullPass2, null);
                    System.out.println("OBU Auth FullPass2 response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass2));
                    System.out.println("Data length : " + OBUAuthFullPass2.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-OBUAuthFullPass2-With Tagdata filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass2 With Tagdata filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass2 authFullPass2 = new Gen2.Denatran.IAV.OBUAuthFullPass2(payload);
                    byte[] OBUAuthFullPass2 = (byte[]) r.executeTagOp(authFullPass2, filter);
                    System.out.println("OBU Auth FullPass2 response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass2));
                    System.out.println("Data length : " + OBUAuthFullPass2.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-OBUAuthFullPass2-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass2 With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass2 authFullPass2 = new Gen2.Denatran.IAV.OBUAuthFullPass2(payload);
                    byte[] OBUAuthFullPass2 = (byte[]) r.executeTagOp(authFullPass2, selectfilter);
                    System.out.println("OBU Auth FullPass2 response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass2));
                    System.out.println("Data length : " + OBUAuthFullPass2.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUReadFromMemMap-With out filter
                {
                    System.out.println("Standalone Tagop :- OBU ReadFromMemMap With out filter");
                    Gen2.Denatran.IAV.OBUReadFromMemMap readFromMemMap = new Gen2.Denatran.IAV.OBUReadFromMemMap(payload, readPtr);
                    byte[] OBUReadFromMemMap = (byte[]) r.executeTagOp(readFromMemMap, null);
                    System.out.println("ReadFromMemMap response data : " + ReaderUtil.byteArrayToHexString(OBUReadFromMemMap));
                    System.out.println("Data length : " + OBUReadFromMemMap.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-OBUReadFromMemMap-With Tagdata filter
                {
                    System.out.println("Standalone Tagop :- OBU ReadFromMemMap With Tagdata filter");
                    Gen2.Denatran.IAV.OBUReadFromMemMap readFromMemMap = new Gen2.Denatran.IAV.OBUReadFromMemMap(payload, readPtr);
                    byte[] OBUReadFromMemMap = (byte[]) r.executeTagOp(readFromMemMap, filter);
                    System.out.println("ReadFromMemMap response data : " + ReaderUtil.byteArrayToHexString(OBUReadFromMemMap));
                    System.out.println("Data length : " + OBUReadFromMemMap.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-OBUReadFromMemMap-With Gen2.select filter
                {
                    System.out.println("Standalone Tagop :- OBU ReadFromMemMap With Gen2.select filter");
                    Gen2.Denatran.IAV.OBUReadFromMemMap readFromMemMap = new Gen2.Denatran.IAV.OBUReadFromMemMap(payload, readPtr);
                    byte[] OBUReadFromMemMap = (byte[]) r.executeTagOp(readFromMemMap, selectfilter);
                    System.out.println("ReadFromMemMap response data : " + ReaderUtil.byteArrayToHexString(OBUReadFromMemMap));
                    System.out.println("Data length : " + OBUReadFromMemMap.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUWriteToMemMap-With out filter
                {
                    System.out.println("Standalone Tagop :- OBU WriteToMemMap With out filter");
                    Gen2.Denatran.IAV.OBUWriteToMemMap writeToMemMap = new Gen2.Denatran.IAV.OBUWriteToMemMap(payload, wordAddress, word, tagId, writeCredentials);
                    byte[] OBUWriteToMemMap = (byte[]) r.executeTagOp(writeToMemMap, null);
                    System.out.println("WriteToMemMap response data : " + ReaderUtil.byteArrayToHexString(OBUWriteToMemMap));
                    System.out.println("Data length : " + OBUWriteToMemMap.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-OBUWriteToMemMap-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU WriteToMemMap With TagData filter");
                    Gen2.Denatran.IAV.OBUWriteToMemMap writeToMemMap = new Gen2.Denatran.IAV.OBUWriteToMemMap(payload, wordAddress, word, tagId, writeCredentials);
                    byte[] OBUWriteToMemMap = (byte[]) r.executeTagOp(writeToMemMap, filter);
                    System.out.println("WriteToMemMap response data : " + ReaderUtil.byteArrayToHexString(OBUWriteToMemMap));
                    System.out.println("Data length : " + OBUWriteToMemMap.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-OBUWriteToMemMap-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU WriteToMemMap With Gen2select filter");
                    Gen2.Denatran.IAV.OBUWriteToMemMap writeToMemMap = new Gen2.Denatran.IAV.OBUWriteToMemMap(payload, wordAddress, word, tagId, writeCredentials);
                    byte[] OBUWriteToMemMap = (byte[]) r.executeTagOp(writeToMemMap, selectfilter);
                    System.out.println("WriteToMemMap response data : " + ReaderUtil.byteArrayToHexString(OBUWriteToMemMap));
                    System.out.println("Data length : " + OBUWriteToMemMap.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-IAV ReadSec-With out filter
                {
                    System.out.println("Standalone Tagop :- IAV ReadSec With out filter");
                    Gen2.Denatran.IAV.ReadSec readSec = new Gen2.Denatran.IAV.ReadSec(payload, readSecPtr);
                    byte[] IAVReadSec = (byte[]) r.executeTagOp(readSec, null);
                    System.out.println("ReadSec response data : " + ReaderUtil.byteArrayToHexString(IAVReadSec));
                    System.out.println("Data length : " + IAVReadSec.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-IAV ReadSec-With Tagdata filter
                {
                    System.out.println("Standalone Tagop :- IAV ReadSec With Tagdata filter");
                    Gen2.Denatran.IAV.ReadSec readSec = new Gen2.Denatran.IAV.ReadSec(payload, readSecPtr);
                    byte[] IAVReadSec = (byte[]) r.executeTagOp(readSec, filter);
                    System.out.println("ReadSec response data : " + ReaderUtil.byteArrayToHexString(IAVReadSec));
                    System.out.println("Data length : " + IAVReadSec.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-IAV ReadSec-With Gen2.select filter
                {
                    System.out.println("Standalone Tagop :- IAV ReadSec With Gen2.select filter");
                    Gen2.Denatran.IAV.ReadSec readSec = new Gen2.Denatran.IAV.ReadSec(payload, readSecPtr);
                    byte[] IAVReadSec = (byte[]) r.executeTagOp(readSec, selectfilter);
                    System.out.println("ReadSec response data : " + ReaderUtil.byteArrayToHexString(IAVReadSec));
                    System.out.println("Data length : " + IAVReadSec.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-IAV WriteSec-With out filter
                {
                    System.out.println("Standalone Tagop :- IAV WriteSec With out filter");
                    Gen2.Denatran.IAV.WriteSec writeSec = new Gen2.Denatran.IAV.WriteSec(payload, writeSecData, writeSecCredentials);
                    byte[] IAVWriteSec = (byte[]) r.executeTagOp(writeSec, null);
                    System.out.println("WriteSec response data : " + ReaderUtil.byteArrayToHexString(IAVWriteSec));
                    System.out.println("Data length : " + IAVWriteSec.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-IAV WriteSec-With TagData filter
                {
                    System.out.println("Standalone Tagop :- IAV WriteSec With TagData filter");
                    Gen2.Denatran.IAV.WriteSec writeSec = new Gen2.Denatran.IAV.WriteSec(payload, writeSecData, writeSecCredentials);
                    byte[] IAVWriteSec = (byte[]) r.executeTagOp(writeSec, filter);
                    System.out.println("WriteSec response data : " + ReaderUtil.byteArrayToHexString(IAVWriteSec));
                    System.out.println("Data length : " + IAVWriteSec.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-IAV WriteSec-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- IAV WriteSec With Gen2select filter");
                    Gen2.Denatran.IAV.WriteSec writeSec = new Gen2.Denatran.IAV.WriteSec(payload, writeSecData, writeSecCredentials);
                    byte[] IAVWriteSec = (byte[]) r.executeTagOp(writeSec, selectfilter);
                    System.out.println("WriteSec response data : " + ReaderUtil.byteArrayToHexString(IAVWriteSec));
                    System.out.println("Data length : " + IAVWriteSec.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUAuthFullPass-With out filter
                {
                    System.out.println("Standalone Tagop :- OBU Auth FullPass With out filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass authFullPass = new Gen2.Denatran.IAV.OBUAuthFullPass(payload);
                    byte[] OBUAuthFullPass = (byte[]) r.executeTagOp(authFullPass, null);
                    System.out.println("OBU Auth FullPass response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass));
                    System.out.println("Data length : " + OBUAuthFullPass.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-authFullPass-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU authFullPass With TagData filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass authFullPass = new Gen2.Denatran.IAV.OBUAuthFullPass(payload);
                    byte[] OBUAuthFullPass = (byte[]) r.executeTagOp(authFullPass, filter);
                    System.out.println("OBU Auth FullPass response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass));
                    System.out.println("Data length : " + OBUAuthFullPass.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-authFullPass-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU authFullPass With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass authFullPass = new Gen2.Denatran.IAV.OBUAuthFullPass(payload);
                    byte[] OBUAuthFullPass = (byte[]) r.executeTagOp(authFullPass, selectfilter);
                    System.out.println("OBU Auth FullPass response data : " + ReaderUtil.byteArrayToHexString(OBUAuthFullPass));
                    System.out.println("Data length : " + OBUAuthFullPass.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetTokenId-With out filter
                {
                    System.out.println("Standalone Tagop :- GetTokenId With out filter");
                    Gen2.Denatran.IAV.GetTokenId tokenid = new Gen2.Denatran.IAV.GetTokenId();
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, null);
                    System.out.println("OBU Auth getTokenid response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-GetTokenId-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU GetTokenId With TagData filter");
                    Gen2.Denatran.IAV.GetTokenId tokenid = new Gen2.Denatran.IAV.GetTokenId();
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, filter);
                    System.out.println("OBU Auth getTokenid response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-GetTokenId-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU GetTokenId With Gen2select filter");
                    Gen2.Denatran.IAV.GetTokenId tokenid = new Gen2.Denatran.IAV.GetTokenId();
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, selectfilter);
                    System.out.println("OBU Auth getTokenid response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                //scenario1-GetG0PAOBUAuth-With out filter
                try 
                {
                    System.out.println("Standalone Tagop :- GetG0PAOBUAuth With out filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuth);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, null);
                    System.out.println("OBU Auth getG0PAOBUAuth response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-GetG0PAOBUAuth-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU GetG0PAOBUAuth With TagData filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuth);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, filter);
                    System.out.println("OBU Auth getG0PAOBUAuth response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario3-GetG0PAOBUAuth-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU GetG0PAOBUAuth With Gen2select filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuth);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, selectfilter);
                    System.out.println("OBU Auth getG0PAOBUAuth response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetAuthFullPass-With out filter
                {
                    System.out.println("Standalone Tagop :- GetAuthFullPass With out filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, null);
                    System.out.println("OBU Auth getAuthFullPass response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario2-GetG0PAOBUAuthFullPass-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU GetAuthFullPass With TagData filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, filter);
                    System.out.println("OBU Auth getAuthFullPass response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario3-GetG0PAOBUAuthID-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU GetG0PAOBUAuthID With Gen2select filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, selectfilter);
                    System.out.println("OBU Auth getG0PAOBUAuthID response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetG0PAOBUAuthFullPass1-With out filter
                {
                    System.out.println("Standalone Tagop :- GetAuthFullPass With out filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass1);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, null);
                    System.out.println("OBU Auth getAuthFullPass1 response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario2-GetG0PAOBUAuthFullPass1-With TagData filter
                {
                    System.out.println("Standalone Tagop :- OBU GetAuthFullPass1 With TagData filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass1);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, filter);
                    System.out.println("OBU Auth getAuthFullPass1 response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario3-GetG0PAOBUAuthFullPass1-With Gen2select filter
                {
                    System.out.println("Standalone Tagop :- OBU GetG0PAOBUAuthFullPass1 With Gen2select filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass1);
                    byte[] getTokenid = (byte[]) r.executeTagOp(tokenid, selectfilter);
                    System.out.println("OBU Auth getAuthenId response data : " + ReaderUtil.byteArrayToHexString(getTokenid));
                    System.out.println("Data length : " + getTokenid.length);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                /* Embedded tagoperations */
                try//scenario1-Embedded ActivateSecureMode-With out filter
                {
                    System.out.println("Embedded Tagop :- Activate Secure Mode with out filter");
                    Gen2.Denatran.IAV.ActivateSecureMode securemode = new Gen2.Denatran.IAV.ActivateSecureMode(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, securemode, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-Embedded ActivateSecureMode-With TagData filter
                {
                    System.out.println("Embedded Tagop :- Activate Secure Mode with TagData filter");
                    Gen2.Denatran.IAV.ActivateSecureMode securemode = new Gen2.Denatran.IAV.ActivateSecureMode(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, securemode, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-Embedded ActivateSecureMode-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- Activate Secure Mode with  Gen2select filter");
                    Gen2.Denatran.IAV.ActivateSecureMode securemode = new Gen2.Denatran.IAV.ActivateSecureMode(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, securemode, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EmbAuthenticateOBU -With out filter
                {
                    System.out.println("Embedded Tagop :- Emb Authenticate OBU With out filter");
                    Gen2.Denatran.IAV.AuthenticateOBU authenticateOBU = new Gen2.Denatran.IAV.AuthenticateOBU(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, authenticateOBU, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EmbAuthenticateOBU -With TagData filter
                {
                    System.out.println("Embedded Tagop :- Emb Authenticate OBU With TagData filter");
                    Gen2.Denatran.IAV.AuthenticateOBU authenticateOBU = new Gen2.Denatran.IAV.AuthenticateOBU(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, authenticateOBU, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EmbAuthenticateOBU -With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- Emb Authenticate OBU With Gen2select filter");
                    Gen2.Denatran.IAV.AuthenticateOBU authenticateOBU = new Gen2.Denatran.IAV.AuthenticateOBU(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, authenticateOBU, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EmbActivateSiniavMode-With out filter
                {
                    System.out.println("Embedded Tagop :- Emb Activate Siniav Mode With out filter ");
                    Gen2.Denatran.IAV.ActivateSiniavMode siniavMode = new Gen2.Denatran.IAV.ActivateSiniavMode(payload, token);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, siniavMode, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EmbActivateSiniavMode-With TagData filter
                {
                    System.out.println("Embedded Tagop :- Emb Activate Siniav Mode With TagData filter ");
                    Gen2.Denatran.IAV.ActivateSiniavMode siniavMode = new Gen2.Denatran.IAV.ActivateSiniavMode(payload, token);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, siniavMode, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EmbActivateSiniavMode-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- Emb Activate Siniav Mode With Gen2select filter");
                    Gen2.Denatran.IAV.ActivateSiniavMode siniavMode = new Gen2.Denatran.IAV.ActivateSiniavMode(payload, token);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, siniavMode, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EmbOBUAuthID-With out filter
                {
                    System.out.println("Embedded Tagop :- Emb OBUAuthID With out filter");
                    Gen2.Denatran.IAV.OBUAuthID authID = new Gen2.Denatran.IAV.OBUAuthID(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, authID, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EmbOBUAuthID-With TagData filter
                {
                    System.out.println("Embedded Tagop :- Emb OBUAuthID With TagData filter");
                    Gen2.Denatran.IAV.OBUAuthID authID = new Gen2.Denatran.IAV.OBUAuthID(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, authID, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EmbOBUAuthID-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- Emb OBUAuthID With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthID authID = new Gen2.Denatran.IAV.OBUAuthID(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, authID, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EmbOBUAuthFullPass1-With out filter
                {
                    System.out.println("Embedded Tagop :- OBUAuthFullPass1 With out filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass1 authFullPass1 = new Gen2.Denatran.IAV.OBUAuthFullPass1(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, authFullPass1, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EmbOBUAuthFullPass1-With TagData filter
                {
                    System.out.println("Embedded Tagop :- OBUAuthFullPass1 With TagData filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass1 authFullPass1 = new Gen2.Denatran.IAV.OBUAuthFullPass1(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, authFullPass1, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EmbOBUAuthFullPass1-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- OBUAuthFullPass1 With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass1 authFullPass1 = new Gen2.Denatran.IAV.OBUAuthFullPass1(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, authFullPass1, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EmbOBUAuthFullPass2-With out filter
                {
                    System.out.println("Embedded Tagop :- OBUAuthFullPass2 With out filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass2 authFullPass2 = new Gen2.Denatran.IAV.OBUAuthFullPass2(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, authFullPass2, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EmbOBUAuthFullPass2-With TagData filter
                {
                    System.out.println("Embedded Tagop :- OBUAuthFullPass2 With TagData filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass2 authFullPass2 = new Gen2.Denatran.IAV.OBUAuthFullPass2(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, authFullPass2, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EmbOBUAuthFullPass2-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- OBUAuthFullPass2 With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass2 authFullPass2 = new Gen2.Denatran.IAV.OBUAuthFullPass2(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, authFullPass2, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-OBUReadFromMemMap-With out filter
                {
                    System.out.println("Embedded Tagop :- OBU ReadFromMemMap With out filter");
                    Gen2.Denatran.IAV.OBUReadFromMemMap readFromMemMap = new Gen2.Denatran.IAV.OBUReadFromMemMap(payload, readPtr);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, readFromMemMap, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-OBUReadFromMemMap-With TagData filter
                {
                    System.out.println("Embedded Tagop :- OBU ReadFromMemMap With TagData filter");
                    Gen2.Denatran.IAV.OBUReadFromMemMap readFromMemMap = new Gen2.Denatran.IAV.OBUReadFromMemMap(payload, readPtr);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, readFromMemMap, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-OBUReadFromMemMap-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- OBU ReadFromMemMap With Gen2select filter");
                    Gen2.Denatran.IAV.OBUReadFromMemMap readFromMemMap = new Gen2.Denatran.IAV.OBUReadFromMemMap(payload, readPtr);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, readFromMemMap, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EmbOBUWriteToMemMap-With out filter
                {
                    System.out.println("Embedded Tagop :- OBU WriteToMemMap With out filter");
                    Gen2.Denatran.IAV.OBUWriteToMemMap writeToMemMap = new Gen2.Denatran.IAV.OBUWriteToMemMap(payload, wordAddress, word, tagId, writeCredentials);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, writeToMemMap, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EmbOBUWriteToMemMap-With TagData filter
                {
                    System.out.println("Embedded Tagop :- OBU WriteToMemMap With TagData filter");
                    Gen2.Denatran.IAV.OBUWriteToMemMap writeToMemMap = new Gen2.Denatran.IAV.OBUWriteToMemMap(payload, wordAddress, word, tagId, writeCredentials);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, writeToMemMap, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EmbOBUWriteToMemMap-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- OBU WriteToMemMap With Gen2select filter");
                    Gen2.Denatran.IAV.OBUWriteToMemMap writeToMemMap = new Gen2.Denatran.IAV.OBUWriteToMemMap(payload, wordAddress, word, tagId, writeCredentials);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, writeToMemMap, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-EMB ReadSec-With out filter
                {
                    System.out.println("Embedded Tagop :- IAV ReadSec With out filter");
                    Gen2.Denatran.IAV.ReadSec readSec = new Gen2.Denatran.IAV.ReadSec(payload, readSecPtr);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, readSec, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-EMB ReadSec-With TagData filter
                {
                    System.out.println("Embedded Tagop :- IAV ReadSec With TagData filter");
                    Gen2.Denatran.IAV.ReadSec readSec = new Gen2.Denatran.IAV.ReadSec(payload, readSecPtr);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, readSec, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-EMB ReadSec-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- IAV ReadSec With Gen2select filter");
                    Gen2.Denatran.IAV.ReadSec readSec = new Gen2.Denatran.IAV.ReadSec(payload, readSecPtr);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, readSec, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-Emb WriteSec-With out filter
                {
                    System.out.println("Embedded Tagop :- IAV WriteSec With out filter");
                    Gen2.Denatran.IAV.WriteSec writeSec = new Gen2.Denatran.IAV.WriteSec(payload, writeSecData, writeCredentials);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, writeSec, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-Emb WriteSec-With TagData filter
                {
                    System.out.println("Embedded Tagop :- IAV WriteSec With TagData filter");
                    Gen2.Denatran.IAV.WriteSec writeSec = new Gen2.Denatran.IAV.WriteSec(payload, writeSecData, writeCredentials);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, writeSec, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-Emb WriteSec-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- IAV WriteSec With Gen2select filter");
                    Gen2.Denatran.IAV.WriteSec writeSec = new Gen2.Denatran.IAV.WriteSec(payload, writeSecData, writeCredentials);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, writeSec, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-authFullPass-With out filter
                {
                    System.out.println("Embedded Tagop :- OBU authFullPass With out filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass authFullPass = new Gen2.Denatran.IAV.OBUAuthFullPass(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, authFullPass, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-authFullPass-With TagData filter
                {
                    System.out.println("Embedded Tagop :- OBU authFullPass With TagData filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass authFullPass = new Gen2.Denatran.IAV.OBUAuthFullPass(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, authFullPass, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-authFullPass-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- OBU authFullPass With Gen2select filter");
                    Gen2.Denatran.IAV.OBUAuthFullPass authFullPass = new Gen2.Denatran.IAV.OBUAuthFullPass(payload);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, authFullPass, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetTokenId-With out filter
                {
                    System.out.println("Embedded Tagop :- GetTokenId With out filter");
                    Gen2.Denatran.IAV.GetTokenId tokenid = new Gen2.Denatran.IAV.GetTokenId();
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-GetTokenId-With TagData filter
                {
                    System.out.println("Embedded Tagop :- GetTokenId With TagData filter");
                    Gen2.Denatran.IAV.GetTokenId tokenid = new Gen2.Denatran.IAV.GetTokenId();
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-GetTokenId-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- GetTokenId With Gen2select filter");
                    Gen2.Denatran.IAV.GetTokenId tokenid = new Gen2.Denatran.IAV.GetTokenId();
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetG0PAOBUAuth-With out filter
                            {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuth With out filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuth);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-GetG0PAOBUAuth-With TagData filter
                            {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuth With TagData filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuth);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-GetG0PAOBUAuth-With Gen2select filter
                            {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuth With Gen2select filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuth);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetG0PAOBUAuthFullPass-With out filter
                {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuthFullPass With out filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-GetG0PAOBUAuthFullPass-With TagData filter
                {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuthFullPass With TagData filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-GetG0PAOBUAuthFullPass-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuthFullPass With Gen2select filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                try//scenario1-GetG0PAOBUAuthFullPass1-With out filter
                {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuthFullPass1 With out filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass1);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, null, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario2-GetG0PAOBUAuthFullPass1-With TagData filter
                {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuthFullPass1 With TagData filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass1);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, filter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                try//scenario3-GetG0PAOBUAuthFullPass1-With Gen2select filter
                {
                    System.out.println("Embedded Tagop :- GetG0PAOBUAuthFullPass1 With Gen2select filter");
                    Gen2.Denatran.IAV.G0PAOBUAuth tokenid = new Gen2.Denatran.IAV.G0PAOBUAuth(GOPAOBUAuthFullPass1);
                    rp = new SimpleReadPlan(antenna, TagProtocol.GEN2, selectfilter, tokenid, 10);
                    r.paramSet("/reader/read/plan", rp);
                    tagReads = r.read(1000);
                    for (TagReadData tr : tagReads)
                    {
                        System.out.println("read pass: " + ReaderUtil.byteArrayToHexString(tr.getData()));
                        System.out.println("Data length : " + tr.getData().length);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
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

    static int[] parseAntennaList(String[] args, int argPosition)
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
