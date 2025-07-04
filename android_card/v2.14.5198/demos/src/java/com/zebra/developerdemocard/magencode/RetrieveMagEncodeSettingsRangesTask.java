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
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrieveMagEncodeSettingsRangesTask extends AsyncTask<Void, Void, Map<String, List<String>>> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private OnRetrieveMagEncodeSettingsRangesListener onRetrieveMagEncodeSettingsRangesListener;
    private Exception exception;

    public interface OnRetrieveMagEncodeSettingsRangesListener {
        void onRetrieveMagEncodeSettingsRangesStarted();
        void onRetrieveMagEncodeSettingsRangesFinished(Exception exception, Map<String, List<String>> magEncodeSettingsMap);
    }

    RetrieveMagEncodeSettingsRangesTask(Context context, DiscoveredPrinter printer) {
        weakContext = new WeakReference<>(context);
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onRetrieveMagEncodeSettingsRangesListener != null) {
            onRetrieveMagEncodeSettingsRangesListener.onRetrieveMagEncodeSettingsRangesStarted();
        }
    }

    @Override
    protected Map<String, List<String>> doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        Map<String, List<String>> magEncodeSettingsMap = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            boolean hasMagEncoder = zebraCardPrinter.hasMagneticEncoder();
            boolean hasLaminator = zebraCardPrinter.hasLaminator();

            if (hasMagEncoder) {
                magEncodeSettingsMap = new HashMap<>();

                List<String> sourceList = new ArrayList<>();
                List<String> destinationList = new ArrayList<>();
                List<String> coercivityTypeList = new ArrayList<>();

                String cardSourceRange = zebraCardPrinter.getJobSettingRange(ZebraCardJobSettingNames.CARD_SOURCE);
                for (CardSource source : CardSource.values()) {
                    if (cardSourceRange != null && cardSourceRange.contains(source.name())) {
                        sourceList.add(source.name());
                    }
                }

                String cardDestinationRange = zebraCardPrinter.getJobSettingRange(ZebraCardJobSettingNames.CARD_DESTINATION);
                for (CardDestination destination : CardDestination.values()) {
                    if (cardDestinationRange != null && cardDestinationRange.contains(destination.name())) {
                        if (!destination.name().contains("Laminator") || hasLaminator) {
                            destinationList.add(destination.name());
                        }
                    }
                }

                String coercivityTypeRange = zebraCardPrinter.getJobSettingRange(ZebraCardJobSettingNames.MAG_COERCIVITY);
                for (CoercivityType coercivity : CoercivityType.values()) {
                    if (coercivityTypeRange != null && coercivityTypeRange.contains(coercivity.name())) {
                        coercivityTypeList.add(coercivity.name());
                    }
                }

                magEncodeSettingsMap.put(ZebraCardJobSettingNames.CARD_SOURCE, sourceList);
                magEncodeSettingsMap.put(ZebraCardJobSettingNames.CARD_DESTINATION, destinationList);
                magEncodeSettingsMap.put(ZebraCardJobSettingNames.MAG_COERCIVITY, coercivityTypeList);
            } else {
                throw new Exception(weakContext.get().getString(R.string.no_magnetic_encoder_found_error));
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return magEncodeSettingsMap;
    }

    @Override
    protected void onPostExecute(Map<String, List<String>> magEncodeSettingsMap) {
        super.onPostExecute(magEncodeSettingsMap);

        if (onRetrieveMagEncodeSettingsRangesListener != null) {
            onRetrieveMagEncodeSettingsRangesListener.onRetrieveMagEncodeSettingsRangesFinished(exception, magEncodeSettingsMap);
        }
    }

    void setOnRetrieveMagEncodeSettingsRangesListener(OnRetrieveMagEncodeSettingsRangesListener onRetrieveMagEncodeSettingsRangesListener) {
        this.onRetrieveMagEncodeSettingsRangesListener = onRetrieveMagEncodeSettingsRangesListener;
    }
}
