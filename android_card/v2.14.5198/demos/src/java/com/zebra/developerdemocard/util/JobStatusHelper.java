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
import android.util.SparseArray;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JobStatusHelper {
    private static SparseArray<String> jobIdLastMessageMap = new SparseArray<>();

    public static void updateJobStatusLog(Activity activity, TextView jobStatusTextView, String jobStatusMessage) {
        updateJobStatusLog(activity, null, jobStatusTextView, jobStatusMessage);
    }

    public static void updateJobStatusLog(Activity activity, final Integer jobId, final TextView jobStatusTextView, final String jobStatusMessage) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (jobId != null) {
                        String lastMessageForJobId = jobIdLastMessageMap.get(jobId);
                        if (lastMessageForJobId == null || !lastMessageForJobId.equals(jobStatusMessage)) {
                            writeToJobStatusLog(jobStatusTextView, jobStatusMessage);

                            jobIdLastMessageMap.put(jobId, jobStatusMessage);
                        }
                    } else {
                        writeToJobStatusLog(jobStatusTextView, jobStatusMessage);
                    }
                }
            });
        }
    }

    private static void writeToJobStatusLog(TextView jobStatusTextView, String jobStatusMessage) {
        String jobStatusLog = jobStatusTextView.getText().toString();
        if (!jobStatusLog.isEmpty()) {
            jobStatusLog += "\n";
        }
        jobStatusLog += getCurrentTimestamp() + " " + jobStatusMessage;
        jobStatusTextView.setText(jobStatusLog);
    }

    private static String getCurrentTimestamp(){
        return new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]", Locale.getDefault()).format(new Date());
    }
}
