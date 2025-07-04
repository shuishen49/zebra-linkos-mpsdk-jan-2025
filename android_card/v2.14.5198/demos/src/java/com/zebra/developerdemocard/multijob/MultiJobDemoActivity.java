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

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.jobstatus.JobInfo;
import com.zebra.developerdemocard.jobstatus.PollJobStatusTask;
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.StorageHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;

import java.util.ArrayList;
import java.util.List;

import static com.zebra.developerdemocard.util.StorageHelper.handleRequestStoragePermissionsResult;

public class MultiJobDemoActivity extends AppCompatActivity implements RetrieveMultiJobSettingsRangesTask.OnRetrieveMultiJobSettingsRangesListener,
        SendMultiJobsTask.OnSendMultiJobsListener,
        PollJobStatusTask.OnJobStatusPollListener {

    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2001;

    private DiscoveredPrinter printer;
    private MultiJobFragmentPagerAdapter multiJobFragmentPagerAdapter;
    private RetrieveMultiJobSettingsRangesTask retrieveMultiJobSettingsRangesTask;
    private SendMultiJobsTask sendMultiJobsTask;
    private PollJobStatusTask pollJobStatusTask;
    private AlertDialog insertCardDialog;

    private ViewPager viewPager;
    private LinearLayout progressOverlay;
    private TextView progressMessage;
    private ZebraButton sendJobsButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_multijob);
        setTitle(R.string.multijob_demo);
        UIHelper.setLogoOnActionBar(this);

        if (!StorageHelper.isExternalStorageWritable()) {
            DialogHelper.showStorageErrorDialog(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        }

        StorageHelper.requestStoragePermissionIfNotGranted(this, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);

        printer = SelectedPrinterManager.getSelectedPrinter();

        multiJobFragmentPagerAdapter = new MultiJobFragmentPagerAdapter(this, getSupportFragmentManager());

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        ZebraButton cancelButton = findViewById(R.id.cancelButton);
        sendJobsButton = findViewById(R.id.sendJobsButton);

        viewPager.setAdapter(multiJobFragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        tabLayout.setupWithViewPager(viewPager);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sendJobsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AsyncTaskHelper.isAsyncTaskRunning(sendMultiJobsTask)) {
                    UIHelper.hideSoftKeyboard(MultiJobDemoActivity.this);
                    sendJobsButton.setEnabled(false);

                    List<MultiJobInfo> multiJobInfoList = new ArrayList<>();
                    multiJobInfoList.add(multiJobFragmentPagerAdapter.getMultiJob1Fragment().buildMultiJobInfo());
                    multiJobInfoList.add(multiJobFragmentPagerAdapter.getMultiJob2Fragment().buildMultiJobInfo());
                    multiJobInfoList.add(multiJobFragmentPagerAdapter.getMultiJob3Fragment().buildMultiJobInfo());
                    multiJobInfoList.add(multiJobFragmentPagerAdapter.getMultiJob4Fragment().buildMultiJobInfo());

                    List<MultiJobInfo> validMultiJobInfoList = new ArrayList<>();
                    for (MultiJobInfo multiJobInfo : multiJobInfoList) {
                        if (multiJobInfo.hasPrintableFrontSide() || multiJobInfo.hasPrintableBackSide() || multiJobInfo.hasEncodableMagData()) {
                            validMultiJobInfoList.add(multiJobInfo);
                        } else {
                            multiJobFragmentPagerAdapter.getMultiJobPrintFragment().setJobStatus(multiJobInfo.getJobNumber(), getString(R.string.not_configured));
                        }
                    }

                    if (sendMultiJobsTask != null) {
                        sendMultiJobsTask.cancel(true);
                    }

                    sendMultiJobsTask = new SendMultiJobsTask(MultiJobDemoActivity.this, printer, validMultiJobInfoList);
                    sendMultiJobsTask.setOnSendMultiJobsListener(MultiJobDemoActivity.this);
                    sendMultiJobsTask.execute();
                }
            }
        });

        retrieveSettingsRanges();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!AsyncTaskHelper.isAsyncTaskRunning(sendMultiJobsTask)) {
            if (insertCardDialog != null && insertCardDialog.isShowing()) {
                insertCardDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (retrieveMultiJobSettingsRangesTask != null) {
            retrieveMultiJobSettingsRangesTask.cancel(true);
        }

        if (sendMultiJobsTask != null) {
            sendMultiJobsTask.cancel(true);
        }

        if (pollJobStatusTask != null) {
            pollJobStatusTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                handleRequestStoragePermissionsResult(this, permission, grantResult);
            }
        }
    }

    @Override
    public void onRetrieveMultiJobSettingsRangesStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.retrieving_settings_ranges));
        sendJobsButton.setEnabled(false);
    }

    @Override
    public void onRetrieveMultiJobSettingsRangesFinished(Exception exception, MultiJobPrinterSettings multiJobPrinterSettings) {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);
        sendJobsButton.setEnabled(true);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_retrieving_settings_ranges_message, exception.getMessage()));
        } else if (multiJobPrinterSettings != null) {
            multiJobFragmentPagerAdapter.getMultiJob1Fragment().updateMultiJobPrinterSettings(multiJobPrinterSettings);
            multiJobFragmentPagerAdapter.getMultiJob2Fragment().updateMultiJobPrinterSettings(multiJobPrinterSettings);
            multiJobFragmentPagerAdapter.getMultiJob3Fragment().updateMultiJobPrinterSettings(multiJobPrinterSettings);
            multiJobFragmentPagerAdapter.getMultiJob4Fragment().updateMultiJobPrinterSettings(multiJobPrinterSettings);
        }
    }

    @Override
    public void onSendMultiJobsStarted() {
        multiJobFragmentPagerAdapter.getMultiJobPrintFragment().clearJobStatusLog();
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.sending_jobs));

        viewPager.setCurrentItem(multiJobFragmentPagerAdapter.getItemPosition(multiJobFragmentPagerAdapter.getMultiJobPrintFragment()), true);
    }

    @Override
    public void onPrinterReadyUpdate(String message, boolean showDialog) {
        multiJobFragmentPagerAdapter.getMultiJobPrintFragment().updateJobStatusLog(null, message);

        if (showDialog) {
            DialogHelper.showErrorDialog(this, message);
        }
    }

    @Override
    public void onSendMultiJobsLogUpdate(String message) {
        multiJobFragmentPagerAdapter.getMultiJobPrintFragment().updateJobStatusLog(null, message);
    }

    @Override
    public void onSendMultiJobsFinished(Exception exception, List<MultiJobInfo> multiJobInfoList) {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            sendJobsButton.setEnabled(true);

            String errorMessage = getString(R.string.error_sending_jobs_message, exception.getMessage());
            multiJobFragmentPagerAdapter.getMultiJobPrintFragment().updateJobStatusLog(null, errorMessage);
            DialogHelper.showErrorDialog(this, errorMessage);
        } else {
            UIHelper.showSnackbar(this, getString(R.string.jobs_sent_successfully));

            if (pollJobStatusTask != null) {
                pollJobStatusTask.cancel(true);
            }

            List<JobInfo> jobInfoList = new ArrayList<>();
            jobInfoList.addAll(multiJobInfoList);

            pollJobStatusTask = new PollJobStatusTask(this, printer, jobInfoList);
            pollJobStatusTask.setOnJobStatusPollListener(this);
            pollJobStatusTask.execute();
        }
    }

    @Override
    public void onJobStatusUpdate(final JobInfo jobInfo, final JobStatusInfo jobStatusInfo, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MultiJobPrintFragment multiJobPrintFragment = multiJobFragmentPagerAdapter.getMultiJobPrintFragment();
                multiJobPrintFragment.updateJobStatusLog(jobInfo != null ? jobInfo.getJobId() : null, message);

                if (jobInfo != null && jobStatusInfo != null) {
                    String displayJobStatus = jobStatusInfo.printStatus;
                    if (displayJobStatus.equals("done_ok")) {
                        displayJobStatus = getString(R.string.completed);
                    } else if (displayJobStatus.equals("done_error")) {
                        displayJobStatus = getString(R.string.completed_with_error);
                    } else if (displayJobStatus.contains("cancelled")) {
                        displayJobStatus = jobStatusInfo.errorInfo.value != 0 ? getString(R.string.cancelled_with_error) : getString(R.string.cancelled);
                    }

                    multiJobPrintFragment.setJobStatus(((MultiJobInfo) jobInfo).getJobNumber(), getString(R.string.job_id_job_status, jobInfo.getJobId(), displayJobStatus));
                }
            }
        });
    }

    @Override
    public void onJobStatusUserInputRequired(String title, String message, String positiveButtonText, String negativeButtonText, final PollJobStatusTask.OnUserInputListener onUserInputListener) {
        DialogHelper.showAlarmEncounteredDialog(this, title, message, positiveButtonText, negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onUserInputListener != null) {
                    onUserInputListener.onPositiveButtonClicked();
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onUserInputListener != null) {
                    onUserInputListener.onNegativeButtonClicked();
                }
            }
        });
    }

    @Override
    public void onJobStatusAtmCardRequired() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (insertCardDialog != null && insertCardDialog.isShowing()) {
                    insertCardDialog.dismiss();
                }

                insertCardDialog = DialogHelper.createInsertCardDialog(MultiJobDemoActivity.this);
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onJobStatusPollFinished(Exception exception) {
        sendJobsButton.setEnabled(true);

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_polling_job_status_message, exception.getMessage()));
        }
    }

    private void retrieveSettingsRanges() {
        if (retrieveMultiJobSettingsRangesTask != null) {
            retrieveMultiJobSettingsRangesTask.cancel(true);
        }

        retrieveMultiJobSettingsRangesTask = new RetrieveMultiJobSettingsRangesTask(this, printer);
        retrieveMultiJobSettingsRangesTask.setOnRetrieveMultiJobSettingsRangesListener(this);
        retrieveMultiJobSettingsRangesTask.execute();
    }

    public void setSendJobsButtonEnabled(boolean enabled) {
        sendJobsButton.setEnabled(enabled);
    }

    public void showProgressOverlay(String message) {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, message);
    }

    public void hideProgressOverlay() {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);
    }
}
