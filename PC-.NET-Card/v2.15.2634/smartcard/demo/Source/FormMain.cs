/***********************************************
 * CONFIDENTIAL AND PROPRIETARY 
 * 
 * The source code and other information contained herein is the confidential and exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published, 
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * 
 * Copyright ZIH Corp. 2018
 * 
 * ALL RIGHTS RESERVED
 ***********************************************/

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Threading;
using System.Windows.Forms;
using ZCSmartCardLib;

namespace ZCSCDevDemo {

    public partial class FormMain : Form {

        #region Declarations
        private string cardSlot = string.Empty;
        private string virtualSlot = string.Empty;
        private PrinterOps prnOps = null;
        private ReaderLib scReader = null;
        #endregion

        #region Main Form

        public FormMain() {
            InitializeComponent();
        }

        private void FormMain_Load(object sender, EventArgs e) {
            string errMsg = string.Empty;

            try {
                prnOps = new PrinterOps();

                if (!CheckForPrinter(ref errMsg)) {
                    throw new ArgumentException(errMsg);
                }

                if (!ReaderSearch(out errMsg)) {
                    throw new ArgumentException(errMsg);
                }

                this.lblVersion.Text += GetZCSmartCardLibVersion();
                SetLblReaderVersion();

                CboCardTypeInit();
            } catch (Exception ex) {
                MessageBox.Show("Form Load Error: " + ex.Message);
                Application.Exit();
            } finally {
                scReader = null;
                prnOps = null;
            }
        }
        #endregion

        #region Buttons

        private void BtnExit_Click(object sender, EventArgs e) {
            try {
                Application.Exit();
            } catch (Exception ex) {
                MessageBox.Show(ex.Message);
            }
        }

        private void BtnTest_Click(object sender, EventArgs e) {
            string errMsg = string.Empty;

            try {
                prnOps = new PrinterOps();

                if (!CheckForPrinter(ref errMsg)) {
                    throw new ArgumentException(errMsg);
                }

                if (!prnOps.OpenConnection())
                    throw new ArgumentException(prnOps.printerError);

                string alarmDescr = string.Empty;
                if (prnOps.IsAlarmHandling(out alarmDescr)) {
                    errMsg = "Alarm: " + alarmDescr + " : cannot run test";
                    throw new Exception(errMsg);
                }

                if (string.IsNullOrEmpty(cardSlot)) {
                    throw new Exception("No reader selected");
                }

                scReader = new ReaderLib();

                switch (cboCardType.Text) {
                    case "MIFARE CLASSIC":
                    case "MIFARE ULTRALIGHT":
                    case "MIFARE DESFIRE":
                        if (cardSlot.Contains("Elatec")) {
                            if (!scReader.SetRFHF(virtualSlot, true)) {
                                throw new Exception(scReader.ReaderError);
                            }
                        }
                        RunTest(out errMsg);
                        if (!scReader.SetRFHF(virtualSlot, false)) {
                            throw new Exception(scReader.ReaderError);
                        }
                        if (!string.IsNullOrEmpty(errMsg)) {
                            throw new Exception(errMsg);
                        }
                        break;

                    case "PROX":
                    case "HITAG":
                        if (cardSlot.Contains("Elatec")) {
                            if (!scReader.SetRFLF(virtualSlot, true)) {
                                throw new Exception(scReader.ReaderError);
                            }
                        }
                        RunTest(out errMsg);
                        if (!scReader.SetRFLF(virtualSlot, false)) {
                            throw new Exception(scReader.ReaderError);
                        }
                        if (!string.IsNullOrEmpty(errMsg)) {
                            throw new Exception(errMsg);
                        }
                        break;

                    case "AT88SC0104C":
                        RunTest(out errMsg);
                        if (!string.IsNullOrEmpty(errMsg)) {
                            throw new Exception(errMsg);
                        }
                        break;

                    case "SLEXX42":
                    case "SLEXX28":
                        RunTest(out errMsg);
                        if (!string.IsNullOrEmpty(errMsg)) {
                            throw new Exception(errMsg);
                        }
                        break;

                    default:
                        throw new Exception("Unknown Test");
                }
            } catch (Exception ex) {
                MessageBox.Show("Test Click Error : " + ex.Message);
            } finally {
                scReader = null;
                if (prnOps != null) {
                    prnOps.CloseConnection();
                    prnOps = null;
                }
            }
        }
        #endregion

        #region Combo Boxes

