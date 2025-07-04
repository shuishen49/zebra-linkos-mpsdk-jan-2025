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

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.settings.ZebraCardSettingNames;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.zmotif.settings.ZebraCardSettingNamesZmotif;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SaveSettingsTask extends AsyncTask<Void, Void, Void> {

    static final List<String> SGD_LIST_NETWORK_RESET_REQUIRED = Arrays.asList(
            ZebraCardSettingNamesZmotif.WIRED_SNMP,
            ZebraCardSettingNames.WIRED_DHCP,
            ZebraCardSettingNames.WIRED_ADDRESS,
            ZebraCardSettingNames.WIRED_SUBNET,
            ZebraCardSettingNames.WIRED_GATEWAY,
            ZebraCardSettingNamesZmotif.WIRED_DNS_NAME,
            ZebraCardSettingNamesZmotif.WIRELESS_SNMP,
            ZebraCardSettingNamesZmotif.WIRELESS_DHCP,
            ZebraCardSettingNamesZmotif.WIRELESS_ADDRESS,
            ZebraCardSettingNamesZmotif.WIRELESS_SUBNET,
            ZebraCardSettingNamesZmotif.WIRELESS_GATEWAY
    );

    static final List<String> SGD_LIST_PRINTER_RESET_REQUIRED = Arrays.asList(
            ZebraCardSettingNamesZmotif.OCP_LANGUAGE_TYPE,
            ZebraCardSettingNamesZmotif.OCP_LANGUAGE_NAME,
            ZebraCardSettingNamesZmotif.STANDBY_TIMEOUT
    );

    private static final List<String> SGD_LIST_DISABLE_DHCP_ETHERNET = Arrays.asList(
            ZebraCardSettingNamesZmotif.WIRED_ADDRESS,
            ZebraCardSettingNamesZmotif.WIRED_SUBNET,
            ZebraCardSettingNamesZmotif.WIRED_GATEWAY
    );

    private static final List<String> SGD_LIST_DISABLE_DHCP_WIRELESS = Arrays.asList(
            ZebraCardSettingNamesZmotif.WIRELESS_ADDRESS,
            ZebraCardSettingNamesZmotif.WIRELESS_SUBNET,
            ZebraCardSettingNamesZmotif.WIRELESS_GATEWAY
    );

    private DiscoveredPrinter printer;
    private Map<String, String> changedSettings;
    private OnSaveSettingsListener onSaveSettingsListener;
    private Exception exception;
    private List<String> resetRequiredSettings = new ArrayList<>();
    private boolean resetPrinter;

    public interface OnSaveSettingsListener {
        void onSaveSettingsStarted();
        void onSaveSettingsFinished(Exception exception, List<String> resetRequiredSettings, boolean resetPrinter);
    }

    SaveSettingsTask(DiscoveredPrinter printer, Map<String, String> changedSettings, boolean resetPrinter) {
        this.printer = printer;
        this.changedSettings = changedSettings;
        this.resetPrinter = resetPrinter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onSaveSettingsListener != null) {
            onSaveSettingsListener.onSaveSettingsStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            if (changedSettings != null) {
                boolean disableEthernetDhcp = false;
                boolean disableWirelessDhcp = false;

                for (String setting : changedSettings.keySet()) {
                    if (isCancelled()) {
                        break;
                    }

                    if (SGD_LIST_NETWORK_RESET_REQUIRED.contains(setting) || SGD_LIST_PRINTER_RESET_REQUIRED.contains(setting)) {
                        resetRequiredSettings.add(setting);
                    }

                    if (SGD_LIST_DISABLE_DHCP_ETHERNET.contains(setting)) {
                        disableEthernetDhcp = true;
                    }

                    if (SGD_LIST_DISABLE_DHCP_WIRELESS.contains(setting)) {
                        disableWirelessDhcp = true;
                    }
                }

                if (resetRequiredSettings.size() == 1) {
                    String setting = resetRequiredSettings.get(0);
                    if (setting.equals(ZebraCardSettingNames.WIRED_DHCP)) {
                        disableEthernetDhcp = false;
                    } else if (setting.equals(ZebraCardSettingNamesZmotif.WIRELESS_DHCP)) {
                        disableWirelessDhcp = false;
                    }
                }

                if (disableEthernetDhcp) {
                    zebraCardPrinter.setSetting(ZebraCardSettingNames.WIRED_DHCP, "disabled");
                }

                if (disableWirelessDhcp) {
                    zebraCardPrinter.setSetting(ZebraCardSettingNamesZmotif.WIRELESS_DHCP, "disabled");
                }

                zebraCardPrinter.setSettings(changedSettings);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onSaveSettingsListener != null) {
            onSaveSettingsListener.onSaveSettingsFinished(exception, resetRequiredSettings, resetPrinter);
        }
    }

    void setOnSaveSettingsListener(OnSaveSettingsListener onSaveSettingsListener) {
        this.onSaveSettingsListener = onSaveSettingsListener;
    }
}
