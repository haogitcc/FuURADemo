using System;
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

            //Johar
            class_id_combo.Items.Add("E2");
            vendor_id_combo.Items.Add("035");
            model_id_combo.Items.Add("106");

            //Fudan
            //class_id_combo.Items.Add("E2");
            vendor_id_combo.Items.Add("827");
            model_id_combo.Items.Add("001");

            class_id_combo.SelectedIndex = 0;
            vendor_id_combo.SelectedIndex = 0;
            model_id_combo.SelectedIndex = 0;

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

        private void Class_id_combo_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Console.WriteLine("### class_id_textbox");
            class_id_textbox.Text = class_id_combo.SelectedValue.ToString();
        }

        private void Vendor_id_combo_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Console.WriteLine("### vendor_id_textbox");
            vendor_id_textbox.Text = vendor_id_combo.SelectedValue.ToString();
        }

        private void Model_id_combo_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Console.WriteLine("### model_id_textbox");
            model_id_textbox.Text = model_id_combo.SelectedValue.ToString();
        }

        private void Temp_read_button_Click(object sender, RoutedEventArgs e)
        {
            if (temp_read_button.Content.Equals("Read"))
            {
                if(antennaList.Count == 0)
                {
                    MessageBox.Show("Please Select TagOp antenna", "No Antenna Selected", MessageBoxButton.OK, MessageBoxImage.Error);
                    return;
                }
                temp_read_button.Content = "Stop";
                if (johar_radiobutton.IsChecked == true && vbl_radiobutton.IsChecked == false)
                {
                    startReadJohar();
                }
                else if (johar_radiobutton.IsChecked == false && vbl_radiobutton.IsChecked == true)
                {
                    startReadVBL();
                }
            }
            else if (temp_read_button.Content.Equals("Stop"))
            {
                temp_read_button.Content = "Read";
                if (johar_radiobutton.IsChecked == true && vbl_radiobutton.IsChecked == false)
                {
                    stopReadJohar();
                }
                else if (johar_radiobutton.IsChecked == false && vbl_radiobutton.IsChecked == true)
                {
                    stopReadVBL();
                }
            }
        }

        private void stopReadVBL()
        {
            isStartRead = false;
            tagdb.tagdbIsJohar = false;
            tagdb.tagdbIsVBL = false;
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

            originalTarget = (Gen2.Target)objReader.ParamGet("/reader/gen2/target");
            objReader.ParamSet("/reader/gen2/target", Gen2.Target.AB);
            Console.WriteLine("### set target to AB success");

            StartReading();
        }

        private void stopReadJohar()
        {
            tagdb.tagdbIsJohar = false;
            objReader.StopReading();
            objReader.TagRead -= PrintTagreads;
            objReader.ReadException -= new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
        }

        private void startReadJohar()
        {
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
            tagdb.tagdbIsJohar = true;

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
    }
}
