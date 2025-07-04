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
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Media;
using System.Windows.Media.Animation;
using Zebra.PrintStationCard.Util;
using Zebra.Sdk.Card.Printer.Discovery;
using Zebra.Sdk.Printer.Discovery;

namespace Zebra.PrintStationCard.ChoosePrinter {

    /// <summary>
    /// Interaction logic for PrinterDiscoveryWindow.xaml
    /// </summary>
    public partial class PrinterDiscoveryWindow : Window {

        private const string NETWORK_RESOURCE_IMAGE = "/Resources/network.png";
        private const string USB_RESOURCE_IMAGE = "/Resources/usb.png";
        
        private PrinterInfo selectedPrinterInfo = new PrinterInfo();
        
        public PrinterInfo SelectedPrinterInfo {
            get => selectedPrinterInfo;
        }

        public PrinterDiscoveryWindow() {
            InitializeComponent();

            DoubleAnimation da = new DoubleAnimation() {
                From = 0,
                To = 360,
                Duration = new Duration(TimeSpan.FromSeconds(1.5)),
                RepeatBehavior = RepeatBehavior.Forever
            };

            RotateTransform rt = new RotateTransform();
            animatedDiscoveryArrow_image.RenderTransform = rt;
            animatedDiscoveryArrow_image.RenderTransformOrigin = new Point(0.5, 0.5);
            rt.BeginAnimation(RotateTransform.AngleProperty, da);

            Application.Current.Dispatcher.Invoke(() => {
                PrinterDiscoverListView.Items.Add(new { Model = "Manually Add Printer", IpAddress = "" });
            });

            List<DiscoveredUsbPrinter> connectedUsbPrinters = UsbDiscoverer.GetZebraUsbPrinters(new ZebraCardPrinterFilter());
            foreach (DiscoveredUsbPrinter usbPrinter in connectedUsbPrinters) {
                string imageSource = USB_RESOURCE_IMAGE;
                Application.Current.Dispatcher.Invoke(() => {
                    PrinterDiscoverListView.Items.Add(new { ConnectionImageSource = imageSource, Model = $"USB Printer ({usbPrinter.DiscoveryDataMap["SERIAL_NUMBER"]})", IpAddress = usbPrinter.Address });
                });
            }

            DiscoveryHandlerImpl networkDiscoHandler = new DiscoveryHandlerImpl(this);
            NetworkCardDiscoverer.FindPrinters(networkDiscoHandler);

            Task.Run(() => {
                while (!networkDiscoHandler.DiscoveryComplete) {
                    ThreadSleeper.Sleep(50);
                }

                Application.Current.Dispatcher.Invoke(() => {
                    animatedDiscoveryArrow_image.Visibility = Visibility.Hidden;
                });
            });
        }

        private void Window_Loaded(object sender, RoutedEventArgs e) {
            MinWidth = ActualWidth;
            MinHeight = ActualHeight;
        }

        private void PrinterDiscoverListView_MouseDoubleClick(object sender, System.Windows.Input.MouseButtonEventArgs e) {
            dynamic selectedItem = PrinterDiscoverListView.SelectedItem;
            if (selectedItem.Model.ToLower().Equals("manually add printer")) {
                ManualConnectWindow manualConnectWindow = new ManualConnectWindow() {
                    Owner = this
                };
                manualConnectWindow.ShowDialog();

                PrinterInfo manuallyAddedPrinter = manualConnectWindow.PrinterInfo;
                if (manuallyAddedPrinter != null) {
                    string imageSource = "/Resources/network.png";
                    PrinterDiscoverListView.Items.Add(new { ConnectionImageSource = imageSource, Model = manuallyAddedPrinter.Model, IpAddress = manuallyAddedPrinter.Address });
                }
            }
        }

        public static bool IsBluetoothSupported() {
            return Environment.OSVersion.Version.Major >= 10;
        }

        private void SelectPrinter_Click(object sender, RoutedEventArgs e) {
            if (PrinterDiscoverListView.SelectedItem != null) {
                dynamic selectedItem = PrinterDiscoverListView.SelectedItem;
                if (!selectedItem.Model.ToLower().Equals("manually add printer")) {
                    selectedPrinterInfo.Address = selectedItem.IpAddress;
                    selectedPrinterInfo.Model = selectedItem.Model;
                    Close();
                } else {
                    MessageBoxHelper.ShowError("No printer selected.\nPlease select a printer from the list.");
                }
            }
        }

        private class DiscoveryHandlerImpl : DiscoveryHandler {

            private bool discoveryComplete = false;
            private PrinterDiscoveryWindow printerDiscoveryWindow;

            public DiscoveryHandlerImpl(PrinterDiscoveryWindow pdw) {
                printerDiscoveryWindow = pdw;
            }

            public bool DiscoveryComplete {
                get => discoveryComplete;
            }

            public void DiscoveryError(string message) {
                discoveryComplete = true;
            }

            public void DiscoveryFinished() {
                discoveryComplete = true;
            }

            public void FoundPrinter(DiscoveredPrinter printer) {
                string imageSource = NETWORK_RESOURCE_IMAGE;
                Application.Current.Dispatcher.Invoke(() => {
                    try {
                        printerDiscoveryWindow.PrinterDiscoverListView.Items.Add(new { ConnectionImageSource = imageSource, Model = printer.DiscoveryDataMap["MODEL"], IpAddress = printer.Address });
                    } catch (Exception e) {
                        string test = e.Message;
                    }
                });
            }
        }
    }
}
