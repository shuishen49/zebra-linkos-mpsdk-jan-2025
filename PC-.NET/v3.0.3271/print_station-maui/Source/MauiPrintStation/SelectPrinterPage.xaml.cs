using ExtensionMethods;
using MauiPrintStation.Utilities;
using Zebra.Sdk.Comm;
using Zebra.Sdk.Printer.Discovery;
using ViewExtensions = Microsoft.Maui.Controls.ViewExtensions;

namespace MauiPrintStation
{
    public partial class SelectPrinterPage : ContentPage
    {

        private MainPage mainPage;
        private SelectPrinterPageViewModel viewModel = new SelectPrinterPageViewModel();

        public SelectPrinterPage(MainPage mainPage)
        {
            InitializeComponent();
            BindingContext = viewModel;
            this.mainPage = mainPage;

            MainThread.BeginInvokeOnMainThread(async () =>
            {
                await DiscoverPrintersAsync();
            });
        }

        private async Task ClearDiscoveredPrinterListAsync()
        {
            await Task.Factory.StartNew(() =>
            {
                viewModel.HighlightedPrinter = null;
            });
            viewModel.DiscoveredPrinterList.Clear();
        }

        private async Task AnimateRefreshIconAsync()
        {
            await RefreshIconAnimator.AnimateOneRotationAsync(RefreshIcon);

            if (viewModel.IsPrinterListRefreshing)
            {
                await AnimateRefreshIconAsync();
            }
        }

