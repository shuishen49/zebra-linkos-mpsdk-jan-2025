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

package com.zebra.developerdemocard.graphicconversion;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.BitmapHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.JobStatusHelper;
import com.zebra.developerdemocard.util.PrinterModelInfo;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.StorageHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.developerdemocard.util.UriHelper;
import com.zebra.zebraui.ZebraButton;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraSpinnerView;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.zebra.developerdemocard.util.StorageHelper.handleRequestStoragePermissionsResult;

public class GraphicConversionDemoActivity extends AppCompatActivity implements ProcessImageTask.OnProcessImageListener {

    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2001;
    public static final int REQUEST_SELECT_SOURCE_GRAPHIC = 3001;

    private Uri sourceGraphicUri;
    private int sourceGraphicWidth = -1;
    private int sourceGraphicHeight = -1;
    private ProcessImageTask processImageTask;

    private ZebraEditText sourceGraphicFile;
    private ZebraEditText convertedGraphicFile;
    private ZebraSpinnerView graphicsFormats;
    private ZebraSpinnerView printerModels;
    private ZebraSpinnerView dimensionOptions;
    private ZebraEditText widthInput;
    private ZebraEditText heightInput;
    private ZebraEditText xOffsetInput;
    private ZebraEditText yOffsetInput;
    private TextView jobStatusLog;
    private ZebraButton convertButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_graphic_conversion);
        setTitle(R.string.graphic_conversion_demo);
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

        sourceGraphicFile = findViewById(R.id.sourceGraphicFile);
        ImageView sourceGraphicBrowseButton = findViewById(R.id.sourceGraphicBrowseButton);
        convertedGraphicFile = findViewById(R.id.convertedGraphicFile);
        ImageView convertedGraphicFileInfoButton = findViewById(R.id.convertedGraphicFileInfoButton);
        graphicsFormats = findViewById(R.id.graphicsFormats);
        printerModels = findViewById(R.id.printerModels);
        dimensionOptions = findViewById(R.id.dimensionOptions);
        widthInput = findViewById(R.id.widthInput);
        heightInput = findViewById(R.id.heightInput);
        xOffsetInput = findViewById(R.id.xOffsetInput);
        yOffsetInput = findViewById(R.id.yOffsetInput);
        jobStatusLog = findViewById(R.id.jobStatusLog);
        convertButton = findViewById(R.id.convertButton);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);

        sourceGraphicFile.getEditText().setFocusable(false);

        sourceGraphicBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(createGraphicSelectIntent(), REQUEST_SELECT_SOURCE_GRAPHIC);
            }
        });

        convertedGraphicFileInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.showConvertedGraphicFileInfoDialog(GraphicConversionDemoActivity.this);
            }
        });

        List<String> printerModelDisplayNames = new ArrayList<>();
        for (PrinterModelInfo printerModelInfo : PrinterModelInfo.values()) {
            printerModelDisplayNames.add(printerModelInfo.toString());
        }

        graphicsFormats.setSpinnerEntries(GraphicsFormat.getAllGraphicsFormatStrings(this));
        printerModels.setSpinnerEntries(printerModelDisplayNames);
        dimensionOptions.setSpinnerEntries(DimensionOption.getAllDimensionOptionStrings(this));
        dimensionOptions.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DimensionOption dimensionOption = DimensionOption.getDimensionOption(GraphicConversionDemoActivity.this, dimensionOptions.getSelectedItem().toString());
                switch (dimensionOption) {
                    case CROP:
                        setPixelInputsEnabled(true, true);
                        break;
                    case RESIZE:
                        setPixelInputsEnabled(true, false);
                        break;
                    case ORIGINAL:
                    default:
                        setPixelInputsEnabled(false, false);
                        break;
                }

                updateDimensionValues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPixelInputsEnabled(false, false);
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sourceGraphicFile.getText().trim().isEmpty()) {
                    DialogHelper.showErrorDialog(GraphicConversionDemoActivity.this, getString(R.string.value_required_for_field, getString(R.string.source_graphic_file)));
                    return;
                }

                if (convertedGraphicFile.getText().trim().isEmpty()) {
                    DialogHelper.showErrorDialog(GraphicConversionDemoActivity.this, getString(R.string.value_required_for_field, getString(R.string.converted_graphic_file)));
                    return;
                }

                if (!AsyncTaskHelper.isAsyncTaskRunning(processImageTask)) {
                    UIHelper.hideSoftKeyboard(GraphicConversionDemoActivity.this);
                    convertButton.setEnabled(false);

                    try {
                        int width = !widthInput.getText().isEmpty() ? Integer.parseInt(widthInput.getText()) : -1;
                        int height = !heightInput.getText().isEmpty() ? Integer.parseInt(heightInput.getText()) : -1;
                        int xOffset = !xOffsetInput.getText().isEmpty() ? Integer.parseInt(xOffsetInput.getText()) : -1;
                        int yOffset = !yOffsetInput.getText().isEmpty() ? Integer.parseInt(yOffsetInput.getText()) : -1;

                        GraphicConversionOptions graphicConversionOptions = new GraphicConversionOptions(sourceGraphicUri,
                                convertedGraphicFile.getText() + ".bmp",
                                GraphicsFormat.getGraphicsFormat(GraphicConversionDemoActivity.this, graphicsFormats.getSelectedItem().toString()),
                                PrinterModelInfo.getFromDisplayName(printerModels.getSelectedItem().toString()),
                                DimensionOption.getDimensionOption(GraphicConversionDemoActivity.this, dimensionOptions.getSelectedItem().toString()),
                                width,
                                height,
                                xOffset,
                                yOffset);

                        processImageTask = new ProcessImageTask(GraphicConversionDemoActivity.this, graphicConversionOptions);
                        processImageTask.setOnProcessImageListener(GraphicConversionDemoActivity.this);
                        processImageTask.execute();
                    } catch (NumberFormatException e) {
                        convertButton.setEnabled(true);

                        DialogHelper.showErrorDialog(GraphicConversionDemoActivity.this, getString(R.string.error_parsing_number_message, e.getMessage()));
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (processImageTask != null) {
            processImageTask.cancel(true);
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
        switch (requestCode) {
            case REQUEST_SELECT_SOURCE_GRAPHIC:
                if (resultCode == RESULT_OK) {
                    byte[] imageData = null;
                    Uri uri = data.getData();
                    if (uri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            if (inputStream != null) {
                                imageData = IOUtils.toByteArray(inputStream);
                            }
                        } catch (Exception e) {
                            imageData = null;
                        }
                    }

                    BitmapHelper.Dimensions dimensions = BitmapHelper.getBitmapDimensions(imageData);
                    String filename = UriHelper.getFilename(this, uri);

                    if (dimensions != null && filename != null && !filename.trim().isEmpty()) {
                        sourceGraphicWidth = dimensions.getWidth();
                        sourceGraphicHeight = dimensions.getHeight();
                        sourceGraphicUri = uri;
                        sourceGraphicFile.setText(filename);
                    } else {
                        sourceGraphicWidth = -1;
                        sourceGraphicHeight = -1;
                        sourceGraphicUri = null;
                        sourceGraphicFile.setText(null);
                    }

                    updateDimensionValues();
                }
                break;
        }
    }

    @Override
    public void onProcessImageStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.converting_graphic));

        jobStatusLog.setText(null);
    }

    @Override
    public void onProcessImageUpdate(String message) {
        JobStatusHelper.updateJobStatusLog(this, jobStatusLog, message);
    }

    @Override
    public void onProcessImageFinished(Exception exception) {
        convertButton.setEnabled(true);
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            String errorMessage = getString(R.string.error_converting_graphic_message, exception.getMessage());
            JobStatusHelper.updateJobStatusLog(this, jobStatusLog, errorMessage);
            DialogHelper.showErrorDialog(this, errorMessage);
        } else {
            UIHelper.showSnackbar(this, getString(R.string.graphic_conversion_job_finished_successfully));
        }
    }

    private Intent createGraphicSelectIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        return Intent.createChooser(getIntent, getString(R.string.select_graphic));
    }

    private void setPixelInputsEnabled(boolean areDimensionsEnabled, boolean areOffsetsEnabled) {
        widthInput.setEnabled(areDimensionsEnabled);
        heightInput.setEnabled(areDimensionsEnabled);
        xOffsetInput.setEnabled(areOffsetsEnabled);
        yOffsetInput.setEnabled(areOffsetsEnabled);
    }

    private void updateDimensionValues() {
        if (widthInput.isEnabled()) {
            widthInput.setText(sourceGraphicWidth != -1 ? Integer.toString(sourceGraphicWidth) : null);
        } else {
            widthInput.setText(null);
        }

        if (heightInput.isEnabled()) {
            heightInput.setText(sourceGraphicHeight != -1 ? Integer.toString(sourceGraphicHeight) : null);
        } else {
            heightInput.setText(null);
        }
    }
}
