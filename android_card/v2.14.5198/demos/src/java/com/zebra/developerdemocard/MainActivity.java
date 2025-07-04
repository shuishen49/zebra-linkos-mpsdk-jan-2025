/*
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
 */

package com.zebra.developerdemocard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.developerdemocard.discovery.PrinterSelectionActivity;
import com.zebra.developerdemocard.discovery.PrinterStatusUpdateTask;
import com.zebra.developerdemocard.discovery.ReconnectPrinterTask;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.graphicconversion.GraphicConversionDemoActivity;
import com.zebra.developerdemocard.magencode.MagEncodeDemoActivity;
import com.zebra.developerdemocard.multijob.MultiJobDemoActivity;
import com.zebra.developerdemocard.print.SendPrintJobDemoActivity;
import com.zebra.developerdemocard.printerstatus.PrinterStatusDemoActivity;
import com.zebra.developerdemocard.settings.SettingsDemoActivity;
import com.zebra.developerdemocard.smartcard.SmartCardDemoActivity;
import com.zebra.developerdemocard.template.TemplateDemoActivity;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.StorageHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.developerdemocard.util.UsbHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import com.zebra.zebraui.ZebraChevronButton;
import com.zebra.zebraui.ZebraPrinterView;

public class MainActivity extends AppCompatActivity implements PrinterStatusUpdateTask.OnUpdatePrinterStatusListener,
        ReconnectPrinterTask.OnReconnectPrinterListener {

    private static final int REQUEST_START_DEMO_ACTIVITY = 3001;

    private ReconnectPrinterTask reconnectPrinterTask;
    private PrinterStatusUpdateTask printerStatusUpdateTask;

    private LinearLayout printerSelectedContainer;
    private FrameLayout noPrinterSelectedContainer;
    private ZebraPrinterView printerView;
    private TextView printerAddress;
    private TextView printerModel;
    private LinearLayout progressOverlay;
    private TextView progressMessage;

    private ZebraChevronButton magEncodeDemoButton;
    private ZebraChevronButton multiJobDemoButton;
    private ZebraChevronButton printDemoButton;
    private ZebraChevronButton printerStatusDemoButton;
    private ZebraChevronButton settingsDemoButton;
    private ZebraChevronButton smartCardDemoButton;
    private ZebraChevronButton templateDemoButton;

    private BroadcastReceiver usbDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                DiscoveredPrinter printer = SelectedPrinterManager.getSelectedPrinter();

                if (printer instanceof DiscoveredPrinterUsb
                        && UsbDiscoverer.isZebraUsbDevice(device)
                        && ((DiscoveredPrinterUsb) printer).device.equals(device)) {
                    SelectedPrinterManager.setSelectedPrinter(null);
                    refreshSelectedPrinterView();
                }
            }
        }
    };
    private BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsbHelper.ACTION_USB_PERMISSION_GRANTED.equals(intent.getAction())) {
                synchronized (this) {
                    boolean permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null && UsbDiscoverer.isZebraUsbDevice(device)) {
                        if (permissionGranted) {
                            SelectedPrinterManager.setSelectedPrinter(new DiscoveredPrinterUsb(device.getDeviceName(), UsbHelper.getUsbManager(MainActivity.this), device));
                        } else {
                            DialogHelper.showErrorDialog(MainActivity.this, getString(R.string.usb_permissions_denied_message));
                        }
                    }

                    ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);
                    refreshSelectedPrinterView();
                }
            }
        }
    };
    private BroadcastReceiver usbDeviceAttachedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && UsbDiscoverer.isZebraUsbDevice(device)) {
                    SelectedPrinterManager.setSelectedPrinter(null);

                    UsbManager usbManager = UsbHelper.getUsbManager(MainActivity.this);
                    if (!usbManager.hasPermission(device)) {
                        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.requesting_usb_permission));
                        UsbHelper.requestUsbPermission(MainActivity.this, usbManager, device);
                    } else {
                        SelectedPrinterManager.setSelectedPrinter(new DiscoveredPrinterUsb(device.getDeviceName(), UsbHelper.getUsbManager(MainActivity.this), device));
                        refreshSelectedPrinterView();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        UIHelper.setLogoOnActionBar(this);

        registerReceivers();

        printerSelectedContainer = findViewById(R.id.printerSelectedContainer);
        noPrinterSelectedContainer = findViewById(R.id.noPrinterSelectedContainer);
        printerView = findViewById(R.id.printerView);
        printerAddress = findViewById(R.id.printerAddress);
        printerModel = findViewById(R.id.printerModel);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);

        ZebraChevronButton graphicConversionDemoButton = findViewById(R.id.graphicConversionDemoButton);
        magEncodeDemoButton = findViewById(R.id.magEncodeDemoButton);
        multiJobDemoButton = findViewById(R.id.multiJobDemoButton);
        printDemoButton = findViewById(R.id.printDemoButton);
        printerStatusDemoButton = findViewById(R.id.printerStatusDemoButton);
        settingsDemoButton = findViewById(R.id.settingsDemoButton);
        smartCardDemoButton = findViewById(R.id.smartCardDemoButton);
        templateDemoButton = findViewById(R.id.templateDemoButton);

        graphicConversionDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, GraphicConversionDemoActivity.class), REQUEST_START_DEMO_ACTIVITY);
            }
        });

        magEncodeDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MagEncodeDemoActivity.class));
            }
        });

        multiJobDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MultiJobDemoActivity.class));
            }
        });

        printDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, SendPrintJobDemoActivity.class), REQUEST_START_DEMO_ACTIVITY);
            }
        });

        printerStatusDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PrinterStatusDemoActivity.class));
            }
        });

        settingsDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SettingsDemoActivity.class), REQUEST_START_DEMO_ACTIVITY);
            }
        });

        smartCardDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SmartCardDemoActivity.class));
            }
        });

        templateDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, TemplateDemoActivity.class), REQUEST_START_DEMO_ACTIVITY);
            }
        });

        printerSelectedContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptDisconnectPrinter();
            }
        });

        noPrinterSelectedContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PrinterSelectionActivity.class));
            }
        });

        refreshSelectedPrinterView();
    }

    @Override
    protected void onDestroy() {
        if (reconnectPrinterTask != null) {
            reconnectPrinterTask.cancel(true);
        }

        if (printerStatusUpdateTask != null) {
            printerStatusUpdateTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
        refreshSelectedPrinterView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_START_DEMO_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    boolean resetPrinter = data.getBooleanExtra(SettingsDemoActivity.KEY_RESET_PRINTER, false);

                    if (reconnectPrinterTask != null) {
                        reconnectPrinterTask.cancel(true);
                    }

                    reconnectPrinterTask = new ReconnectPrinterTask(SelectedPrinterManager.getSelectedPrinter(), resetPrinter);
                    reconnectPrinterTask.setOnPrinterDiscoveryListener(this);
                    reconnectPrinterTask.execute();
                } else if (resultCode == RESULT_CANCELED) {
                    if (data != null) {
                        int permissionResult = data.getIntExtra(StorageHelper.KEY_STORAGE_PERMISSIONS_RESULT, -1);
                        if (permissionResult == StorageHelper.PERMISSION_DENIED) {
                            UIHelper.showSnackbar(this, getString(R.string.storage_permissions_denied));
                        } else if (permissionResult == StorageHelper.PERMISSION_NEVER_ASK_AGAIN_SET) {
                            UIHelper.showSnackbar(this, getString(R.string.storage_permissions_request_enable_message));
                        }
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem disconnectPrinterMenuItem = menu.findItem(R.id.disconnectPrinter);
        disconnectPrinterMenuItem.setVisible(SelectedPrinterManager.getSelectedPrinter() != null);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnectPrinter:
                promptDisconnectPrinter();
                return true;
            case R.id.discoverPrinters:
                startActivity(new Intent(this, PrinterSelectionActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onUpdatePrinterStatusStarted() {
        printerView.setPrinterStatus(ZebraPrinterView.PrinterStatus.REFRESHING);
    }

    @Override
    public void onUpdatePrinterStatusFinished(Exception exception, ZebraPrinterView.PrinterStatus printerStatus) {
        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_updating_printer_status_message, exception.getMessage()));
            SelectedPrinterManager.setSelectedPrinter(null);
            refreshSelectedPrinterView();
        } else {
            printerView.setPrinterStatus(printerStatus);
            updateDemoButtons();
        }
    }

    @Override
    public void onReconnectPrinterStarted() {
        refreshSelectedPrinterView();
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.reconnecting_to_printer));
    }

    @Override
    public void onReconnectPrinterFinished(Exception exception) {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_reconnecting_to_printer_message, exception.getMessage()));
            SelectedPrinterManager.setSelectedPrinter(null);
        }

        refreshSelectedPrinterView();
    }

    private void promptDisconnectPrinter() {
        DialogHelper.createDisconnectDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SelectedPrinterManager.setSelectedPrinter(null);
                refreshSelectedPrinterView();
            }
        }).show();
    }

    private void refreshSelectedPrinterView() {
        DiscoveredPrinter printer = SelectedPrinterManager.getSelectedPrinter();
        boolean isPrinterSelected = printer != null;
        if (isPrinterSelected) {
            String address = printer.getDiscoveryDataMap().get("ADDRESS");
            printerAddress.setVisibility(address != null && !address.isEmpty() ? View.VISIBLE : View.GONE);
            printerAddress.setText(address);

            String model = printer.getDiscoveryDataMap().get("MODEL");
            printerModel.setVisibility(model != null && !model.isEmpty() ? View.VISIBLE : View.GONE);
            printerModel.setText(model);
            printerView.setPrinterModel(model);

            if (reconnectPrinterTask == null || reconnectPrinterTask.isBackgroundTaskFinished()) {
                if (printerStatusUpdateTask != null) {
                    printerStatusUpdateTask.cancel(true);
                }

                printerStatusUpdateTask = new PrinterStatusUpdateTask(this, printer);
                printerStatusUpdateTask.setOnUpdatePrinterStatusListener(this);
                printerStatusUpdateTask.execute();
            }
        }

        printerSelectedContainer.setVisibility(isPrinterSelected ? View.VISIBLE : View.GONE);
        noPrinterSelectedContainer.setVisibility(isPrinterSelected ? View.GONE : View.VISIBLE);
        updateDemoButtons();
        invalidateOptionsMenu();
    }

    private void updateDemoButtons() {
        boolean isReconnecting = reconnectPrinterTask != null && !reconnectPrinterTask.isBackgroundTaskFinished();
        boolean enabled = printerSelectedContainer.getVisibility() == View.VISIBLE && printerView.getPrinterStatus() != ZebraPrinterView.PrinterStatus.REFRESHING && !isReconnecting;
        magEncodeDemoButton.setEnabled(enabled);
        multiJobDemoButton.setEnabled(enabled);
        printDemoButton.setEnabled(enabled);
        printerStatusDemoButton.setEnabled(enabled);
        settingsDemoButton.setEnabled(enabled);
        templateDemoButton.setEnabled(enabled);
        smartCardDemoButton.setEnabled(enabled);
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDisconnectReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbHelper.ACTION_USB_PERMISSION_GRANTED);
        registerReceiver(usbPermissionReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(usbDeviceAttachedReceiver, filter);
    }

    private void unregisterReceivers() {
        unregisterReceiver(usbDisconnectReceiver);
        unregisterReceiver(usbPermissionReceiver);
        unregisterReceiver(usbDeviceAttachedReceiver);
    }
}
