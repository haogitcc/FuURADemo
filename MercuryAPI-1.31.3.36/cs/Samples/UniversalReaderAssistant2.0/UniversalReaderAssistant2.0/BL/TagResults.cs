using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;
using System.Collections;

namespace ThingMagic.URA2.BL
{
    /// <summary>
    /// DataGridView adapter for a TagReadData
    /// </summary>
    public class TagReadRecord : INotifyPropertyChanged
    {
        protected TagReadData RawRead = null;
        protected bool dataChecked = false;
        protected UInt32 serialNo = 0;
        protected string epcInAscii = null;
        protected string dataInAscii = null;
        protected string epcInReverseBase36 = null;
        //protected string dataInReverseBase36 = null;
        protected string newEpc = "";
        protected int writeStatus = 0;
        protected double temperature = 0.0;
        protected bool isJohar = false;
        protected bool isVBL = false;
        protected bool isVBL_Tune = false;
        protected bool isVBL_NValue = false;
        protected bool isILian = false;

        public TagReadRecord(TagReadData newData)
        {
            lock (new Object())
            {
                RawRead = newData;
            }
        }
        /// <summary>
        /// Merge new tag read with existing one
        /// </summary>
        /// <param name="data">New tag read</param>
        public void Update(TagReadData mergeData)
        {
            //Console.WriteLine("*** Update " + mergeData.EpcString);
            mergeData.ReadCount += ReadCount;
            TimeSpan timediff = mergeData.Time.ToUniversalTime() - this.TimeStamp.ToUniversalTime();
            // Update only the read counts and not overwriting the tag
            // read data of the existing tag in tag database when we 
            // receive tags in incorrect order.
            if (0 <= timediff.TotalMilliseconds)
            {
                RawRead = mergeData;
            }
            else
            {
                RawRead.ReadCount = mergeData.ReadCount;
            }

            //Console.WriteLine("Update isJohar=" + isJohar);
            if (isJohar)
            {
                double temp = getJoharTemp(mergeData);
                if (temp != UNSPECTTEMP)
                    temperature = temp;
            }
            else if(isVBL)
            {
                if (isVBL_Tune == true && isVBL_NValue == false)
                {
                    if (VBL_Tune.Trim().Equals(""))
                        VBL_Tune = ByteFormat.ToHex(mergeData.Data, "", "");
                }
                else if (isVBL_Tune == false && isVBL_NValue == true)
                {
                    VBL_NValue = ByteFormat.ToHex(mergeData.Data, "", "");
                    if(!VBL_Tune.Trim().Equals("")&&!VBL_NValue.Trim().Equals(""))
                    {
                        double temp = getVBLTemp(VBL_Tune, VBL_NValue, mergeData);
                        if(temp != UNSPECTTEMP)
                            temperature = temp;
                    }
                }
            }
            else if (isILian)
            {
                double temp = getILianTemp(mergeData);
                if (temp != UNSPECTTEMP)
                    temperature = temp; 
            }

            OnPropertyChanged(null);
        }
        
        public UInt32 SerialNumber
        {
            get { return serialNo; }
            set { serialNo = value; }
        }

        public string EPCInASCII
        {
            get
            {
                if (RawRead.Epc.Length > 0)
                    epcInAscii = Utilities.HexStringToAsciiString(RawRead.EpcString);
                else
                    epcInAscii = String.Empty;
                return epcInAscii;
            }
        }

        public string EPCInReverseBase36
        {
            get
            {
                if (RawRead.EpcString.Length > 0)
                    epcInReverseBase36 = Utilities.ConvertHexToBase36(RawRead.EpcString);
                else
                    epcInReverseBase36 = String.Empty;
                return epcInReverseBase36;
            }

        }

