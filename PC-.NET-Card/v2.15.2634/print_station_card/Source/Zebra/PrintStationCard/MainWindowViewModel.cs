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

using System.Collections.Generic;
using System.Collections.ObjectModel;
using Zebra.PrintStationCard.Util;

namespace Zebra.PrintStationCard {

    public class MainWindowViewModel : ViewModelBase {

        private string selectedTemplateName;
        private PrinterInfo selectedPrinter;
        private LinkedList<PrinterInfo> recentlySelectedPrinters = new LinkedList<PrinterInfo>();
        private ObservableCollection<string> templateNames = new ObservableCollection<string>();
        private ObservableCollection<TemplateVariable> templateVariables = new ObservableCollection<TemplateVariable>();

        public MainWindowViewModel() {
            recentlySelectedPrinters.AddFirst(new PrinterInfo {
                Address = "Select a Printer",
                Model = ""
            });
            selectedPrinter = recentlySelectedPrinters.First.Value;
        }

        public string SelectedTemplateName {
            get => selectedTemplateName;
            set {
                selectedTemplateName = value;
                OnPropertyChanged();
            }
        }

        public PrinterInfo SelectedPrinter {
            get => selectedPrinter;
            set {
                selectedPrinter = value;
                OnPropertyChanged();
            }
        }

        public LinkedList<PrinterInfo> RecentlySelectedPrinters {
            get => recentlySelectedPrinters;
        }

        public ObservableCollection<string> TemplateNames {
            get => templateNames;
        }

        public ObservableCollection<TemplateVariable> TemplateVariables {
            get => templateVariables;
        }
    }
}
