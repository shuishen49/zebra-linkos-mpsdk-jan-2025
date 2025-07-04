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

using Zebra.PrintStation.Util;
using System;
using System.Collections.Generic;
using System.Windows;
using Zebra.Sdk.Printer.Discovery;
using System.Windows.Media.Animation;
using System.Windows.Media;
using System.Threading.Tasks;
using System.Threading;

namespace Zebra.PrintStation.ChoosePrinter {

    public partial class PrinterDiscoveryWindow : Window {

        private PrinterInfo selectedPrinterInfo;
        private RotateTransform rt;

        public PrinterInfo SelectedPrinterInfo {
            get { return selectedPrinterInfo; }
        }

        public PrinterDiscoveryWindow() {
            InitializeComponent();

            selectedPrinterInfo = new PrinterInfo();

            DoubleAnimation da = new DoubleAnimation() {
                From = 0,
                To = 360,
                Duration = new Duration(TimeSpan.FromSeconds(1.5)),
                RepeatBehavior = RepeatBehavior.Forever
            };

            rt = new RotateTransform();
            animatedDiscoveryArrow_image.RenderTransform = rt;
            animatedDiscoveryArrow_image.RenderTransformOrigin = new Point(0.5, 0.5);
            rt.BeginAnimation(RotateTransform.AngleProperty, da);

            List<DiscoveredUsbPrinter> connectedUsbPrinters = UsbDiscoverer.GetZebraUsbPrinters(new ZebraPrinterFilter());
            foreach (DiscoveredUsbPrinter usbPrinter in connectedUsbPrinters) {
                string imageSource = "/Resources/usb.png";
                Application.Current.Dispatcher.Invoke(() => {
                    PrinterDiscoverListView.Items.Add(new { ConnectionImageSource = imageSource, FriendlyName = "USB Printer (" + usbPrinter.DiscoveryDataMap["SERIAL_NUMBER"] + ")", IpAddress = usbPrinter.Address });
                });
            }

            DiscoveryHandlerImpl networkDiscoHandler = new DiscoveryHandlerImpl(this);
            NetworkDiscoverer.FindPrinters(networkDiscoHandler);

            DiscoveryHandlerImpl bluetoothDiscoHandler = null;

            if (IsBluetoothSupported()) {
                bluetoothDiscoHandler = new DiscoveryHandlerImpl(this);
                BluetoothDiscoverer.FindPrinters(bluetoothDiscoHandler);
            }

            if(bluetoothDiscoHandler != null) {
                Task.Run(() => {
                    while(!bluetoothDiscoHandler.DiscoveryComplete || !networkDiscoHandler.DiscoveryComplete) {
                        Thread.Sleep(50);
                    }
                    Application.Current.Dispatcher.Invoke(() => {
                        animatedDiscoveryArrow_image.Visibility = Visibility.Hidden;
                    });
                });
            } else {
                Task.Run(() => {
                    while(!networkDiscoHandler.DiscoveryComplete) {
                        Thread.Sleep(50);
                    }
                    Application.Current.Dispatcher.Invoke(() => {
                        animatedDiscoveryArrow_image.Visibility = Visibility.Hidden;
                    });
                });
            }

        }

        private void Window_Loaded(object sender, RoutedEventArgs e) {
            MinWidth = ActualWidth;
            MinHeight = ActualHeight;
        }

        public static bool IsBluetoothSupported() {
            var osVersion = Environment.OSVersion;
            if (osVersion.Version.Major >= 10) {
                return true;
            }
            return false;
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
                if (printer is DiscoveredPrinterNetwork) {
                    FoundNetworkPrinter(printer);
                } else if (printer is DiscoveredPrinterBluetooth) {
                    FoundBluetoothPrinter(printer);
                }
            }

            private void FoundBluetoothPrinter(DiscoveredPrinter printer) {
                string imageSource = "/Resources/bt.png";
                Application.Current.Dispatcher.Invoke(() => {
                    printerDiscoveryWindow.PrinterDiscoverListView.Items.Add(new { ConnectionImageSource = imageSource, FriendlyName = printer.DiscoveryDataMap["FRIENDLY_NAME"], IpAddress = printer.Address });
                });
            }

            private void FoundNetworkPrinter(DiscoveredPrinter printer) {
                string imageSource = "/Resources/network.png";
                Application.Current.Dispatcher.Invoke(() => {
                    printerDiscoveryWindow.PrinterDiscoverListView.Items.Add(new { ConnectionImageSource = imageSource, FriendlyName = printer.DiscoveryDataMap["SYSTEM_NAME"], IpAddress = printer.Address });
                });
            }
        }

        private void SelectPrinter_Click(object sender, RoutedEventArgs e) {
            if (PrinterDiscoverListView.SelectedItem != null) {
                dynamic selectedItem = PrinterDiscoverListView.SelectedItem;
                selectedPrinterInfo.Address = selectedItem.IpAddress;
                selectedPrinterInfo.FriendlyName = selectedItem.FriendlyName;
                Close();
            }
        }
    }
}
