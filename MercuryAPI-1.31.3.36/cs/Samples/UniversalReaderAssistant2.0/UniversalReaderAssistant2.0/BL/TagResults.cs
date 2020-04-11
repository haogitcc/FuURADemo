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

        protected SensorType sensorType = SensorType.Normal;
        protected SensorSubType sensorSubType = SensorSubType.NONE;

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
            if (sensorType == SensorType.Johar)
            {
                double temp = getJoharTemp(mergeData);
                if (temp != UNSPECTTEMP)
                    temperature = temp;
            }
            else if(sensorType == SensorType.VBL)
            {
                if (sensorSubType == SensorSubType.VBL_TUNE)
                {
                    if (tuneValue.Trim().Equals(""))
                        tuneValue = ByteFormat.ToHex(mergeData.Data, "", "");
                }
                else if (sensorSubType == SensorSubType.VBL_NValue)
                {
                    temperatureCodeValue = ByteFormat.ToHex(mergeData.Data, "", "");
                    if(!tuneValue.Trim().Equals("") && !temperatureCodeValue.Trim().Equals(""))
                    {
                        double temp = getVBLTemp(tuneValue, temperatureCodeValue, mergeData);
                        if(temp != UNSPECTTEMP)
                            temperature = temp;
                    }
                }
            }
            else if (sensorType == SensorType.ILian)
            {
                double temp = getILianTemp(mergeData);
                if (temp != UNSPECTTEMP)
                    temperature = temp; 
            }
            else if (sensorType == SensorType.RFMicronMagnusS3)
            {
                if (sensorSubType == SensorSubType.RFMicroMagnusS3_CalibratedCode)
                {
                    if (tuneValue.Trim().Equals(""))
                        tuneValue = ByteFormat.ToHex(mergeData.Data, "", "");
                }
                else if (sensorSubType == SensorSubType.RFMicroMagnusS3_TemperatureCode)
                {
                    temperatureCodeValue = ByteFormat.ToHex(mergeData.Data, "", "");
                    if (!tuneValue.Trim().Equals("") && !temperatureCodeValue.Trim().Equals(""))
                    {
                        double temp = getRFMicronMagnusS3Temp(tuneValue, temperatureCodeValue, mergeData);
                        if (temp != UNSPECTTEMP)
                            temperature = temp;
                    }
                }
            }

            OnPropertyChanged(null);
        }

        private double getRFMicronMagnusS3Temp(string TuneValue, string TemperatureCode, TagReadData mergeData)
        {
            double temp = 0.0;
            if (TemperatureCode.Length < 4)
                return temp;
            int TEMP_CODE = Convert.ToInt32(TemperatureCode, 16) & 0x00000FFF;
            return GetRFMicroTemp(TuneValue, TEMP_CODE);
        }

        private double GetRFMicroTemp(string CalibratedCode, int TEMP_CODE)
        {
            if (CalibratedCode == null || CalibratedCode.Length < 16)
                return 0.0;
            String USER_8H = CalibratedCode.Substring(0, 4);
            String USER_9H = CalibratedCode.Substring(4, 4);
            String USER_AH = CalibratedCode.Substring(8, 4);
            String USER_BH = CalibratedCode.Substring(12, 4);
            Console.WriteLine(string.Format("USER_8H={0}, USER_9H={1}, USER_AH={2}, USER_BH={3}", USER_8H, USER_9H, USER_AH, USER_BH));

            int CRC = Convert.ToInt32(USER_8H, 16) & 0x0000FFFF;
            int tempCRC = Convert.ToInt32(CRC16(CalibratedCode.Substring(4, 12)), 16);
            int CODE1 = (Convert.ToInt32(USER_9H, 16) & 0x0000FFF0) >> 4;
            int TEMP1 = (Convert.ToInt32(USER_9H, 16) & 0x0000000F) << 7 | ((Convert.ToInt32(USER_AH, 16) & 0x0000FE00) >> 9);
            int CODE2 = (Convert.ToInt32(USER_AH, 16) & 0x000001FF) << 3 | ((Convert.ToInt32(USER_BH, 16) & 0x0000E000) >> 13);
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
            Console.WriteLine("============================");
            return Math.Round(TEMP_in_Celsius, 2);
        }

        private string CRC16(string dataHexString)
        {
            int numBytes = dataHexString.Length / 2;
            byte[] dataByteArray = new byte[numBytes];
            for (int b = 0; b < numBytes; b++)
            {
                dataByteArray[numBytes - 1 - b] = Convert.ToByte(dataHexString.Substring(2 * b, 2), 16);
            }
            BitArray data = new BitArray(dataByteArray);
            BitArray CRC = new BitArray(16);
            CRC.SetAll(true);
            for (int j = data.Length - 1; j >= 0; j--)
            {
                bool newBit = CRC[15] ^ data[j];
                for (int i = 15; i >= 1; i--)
                {
                    if (i == 12 || i == 15)
                    {
                        CRC[i] = CRC[i - 1] ^ newBit;
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

            string CRCword = Convert.ToString(CRCbytes[1], 16).PadLeft(2, '0')
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

        public SensorType SensorType
        {
            get { return sensorType; }
            set
            {
                sensorType = value;
            }
        }

        public SensorSubType SensorSubType
        {
            get { return sensorSubType; }
            set
            {
                sensorSubType = value;
            }
        }

        public double Temperature
        {
            get { return temperature; }
            set
            {
                //Console.WriteLine("#### set Temperature= " + value);
                temperature = value;
            }
        }

        public string TuneValue 
        {
            get { return tuneValue; } 
            set { tuneValue = value; }
        }
        public string TemperatureCode
        {
            get { return temperatureCodeValue; }
            set { temperatureCodeValue = value; }
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
                if (bepc.Count() < 11)
                    return 0;
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
        private string tuneValue = "";
        private string temperatureCodeValue = "";

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

                case "TuneValue":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.TuneValue, b.TuneValue);
                    });
                    break;
                case "TemperatureCode":
                    comparer = new Comparison<TagReadRecord>(delegate (TagReadRecord a, TagReadRecord b)
                    {
                        return String.Compare(a.TemperatureCode, b.TemperatureCode);
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

        private SensorType tagdbSensorType = SensorType.Normal;

        private SensorSubType tagdbSensorSubType = SensorSubType.NONE;

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
        
        public SensorType SensorType { 
            get { return tagdbSensorType; }
            set { tagdbSensorType = value; }
        }

        public SensorSubType SensorSubType { 
            get { return tagdbSensorSubType; }
            set { tagdbSensorSubType = value; }
        }

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

                if (tagdbSensorType == SensorType.Johar)
                {
                    key = addData.EpcString.Substring(0, 4);
                    //Console.WriteLine("*** tagdb add key=" + key);
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
                    value.SensorType = tagdbSensorType;
                    value.SensorSubType = tagdbSensorSubType;
                    Console.WriteLine(string.Format("Sensor={0}, Sub={1}", tagdbSensorType, tagdbSensorSubType));

                    _tagList.Add(value);
                    EpcIndex.Add(key, value);
                    //Call this method to calculate total tag reads and unique tag read counts 
                    UpdateTagCountTextBox(EpcIndex);
                    //Console.WriteLine("tagdb add ["+key+"]");
                }
                else
                {
                    EpcIndex[key].SensorSubType = tagdbSensorSubType;
                    if (tagdbSensorType == SensorType.VBL)
                    {
                        if (tagdbSensorSubType == SensorSubType.VBL_TUNE)
                        {
                            if (EpcIndex[key].TuneValue.Trim().Equals(""))
                                EpcIndex[key].TuneValue = ByteFormat.ToHex(addData.Data, "", "");
                        }
                        else if (tagdbSensorSubType == SensorSubType.VBL_NValue)
                        {
                            EpcIndex[key].TemperatureCode = ByteFormat.ToHex(addData.Data, "", "");
                        }
                    }
                    if(tagdbSensorType == SensorType.RFMicronMagnusS3)
                    {
                        if (tagdbSensorSubType == SensorSubType.RFMicroMagnusS3_CalibratedCode)
                        {
                            if (EpcIndex[key].TuneValue.Trim().Equals(""))
                                EpcIndex[key].TuneValue = ByteFormat.ToHex(addData.Data, "", "");
                            else if(tagdbSensorSubType == SensorSubType.RFMicroMagnusS3_TemperatureCode)
                                EpcIndex[key].TemperatureCode = ByteFormat.ToHex(addData.Data, "", "");
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

namespace ThingMagic.URA2
{
    public enum SensorType
    {
        Normal = 0,
        Johar = 1,
        VBL = 2,
        ILian = 3,
        RFMicronMagnusS3 = 4
    }

    public enum SensorSubType
    {
        NONE = 00,
        //VBL
        VBL_NONE   = 10,
        VBL_TUNE   = 11,
        VBL_NValue = 12,

        //RF Micron Magnus-S3
        RFMicroMagnusS3_NONE            = 20,
        RFMicroMagnusS3_SensorCode      = 21,
        RFMicroMagnusS3_OnChipRSSI      = 22,
        RFMicroMagnusS3_CalibratedCode  = 23,
        RFMicroMagnusS3_TemperatureCode = 24
    }
}
