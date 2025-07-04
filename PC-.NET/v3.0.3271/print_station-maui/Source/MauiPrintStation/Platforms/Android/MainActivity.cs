
using Android.App;
using Android.Content.PM;
using Android.OS;
using Android.Nfc;
using Android.Content;
using Android;
using AndroidX.Core.Content;
using AndroidX.Core.App;

namespace MauiPrintStation.Droid
{
    [Activity(Label = "Print Station",LaunchMode = LaunchMode.SingleTop, Icon = "@mipmap/ic_launcher", RoundIcon = "@mipmap/ic_launcher_round", Theme = "@style/MainTheme", MainLauncher = true, ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation, ScreenOrientation = ScreenOrientation.Portrait),
        IntentFilter(new[] { "android.nfc.action.NDEF_DISCOVERED" }, DataHost = "zebra.com", DataPath = "/apps/r/nfc", DataScheme = "http", Categories = new[] { "android.intent.category.DEFAULT" }),
        IntentFilter(new[] { "android.nfc.action.NDEF_DISCOVERED" }, DataHost = "www.zebra.com", DataPath = "/apps/r/nfc", DataScheme = "http", Categories = new[] { "android.intent.category.DEFAULT" })]
    public class MainActivity : MauiAppCompatActivity
    {

        public const int AccessCoarseLocationPermissionRequestCode = 0;

        public NfcAdapter nfcAdapter = ((NfcManager)Android.App.Application.Context.GetSystemService(NfcService)).DefaultAdapter;
        public NfcManagerImplementation nfcManagerImplementation;
        bool isNfcAvailable = false;

        protected override void OnCreate(Bundle bundle)
        {
            //TabLayoutResource = Resource.Layout.Tabbar;
            //ToolbarResource = Resource.Layout.Toolbar;

            base.OnCreate(bundle);

            nfcManagerImplementation = DependencyService.Get<INfcManager>() as NfcManagerImplementation;
            isNfcAvailable = nfcManagerImplementation.IsNfcAvailable();

            //LoadApplication(new App());

            GetAccessCoarseLocationPermission();
        }

        protected override void OnResume()
        {
            base.OnResume();

            if (DependencyService.Get<INfcManager>().IsNfcAvailable())
            {
                if (NfcAdapter.ActionNdefDiscovered.Equals(Intent.Action))
                {
                    nfcManagerImplementation.OnNewIntent(this, Intent, false);
                }

                if (nfcAdapter != null)
                {
                    Intent intent = new Intent(this, GetType()).AddFlags(ActivityFlags.SingleTop);
                    PendingIntent pendingIntent = PendingIntent.GetActivity(this, 0, intent, PendingIntentFlags.Mutable);

                    nfcAdapter.EnableForegroundDispatch(this, pendingIntent, null, null);
                }
            }
        }

        protected override void OnPause()
        {
            base.OnPause();

            if (isNfcAvailable)
            {
                nfcAdapter.DisableForegroundDispatch(this);
            }
        }

        protected override void OnNewIntent(Intent intent)
        {
            base.OnNewIntent(intent);

            if (isNfcAvailable)
            {
                nfcManagerImplementation.OnNewIntent(this, intent, true);
            }
        }


        private void GetAccessCoarseLocationPermission()
        {
            List<string> deniedPermissions = new List<string>();
            if (Build.VERSION.SdkInt < BuildVersionCodes.S)
            {
                if (ContextCompat.CheckSelfPermission(this, Manifest.Permission.AccessCoarseLocation) == Permission.Granted)
                {
                    return;
                }

                if (ActivityCompat.ShouldShowRequestPermissionRationale(this, Manifest.Permission.AccessCoarseLocation))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.SetTitle("Permission Required")
                        .SetMessage("Print Station requires permission to access your location in order to perform Bluetooth discovery. Please accept this permission to allow Bluetooth discovery to function properly.")
                        .SetPositiveButton("OK", OnPermissionRequiredDialogOkClicked)
                        .SetCancelable(false)
                        .Show();

                    return;
                }

                RequestAccessCoarseLocationPermission();
            }
            else
            {
                if (!CheckPermission(Manifest.Permission.BluetoothScan))
                    deniedPermissions.Add(Manifest.Permission.BluetoothScan);
                if (!CheckPermission(Manifest.Permission.BluetoothConnect))
                    deniedPermissions.Add(Manifest.Permission.BluetoothConnect);
                if (deniedPermissions.Count != 0)
                {
                    RequestRuntimePermissions(
                        "Bluetooth permissions request",
                        "Bluetooth permissions request rationale",
                        1111,
                        deniedPermissions.ToArray());
                }
            }
        }
        /**
         * This method checks if a runtime permission has been granted.
         * @param permission The permission to check.
         * @return <code>true</code> if the permission has been granted, <code>false</code> otherwise.
         */
        private bool CheckPermission(string permission)
        {
            return ContextCompat.CheckSelfPermission(this, permission) == Permission.Granted;
        }

        private void RequestRuntimePermissions(string title, string description, int requestCode, params string[] permissions)
        {
            if (ActivityCompat.ShouldShowRequestPermissionRationale(this, permissions[0]))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                    .SetTitle(title)
                    .SetMessage(description)
                    .SetCancelable(false)
                    .SetNegativeButton(Android.Resource.String.No, (dialog, id) =>
                    {
                        // do nothing
                    })
                    .SetPositiveButton(Android.Resource.String.Ok, (dialog, id) =>
                    {
                        ActivityCompat.RequestPermissions(this, permissions, requestCode);
                    });
                builder.Show(); // method to show a dialog
            }
            else
            {
                ActivityCompat.RequestPermissions(this, permissions, requestCode);
            }
        }

        private void OnPermissionRequiredDialogOkClicked(object sender, DialogClickEventArgs e)
        {
            RequestAccessCoarseLocationPermission();
        }

        private void RequestAccessCoarseLocationPermission()
        {
            ActivityCompat.RequestPermissions(this, new string[] { Manifest.Permission.AccessCoarseLocation, Manifest.Permission.AccessFineLocation }, AccessCoarseLocationPermissionRequestCode);
        }
    }
}

