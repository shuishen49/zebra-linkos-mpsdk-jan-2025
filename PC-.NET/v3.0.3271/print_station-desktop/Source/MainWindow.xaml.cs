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

using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Threading;
using Zebra.PrintStation.ChoosePrinter;
using Zebra.PrintStation.Util;
using Zebra.Sdk.Comm;
using Zebra.Sdk.Printer;

namespace Zebra.PrintStation {

    public partial class MainWindow : Window {

        private const string FORMAT_SOURCE_KEY = "FormatSource";
        private const string FORMAT_SOURCE_PRINTER = "printer";
        private const string FORMAT_SOURCE_DATABASE = "database";

        public LinkedList<PrinterInfo> recentlySelectedPrinters;
        public Dictionary<string, string> previousPrinters;
        private PrinterActionsWindow printerActionsWindow;
        private PrinterDiscoveryWindow printerDiscoveryWindow;
        private Connection printerConnection;
        private BackgroundWorker backgroundWorker;
        private LinkedListNode<PrinterInfo> selectAPrinterNode;
        private List<string> attributeKeys;
        private List<FieldDescriptionData> fieldDescDataVars;
        private List<Dictionary<string, string>> formatInfo;
        private ObservableCollection<FormatVariable> formatVariableCollection;

        public string printerIpAddress;
        public string printerFriendlyName;
        private string errorMessage;
        private string lastFormatOpened = "";
        private string lastFormatOpenedSource = "";
        private string lastFormatOpenedContents = "";

        private NFCHelper nfcHelper;
        private static Regex ipAddressRegex = new Regex(@"\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b");
        private static Regex btAddressRegex = new Regex(@"([a-fA-F0-9]{2}):([a-fA-F0-9]{2}):([a-fA-F0-9]{2}):([a-fA-F0-9]{2}):([a-fA-F0-9]{2}):([a-fA-F0-9]{2})");

        #region UIActions

        public MainWindow() {
            Closed += OnClosing;
            InitializeComponent();

            if (PrinterDiscoveryWindow.IsBluetoothSupported()) {
                nfcHelper = new NFCHelper(this, SelectedPrinterComboBox);
                nfcHelper.SubscribeForNfcMessage();
            }

            formatInfo = new List<Dictionary<string, string>>();
            attributeKeys = new List<string> {
                "drive",
                "name",
                "extension",
                "star"
            };

            recentlySelectedPrinters = new LinkedList<PrinterInfo>();
            selectAPrinterNode = new LinkedListNode<PrinterInfo>(new PrinterInfo("Select a Printer", ""));
            recentlySelectedPrinters.AddFirst(selectAPrinterNode);

            if (!string.IsNullOrEmpty(Properties.Settings.Default.PreviousPrinters)) {
                previousPrinters = JsonConvert.DeserializeObject<Dictionary<string, string>>(Properties.Settings.Default.PreviousPrinters);
                foreach (string address in previousPrinters.Keys) {
                    if (address.Length > 0) {
                        recentlySelectedPrinters.AddAfter(selectAPrinterNode, new PrinterInfo(previousPrinters[address], address));
                    }
                }
            }

            SelectedPrinterComboBox.ItemsSource = recentlySelectedPrinters;
            if (previousPrinters == null) {
                previousPrinters = new Dictionary<string, string>();
            }
        }

        private void Window_Loaded(object sender, RoutedEventArgs e) {
            string savedPrinterIpAddress = Properties.Settings.Default.IpAddress;
            if (!string.IsNullOrEmpty(savedPrinterIpAddress)) {
                string savedPrinterFriendlyName = Properties.Settings.Default.FriendlyName;
                SetSelectedPrinter(savedPrinterFriendlyName, savedPrinterIpAddress);
            } else {
                ShowPrinterDiscovery();
            }
        }

        public void OnClosing(object sender, EventArgs e) {
            Properties.Settings.Default.PreviousPrinters = JsonConvert.SerializeObject(previousPrinters);
            Properties.Settings.Default.Save();

            if (nfcHelper != null) {
                nfcHelper.UnsubscribeForNfcMessage();
            }
        }