        private async Task DiscoverPrintersAsync()
        {
            await ClearDiscoveredPrinterListAsync();

            await Task.Factory.StartNew(() =>
            {
                viewModel.IsPrinterListRefreshing = true;
            });

            MainThread.BeginInvokeOnMainThread(async () =>
            {
                await AnimateRefreshIconAsync();
            });

            await Task.Factory.StartNew(() =>
            {
                try
                {
                    List<DiscoveredPrinter> usbDriverPrinters = DependencyService.Get<IConnectionManager>().GetZebraUsbDriverPrinters();
                    foreach (DiscoveredPrinter printer in usbDriverPrinters)
                    {
                        viewModel.DiscoveredPrinterList.SafeAdd(printer);
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("An exception occurred: " + ex.Message);
                    Console.WriteLine("Stack Trace:");
                    Console.WriteLine(ex.StackTrace);
                }

                try
                {
                    DiscoveryHandlerImplementation usbDiscoveryHandler = new DiscoveryHandlerImplementation(this);
                    DependencyService.Get<IConnectionManager>().GetZebraUsbDirectPrinters(usbDiscoveryHandler);

                    while (!usbDiscoveryHandler.IsFinished)
                    {
                        Thread.Sleep(100);
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("An exception occurred: " + ex.Message);
                    Console.WriteLine("Stack Trace:");
                    Console.WriteLine(ex.StackTrace);
                }

                DiscoveryHandlerImplementation networkDiscoveryHandler = new DiscoveryHandlerImplementation(this);
                NetworkDiscoverer.LocalBroadcast(networkDiscoveryHandler);

                while (!networkDiscoveryHandler.IsFinished)
                {
                    Thread.Sleep(100);
                }

                PlatformHelperImplementation platformHelperImplementation = new PlatformHelperImplementation();
                // TODO Xamarin.Forms.Device.RuntimePlatform is no longer supported. Use Microsoft.Maui.Devices.DeviceInfo.Platform instead. For more details see https://learn.microsoft.com/en-us/dotnet/maui/migration/forms-projects#device-changes
                //if (platformHelperImplementation.IsWindows10())
                //{
                try
                {
                    DiscoveryHandlerImplementation bluetoothDiscoveryHandler = new DiscoveryHandlerImplementation(this);
                    DependencyService.Get<IConnectionManager>().FindBluetoothPrinters(bluetoothDiscoveryHandler);

                    while (!bluetoothDiscoveryHandler.IsFinished)
                    {
                        Thread.Sleep(100);
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine("An exception occurred: " + ex.Message);
                    Console.WriteLine("Stack Trace:");
                    Console.WriteLine(ex.StackTrace);
                }
                //}
            });

            await Task.Factory.StartNew(() =>
            {
                viewModel.IsPrinterListRefreshing = false;
            });

            ViewExtensions.CancelAnimations(RefreshIcon);
        }

        private async void RefreshIcon_Tapped(object sender, EventArgs e)
        {
            if (!viewModel.IsPrinterListRefreshing)
            {
                await DiscoverPrintersAsync();
            }
        }

        private async void SelectButton_Clicked(object sender, EventArgs eventArgs)
        {
            if (!viewModel.IsSelectingPrinter && Navigation.NavigationStack.Count > 0 && Navigation.NavigationStack.Last().GetType() == typeof(SelectPrinterPage))
            {
                viewModel.IsSelectingPrinter = true;

                try
                {
                    DiscoveredPrinter selectedPrinter = (DiscoveredPrinter)PrinterList.SelectedItem;
                    Connection connection = null;

                    try
                    {
                        await Task.Factory.StartNew(() =>
                        {
                            connection = ConnectionCreator.Create(selectedPrinter);
                            connection.Open();
                        });
                    }
                    catch (Exception e)
                    {
                        await AlertCreator.ShowErrorAsync(this, e.Message);
                        return;
                    }
                    finally
                    {
                        await Task.Factory.StartNew(() =>
                        {
                            try
                            {
                                connection?.Close();
                            }
                            catch (ConnectionException) { }
                        });
                    }

                    await Task.Factory.StartNew(() =>
                    {
                        mainPage.ViewModel.SelectedPrinter = selectedPrinter;
                    });

                    mainPage.RefreshFormatLists();

                    await Navigation.PopAsync();
                }
                finally
                {
                    await Task.Factory.StartNew(() =>
                    {
                        viewModel.IsSelectingPrinter = false;
                    });
                }
            }
        }

        class DiscoveryHandlerImplementation : DiscoveryHandler
        {

            private SelectPrinterPage selectPrinterPage;

            public bool IsFinished { get; private set; } = false;

            public DiscoveryHandlerImplementation(SelectPrinterPage selectPrinterPage)
            {
                this.selectPrinterPage = selectPrinterPage;
            }

            public void DiscoveryError(string message)
            {
                IsFinished = true;
            }

            public void DiscoveryFinished()
            {
                IsFinished = true;
            }

            public void FoundPrinter(DiscoveredPrinter printer)
            {
                MainThread.BeginInvokeOnMainThread(async () =>
                {
                    selectPrinterPage.viewModel.DiscoveredPrinterList.Add(printer); // ListView view model operations must be done on UI thread due to iOS issues when clearing list while item is selected: https://forums.xamarin.com/discussion/19114/invalid-number-of-rows-in-section
                });
            }
        }

        StackLayout oldStackLayout = null;
        private void TapGestureRecognizer_Tapped(object sender, EventArgs e)
        {
            var tappedStackLayout = (StackLayout)sender;
            if (oldStackLayout != null)
            {
                Frame oldParentFrame = FindParent<Frame>(oldStackLayout);
                if (oldParentFrame != null)
                {
                    oldParentFrame.BorderColor = (Color)ResourceHelper.FindResource(this, "White"); ;
                }
                oldStackLayout.BackgroundColor = (Color)ResourceHelper.FindResource(this, "White");
            }


            Frame parentFrame = FindParent<Frame>(tappedStackLayout);
            if (parentFrame != null)
            {
                parentFrame.BorderColor = (Color)ResourceHelper.FindResource(this, "Primary"); ;
            }
            tappedStackLayout.BackgroundColor = (Color)ResourceHelper.FindResource(this, "PrimaryLight"); ;
            oldStackLayout = tappedStackLayout;
            if ((DeviceInfo.Current.Platform == DevicePlatform.Android) || (DeviceInfo.Current.Platform == DevicePlatform.iOS))
            {
                var tappedElement = (VisualElement)sender;
                var tappedItem = tappedElement.BindingContext;
                PrinterList.SelectedItem = tappedItem;
            }
        }


        private T FindParent<T>(View view) where T : View
        {
            Element parent = view?.Parent;
            while (parent != null)
            {
                if (parent is T typedParent)
                {
                    return typedParent;
                }
                parent = parent.Parent;
            }
            return null;
        }
    }
}