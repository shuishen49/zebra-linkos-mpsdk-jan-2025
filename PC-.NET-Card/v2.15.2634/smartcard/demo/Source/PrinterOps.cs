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
using System.IO;
using System.Threading;
using Zebra.Sdk.Card.Containers;
using Zebra.Sdk.Card.Enumerations;
using Zebra.Sdk.Card.Graphics;
using Zebra.Sdk.Card.Graphics.Enumerations;
using Zebra.Sdk.Card.Job;
using Zebra.Sdk.Card.Printer;
using Zebra.Sdk.Card.Printer.Discovery;
using Zebra.Sdk.Comm;
using Zebra.Sdk.Printer.Discovery;

namespace ZCSCDevDemo {

    public class PrinterOps {

        #region Private Declarations
        private Connection connection = null;
        private ZebraPrinterZmotif printer = null;
        private int jobID = 0;
        private int loopCount = 20;
        private int CARD_FEED_TIMEOUT = 30000;
        #endregion

        #region Properties
        public string printerError { get; set; }
        public DiscoveredUsbPrinter UsbPrinter { get; set; }
        public List<DiscoveredUsbPrinter> UsbPrinters { get; set; }
        #endregion

        #region Class Initialization
        public PrinterOps() {
            printerError = string.Empty;
        }
        #endregion

        #region Discovery

        /// <summary>
        /// Discovers USB connected Zebra Card Printers
        /// </summary>
        /// <param name="count">number printers discovered</param>
        /// <returns>true if no errors are encountered</returns>
        public bool DiscoverUSBPrinters() {
            bool discovered = false;

            try {
                UsbPrinters = UsbDiscoverer.GetZebraUsbPrinters(new ZebraCardPrinterFilter());
                if (UsbPrinters == null || UsbPrinters.Count == 0) {
                    throw new Exception("No printers found");
                }
                UsbPrinter = UsbPrinters[0];
                discovered = true;
            } catch (Exception ex) {
                printerError = ex.Message;
            }
            return discovered;
        }
        #endregion

        #region Connection
        /// <summary>
        /// Opens a connection to a discovered printer
        /// </summary>
        /// <returns>true if connection is successful</returns>
        public bool OpenConnection() {
            bool connected = false;

            try {
                connection = UsbPrinter.GetConnection();
                if (connection == null) {
                    throw new Exception("Unable to connect to an USB printer");
                }

                connection.Open();
                printer = ZebraCardPrinterFactory.GetZmotifPrinter(connection);
                if (printer == null) {
                    throw new Exception("Unable to get an instance to an USB printer");
                }

                connected = true;
            } catch (Exception ex) {
                connection = null;
                printerError = ex.Message;
            }
            return connected;
        }

        /// <summary>
        /// Close the connection to a printer
        /// </summary>
        public void CloseConnection() {
            try {
                if (connection != null && connection.Connected) {
                    connection.Close();
                }
            } catch (Exception ex) {
                printerError = ex.Message;
            } finally {
                connection = null;
            }
        }
        #endregion

        #region Printer

        /// <summary>
        /// Ejects a card from a printer
        /// </summary>
        /// <returns>true if successful</returns>
        public bool EjectCard() {
            bool ejected = false;
            try {
                this.printer.EjectCard();
                ejected = true;
            } catch {
            }

            return ejected;
        }

        /// <summary>
        /// Determines if a printer is in alarm state
        /// </summary>
        /// <param name="descr">description of the alarm</param>
        /// <returns>true if printer is in alarm state</returns>
        public bool IsAlarmHandling(out string descr) {
            bool isAlarm = false;
            descr = string.Empty;

            try {
                PrinterStatusInfo psi = printer.GetPrinterStatus();
                if (psi.Status.Equals("alarm_handling")) {
                    descr = psi.AlarmInfo.Description;
                    isAlarm = true;
                }
            } catch {
                isAlarm = true;
            }
            return isAlarm;
        }
        #endregion