        public void ShowPrinterDiscovery() {
            if (PrinterFormatsListView.Items.Count > 0) {
                PrinterFormatsListView.Items.Clear();
            }

            printerDiscoveryWindow = new PrinterDiscoveryWindow() {
                Topmost = true
            };

            IsEnabled = false;
            printerDiscoveryWindow.Closed += PrinterDiscoveryWindow_Closed;

            if (printerDiscoveryWindow.ShowDialog() == false) {
                if (printerDiscoveryWindow != null) {
                    PrinterInfo selectedPrinter = printerDiscoveryWindow.SelectedPrinterInfo;
                    if (selectedPrinter.Address != null && selectedPrinter.FriendlyName != null) {
                        LinkedListNode<PrinterInfo> node = recentlySelectedPrinters.First;
                        while (node != null) {
                            PrinterInfo printerInfo = node.Value;
                            if (printerInfo.Address.Equals(selectedPrinter.Address)) {
                                if (!printerInfo.FriendlyName.Equals(selectedPrinter.FriendlyName)) {
                                    printerInfo.FriendlyName = selectedPrinter.FriendlyName;
                                    node.Value = printerInfo;
                                }
                                SelectedPrinterComboBox.SelectedItem = printerInfo;
                                return;
                            }
                            node = node.Next;
                        }

                        string previousPrinterIp = Properties.Settings.Default.IpAddress;
                        if (previousPrinterIp.Length > 0) {
                            previousPrinters.Add(previousPrinterIp, Properties.Settings.Default.FriendlyName);
                        }

                        printerFriendlyName = selectedPrinter.FriendlyName;
                        printerIpAddress = selectedPrinter.Address;
                        SetSelectedPrinter(printerFriendlyName, printerIpAddress);
                    }
                    printerDiscoveryWindow = null;
                }
            }
        }

        private void PrinterErrorWindow_Closed(object sender, EventArgs e) {
            IsEnabled = true;
        }

        private void PrinterDiscoveryWindow_Closed(object sender, EventArgs e) {
            IsEnabled = true;
        }

        private void SelectedPrinterClosedEvent(object sender, EventArgs e) {
            int selectedIndex = SelectedPrinterComboBox.SelectedIndex;
            if (selectedIndex == 0 && printerDiscoveryWindow == null) {
                ShowPrinterDiscovery();
            }
        }

        private void SelectedPrinterChangeEvent(object sender, SelectionChangedEventArgs e) {
            if (SelectedPrinterComboBox.SelectedIndex == 0) {
                ShowPrinterDiscovery();
            } else {
                if (printerDiscoveryWindow != null) {
                    printerDiscoveryWindow.Close();
                    printerDiscoveryWindow = null;
                }

                if (PrinterFormatsListView.Visibility == Visibility.Hidden) {

                    PrinterFormatsListView.Visibility = Visibility.Visible;
                    FormatGrid.Visibility = Visibility.Hidden;
                }

                dynamic selectedItem = SelectedPrinterComboBox.SelectedItem;
                if (PrinterFormatsListView.Items.Count > 0) {
                    PrinterFormatsListView.Items.Clear();
                }

                if (!previousPrinters.ContainsKey(selectedItem.Address) && previousPrinters.Count >= 5) {
                    string removeKey = recentlySelectedPrinters.Last.Value.Address;
                    previousPrinters.Remove(removeKey);
                    recentlySelectedPrinters.RemoveLast();
                }

                if (previousPrinters.ContainsKey(Properties.Settings.Default.IpAddress)) {
                    previousPrinters.Remove(Properties.Settings.Default.IpAddress);
                }

                previousPrinters.Add(Properties.Settings.Default.IpAddress, Properties.Settings.Default.FriendlyName);
                Properties.Settings.Default.FriendlyName = selectedItem.FriendlyName;
                Properties.Settings.Default.IpAddress = selectedItem.Address;

                if (previousPrinters.ContainsKey(selectedItem.Address)) {
                    previousPrinters.Remove(selectedItem.Address);
                }

                SetSelectedConnection(selectedItem);
            }
        }

        private void PrinterActionsCompleted(object sender, RunWorkerCompletedEventArgs e) {
            Dispatcher.Invoke(new Action(() => {
                printerActionsWindow.Close();
            }));

            if (errorMessage != null) {
                ShowErrorDialogWindow(errorMessage);
                errorMessage = null;
            }
        }

        private void OptionsButton_Click(object sender, RoutedEventArgs e) {
            ContextMenu optionsContextMenu = (sender as Button).ContextMenu;
            optionsContextMenu.IsEnabled = true;
            optionsContextMenu.PlacementTarget = (sender as Button);
            optionsContextMenu.Placement = PlacementMode.Bottom;
            optionsContextMenu.IsOpen = true;
        }

