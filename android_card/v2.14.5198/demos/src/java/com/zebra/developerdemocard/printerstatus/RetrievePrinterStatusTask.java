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

package com.zebra.developerdemocard.printerstatus;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.LinkedHashMap;
import java.util.Map;

public class RetrievePrinterStatusTask extends AsyncTask<Void, Void, PrinterStatus> {

    private DiscoveredPrinter printer;
    private OnRetrievePrinterStatusListener onRetrievePrinterStatusListener;
    private Exception exception;

    public interface OnRetrievePrinterStatusListener {
        void onRetrievePrinterStatusStarted();
        void onRetrievePrinterStatusFinished(Exception exception, @Nullable PrinterStatus printerStatus);
    }

    RetrievePrinterStatusTask(DiscoveredPrinter printer) {
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onRetrievePrinterStatusListener != null) {
            onRetrievePrinterStatusListener.onRetrievePrinterStatusStarted();
        }
    }

    @Override
    @Nullable
    protected PrinterStatus doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        PrinterStatus printerStatus = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            Map<String, String> sensorInfo = new LinkedHashMap<>();
            sensorInfo.putAll(zebraCardPrinter.getSensorStates());
            sensorInfo.putAll(zebraCardPrinter.getSensorValues());

            printerStatus = new PrinterStatus(zebraCardPrinter.getPrinterInformation(), zebraCardPrinter.getPrinterStatus(), zebraCardPrinter.getMediaInformation(), sensorInfo);
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return printerStatus;
    }

    @Override
    protected void onPostExecute(@Nullable PrinterStatus printerStatus) {
        super.onPostExecute(printerStatus);

        if (onRetrievePrinterStatusListener != null) {
            onRetrievePrinterStatusListener.onRetrievePrinterStatusFinished(exception, printerStatus);
        }
    }

    void setOnRetrievePrinterStatusListener(OnRetrievePrinterStatusListener onRetrievePrinterStatusListener) {
        this.onRetrievePrinterStatusListener = onRetrievePrinterStatusListener;
    }
}
