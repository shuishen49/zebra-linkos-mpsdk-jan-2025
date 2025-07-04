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

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.jobstatus.JobInfo;
import com.zebra.developerdemocard.jobstatus.PollJobStatusTask;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.JobStatusHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.developerdemocard.util.UriHelper;
import com.zebra.sdk.common.card.containers.JobStatusInfo;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.zebraui.ZebraEditText;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class TemplateJobFragment extends Fragment implements SendTemplateJobTask.OnSendTemplateJobListener,
        PollJobStatusTask.OnJobStatusPollListener {

    public static final int REQUEST_SELECT_TEMPLATE_AND_GRAPHICS_FILES = 3001;

    private TemplateDemoActivity templateDemoActivity;
    private ZebraCardTemplate zebraCardTemplate;
    private Map<String, View> templateGraphicViewMap = new LinkedHashMap<>();
    private PollJobStatusTask pollJobStatusTask;
    private AlertDialog insertCardDialog;

    private String templateName;
    private Map<String, String> variableData = new HashMap<>();

    private FrameLayout templateFileContainer;
    private TextView templateXmlFileName;
    private TextView noTemplateSelectedEmptyView;
    private LinearLayout templateGraphicsList;
    private TextView noGraphicFilesSelectedEmptyView;
    private LinearLayout templateFields;
    private TextView jobStatusText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_template_job, container, false);

        templateDemoActivity = (TemplateDemoActivity) getActivity();
        zebraCardTemplate = templateDemoActivity.getZebraCardTemplate();

        templateFileContainer = rootView.findViewById(R.id.templateFileContainer);
        ImageView templateXmlFileImage = templateFileContainer.findViewById(R.id.templateFileImage);
        templateXmlFileName = templateFileContainer.findViewById(R.id.templateFileName);
        noTemplateSelectedEmptyView = rootView.findViewById(R.id.noTemplateSelectedEmptyView);
        templateGraphicsList = rootView.findViewById(R.id.templateGraphicsList);
        noGraphicFilesSelectedEmptyView = rootView.findViewById(R.id.noGraphicFilesSelectedEmptyView);
        templateFields = rootView.findViewById(R.id.templateFields);
        jobStatusText = rootView.findViewById(R.id.jobStatusText);

        templateXmlFileImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_code));

        rootView.findViewById(R.id.selectTemplateAddGraphicButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(createTemplateFileSelectIntent(), REQUEST_SELECT_TEMPLATE_AND_GRAPHICS_FILES);
            }
        });

        resetTemplateGraphicFiles();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!templateDemoActivity.isWaitingForAsyncTask()) {
            if (insertCardDialog != null && insertCardDialog.isShowing()) {
                insertCardDialog.dismiss();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (pollJobStatusTask != null) {
            pollJobStatusTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_TEMPLATE_AND_GRAPHICS_FILES:
                if (resultCode == RESULT_OK) {
                    ClipData clipData = data.getClipData();
                    if (clipData == null) { // Handle one file selected
                        Uri uri = data.getData();
                        if (UriHelper.isXmlFile(getActivity(), uri)) {
                            updateSelectedTemplate(uri);
                        } else {
                            addTemplateGraphic(uri);
                        }
                    } else { // Handle multiple files selected
                        List<Uri> xmlFiles = new ArrayList<>();
                        List<Uri> imageFiles = new ArrayList<>();

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            if (UriHelper.isXmlFile(getActivity(), uri)) {
                                xmlFiles.add(uri);
                            } else {
                                imageFiles.add(uri);
                            }
                        }

                        if (xmlFiles.size() > 1) {
                            updateSelectedTemplate(null);
                            DialogHelper.showErrorDialog(getActivity(), getString(R.string.please_select_only_one_template_file));
                        } else if (xmlFiles.size() == 1) {
                            updateSelectedTemplate(xmlFiles.get(0));
                        }

                        for (Uri uri : imageFiles) {
                            addTemplateGraphic(uri);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onSendTemplateJobStarted() {
        templateDemoActivity.showProgressOverlay(getString(R.string.sending_print_job_to_printer));
        jobStatusText.setText(null);
    }

    @Override
    public void onPrinterReadyUpdate(String message, boolean showDialog) {
        JobStatusHelper.updateJobStatusLog(getActivity(), jobStatusText, message);

        if (showDialog) {
            DialogHelper.showErrorDialog(getActivity(), message);
        }
    }

    @Override
    public void onSendTemplateJobFinished(Exception exception, Integer jobId, CardSource cardSource) {
        templateDemoActivity.hideProgressOverlay();

        if (exception != null) {
            templateDemoActivity.enablePrintButton();

            String errorMessage = getString(R.string.error_printing_card_message, exception.getMessage());
            JobStatusHelper.updateJobStatusLog(getActivity(), jobStatusText, errorMessage);
            DialogHelper.showErrorDialog(getActivity(), errorMessage);
        } else if (jobId != null) {
            UIHelper.showSnackbar(getActivity(), getString(R.string.print_job_sent_successfully));

            if (pollJobStatusTask != null) {
                pollJobStatusTask.cancel(true);
            }

            pollJobStatusTask = new PollJobStatusTask(getActivity(), SelectedPrinterManager.getSelectedPrinter(), new JobInfo(jobId, cardSource));
            pollJobStatusTask.setOnJobStatusPollListener(this);
            pollJobStatusTask.execute();
        } else {
            templateDemoActivity.enablePrintButton();
        }
    }

    @Override
    public void onJobStatusUpdate(JobInfo jobInfo, JobStatusInfo jobStatusInfo, String message) {
        JobStatusHelper.updateJobStatusLog(getActivity(), jobInfo != null ? jobInfo.getJobId() : null, jobStatusText, message);
    }

    @Override
    public void onJobStatusUserInputRequired(String title, String message, String positiveButtonText, String negativeButtonText, final PollJobStatusTask.OnUserInputListener onUserInputListener) {
        DialogHelper.showAlarmEncounteredDialog(getActivity(), title, message, positiveButtonText, negativeButtonText, new DialogInterface.OnClickListener() {
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (insertCardDialog != null && insertCardDialog.isShowing()) {
                    insertCardDialog.dismiss();
                }

                insertCardDialog = DialogHelper.createInsertCardDialog(getActivity());
                insertCardDialog.show();
            }
        });
    }

    @Override
    public void onJobStatusPollFinished(Exception exception) {
        templateDemoActivity.enablePrintButton();

        if (insertCardDialog != null && insertCardDialog.isShowing()) {
            insertCardDialog.dismiss();
        }

        if (exception != null) {
            DialogHelper.showErrorDialog(getActivity(), getString(R.string.error_polling_job_status_message, exception.getMessage()));
        }
    }

    private String getTemplateDataString(Uri uri) throws IOException {
        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            return IOUtils.toString(inputStream);
        }
        throw new IOException(getString(R.string.could_not_open_input_stream_for_uri, uri.toString()));
    }

    private byte[] getTemplateImageDataByteArray(Uri uri) throws IOException {
        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            return IOUtils.toByteArray(inputStream);
        }
        throw new IOException(getString(R.string.could_not_open_input_stream_for_uri, uri.toString()));
    }

    private void updateSelectedTemplateView(String filename) {
        templateXmlFileName.setText(filename);
        templateFileContainer.setVisibility(filename != null ? View.VISIBLE : View.GONE);
        noTemplateSelectedEmptyView.setVisibility(filename != null ? View.GONE : View.VISIBLE);
    }

    private View createTemplateVariableView(final String templateField) {
        final ZebraEditText view = (ZebraEditText) getLayoutInflater().inflate(R.layout.list_item_template_field, templateFields, false);
        view.setHeaderText(templateField);
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = view.getText();
                if (!text.isEmpty()) {
                    variableData.put(templateField, text);
                } else if (variableData.containsKey(templateField)) {
                    variableData.remove(templateField);
                }
            }
        });
        return view;
    }

    private void updateSelectedTemplate(Uri uri) {
        templateFields.removeAllViews();
        variableData.clear();

        String filename = UriHelper.getFilename(getActivity(), uri);
        if (filename != null) {
            updateSelectedTemplateView(filename);
            try {
                templateName = FilenameUtils.removeExtension(filename);

                List<String> storedTemplateNames = zebraCardTemplate.getAllTemplateNames();
                if (storedTemplateNames.contains(templateName)) {
                    zebraCardTemplate.deleteTemplate(templateName);
                }

                zebraCardTemplate.saveTemplate(templateName, getTemplateDataString(uri));

                List<String> templateFieldList = zebraCardTemplate.getTemplateFields(templateName);
                for (final String templateField : templateFieldList) {
                    templateFields.addView(createTemplateVariableView(templateField));
                }
            } catch (Exception e) {
                updateSelectedTemplateView(null);

                String errorMessage = e instanceof ZebraCardException ? getString(R.string.invalid_template_file_selected) : e.getMessage();
                DialogHelper.showErrorDialog(getActivity(), getString(R.string.error_selecting_template_message, errorMessage));
            }
        } else {
            updateSelectedTemplateView(null);
        }
    }

    private View createTemplateGraphicView(String filename) {
        View view = getLayoutInflater().inflate(R.layout.list_item_template_file, templateGraphicsList, false);
        ImageView templateFileImage = view.findViewById(R.id.templateFileImage);
        TextView templateFileName = view.findViewById(R.id.templateFileName);
        templateFileImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_image));
        templateFileName.setText(filename);
        return view;
    }

    private void addTemplateGraphicViewIfNotExisting(String imageName) {
        if (!templateGraphicViewMap.containsKey(imageName)) {
            templateGraphicViewMap.put(imageName, createTemplateGraphicView(imageName));
            templateGraphicsList.addView(templateGraphicViewMap.get(imageName));

            noGraphicFilesSelectedEmptyView.setVisibility(View.GONE);
        }
    }

    private void addTemplateGraphic(Uri uri) {
        try {
            String filename = UriHelper.getFilename(getActivity(), uri);

            List<String> existingTemplateImageNames = zebraCardTemplate.getAllTemplateImageNames();
            if (existingTemplateImageNames.contains(filename)) {
                zebraCardTemplate.deleteTemplateImage(filename);
            }

            byte[] imageData = getTemplateImageDataByteArray(uri);
            zebraCardTemplate.saveTemplateImage(filename, imageData);

            addTemplateGraphicViewIfNotExisting(filename);
        } catch (Exception e) {
            DialogHelper.showErrorDialog(getActivity(), getString(R.string.error_adding_template_graphic_message, e.getMessage()));
        }
    }

    private void resetTemplateGraphicFiles() {
        try {
            List<String> existingTemplateImageNames = zebraCardTemplate.getAllTemplateImageNames();
            for (String imageName : existingTemplateImageNames) {
                zebraCardTemplate.deleteTemplateImage(imageName);
            }
        } catch (Exception e) {
            DialogHelper.showErrorDialog(getActivity(), getString(R.string.error_resetting_template_graphics, e.getMessage()));
        }
    }

    private Intent createTemplateFileSelectIntent() {
        String[] mimeTypes = {"text/xml", "image/*"};
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        return Intent.createChooser(getIntent, getString(R.string.select_template_and_graphic_files));
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, String> getVariableData() {
        return variableData;
    }
}
