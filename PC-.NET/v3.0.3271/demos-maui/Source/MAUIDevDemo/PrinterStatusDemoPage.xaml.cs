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

using System.Text;
using Zebra.Sdk.Comm;
using Zebra.Sdk.Printer;
using Zebra.Sdk.Printer.Discovery;
using ConnectionMgrPartial;

namespace MauiDevDemo
{

    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class PrinterStatusDemoPage : ContentPage {

        ConnectionMgr connectionMgr = null;

        private const string MacAddressSettingsKey = "MacAddress";
        private const string IpAddressSettingsKey = "IpAddress";
        private const string SymbolicNameSettingsKey = "SymbolicName";

        public enum ConnectionType {
            Network,
            Bluetooth,
            UsbDirect,
            UsbDriver
        }

        public PrinterStatusDemoPage() {
            InitializeComponent();
            if (connectionMgr == null)
            {
                connectionMgr = new ConnectionMgr();
            }
            ConnectionTypePicker.SelectedIndex = 0;

            IpAddressEntry.TextChanged += (object sender, TextChangedEventArgs e) => {
                ConnectivityDemoPageProperties.IpAddressSettingsKey = e.NewTextValue;
            };

            MacAddressEntry.TextChanged += (object sender, TextChangedEventArgs e) => {
                ConnectivityDemoPageProperties.MacAddressSettingsKey = e.NewTextValue;
            };

            SymbolicNameEntry.TextChanged += (object sender, TextChangedEventArgs e) => {
                ConnectivityDemoPageProperties.SymbolicNameSettingsKey = e.NewTextValue;
            };
        }

        private async void ConnectionTypePicker_SelectedIndexChanged(object sender, EventArgs e) {
            IpAddressEntry.IsVisible = false;
            MacAddressEntry.IsVisible = false;
            SymbolicNameEntry.IsVisible = false;
            UsbDriverPrinterPicker.IsVisible = false;
            PortNumberEntry.IsVisible = false;

            switch (GetSelectedConnectionType()) {
                case ConnectionType.Network:
                    IpAddressEntry.IsVisible = true;
                    PortNumberEntry.IsVisible = true;
                    if (!string.IsNullOrEmpty(ConnectivityDemoPageProperties.IpAddressSettingsKey)) {
                        IpAddressEntry.Text = ConnectivityDemoPageProperties.IpAddressSettingsKey;
                    } else {
                        IpAddressEntry.Text = null;
                    }
                    break;

                case ConnectionType.Bluetooth:
                    MacAddressEntry.IsVisible = true;
                    if (!string.IsNullOrEmpty(ConnectivityDemoPageProperties.MacAddressSettingsKey)) {
                        MacAddressEntry.Text = ConnectivityDemoPageProperties.MacAddressSettingsKey;
                    } else {
                        MacAddressEntry.Text = null;
                    }
                    break;

                case ConnectionType.UsbDirect:
                    SymbolicNameEntry.IsVisible = true;
                    if (!string.IsNullOrEmpty(ConnectivityDemoPageProperties.SymbolicNameSettingsKey)) { 
                        SymbolicNameEntry.Text = ConnectivityDemoPageProperties.SymbolicNameSettingsKey;
                    } else {
                        SymbolicNameEntry.Text = null;
                    }
                    break;

                case ConnectionType.UsbDriver:
                    UsbDriverPrinterPicker.IsVisible = true;

                    try {
                        UsbDriverPrinterPicker.ItemsSource = connectionMgr.GetZebraUsbDriverPrinters();
                    } catch (NotImplementedException) {
                        ConnectionTypePicker.SelectedIndex = 0;
                        await DisplayAlert("Error", "USB driver not supported on this platform", "OK");
                    }
                    break;
            }
        }

        private async void CheckPrinterStatusButton_Clicked(object sender, EventArgs eventArgs) {
            SetInputEnabled(false);
            PrinterStatusLabel.Text = "Checking printer status...";

            Connection connection = null;

            try {
                await Task.Factory.StartNew(() => {
                    connection = CreateConnection();
                    connection.Open();

                    ZebraPrinter printer = ZebraPrinterFactory.GetInstance(connection);
                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.CreateLinkOsPrinter(printer);

                    PrinterStatus printerStatus = linkOsPrinter?.GetCurrentStatus() ?? printer.GetCurrentStatus();

                    Device.BeginInvokeOnMainThread(() => {
                        PrinterStatusLabel.Text = BuildPrinterStatusString(printerStatus);
                    });
                });
            } catch (Exception e) {
                PrinterStatusLabel.Text = $"Error: {e.Message}";
                await DisplayAlert("Error", e.Message, "OK");
            } finally {
                try {
                    connection?.Close();
                } catch (ConnectionException) { }

                SetInputEnabled(true);
            }
        }

        private ConnectionType? GetSelectedConnectionType() {
            string connectionType = (string)ConnectionTypePicker.SelectedItem;
            switch (connectionType) {
                case "Network":
                    return ConnectionType.Network;

                case "Bluetooth":
                    return ConnectionType.Bluetooth;

                case "USB Direct":
                    return ConnectionType.UsbDirect;

                case "USB Driver":
                    return ConnectionType.UsbDriver;

                default:
                    return null;
            }
        }

        private int GetPortNumber(string portNumberString) {
            if (!string.IsNullOrWhiteSpace(portNumberString)) {
                try {
                    return int.Parse(portNumberString);
                } catch (Exception) {
                    throw new ArgumentException("Port number must be an integer");
                }
            } else {
                return 9100;
            }
        }

        private Connection CreateConnection() {
            switch (GetSelectedConnectionType()) {
                case ConnectionType.Network:
                    return new TcpConnection(IpAddressEntry.Text, GetPortNumber(PortNumberEntry.Text));

                case ConnectionType.Bluetooth:
                    try {
                        return connectionMgr.GetBluetoothConnection(MacAddressEntry.Text);
                    } catch (NotImplementedException) {
                        throw new NotImplementedException("Bluetooth connection not supported on this platform");
                    }

                case ConnectionType.UsbDirect:
                    try {
                        return connectionMgr.GetUsbConnection(SymbolicNameEntry.Text);
                    } catch (NotImplementedException) {
                        throw new NotImplementedException("USB connection not supported on this platform");
                    }

                case ConnectionType.UsbDriver:
                    return ((DiscoveredPrinter)UsbDriverPrinterPicker.SelectedItem)?.GetConnection();

                default:
                    throw new ArgumentNullException("No connection type selected");
            }
        }

        private string BuildPrinterStatusString(PrinterStatus printerStatus) {
            string readiness = printerStatus != null && printerStatus.isReadyToPrint ? "Printer ready" : "Printer not ready";
            string labelsInBatch = $"Labels in batch: {Convert.ToString(printerStatus.labelsRemainingInBatch)}";
            string labelsInBuffer = $"Labels in buffer: {Convert.ToString(printerStatus.numberOfFormatsInReceiveBuffer)}";
            string statusMessages = String.Join(", ", new PrinterStatusMessages(printerStatus).GetStatusMessage());

            StringBuilder sb = new StringBuilder();
            sb.AppendLine(readiness);
            sb.AppendLine(labelsInBatch);
            sb.AppendLine(labelsInBuffer);
            sb.AppendLine(statusMessages);
            return sb.ToString();
        }

        [Obsolete]
        private void SetInputEnabled(bool enabled) {
            Device.BeginInvokeOnMainThread(() => {
                ConnectionTypePicker.IsEnabled = enabled;
                IpAddressEntry.IsEnabled = enabled;
                MacAddressEntry.IsEnabled = enabled;
                SymbolicNameEntry.IsEnabled = enabled;
                UsbDriverPrinterPicker.IsEnabled = enabled;
                PortNumberEntry.IsEnabled = enabled;
                CheckPrinterStatusButton.IsEnabled = enabled;
            });
        }
    }
}