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

package com.zebra.developerdemocard.magencode;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.jobstatus.JobInfo;
import com.zebra.developerdemocard.jobstatus.PollJobStatusTask;
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.JobStatusHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.containers.MagTrackData;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraSpinnerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MagEncodeDemoActivity extends AppCompatActivity implements RetrieveMagEncodeSettingsRangesTask.OnRetrieveMagEncodeSettingsRangesListener,
        MagEncodeReadDataTask.OnMagEncodeReadDataListener,
        MagEncodeWriteDataTask.OnMagEncodeWriteDataListener,
        PollJobStatusTask.OnJobStatusPollListener {

    private DiscoveredPrinter printer;
    private RetrieveMagEncodeSettingsRangesTask retrieveMagEncodeSettingsRangesTask;
    private MagEncodeReadDataTask magEncodeReadDataTask;
    private MagEncodeWriteDataTask magEncodeWriteDataTask;
    private PollJobStatusTask pollJobStatusTask;
    private AlertDialog insertCardDialog;
    private boolean isLogSystemOut = false;

    private ZebraSpinnerView magEncodeJobs;
    private ZebraSpinnerView magEncodeSources;
    private ZebraSpinnerView magEncodeDestinations;
    private ZebraSpinnerView magEncodeCoercivityTypes;
    private ZebraEditText track1DataInput;
    private ZebraEditText track2DataInput;
    private ZebraEditText track3DataInput;
    private LinearLayout verifyEncodingContainer;
    private Switch verifyEncodingSwitch;
    private ZebraButton readWriteButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;
    private TextView jobStatusText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_mag_encode);
        setTitle(R.string.mag_encode_demo);
        UIHelper.setLogoOnActionBar(this);

        printer = SelectedPrinterManager.getSelectedPrinter();

        magEncodeJobs = findViewById(R.id.magEncodeJobs);
        magEncodeSources = findViewById(R.id.magEncodeSources);
        magEncodeDestinations = findViewById(R.id.magEncodeDestinations);
        magEncodeCoercivityTypes = findViewById(R.id.magEncodeCoercivityTypes);
        track1DataInput = findViewById(R.id.track1DataInput);
        track2DataInput = findViewById(R.id.track2DataInput);
        track3DataInput = findViewById(R.id.track3DataInput);
        verifyEncodingContainer = findViewById(R.id.verifyEncodingContainer);
        verifyEncodingSwitch = findViewById(R.id.verifyEncodingSwitch);
        ZebraButton cancelButton = findViewById(R.id.cancelButton);
        readWriteButton = findViewById(R.id.readWriteButton);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        jobStatusText = findViewById(R.id.jobStatusText);

        magEncodeJobs.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFormInputs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        readWriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWaitingForAsyncTask()) {
                    UIHelper.hideSoftKeyboard(MagEncodeDemoActivity.this);
                    readWriteButton.setEnabled(false);

                    String buttonText = readWriteButton.getText().toString();
                    if (getString(R.string.read).equals(buttonText)) {
                        if (magEncodeReadDataTask != null) {
                            magEncodeReadDataTask.cancel(true);
                        }

                        magEncodeReadDataTask = new MagEncodeReadDataTask(MagEncodeDemoActivity.this,
                                printer,
                                CardSource.fromString(magEncodeSources.getSelectedItem().toString()),
                                CardDestination.fromString(magEncodeDestinations.getSelectedItem().toString()));
                        magEncodeReadDataTask.setOnMagEncodeReadDataListener(MagEncodeDemoActivity.this);
                        magEncodeReadDataTask.execute();
                    } else if (getString(R.string.write).equals(buttonText)) {
                        MagEncodeOptions magEncodeOptions = new MagEncodeOptions(CardSource.fromString(magEncodeSources.getSelectedItem().toString()),
                                CardDestination.fromString(magEncodeDestinations.getSelectedItem().toString()),
                                CoercivityType.fromString(magEncodeCoercivityTypes.getSelectedItem().toString()),
                                verifyEncodingSwitch.isChecked(),
                                track1DataInput.getText(),
                                track2DataInput.getText(),
                                track3DataInput.getText());

                        if (magEncodeWriteDataTask != null) {
                            magEncodeWriteDataTask.cancel(true);
                        }

                        magEncodeWriteDataTask = new MagEncodeWriteDataTask(MagEncodeDemoActivity.this, printer, magEncodeOptions);
                        magEncodeWriteDataTask.setOnMagEncodeWriteDataListener(MagEncodeDemoActivity.this);
                        magEncodeWriteDataTask.execute();
                    }
                }
            }
        });

        updateFormInputs();
        refreshMagEncodeData();
    }

    @Override
    protected void onDestroy() {
        if (retrieveMagEncodeSettingsRangesTask != null) {
            retrieveMagEncodeSettingsRangesTask.cancel(true);
        }

        if (magEncodeReadDataTask != null) {
            magEncodeReadDataTask.cancel(true);
        }

        if (magEncodeWriteDataTask != null) {
            magEncodeWriteDataTask.cancel(true);
        }

        if (pollJobStatusTask != null) {
            pollJobStatusTask.cancel(true);
        }

        super.onDestroy();
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
    public void onRetrieveMagEncodeSettingsRangesStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.retrieving_settings_ranges));
    }

    @Override
    public void onRetrieveMagEncodeSettingsRangesFinished(Exception exception, Map<String, List<String>> magEncodeSettingsMap) {
        boolean isMagEncodeSettingsMapPresent = magEncodeSettingsMap != null;
        readWriteButton.setEnabled(isMagEncodeSettingsMapPresent);
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        magEncodeJobs.setEnabled(isMagEncodeSettingsMapPresent);
        magEncodeSources.setEnabled(isMagEncodeSettingsMapPresent);
        magEncodeDestinations.setEnabled(isMagEncodeSettingsMapPresent);
        magEncodeCoercivityTypes.setEnabled(isMagEncodeSettingsMapPresent);
        track1DataInput.setEnabled(isMagEncodeSettingsMapPresent);
        track2DataInput.setEnabled(isMagEncodeSettingsMapPresent);
        track3DataInput.setEnabled(isMagEncodeSettingsMapPresent);
        verifyEncodingSwitch.setEnabled(isMagEncodeSettingsMapPresent);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_retrieving_settings_ranges_message, exception.getMessage()));
        } else if (isMagEncodeSettingsMapPresent) {
            magEncodeSources.setSpinnerEntries(new ArrayList<>(magEncodeSettingsMap.get(ZebraCardJobSettingNames.CARD_SOURCE)));
            magEncodeDestinations.setSpinnerEntries(new ArrayList<>(magEncodeSettingsMap.get(ZebraCardJobSettingNames.CARD_DESTINATION)));
            magEncodeCoercivityTypes.setSpinnerEntries(new ArrayList<>(magEncodeSettingsMap.get(ZebraCardJobSettingNames.MAG_COERCIVITY)));
        }
    }

    @Override
    public void onPrinterReadyUpdate(String message, boolean showDialog) {
        if (isLogSystemOut) {
            System.out.println(message);
        } else {
            JobStatusHelper.updateJobStatusLog(this, jobStatusText, message);
        }

        if (showDialog) {
            DialogHelper.showErrorDialog(this, message);
        }
    }

    @Override
    public void onMagEncodeReadDataStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.reading_mag_encode_data));
        jobStatusText.setText(null);

        System.setOut(new PrintStream(new OutputStream() {
            private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public void write(int i) throws IOException {
                outputStream.write(i);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        jobStatusText.setText(new String(outputStream.toByteArray()));
                    }
                });
            }
        }));
        isLogSystemOut = true;
    }

    @Override
    public void onMagEncodeReadDataAtmCardRequired() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (insertCardDialog != null && insertCardDialog.isShowing()) {
                    insertCardDialog.dismiss();
                }

                insertCardDialog = DialogHelper.createInsertCardDialog(MagEncodeDemoActivity.this);
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onMagEncodeReadDataFinished(Exception exception, MagTrackData magTrackData) {
        readWriteButton.setEnabled(true);
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        isLogSystemOut = false;

        if (exception != null) {
            String errorMessage = getString(R.string.error_reading_mag_encode_data_message, exception.getMessage());
            System.out.println(errorMessage);
            DialogHelper.showErrorDialog(this, errorMessage);
        } else if (magTrackData != null) {
            UIHelper.showSnackbar(this, getString(R.string.mag_encode_data_read_successfully));

            track1DataInput.setText(magTrackData.track1Data);
            track2DataInput.setText(magTrackData.track2Data);
            track3DataInput.setText(magTrackData.track3Data);
        }

        System.setOut(System.out);
    }

    @Override
    public void onMagEncodeWriteDataStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.writing_mag_encode_data));
        jobStatusText.setText(null);
    }

    @Override
    public void onMagEncodeWriteDataFinished(Exception exception, Integer jobId, CardSource cardSource) {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            readWriteButton.setEnabled(true);

            String errorMessage = getString(R.string.error_writing_mag_encode_data_message, exception.getMessage());
            JobStatusHelper.updateJobStatusLog(this, jobId, jobStatusText, errorMessage);
            DialogHelper.showErrorDialog(this, errorMessage);
        } else if (jobId != null) {
            UIHelper.showSnackbar(this, getString(R.string.mag_encode_job_sent_successfully));

            if (pollJobStatusTask != null) {
                pollJobStatusTask.cancel(true);
            }

            pollJobStatusTask = new PollJobStatusTask(this, SelectedPrinterManager.getSelectedPrinter(), new JobInfo(jobId, cardSource));
            pollJobStatusTask.setOnJobStatusPollListener(this);
            pollJobStatusTask.execute();
        } else {
            readWriteButton.setEnabled(true);
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

                insertCardDialog = DialogHelper.createInsertCardDialog(MagEncodeDemoActivity.this);
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onJobStatusPollFinished(Exception exception) {
        readWriteButton.setEnabled(true);

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_polling_job_status_message, exception.getMessage()));
        }
    }

    private boolean isWaitingForAsyncTask() {
        return AsyncTaskHelper.isAsyncTaskRunning(magEncodeReadDataTask) || AsyncTaskHelper.isAsyncTaskRunning(magEncodeWriteDataTask);
    }

    private void updateFormInputs() {
        boolean isWriting = getString(R.string.write).equals(magEncodeJobs.getSelectedItem().toString());
        verifyEncodingContainer.setVisibility(isWriting ? View.VISIBLE : View.GONE);
        readWriteButton.setText(magEncodeJobs.getSelectedItem().toString());
        track1DataInput.setText(null);
        track2DataInput.setText(null);
        track3DataInput.setText(null);
    }

    private void refreshMagEncodeData() {
        if (retrieveMagEncodeSettingsRangesTask != null) {
            retrieveMagEncodeSettingsRangesTask.cancel(true);
        }

        retrieveMagEncodeSettingsRangesTask = new RetrieveMagEncodeSettingsRangesTask(this, printer);
        retrieveMagEncodeSettingsRangesTask.setOnRetrieveMagEncodeSettingsRangesListener(this);
        retrieveMagEncodeSettingsRangesTask.execute();
    }
}
