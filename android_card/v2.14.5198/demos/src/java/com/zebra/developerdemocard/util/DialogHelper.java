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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.zebra.developerdemocard.R;

import java.util.List;

import static com.zebra.developerdemocard.graphicconversion.ProcessImageTask.DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_FILE;

public class DialogHelper {

    public static void showAlarmEncounteredDialog(final Activity activity,
                                                  final String title,
                                                  final String message,
                                                  final String positiveButtonText,
                                                  final String negativeButtonText,
                                                  final DialogInterface.OnClickListener onPositiveButtonClickListener,
                                                  final DialogInterface.OnClickListener onNegativeButtonClickListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(positiveButtonText, onPositiveButtonClickListener)
                        .setNegativeButton(negativeButtonText, onNegativeButtonClickListener)
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }

    public static void showStorageErrorDialog(final Activity activity, final DialogInterface.OnClickListener onPositiveButtonClickListener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(R.string.storage_error)
                        .setMessage(activity.getString(R.string.storage_error_message))
                        .setPositiveButton(android.R.string.ok, onPositiveButtonClickListener)
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }

    public static void showConvertedGraphicFileInfoDialog(final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(R.string.note)
                        .setMessage(activity.getString(R.string.converted_graphic_file_location_message, DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_FILE.getAbsolutePath()))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }

    public static AlertDialog createInsertCardDialog(Context context) {
        return new AlertDialog.Builder(context).setTitle(R.string.insert_card)
                .setMessage(context.getString(R.string.insert_card_into_atm_slot_message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
    }

    public static AlertDialog createManuallyConnectDialog(Context context, DialogInterface.OnClickListener onPositiveButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(R.string.dialog_title_manually_connect)
                .setView(R.layout.dialog_manually_connect)
                .setPositiveButton(R.string.connect, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public static AlertDialog createDisconnectDialog(Context context, DialogInterface.OnClickListener onPositiveButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(R.string.dialog_title_disconnect_printer)
                .setMessage(R.string.dialog_message_disconnect_printer)
                .setPositiveButton(R.string.disconnect, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public static AlertDialog.Builder createContinuePrinterResetDialog(Context context, List<String> resetRequiredSettings, boolean resetPrinter, DialogInterface.OnClickListener onPositiveButtonClickListener, DialogInterface.OnClickListener onNegativeButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(resetPrinter ? R.string.printer_reset_required : R.string.network_reset_required)
                .setMessage(context.getString(resetPrinter ? R.string.continue_printer_reset_message : R.string.continue_network_reset_message, TextUtils.join(context.getString(R.string.list_delimiter), resetRequiredSettings)))
                .setPositiveButton(android.R.string.ok, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, onNegativeButtonClickListener);
    }

    public static AlertDialog.Builder createPrinterResetDialog(Context context, List<String> resetRequiredSettings, boolean resetPrinter, DialogInterface.OnClickListener onPositiveButtonClickListener) {
        return new AlertDialog.Builder(context).setTitle(resetPrinter ? R.string.printer_reset_required : R.string.network_reset_required)
                .setMessage(context.getString(resetPrinter ? R.string.printer_reset_required_message : R.string.network_reset_required_message, TextUtils.join(context.getString(R.string.list_delimiter), resetRequiredSettings), context.getString(R.string.reset)))
                .setPositiveButton(R.string.reset, onPositiveButtonClickListener)
                .setCancelable(false);
    }

    public static void showErrorDialog(Activity activity, String message) {
        showErrorDialog(activity, activity.getString(R.string.error), message);
    }

    public static void showErrorDialog(final Activity activity, final String title, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                AlertDialog dialog = builder.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create();
                dialog.show();
            }
        });
    }
}
