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

package com.zebra.developerdemocard.smartcard;

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RetrieveSmartCardSettingsRangesTask extends AsyncTask<Void, Void, Map<String, List<String>>> {

    static final String KEY_CARD_SOURCE = "KEY_CARD_SOURCE";
    static final String KEY_CARD_DESTINATION = "KEY_CARD_DESTINATION";
    static final String KEY_ENCODER_TYPE = "KEY_ENCODER_TYPE";

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private OnRetrieveSmartCardSettingsRangesListener onRetrieveSmartCardSettingsRangesListener;
    private Exception exception;

    public interface OnRetrieveSmartCardSettingsRangesListener {
        void onRetrieveSmartCardSettingsRangesStarted();
        void onRetrieveSmartCardSettingsRangesFinished(Exception exception, Map<String, List<String>> smartCardSettingsMap);
    }

    RetrieveSmartCardSettingsRangesTask(Context context, DiscoveredPrinter printer) {
        weakContext = new WeakReference<>(context);
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onRetrieveSmartCardSettingsRangesListener != null) {
            onRetrieveSmartCardSettingsRangesListener.onRetrieveSmartCardSettingsRangesStarted();
        }
    }

    @Override
    protected Map<String, List<String>> doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        Map<String, List<String>> smartCardSettingsMap = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);
            boolean hasLaminator = zebraCardPrinter.hasLaminator();

            if (zebraCardPrinter.hasSmartCardEncoder()) {
                smartCardSettingsMap = new HashMap<>();
                List<String> sourceList = new ArrayList<>();
                List<String> destinationList = new ArrayList<>();
                List<String> cardTypeList = new ArrayList<>();

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

                Map<String, String> smartCardConfigurations = zebraCardPrinter.getSmartCardConfigurations();
                cardTypeList.addAll(smartCardConfigurations.keySet());

                smartCardSettingsMap.put(KEY_CARD_SOURCE, sourceList);
                smartCardSettingsMap.put(KEY_CARD_DESTINATION, destinationList);
                smartCardSettingsMap.put(KEY_ENCODER_TYPE, cardTypeList);
            } else {
                throw new Exception(weakContext.get().getString(R.string.no_smart_card_encoder_found_error));
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return smartCardSettingsMap;
    }

    @Override
    protected void onPostExecute(Map<String, List<String>> magEncodeSettingsMap) {
        super.onPostExecute(magEncodeSettingsMap);

        if (onRetrieveSmartCardSettingsRangesListener != null) {
            onRetrieveSmartCardSettingsRangesListener.onRetrieveSmartCardSettingsRangesFinished(exception, magEncodeSettingsMap);
        }
    }

    void setOnRetrieveSmartCardSettingsRangesListener(OnRetrieveSmartCardSettingsRangesListener onRetrieveSmartCardSettingsRangesListener) {
        this.onRetrieveSmartCardSettingsRangesListener = onRetrieveSmartCardSettingsRangesListener;
    }
}