        #region Job
        /// <summary>
        /// Loops until a smart card is a encoding station
        /// </summary>
        /// <param name="loopCount">number of tries before giving up</param>
        /// <returns>true if card is at encoder station</returns>
        public bool PollJobForAtStation(int loopCount) {
            bool atStation = false;
            JobStatusInfo jobStatusInfo = new JobStatusInfo();

            long start = Math.Abs(Environment.TickCount);
            while (!atStation && loopCount > 0) {
                jobStatusInfo = printer.GetJobStatus(jobID);

                if (jobStatusInfo.ContactlessSmartCardStatus.Contains("at_station") || jobStatusInfo.ContactSmartCardStatus.Contains("at_station")) {
                    atStation = true;
                } else if (jobStatusInfo.PrintStatus.Contains("error") || jobStatusInfo.PrintStatus.Contains("cancelled")) {
                    break;
                } else if (jobStatusInfo.ErrorInfo.Value > 0) {
                    printer.Cancel(jobID);
                } else if (jobStatusInfo.PrintStatus.Contains("in_progress") && jobStatusInfo.CardPosition.Contains("feeding")) {
                    if (Math.Abs(Environment.TickCount) > start + CARD_FEED_TIMEOUT) {
                        printer.Cancel(jobID);
                        break;
                    }
                }
                loopCount--;
                if (!atStation) {
                    Thread.Sleep(1000);
                }
            }
            return atStation;
        }

        /// <summary>
        /// Loops until a job is done
        /// </summary>
        /// <param name="loopCount">number of tries before giving up</param>
        /// <returns>true if job done OK</returns>
        public bool PollJobForDoneOk(int loopCount) {
            JobStatusInfo jobStatusInfo = new JobStatusInfo();

            bool done = false;
            long start = Math.Abs(Environment.TickCount);
            while (!done && loopCount > 0) {
                jobStatusInfo = printer.GetJobStatus(jobID);

                if (jobStatusInfo.PrintStatus.Contains("done_ok")) {
                    done = true;
                } else if (jobStatusInfo.PrintStatus.Contains("error") || jobStatusInfo.PrintStatus.Contains("cancelled")) {
                    break;
                } else if (jobStatusInfo.ErrorInfo.Value > 0) {
                    printer.Cancel(jobID);
                } else if (jobStatusInfo.CardPosition.Contains("feeding")) {
                    if (Math.Abs(Environment.TickCount) > start + CARD_FEED_TIMEOUT) {
                        printer.Cancel(jobID);
                        break;
                    }
                }
                loopCount--;
                if (!done) {
                    Thread.Sleep(1000);
                }
            }
            return done;
        }

        /// <summary>
        /// Printes test card [0]
        /// </summary>
        /// <returns></returns>
        public bool PrintTestCard() {
            bool done = false;
            try {
                string errMsg = string.Empty;
                if (IsAlarmHandling(out errMsg)) {
                    throw new Exception(errMsg);
                }
                List<string> imageNames = printer.GetTestPrintImageNames();
                List<MediaInfo> mi = printer.GetMediaInformation();
                if (mi[0].Description.Equals("K"))
                    printer.PrintTestCard(imageNames[6]);
                else
                    printer.PrintTestCard(imageNames[2]);
                done = true;
            } catch (Exception ex) {
                printerError = ex.Message;
            }
            return done;
        }

        /// <summary>
        /// Resumes a suspended job
        /// </summary>
        /// <returns></returns>
        public bool ResumeJob() {
            bool resumed = false;
            try {
                if (jobID.Equals(0)) {
                    printer.EjectCard();
                } else {
                    printer.Resume();
                }

                Thread.Sleep(1000);
                if (!PollJobForDoneOk(10)) {
                    printer.Cancel(jobID);
                    throw new Exception("Not done OK ");
                }

                resumed = true;
            } catch (Exception ex) {
                printerError = ex.Message;
            }
            return resumed;
        }