        public string DataInASCII
        {
            get
            {
                if (RawRead.Data.Length > 0)
                    dataInAscii = Utilities.HexStringToAsciiString(ByteFormat.ToHex(RawRead.Data).Split('x')[1]);
                else
                    dataInAscii = String.Empty;
                return dataInAscii;
            }
        }
        //public string DataInReverseBase36
        //{
        //    get { return dataInReverseBase36; }
        //    set 
        //    {
        //        if (value != string.Empty)
        //        {
        //            dataInReverseBase36 = value;
        //        }
        //        else
        //        {
        //            dataInReverseBase36 = string.Empty;
        //        }
        //    }
        //}
        public DateTime TimeStamp
        {
            get
            {
                //return DateTime.Now.ToLocalTime();
                TimeSpan difftime = (DateTime.Now.ToUniversalTime() - RawRead.Time.ToUniversalTime());
                //double a1111 = difftime.TotalSeconds;
                if (difftime.TotalHours > 24)
                    return DateTime.Now.ToLocalTime();
                else
                    return RawRead.Time.ToLocalTime();
            }
        }
        public int ReadCount
        {
            get { return RawRead.ReadCount; }
        }
        public int Antenna
        {
            get { return RawRead.Antenna; }
        }
        public TagProtocol Protocol
        {
            get { return RawRead.Tag.Protocol; }
        }
        public int RSSI
        {
            get { return RawRead.Rssi; }
        }
        public string EPC
        {
            get { return RawRead.EpcString; }
        }
        public string DataRaw
        {
            get { return ByteFormat.ToHex(RawRead.Data); }
        }
        public string Data
        {
            get { return ByteFormat.ToHex(RawRead.Data, "", " "); }
        }
        public int Frequency
        {
            get { return RawRead.Frequency; }
        }
        public int Phase
        {
            get { return RawRead.Phase; }
        }
        public bool Checked
        {
            get { return dataChecked; }
            set
            {
                dataChecked = value;
            }
        }

        //public string BrandID { 
        //    get { return RawRead.BRAND_IDENTIFIER;}
        //}

        public string GPIO {
            get {
                string gpi = "IN:", gpo = "OUT:";
                if (RawRead.GPIO != null)
                {
                    foreach (GpioPin item in RawRead.GPIO)
                    {
                        gpi += " " + item.Id + "-" + (item.Output ? "H" : "L");
                        gpo += " " + item.Id + "-" + (item.High ? "H" : "L");
                    }
                }
                return (gpi + "\r" + gpo);
            }
        }

        public string NewEPC
        {
            get { return newEpc; }
            set
            {
                newEpc = value;
            }
        }

        public int WriteStatus
        {
            get { return writeStatus; }
            set
            {
                writeStatus = value;
            }
        }

        public string VBL_Tune { get; set; } = "";

        public string VBL_NValue { get; set; } = "";

        public double Temperature
        {
            get { return temperature; }
            set
            {
                //Console.WriteLine("#### set Temperature= " + value);
                temperature = value;
            }
        }

        private double getILianTemp(TagReadData RawRead)
        {
            //Console.WriteLine("### getILianTemp");
            double temp = UNSPECTTEMP;
            if (RawRead.Data.Length > 0)
            {
                double temperature = 0;
                byte[] bdata = RawRead.Data;
                string sdata = ByteFormat.ToHex(bdata, "", "");
                if (sdata.Trim().Equals("0000"))
                {
                    return 0;
                }
                Console.WriteLine("sdata={0} {1} {2}", sdata, sdata.Substring(0, 2), sdata.Substring(2, 2));
                int t1 = Convert.ToInt32(sdata.Substring(0, 2), 16);
                int t2 = Convert.ToInt32(sdata.Substring(2, 2), 16);
                temperature = (t1 - 30) + (t2/(double)256);
                Console.WriteLine("t1={0}, t2={1}", t1-30, t2/ (double)256);
                Console.WriteLine("temperature=" + temperature);
                Console.WriteLine();
                //temp = temperature;
                temp = Math.Round(temperature, 2);//保留两位小数
            }
            return temp;
        }

