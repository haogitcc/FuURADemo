//Uncomment this to enable filter
#define ENABLE_FILTER
//Uncomment this to enable readafterwrite functionality
#define ENABLE_READ_AFTER_WRITE 

using System;
using System.Collections.Generic;
using System.Text;

// Reference the API
using ThingMagic;

namespace WriteTag
{
    /// <summary>
    /// Sample program that writes an EPC to a tag and demonstrates the functionality of read after write
    /// </summary>
    class WriteTag
    {
        static int[] antennaList = null;
        static Reader r = null;
        static void Usage()
        {
            Console.WriteLine(String.Join("\r\n", new string[] {
                    " Usage: "+"Please provide valid reader URL, such as: [-v] [reader-uri] [--ant n[,n...]]",
                    " -v : (Verbose)Turn on transport listener",
                    " reader-uri : e.g., 'tmr:///com4' or 'tmr:///dev/ttyS0/' or 'tmr://readerIP'",
                    " [--ant n[,n...]] : e.g., '--ant 1,2,..,n",
                    " Example: 'tmr:///com4' or 'tmr:///com4 --ant 1,2' or '-v tmr:///com4 --ant 1,2'"
            }));
            Environment.Exit(1);
        }
        static void Main(string[] args)
        {
            // Program setup
            if (1 > args.Length)
            {
                Usage();
            }           
            TagFilter filter = null;
            for (int nextarg = 1; nextarg < args.Length; nextarg++)
            {
                string arg = args[nextarg];
                if (arg.Equals("--ant"))
                {
                    if (null != antennaList)
                    {
                        Console.WriteLine("Duplicate argument: --ant specified more than once");
                        Usage();
                    }
                    antennaList = ParseAntennaList(args, nextarg);
                    nextarg++;
                }
                else
                {
                    Console.WriteLine("Argument {0}:\"{1}\" is not recognized", nextarg, arg);
                    Usage();
                }
            }

            try
            {
                // Create Reader object, connecting to physical device.
                // Wrap reader in a "using" block to get automatic
                // reader shutdown (using IDisposable interface).
                using (r = Reader.Create(args[0]))
                {
                    //Uncomment this line to add default transport listener.
                    //r.Transport += r.SimpleTransportListener;

                    r.Connect();
                    if (Reader.Region.UNSPEC == (Reader.Region)r.ParamGet("/reader/region/id"))
                    {
                        Reader.Region[] supportedRegions = (Reader.Region[])r.ParamGet("/reader/region/supportedRegions");
                        if (supportedRegions.Length < 1)
                        {
                            throw new FAULT_INVALID_REGION_Exception();
                        }
                        r.ParamSet("/reader/region/id", supportedRegions[0]);
                    }
                    if (r.isAntDetectEnabled(antennaList))
                    {
                        Console.WriteLine("Module doesn't has antenna detection support please provide antenna list");
                        Usage();
                    }
                    //Use first antenna for operation
                    if (antennaList != null)
                        r.ParamSet("/reader/tagop/antenna", antennaList[0]);

                    // This select filter matches all Gen2 tags where bits 32-48 of the EPC are 0x0123 
#if ENABLE_FILTER
                    filter = new Gen2.Select(false, Gen2.Bank.EPC, 32, 16, new byte[] { 0x01, 0x23});
                   
#endif

                    //Gen2.TagData epc = new Gen2.TagData(new byte[] {
                    //    0x01, 0x23, 0x45, 0x67, 0x89, 0xAB,
                    //    0xCD, 0xEF, 0x01, 0x23, 0x45, 0x67,
                    //});
                    //Gen2.WriteTag tagop = new Gen2.WriteTag(epc);
                    //r.ExecuteTagOp(tagop, null);

                    // Reads data from a tag memory bank after writing data to the requested memory bank without powering down of tag
#if ENABLE_READ_AFTER_WRITE
                    {
                        //create a tagopList with write tagop followed by read tagop
                        TagOpList tagopList = new TagOpList();
                        byte wordCount;
                        ushort[] readData;

                        //Write one word of data to USER memory and read back 8 words from EPC memory using WriteData and ReadData
                        {
                            ushort[] writeData = { 0x9999 };
                            wordCount = 8;
                            Gen2.WriteData wData = new Gen2.WriteData(Gen2.Bank.USER, 2, writeData);
                            Gen2.ReadData rData = new Gen2.ReadData(Gen2.Bank.EPC, 0, wordCount);
                            //Gen2.WriteTag wTag = new Gen2.WriteTag(epc);

                            // assemble tagops into list
                            tagopList.list.Add(wData);
                            tagopList.list.Add(rData);

                            Console.WriteLine("###################Embedded Read after write######################");
                            // uncomment the following for embedded read after write.
                            embeddedReadAfterWrite(null, tagopList);

                            // call executeTagOp with list of tagops
                            //readData = (ushort[])r.ExecuteTagOp(tagopList, null);
                            //Console.WriteLine("ReadData: ");
                            //foreach (ushort word in readData)
                            //{
                            //    Console.Write(" {0:X4}", word);
                            //}
                            //Console.WriteLine("\n");
                        }

                        //clearing the list for next operation
                        tagopList.list.Clear();

                        //Write 12 bytes(6 words) of EPC and read back 8 words from EPC memory using WriteTag and ReadData
                        {
                            Gen2.TagData epc1 = new Gen2.TagData(new byte[] {
                                 0x11, 0x22, 0x33, 0x44, 0x55, 0x66,
                                 0x77, 0x88, 0x99, 0xaa, 0xbb, 0xcc,
                            });
                            wordCount = 8;
                            Gen2.WriteTag wtag = new Gen2.WriteTag(epc1);
                            Gen2.ReadData rData = new Gen2.ReadData(Gen2.Bank.EPC, 0, wordCount);

                            // assemble tagops into list
                            tagopList.list.Add(wtag);
                            tagopList.list.Add(rData);

                            // call executeTagOp with list of tagops
                            //readData = (ushort[])r.ExecuteTagOp(tagopList, null);
                            //Console.WriteLine("ReadData: ");
                            //foreach (ushort word in readData)
                            //{
                            //    Console.Write(" {0:X4}", word);
                            //}
                            //Console.WriteLine("\n");

                           
                        }
                    }
#endif
                }
            }
            catch (ReaderException re)
            {
                Console.WriteLine("Error: " + re.Message);
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error: " + ex.Message);
            }
        }

