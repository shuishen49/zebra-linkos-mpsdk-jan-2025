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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zebra.android.devdemo.R;
import com.zebra.android.devdemo.util.UIHelper;
import com.zebra.sdk.btleComm.BluetoothLeConnection;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.FieldDescriptionData;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

public class VariablesScreen extends AppCompatActivity {

    private boolean bluetoothSelected;
    private boolean bleSelected;
    private String macAddress;
    private String tcpAddress;
    private String tcpPort;
    private String formatName;
    private List<FieldDescriptionData> variablesList = new ArrayList<FieldDescriptionData>();
    private List<EditText> variableValues = new ArrayList<EditText>();
    private UIHelper helper = new UIHelper(this);
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stored_format_variables);
        Bundle b = getIntent().getExtras();
        bluetoothSelected = b.getBoolean("bluetooth selected");
        bleSelected = b.getBoolean("ble selected");
        macAddress = b.getString("mac address");
        tcpAddress = b.getString("tcp address");
        tcpPort = b.getString("tcp port");
        formatName = b.getString("format name");

        TextView formatNameTextView = findViewById(R.id.formatName);
        formatNameTextView.setText(formatName);

        final Button printButton = findViewById(R.id.printFormatButton);

        printButton.setOnClickListener(v -> {
            printButton.setEnabled(false);
            new Thread(() -> {
                printFormat();
                runOnUiThread(() -> printButton.setEnabled(true));
            }).start();
        });

        new Thread(() -> {
            Looper.prepare();
            getVariables();
            Looper.loop();
            Looper.myLooper().quit();
        }).start();

    }

    protected void getVariables() {
        helper.showLoadingDialog("Retrieving variables...");

        connection = getPrinterConnection();

        if (connection != null) {
            try {
                connection.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

                byte[] formatContents = printer.retrieveFormatFromPrinter(formatName);
                FieldDescriptionData[] variables = printer.getVariableFields(new String(formatContents, "utf8"));

                for (int i = 0; i < variables.length; i++) {
                    variablesList.add(variables[i]);
                }
                connection.close();
                updateGuiWithFormats();
            } catch (ConnectionException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (ZebraPrinterLanguageUnknownException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            }
        }
        helper.dismissLoadingDialog(2000);
    }

    protected void printFormat() {
        helper.showLoadingDialog("Printing " + formatName + "...");
        connection = getPrinterConnection();
        if (connection != null) {
            try {
                connection.open();
                ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                Map<Integer, String> vars = new HashMap<Integer, String>();

                for (int i = 0; i < variablesList.size(); i++) {
                    FieldDescriptionData var = variablesList.get(i);
                    vars.put(var.fieldNumber, variableValues.get(i).getText().toString());
                }
                printer.printStoredFormat(formatName, vars, "utf8");
                connection.close();
            } catch (ConnectionException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (ZebraPrinterLanguageUnknownException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                helper.showErrorDialogOnGuiThread(e.getMessage());
            }
        }
        helper.dismissLoadingDialog(2000);
    }

    private Connection getPrinterConnection() {
        if (bluetoothSelected) {
            connection = new BluetoothConnection(macAddress);
        } else if (bleSelected) {
            connection = new BluetoothLeConnection(macAddress, this);
        } else {
            try {
                int port = Integer.parseInt(tcpPort);
                connection = new TcpConnection(tcpAddress, port);
            } catch (NumberFormatException e) {
                helper.showErrorDialogOnGuiThread("Port number is invalid");
                return null;
            }
        }
        return connection;
    }

    private void updateGuiWithFormats() {
        runOnUiThread(() -> {

            TableLayout varTable = findViewById(R.id.variablesTable);

            for (int i = 0; i < variablesList.size(); i++) {
                TableRow aRow = new TableRow(VariablesScreen.this);
                aRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView varName = new TextView(VariablesScreen.this);

                FieldDescriptionData var = variablesList.get(i);
                varName.setText(var.fieldName == null ? "Field " + var.fieldNumber : var.fieldName);
                varName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                aRow.addView(varName);

                EditText value = new EditText(VariablesScreen.this);
                value.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                value.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                variableValues.add(value);
                aRow.addView(value);

                varTable.addView(aRow, new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            }

        });
    }
}
