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

package com.zebra.developerdemocard.discovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.developerdemocard.util.UsbHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import com.zebra.zebraui.ZebraEditText;

import java.util.ArrayList;

public class PrinterSelectionActivity extends AppCompatActivity implements NetworkAndUsbDiscoveryTask.OnPrinterDiscoveryListener,
        ManualConnectionTask.OnManualConnectionListener {

    private boolean isApplicationBusy = false;
    private ManualConnectionTask manualConnectionTask;
    private NetworkAndUsbDiscoveryTask networkAndUsbDiscoveryTask;

    private DiscoveredPrinterListAdapter discoveredPrinterListAdapter;
    private Animation rotation;
    private ImageView refreshPrintersButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;

    private BroadcastReceiver usbDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && UsbDiscoverer.isZebraUsbDevice(device)) {
                    DiscoveredPrinterUsb disconnectedPrinter = new DiscoveredPrinterUsb(device.getDeviceName(), UsbHelper.getUsbManager(PrinterSelectionActivity.this), device);
                    String address = disconnectedPrinter.getDiscoveryDataMap().get("ADDRESS");
                    discoveredPrinterListAdapter.removePrinterWithAddress(address);
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
                            SelectedPrinterManager.setSelectedPrinter(new DiscoveredPrinterUsb(device.getDeviceName(), UsbHelper.getUsbManager(PrinterSelectionActivity.this), device));
                            finish();
                        } else {
                            DialogHelper.showErrorDialog(PrinterSelectionActivity.this, getString(R.string.usb_permissions_denied_message));
                        }
                    }

                    isApplicationBusy = false;
                    ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_printer_selection);
        UIHelper.setLogoOnActionBar(this);
        setTitle(R.string.discover_printers);

        registerReceivers();

        ImageView addPrinterButton = findViewById(R.id.addPrinterButton);
        ListView discoveredPrintersList = findViewById(R.id.discoveredPrintersList);
        refreshPrintersButton = findViewById(R.id.refreshPrintersButton);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);

        rotation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        discoveredPrinterListAdapter = new DiscoveredPrinterListAdapter(this, R.layout.list_item_discovered_printer, new ArrayList<DiscoveredPrinter>());
        discoveredPrintersList.setAdapter(discoveredPrinterListAdapter);
        discoveredPrintersList.setEmptyView(findViewById(R.id.noPrintersFoundContainer));
        discoveredPrintersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isApplicationBusy) {
                    isApplicationBusy = true;

                    ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.connecting_to_printer));

                    DiscoveredPrinter printer = discoveredPrinterListAdapter.getPrinterAt(position);
                    if (printer instanceof DiscoveredPrinterUsb) {
                        UsbManager usbManager = UsbHelper.getUsbManager(PrinterSelectionActivity.this);
                        UsbDevice device = ((DiscoveredPrinterUsb) printer).device;
                        if (!usbManager.hasPermission(device)) {
                            ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.requesting_usb_permission));
                            UsbHelper.requestUsbPermission(PrinterSelectionActivity.this, usbManager, device);
                            return;
                        }
                    }

                    SelectedPrinterManager.setSelectedPrinter(printer);
                    finish();
                }
            }
        });

        addPrinterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isApplicationBusy) {
                    DialogHelper.createManuallyConnectDialog(PrinterSelectionActivity.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ZebraEditText printerDnsIpAddressInput = ((AlertDialog) dialog).findViewById(R.id.printerDnsIpAddressInput);
                            if (printerDnsIpAddressInput != null) {
                                String ipAddress = printerDnsIpAddressInput.getText();

                                if (manualConnectionTask != null) {
                                    manualConnectionTask.cancel(true);
                                }

                                manualConnectionTask = new ManualConnectionTask(PrinterSelectionActivity.this, ipAddress);
                                manualConnectionTask.setOnManualConnectionListener(PrinterSelectionActivity.this);
                                manualConnectionTask.execute();
                            } else {
                                DialogHelper.showErrorDialog(PrinterSelectionActivity.this, getString(R.string.unable_to_find_ip_address_input));
                            }
                        }
                    }).show();
                }
            }
        });

        refreshPrintersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoveredPrinterListAdapter.clearPrinters();

                startRefreshAnimation();

                if (networkAndUsbDiscoveryTask != null) {
                    networkAndUsbDiscoveryTask.cancel(true);
                }

                networkAndUsbDiscoveryTask = new NetworkAndUsbDiscoveryTask(UsbHelper.getUsbManager(PrinterSelectionActivity.this));
                networkAndUsbDiscoveryTask.setOnPrinterDiscoveryListener(PrinterSelectionActivity.this);
                networkAndUsbDiscoveryTask.execute();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
    }

    @Override
    protected void onDestroy() {
        if (manualConnectionTask != null) {
            manualConnectionTask.cancel(true);
        }

        if (networkAndUsbDiscoveryTask != null) {
            networkAndUsbDiscoveryTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onManualConnectionStarted() {
        isApplicationBusy = true;
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.connecting_to_printer));
    }

    @Override
    public void onManualConnectionFinished(Exception exception, DiscoveredPrinter printer) {
        isApplicationBusy = false;
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_manually_connecting_message, exception.getMessage()));
        } else if (printer != null) {
            SelectedPrinterManager.setSelectedPrinter(printer);
            finish();
        }
    }

    @Override
    public void onPrinterDiscoveryStarted() {

    }

    @Override
    public void onPrinterDiscovered(final DiscoveredPrinter printer) {
        runOnUiThread(new Runnable() {
            public void run() {
                discoveredPrinterListAdapter.addPrinter(printer);
            }
        });
    }

    @Override
    public void onPrinterDiscoveryFinished(Exception exception) {
        finishRefreshAnimation();

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_discovering_printers_message, exception.getMessage()));
        }
    }

    private void startRefreshAnimation() {
        refreshPrintersButton.setEnabled(false);
        refreshPrintersButton.setAlpha(0.5f);
        refreshPrintersButton.startAnimation(rotation);
    }

    private void finishRefreshAnimation() {
        refreshPrintersButton.setEnabled(true);
        refreshPrintersButton.setAlpha(1.0f);
        refreshPrintersButton.clearAnimation();
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDisconnectReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(UsbHelper.ACTION_USB_PERMISSION_GRANTED);
        registerReceiver(usbPermissionReceiver, filter);
    }

    private void unregisterReceivers() {
        unregisterReceiver(usbDisconnectReceiver);
        unregisterReceiver(usbPermissionReceiver);
    }
}
