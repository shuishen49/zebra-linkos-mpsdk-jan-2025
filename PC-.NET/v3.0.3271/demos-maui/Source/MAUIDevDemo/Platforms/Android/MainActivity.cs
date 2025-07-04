
using Android.App;
using Android.Content.PM;
using Android.OS;
using Android.Nfc;
using Android.Content;
using Android;
using AndroidX.Core.Content;
using AndroidX.Core.App;
using Android.Provider;
using Android.Locations;

namespace MauiDevDemo
{
    [Activity(Theme = "@style/Maui.SplashTheme", MainLauncher = true, ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation | ConfigChanges.UiMode | ConfigChanges.ScreenLayout | ConfigChanges.SmallestScreenSize | ConfigChanges.Density)]
    public class MainActivity : MauiAppCompatActivity
    {

        public const int AccessCoarseLocationPermissionRequestCode = 0;

        protected override void OnCreate(Bundle bundle)
        {
            base.OnCreate(bundle);

            GetAccessCoarseLocationPermission();
        }

        private void GetAccessCoarseLocationPermission()
        {
            TurnOnLocation(this);
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
                        .SetMessage("Zebra Developer Demos requires permission to access your location in order to perform Bluetooth discovery. Please accept this permission to allow Bluetooth discovery to function properly.")
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

        public void TurnOnLocation(Context context)
        {
            LocationManager locationManager = (LocationManager)context.GetSystemService(Context.LocationService);
            if (!locationManager.IsProviderEnabled(LocationManager.GpsProvider))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.SetTitle("Location Permission")
                    .SetMessage("Zebra Developer Demos requires location services to be enabled. Please enable your device's location settings.")
                    .SetPositiveButton("OK", OnLocationPermissionRequiredDialogOkClicked)
                    .SetCancelable(false)
                    .Show();
            }
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

        private void OnLocationPermissionRequiredDialogOkClicked(object sender, DialogClickEventArgs e)
        {
            Intent intent = new Intent(Android.Provider.Settings.ActionLocationSourceSettings);
            StartActivity(intent);
        }

        private void OnPermissionRequiredDialogOkClicked(object sender, DialogClickEventArgs e)
        {
            RequestAccessCoarseLocationPermission();
        }

        private void RequestAccessCoarseLocationPermission()
        {
            ActivityCompat.RequestPermissions(this, new string[] { Manifest.Permission.AccessFineLocation, Manifest.Permission.AccessCoarseLocation }, AccessCoarseLocationPermissionRequestCode);
        }
    }
}
