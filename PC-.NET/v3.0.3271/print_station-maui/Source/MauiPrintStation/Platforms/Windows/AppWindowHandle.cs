using Microsoft.UI;
using Microsoft.UI.Windowing;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace MauiPrintStation.Platforms.Windows
{
    public static class AppWindowHandle
    {

        [DllImport("Microsoft.UI.Windowing.Core.dll", CharSet = CharSet.Unicode)]
        private static extern int GetWindowHandleFromWindowId(WindowId windowId, out IntPtr result);

        [DllImport("Microsoft.UI.Windowing.Core.dll", CharSet = CharSet.Unicode)]
        private static extern int GetWindowIdFromWindowHandle(IntPtr hwnd, out WindowId result);

        public static AppWindow GetAppWindow(this Microsoft.UI.Xaml.Window window)
        {
            IntPtr windowHandle = WinRT.Interop.WindowNative.GetWindowHandle(window);
            _ = GetWindowIdFromWindowHandle(windowHandle, out WindowId windowId);
            return AppWindow.GetFromWindowId(windowId);
        }
    }
}