        private double getJoharTemp(TagReadData RawRead)
        {
            double temp = UNSPECTTEMP;
            if (RawRead.Data.Length > 0)
            {
                //Console.WriteLine(ByteFormat.ToHex(RawRead.Data, "", " "));
                int delta1 = 0;
                double delta2 = 0;
                delta1 = tagdbByteArrayToInt(RawRead.Data, 0);
                delta2 = delta1 / 100d - 101d;
                //Console.WriteLine("d1=" + delta1 + ", d2=" + delta2);

                //2A54 0000 0000 0000 F70B F045
                byte[] bepc = RawRead.Tag.EpcBytes;
                byte[] s06 = new byte[] { bepc[8], bepc[9] };
                byte[] s07 = new byte[] { bepc[10], bepc[11] };
                //Console.WriteLine("s06=" + ByteFormat.ToHex(s06, "", ""));
                //Console.WriteLine("s07=" + ByteFormat.ToHex(s07, "", ""));

                string s_SEN_DATA = ByteFormat.ToHex(s06, "", "").Substring(1) + ByteFormat.ToHex(s07, "", "").Substring(1);
                //Console.WriteLine("s_SEN_DATA=" + s_SEN_DATA);

                //int i_SEN_DATA = int.Parse(s_SEN_DATA, System.Globalization.NumberStyles.HexNumber);
                int i_SEN_DATA = Convert.ToInt32(s_SEN_DATA, 16);
                //Console.WriteLine("i_SEN_DATA=" + i_SEN_DATA);

                double D1 = (i_SEN_DATA & 0x00F80000) >> 19;
                double D2 = ((i_SEN_DATA & 0x0007FFF8) >> 3) & 0x0000FFFF;
                //Console.WriteLine("D1=" + D1 + ", D2=" + D2);
                //Console.WriteLine(11984.47 + ":" + (21.25 + D1 + D2 / 2752 + delta2));
                double temperature = 11984.47 / (21.25 + D1 + (D2 / 2752) + delta2) - 301.57;
                Console.WriteLine("temperature=" + temperature);
                Console.WriteLine();
                //temp = temperature;
                temp = Math.Round(temperature, 2);//保留两位小数
            }
            return temp;
        }

        string old_NValue = "";
        private double UNSPECTTEMP = -100.0;
        private double getVBLTemp(string Tune, string NValue, TagReadData mergeData)
        {
            double temp = UNSPECTTEMP;
            double tune = 0.0;
            double nvalue = 0.0;
            //Console.WriteLine(mergeData.EpcString + " ,Tune=" + Tune + ", old_NValue=" + old_NValue + ", NValue=" + NValue);
            if (!old_NValue.Equals("") && old_NValue.StartsWith("3"))
            {
                if (NValue.StartsWith("0"))
                {
                    //Console.WriteLine(mergeData.EpcString + " ,Tune=" + Tune + ", old_NValue=" + old_NValue + ", NValue=" + NValue);
                    tune = parseVBLTune(Tune);
                    nvalue = parseVBLNValue(old_NValue);
                    double temperature = (nvalue + tune - 500) / 5.4817 + 24.9;
                    //temp = temperature;
                    temp = Math.Round(temperature, 2);//保留两位小数
                }
            }
            old_NValue = NValue;
            return temp;
        }

        private double parseVBLNValue(string nvalue)
        {
            double NValue = 0.0;
            string nvalue_string = "0" + nvalue.Substring(1);
            NValue = Convert.ToUInt16(nvalue_string, 16);
            //Console.WriteLine("NValue(" + nvalue_string + ")=" + NValue);
            return NValue;
        }

        private double parseVBLTune(string tune)
        {
            double Tune = 0.0;
            byte[] data = ByteFormat.FromHex(tune);
            if(data[0] == 0x0)
            {
                Tune = data[1];
            }
            else if(data[0] == 0x1)
            {
                Tune = - data[1];
            }
            //Console.WriteLine("Tune= " + Tune);
            return Tune;
        }

