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

namespace MauiDevDemo
{

    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class DemoSelectionPage : ContentPage {

        public DemoSelectionPage() {
            InitializeComponent();
        }

        private async Task PushPageAsync(Page page) {
            if (Navigation.NavigationStack.Count == 0 || Navigation.NavigationStack.Last().GetType() == typeof(DemoSelectionPage)) {
                await Navigation.PushAsync(page);
            }
        }

        private void ConnectivityDemoButton_Clicked(object sender, EventArgs e)
        {
            _ = Shell.Current.GoToAsync(nameof(ConnectivityDemoPage));
        }

        private void DiscoveryDemoButton_Clicked(object sender, EventArgs e)
        {
            _ = Shell.Current.GoToAsync(nameof(DiscoveryDemoPage));
        }

        private void MultichannelDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(MultichannelDemoPage));
        }

        private void PrinterStatusDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(PrinterStatusDemoPage));
        }

        private void ProfileDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(ProfileDemoPage));
        }

        private void SendFileDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(SendFileDemoPage));
        }

        private void SettingsDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(SettingsDemoPage));
        }

        private void SignatureCaptureDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(SignatureCaptureDemoPage));
        }

        private void StatusChannelDemoButton_Clicked(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(StatusChannelDemoPage));
        }
    }
}