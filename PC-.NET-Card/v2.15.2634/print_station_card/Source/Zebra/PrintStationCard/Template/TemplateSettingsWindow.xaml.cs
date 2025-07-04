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
using System.Windows.Forms;
using Zebra.PrintStationCard.Util;
using Zebra.Sdk.Card.Job.Template;

namespace Zebra.PrintStationCard.Template {

    /// <summary>
    /// Interaction logic for TemplateSettingsWindow.xaml
    /// </summary>
    public partial class TemplateSettingsWindow : Window {
        
        private string templateFileDirectory = Properties.Settings.Default.TemplateFileDirectory;
        private string templateImageDirectory = Properties.Settings.Default.TemplateImageDirectory;
        
        public TemplateSettingsWindow() {
            InitializeComponent();

            TemplateDirTextBox.Text = Properties.Settings.Default.TemplateFileDirectory;
            TemplateImageDirTextBox.Text = Properties.Settings.Default.TemplateImageDirectory;
        }

        private void TemplateDirBrowse_Click(object sender, RoutedEventArgs e) {
            using (var folderBrowserDialog = new FolderBrowserDialog()) {
                DialogResult result = folderBrowserDialog.ShowDialog();
                if (result == System.Windows.Forms.DialogResult.OK) {
                    TemplateDirTextBox.Text = folderBrowserDialog.SelectedPath;
                    templateFileDirectory = folderBrowserDialog.SelectedPath;
                }
            }
        }

        private void TemplateImageDirBroswe_Click(object sender, RoutedEventArgs e) {
            using (var folderBrowserDialog = new FolderBrowserDialog()) {
                DialogResult result = folderBrowserDialog.ShowDialog();
                if (result == System.Windows.Forms.DialogResult.OK) {
                    TemplateImageDirTextBox.Text = folderBrowserDialog.SelectedPath;
                    templateImageDirectory = folderBrowserDialog.SelectedPath;
                }
            }
        }

        private void Save_Click(object sender, RoutedEventArgs e) {
            if (string.IsNullOrEmpty(templateFileDirectory) || string.IsNullOrEmpty(templateImageDirectory)) {
                MessageBoxHelper.ShowError("The specified directories cannot be empty.\nPlease update the directories before saving.");
            } else {
                Properties.Settings.Default.TemplateFileDirectory = templateFileDirectory;
                Properties.Settings.Default.TemplateImageDirectory = templateImageDirectory;
                DialogResult = true;
                Close();
            }
        }

        private void Cancel_Click(object sender, RoutedEventArgs e) {
            DialogResult = false;
            Close();
        }

        private void TemplateDirTextBox_TextChanged(object sender, System.Windows.Controls.TextChangedEventArgs e) {
            templateFileDirectory = TemplateDirTextBox.Text;
        }

        private void TemplateImageDirTextBox_TextChanged(object sender, System.Windows.Controls.TextChangedEventArgs e) {
            templateImageDirectory = TemplateImageDirTextBox.Text;
        }
    }
}