        /// <summary>
        /// Sets the card destination at the completion of a job
        /// </summary>
        /// <param name="destination"></param>
        public void SetJobDestination(string destination) {
            printer.SetJobSetting(ZebraCardJobSettingNames.CARD_DESTINATION, (destination.Equals("Hold") ? destination : "Eject"));
        }

        /// <summary>
        /// Sets the starting location where a job expects the card
        /// </summary>
        /// <param name="src"></param>
        public void SetJobSource(string src) {
            printer.SetJobSetting(ZebraCardJobSettingNames.CARD_SOURCE, (src.Equals("Internal") ? src : "Feeder"));
        }
        #endregion

        #region Smart Card Job

        /// <summary>
        /// Gets smart card offset ranges based on card type
        /// </summary>
        /// <param name="cardType">contact or contactless</param>
        /// <param name="min">minimum offset value</param>
        /// <param name="max">maximum offset value</param>
        /// <returns>true if successful</returns>
        public bool GetSmartOffsetRange(string cardType, out int min, out int max ) {
            bool gotRange = false;
            min = max = 0;

            try {
                string range = string.Empty;

                ZebraPrinterZmotif zmotifPrn = ZebraCardPrinterFactory.GetZmotifPrinter(this.connection);
                switch(cardType) {
                    case "contact":
                        range = zmotifPrn.GetSettingRange("mech_adjustments.card_smart_card_x_offset");
                        break;
                    case "hf":
                        range = zmotifPrn.GetSettingRange("mech_adjustments.card_smart_card_hf_x_offset");
                        break;
                    case "lf":
                        range = zmotifPrn.GetSettingRange("mech_adjustments.card_smart_card_lf_x_offset");
                        break;
                    case "uhf":
                        range = zmotifPrn.GetSettingRange("mech_adjustments.card_smart_card_uhf_x_offset");
                        break;
                }
                string[] strArray = range.Split('-');
                if (strArray.Length.Equals(3)) {
                    min = Convert.ToInt32(strArray[1]) * -1;
                    max = Convert.ToInt32(strArray[2]);
                    gotRange = true;
                }
            } catch {

            }
            return gotRange;
        }

        /// <summary>
        /// Indicates if a printer has a smart card encoder
        /// </summary>
        /// <returns>true if encoder is found</returns>
        public bool HasSmartCardEncoder() {
            return (printer != null && printer.HasSmartCardEncoder());
        }

        /// <summary>
        /// Starts a contactless smart card job.
        /// </summary>
        /// <returns>true if smart card is at station</returns>
        public bool ContactlessSmartCardJob(int offset) {
            bool started = false;
            try {
                Dictionary<string, string> smartCardInfo = printer.GetSmartCardConfigurations();
                if (smartCardInfo == null || smartCardInfo.Count <= 0) {
                    throw new Exception("Smart Card Info is Null");
                }

                if (!HasContactlessEncoder(smartCardInfo)) {
                    throw new Exception("No Contactless Smart Card Encoder");
                }

                string errMsg = string.Empty;
                if (!SetContactlessHFLFSmartCardOffset(offset, out errMsg)) {
                    throw new Exception(errMsg);
                }

                string encoderType = "hf";
                if (smartCardInfo.ContainsKey(encoderType)) {
                    printer.SetJobSetting(ZebraCardJobSettingNames.SMART_CARD_CONTACTLESS, encoderType);
                } else {
                    throw new Exception($"No Contactless {encoderType} Smart Card Encoder");
                }

                jobID = printer.SmartCardEncode(1);
                if (jobID.Equals(0)) {
                    throw new Exception("Job ID = 0");
                }

                Thread.Sleep(1000);
                if (!PollJobForAtStation(10)) {
                    throw new Exception("Card not at Contactless Station");
                }

                started = true;
            } catch (Exception ex) {
                printerError = ex.Message;
            }
            return started;
        }

