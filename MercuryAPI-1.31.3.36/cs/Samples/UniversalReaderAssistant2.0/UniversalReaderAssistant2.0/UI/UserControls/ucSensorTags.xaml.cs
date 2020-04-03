using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Data;
using System.Windows.Input;
using System.Windows.Media;
using ThingMagic.URA2.BL;

namespace ThingMagic.URA2.UI.UserControls
{
    /// <summary>
    /// ucSensorTags.xaml 的交互逻辑
    /// </summary>
    public partial class ucSensorTags : UserControl
    {
        String Tags = "ucSensorTags ";
        Reader objReader;
        uint startAddress = 0;
        string model = string.Empty;
        Gen2.Bank selectMemBank;
        TagFilter searchSelect = null;
        List<int> antennaList = null;

        TagDatabase tagdb = new TagDatabase();
        public bool chkEnableTagAging = false;
        public bool enableTagAgingOnRead = false;


        private ManualResetEvent waitUntilReadMethodCalled = new ManualResetEvent(false);
        private Thread asyncReadThread = null;
        private Thread vblTagsCountDetectThread = null;

        private Gen2.Target originalTarget;
        bool isStartRead = false;

        static int tid = 0;

        private bool _exitNow = false;
        private bool IsCountChange = true;
        private bool isReadingTune = false;
        private bool isReadingNValue = true;

        public ucSensorTags()
        {
            Console.WriteLine("### init ucSensorTags");
            InitializeComponent();

            List<CategoryInfo> categoryList = new List<CategoryInfo>();
            sensortag_combobox.ItemsSource = categoryList;
            //这里的Name和Value不能乱填哦
            sensortag_combobox.DisplayMemberPath = "Name";//显示出来的值
            sensortag_combobox.SelectedValuePath = "Value";//实际选中后获取的结果的值

            categoryList.Add(new CategoryInfo { Name = "悦和", Value = "Johar" });
            categoryList.Add(new CategoryInfo { Name = "VBL", Value = "VBL" });
            categoryList.Add(new CategoryInfo { Name = "宜链", Value = "iLian" });
            categoryList.Add(new CategoryInfo { Name = "RF Micro Magnus S3", Value = "rfMicro" });

            sensortag_combobox.SelectedIndex = 0;

            GenerateColmnsForDataGrid();
            this.DataContext = tagdb.TagList;
        }

        public void LoadSensorTagsMemory(Reader reader, string readerModel, List<int> antlist)
        {
            Console.WriteLine(Tags + "### LoadSensorTagsMemory --> " + tagdb.TotalTagCount);
            objReader = reader;
            model = readerModel;
            antennaList = antlist;
        }

        /// <summary>
        /// Generate columns for datagrid
        /// </summary>
        public void GenerateColmnsForDataGrid()
        {
            Console.WriteLine(Tags + "### GenerateColmnsForDataGrid");
            temp_dgTagResults.AutoGenerateColumns = false;
            serialNoColumn.Binding = new Binding("SerialNumber");
            serialNoColumn.Header = "#";
            serialNoColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            epcColumn.Binding = new Binding("EPC");
            epcColumn.Header = "EPC";
            epcColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            antennaColumn.Binding = new Binding("Antenna");
            antennaColumn.Header = "Ant";
            antennaColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            dataColumn.Binding = new Binding("Data");
            dataColumn.Header = "Data";
            dataColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            tuneColumn.Binding = new Binding("VBL_Tune");
            tuneColumn.Header = "Tune";
            tuneColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            nValueColumn.Binding = new Binding("VBL_NValue");
            nValueColumn.Header = "NValue";
            nValueColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            TemperatureColumn.Binding = new Binding("Temperature"); 
            TemperatureColumn.Header = "Temperature";
            TemperatureColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            readCountColumn.Binding = new Binding("ReadCount");
            readCountColumn.Header = "ReadCount";
            readCountColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

            temp_dgTagResults.ItemsSource = tagdb.TagList;
            Console.WriteLine(Tags + "### GenerateColmnsForDataGrid done");
        }

        #region EventHandler

        #region DataGridHeaderChkBox
        private void CheckBox_Checked(object sender, RoutedEventArgs e)
        {
            HeadCheck(sender, e, true);
        }

        private void CheckBox_Unchecked(object sender, RoutedEventArgs e)
        {
            HeadCheck(sender, e, false);
        }

        private void HeadCheck(object sender, RoutedEventArgs e, bool IsChecked)
        {
            foreach (TagReadRecord mf in temp_dgTagResults.Items)
            {
                mf.Checked = IsChecked;
            }
            temp_dgTagResults.Items.Refresh();
        }
        #endregion

        /// <summary>
        /// Change the ToolTip content based on the state of header checkbox in datagrid
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void headerCheckBox_MouseEnter(object sender, MouseEventArgs e)
        {
            CheckBox ch = (CheckBox)sender;
            if ((bool)ch.IsChecked)
            {
                ch.ToolTip = "DeSelectALL";
            }
            else
            {
                ch.ToolTip = "SelectALL";
            }
        }

        /// <summary>
        /// Retain aged tag cell colour
        /// </summary>
        public Dictionary<string, Brush> tagagingColourCache = new Dictionary<string, Brush>();

