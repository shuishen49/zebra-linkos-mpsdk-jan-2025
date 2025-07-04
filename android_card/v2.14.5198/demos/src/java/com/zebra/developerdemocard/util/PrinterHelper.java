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

package com.zebra.developerdemocard.util;

import android.content.Context;

import com.zebra.developerdemocard.R;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.common.card.containers.PrinterStatusInfo;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.settings.SettingsException;

public class PrinterHelper {

    public interface OnPrinterReadyListener {
        void onPrinterReadyUpdate(String message, boolean showDialog);
    }

    public static boolean isPrinterReady(Context context, ZebraCardPrinter zebraCardPrinter, OnPrinterReadyListener onPrinterReadyListener) throws ConnectionException, SettingsException, ZebraCardException {
        PrinterStatusInfo statusInfo = zebraCardPrinter.getPrinterStatus();

        if (onPrinterReadyListener != null) {
            onPrinterReadyListener.onPrinterReadyUpdate(context.getString(R.string.checking_printer_status), false);
        }

        if (statusInfo.errorInfo.value > 0) {
            if (onPrinterReadyListener != null) {
                onPrinterReadyListener.onPrinterReadyUpdate(context.getString(R.string.printer_not_ready_message, statusInfo.status, statusInfo.errorInfo.description), true);
            }
            return false;
        } else if (statusInfo.alarmInfo.value > 0) {
            if (onPrinterReadyListener != null) {
                onPrinterReadyListener.onPrinterReadyUpdate(context.getString(R.string.printer_not_ready_message, statusInfo.status, statusInfo.alarmInfo.description), true);
            }
            return false;
        }

        return true;
    }
}
