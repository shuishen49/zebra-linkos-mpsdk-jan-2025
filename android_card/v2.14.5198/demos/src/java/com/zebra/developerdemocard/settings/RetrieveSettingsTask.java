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

package com.zebra.developerdemocard.settings;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zebra.developerdemocard.settings.SettingsGroup.DEVICE;

public class RetrieveSettingsTask extends AsyncTask<Void, Void, Map<SettingsGroup, List<PrinterSetting>>> {
    private DiscoveredPrinter printer;
    private OnRetrieveSettingsListener onRetrieveSettingsListener;
    private Exception exception;

    public interface OnRetrieveSettingsListener {
        void onRetrieveSettingsStarted();
        void onRetrieveSettingsFinished(Exception exception, Map<SettingsGroup, List<PrinterSetting>> printerSettings);
    }

    RetrieveSettingsTask(DiscoveredPrinter printer) {
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onRetrieveSettingsListener != null) {
            onRetrieveSettingsListener.onRetrieveSettingsStarted();
        }
    }

    @Override
    @NonNull
    protected Map<SettingsGroup, List<PrinterSetting>> doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        Map<SettingsGroup, List<PrinterSetting>> settingsMap = new HashMap<>();

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            settingsMap.put(SettingsGroup.DEVICE, buildSettingsList(SettingsGroup.DEVICE, zebraCardPrinter.getAllSettingValues()));
            settingsMap.put(SettingsGroup.PRINT, buildSettingsList(SettingsGroup.PRINT, zebraCardPrinter.getAllJobSettingValues()));
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return settingsMap;
    }

    @Override
    protected void onPostExecute(@NonNull Map<SettingsGroup, List<PrinterSetting>> printerSettings) {
        super.onPostExecute(printerSettings);

        if (onRetrieveSettingsListener != null) {
            onRetrieveSettingsListener.onRetrieveSettingsFinished(exception, printerSettings);
        }
    }

    private List<PrinterSetting> buildSettingsList(SettingsGroup settingsGroup, Map<String, String> settingsMap) {
        boolean isSettable = settingsGroup == DEVICE;
        List<PrinterSetting> result = new ArrayList<>();

        for (String key : settingsMap.keySet()) {
            if (isCancelled()) {
                break;
            }

            result.add(new PrinterSetting(key, settingsMap.get(key), isSettable));
        }

        return result;
    }

    void setOnRetrieveSettingsListener(OnRetrieveSettingsListener onRetrieveSettingsListener) {
        this.onRetrieveSettingsListener = onRetrieveSettingsListener;
    }
}
