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

package com.zebra.developerdemocard.jobstatus;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.ThreadSleeper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PollJobStatusTask extends AsyncTask<Void, Void, Void> {

    private static final int CARD_FEED_TIMEOUT = 60 * 1000;

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer = null;
    private List<JobInfo> jobInfoList = new ArrayList<>();
    private OnJobStatusPollListener onJobStatusPollListener;
    private Exception exception;
    boolean showAtmDialog = true;
    private long startTime;
    private boolean cancelledByUser = false;

    public interface OnJobStatusPollListener {
        void onJobStatusUpdate(@Nullable JobInfo jobInfo, @Nullable JobStatusInfo jobStatusInfo, String message);
        void onJobStatusUserInputRequired(String title, String message, String positiveButtonText, String negativeButtonText, PollJobStatusTask.OnUserInputListener onUserInputListener);
        void onJobStatusAtmCardRequired();
        void onJobStatusPollFinished(Exception exception);
    }

    public interface OnUserInputListener {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

    public PollJobStatusTask(Context context, DiscoveredPrinter printer, JobInfo jobInfo) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        jobInfoList.add(jobInfo);
    }

    public PollJobStatusTask(Context context, DiscoveredPrinter printer, List<JobInfo> jobInfoList) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.jobInfoList = jobInfoList;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        boolean isFeeding = false;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            startTime = System.currentTimeMillis();

            for (JobInfo jobInfo : jobInfoList) {
                if (onJobStatusPollListener != null) {
                    onJobStatusPollListener.onJobStatusUpdate(jobInfo, null, weakContext.get().getString(R.string.polling_job_status, jobInfo.getJobId()));
                }
            }

            while (jobInfoList.size() > 0) {
                for (JobInfo jobInfo : new ArrayList<>(jobInfoList)) {
                    if (isCancelled()) {
                        break;
                    }

                    JobStatusInfo jobStatusInfo = zebraCardPrinter.getJobStatus(jobInfo.getJobId());

                    if (!isFeeding) {
                        startTime = System.currentTimeMillis();
                    }

                    boolean isAlarmInfoPresent = jobStatusInfo.alarmInfo.value > 0;
                    boolean isErrorInfoPresent = jobStatusInfo.errorInfo.value > 0;
                    isFeeding = jobStatusInfo.cardPosition.contains("feeding");

                    if (onJobStatusPollListener != null) {
                        String alarmInfo = isAlarmInfoPresent ? weakContext.get().getString(R.string.alarm_error_value_and_description, jobStatusInfo.alarmInfo.value, jobStatusInfo.alarmInfo.description) : Integer.toString(jobStatusInfo.alarmInfo.value);
                        String errorInfo = isErrorInfoPresent ? weakContext.get().getString(R.string.alarm_error_value_and_description, jobStatusInfo.errorInfo.value, jobStatusInfo.errorInfo.description) : Integer.toString(jobStatusInfo.errorInfo.value);
                        String jobStatusMessage = weakContext.get().getString(R.string.job_status_message,
                                jobInfo.getJobId(),
                                jobStatusInfo.printStatus,
                                jobStatusInfo.cardPosition,
                                jobStatusInfo.contactSmartCard,
                                jobStatusInfo.contactlessSmartCard,
                                alarmInfo,
                                errorInfo);
                        onJobStatusPollListener.onJobStatusUpdate(jobInfo, jobStatusInfo, jobStatusMessage);
                    }

                    if (jobStatusInfo.printStatus.equals("done_ok")) {
                        finishPolling(jobInfo, jobStatusInfo, weakContext.get().getString(R.string.job_id_completed, jobInfo.getJobId()));
                    } else if (jobStatusInfo.printStatus.equals("done_error")) {
                        finishPolling(jobInfo, jobStatusInfo, weakContext.get().getString(R.string.job_id_completed_with_error_message, jobInfo.getJobId(), jobStatusInfo.errorInfo.description));
                    } else if (jobStatusInfo.printStatus.contains("cancelled")) {
                        String message = isErrorInfoPresent ? weakContext.get().getString(R.string.job_id_cancelled_with_error_message, jobInfo.getJobId(), jobStatusInfo.errorInfo.description) : weakContext.get().getString(R.string.job_id_cancelled, jobInfo.getJobId());
                        finishPolling(jobInfo, jobStatusInfo, message);
                    } else if (isAlarmInfoPresent) {
                        if (onJobStatusPollListener != null) {
                            final CountDownLatch alarmEncounteredLatch = new CountDownLatch(1);
                            showAlarmEncounteredDialog(alarmEncounteredLatch, jobInfo, jobStatusInfo);
                            alarmEncounteredLatch.await();
                        }

                        if (cancelledByUser) {
                            cancelledByUser = false;
                            zebraCardPrinter.cancel(jobInfo.getJobId());
                        }
                    } else if (isErrorInfoPresent) {
                        zebraCardPrinter.cancel(jobInfo.getJobId());
                    } else if (jobStatusInfo.contactSmartCard.contains("at_station") || jobStatusInfo.contactlessSmartCard.contains("at_station")) {
                        if (onJobStatusPollListener != null) {
                            final CountDownLatch smartCardAtStationLatch = new CountDownLatch(1);
                            showCardAtStationDialog(smartCardAtStationLatch);
                            smartCardAtStationLatch.await();
                        }

                        if (cancelledByUser) {
                            cancelledByUser = false;
                            zebraCardPrinter.cancel(jobInfo.getJobId());
                        } else {
                            zebraCardPrinter.resume();
                        }
                    } else if (isFeeding) {
                        if (showAtmDialog && jobInfo.getCardSource() == CardSource.ATM) {
                            if (onJobStatusPollListener != null) {
                                onJobStatusPollListener.onJobStatusAtmCardRequired();
                            }

                            showAtmDialog = false;
                        } else if (System.currentTimeMillis() > startTime + CARD_FEED_TIMEOUT) {
                            if (onJobStatusPollListener != null) {
                                onJobStatusPollListener.onJobStatusUpdate(jobInfo, jobStatusInfo, weakContext.get().getString(R.string.job_id_timed_out_message, jobInfo.getJobId()));
                            }
                            zebraCardPrinter.cancel(jobInfo.getJobId());
                        }
                    }

                    ThreadSleeper.sleep(500);
                }
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

        if (onJobStatusPollListener != null) {
            onJobStatusPollListener.onJobStatusPollFinished(exception);
        }
    }

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);

        if (onJobStatusPollListener != null) {
            onJobStatusPollListener.onJobStatusUpdate(null, null, weakContext.get().getString(R.string.job_status_polling_cancelled));
        }
    }

    private void finishPolling(JobInfo jobInfo, JobStatusInfo jobStatusInfo, String message) {
        if (onJobStatusPollListener != null) {
            onJobStatusPollListener.onJobStatusUpdate(jobInfo, jobStatusInfo, message);
        }

        showAtmDialog = true;
        startTime = System.currentTimeMillis();
        jobInfoList.remove(jobInfo);
    }

    private void showAlarmEncounteredDialog(final CountDownLatch alarmEncounteredLatch, JobInfo jobInfo, JobStatusInfo jobStatusInfo) {
        String title = weakContext.get().getString(R.string.alarm_encountered);
        String positiveButtonText = weakContext.get().getString(android.R.string.ok);
        String negativeButtonText = weakContext.get().getString(android.R.string.cancel);
        String message = weakContext.get().getString(R.string.alarm_encountered_message, jobInfo.getJobId(), jobStatusInfo.alarmInfo.description, positiveButtonText, negativeButtonText);

        onJobStatusPollListener.onJobStatusUserInputRequired(title, message, positiveButtonText, negativeButtonText, new OnUserInputListener() {
            @Override
            public void onPositiveButtonClicked() {
                cancelledByUser = false;
                alarmEncounteredLatch.countDown();
            }

            @Override
            public void onNegativeButtonClicked() {
                cancelledByUser = true;
                alarmEncounteredLatch.countDown();
            }
        });
    }

    private void showCardAtStationDialog(final CountDownLatch smartCardAtStationLatch) {
        String title = weakContext.get().getString(R.string.card_at_station);
        String positiveButtonText = weakContext.get().getString(R.string.resume);
        String negativeButtonText = weakContext.get().getString(android.R.string.cancel);
        String message = weakContext.get().getString(R.string.card_at_station_message, positiveButtonText, negativeButtonText);

        onJobStatusPollListener.onJobStatusUserInputRequired(title, message, positiveButtonText, negativeButtonText, new OnUserInputListener() {
            @Override
            public void onPositiveButtonClicked() {
                cancelledByUser = false;
                smartCardAtStationLatch.countDown();
            }

            @Override
            public void onNegativeButtonClicked() {
                cancelledByUser = true;
                smartCardAtStationLatch.countDown();
            }
        });
    }

    public void setOnJobStatusPollListener(OnJobStatusPollListener onJobStatusPollListener) {
        this.onJobStatusPollListener = onJobStatusPollListener;
    }
}