        private void RefreshFormats_Click(object sender, RoutedEventArgs e) {
            RefreshFormats();
        }

        private void ShowAboutWindow_Click(object sender, RoutedEventArgs e) {
            AboutWindow aboutWindow = new AboutWindow() {
                Owner = this
            };

            aboutWindow.Show();
        }

        private void ShowBluetoothHelp_Click(object sender, RoutedEventArgs e) {
            BluetoothHelpWindow bluetoothHelpWindow = new BluetoothHelpWindow() {
                Owner = this
            };

            bluetoothHelpWindow.Show();
        }

        private void Opening_ContextMenu(object sender, MouseButtonEventArgs e) {
            dynamic item = PrinterFormatsListView.SelectedItem;
            string starImageResource = item.StarImage;
            if (starImageResource.Equals("/Resources/btn_star_big_on.png")) {
                Delete_MenuItem.Visibility = Visibility.Visible;
                Save_MenuItem.Visibility = Visibility.Collapsed;
            } else {
                Save_MenuItem.Visibility = Visibility.Visible;
                Delete_MenuItem.Visibility = Visibility.Collapsed;
            }

            PrinterFormatsListView.ContextMenu.IsOpen = true;
        }

        private void OnDeleteFormat_Click(object sender, RoutedEventArgs e) {
            dynamic item = PrinterFormatsListView.SelectedItem;
            long formatId = long.Parse(item.FormatId);

            using (SavedFormatProvider provider = new SavedFormatProvider()) {
                provider.Delete(formatId);
            }

            RefreshFormats();
        }

        private void OnSaveFormat_Click(object sender, RoutedEventArgs e) {
            try {
                printerActionsWindow = new PrinterActionsWindow("Saving format...") {
                    Owner = this
                };

                dynamic selectedItem = PrinterFormatsListView.SelectedItem;
                string formatDrive = selectedItem.FormatDrive;
                string formatName = selectedItem.FormatName;
                string extension = selectedItem.FormatExtension;

                Task.Run(() => {
                    try {
                        string format = GetFormatFromPrinter(formatDrive + formatName + extension);
                        format = AddFormatLocationToFormat(format, formatDrive + formatName + extension);

                        using (SavedFormatProvider provider = new SavedFormatProvider()) {
                            provider.Insert(formatName, extension, format);
                        }
                    } finally {
                        Dispatcher.Invoke(new Action(() => {
                            printerActionsWindow.Close();
                        }));
                    }
                });

                printerActionsWindow.ShowDialog();
                RefreshFormats();
            } catch (Exception ex) {
                if (printerActionsWindow != null) {
                    printerActionsWindow.Close();
                }
                ShowErrorDialogWindow($"Save format error: {ex.Message}");
            }
        }

        private void OnPrintFormat_Click(object sender, RoutedEventArgs e) {
            Dispatcher.Invoke(new Action(() => { }), DispatcherPriority.ContextIdle, null);
            SelectedPrinterComboBox.Items.Refresh();
            formatInfo.Clear();

            printerActionsWindow = new PrinterActionsWindow("Printing format...") {
                Owner = this
            };

            backgroundWorker = new BackgroundWorker();
            backgroundWorker.DoWork += new DoWorkEventHandler(PrintFormat);
            backgroundWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(PrinterActionsCompleted);
            backgroundWorker.RunWorkerAsync();

            printerActionsWindow.ShowDialog();
        }

        private void OnCancel_Click(object sender, RoutedEventArgs e) {
            PrinterFormatsListView.Visibility = Visibility.Visible;
            FormatGrid.Visibility = Visibility.Hidden;
        }

        private void PrinterFormatsListView_MouseDoubleClick(object sender, MouseButtonEventArgs e) {
            if (FormatFieldsListView.Items.Count > 0) {
                FormatFieldsListView.ItemsSource = null;
            }

            printerActionsWindow = new PrinterActionsWindow("Retrieving Variables...") {
                Owner = this
            };

            backgroundWorker = new BackgroundWorker();
            backgroundWorker.DoWork += new DoWorkEventHandler(GetFormatVariables);
            backgroundWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(FormatVariablesRetrieved);

            object[] bwParameters = new object[4];
            bwParameters[0] = PrinterFormatsListView.SelectedItem;
            bwParameters[1] = lastFormatOpened;
            bwParameters[2] = lastFormatOpenedContents;
            bwParameters[3] = lastFormatOpenedSource;
            backgroundWorker.RunWorkerAsync(bwParameters);

            printerActionsWindow.ShowDialog();
        }

