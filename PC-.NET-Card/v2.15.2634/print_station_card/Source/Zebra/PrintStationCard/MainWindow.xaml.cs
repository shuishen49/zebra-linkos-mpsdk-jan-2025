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
using System.ComponentModel;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Threading;
using Zebra.PrintStationCard.ChoosePrinter;
using Zebra.PrintStationCard.Template;
using Zebra.PrintStationCard.Util;
using Zebra.Sdk.Card.Containers;
using Zebra.Sdk.Card.Exceptions;
using Zebra.Sdk.Card.Job.Template;
using Zebra.Sdk.Card.Printer;
using Zebra.Sdk.Comm;

namespace Zebra.PrintStationCard {

    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window {
        
        private static readonly Regex IpAddressRegex = new Regex(@"\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b");

        private MainWindowViewModel viewModel;

        public Dictionary<string, string> previousPrinters;
        private PrinterActionsWindow printerActionsWindow;
        private PrinterDiscoveryWindow printerDiscoveryWindow;
        private Connection connection;

        public string printerIpAddress;
        public string printerModel;
        private string errorMessage;
        
        public MainWindow() {
            InitializeComponent();

            viewModel = DataContext as MainWindowViewModel;

            if (!string.IsNullOrEmpty(Properties.Settings.Default.PreviousPrinters)) {
                previousPrinters = JsonConvert.DeserializeObject<Dictionary<string, string>>(Properties.Settings.Default.PreviousPrinters);
                foreach (string address in previousPrinters.Keys) {
                    if (address.Length > 0) {
                        viewModel.RecentlySelectedPrinters.AddAfter(viewModel.RecentlySelectedPrinters.First, new Util.PrinterInfo {
                            Address = address,
                            Model = previousPrinters[address]
                        });
                    }
                }
            }

            if (previousPrinters == null) {
                previousPrinters = new Dictionary<string, string>();
            }

            string savedPrinterIpAddress = Properties.Settings.Default.IpAddress;
            if (!string.IsNullOrEmpty(savedPrinterIpAddress)) {
                string savedPrinterModel = Properties.Settings.Default.Model;

                Util.PrinterInfo selectedPrinter = new Util.PrinterInfo {
                    Address = savedPrinterIpAddress,
                    Model = savedPrinterModel
                };
                viewModel.RecentlySelectedPrinters.AddAfter(viewModel.RecentlySelectedPrinters.First, selectedPrinter);
                viewModel.SelectedPrinter = selectedPrinter;
                RefreshTemplates();
            } else {
                ShowPrinterDiscoveryWindow();
            }
        }

        private void Window_Loaded(object sender, RoutedEventArgs e) {
            MinWidth = Width;
            MinHeight = Height;
        }

        private void Window_Closing(object sender, CancelEventArgs e) {
            Properties.Settings.Default.PreviousPrinters = JsonConvert.SerializeObject(previousPrinters);
            Properties.Settings.Default.Save();
        }

        public void ShowPrinterDiscoveryWindow() {
            printerDiscoveryWindow = new PrinterDiscoveryWindow() {
                Topmost = true
            };

            if (printerDiscoveryWindow.ShowDialog() == false) {
                if (printerDiscoveryWindow != null) {
                    Util.PrinterInfo selectedPrinter = printerDiscoveryWindow.SelectedPrinterInfo;
                    if (selectedPrinter.Address != null && selectedPrinter.Model != null && viewModel != null) {
                        LinkedListNode<Util.PrinterInfo> node = viewModel.RecentlySelectedPrinters.First;
                        while (node != null) {
                            Util.PrinterInfo printerInfo = node.Value;
                            if (printerInfo.Address.Equals(selectedPrinter.Address)) {
                                if (!printerInfo.Model.Equals(selectedPrinter.Model)) {
                                    printerInfo.Model = selectedPrinter.Model;
                                    node.Value = printerInfo;
                                }
                                SelectedPrinterComboBox.SelectedItem = printerInfo;
                                return;
                            }
                            node = node.Next;
                        }

                        string previousPrinterIp = Properties.Settings.Default.IpAddress;
                        if (previousPrinterIp.Length > 0) {
                            previousPrinters.Add(previousPrinterIp, Properties.Settings.Default.Model);
                        }

                        viewModel.RecentlySelectedPrinters.AddAfter(viewModel.RecentlySelectedPrinters.First, selectedPrinter);
                        viewModel.SelectedPrinter = selectedPrinter;
                    }
                    printerDiscoveryWindow = null;
                }
            }
        }

        private void SelectedPrinterClosedEvent(object sender, EventArgs e) {
            int selectedIndex = SelectedPrinterComboBox.SelectedIndex;
            if (selectedIndex == 0 && printerDiscoveryWindow == null) {
                ShowPrinterDiscoveryWindow();
            }
        }

        private void SelectedPrinterChangeEvent(object sender, SelectionChangedEventArgs e) {
            if (viewModel != null) {
                if (SelectedPrinterComboBox.SelectedIndex == 0) {
                    ShowPrinterDiscoveryWindow();
                } else {
                    if (printerDiscoveryWindow != null) {
                        printerDiscoveryWindow.Close();
                        printerDiscoveryWindow = null;
                    }

                    if (PrinterTemplatesListView.Visibility == Visibility.Collapsed) {
                        PrinterTemplatesListView.Visibility = Visibility.Visible;
                        RefreshTemplates_MenuItem.Visibility = Visibility.Visible;
                        SetTemplateDir_MenuItem.Visibility = Visibility.Visible;
                        TemplateGrid.Visibility = Visibility.Collapsed;
                    }

                    dynamic selectedItem = SelectedPrinterComboBox.SelectedItem;
                    if (!previousPrinters.ContainsKey(selectedItem.Address) && previousPrinters.Count >= 5) {
                        string removeKey = viewModel.RecentlySelectedPrinters.Last.Value.Address;
                        previousPrinters.Remove(removeKey);
                        viewModel.RecentlySelectedPrinters.RemoveLast();
                    }

                    if (previousPrinters.ContainsKey(Properties.Settings.Default.IpAddress)) {
                        previousPrinters.Remove(Properties.Settings.Default.IpAddress);
                    }

                    previousPrinters.Add(Properties.Settings.Default.IpAddress, Properties.Settings.Default.Model);
                    Properties.Settings.Default.Model = selectedItem.Model;
                    Properties.Settings.Default.IpAddress = selectedItem.Address;

                    if (previousPrinters.ContainsKey(selectedItem.Address)) {
                        previousPrinters.Remove(selectedItem.Address);
                    }

                    SetSelectedConnection(selectedItem);
                    if (viewModel.TemplateNames.Count == 0) {
                        RefreshTemplates();
                    }
                }
            }
        }

        private void OptionsButton_Click(object sender, RoutedEventArgs e) {
            ContextMenu optionsContextMenu = (sender as Button).ContextMenu;
            optionsContextMenu.PlacementTarget = (sender as Button);
            optionsContextMenu.Placement = PlacementMode.Bottom;
            optionsContextMenu.IsOpen = true;
        }

        private void SetTemplateDirectories_Click(object sender, RoutedEventArgs e) {
            bool areDirectoriesUpdated = (bool) new TemplateSettingsWindow() {
                Owner = this
            }.ShowDialog();
            if (areDirectoriesUpdated) {
                RefreshTemplates();
            }
        }

        private void RefreshTemplates_Click(object sender, RoutedEventArgs e) {
            RefreshTemplates();
        }

        private void ShowAboutWindow_Click(object sender, RoutedEventArgs e) {
            new AboutWindow() {
                Owner = this
            }.ShowDialog();
        }

        private void OnCancel_Click(object sender, RoutedEventArgs e) {
            PrinterTemplatesListView.Visibility = Visibility.Visible;
            RefreshTemplates_MenuItem.Visibility = Visibility.Visible;
            SetTemplateDir_MenuItem.Visibility = Visibility.Visible;
            TemplateGrid.Visibility = Visibility.Collapsed;
        }

        private void PrinterTemplatesListView_MouseDoubleClick(object sender, MouseButtonEventArgs e) {
            if (viewModel.SelectedTemplateName != null) {
                viewModel.TemplateVariables.Clear();
                string templateName = viewModel.SelectedTemplateName;
                printerActionsWindow = new PrinterActionsWindow("Retrieving variables...") {
                    Owner = this
                };

                new  Task(() => {
                    List<string> templateVariables = GetTemplateVariables(templateName);
                    TemplateVariablesRetrieved(templateVariables);
                }).Start();

                printerActionsWindow.ShowDialog();
            }
        }

        private void OnPrintTemplate_Click(object sender, RoutedEventArgs e) {
            SelectedPrinterComboBox.Items.Refresh();
            string selectedTemplate = viewModel.SelectedTemplateName;

            printerActionsWindow = new PrinterActionsWindow("Printing template...") {
                Owner = this
            };

            new Task(() => {
                PrintTemplate(selectedTemplate);
                PrintTemplateCompleted();
            }).Start();

            printerActionsWindow.ShowDialog();
        }
        
        private List<String> GetTemplateVariables(string selectedTemplate) {
            List<string> templateVariables = null;
            ZebraCardPrinter zebraCardPrinter = null;

            try {
                connection.Open();
                zebraCardPrinter = ZebraCardPrinterFactory.GetInstance(connection);

                ZebraCardTemplate zebraCardTemplate = new ZebraCardTemplate(zebraCardPrinter);
                zebraCardTemplate.SetTemplateFileDirectory(Properties.Settings.Default.TemplateFileDirectory);

                templateVariables = zebraCardTemplate.GetTemplateFields(selectedTemplate + ".xml");
            } catch (Exception error) {
                errorMessage = $"Connection Error: {error.Message}";
            } finally {
                ConnectionHelper.CleanUpQuietly(zebraCardPrinter, connection);
            }
            return templateVariables;
        }

        private void TemplateVariablesRetrieved(List<string> templateVariables) {
            Application.Current.Dispatcher.Invoke(() => {
                printerActionsWindow.Close();


                if (errorMessage != null) {
                    PrinterTemplatesListView.Visibility = Visibility.Visible;
                    RefreshTemplates_MenuItem.Visibility = Visibility.Visible;
                    SetTemplateDir_MenuItem.Visibility = Visibility.Visible;
                    TemplateGrid.Visibility = Visibility.Collapsed;

                    MessageBoxHelper.ShowError(errorMessage);
                    errorMessage = null;
                } else {
                    PrinterTemplatesListView.Visibility = Visibility.Collapsed;
                    RefreshTemplates_MenuItem.Visibility = Visibility.Collapsed;
                    SetTemplateDir_MenuItem.Visibility = Visibility.Collapsed;
                    TemplateGrid.Visibility = Visibility.Visible;

                    viewModel.TemplateVariables.Clear();

                    foreach (string fieldName in templateVariables) {
                        viewModel.TemplateVariables.Add(new TemplateVariable {
                            FieldName = fieldName
                        });
                    }
                }
            });
        }

        private void PrintTemplate(string selectedTemplate) {
            ZebraCardPrinter zebraCardPrinter = null;

            try {
                int quantity = 0;
                Application.Current.Dispatcher.Invoke(() => {
                    quantity = int.Parse(Quantity_ComboBox.Text);
                });

                connection.Open();
                zebraCardPrinter = ZebraCardPrinterFactory.GetInstance(connection);

                PrinterStatusInfo statusInfo = zebraCardPrinter.GetPrinterStatus();
                if (statusInfo.ErrorInfo.Value > 0) {
                    throw new ZebraCardException($"{statusInfo.Status} ({statusInfo.ErrorInfo.Description}). Please correct the issue and try again.");
                } else if (statusInfo.AlarmInfo.Value > 0) {
                    throw new ZebraCardException($"{statusInfo.Status} ({statusInfo.AlarmInfo.Description}). Please correct the issue and try again.");
                } else {
                    ZebraCardTemplate zebraCardTemplate = new ZebraCardTemplate(zebraCardPrinter);
                    zebraCardTemplate.SetTemplateFileDirectory(Properties.Settings.Default.TemplateFileDirectory);
                    zebraCardTemplate.SetTemplateImageDirectory(Properties.Settings.Default.TemplateImageDirectory);

                    Dictionary<string, string> templateData = GetTemplateData();
                    TemplateJob templateJob = zebraCardTemplate.GenerateTemplateJob(selectedTemplate + ".xml", templateData);
                    int jobId = zebraCardPrinter.PrintTemplate(quantity, templateJob);
                }
            } catch (Exception error) {
                MessageBoxHelper.ShowError(error.Message);
            } finally {
                ConnectionHelper.CleanUpQuietly(zebraCardPrinter, connection);
            }
        }

        private void PrintTemplateCompleted() {
            Dispatcher.Invoke(new Action(() => {
                printerActionsWindow.Close();
            }));
        }

        private void RefreshTemplates() {
            if (viewModel.TemplateNames.Count > 0) {
                viewModel.TemplateNames.Clear();
            }

            if (!Directory.Exists(Properties.Settings.Default.TemplateFileDirectory)) {
                MessageBoxHelper.ShowError("The template file directory does not exist.\nPlease verify the specified directories in the Options menu.");
                return;
            }

            string[] savedTemplateFiles = Directory.GetFiles(Properties.Settings.Default.TemplateFileDirectory, "*.xml");
            if (savedTemplateFiles.Length > 0) {
                string test = savedTemplateFiles[0].Substring(savedTemplateFiles[0].LastIndexOf("\\") + 1);
                foreach (string templateFile in savedTemplateFiles) {
                    string templateName = templateFile.Substring(templateFile.LastIndexOf("\\") + 1);
                    int indexOfLastDot = templateName.LastIndexOf(".");
                    if (indexOfLastDot >= 0) {
                        templateName = templateName.Substring(0, indexOfLastDot);
                    }

                    viewModel.TemplateNames.Add(templateName);
                }
            } else {
                MessageBoxHelper.ShowError("No templates found.\nPlease verify the specified directories in the Options menu.");
            }
        }

        public void SetSelectedConnection(dynamic selectedItem) {
            if (IpAddressRegex.IsMatch(selectedItem.Address)) {
                connection = new TcpConnection(selectedItem.Address, 9100);
            } else {
                connection = new UsbConnection(selectedItem.Address);
            }

            if (SelectedPrinterComboBox != null) {
                int index = SelectedPrinterComboBox.Items.IndexOf(selectedItem);
                SelectedPrinterComboBox.SelectedIndex = index;
                SelectedPrinterComboBox.Items.Refresh();
            }
        }

        private Dictionary<string, string> GetTemplateData() {
            Dictionary<string, string> templateData = new Dictionary<string, string>();
            for (int i = 0; i < viewModel.TemplateVariables.Count; i++) {
                templateData.Add(viewModel.TemplateVariables[i].FieldName, viewModel.TemplateVariables[i].FieldValue);
            }
            return templateData;
        }
    }
}