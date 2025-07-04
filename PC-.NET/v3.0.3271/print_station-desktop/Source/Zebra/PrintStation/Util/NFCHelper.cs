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
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using Windows.Networking.Proximity;
using Windows.Storage.Streams;
using Zebra.Sdk.Printer.Discovery;

namespace Zebra.PrintStation.Util {

    public class NFCHelper {

        private ProximityDevice proximityDevice;
        private long messageId = -1;
        private static MainWindow myMainWindow;
        private ComboBox selectedPrinterComboBox;

        private PrinterActionsWindow printerActionsWindow;

        public NFCHelper(MainWindow mainWindow, ComboBox printerComboBox) {
            myMainWindow = mainWindow;
            selectedPrinterComboBox = printerComboBox;
        }

        public void SubscribeForNfcMessage() {
            proximityDevice = ProximityDevice.GetDefault();
            if (proximityDevice != null) {
                if (messageId == -1) {  // Only subscribe once
                    messageId = proximityDevice.SubscribeForMessage("WindowsUri", (device, message) => {
                        using (var reader = DataReader.FromBuffer(message.Data)) {
                            reader.UnicodeEncoding = UnicodeEncoding.Utf16LE;
                            string receivedMessage = reader.ReadString(reader.UnconsumedBufferLength / 2 - 1);
                            ProcessReceivedMessage(receivedMessage);
                        }
                    });
                }
            }
        }

        public void UnsubscribeForNfcMessage() {
            if (proximityDevice != null) {
                proximityDevice.StopSubscribingForMessage(messageId);
                messageId = -1;
            }
        }

        private void ProcessReceivedMessage(string message) {
            try {
                CreateScanDialogWindow();

                Task.Run(() => {
                    DiscoverPrinters(message);
                });

                ShowScanDialogWindow();
            } catch (DiscoveryException) {
            } finally {
                CloseScanDialogWindow();
            }
        }

        private void DiscoverPrinters(string message) {
            try {
                UrlPrinterDiscoveryHandler urlDiscoHandler = new UrlPrinterDiscoveryHandler();
                UrlPrinterDiscoverer.FindPrinters(message, urlDiscoHandler);

                while (!urlDiscoHandler.IsDiscoveryFinsished) {
                    Thread.Sleep(100);
                }

                CloseScanDialogWindow();

                DiscoveredPrinter discoveredPrinter = urlDiscoHandler.PreferredPrinter;
                if (discoveredPrinter != null) {
                    UpdateSelectedPrinter(discoveredPrinter);
                } else {
                    ShowErrorDialogWindow("Discovery Error: No printers found.");
                }
            } catch (Exception e) {
                ShowErrorDialogWindow($"Connection Error: {e.Message}");
            } finally {
                CloseScanDialogWindow();
            }
        }

        private void UpdateSelectedPrinter(DiscoveredPrinter discoveredPrinter) {
            PrinterInfo discoveredPrinterInfo = new PrinterInfo {
                Address = discoveredPrinter.Address,
                FriendlyName = discoveredPrinter.DiscoveryDataMap.ContainsKey("FRIENDLY_NAME") ? discoveredPrinter.DiscoveryDataMap["FRIENDLY_NAME"] : discoveredPrinter.DiscoveryDataMap["SYSTEM_NAME"]
            };

            if (discoveredPrinterInfo.Address != null && discoveredPrinterInfo.FriendlyName != null) {
                LinkedListNode<PrinterInfo> node = myMainWindow.recentlySelectedPrinters.First;
                while (node != null) {
                    PrinterInfo printerInfo = node.Value;
                    if (printerInfo.Address.Equals(discoveredPrinterInfo.Address)) {
                        Application.Current.Dispatcher.Invoke(() => {
                            selectedPrinterComboBox.SelectedItem = printerInfo;
                        });
                        return;
                    }
                    node = node.Next;
                }

                string previousPrinterIp = Properties.Settings.Default.IpAddress;
                if (previousPrinterIp.Length > 0) {
                    myMainWindow.previousPrinters.Add(previousPrinterIp, Properties.Settings.Default.FriendlyName);
                }

                myMainWindow.printerIpAddress = discoveredPrinterInfo.Address;
                myMainWindow.printerFriendlyName = discoveredPrinterInfo.FriendlyName;
                myMainWindow.SetSelectedPrinter(myMainWindow.printerFriendlyName, myMainWindow.printerIpAddress);
            }
        }

        private void ShowErrorDialogWindow(string errorMessage) {
            Application.Current.Dispatcher.Invoke(new Action(() => {
                PrinterErrorsWindow printerErrorsWindow = new PrinterErrorsWindow(errorMessage) {
                    Owner = myMainWindow
                };
                printerErrorsWindow.ShowDialog();
            }));
        }

        private void CreateScanDialogWindow() {
            Application.Current.Dispatcher.Invoke(new Action(() => {
                printerActionsWindow = new PrinterActionsWindow("Processing NFC Scan...") {
                    Owner = myMainWindow
                };
            }));
        }

        private void ShowScanDialogWindow() {
            if (printerActionsWindow != null) {
                Application.Current.Dispatcher.Invoke(new Action(() => {
                    printerActionsWindow.ShowDialog();
                }));
            }
        }

        private void CloseScanDialogWindow() {
            if (printerActionsWindow != null && printerActionsWindow.IsVisible) {
                Application.Current.Dispatcher.Invoke(new Action(() => {
                    printerActionsWindow.Close();
                }));
            }
        }

        private class UrlPrinterDiscoveryHandler : DiscoveryHandler {

            private bool discoveryFinished = false;
            private DiscoveredPrinter preferredPrinter = null;

            public void DiscoveryError(string message) {
                discoveryFinished = true;
            }

            public void DiscoveryFinished() {
                discoveryFinished = true;
            }

            public void FoundPrinter(DiscoveredPrinter printer) {
                if (preferredPrinter == null) {
                    preferredPrinter = printer;
                } else if ((preferredPrinter is DiscoveredPrinterBluetooth) && printer is DiscoveredPrinterNetwork) {
                    preferredPrinter = printer;
                }     
            }

            public bool IsDiscoveryFinsished {
                get => discoveryFinished;
            }

            public DiscoveredPrinter PreferredPrinter {
                get => preferredPrinter;
            }
        }
    }
}
