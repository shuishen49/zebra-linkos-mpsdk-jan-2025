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

package com.zebra.developerdemocard.template;

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.PrinterHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.containers.TemplateJob;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.Map;

public class SendTemplateJobTask extends AsyncTask<Void, Void, Integer> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private ZebraCardTemplate zebraCardTemplate;
    private String templateName;
    private Map<String, String> variableData;
    private OnSendTemplateJobListener onSendTemplateJobListener;
    private Exception exception;
    private CardSource cardSource;

    public interface OnSendTemplateJobListener extends PrinterHelper.OnPrinterReadyListener {
        void onSendTemplateJobStarted();
        void onSendTemplateJobFinished(Exception exception, Integer jobId, CardSource cardSource);
    }

    SendTemplateJobTask(Context context, DiscoveredPrinter printer, ZebraCardTemplate zebraCardTemplate, String templateName, Map<String, String> variableData) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.zebraCardTemplate = zebraCardTemplate;
        this.templateName = templateName;
        this.variableData = variableData;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onSendTemplateJobListener != null) {
            onSendTemplateJobListener.onSendTemplateJobStarted();
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        ZebraCardPrinter zebraCardPrinter = null;
        Connection connection = null;
        Integer jobId = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            if (PrinterHelper.isPrinterReady(weakContext.get(), zebraCardPrinter, onSendTemplateJobListener)) {
                TemplateJob templateJob = zebraCardTemplate.generateTemplateJob(templateName, variableData);
                if (templateJob.jobInfo.cardDestination != null) {
                    if (templateJob.jobInfo.cardDestination == CardDestination.Eject && zebraCardPrinter.hasLaminator()) {
                        templateJob.jobInfo.cardDestination = CardDestination.LaminatorAny;
                    }
                }

                cardSource = templateJob.jobInfo.cardSource;

                if (templateJob.magInfo != null && templateJob.magData != null) {
                    boolean hasTrack1Data = templateJob.magData.track1Data != null && !templateJob.magData.track1Data.isEmpty();
                    boolean hasTrack2Data = templateJob.magData.track2Data != null && !templateJob.magData.track2Data.isEmpty();
                    boolean hasTrack3Data = templateJob.magData.track3Data != null && !templateJob.magData.track3Data.isEmpty();
                    if (templateJob.magInfo.verify && !hasTrack1Data && !hasTrack2Data && !hasTrack3Data) {
                        throw new IllegalArgumentException(weakContext.get().getString(R.string.no_magnetic_track_data_to_encode));
                    }
                }

                jobId = zebraCardPrinter.printTemplate(1, templateJob);
            }
        } catch (ZebraCardException e) {
            exception = new ZebraCardException(weakContext.get().getString(R.string.invalid_template_file_selected), e);
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

        if (onSendTemplateJobListener != null) {
            onSendTemplateJobListener.onSendTemplateJobFinished(exception, jobId, cardSource);
        }
    }

    void setOnSendTemplateJobListener(OnSendTemplateJobListener onSendTemplateJobListener) {
        this.onSendTemplateJobListener = onSendTemplateJobListener;
    }
}