        private void FormatVariablesRetrieved(object sender, RunWorkerCompletedEventArgs e) {
            printerActionsWindow.Close();
            if (errorMessage != null) {
                PrinterFormatsListView.Visibility = Visibility.Visible;
                FormatGrid.Visibility = Visibility.Hidden;
                ShowErrorDialogWindow(errorMessage);
                errorMessage = null;
            } else {
                PrinterFormatsListView.Visibility = Visibility.Hidden;
                FormatGrid.Visibility = Visibility.Visible;
                object[] results = e.Result as object[];
                lastFormatOpened = (string)results[0];
                lastFormatOpenedSource = (string)results[1];
                lastFormatOpenedContents = (string)results[2];
                formatVariableCollection = (ObservableCollection<FormatVariable>)results[3];
                AddFieldsToFormat(formatVariableCollection);
            }
        }

        private void GetFormatVariables(object sender, DoWorkEventArgs e) {
            object[] parameters = e.Argument as object[];
            dynamic selectedFormat = parameters[0] as dynamic;
            string lastFormatOpened = (string)parameters[1];
            string lastFormatOpenedSource = (string)parameters[2];
            string lastFormatOpenedContents = (string)parameters[3];
            object[] results = new object[4];

            long formatId = long.Parse(selectedFormat.FormatId);
            lastFormatOpened = selectedFormat.FormatDrive + selectedFormat.FormatName + selectedFormat.FormatExtension;
            lastFormatOpenedSource = selectedFormat.FormatSource;

            try {
                OpenConnection();
                ZebraPrinterLinkOs printer = ZebraPrinterFactory.GetLinkOsPrinter(printerConnection);

                if (lastFormatOpenedSource.Equals("Sample") && formatId > 0) {
                    using (SavedFormatProvider provider = new SavedFormatProvider()) {
                        lastFormatOpenedContents = provider.GetFormatContents(formatId);
                    }
                } else {
                    byte[] formatInBytes = printer.RetrieveFormatFromPrinter(lastFormatOpened);
                    lastFormatOpenedContents = Encoding.UTF8.GetString(formatInBytes);
                }

                fieldDescDataVars = printer.GetVariableFields(lastFormatOpenedContents).ToList();
                fieldDescDataVars = FormatFieldDescriptionDataVars(fieldDescDataVars);

                formatVariableCollection = new ObservableCollection<FormatVariable>();
                for (int i = 0; i < fieldDescDataVars.Count; ++i) {
                    formatVariableCollection.Add(new FormatVariable { FieldName = fieldDescDataVars[i].FieldName, FieldValue = "" });
                }

                results[0] = lastFormatOpened;
                results[1] = lastFormatOpenedSource;
                results[2] = lastFormatOpenedContents;
                results[3] = formatVariableCollection;
                e.Result = results;
            } catch (ConnectionException error) {
                errorMessage = "Connection Error: " + error.Message;
            } finally {
                CloseConnection();
            }
        }

        private ListView GetPrinterFormatListView() {
            return PrinterFormatsListView;
        }

        private void ShowErrorDialogWindow(string errorMessage) {
            Dispatcher.Invoke(new Action(() => {
                PrinterErrorsWindow printerErrorsWindow = new PrinterErrorsWindow(errorMessage) {
                    Owner = this
                };
                printerErrorsWindow.ShowDialog();
            }));
        }

        #endregion UIActions

        #region PrinterActions

        private void RefreshFormats() {
            if (PrinterFormatsListView.Items.Count > 0) {
                PrinterFormatsListView.Items.Clear();
            }

            Task.Run(() => {
                GetFormatsFromPrinter();
            });
        }

