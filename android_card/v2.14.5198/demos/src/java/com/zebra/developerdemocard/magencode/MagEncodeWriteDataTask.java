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

package com.zebra.developerdemocard.magencode;

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.PrinterHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class MagEncodeWriteDataTask extends AsyncTask<Void, Void, Integer> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private MagEncodeOptions magEncodeOptions;
    private OnMagEncodeWriteDataListener onMagEncodeWriteDataListener;
    private Exception exception;

    public interface OnMagEncodeWriteDataListener extends PrinterHelper.OnPrinterReadyListener {
        void onMagEncodeWriteDataStarted();
        void onMagEncodeWriteDataFinished(Exception exception, Integer jobId, CardSource cardSource);
    }

    MagEncodeWriteDataTask(Context context, DiscoveredPrinter printer, MagEncodeOptions magEncodeOptions) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.magEncodeOptions = magEncodeOptions;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onMagEncodeWriteDataListener != null) {
            onMagEncodeWriteDataListener.onMagEncodeWriteDataStarted();
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        Integer jobId = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            if (PrinterHelper.isPrinterReady(weakContext.get(), zebraCardPrinter, onMagEncodeWriteDataListener)) {
                Map<String, String> jobSettings = new HashMap<>();
                jobSettings.put(ZebraCardJobSettingNames.CARD_SOURCE, magEncodeOptions.getCardSource().name());
                jobSettings.put(ZebraCardJobSettingNames.CARD_DESTINATION, magEncodeOptions.getCardDestination().name());
                jobSettings.put(ZebraCardJobSettingNames.MAG_COERCIVITY, magEncodeOptions.getCoercivityType().name());
                jobSettings.put(ZebraCardJobSettingNames.MAG_VERIFY, magEncodeOptions.getVerifyEncoding() ? "yes" : "no");

                zebraCardPrinter.setJobSettings(jobSettings);
                jobId = zebraCardPrinter.magEncode(1, magEncodeOptions.getTrack1Data(), magEncodeOptions.getTrack2Data(), magEncodeOptions.getTrack3Data());
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return jobId;
    }

    @Override
    protected void onPostExecute(Integer jobId) {
        super.onPostExecute(jobId);

        if (onMagEncodeWriteDataListener != null) {
            onMagEncodeWriteDataListener.onMagEncodeWriteDataFinished(exception, jobId, magEncodeOptions.getCardSource());
        }
    }

    void setOnMagEncodeWriteDataListener(OnMagEncodeWriteDataListener onMagEncodeWriteDataListener) {
        this.onMagEncodeWriteDataListener = onMagEncodeWriteDataListener;
    }
}
