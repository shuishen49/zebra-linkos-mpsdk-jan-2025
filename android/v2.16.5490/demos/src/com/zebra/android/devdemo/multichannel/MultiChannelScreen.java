/***********************************************
 * CONFIDENTIAL AND PROPRIETARY
 *
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 *
 * Copyright ZIH Corp. 2012
 *
 * ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.android.devdemo.multichannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.zebra.android.devdemo.ConnectionScreenPrimary;
import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.DemoSleeper;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.btleComm.MultichannelBluetoothLeConnection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.MultichannelBluetoothConnection;
import com.zebra.sdk.comm.MultichannelConnection;
import com.zebra.sdk.comm.MultichannelTcpConnection;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.printer.LinkOsInformation;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.settings.SettingsValues;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MultiChannelScreen extends AppCompatActivity {

    private boolean bluetoothSelected;
    private boolean bleSelected;
    private boolean hasPrintJobFinished;
    private boolean jobStarted = false;
    private String macAddress;
    private String tcpAddress;
    private String tcpPort;
    private String tcpStatusPort;
    private UIHelper helper = new UIHelper(this);
    private ArrayAdapter<String> multichannelListAdapter;
    private List<String> multichannelMessageList = new ArrayList<String>();
    public MultichannelConnection multichannelConnection;
    OnBackPressedDispatcher onBackPressedDispatcher = getOnBackPressedDispatcher();
    boolean backPressed = false;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.status_channel_activity);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        bleSelected = b.getBoolean("ble selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpPort = b.getString("tcp port");
        tcpStatusPort = b.getString("tcp status port");
        multichannelListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, multichannelMessageList);
        ListView multichannelList = findViewById(R.id.statusListView);
        multichannelList.setAdapter(multichannelListAdapter);

        new Thread(() -> {
            updateGui(null, null, 0, 0);
            saveSettings();
            runMultichannelDemo();
        }).start();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPressed = true;
                if(jobStarted){
                    showAlert();
                } else {
                    remove();
                    onBackPressedDispatcher.onBackPressed();
                }
            }
        });
    }

    private void showAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please wait, connection is not yet closed...")
                .setCancelable(false); // Prevents dismissing the dialog by clicking outside

        alert = builder.create();
        alert.show();
    }

    private void runMultichannelDemo() {
        AtomicReference<String> error = new AtomicReference<>();
        hasPrintJobFinished = false;
        jobStarted = true;
        try {

            if (bluetoothSelected) {
                // Printer only broadcasts the status connection if a valid raw connection is open
                multichannelConnection = new MultichannelBluetoothConnection(macAddress);
            } else if (bleSelected) {
                multichannelConnection = new MultichannelBluetoothLeConnection(macAddress, this);
            }else {
                // Connect using 9100 by default for printing port and 9200 default for status port.
                try {
                    multichannelConnection = new MultichannelTcpConnection(tcpAddress, Integer.parseInt(tcpPort), Integer.parseInt(tcpStatusPort));
                    setStoredString(ConnectionScreenPrimary.tcpPortKey, tcpPort);
                    setStoredString(ConnectionScreenPrimary.tcpStatusPortKey, tcpStatusPort);
                } catch (NumberFormatException e) {
                    helper.showErrorDialogOnGuiThread("Invalid port number");
                    hasPrintJobFinished = true;
                    return;
                }
            }

            multichannelConnection.open();

            new Thread(() -> {
                int statusQueryCount = 1;
                List<String> odometerSettings = Arrays.asList("odometer.total_label_count", "odometer.total_print_length");
                try {
                    ZebraPrinter printer = ZebraPrinterFactory.getLinkOsPrinter(multichannelConnection.getStatusChannel());
                    while (multichannelConnection.getStatusChannel().isConnected() && !hasPrintJobFinished) {

                        long startTime = System.currentTimeMillis();
                        LinkOsInformation linkOsVersion = new LinkOsInformation(SGD.GET("appl.link_os_version", multichannelConnection));
                        final Map<String, String> odometerValues = new SettingsValues().getValues(odometerSettings, multichannelConnection.getStatusChannel(), printer.getPrinterControlLanguage(), linkOsVersion);

                        final long totalTime = System.currentTimeMillis() - startTime;
                        final int count = statusQueryCount++;
                        if(bleSelected){
                            runOnUiThread(() -> updateGui(odometerValues, null, totalTime, count));
                        } else {
                            final PrinterStatus myPrinterStatus = printer.getCurrentStatus();
                            runOnUiThread(() -> updateGui(odometerValues, myPrinterStatus, totalTime, count));
                        }
                    }
                } catch (ZebraIllegalArgumentException e) {
                    e.printStackTrace();
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try {
                    // Send the "^XA" to open the channel and sleep to hold the channel open while querying the
                    // status.
                    // Sleep for 2 seconds after sending the end of the label to let the user see the print job
                    // finish while querying the status.
                    multichannelConnection.getPrintingChannel().write(beginningOfLabel.getBytes());
                    DemoSleeper.sleep(500);
                    multichannelConnection.getPrintingChannel().write(endOfLabel.getBytes());
                    DemoSleeper.sleep(2000);
                    hasPrintJobFinished = true;
                } catch (ConnectionException e) {
                    e.printStackTrace();
                    hasPrintJobFinished = true;
                }
            }).start();
        } catch (Exception e) {
            hasPrintJobFinished = true;
            if(!backPressed){
                helper.showErrorDialogOnGuiThread(e.getMessage());
            }
        } finally {
            if (error.get() != null) {
                if(!backPressed){
                    helper.showErrorDialogOnGuiThread(error.get());
                }
            } else{
                while (!hasPrintJobFinished) {
                    DemoSleeper.sleep(100);
                }
                try {
                    if (multichannelConnection != null) {
                        multichannelConnection.close();
                    }
                    // Simulate checking the print job status, dismiss the dialog when done
                } catch (ConnectionException e) {
                    if(!backPressed){
                        helper.showErrorDialogOnGuiThread(e.getMessage());
                    }
                }
                if(backPressed){
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Dismiss the dialog when the condition is met
                        alert.dismiss();
                        onBackPressedDispatcher.onBackPressed();
                    }, 3000);
                }
            }
            jobStarted = false;
        }
    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(MultiChannelScreen.this, macAddress);
        SettingsHelper.saveIp(MultiChannelScreen.this, tcpAddress);
        SettingsHelper.savePort(MultiChannelScreen.this, tcpPort);
        SettingsHelper.saveStatusPort(MultiChannelScreen.this, tcpStatusPort);
    }

    private void setStoredString(String key, String value) {
        SharedPreferences settings = getSharedPreferences(ConnectionScreenPrimary.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    protected void updateGui(final Map<String, String> odometerValues, final PrinterStatus myPrinterStatus, final long totalTime, final int count) {
        multichannelListAdapter.clear();
        multichannelMessageList.clear();
        multichannelMessageList.add("Total number of queries: " + count);
        multichannelMessageList.add("Total time to query settings: " + totalTime + "ms");
        if (null != odometerValues) {
            multichannelMessageList.add("Total label count: " + odometerValues.get("odometer.total_label_count"));
            multichannelMessageList.add("Total print length: " + odometerValues.get("odometer.total_print_length"));
        }
        if (null != myPrinterStatus) {
            multichannelMessageList.add("Is Printer Ready: " + myPrinterStatus.isReadyToPrint);
            multichannelMessageList.add("Is Head Open: " + myPrinterStatus.isHeadOpen);
            multichannelMessageList.add("Is Paper Out: " + myPrinterStatus.isPaperOut);
            multichannelMessageList.add("Is Printer Paused: " + myPrinterStatus.isPaused);
            multichannelMessageList.add("Batch Labels Remaining: " + myPrinterStatus.labelsRemainingInBatch);
        }
        multichannelListAdapter.notifyDataSetChanged();
    }

    private final static String beginningOfLabel = "^XA";
    private final static String endOfLabel = "^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ";

}