        public void SetSelectedConnection(dynamic selectedItem) {
            if (ipAddressRegex.IsMatch(selectedItem.Address)) {
                printerConnection = new MultichannelTcpConnection(selectedItem.Address, 9100, 9200);
            } else if (btAddressRegex.IsMatch(selectedItem.Address)) {
                printerConnection = new MultichannelBluetoothConnection(selectedItem.Address);
            } else {
                printerConnection = new UsbConnection(selectedItem.Address);
            }

            if (SelectedPrinterComboBox != null) {
                int index = SelectedPrinterComboBox.Items.IndexOf(selectedItem);
                SelectedPrinterComboBox.SelectedIndex = index;
                SelectedPrinterComboBox.Items.Refresh();
            }

            Task.Run(() => {
                GetFormatsFromPrinter();
            });
        }

        public void SetSelectedPrinter(string friendlyName, string address) {
            if (SelectedPrinterComboBox != null) {
                PrinterInfo selectedPrinter = new PrinterInfo(friendlyName, address);
                recentlySelectedPrinters.AddAfter(selectAPrinterNode, selectedPrinter);
                Application.Current.Dispatcher.Invoke(() => {
                    SelectedPrinterComboBox.SelectedItem = selectedPrinter;
                });
            }
        }

        private void GetFormatsFromPrinter() {
            formatInfo.Clear();
            Dispatcher.Invoke(new Action(() => {
                printerActionsWindow = new PrinterActionsWindow("Retrieving formats...") {
                    Owner = this
                };
            }));

            backgroundWorker = new BackgroundWorker();
            backgroundWorker.DoWork += new DoWorkEventHandler(RetrieveFormatsFromPrinter);
            backgroundWorker.RunWorkerCompleted += new RunWorkerCompletedEventHandler(PrinterActionsCompleted);
            backgroundWorker.RunWorkerAsync();

            Dispatcher.Invoke(new Action(() => {
                printerActionsWindow.ShowDialog();
            }));
        }

        private void RetrieveFormatsFromPrinter(object sender, DoWorkEventArgs e) {
            List<SavedFormat> savedFormats = GetSavedFormats();

            foreach (SavedFormat format in savedFormats) {
                Dictionary<string, string> formatAttributes = new Dictionary<string, string> {
                    { attributeKeys[0], format.formatDrive },
                    { attributeKeys[1], format.formatName },
                    { attributeKeys[2], format.formatExtension },
                    { attributeKeys[3], "/Resources/btn_star_big_on.png" },
                    { FORMAT_SOURCE_KEY, format.sourcePrinterName },
                    { SavedFormat._ID, format.id.ToString() }
                };

                AddFormatToList(formatAttributes);
            }

            try {
                OpenConnection();
                ZebraPrinterLinkOs printer = ZebraPrinterFactory.GetLinkOsPrinter(printerConnection);
                string[] printerFormats = printer.RetrieveFileNames(new string[] { "ZPL" });
                foreach (string format in printerFormats) {
                    int colonPosition = format.IndexOf(":");
                    int dotPosition = format.LastIndexOf(".");

                    if (dotPosition < 0) {
                        dotPosition = format.Length;
                    }

                    string drive = format.Substring(0, colonPosition + 1);
                    string extension = format.Substring(dotPosition);
                    string name = format.Substring(colonPosition + 1, dotPosition - 2);

                    Dictionary<string, string> formatAttributes = new Dictionary<string, string> {
                        { attributeKeys[0], drive },
                        { attributeKeys[1], name },
                        { attributeKeys[2], extension },
                        { attributeKeys[3], "/Resources/btn_star_big_off.png" },
                        { FORMAT_SOURCE_KEY, FORMAT_SOURCE_PRINTER },
                        { SavedFormat._ID, "-1" }
                    };

                    AddFormatToList(formatAttributes);
                }
            } catch (ConnectionException error) {
                errorMessage = "Connection Error: " + error.Message;
            } finally {
                CloseConnection();
            }
        }

        private void AddFormatToList(Dictionary<string, string> formatAttributes) {
            formatInfo.Add(formatAttributes);
            Application.Current.Dispatcher.Invoke(() => {
                PrinterFormatsListView.Items.Add(new {
                    FormatDrive = formatAttributes[attributeKeys[0]],
                    FormatName = formatAttributes[attributeKeys[1]],
                    FormatExtension = formatAttributes[attributeKeys[2]],
                    StarImage = formatAttributes[attributeKeys[3]],
                    FormatSource = formatAttributes[FORMAT_SOURCE_KEY],
                    FormatId = formatAttributes[SavedFormat._ID]
                });
            });
        }

        private bool OpenConnection() {
            printerConnection.Open();
            return printerConnection.Connected;
        }

