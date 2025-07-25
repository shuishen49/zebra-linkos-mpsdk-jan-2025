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

package com.zebra.android.devdemo.storedformat;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.SettingsHelper;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.btleComm.BluetoothLeConnection;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class StoredFormatScreen extends ListActivity {

    private boolean bluetoothSelected;
    private boolean bleSelected;
    private String macAddress;
    private String tcpAddress;
    private String tcpPort;
    private ArrayAdapter<String> statusListAdapter;
    private List<String> formatsList = new ArrayList<String>();
    private UIHelper helper = new UIHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stored_format_demo);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        bleSelected = b.getBoolean("ble selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpPort = b.getString("tcp port");
        statusListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, formatsList);

        setListAdapter(statusListAdapter);
        new Thread(() -> {
            Looper.prepare();
            getFileList();
            Looper.loop();
            Looper.myLooper().quit();
        }).start();

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent;
        intent = new Intent(this, VariablesScreen.class);
        intent.putExtra("bluetooth selected", bluetoothSelected);
        intent.putExtra("ble selected", bleSelected);
        intent.putExtra("mac address", macAddress);
        intent.putExtra("tcp address", tcpAddress);
        intent.putExtra("tcp port", tcpPort);
        intent.putExtra("format name", (String) l.getItemAtPosition(position));

        startActivity(intent);
    }

    private void getFileList() {
        helper.showLoadingDialog("Retrieving Formats...");
        Connection connection;
        if (bluetoothSelected) {
            connection = new BluetoothConnection(macAddress);
        } else if (bleSelected) {
            connection = new BluetoothLeConnection(macAddress, this);
        } else {
            try {
                int port = Integer.parseInt(tcpPort);
                connection = new TcpConnection(tcpAddress, port);
            } catch (NumberFormatException e) {
                helper.dismissLoadingDialog();
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return;
            }
        }
        try {
            connection.open();
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
            PrinterLanguage pl = printer.getPrinterControlLanguage();
            String[] formatExtensions;

            if (pl == PrinterLanguage.ZPL) {
                formatExtensions = new String[] { "ZPL" };
            } else {
                formatExtensions = new String[] { "FMT", "LBL" };
            }

            String[] formats = printer.retrieveFileNames(formatExtensions);
            for (int i = 0; i < formats.length; i++) {
                formatsList.add(formats[i]);
            }
            connection.close();
            saveSettings();
            updateGuiWithFormats();
        } catch (ConnectionException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraPrinterLanguageUnknownException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } catch (ZebraIllegalArgumentException e) {
            helper.showErrorDialogOnGuiThread(e.getMessage());
        } finally {
            helper.dismissLoadingDialog(2000);
        }
    }

    private void saveSettings() {
        SettingsHelper.saveBluetoothAddress(StoredFormatScreen.this, macAddress);
        SettingsHelper.saveIp(StoredFormatScreen.this, tcpAddress);
        SettingsHelper.savePort(StoredFormatScreen.this, tcpPort);
    }

    private void updateGuiWithFormats() {
        runOnUiThread(new Runnable() {
            public void run() {
                statusListAdapter.notifyDataSetChanged();
                Toast.makeText(StoredFormatScreen.this, "Found " + formatsList.size() + " Formats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
