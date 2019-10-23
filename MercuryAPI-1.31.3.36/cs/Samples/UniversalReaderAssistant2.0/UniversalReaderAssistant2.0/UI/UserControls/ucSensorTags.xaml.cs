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

            TemperatureColumn.Binding = new Binding("Temperature"); 
            TemperatureColumn.Header = "Temperature";
            TemperatureColumn.Width = new DataGridLength(1, DataGridLengthUnitType.Auto);

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
                temp_read_button.Content = "Stop";

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

                TagOp readUsrOp = new Gen2.ReadData(Gen2.Bank.USER, 8, (byte)1);;

                objReader.ParamSet("/reader/read/plan", new SimpleReadPlan(antennaList, TagProtocol.GEN2, multiFilter, readUsrOp,1000));
                //objReader.ParamSet("/reader/read/plan", new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, 1000));
                Console.WriteLine("****** ParamSet read plan");

                //开始读温度数据
                objReader.TagRead += PrintTagreads;
                objReader.ReadException += new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
                // search for tags in the background
                objReader.StartReading();
                Console.WriteLine("****** startReading...");
            }
            else if (temp_read_button.Content.Equals("Stop"))
            {
                temp_read_button.Content = "Read";
                objReader.StopReading();
                objReader.TagRead -= PrintTagreads;
                objReader.ReadException -= new EventHandler<ReaderExceptionEventArgs>(r_ReadException);
            }
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
                tagdb.IsTagdbSensortags = true;
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

        private void Test_button_Click(object sender, RoutedEventArgs e)
        {
            if(test_button.Content.Equals("Test"))
            {
                test_button.Content = "Testing";
                originalTarget = (Gen2.Target)objReader.ParamGet("/reader/gen2/target");
                objReader.ParamSet("/reader/gen2/target", Gen2.Target.AB);
                ReadVblTemp();
            }
            else if (test_button.Content.Equals("Testing"))
            {
                test_button.Content = "Test";

                _exitNow = true;
                while (asyncReadThread.ThreadState == ThreadState.Background)
                {
                    Thread.Sleep(100);
                    Console.WriteLine("################ " + asyncReadThread.ThreadState);
                }
                objReader.ParamSet("/reader/gen2/target", originalTarget);
            }
        }

        private Thread asyncReadThread = null;
        protected bool _exitNow = false;
        private ManualResetEvent waitUntilReadMethodCalled = new ManualResetEvent(false);
        int gpi_timeout = 1000;
        private Gen2.Target originalTarget;

        private void ReadVblTemp()
        {
            if (null == asyncReadThread)
            {
                asyncReadThread = new Thread(StartContinuousRead);
                asyncReadThread.IsBackground = true;
                asyncReadThread.Start();
            }
        }

        private void StartContinuousRead()
        {
            Console.WriteLine("1@#### StartContinuousRead");
            try
            {
                int[] ants = new int[] { 1 };
                //E2 C19 CB1 VBL
                byte[] tid_mask = new byte[] { 0xE2, 0xC1, 0x9c, 0xB1 };
                TagFilter target = new Gen2.Select(false, Gen2.Bank.TID, 0, 32, tid_mask);
                List<ReadPlan> plans = new List<ReadPlan>();
                TagOp tagOp1 = new Gen2.ReadData(Gen2.Bank.USER, 31, 1);
                ReadPlan plan1 = new SimpleReadPlan(ants, TagProtocol.GEN2, target, tagOp1, false, 1000);
                TagOp tagOp2 = new Gen2.ReadData(Gen2.Bank.RESERVED, 8, 1);
                ReadPlan plan2 = new SimpleReadPlan(ants, TagProtocol.GEN2, target, tagOp2, false, 1000);
                
                while (!_exitNow)
                {
                    //int readTime = (int)objReader.ParamGet("/reader/read/asyncOnTime");
                    int readTime = gpi_timeout;
                    TagReadData[] trds1 = tRead(readTime, plan1);
                    TagReadData[] trds2 = tRead(readTime, plan2);
                    Console.WriteLine("############# TRD111111111");
                    PrintTagReads(trds1);
                    Console.WriteLine("############# TRD22222222");
                    PrintTagReads(trds2);
                    Console.WriteLine("#########################");

                    //Dispatcher.BeginInvoke(new ThreadStart(delegate ()
                    //{
                    //    lock (tagdb)
                    //    {
                    //        tagdb.AddRange(trds);
                    //    }
                    //}));



                    Console.WriteLine("2222 @#### StartContinuousRead ");
                }

                Console.WriteLine("2222 @#### StartContinuousRead  exit");
            }
            // Catch all exceptions.  We're in a background thread,
            // so exceptions will be lost if we don't pass them on.
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
            }
        }

        /// <summary>
        /// Read RFID tags for a fixed duration.
        /// </summary>
        /// <param name="timeout">the time to spend reading tags, in milliseconds</param>
        /// <returns>the tags read</returns>
        public TagReadData[] tRead(int timeout, ReadPlan rp)
        {
            Console.WriteLine("1@#### tRead timeout=" + timeout);
            //CheckRegion();
            if (timeout < 0)
                throw new ArgumentOutOfRangeException("Timeout (" + timeout.ToString() + ") must be greater than or equal to 0");

            else if (timeout > 65535)
                throw new ArgumentOutOfRangeException("Timeout (" + timeout.ToString() + ") must be less than 65536");
            
            List<TagReadData> tagReads = new List<TagReadData>();

            ReadInternal((UInt16)timeout, rp, ref tagReads);

            return tagReads.ToArray();
        }

        // Stop trigger feature enabled or disabled
        private void ReadInternal(UInt16 timeout, ReadPlan rp, ref List<TagReadData> tagReads)
        {
            Console.WriteLine("1@#### ReadInternal ");
            if ((rp is SimpleReadPlan))
            {
                objReader.ParamSet("/reader/read/plan", rp);

                DateTime now = DateTime.Now;
                DateTime endTime = now.AddMilliseconds(timeout);

                while (now <= endTime)
                {
                    TimeSpan totalTagFetchTime = new TimeSpan();
                    TimeSpan timeElapsed = endTime - now;
                    timeout = ((ushort)timeElapsed.TotalMilliseconds < 65535) ? (ushort)timeElapsed.TotalMilliseconds : (ushort)65535;
                    Console.WriteLine("1@#### ReadInternal timeout=" + timeout);
                    TagReadData[] trds = objReader.Read(timeout);
                    tagReads.AddRange(trds);

                    now = DateTime.Now - totalTagFetchTime;
                }
            }
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
                strl.Add("Data: " + ByteFormat.ToHex(read.RESERVEDMemData));
            }
            Console.WriteLine(String.Join(" ", strl.ToArray()));
        }
    }
}
