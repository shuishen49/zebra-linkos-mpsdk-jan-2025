using Microsoft.Maui.Controls;
using Microsoft.Maui.Handlers;
using Microsoft.UI;
using Microsoft.UI.Windowing;
using Microsoft.UI.Xaml;
using Windows.Graphics;
using Windows.Networking.Proximity;
using Windows.UI.ViewManagement;
using WinRT.Interop;
using static Microsoft.Maui.ApplicationModel.Permissions;
using Colors = Microsoft.UI.Colors;
using uiMedia = Microsoft.UI.Xaml.Media;
// To learn more about WinUI, the WinUI project structure,
// and more about our project templates, see: http://aka.ms/winui-project-info.

namespace MauiPrintStation.WinUI
{
    /// <summary>
    /// Provides application-specific behavior to supplement the default Application class.
    /// </summary>
    public partial class App : MauiWinUIApplication
    {

        public NfcManagerImplementation nfcManagerImplementation;
        private ProximityDevice proximityDevice;
        private long messageId = -1;

        private MauiWinUIWindow mianWindow;
        private AppWindow appWindow;
        /// <summary>
        /// Initializes the singleton application object.  This is the first line of authored code
        /// executed, and as such is the logical equivalent of main() or WinMain().
        /// </summary>
        public App()
        {
            this.InitializeComponent();
            WindowHandler.Mapper.AppendToMapping(nameof(IWindow), (h, v) =>
            {
                mianWindow = h.PlatformView as MauiWinUIWindow;

                var nativeWindow = h.PlatformView;
                nativeWindow.Activate();

                var hWnd = WindowNative.GetWindowHandle(nativeWindow);
                var windowId = Win32Interop.GetWindowIdFromWindow(hWnd);
                appWindow = AppWindow.GetFromWindowId(windowId);
            });

            nfcManagerImplementation = DependencyService.Get<INfcManager>() as NfcManagerImplementation;
            proximityDevice = ProximityDevice.GetDefault();
            messageId = -1;

        }

        protected override MauiApp CreateMauiApp() => MauiProgram.CreateMauiApp();

        protected override void OnLaunched(LaunchActivatedEventArgs args)
        {
            base.OnLaunched(args);

            var currentWindow = Application.Windows[0].Handler.PlatformView;
            IntPtr _windowHandle = WindowNative.GetWindowHandle(currentWindow);
            var windowId = Win32Interop.GetWindowIdFromWindow(_windowHandle);
            AppWindow appWindow = AppWindow.GetFromWindowId(windowId);
            appWindow.Resize(new SizeInt32(480, 640));
        }

        private void SubscribeForNfcMessage()
        {
            proximityDevice = ProximityDevice.GetDefault();
            if (proximityDevice != null)
            {
                if (messageId == -1)
                {  // Only subscribe once
                    messageId = proximityDevice.SubscribeForMessage("WindowsUri", (device, message) => {
                        nfcManagerImplementation.OnNfcMessageReceived(device, message);
                    });
                }
            }
        }

        private void UnsubscribeForNfcMessage()
        {
            if (proximityDevice != null)
            {
                proximityDevice.StopSubscribingForMessage(messageId);
                messageId = -1;
            }
        }
    }
}