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
using System.Windows;
using Zebra.PrintStationCard.Util;
using Zebra.Sdk.Card.Printer;
using Zebra.Sdk.Comm;

namespace Zebra.PrintStationCard.ChoosePrinter {

    /// <summary>
    /// Interaction logic for ManualConnectWindow.xaml
    /// </summary>
    public partial class ManualConnectWindow : Window {

        private PrinterInfo printerInfo;

        public ManualConnectWindow() {
            InitializeComponent();
        }

        private void Save_Click(object sender, RoutedEventArgs e) {
            printerInfo = new PrinterInfo {
                Address = PrinterAddress_TextBox.Text
            };

            Connection connection = null;
            ZebraCardPrinter zebraCardPrinter = null;

            try {
                connection = new TcpConnection(printerInfo.Address, 9100);
                connection.Open();

                zebraCardPrinter = ZebraCardPrinterFactory.GetInstance(connection);
                printerInfo.Model = zebraCardPrinter.GetPrinterInformation().Model;
                if (printerInfo.Model.ToLower().Contains("zxp1") || printerInfo.Model.ToLower().Contains("zxp3")) {
                    throw new ConnectionException("Printer model not supported");
                }
            } catch (Exception error) {
                printerInfo = null;
                MessageBoxHelper.ShowError(error.Message);
            } finally {
                ConnectionHelper.CleanUpQuietly(zebraCardPrinter, connection);
                Close();
            }
        }

        private void Cancel_Click(object sender, RoutedEventArgs e) {
            printerInfo = null;
            Close();
        }

        public PrinterInfo PrinterInfo {
            get => printerInfo;
        }
    }
}