        private void CboCardGroup_SelectedIndexChanged(object sender, EventArgs e) {
            string cardType = string.Empty;

            try {
                switch (cboCardGroup.Text) {
                    case "Contact Tests":
                        cboCardType.Items.Clear();
                        cboCardType.Text = string.Empty;
                        cboCardType.Items.Add("AT88SC0104C");
                        cboCardType.Items.Add("SLEXX28");
                        cboCardType.Items.Add("SLEXX42");
                        cardType = "contact";
                        break;
                    case "Contactless HF Tests":
                        cboCardType.Items.Clear();
                        cboCardType.Text = string.Empty;
                        cboCardType.Items.Add("MIFARE CLASSIC");
                        cboCardType.Items.Add("MIFARE ULTRALIGHT");
                        cboCardType.Items.Add("MIFARE DESFIRE");
                        cardType = "hf";
                        break;
                    case "Contactless LF Tests":
                        cboCardType.Items.Clear();
                        cboCardType.Text = string.Empty;
                        cboCardType.Items.Add("PROX");
                        cboCardType.Items.Add("HITAG");
                        cardType = "lf";
                        break;
                    default:
                        throw new Exception("Unknow Card Type");
                }
                SetOffsets(cardType);
            } catch (Exception ex) {
                MessageBox.Show("Card Type Selection Error: " + ex.Message);
            }
        }

        private void CboCardTypeInit() {
            try {
                cboCardGroup.Items.Clear();
                cboCardGroup.Items.Add("Contact Tests");
                cboCardGroup.Items.Add("Contactless HF Tests");

                if (lblReaderVersion.Text.Contains("/P")) {
                    cboCardGroup.Items.Add("Contactless LF Tests");
                }
            } catch {
            }
        }

        #endregion

        #region Support

        private bool CheckForPrinter(ref string errMsg) {
            bool found = false;
            try {
                if (!prnOps.DiscoverUSBPrinters()) {
                    throw new Exception(prnOps.printerError);
                }
                found = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            }
            return found;
        }

        private int Obj2Int(object value) {
            int ret = 0;
            try {
                ret = Convert.ToInt32(value);
            } catch {
                ret = 0;
            }
            return ret;
        }

        private void LblMsgSet(string text, Color color) {
            lblMsg.Text = text;
            lblMsg.ForeColor = color;
            lblMsg.Visible = true;
            Application.DoEvents();
        }

        private void LblReaderSet(string text, Color color) {
            lblReader.Text = text;
            lblReader.ForeColor = color;
            Application.DoEvents();
        }

        private string GetZCSmartCardLibVersion() {
            string version = string.Empty;
            SmartCardLib scLib = null;

            try {
                scLib = new SmartCardLib();
                version = scLib.GetVersion();
            } catch {
                version = "Unknown";
            } finally {
                scLib = null;
            }
            return version;
        }

        private byte[] ImageToByteArray(string filename) {
            Image img = Image.FromFile(filename);
            using (MemoryStream ms = new MemoryStream()) {
                img.Save(ms, ImageFormat.Bmp);
                return ms.ToArray();
            }
        }

        private bool ReaderSearch(out string errMsg) {
            errMsg = string.Empty;
            bool ret = false;
            ReaderLib scReader = new ReaderLib();

            try
            {
                List<string> readers = scReader.GetSmartCardReaders();
                foreach (string r in readers) {
                    if (r.Contains("Slot 0")) {
                        cardSlot = r;
                    } else if (r.Contains("Slot 5")) {
                        virtualSlot = r;
                    }
                }
                if (string.IsNullOrEmpty(cardSlot)) {
                    throw new Exception("Smart Card Driver not Found");
                }
                if (cardSlot.Contains("Elatec") && string.IsNullOrEmpty(virtualSlot)) {
                    throw new Exception("Configuration Slot not Available");
                }
                LblReaderSet(cardSlot, Color.Black);
                ret = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            } finally {
                scReader = null;
            }
            return ret;
        }

