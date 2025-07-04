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
using System.Diagnostics;
using System.Windows;
using System.Windows.Documents;
using Zebra.PrintStation.ChoosePrinter;

namespace Zebra.PrintStation.Util {

    /// <summary>
    /// Interaction logic for BluetoothHelpWindow.xaml
    /// </summary>
    public partial class BluetoothHelpWindow : Window {

        private string supportedWindowsVersionsText = "To pair with a Zebra printer via Bluetooth, install Windows 10 on your device.";

        private string bluetoothSecurityTextBeginning = "When pairing with a Zebra printer, the Bluetooth setting \"bluetooth.minimum_security_mode\"" +
            " changes the steps required to successfully connect to your printer.\n\t• If the security mode is set to 1 or 2," +
            " connect to your printer using Print Station.\n\t• If your security mode is 3 or 4, manually pair your" +
            " printer using ";

        private string bluetoothSecurityTextEnding = " on your device before attempting to communicate with it on Print Station.";

        public BluetoothHelpWindow() {
            InitializeComponent();
            SupportedWindowsVersionsTextBlock.Text = supportedWindowsVersionsText;

            Run btSecurityTextBeginningRun = new Run() {
                Text = bluetoothSecurityTextBeginning
            };

            Run bluetoothSettingsRun = new Run() {
                Text = "Settings > Bluetooth"
            };

            Hyperlink bluetoothSettingsHyperlink = new Hyperlink(bluetoothSettingsRun) {
                NavigateUri = new Uri("ms-settings:bluetooth")
            };
            bluetoothSettingsHyperlink.RequestNavigate += BluetoothHyperlink_RequestNavigate;

            Run btSecurityTextEndRun = new Run() {
                Text = bluetoothSecurityTextEnding
            };

            BluetoothHelpTextBlock.Inlines.Add(btSecurityTextBeginningRun);
            if (PrinterDiscoveryWindow.IsBluetoothSupported()) {
                BluetoothHelpTextBlock.Inlines.Add(bluetoothSettingsHyperlink);
            } else {
                BluetoothHelpTextBlock.Inlines.Add(bluetoothSettingsRun);
            }
            BluetoothHelpTextBlock.Inlines.Add(btSecurityTextEndRun);
        }

        private void BluetoothHyperlink_RequestNavigate(object sender, System.Windows.Navigation.RequestNavigateEventArgs e) {
            //System.Diagnostics.Process.Start("ms-settings:bluetooth");
            try
            {
                Process.Start("control.exe", "bthprops.cpl");
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error: " + ex.Message);
            }
        }
    }
}
