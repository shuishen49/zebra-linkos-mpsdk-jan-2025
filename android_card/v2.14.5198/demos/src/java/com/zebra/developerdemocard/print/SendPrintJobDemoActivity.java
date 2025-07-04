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

package com.zebra.developerdemocard.print;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.zebra.developerdemocard.util.StorageHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.developerdemocard.util.UriHelper;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraSpinnerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.zebra.developerdemocard.util.StorageHelper.handleRequestStoragePermissionsResult;

public class SendPrintJobDemoActivity extends AppCompatActivity implements SendPrintJobTask.OnSendPrintJobListener,
        PollJobStatusTask.OnJobStatusPollListener {

    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2001;
    public static final int REQUEST_SELECT_FRONT_SIDE_IMAGE_FILE = 3001;
    public static final int REQUEST_SELECT_FRONT_SIDE_OVERLAY_IMAGE_FILE = 3002;
    public static final int REQUEST_SELECT_BACK_SIDE_IMAGE_FILE = 3003;

    private DiscoveredPrinter printer;
    private Uri frontSideImageUri;
    private Uri frontSideOverlayImageUri;
    private Uri backSideImageUri;
    private SendPrintJobTask sendPrintJobTask;
    private PollJobStatusTask pollJobStatusTask;
    private AlertDialog insertCardDialog;

    private Switch printFrontSideSwitch;
    private RelativeLayout frontSideImageFileContainer;
    private ZebraEditText frontSideImageFileEditText;
    private ZebraSpinnerView frontSideTypesSpinner;
    private Switch printFrontSideOverlaySwitch;
    private RelativeLayout frontSideOverlayImageFileContainer;
    private ZebraEditText frontSideOverlayImageFile;
    private RelativeLayout backSideImageFileContainer;
    private ZebraEditText backSideImageFileEditText;
    private Switch printBackSideSwitch;
    private ZebraSpinnerView printQuantities;
    private ZebraButton printButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;
    private TextView jobStatusText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_print);
        setTitle(R.string.print_ymcko_mono_demo);
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

        createFrontSideInterface();
        createFrontSideOverlayInterface();
        createBackSideInterface();

        printQuantities = findViewById(R.id.printQuantities);

        ZebraButton cancelButton = findViewById(R.id.cancelButton);
        printButton = findViewById(R.id.printButton);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        jobStatusText = findViewById(R.id.jobStatusText);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!AsyncTaskHelper.isAsyncTaskRunning(sendPrintJobTask)) {
                    UIHelper.hideSoftKeyboard(SendPrintJobDemoActivity.this);
                    printButton.setEnabled(false);

                    Map<PrintType, Uri> frontSideImageUriMap = new HashMap<>();
                    Map<PrintType, Uri> backSideImageUriMap = new HashMap<>();

                    if (printFrontSideSwitch.isChecked()) {
                        PrintType printType = PrintType.valueOf((String) frontSideTypesSpinner.getSelectedItem());
                        frontSideImageUriMap.put(printType, frontSideImageUri);
                    }

                    if (printFrontSideOverlaySwitch.isChecked()) {
                        frontSideImageUriMap.put(PrintType.Overlay, frontSideOverlayImageUri);
                    }

                    if (printBackSideSwitch.isChecked()) {
                        backSideImageUriMap.put(PrintType.MonoK, backSideImageUri);
                    }

                    int quantity = Integer.parseInt((String) printQuantities.getSelectedItem());

                    PrintOptions printOptions = new PrintOptions(frontSideImageUriMap, backSideImageUriMap, quantity);
                    sendPrintJobTask = new SendPrintJobTask(SendPrintJobDemoActivity.this, printer, printOptions);
                    sendPrintJobTask.setOnSendPrintJobListener(SendPrintJobDemoActivity.this);
                    sendPrintJobTask.execute();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!AsyncTaskHelper.isAsyncTaskRunning(sendPrintJobTask)) {
            if (insertCardDialog != null && insertCardDialog.isShowing()) {
                insertCardDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (sendPrintJobTask != null) {
            sendPrintJobTask.cancel(true);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String filename = UriHelper.getFilename(this, uri);
            boolean filenameExists = filename != null && !filename.trim().isEmpty();

            switch (requestCode) {
                case REQUEST_SELECT_FRONT_SIDE_IMAGE_FILE:
                    frontSideImageUri = filenameExists ? uri : null;
                    frontSideImageFileEditText.setText(filenameExists ? filename : null);
                    break;
                case REQUEST_SELECT_FRONT_SIDE_OVERLAY_IMAGE_FILE:
                    frontSideOverlayImageUri = filenameExists ? uri : null;
                    frontSideOverlayImageFile.setText(filenameExists ? filename : null);
                    break;
                case REQUEST_SELECT_BACK_SIDE_IMAGE_FILE:
                    backSideImageUri = filenameExists ? uri : null;
                    backSideImageFileEditText.setText(filenameExists ? filename : null);
                    break;
            }
        }
    }

    @Override
    public void onSendPrintJobStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.sending_print_job_to_printer));
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
    public void onSendPrintJobFinished(Exception exception, Integer jobId, CardSource cardSource) {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            printButton.setEnabled(true);

            DialogHelper.showErrorDialog(this, getString(R.string.error_printing_card_message, exception.getMessage()));
        } else if (jobId != null) {
            UIHelper.showSnackbar(this, getString(R.string.print_job_sent_successfully));

            if (pollJobStatusTask != null) {
                pollJobStatusTask.cancel(true);
            }

            pollJobStatusTask = new PollJobStatusTask(this, SelectedPrinterManager.getSelectedPrinter(), new JobInfo(jobId, cardSource));
            pollJobStatusTask.setOnJobStatusPollListener(this);
            pollJobStatusTask.execute();
        } else {
            printButton.setEnabled(true);
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

                insertCardDialog = DialogHelper.createInsertCardDialog(SendPrintJobDemoActivity.this);
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onJobStatusPollFinished(Exception exception) {
        printButton.setEnabled(true);

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        if (exception != null) {
            String errorMessage = getString(R.string.error_printing_card_message, exception.getMessage());
            JobStatusHelper.updateJobStatusLog(this, jobStatusText, errorMessage);
            DialogHelper.showErrorDialog(this, errorMessage);
        }
    }

    private void createFrontSideInterface() {
        printFrontSideSwitch = findViewById(R.id.printFrontSideSwitch);
        frontSideImageFileContainer = findViewById(R.id.frontSideImageFileContainer);
        frontSideImageFileEditText = findViewById(R.id.frontSideImageFileEditText);
        ImageView frontSideImageFileBrowseButton = findViewById(R.id.frontSideImageFileBrowseButton);
        frontSideTypesSpinner = findViewById(R.id.frontSideTypesSpinner);

        frontSideTypesSpinner.setSpinnerEntries(Arrays.asList(PrintType.Color.toString(), PrintType.MonoK.toString()));

        frontSideImageFileContainer.setVisibility(printFrontSideSwitch.isChecked() ? View.VISIBLE : View.GONE);
        frontSideTypesSpinner.setVisibility(printFrontSideSwitch.isChecked() ? View.VISIBLE : View.GONE);

        printFrontSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int visibility = isChecked ? View.VISIBLE : View.GONE;
                frontSideImageFileContainer.setVisibility(visibility);
                frontSideTypesSpinner.setVisibility(visibility);
            }
        });

        frontSideImageFileBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(createImageFileSelectIntent(), REQUEST_SELECT_FRONT_SIDE_IMAGE_FILE);
            }
        });
    }

    private void createFrontSideOverlayInterface() {
        printFrontSideOverlaySwitch = findViewById(R.id.printFrontSideOverlaySwitch);
        frontSideOverlayImageFileContainer = findViewById(R.id.frontSideOverlayImageFileContainer);
        frontSideOverlayImageFile = findViewById(R.id.frontSideOverlayImageFile);
        ImageView frontSideOverlayImageFileBrowseButton = findViewById(R.id.frontSideOverlayImageFileBrowseButton);

        frontSideOverlayImageFileContainer.setVisibility(printFrontSideOverlaySwitch.isChecked() ? View.VISIBLE : View.GONE);

        printFrontSideOverlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int visibility = isChecked ? View.VISIBLE : View.GONE;
                frontSideOverlayImageFileContainer.setVisibility(visibility);
            }
        });

        frontSideOverlayImageFileBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(createImageFileSelectIntent(), REQUEST_SELECT_FRONT_SIDE_OVERLAY_IMAGE_FILE);
            }
        });
    }

    private void createBackSideInterface() {
        backSideImageFileContainer = findViewById(R.id.backSideImageFileContainer);
        backSideImageFileEditText = findViewById(R.id.backSideImageFileEditText);
        ImageView backSideImageFileBrowseButton = findViewById(R.id.backSideImageFileBrowseButton);
        printBackSideSwitch = findViewById(R.id.printBackSideSwitch);

        backSideImageFileContainer.setVisibility(printBackSideSwitch.isChecked() ? View.VISIBLE : View.GONE);

        printBackSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                int visibility = isChecked ? View.VISIBLE : View.GONE;
                backSideImageFileContainer.setVisibility(visibility);
            }
        });

        backSideImageFileBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(createImageFileSelectIntent(), REQUEST_SELECT_BACK_SIDE_IMAGE_FILE);
            }
        });
    }

    private Intent createImageFileSelectIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        return Intent.createChooser(getIntent, getString(R.string.select_graphic_file));
    }
}