        private void temp_dgTagResults_LoadingRow(object sender, DataGridRowEventArgs e)
        {
            try
            {
                if (chkEnableTagAging)
                {
                    var data = (TagReadRecord)e.Row.DataContext;
                    TimeSpan difftimeInSeconds = (DateTime.UtcNow - data.TimeStamp.ToUniversalTime());
                    BrushConverter brush = new BrushConverter();
                    if (enableTagAgingOnRead)
                    {
                        if (difftimeInSeconds.TotalSeconds < 12)
                        {
                            switch (Math.Round(difftimeInSeconds.TotalSeconds).ToString())
                            {
                                case "5":
                                    e.Row.Background = (Brush)brush.ConvertFrom("#FFEEEEEE");
                                    break;
                                case "6":
                                    e.Row.Background = (Brush)brush.ConvertFrom("#FFD3D3D3");
                                    break;
                                case "7":
                                    e.Row.Background = (Brush)brush.ConvertFrom("#FFCCCCCC");
                                    break;
                                case "8":
                                    e.Row.Background = (Brush)brush.ConvertFrom("#FFC3C3C3");
                                    break;
                                case "9":
                                    e.Row.Background = (Brush)brush.ConvertFrom("#FFBBBBBB");
                                    break;
                                case "10":
                                    e.Row.Background = (Brush)brush.ConvertFrom("#FFA1A1A1");
                                    break;
                                case "11":
                                    e.Row.Background = new SolidColorBrush(Colors.Gray);
                                    break;
                            }
                            Dispatcher.BeginInvoke(new System.Threading.ThreadStart(delegate () { RetainAgingOnStopRead(data.SerialNumber.ToString(), e.Row.Background); }));
                        }
                        else
                        {
                            e.Row.Background = (Brush)brush.ConvertFrom("#FF888888");
                            Dispatcher.BeginInvoke(new System.Threading.ThreadStart(delegate () { RetainAgingOnStopRead(data.SerialNumber.ToString(), e.Row.Background); }));
                        }
                    }
                    else
                    {
                        if (tagagingColourCache.ContainsKey(data.SerialNumber.ToString()))
                        {
                            e.Row.Background = tagagingColourCache[data.SerialNumber.ToString()];
                        }
                        else
                        {
                            e.Row.Background = Brushes.White;
                        }
                    }
                }
            }
            catch { }
        }

        /// <summary>
        /// To retain colour of aged tag after stop reading
        /// </summary>
        /// <param name="slno"></param>
        /// <param name="row"></param>
        private void RetainAgingOnStopRead(string slno, Brush row)
        {
            if (!tagagingColourCache.ContainsKey(slno))
            {
                tagagingColourCache.Add(slno, row);
            }
            else
            {
                tagagingColourCache.Remove(slno);
                tagagingColourCache.Add(slno, row);
            }
        }

        #endregion EventHandler

        private void temp_dgTagResults_LostFocus(object sender, RoutedEventArgs e)
        {
            temp_dgTagResults.UnselectAll();
            ContextMenu ctMenu = (ContextMenu)App.Current.MainWindow.FindName("ctMenu");
            ctMenu.Visibility = System.Windows.Visibility.Collapsed;
        }
        
