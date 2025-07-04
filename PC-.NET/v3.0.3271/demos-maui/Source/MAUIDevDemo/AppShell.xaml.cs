namespace MauiDevDemo
{
    public partial class AppShell : Shell
    {
        public AppShell()
        {
            InitializeComponent();
            Routing.RegisterRoute(nameof(DemoSelectionPage), typeof(DemoSelectionPage));
            Routing.RegisterRoute(nameof(ConnectivityDemoPage), typeof(ConnectivityDemoPage));
            Routing.RegisterRoute(nameof(DiscoveryDemoPage), typeof(DiscoveryDemoPage));
            Routing.RegisterRoute(nameof(MultichannelDemoPage), typeof(MultichannelDemoPage));
            Routing.RegisterRoute(nameof(PrinterStatusDemoPage), typeof(PrinterStatusDemoPage));
            Routing.RegisterRoute(nameof(ProfileDemoPage), typeof(ProfileDemoPage));
            Routing.RegisterRoute(nameof(SendFileDemoPage), typeof(SendFileDemoPage));
            Routing.RegisterRoute(nameof(SettingsDemoPage), typeof(SettingsDemoPage));
            Routing.RegisterRoute(nameof(SignatureCaptureDemoPage), typeof(SignatureCaptureDemoPage));
            Routing.RegisterRoute(nameof(StatusChannelDemoPage), typeof(StatusChannelDemoPage));
        }
    }
}
