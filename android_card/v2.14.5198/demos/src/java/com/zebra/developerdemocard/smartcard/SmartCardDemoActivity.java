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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.zebra.developerdemocard.util.JobStatusHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;
import com.zebra.zebraui.ZebraSpinnerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmartCardDemoActivity extends AppCompatActivity implements RetrieveSmartCardSettingsRangesTask.OnRetrieveSmartCardSettingsRangesListener,
        SendSmartCardJobTask.OnSendSmartCardJobListener,
        PollJobStatusTask.OnJobStatusPollListener {

    private DiscoveredPrinter printer;
    private RetrieveSmartCardSettingsRangesTask retrieveSmartCardSettingsRangesTask;
    private SendSmartCardJobTask sendSmartCardJobTask;
    private PollJobStatusTask pollJobStatusTask;
    private AlertDialog insertCardDialog;

    private ZebraSpinnerView smartCardSources;
    private ZebraSpinnerView smartCardDestinations;
    private ZebraSpinnerView smartCardCardTypes;
    private ZebraButton startJobButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;
    private TextView jobStatusText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_smart_card);
        setTitle(R.string.smart_card_demo);
        UIHelper.setLogoOnActionBar(this);

        printer = SelectedPrinterManager.getSelectedPrinter();

        smartCardSources = findViewById(R.id.smartCardSources);
        smartCardDestinations = findViewById(R.id.smartCardDestinations);
        smartCardCardTypes = findViewById(R.id.smartCardCardTypes);

        ZebraButton cancelButton = findViewById(R.id.cancelButton);
        startJobButton = findViewById(R.id.startJobButton);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        jobStatusText = findViewById(R.id.jobStatusText);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        startJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWaitingForAsyncTask()) {
                    UIHelper.hideSoftKeyboard(SmartCardDemoActivity.this);
                    startJobButton.setEnabled(false);

                    SmartCardOptions smartCardOptions = new SmartCardOptions(CardSource.fromString(smartCardSources.getSelectedItem().toString()),
                            CardDestination.valueOf(smartCardDestinations.getSelectedItem().toString()),
                            smartCardCardTypes.getSelectedItem().toString());

                    if (sendSmartCardJobTask != null) {
                        sendSmartCardJobTask.cancel(true);
                    }

                    sendSmartCardJobTask = new SendSmartCardJobTask(SmartCardDemoActivity.this, printer, smartCardOptions);
                    sendSmartCardJobTask.setOnSendSmartCardJobListener(SmartCardDemoActivity.this);
                    sendSmartCardJobTask.execute();
                }
            }
        });

        refreshSmartCardData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isWaitingForAsyncTask()) {
            if (insertCardDialog != null && insertCardDialog.isShowing()) {
                insertCardDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (retrieveSmartCardSettingsRangesTask != null) {
            retrieveSmartCardSettingsRangesTask.cancel(true);
        }

        if (sendSmartCardJobTask != null) {
            sendSmartCardJobTask.cancel(true);
        }

        if (pollJobStatusTask != null) {
            pollJobStatusTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onRetrieveSmartCardSettingsRangesStarted() {
        showProgressOverlay(getString(R.string.retrieving_settings_ranges));
    }

    @Override
    public void onRetrieveSmartCardSettingsRangesFinished(Exception exception, Map<String, List<String>> smartCardSettingsMap) {
        boolean isSettingsMapPresent = smartCardSettingsMap != null;
        startJobButton.setEnabled(isSettingsMapPresent);
        hideProgressOverlay();

        smartCardSources.setEnabled(isSettingsMapPresent);
        smartCardDestinations.setEnabled(isSettingsMapPresent);
        smartCardCardTypes.setEnabled(isSettingsMapPresent);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_retrieving_settings_ranges_message, exception.getMessage()));
        } else if (isSettingsMapPresent) {
            smartCardSources.setSpinnerEntries(new ArrayList<>(smartCardSettingsMap.get(RetrieveSmartCardSettingsRangesTask.KEY_CARD_SOURCE)));
            smartCardDestinations.setSpinnerEntries(new ArrayList<>(smartCardSettingsMap.get(RetrieveSmartCardSettingsRangesTask.KEY_CARD_DESTINATION)));
            smartCardCardTypes.setSpinnerEntries(new ArrayList<>(smartCardSettingsMap.get(RetrieveSmartCardSettingsRangesTask.KEY_ENCODER_TYPE)));
        }
    }

    @Override
    public void onSendSmartCardJobStarted() {
        showProgressOverlay(getString(R.string.starting_smart_card_operation));
        jobStatusText.setText(null);
    }

    @Override
    public void onPrinterReadyUpdate(String message, boolean showDialog) {
        JobStatusHelper.updateJobStatusLog(this, jobStatusText, message);

        if (showDialog) {
            DialogHelper.showErrorDialog(this, message);
        }
    }

    @Override
    public void onSendSmartCardJobAtmCardRequired() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (insertCardDialog != null && insertCardDialog.isShowing()) {
                    insertCardDialog.dismiss();
                }

                insertCardDialog = DialogHelper.createInsertCardDialog(SmartCardDemoActivity.this);
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onSendSmartCardJobFinished(Exception exception, Integer jobId, CardSource cardSource) {
        hideProgressOverlay();

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        if (exception != null) {
            startJobButton.setEnabled(true);

            String errorMessage = getString(R.string.error_sending_smart_card_job_message, exception.getMessage());
            JobStatusHelper.updateJobStatusLog(this, jobStatusText, errorMessage);
            DialogHelper.showErrorDialog(this, errorMessage);
        } else if (jobId != null) {
            UIHelper.showSnackbar(this, getString(R.string.smart_card_job_sent_successfully));

            if (pollJobStatusTask != null) {
                pollJobStatusTask.cancel(true);
            }

            pollJobStatusTask = new PollJobStatusTask(this, SelectedPrinterManager.getSelectedPrinter(), new JobInfo(jobId, cardSource));
            pollJobStatusTask.setOnJobStatusPollListener(this);
            pollJobStatusTask.execute();
        } else {
            startJobButton.setEnabled(true);
        }
    }

    @Override
    public void onJobStatusUpdate(JobInfo jobInfo, JobStatusInfo jobStatusInfo, String message) {
        JobStatusHelper.updateJobStatusLog(this, jobInfo != null ? jobInfo.getJobId() : null, jobStatusText, message);
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

                insertCardDialog = DialogHelper.createInsertCardDialog(SmartCardDemoActivity.this);
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onJobStatusPollFinished(Exception exception) {
        startJobButton.setEnabled(true);

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_polling_job_status_message, exception.getMessage()));
        }
    }

    private boolean isWaitingForAsyncTask() {
        return AsyncTaskHelper.isAsyncTaskRunning(retrieveSmartCardSettingsRangesTask) || AsyncTaskHelper.isAsyncTaskRunning(sendSmartCardJobTask);
    }

    private void refreshSmartCardData() {
        if (!isWaitingForAsyncTask()) {
            if (retrieveSmartCardSettingsRangesTask != null) {
                retrieveSmartCardSettingsRangesTask.cancel(true);
            }

            retrieveSmartCardSettingsRangesTask = new RetrieveSmartCardSettingsRangesTask(this, printer);
            retrieveSmartCardSettingsRangesTask.setOnRetrieveSmartCardSettingsRangesListener(this);
            retrieveSmartCardSettingsRangesTask.execute();
        }
    }

    private void showProgressOverlay(String message) {
        progressMessage.setText(message);
        progressOverlay.setVisibility(View.VISIBLE);
    }

    private void hideProgressOverlay() {
        progressMessage.setText(null);
        progressOverlay.setVisibility(View.GONE);
    }
}
