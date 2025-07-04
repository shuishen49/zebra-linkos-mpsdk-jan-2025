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

package com.zebra.developerdemocard.multijob;

import android.os.AsyncTask;

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

public class CancelJobsTask extends AsyncTask<Void, Void, Void> {

    private DiscoveredPrinter printer;
    private Integer jobId;
    private OnCancelJobsListener onCancelJobsListener;
    private Exception exception;

    public interface OnCancelJobsListener {
        void onCancelJobsStarted(Integer jobId);
        void onCancelJobsFinished(Exception exception, Integer jobId);
    }

    CancelJobsTask(DiscoveredPrinter printer) {
        this.printer = printer;
    }

    CancelJobsTask(DiscoveredPrinter printer, Integer jobId) {
        this.printer = printer;
        this.jobId = jobId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onCancelJobsListener != null) {
            onCancelJobsListener.onCancelJobsStarted(jobId);
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

            zebraCardPrinter.cancel(jobId != null ? jobId : 0); // 0 cancels all jobs
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

        if (onCancelJobsListener != null) {
            onCancelJobsListener.onCancelJobsFinished(exception, jobId);
        }
    }

    void setOnCancelJobsListener(OnCancelJobsListener onCancelJobsListener) {
        this.onCancelJobsListener = onCancelJobsListener;
    }
}
