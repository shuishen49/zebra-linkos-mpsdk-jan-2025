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

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.common.card.containers.PrinterStatusInfo;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraPrinterView;

import java.lang.ref.WeakReference;

public class PrinterStatusUpdateTask extends AsyncTask<Void, Void, ZebraPrinterView.PrinterStatus> {

    private DiscoveredPrinter printer;

    private WeakReference<Context> weakContext;
    private OnUpdatePrinterStatusListener onUpdatePrinterStatusListener;
    private Exception exception;

    public interface OnUpdatePrinterStatusListener {
        void onUpdatePrinterStatusStarted();
        void onUpdatePrinterStatusFinished(Exception exception, ZebraPrinterView.PrinterStatus printerStatus);
    }

    public PrinterStatusUpdateTask(Context context, DiscoveredPrinter printer) {
        weakContext = new WeakReference<>(context);
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onUpdatePrinterStatusListener != null) {
            onUpdatePrinterStatusListener.onUpdatePrinterStatusStarted();
        }
    }

    @Override
    protected ZebraPrinterView.PrinterStatus doInBackground(Void... params) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            PrinterStatusInfo printerStatus = zebraCardPrinter.getPrinterStatus();
            if (printerStatus != null) {
                if (printerStatus.errorInfo.value != 0 || printerStatus.alarmInfo.value != 0) {
                    return ZebraPrinterView.PrinterStatus.ERROR;
                } else {
                    return ZebraPrinterView.PrinterStatus.ONLINE;
                }
            } else {
                return ZebraPrinterView.PrinterStatus.ERROR;
            }
        } catch (ConnectionException e) {
            exception = new ConnectionException(weakContext.get().getString(R.string.unable_to_communicate_with_printer_message));
            return ZebraPrinterView.PrinterStatus.ERROR;
        } catch (Exception e) {
            exception = e;
            return ZebraPrinterView.PrinterStatus.ERROR;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }
    }

    @Override
    protected void onPostExecute(ZebraPrinterView.PrinterStatus printerStatus) {
        super.onPostExecute(printerStatus);

        if (onUpdatePrinterStatusListener != null) {
            onUpdatePrinterStatusListener.onUpdatePrinterStatusFinished(exception, printerStatus);
        }
    }

    public void setOnUpdatePrinterStatusListener(OnUpdatePrinterStatusListener onUpdatePrinterStatusListener) {
        this.onUpdatePrinterStatusListener = onUpdatePrinterStatusListener;
    }
}