        private void Temp_read_button_Click(object sender, RoutedEventArgs e)
        {
            string langName = sensortag_combobox.SelectedValue.ToString();
            if (temp_read_button.Content.Equals("Read"))
            {
                sensortag_tid_gropbox.IsEnabled = false;
                if (antennaList.Count == 0)
                {
                    MessageBox.Show("Please Select TagOp antenna", "No Antenna Selected", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
                sensortag_combobox.IsEnabled = false;
                temp_read_button.Content = "Stop";
                

                if (langName.Equals("Johar"))
                {
                    startReadJohar();
                }
                else if (langName.Equals("VBL"))
                {
                    startReadVBL();
                }
                else if (langName.Equals("iLian"))
                {
                    startReadILian();
                }
                else if (langName.Equals("rfMicro"))
                {
                    startReadRFMicro();
                    temp_read_button.Content = "Read";
                }
            }
            else if (temp_read_button.Content.Equals("Stop"))
            {
                sensortag_combobox.IsEnabled = true;
                sensortag_tid_gropbox.IsEnabled = true;
                temp_read_button.Content = "Read";
                if (langName.Equals("Johar"))
                {
                    stopReadJohar();
                }
                else if (langName.Equals("VBL"))
                {
                    stopReadVBL();
                }
                else if (langName.Equals("iLian"))
                {
                    stopReadILian();
                }
                else if (langName.Equals("rfMicro"))
                {
                    stopReadRFMicro();
                }
            }
        }
        #region RF Micron Magnus-S3 Sensor Tag
        private void stopReadRFMicro()
        {
            
        }

        private void startReadRFMicro()
        {
            //Read the Sensor Code 
            ReadRFMicronSensorCode();

            //Read the On-Chip RSSI Code 
            ReadRFMicronOnChipCode();

            //Calibrated Temperature Measurements
            string CalibratedCode = ReadRFMicronCalibratedMeasurements();

            //Read the Temperature Code 
            int TEMP_CODE = ReadRFMicronTemperatureCode();

            GetRFMicroTemp(CalibratedCode, TEMP_CODE);
        }

        private string ReadRFMicronCalibratedMeasurements()
        {
            string CalibratedCode = null;
            TagOp Calibrated = new Gen2.ReadData(Gen2.Bank.USER, 0x8, 4);
            SimpleReadPlan readPlan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, Calibrated, true, 100);
            objReader.ParamSet("/reader/read/plan", readPlan);

            TagReadData[] CalibratedReadResults = objReader.Read(75);
            foreach (TagReadData result in CalibratedReadResults)
            {
                string EPC = ByteFormat.ToHex(result.Epc, "", "");
                string frequency = result.Frequency.ToString();
                byte[] bCalibratedCode = result.Data;
                CalibratedCode = ByteFormat.ToHex(result.Data, "", "");
                Console.WriteLine(string.Format("EPC:{0}, Frequency(kHz): {1}, CalibratedCode Code: {2}", EPC, frequency, CalibratedCode));
            }
            return CalibratedCode;
        }

        private int ReadRFMicronTemperatureCode()
        {
            int TEMP_CODE = 0;
            //Define a Select command which applies to a Magnus-S3 403h 
            //select mask should ba a hex value of USER hex of E0, mask length is zero
            Gen2.Select select = new Gen2.Select(false, Gen2.Bank.USER, 0xE0, 0, new byte[0]);
            //After the tag has recived the Select Command
            //the Temperature Code in the , Reserved bank occupies the least-significant 12 bits of the word location of hex Reserved E
            TagOp temperatureCodeRead = new Gen2.ReadData(Gen2.Bank.RESERVED, 0xE, 1);
            SimpleReadPlan readPlan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, select, temperatureCodeRead, true, 100);
            objReader.ParamSet("/reader/read/plan", readPlan);

            TagReadData[] TemperatureCodeReadResults = objReader.Read(75);
            foreach (TagReadData result in TemperatureCodeReadResults)
            {
                string EPC = ByteFormat.ToHex(result.Epc, "", "");
                string frequency = result.Frequency.ToString();
                string TemperatureCode = ByteFormat.ToHex(result.Data, "", "");
                Console.WriteLine(string.Format("EPC:{0}, Frequency(kHz): {1}, Temperature Code: {2}", EPC, frequency, TemperatureCode));
                TEMP_CODE = Convert.ToInt32(TemperatureCode, 16) & 0x00000FFF;
            }
            return TEMP_CODE;
        }

        private string ReadRFMicronOnChipCode()
        {
            string onChipRSSICode = null;
            //Define a Select command which applies to a Magnus-S3 403h 
            //We want all tags to respond, regardless of their On-Chip RSSI Code Value
            //so our select mask should ba a hex value of 1F
            byte[] mask = { Convert.ToByte("1F", 16) };
            //Select User memory bank and pointer value bit address of hex D0
            Gen2.Select select = new Gen2.Select(false, Gen2.Bank.USER, 0xD0, 8, mask);
            //The On-Chip RSSI Code in the Reserved bank, word location hex D
            TagOp onChipRSSICodeRead = new Gen2.ReadData(Gen2.Bank.RESERVED, 0xD, 1);
            SimpleReadPlan readPlan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, select, onChipRSSICodeRead, true, 100);
            objReader.ParamSet("/reader/read/plan", readPlan);

            TagReadData[] onChipRSSIReadResults = objReader.Read(75);
            foreach (TagReadData result in onChipRSSIReadResults)
            {
                string EPC = ByteFormat.ToHex(result.Epc, "", "");
                string frequency = result.Frequency.ToString();
                onChipRSSICode = ByteFormat.ToHex(result.Data, "", "");
                Console.WriteLine(string.Format("EPC:{0}, Frequency(kHz): {1}, On-Chip RSSI: {2}", EPC, frequency, onChipRSSICode));
            }
            return onChipRSSICode;
        }

        private string ReadRFMicronSensorCode()
        {
            string sensorCode = null;
            //Define a tag read operation which reads from a Magnus-S3 403h 
            //Sensor Code in Reserved bank, in memory hex C
            TagOp sensorCodeRead = new Gen2.ReadData(Gen2.Bank.RESERVED, 0xC, 1);
            SimpleReadPlan readPlan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, sensorCodeRead, true, 100);
            objReader.ParamSet("/reader/read/plan", readPlan);