        private static int tagdbByteArrayToInt(byte[] data, int offset)
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

        public bool IsJohar
        {
            get { return isJohar; }
            set
            {
                isJohar = value;
            }
        }

        public bool IsVBL
        {
            get { return isVBL; }
            set
            {
                isVBL = value;
            }
        }

        public bool IsVBL_Tune
        {
            get { return isVBL_Tune; }
            set
            {
                isVBL_Tune = value;
            }
        }

        public bool IsVBL_NValue
        {
            get { return isVBL_NValue; }
            set
            {
                isVBL_NValue = value;
            }
        }

        public bool IsILian
        {
            get { return isILian; }
            set
            {
                isILian = value;
            }
        }

        #region INotifyPropertyChanged Members

        public event PropertyChangedEventHandler PropertyChanged;

        private void OnPropertyChanged(string name)
        {
            PropertyChangedEventArgs td = new PropertyChangedEventArgs(name);
            try
            {

                if (null != PropertyChanged)
                {
                    PropertyChanged(this, td);
                }
            }
            finally
            {
                td = null;
            }
        }

        #endregion
    }

    public class TagReadRecordBindingList : SortableBindingList<TagReadRecord>
    {
        protected override Comparison<TagReadRecord> GetComparer(PropertyDescriptor prop)
        {
            Comparison<TagReadRecord> comparer = null;
            switch (prop.Name)
            {
                case "TimeStamp":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return DateTime.Compare(a.TimeStamp, b.TimeStamp);
                    });
                    break;
                case "SerialNumber":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return (int)(a.SerialNumber - b.SerialNumber);
                    });
                    break;
                case "ReadCount":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return a.ReadCount - b.ReadCount;
                    });
                    break;
                case "Antenna":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return a.Antenna - b.Antenna;
                    });
                    break;
                case "Protocol":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.Protocol.ToString(), b.Protocol.ToString());
                    });
                    break;
                case "RSSI":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return a.RSSI - b.RSSI;
                    });
                    break;
                case "EPC":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.EPC, b.EPC);
                    });
                    break;
                case "EPCInASCII":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.EPCInASCII, b.EPCInASCII);
                    });
                    break;
                case "EPCInReverseBase36":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.EPCInReverseBase36, b.EPCInReverseBase36);
                    });
                    break;
                case "DataInASCII":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.DataInASCII, b.DataInASCII);
                    });
                    break;
                //case "DataInReverseBase36":
                //    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                //    {
                //        return String.Compare(a.DataInReverseBase36, b.DataInReverseBase36);
                //    });
                //    break;
                case "Data":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.Data, b.Data);
                    });
                    break;
                case "Frequency":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return a.Frequency - b.Frequency;
                    });
                    break;
                case "Phase":
                    comparer = new Comparison<TagReadRecord>(delegate(TagReadRecord a, TagReadRecord b)
                    {
                        return a.Phase - b.Phase;
                    });
                    break;
                case "NewEPC":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.NewEPC, b.NewEPC);
                    });
                    break;

                case "WriteStatus":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        return a.WriteStatus - b.WriteStatus;
                    });
                    break;
                case "Temperature":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        //Console.WriteLine("#### compare Temperature");
                        if (a.Temperature - b.Temperature != 0)
                            return 1;
                        else
                            return 0;
                    });
                    break;

                case "VBL_Tune":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.VBL_Tune, b.VBL_Tune);
                    });
                    break;
                case "VBL_NValue":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.VBL_NValue, b.VBL_NValue);
                    });
                    break;
            }
            return comparer;
        }
    }

    public class TagDatabase
    {
        /// <summary>
        /// TagReadData model (backs data grid display)
        /// </summary>
        TagReadRecordBindingList _tagList = new TagReadRecordBindingList();

        /// <summary>
        /// Cache unique by data checkbox status set in read / write options | enable filter 
        /// </summary>
        public bool chkbxUniqueByData = false;

        /// <summary>
        /// Cache unique by Antenna checkbox status set in Display options
        /// </summary>
        public bool chkbxUniqueByAntenna = false;

        /// <summary>
        /// Cache unique by Frequency checkbox status set in Display options
        /// </summary>
        public bool chkbxUniqueByFrequency = false;

        /// <summary>
        /// Cache show failed data reads checkbox status set in read / write options | enable filter 
        /// </summary>
        public bool chkbxShowFailedDataReads = false;

        /// <summary>
        /// EPC index into tag list
        /// </summary>
        public Dictionary<string, TagReadRecord> EpcIndex = new Dictionary<string, TagReadRecord>();

        static long UniqueTagCounts = 0;
        static long TotalTagCounts = 0;
        
        public TagDatabase()
        {
            // GUI can't keep up with fast updates, so disable automatic triggers
            _tagList.RaiseListChangedEvents = false;
        }

        public TagReadRecordBindingList TagList
        {
            get { return _tagList; }
        }
        public long UniqueTagCount
        {
            get { return UniqueTagCounts; }
        }
        public long TotalTagCount
        {
            get { return TotalTagCounts; }
        }

        public bool tagdbIsJohar { get; set; }

        public bool tagdbIsVBL { get; set; }
        public bool tagdbIsVBL_Tune { get;  set; }
        public bool tagdbIsVBL_NValue { get;  set; }

        public bool tagdbIsILian { get; set; }

        public void Clear()
        {
            EpcIndex.Clear();
            UniqueTagCounts = 0;
            TotalTagCounts = 0;
            _tagList.Clear();
            // Clear doesn't fire notifications on its own
            _tagList.ResetBindings();
        }

        public void Add(TagReadData addData)
        {
            lock (new Object())
            {
                string key = null;

                if (tagdbIsJohar)
                {
                    key = addData.EpcString.Substring(0, 4);
                    //Console.WriteLine("*** tagdb add key=" + key);
                }
                else if (tagdbIsVBL || tagdbIsILian)
                {
                    key = addData.EpcString; //if only keying on EPCID
                }
                else
                {
                    if (chkbxUniqueByData)
                    {
                        if (true == chkbxShowFailedDataReads)
                        {
                            //key = addData.EpcString + ByteFormat.ToHex(addData.Data, "", " ");
                            // When CHECKED - Add the entry to the database. This will result in
                            // potentially two entries for every tag: one with the requested data and one without.
                            if (addData.Data.Length > 0)
                            {
                                key = addData.EpcString + ByteFormat.ToHex(addData.Data, "", " ");
                            }
                            else
                            {
                                key = addData.EpcString + "";
                            }

                        }
                        else if ((false == chkbxShowFailedDataReads) && (addData.Data.Length == 0))
                        {
                            // When UNCHECKED (default) - If the embedded read data fails (data.length==0) then don't add the entry to
                            // the database, thus it won't be displayed.
                            return;
                        }
                        else
                        {
                            key = addData.EpcString + ByteFormat.ToHex(addData.Data, "", " ");
                        }
                    }
                    else
                    {
                        key = addData.EpcString; //if only keying on EPCID
                    }
                }
                //if (chkbxUniqueByAntenna)
                //{
                //    key += addData.Antenna.ToString();
                //}
                //if (chkbxUniqueByFrequency)
                //{
                //    key += addData.Frequency.ToString();
                //}

                UniqueTagCounts = 0;
                TotalTagCounts = 0;
                
                if (!EpcIndex.ContainsKey(key))
                {
                    TagReadRecord value = new TagReadRecord(addData);
                    value.SerialNumber = (uint)EpcIndex.Count + 1;
                    if (tagdbIsJohar)
                    {
                        Console.WriteLine("### IsJohar=" + tagdbIsJohar);
                        value.IsJohar = true;
                        value.Temperature = 0.0;// tagdbGetTemperature(addData); //ToDo add temperature

                    }
                    else if (tagdbIsVBL)
                    {
                        value.IsVBL = true;
                        if (tagdbIsVBL_Tune == true && tagdbIsVBL_NValue == false)
                        {
                            value.IsVBL_Tune = true;
                            value.IsVBL_NValue = false;
                        }
                        else if (tagdbIsVBL_Tune == false && tagdbIsVBL_NValue == true)
                        {
                            value.IsVBL_Tune = false;
                            value.IsVBL_NValue = true;
                        }
                    }
                    else if (tagdbIsILian)
                    {
                        value.IsILian = true;
                    }

                    //Console.WriteLine("### gpio=" + value.GPIO);

                    _tagList.Add(value);
                    EpcIndex.Add(key, value);
                    //Call this method to calculate total tag reads and unique tag read counts 
                    UpdateTagCountTextBox(EpcIndex);
                    //Console.WriteLine("tagdb add ["+key+"]");
                }
                else
                {
                    if (tagdbIsVBL)
                    {
                        EpcIndex[key].IsVBL = true;
                        if (tagdbIsVBL_Tune == true && tagdbIsVBL_NValue == false)
                        {
                            EpcIndex[key].IsVBL_Tune = true;
                            EpcIndex[key].IsVBL_NValue = false;
                            if (EpcIndex[key].VBL_Tune.Trim().Equals(""))
                                EpcIndex[key].VBL_Tune = ByteFormat.ToHex(addData.Data, "", "");
                        }
                        else if (tagdbIsVBL_Tune == false && tagdbIsVBL_NValue == true)
                        {
                            EpcIndex[key].IsVBL_Tune = false;
                            EpcIndex[key].IsVBL_NValue = true;
                            EpcIndex[key].VBL_NValue = ByteFormat.ToHex(addData.Data, "", "");
                        }
                    }

                    EpcIndex[key].Update(addData); //ToDo update temperature
                    UpdateTagCountTextBox(EpcIndex);
                }
            }
        }

        //Calculate total tag reads and unique tag reads.
        public void UpdateTagCountTextBox(Dictionary<string, TagReadRecord> EpcIndex)
        {
            UniqueTagCounts += EpcIndex.Count;
            TagReadRecord[] dataRecord = new TagReadRecord[EpcIndex.Count];
            EpcIndex.Values.CopyTo(dataRecord, 0);
            TotalTagCounts = 0;
            for (int i = 0; i < dataRecord.Length; i++)
            {
                TotalTagCounts += dataRecord[i].ReadCount;
            }
        }

        public void AddRange(ICollection<TagReadData> reads)
        {
            foreach (TagReadData read in reads)
            {
                Add(read);
            }
        }

        /// <summary>
        /// Manually release change events
        /// </summary>
        public void Repaint()
        {
            _tagList.RaiseListChangedEvents = true;

            //Causes a control bound to the BindingSource to reread all the items in the list and refresh their displayed values.
            _tagList.ResetBindings();

            _tagList.RaiseListChangedEvents = false;
        }

        /// <summary>
        /// Generates a random string with the given length
        /// </summary>
        /// <param name="size">Size of the string</param>
        /// <param name="lowerCase">If true, generate lowercase string</param>
        /// <returns>Random string</returns>
        private string RandomString(int size, bool lowerCase)
        {
            StringBuilder builder = new StringBuilder();
            Random random = new Random();
            char ch;
            for (int i = 0; i < size; i++)
            {
                ch = Convert.ToChar(Convert.ToInt32(Math.Floor(26 * random.NextDouble() + 65)));
                builder.Append(ch);
            }
            if (lowerCase)
                return builder.ToString().ToLower();
            return builder.ToString();
        }
    }
}
