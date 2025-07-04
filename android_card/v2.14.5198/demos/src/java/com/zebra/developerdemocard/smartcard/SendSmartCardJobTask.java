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

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.PrinterHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;

public class SendSmartCardJobTask extends AsyncTask<Void, Void, Integer> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private SmartCardOptions smartCardOptions;
    private OnSendSmartCardJobListener onSendSmartCardJobListener;
    private Exception exception;

    public interface OnSendSmartCardJobListener extends PrinterHelper.OnPrinterReadyListener {
        void onSendSmartCardJobStarted();
        void onSendSmartCardJobAtmCardRequired();
        void onSendSmartCardJobFinished(Exception exception, Integer jobId, CardSource cardSource);
    }

    SendSmartCardJobTask(Context context, DiscoveredPrinter printer, SmartCardOptions smartCardOptions) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.smartCardOptions = smartCardOptions;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onSendSmartCardJobListener != null) {
            onSendSmartCardJobListener.onSendSmartCardJobStarted();
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

            if (PrinterHelper.isPrinterReady(weakContext.get(), zebraCardPrinter, onSendSmartCardJobListener)) {
                zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.CARD_SOURCE, smartCardOptions.getCardSource().name());
                zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.CARD_DESTINATION, smartCardOptions.getCardDestination().toString());

                String cardType = smartCardOptions.getCardType();
                boolean isEncoderTypeContact = cardType.equalsIgnoreCase("contact") || cardType.equalsIgnoreCase("contact_station");
                String settingName = isEncoderTypeContact ? ZebraCardJobSettingNames.SMART_CARD_CONTACT : ZebraCardJobSettingNames.SMART_CARD_CONTACTLESS;
                String settingValue = isEncoderTypeContact ? "yes" : cardType;

                zebraCardPrinter.setJobSetting(settingName, settingValue);
                jobId = zebraCardPrinter.smartCardEncode(1);

                if (smartCardOptions.getCardSource() == CardSource.ATM) {
                    if (onSendSmartCardJobListener != null) {
                        onSendSmartCardJobListener.onSendSmartCardJobAtmCardRequired();
                    }
                }
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

        if (onSendSmartCardJobListener != null) {
            onSendSmartCardJobListener.onSendSmartCardJobFinished(exception, jobId, smartCardOptions.getCardSource());
        }
    }

    void setOnSendSmartCardJobListener(OnSendSmartCardJobListener onSendSmartCardJobListener) {
        this.onSendSmartCardJobListener = onSendSmartCardJobListener;
    }
}
