[assembly: XamlCompilation(XamlCompilationOptions.Compile)]
namespace MauiPrintStation
{
    public partial class App : Application
    {

        private static PrintStationDatabase database;

        public static PrintStationDatabase Database
        {
            get
            {
                if (database == null)
                {
                    string directory = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "Print Station");
                    if (!Directory.Exists(directory))
                    {
                        Directory.CreateDirectory(directory);
                    }
                    database = new PrintStationDatabase(Path.Combine(directory, "PrintStation.db3"));
                }
                return database;
            }
        }

        public App()
        {
            InitializeComponent();
            DependencyService.Register<INfcManager, NfcManagerImplementation>();
            DependencyService.Register<IPrinterHelper, PrinterHelperImplementation>();
            DependencyService.Register<IConnectionManager, ConnectionManagerImplementation>();
            DependencyService.Register<IPlatformHelper, PlatformHelperImplementation>();
            MainPage = new NavigationPage(new MainPage());
        }

        protected override Window CreateWindow(IActivationState? activationState)
        {
            var window = base.CreateWindow(activationState);
            window.Title = AppInfo.Current.Name;
            return window;
        }

        protected override void OnStart()
        {
            // Handle when your app starts
        }

        protected override void OnSleep()
        {
            // Handle when your app sleeps
        }

        protected override void OnResume()
        {
            // Handle when your app resumes
        }
    }
}
