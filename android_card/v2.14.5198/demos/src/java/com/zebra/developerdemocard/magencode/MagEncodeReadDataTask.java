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

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.PrinterHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.containers.MagTrackData;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.DataSource;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class MagEncodeReadDataTask extends AsyncTask<Void, Void, MagTrackData> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private CardSource cardSource;
    private CardDestination cardDestination;
    private OnMagEncodeReadDataListener onMagEncodeReadDataListener;
    private Exception exception;

    public interface OnMagEncodeReadDataListener extends PrinterHelper.OnPrinterReadyListener {
        void onMagEncodeReadDataStarted();
        void onMagEncodeReadDataAtmCardRequired();
        void onMagEncodeReadDataFinished(Exception exception, MagTrackData magTrackData);
    }

    MagEncodeReadDataTask(Context context, DiscoveredPrinter printer, CardSource cardSource, CardDestination cardDestination) {
        weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.cardSource = cardSource;
        this.cardDestination = cardDestination;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onMagEncodeReadDataListener != null) {
            onMagEncodeReadDataListener.onMagEncodeReadDataStarted();
        }
    }

    @Override
    protected MagTrackData doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        MagTrackData magTrackData = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            if (PrinterHelper.isPrinterReady(weakContext.get(), zebraCardPrinter, onMagEncodeReadDataListener)) {
                Map<String, String> jobSettings = new HashMap<>();
                jobSettings.put(ZebraCardJobSettingNames.CARD_SOURCE, cardSource.name());
                jobSettings.put(ZebraCardJobSettingNames.CARD_DESTINATION, cardDestination.name());

                zebraCardPrinter.setJobSettings(jobSettings);

                if (cardSource == CardSource.ATM) {
                    if (onMagEncodeReadDataListener != null) {
                        onMagEncodeReadDataListener.onMagEncodeReadDataAtmCardRequired();
                    }
                }

                EnumSet<DataSource> tracksToRead = EnumSet.noneOf(DataSource.class);
                tracksToRead.add(DataSource.Track1);
                tracksToRead.add(DataSource.Track2);
                tracksToRead.add(DataSource.Track3);

                magTrackData = zebraCardPrinter.readMagData(tracksToRead, true);

                if (magTrackData.track1Data.isEmpty() && magTrackData.track2Data.isEmpty() && magTrackData.track3Data.isEmpty()) {
                    System.out.println(weakContext.get().getString(R.string.no_data_read_from_card));
                }
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return magTrackData;
    }

    @Override
    protected void onPostExecute(MagTrackData magTrackData) {
        super.onPostExecute(magTrackData);

        if (onMagEncodeReadDataListener != null) {
            onMagEncodeReadDataListener.onMagEncodeReadDataFinished(exception, magTrackData);
        }
    }

    void setOnMagEncodeReadDataListener(OnMagEncodeReadDataListener onMagEncodeReadDataListener) {
        this.onMagEncodeReadDataListener = onMagEncodeReadDataListener;
    }
}