        /// <summary>
        /// Starts a contact smart card job
        /// </summary>
        /// <returns>true if smart card is at station</returns>
        public bool ContactSmartCardJob(int offset) {
            bool started = false;
            try {
                Dictionary<string, string> smartCardInfo = printer.GetSmartCardConfigurations();
                if (smartCardInfo == null || smartCardInfo.Count <= 0) {
                    throw new Exception("Smart Card Info is Null");
                }

                if (!HasContactEncoder(smartCardInfo)) {
                    throw new Exception("No Contact Smart Card Encoder");
                }

                string errMsg = string.Empty;
                if (!SetContactSmartCardOffset(offset, out errMsg)) {
                    throw new Exception(errMsg);
                }

                printer.SetJobSetting(ZebraCardJobSettingNames.SMART_CARD_CONTACT, "yes");

                jobID = printer.SmartCardEncode(1);
                if (jobID.Equals(0)) {
                    throw new Exception("Job ID = 0");
                }

                Thread.Sleep(1000);
                if (!PollJobForAtStation(10)) {
                    throw new Exception("Contact Card Not at Station");
                }

                started = true;
            } catch (Exception ex) {
                printerError = ex.Message;
            }
            return started;
        }

        /// <summary>
        /// Move card away from encoding station and repositions at encoding station
        /// </summary>
        /// <returns>true if reposition was successful</returns>
        public bool RetrySmartCard() {
            bool started = false;
            try {
                if (jobID.Equals(0)) {
                    throw new Exception("Job ID = 0");
                }

                Thread.Sleep(1000);
                if (!PollJobForAtStation(loopCount)) {
                    throw new Exception("Card not at Contactless Station");
                }

                started = true;
            } catch (Exception ex) {
                printerError = ex.Message;
            }
            return started;
        }

        private static bool HasContactlessEncoder(Dictionary<string, string> smartCardInfo) {
            bool hasContactless = false;
            foreach (string type in smartCardInfo.Keys) {
                if (!string.IsNullOrEmpty(type) && !type.StartsWith("contact")) {
                    hasContactless = true;
                    break;
                }
            }
            return hasContactless;
        }

        private static bool HasContactEncoder(Dictionary<string, string> smartCardInfo) {
            bool hasContact = false;
            foreach (string type in smartCardInfo.Keys) {
                if (!string.IsNullOrEmpty(type) && type.StartsWith("contact")) {
                    hasContact = true;
                    break;
                }
            }
            return hasContact;
        }

        private string GetConfigurationSettings(ref string errMsg) {
            string xmlDoc = string.Empty;
            try {
                ZebraPrinterZmotif zmotifPrn = ZebraCardPrinterFactory.GetZmotifPrinter(connection);
                xmlDoc = zmotifPrn.SettingsHelper.GetConfiguration();
            } catch (Exception ex) {
                errMsg = ex.Message;
            }
            return xmlDoc;
        }

        private bool SetContactSmartCardOffset(int value, out string errMsg) {
            bool wasSet = false;
            errMsg = string.Empty;
            try {
                ZebraPrinterZmotif zmotifPrn = ZebraCardPrinterFactory.GetZmotifPrinter(connection);
                zmotifPrn.SettingsHelper.SetSmartCardOffset(value);
                wasSet = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            }
            return wasSet;
        }

        private bool SetContactlessHFLFSmartCardOffset(int value, out string errMsg) {
            bool wasSet = false;
            errMsg = string.Empty;
            try {
                ZebraPrinterZmotif zmotifPrn = ZebraCardPrinterFactory.GetZmotifPrinter(connection);
                zmotifPrn.SettingsHelper.SetContactlessSmartCardOffset("MIFARE", value);
                wasSet = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            }
            return wasSet;
        }
        
        #endregion


    }
}