        private void CloseConnection() {
            printerConnection.Close();
        }

        private List<SavedFormat> GetSavedFormats() {
            List<SavedFormat> savedFormats = new List<SavedFormat>();
            using (SavedFormatProvider provider = new SavedFormatProvider()) {
                savedFormats = provider.GetSavedFormats();
            }
            return savedFormats;
        }

        private string AddFormatLocationToFormat(string format, string formatName) {
            int index = format.LastIndexOf("\u001eXA") + 3;
            string formatWithLocation = format.Substring(0, index);
            formatWithLocation += "\u001eDF" + formatName;
            formatWithLocation += format.Substring(index);
            return formatWithLocation;
        }

        private string GetFormatFromPrinter(string formatName) {
            string format = "";
            try {
                OpenConnection();
                ZebraPrinterLinkOs printer = ZebraPrinterFactory.GetLinkOsPrinter(printerConnection);

                byte[] formatInBytes = printer.RetrieveFormatFromPrinter(formatName);
                format = new UTF8Encoding().GetString(formatInBytes);
                format = format.Replace("\0", "");
            } catch (ConnectionException error) {
                errorMessage = "Connection Error: " + error.Message;
            } finally {
                CloseConnection();
            }
            return format;
        }

        private void PrintFormat(object sender, DoWorkEventArgs e) {
            Dictionary<int, string> formatVars = GetFormatVariables();
            try {
                OpenConnection();
                ZebraPrinter printer = ZebraPrinterFactory.GetInstance(printerConnection);

                string statusMessage = GetPrinterStatus(printer.GetCurrentStatus());
                if (statusMessage != null) {
                    errorMessage = "Printer Error: " + statusMessage + ". Please check your printer and try again.";
                } else {
                    if (!lastFormatOpenedSource.Equals(FORMAT_SOURCE_PRINTER)) {
                        printerConnection.Write(Encoding.UTF8.GetBytes(lastFormatOpenedContents));
                    }

                    printer.PrintStoredFormat(lastFormatOpened, formatVars, "UTF-8");
                    statusMessage = GetPrinterStatus(printer.GetCurrentStatus());
                    if (statusMessage != null) {
                        errorMessage = "Printer Error after Printing: " + statusMessage + ". Please check your printer.";
                    }
                }
            } catch (ConnectionException error) {
                errorMessage = "Connection Error: " + error.Message;
            } finally {
                CloseConnection();
            }
        }

        private string GetPrinterStatus(PrinterStatus printerStatus) {
            string statusMessage = null;
            if (printerStatus.isReadyToPrint) {
                if (printerStatus.isHeadCold) {
                    statusMessage = "Printhead too cold";
                } else if (printerStatus.isPartialFormatInProgress) {
                    statusMessage = "Partial format in progress";
                }
            } else {
                if (printerStatus.isHeadTooHot) {
                    statusMessage = "Printhead too hot";
                } else if (printerStatus.isHeadOpen) {
                    statusMessage = "Printhead open";
                } else if (printerStatus.isPaperOut) {
                    statusMessage = "Media out";
                } else if (printerStatus.isReceiveBufferFull) {
                    statusMessage = "Receive buffer full";
                } else if (printerStatus.isRibbonOut) {
                    statusMessage = "Ribbon Error";
                } else if (printerStatus.isPaused) {
                    statusMessage = "Printer Paused";
                } else {
                    statusMessage = "Unkown Error";
                }
            }
            return statusMessage;
        }

        private Dictionary<int, string> GetFormatVariables() {
            Dictionary<int, string> formatVars = new Dictionary<int, string>();
            for (int i = 0; i < formatVariableCollection.Count; i++) {
                int fieldNum = fieldDescDataVars[i].FieldNumber;
                formatVars.Add(fieldNum, formatVariableCollection[i].FieldValue);
            }
            return formatVars;
        }

        private List<FieldDescriptionData> FormatFieldDescriptionDataVars(List<FieldDescriptionData> variables) {
            foreach (FieldDescriptionData data in variables) {
                data.FieldName = data.FieldName ?? "Field " + data.FieldNumber;
            }
            return variables;
        }

        private void AddFieldsToFormat(ObservableCollection<FormatVariable> formatFields) {
            ListView formatVariables = FormatFieldsListView;
            formatVariables.ItemsSource = formatVariableCollection;
        }

        #endregion PrinterActions
    }
}
