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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.JobStatusHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraFieldView;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

public class MultiJobPrintFragment extends Fragment implements CancelJobsTask.OnCancelJobsListener {

    private MultiJobDemoActivity multiJobDemoActivity;
    private DiscoveredPrinter printer;
    private CancelJobsTask cancelJobsTask;

    private ZebraFieldView jobStatus1;
    private ZebraFieldView jobStatus2;
    private ZebraFieldView jobStatus3;
    private ZebraFieldView jobStatus4;
    private TextView jobStatusText;
    private ZebraEditText jobIdEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_multijob_print, container, false);

        multiJobDemoActivity = (MultiJobDemoActivity) getActivity();
        printer = SelectedPrinterManager.getSelectedPrinter();

        jobStatus1 = rootView.findViewById(R.id.jobStatus1);
        jobStatus2 = rootView.findViewById(R.id.jobStatus2);
        jobStatus3 = rootView.findViewById(R.id.jobStatus3);
        jobStatus4 = rootView.findViewById(R.id.jobStatus4);
        jobStatusText = rootView.findViewById(R.id.jobStatusText);
        jobIdEditText = rootView.findViewById(R.id.jobIdEditText);
        ZebraButton cancelJobButton = rootView.findViewById(R.id.cancelJobButton);
        ZebraButton cancelAllJobsButton = rootView.findViewById(R.id.cancelAllJobsButton);

        cancelJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int jobId = Integer.parseInt(jobIdEditText.getText());

                    if (!AsyncTaskHelper.isAsyncTaskRunning(cancelJobsTask)) {
                        UIHelper.hideSoftKeyboard(getActivity());
                        multiJobDemoActivity.setSendJobsButtonEnabled(false);

                        if (cancelJobsTask != null) {
                            cancelJobsTask.cancel(true);
                        }

                        cancelJobsTask = new CancelJobsTask(printer, jobId);
                        cancelJobsTask.setOnCancelJobsListener(MultiJobPrintFragment.this);
                        cancelJobsTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
                    }
                } catch (NumberFormatException e) {
                    DialogHelper.showErrorDialog(getActivity(), getString(R.string.please_enter_a_valid_job_id));
                }
            }
        });

        cancelAllJobsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AsyncTaskHelper.isAsyncTaskRunning(cancelJobsTask)) {
                    UIHelper.hideSoftKeyboard(getActivity());
                    multiJobDemoActivity.setSendJobsButtonEnabled(false);

                    if (cancelJobsTask != null) {
                        cancelJobsTask.cancel(true);
                    }

                    cancelJobsTask = new CancelJobsTask(printer);
                    cancelJobsTask.setOnCancelJobsListener(MultiJobPrintFragment.this);
                    cancelJobsTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (cancelJobsTask != null) {
            cancelJobsTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onCancelJobsStarted(Integer jobId) {
        if (jobId == null) {
            multiJobDemoActivity.showProgressOverlay(getString(R.string.cancelling_all_jobs));
        } else {
            multiJobDemoActivity.showProgressOverlay(getString(R.string.cancelling_job_with_job_id, jobId));
        }
    }

    @Override
    public void onCancelJobsFinished(Exception exception, Integer jobId) {
        multiJobDemoActivity.setSendJobsButtonEnabled(true);
        multiJobDemoActivity.hideProgressOverlay();

        if (exception != null) {
            String errorMessage = jobId != null ? getString(R.string.error_cancelling_job_message, jobId, exception.getMessage()) : getString(R.string.error_cancelling_all_jobs_message, exception.getMessage());
            JobStatusHelper.updateJobStatusLog(getActivity(), jobStatusText, errorMessage);
            DialogHelper.showErrorDialog(getActivity(), errorMessage);
        } else {
            String message = jobId != null ? getString(R.string.cancel_job_id_command_sent_successfully, jobId) : getString(R.string.cancel_all_jobs_command_sent_successfully);
            JobStatusHelper.updateJobStatusLog(getActivity(), jobStatusText, message);
            UIHelper.showSnackbar(getActivity(), message);
        }
    }

    public void setJobStatus(MultiJobNumber jobNumber, String jobStatus) {
        switch (jobNumber) {
            case ONE:
                jobStatus1.setBodyText(jobStatus);
                break;
            case TWO:
                jobStatus2.setBodyText(jobStatus);
                break;
            case THREE:
                jobStatus3.setBodyText(jobStatus);
                break;
            case FOUR:
                jobStatus4.setBodyText(jobStatus);
                break;
        }
    }

    public void clearJobStatusLog() {
        jobStatusText.setText(null);
    }

    public void updateJobStatusLog(Integer jobId, String message) {
        if (jobId != null) {
            JobStatusHelper.updateJobStatusLog(getActivity(), jobId, jobStatusText, message);
        } else {
            JobStatusHelper.updateJobStatusLog(getActivity(), jobStatusText, message);
        }
    }
}