            TagReadData[] sensorReadResults = objReader.Read(75);
            foreach (TagReadData result in sensorReadResults)
            {
                string EPC = ByteFormat.ToHex(result.Epc, "", "");
                string frequency = result.Frequency.ToString();
                sensorCode = ByteFormat.ToHex(result.Data, "", "");
                Console.WriteLine(string.Format("EPC:{0}, Frequency(kHz): {1}, Sensor Code: {2}", EPC, frequency, sensorCode));
            }
            return sensorCode;
        }

        private void GetRFMicroTemp(string CalibratedCode, int TEMP_CODE)
        {
            String USER_8H = CalibratedCode.Substring(0, 4);
            String USER_9H = CalibratedCode.Substring(4, 4);
            String USER_AH = CalibratedCode.Substring(8, 4);
            String USER_BH = CalibratedCode.Substring(12, 4);
            Console.WriteLine(string.Format("USER_8H={0}, USER_9H={1}, USER_AH={2}, USER_BH={3}", USER_8H, USER_9H, USER_AH, USER_BH));

            int CRC = Convert.ToInt32(USER_8H, 16) & 0x0000FFFF;
            int tempCRC = Convert.ToInt32(CRC16(CalibratedCode.Substring(4, 12)), 16);
            int CODE1 = (Convert.ToInt32(USER_9H, 16) & 0x0000FFF0) >> 4;
            int TEMP1 = (Convert.ToInt32(USER_9H, 16) & 0x0000000F)<<7 | ((Convert.ToInt32(USER_AH, 16) & 0x0000FE00) >> 9);
            int CODE2 = (Convert.ToInt32(USER_AH, 16) & 0x000001FF)<<3 | ((Convert.ToInt32(USER_BH, 16) & 0x0000E000) >> 13);
            int TEMP2 = (Convert.ToInt32(USER_BH, 16) & 0x00001FFC) >> 2;
            int VER = (Convert.ToInt32(USER_BH, 16) & 0x00000003);

            double TEMP1_in_Celsius = CalculateTempInCelsius(TEMP1);
            double TEMP2_in_Celsius = CalculateTempInCelsius(TEMP2);

            double TEMP_in_Celsius = CalculateTempInCelsius(CODE1, CODE2, TEMP1, TEMP2, TEMP_CODE);

            Console.WriteLine(string.Format("CRC    ={0}", CRC));
            Console.WriteLine(string.Format("tempCRC={0}", tempCRC));
            Console.WriteLine(string.Format("CODE1={0}, TEMP1={1}", CODE1, TEMP1));
            Console.WriteLine(string.Format("CODE2={0}, TEMP2={1}", CODE2, TEMP2));
            Console.WriteLine(string.Format("VER={0}", VER));
            Console.WriteLine(string.Format("TEMP_CODE={0}", TEMP_CODE));

            Console.WriteLine(string.Format("TEMP1_in_Celsius={0}", TEMP1_in_Celsius));
            Console.WriteLine(string.Format("TEMP2_in_Celsius={0}", TEMP2_in_Celsius));
            Console.WriteLine(string.Format("TEMP_in_Celsius={0}", TEMP_in_Celsius));
        }

        private string CRC16(string dataHexString)
        {
            int numBytes = dataHexString.Length / 2;
            byte[] dataByteArray = new byte[numBytes];
            for(int b=0; b<numBytes; b++)
            {
                dataByteArray[numBytes - 1 - b] = Convert.ToByte(dataHexString.Substring(2 * b, 2), 16);
            }
            BitArray data = new BitArray(dataByteArray);
            BitArray CRC = new BitArray(16);
            CRC.SetAll(true);
            for(int j=data.Length-1;j>=0;j--)
            {
                bool newBit = CRC[15] ^ data[j];
                for(int i=15;i>=1;i--)
                {
                    if(i==12||i==15)
                    {
                        CRC[i]=CRC[i-1]^newBit;
                    }
                    else 
                    {
                        CRC[i] = CRC[i - 1];
                    }
                }
                CRC[0] = newBit;
            }
            CRC.Not();
            byte[] CRCbytes = new byte[2];
            CRC.CopyTo(CRCbytes, 0);

            string CRCword = Convert.ToString(CRCbytes[1],16).PadLeft(2,'0')
                + Convert.ToString(CRCbytes[0], 16).PadLeft(2, '0');
            return CRCword;
        }

        private double CalculateTempInCelsius(int CODE1, int CODE2, int TEMP1, int TEMP2, int TEMP_CODE)
        {
            double temp = TEMP2 - TEMP1;
            double code = CODE2 - CODE1;
            double c_code1 = TEMP_CODE - CODE1;
            return (temp / code * c_code1 + TEMP1 - 800) / 10.0;
        }

        private double CalculateTempInCelsius(int temp)
        {
            return (temp - 800) / 10.0;
        }
        #endregion 


        private void stopReadILian()
        {
            tagdb.tagdbIsJohar = false;
            tagdb.tagdbIsVBL = false;
            tagdb.tagdbIsILian = false;

            objReader.StopReading();
            objReader.TagRead -= PrintTagreads;
            objReader.ReadException -= new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
            objReader.ParamSet("/reader/gen2/target", originalTarget);
        }

        private void startReadILian()
        {
            tagdb.tagdbIsJohar = false;
            tagdb.tagdbIsVBL = false;
            tagdb.tagdbIsILian = true;

            foreach (int ant in antennaList)
            {
                objReader.ParamSet("/reader/tagop/antenna", ant);
                Console.WriteLine("iLian set antenna " + ant);
            }

            originalTarget = (Gen2.Target)objReader.ParamGet("/reader/gen2/target");
            objReader.ParamSet("/reader/gen2/target", Gen2.Target.AB);
            Console.WriteLine("### iLian set target to AB success");

            //iLian: ClsId + VendorId + ModelId = 32 14E 0B0
            byte[] tidmask = new byte[] { (byte)0x32, (byte)0x14, (byte)0xE0, (byte)0xB0 };
            Gen2.Select tidFilter = new Gen2.Select(false, Gen2.Bank.TID, 0, (byte)(tidmask.Length * 8), tidmask);
            

            TagOp readUsrOp = new Gen2.ReadData(Gen2.Bank.USER, 0x7F, (byte)1); ;

            objReader.ParamSet("/reader/read/plan", new SimpleReadPlan(antennaList, TagProtocol.GEN2, tidFilter, readUsrOp, 1000));
            //objReader.ParamSet("/reader/read/plan", new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, 1000));
            Console.WriteLine("****** ParamSet read plan");

            //开始读温度数据
            objReader.TagRead += PrintTagreads;
            objReader.ReadException += new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
            // search for tags in the background
            objReader.StartReading();
            Console.WriteLine("****** startReading...");
        }

        private void stopReadVBL()
        {
            isStartRead = false;

            tagdb.tagdbIsJohar = false;
            tagdb.tagdbIsVBL = false;
            tagdb.tagdbIsILian = false;

            tagdb.tagdbIsVBL_Tune = false;
            tagdb.tagdbIsVBL_NValue = false;

            IsCountChange = false;
            isReadingTune = false;
            isReadingNValue = false;

            StopReading();

            objReader.ParamSet("/reader/gen2/target", originalTarget);
        }

        private void startReadVBL()
        {
            isStartRead = true;

            tagdb.tagdbIsJohar = false;
            tagdb.tagdbIsVBL = true;
            tagdb.tagdbIsILian = false;

            originalTarget = (Gen2.Target)objReader.ParamGet("/reader/gen2/target");
            objReader.ParamSet("/reader/gen2/target", Gen2.Target.AB);
            Console.WriteLine("### set target to AB success");

            StartReading();
        }

        private void stopReadJohar()
        {
            tagdb.tagdbIsJohar = false;
            tagdb.tagdbIsVBL = false;
            tagdb.tagdbIsILian = false;

            objReader.StopReading();
            objReader.TagRead -= PrintTagreads;
            objReader.ReadException -= new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
        }

        private void startReadJohar()
        {
            tagdb.tagdbIsJohar = true;
            tagdb.tagdbIsVBL = false;
            tagdb.tagdbIsILian = false;
            foreach (int ant in antennaList)
            {
                objReader.ParamSet("/reader/tagop/antenna", ant);
                Console.WriteLine("Johar set antenna " + ant);
            }
            //Johar: ClsId + VendorId + ModelId = E2 035 016
            //2A54	E2 03 51 06 05 00 96 10 2A 54 00 00 00 00 00 00	
            byte[] tidmask = new byte[] { (byte)0xE2, (byte)0x03, (byte)0x51, (byte)0x06 };
            Gen2.Select tidFilter = new Gen2.Select(false, Gen2.Bank.TID, 0, 32, tidmask);
            tidFilter.target = Gen2.Select.Target.Select;
            tidFilter.action = Gen2.Select.Action.ON_N_OFF;

            //byte[] epcmask = new byte[] { (byte)0x1B, (byte)0x24 };
            //Gen2.Select epcFilter = new Gen2.Select(false, Gen2.Bank.EPC, 32, 16, epcmask);
            //epcFilter.target = Gen2.Select.Target.Select;
            //epcFilter.action = Gen2.Select.Action.ON_N_OFF;

            // create and initialize Filter1 传感数据随EPC数据返回
            // This select filter matches all Gen2 tags where bits 32 to 48 of the EPC memory are 0x1008
            Gen2.Select filter1 = new Gen2.Select(false, Gen2.Bank.EPC, 32, 16, new byte[] { (byte)0x10, (byte)0x08 });
            filter1.target = Gen2.Select.Target.RFU3;
            filter1.action = Gen2.Select.Action.OFF_N_ON;

            // create and initialize Filter2
            // This select filter matches all Gen2 tags where bits 32 to 48 of the EPC memory are 0x1008
            Gen2.Select filter2 = new Gen2.Select(false, Gen2.Bank.EPC, 32, 16, new byte[] { (byte)0x10, (byte)0x08 });
            filter2.target = Gen2.Select.Target.Inventoried_S2;
            filter2.action = Gen2.Select.Action.OFF_N_ON;

            List<TagFilter> filterList = new List<TagFilter>();
            filterList.Add(tidFilter);
            filterList.Add(filter1);
            filterList.Add(filter2);

            // Initialize multifilter with tagFilter array containing list of filters
            // In case of Network readers, ensure that bitLength is a multiple of 8.
            MultiFilter multiFilter = new MultiFilter(filterList);

            // To get the sensor data with every response, select should be sent with every Query.
            // Enable the flag to send Select with every Query
            objReader.ParamSet("/reader/gen2/sendSelect", true);
            Console.WriteLine("****** ParamSet sendSelect");

            // Time interval between successive selects(Selsense and normal select) must be at least 15ms.
            // So, T4 must be set to 15ms
            UInt32 t4Val = 15000;
            objReader.ParamSet("/reader/gen2/t4", t4Val);
            Console.WriteLine("****** ParamSet t4Val=" + t4Val);

            TagOp readUsrOp = new Gen2.ReadData(Gen2.Bank.USER, 8, (byte)1); ;

            objReader.ParamSet("/reader/read/plan", new SimpleReadPlan(antennaList, TagProtocol.GEN2, multiFilter, readUsrOp, 1000));
            //objReader.ParamSet("/reader/read/plan", new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, 1000));
            Console.WriteLine("****** ParamSet read plan");

            //开始读温度数据
            objReader.TagRead += PrintTagreads;
            objReader.ReadException += new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
            // search for tags in the background
            objReader.StartReading();
            Console.WriteLine("****** startReading...");
        }

        private void r_ReadException(object sender, ReaderExceptionEventArgs e)
        {
            Console.WriteLine("Error: " + e.ReaderException.Message);
            MessageBox.Show(e.ReaderException.Message, e.ReaderException.ToString(), MessageBoxButton.OK, MessageBoxImage.Error);
        }

        private void PrintTagreads(Object sender, TagReadDataEventArgs e)
        {
            //Console.WriteLine(Tags + " ### EPC[" + e.TagReadData.EpcString + "]");
            Dispatcher.BeginInvoke(new ThreadStart(delegate ()
            {
                tagdb.Add(e.TagReadData);
            }));

            Dispatcher.BeginInvoke(new ThreadStart(delegate ()
            {
                tagdb.Repaint();
            }));

            Dispatcher.BeginInvoke(new ThreadStart(delegate ()
            {
                total_sensortags_read_count.Content = tagdb.TotalTagCount.ToString();
                unique_sensortags_count.Content = tagdb.UniqueTagCount.ToString();
            }
            ));
        }

        private static int byteArrayToInt(byte[] data, int offset)
        {
            int value = 0;
            int len = data.Length;
            for (int count = 0; count < len; count++)
            {
                value <<= 8;
                value ^= (data[count + offset] & 0x000000FF);
            }
            return value;
        }

        internal void ResetSensorTagsTab()
        {
            Console.WriteLine(Tags + "### ResetSensorTagsTab");
            if(temp_read_button.Content.Equals("Stop"))
            {
                temp_read_button.Content = "Read";
                objReader.StopReading();
                objReader.TagRead -= PrintTagreads;
                objReader.ReadException -= new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
            }
        }

        private void Temp_clear_button_Click(object sender, RoutedEventArgs e)
        {
            Dispatcher.BeginInvoke(new ThreadStart(delegate ()
            {
                tagdb.Clear();
                tagdb.Repaint();
            }));
            Dispatcher.Invoke(new ThreadStart(delegate ()
            {
                unique_sensortags_count.Content = "0";
                total_sensortags_read_count.Content = "0";
            }));
        }

        void PrintTagReadHandler(Object sender, TagReadDataEventArgs e)
        {
            PrintTagRead(e.TagReadData);
        }

        void PrintTagReads(TagReadData[] reads)
        {
            foreach (TagReadData read in reads)
            {
                PrintTagRead(read);
            }
        }

        void PrintTagRead(TagReadData read)
        {
            List<string> strl = new List<string>();
            strl.Add("EPC: " +read.EpcString);
            if(read.RESERVEDMemData.Length > 0)
            {
                strl.Add(",ReservedData: " + ByteFormat.ToHex(read.RESERVEDMemData));
            }
            if (read.USERMemData.Length > 0)
            {
                strl.Add(",UserData: " + ByteFormat.ToHex(read.USERMemData));
            }
            if (read.Data.Length > 0)
            {
                strl.Add(",Data: " + ByteFormat.ToHex(read.Data));
            }
            Console.WriteLine(String.Join(" ", strl.ToArray()));
        }

        #region StartReading
        
        /// <summary>
        /// Start reading RFID tags in the background. The tags found will be
        /// passed to the registered read listeners, and any exceptions that
        /// occur during reading will be passed to the registered exception
        /// listeners. Reading will continue until stopReading() is called.
        /// </summary>
        public void StartReading()
        {
            Console.WriteLine("### StartReading");
            _exitNow = false;
            if(null == vblTagsCountDetectThread)
            {
                vblTagsCountDetectThread = new Thread(TagsCountDetect);
                vblTagsCountDetectThread.IsBackground = true;
                vblTagsCountDetectThread.Name = "#vblTagsCountDetectThread# " + (tid++);
                vblTagsCountDetectThread.Start();
            }
            if (null == asyncReadThread)
            {
                asyncReadThread = new Thread(StartContinuousRead);
                asyncReadThread.IsBackground = true;
                asyncReadThread.Name = "#asyncReadThread# " + (tid++);
                asyncReadThread.Start();
            }
            
            //waitUntilReadMethodCalled.WaitOne();
        }
        
        private void StartContinuousRead()
        {
            Console.WriteLine("### StartContinuousRead");
            int _threadID = Thread.CurrentThread.ManagedThreadId;
            string name = Thread.CurrentThread.Name;
            Console.WriteLine("----> [" + _threadID + "] " + name);

            foreach(int ant in antennaList)
            {
                objReader.ParamSet("/reader/tagop/antenna", ant);
                Console.WriteLine("VBL set antenna " + ant);
            }

            //int[] ants = new int[] { 1 };
            //objReader.ParamSet("/reader/tagop/antenna", ants[0]);
            int readTime = (int)objReader.ParamGet("/reader/read/asyncOnTime");
            Console.WriteLine("############# readTime=" + readTime);
            
            //E2 C19 CB1 VBL
            byte[] tid_mask = new byte[] { 0xE2, 0xC1, 0x9c, 0xB1 };
            TagFilter target = new Gen2.Select(false, Gen2.Bank.TID, 0, 32, tid_mask);
            
            ReadPlan plan0 = new SimpleReadPlan(antennaList, TagProtocol.GEN2, target, null, false, 1000);

            TagOp tagOp1 = new Gen2.ReadData(Gen2.Bank.USER, 31, 1);
            ReadPlan plan1 = new SimpleReadPlan(antennaList, TagProtocol.GEN2, target, tagOp1, false, 1000);

            TagOp tagOp2 = new Gen2.ReadData(Gen2.Bank.RESERVED, 8, 1);
            ReadPlan plan2 = new SimpleReadPlan(antennaList, TagProtocol.GEN2, target, tagOp2, false, 1000);
            
            objReader.TagRead += PrintTagreads;
            objReader.ReadException += new EventHandler<ReaderExceptionEventArgs>(r_ReadException);

            IsCountChange = true;
            isReadingTune = false;
            isReadingNValue = false;

            while (!_exitNow)
            {
                try
                {
                    //读VBL标签
                    if (IsCountChange)
                    {
                        Console.WriteLine("### start read tags");
                        objReader.ParamSet("/reader/read/plan", plan0);
                        Console.WriteLine("### set plan0 success");
                        objReader.StartReading();
                        
                        while (IsCountChange)
                        {
                            Thread.Sleep(500);
                        }
                        objReader.StopReading();
                        Console.WriteLine("### stop read tags");
                    }

                    //读Tune
                    if(isReadingTune)
                    {
                        Console.WriteLine("### start read tune");
                        lock (tagdb)
                        {
                            tagdb.tagdbIsVBL_Tune = true;
                            tagdb.tagdbIsVBL_NValue = false;
                        }

                        objReader.ParamSet("/reader/read/plan", plan1);
                        Console.WriteLine("### set plan1 user [31:1] success");
                        objReader.StartReading();
                        while (isReadingTune)
                        {
                            Thread.Sleep(500);
                        }
                        objReader.StopReading();
                        Console.WriteLine("### stop read tune");
                    }

                    //读NValue
                    if(isReadingNValue)
                    {
                        Console.WriteLine("### start read nValue");
                        lock (tagdb)
                        {
                            tagdb.tagdbIsVBL_Tune = false;
                            tagdb.tagdbIsVBL_NValue = true;
                        }

                        objReader.ParamSet("/reader/read/plan", plan2);
                        Console.WriteLine("### set plan2 reserved [8:1] success");
                        objReader.StartReading();

                        while (isReadingNValue)
                        {
                            Thread.Sleep(500);
                        }
                        objReader.StopReading();
                        Console.WriteLine("### stop read nValue");
                    }

                }
                catch (Exception ex)
                {
                    _exitNow = true;
                    IsCountChange = true;
                    isReadingTune = false;
                    isReadingNValue = false;
                    Console.WriteLine("### error : " + ex.ToString());
                    //waitUntilReadMethodCalled.Set();
                }
            }
            Console.WriteLine("### end with startReding");
        }

        private void TagsCountDetect()
        {
            int _threadID = Thread.CurrentThread.ManagedThreadId;
            string name = Thread.CurrentThread.Name;
            Console.WriteLine("---> [" + _threadID + "] " + name);

            long oldcount = 0;
            long newcount = 0;
            while (isStartRead)
            {
                if(IsCountChange)
                {
                    Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                    {
                        sensortags_readingstatus_label.Content = "reading VBL sensortags ...";
                    }));
                    
                    newcount = tagdb.UniqueTagCount;
                    if (oldcount != 0 && newcount != 0 && oldcount == newcount)
                    {
                        IsCountChange = false;
                        isReadingTune = true;
                        isReadingNValue = false;
                        oldcount = 0;
                        newcount = 0;
                        continue;
                    }
                    Thread.Sleep(5000);
                    Console.WriteLine("IsCountChange=" + IsCountChange + ", " + oldcount + " vs " + newcount);
                    oldcount = newcount;
                }
                else if(isReadingTune)
                {
                    Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                    {
                        sensortags_readingstatus_label.Content = "reading TUNE of VBL sensortags ...";
                    }));
                    TagDatabase temp = tagdb;
                    int count = temp.EpcIndex.Count;
                    int i = 0;
                    foreach (TagReadRecord trd in temp.EpcIndex.Values)
                    {
                        if (!trd.VBL_Tune.Trim().Equals(""))
                            i++;
                    }

                    if (i == count)
                    {
                        isReadingTune = false;
                        IsCountChange = false;
                        isReadingNValue = true;
                    }

                    Thread.Sleep(3000);
                    Console.WriteLine("isReadingTune=" + isReadingTune + ", " + i + " vs " + count);
                }
                else if(isReadingNValue)
                {
                    Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                    {
                        sensortags_readingstatus_label.Content = "reading NValue of VBL sensor tags ...";
                    }));
                    TagDatabase temp = tagdb;
                    int nullCount = 0;
                    foreach (TagReadRecord trd in temp.EpcIndex.Values)
                    {
                        if (trd.VBL_Tune.Trim().Equals(""))
                        {
                            nullCount++;
                        }
                    }
                    if(nullCount>0)
                    {
                        Console.WriteLine("### nulCount= " + nullCount);
                        IsCountChange = true;
                        isReadingTune = false;
                        isReadingNValue = false;
                        continue;
                    }
                    Thread.Sleep(3000);
                    Console.WriteLine("isReadingNValue=" + isReadingNValue);
                }
                else
                {
                    Thread.Sleep(500);
                    Console.WriteLine("########  ");
                }
            }
            Console.WriteLine("IsCountChange="+ IsCountChange + ", isReadingTune="+ isReadingTune + ", isReadingNValue=" + isReadingNValue);
            Dispatcher.BeginInvoke(new ThreadStart(delegate ()
            {
                sensortags_readingstatus_label.Content = "Stop read ...";
            }));
            //IsCountChange = false;
            //isReadingTune = false;
            //isReadingNValue = false;
            Console.WriteLine("end with TagsCountDetect ...  ");
        }

        #endregion

        #region StopReading

        /// <summary>
        /// Stop reading RFID tags in the background.
        /// </summary>
        public void StopReading()
        {
            Console.WriteLine("### StopReading");
            _exitNow = true;
            if (asyncReadThread != null)
            {
                asyncReadThread.Join();
                asyncReadThread = null;
                Console.WriteLine("###asyncReadThread Stop");
            }

            if (vblTagsCountDetectThread != null)
            {
                vblTagsCountDetectThread.Join();
                vblTagsCountDetectThread = null;
                Console.WriteLine("### vblTagsCountDetectThread Stop");
            }

            objReader.TagRead -= PrintTagreads;
            objReader.ReadException -= new EventHandler<ReaderExceptionEventArgs>(r_ReadException);

            //waitUntilReadMethodCalled.Reset();
            Console.WriteLine("### Stop done");
        }
        
        #endregion

        #region Read
        /// <summary>
        /// Read RFID tags for a fixed duration.
        /// </summary>
        /// <param name="timeout">the time to spend reading tags, in milliseconds</param>
        /// <returns>the tags read</returns>
        public TagReadData[] Read(int timeout, ReadPlan readPlan)
        {
            Console.WriteLine("### Read");
            if(readPlan == null)
            {
                readPlan = (ReadPlan)objReader.ParamGet("/reader/read/plan");
            }

            if (timeout < 0)
                throw new ArgumentOutOfRangeException("Timeout (" + timeout.ToString() + ") must be greater than or equal to 0");

            else if (timeout > 65535)
                throw new ArgumentOutOfRangeException("Timeout (" + timeout.ToString() + ") must be less than 65536");

            List<TagReadData> tagReads = new List<TagReadData>();
            
            ReadInternal((UInt16)timeout, readPlan, ref tagReads);

            return tagReads.ToArray();
        }
        #endregion

        #region ReadInternal
        private void ReadInternal(UInt16 timeout, ReadPlan rp, ref List<TagReadData> tagReads)
        {
            Console.WriteLine("### ReadInternal");
            if ((rp is SimpleReadPlan))
            {
                DateTime now = DateTime.Now;
                DateTime endTime = now.AddMilliseconds(timeout);

                while (now <= endTime)
                {
                    TimeSpan totalTagFetchTime = new TimeSpan();
                    TimeSpan timeElapsed = endTime - now;
                    timeout = ((ushort)timeElapsed.TotalMilliseconds < 65535) ? (ushort)timeElapsed.TotalMilliseconds : (ushort)65535;

                    try
                    {
                        objReader.ParamSet("/reader/read/plan", rp);
                        TagReadData[] trds = objReader.Read(timeout);
                        tagReads.AddRange(trds);
                    }
                    catch (ReaderException ex)
                    {
                        throw;
                    }

                    now = DateTime.Now - totalTagFetchTime;
                }
            }
            else
                Console.WriteLine("Unsupported read plan: " + rp.GetType().ToString());
        }
        #endregion


        private void SensortagChange(String langName)
        {
            Console.WriteLine("select = " + langName);
            if (langName.Equals("Johar"))
            {
                Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                {
                    //Johar E2 035 106
                    class_id_label.Content = "E2";
                    vendor_id_label.Content = "035";
                    model_id_label.Content = "106";
                }));
            }
            else if (langName.Equals("VBL"))
            {
                Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                {
                    //VBL E2 C19 CB1
                    class_id_label.Content = "E2";
                    vendor_id_label.Content = "C19";
                    model_id_label.Content = "CB1";
                }));
            }
            else if (langName.Equals("iLian"))
            {
                Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                {
                    //iLian 32 14E 0B0
                    class_id_label.Content = "32";
                    vendor_id_label.Content = "14E";
                    model_id_label.Content = "0B0";
                }));
            }
            else if (langName.Equals("rfMicro"))
            {
                Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                {
                    //RF micro E2 824 03B
                    class_id_label.Content = "E2";
                    vendor_id_label.Content = "824";
                    model_id_label.Content = "03B";
                }));
            }
        }

        private void sensortag_combobox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            string langName = sensortag_combobox.SelectedValue.ToString();
            Console.WriteLine(String.Format("selected item={0}", langName));
            SensortagChange(langName);
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            
            GetRFMicroTemp("BD9F88A7E1477900", 2315);
        }
    }

    public class CategoryInfo
    {
        public string Name { get; set; }
        public string Value { get; set; }
    }
}