        private void RunTest(out string errMsg) {
            errMsg = string.Empty;

            ContactTests contactTests = null;
            ContactlessHFTests contactless_hf = null;
            ContactlessLFTests contactless_lf = null;

            bool passed = false;
            try {
                Cursor.Current = Cursors.WaitCursor;

                switch (cboCardGroup.Text) {
                    case "Contact Tests":
                        contactTests = new ContactTests();
                        break;

                    case "Contactless HF Tests":
                        contactless_hf = new ContactlessHFTests();
                        break;

                    default:
                        contactless_lf = new ContactlessLFTests();
                        break;
                }

                string disp = "Testing " + cboCardType.Text;
                LblMsgSet(disp, Color.DarkBlue);

                prnOps.SetJobSource("Feeder");
                if (cbTestPrint.Checked) {
                    prnOps.SetJobDestination("Hold");
                } else {
                    prnOps.SetJobDestination("Eject");
                }

                switch (cboCardType.Text) {
                    #region HF Contactless Smart Cards
                    case "MIFARE CLASSIC":
                        if (!prnOps.ContactlessSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactless_hf.MifareTest(cardSlot, ref errMsg);
                        }
                        break;

                    case "MIFARE ULTRALIGHT":
                        if (!prnOps.ContactlessSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactless_hf.MifareUltralightTest(cardSlot, ref errMsg);
                        }
                        break;

                    case "MIFARE DESFIRE":
                        if (!prnOps.ContactlessSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactless_hf.DesfireTest(cardSlot, ref errMsg);
                        }
                        break;
                    #endregion

                    #region LF Contactless Smart Cards
                    case "PROX":
                        if (!prnOps.ContactlessSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactless_lf.HIDProxTest(cardSlot, ref errMsg);
                        }
                        break;

                    case "HITAG":
                        if (!prnOps.ContactlessSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactless_lf.HitagTest(cardSlot, ref errMsg);
                        }
                        break;
                    #endregion

                    #region Contact Smart Cards
                    case "AT88SC0104C":
                        if (!prnOps.ContactSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactTests.MemoryTestAT88SC0104C(cardSlot, ref errMsg);
                        }
                        break;

                    case "SLEXX42":
                        if (!prnOps.ContactSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactTests.MemoryTestSLEXX42(cardSlot, ref errMsg);
                        }
                        break;

                    case "SLEXX28":
                        if (!prnOps.ContactSmartCardJob(Obj2Int(this.nbOffset.Value))) {
                            errMsg = prnOps.printerError;
                        } else {
                            passed = contactTests.MemoryTestSLEXX28(cardSlot, ref errMsg);
                        }
                        break;
                        #endregion
                }
                
                Thread.Sleep(500);

                if (!prnOps.ResumeJob()) {
                    errMsg = "Resume Job Error: " + prnOps.printerError;
                    passed = false;
                }

                if (cbTestPrint.Checked && !passed) {
                    prnOps.EjectCard();
                }

                if (passed && cbTestPrint.Checked) {
                    prnOps.SetJobSource("Internal");
                    prnOps.SetJobDestination("Eject");
                    if (!prnOps.PrintTestCard()) {
                        throw new Exception(prnOps.printerError);
                    }
                }

                Cursor.Current = Cursors.Default;
            } catch (Exception ex) {
                errMsg = ex.Message;
            } finally {
                Cursor.Current = Cursors.Default;
                contactless_hf = null;
                contactTests = null;
            }

            string msg = (passed) ? cboCardType.Text + " Passed" : cboCardType.Text + " " + errMsg;
            Color color = (passed) ? Color.DarkBlue : Color.DarkRed;
            LblMsgSet(msg, color);
        }

        private void SetLblReaderVersion() {
            try {
              this.lblReaderVersion.Text = new ReaderLib().GetVersionString(virtualSlot);
              this.lblReaderVersion.Refresh();
            } catch {
            }
        }

        private bool SetOffsets(string cardType) {
            bool offsetsSet = false;
            PrinterOps prnOps = new PrinterOps();

            try {
                prnOps = new PrinterOps();
                if (!prnOps.DiscoverUSBPrinters())
                    throw new Exception(prnOps.printerError);

                if (!prnOps.OpenConnection()) {
                    prnOps = null;
                    throw new Exception(prnOps.printerError);
                }

                int max = 0;
                int min = 0;
                prnOps.GetSmartOffsetRange(cardType, out min, out max);

                if (this.nbOffset.Value < min) {
                    this.nbOffset.Value = 0;
                    this.nbOffset.Minimum = min;
                    this.nbOffset.Value = min;
                } else {
                    this.nbOffset.Minimum = min;
                }
                if (this.nbOffset.Value > max) {
                    this.nbOffset.Value = 0;
                    this.nbOffset.Maximum = max;
                    this.nbOffset.Value = max;
                } else {
                    this.nbOffset.Maximum = max;
                }

                string offsetRange = "Offset range is " + min.ToString() + " to " + max.ToString();
                System.Windows.Forms.ToolTip toolTip = new System.Windows.Forms.ToolTip();
                toolTip.SetToolTip(this.nbOffset, offsetRange);
                offsetsSet = true;

            } catch {
            } finally {
                if (prnOps != null) {
                    prnOps.CloseConnection();
                    prnOps = null;
                }
            }
            return offsetsSet;
        }

        #endregion
    }
}