        #region ParseAntennaList

        private static int[] ParseAntennaList(IList<string> args, int argPosition)
        {
            int[] antennaList = null;
            try
            {
                string str = args[argPosition + 1];
                antennaList = Array.ConvertAll<string, int>(str.Split(','), int.Parse);
                if (antennaList.Length == 0)
                {
                    antennaList = null;
                }
            }
            catch (ArgumentOutOfRangeException)
            {
                Console.WriteLine("Missing argument after args[{0:d}] \"{1}\"", argPosition, args[argPosition]);
                Usage();
            }
            catch (Exception ex)
            {
                Console.WriteLine("{0}\"{1}\"", ex.Message, args[argPosition + 1]);
                Usage();
            }
            return antennaList;
        }

        #endregion

        #region EmbeddedReadAfterWrite
        public static void embeddedReadAfterWrite(TagFilter filter, TagOp tagop)
        {
            TagReadData[] tagReads = null;
            SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, filter, tagop, 1000);
            r.ParamSet("/reader/read/plan", plan);
            tagReads = r.Read(1000);
            //// Print tag reads
            foreach (TagReadData tr in tagReads)
            {
                Console.WriteLine(tr.ToString());
                if (0 < tr.Data.Length)
                {
                    Console.WriteLine("  Data:" + ByteFormat.ToHex(tr.Data, "", " "));
                }
            }
        }
        #endregion

    }
}