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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TemplatePreviewFragment extends Fragment implements GetTemplatePreviewTask.OnGetTemplatePreviewListener {

    private TemplateDemoActivity templateDemoActivity;
    private ZebraCardTemplate zebraCardTemplate;
    private TemplatePreviewAdapter templatePreviewAdapter;
    private GetTemplatePreviewTask getTemplatePreviewTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_template_preview, container, false);

        templateDemoActivity = (TemplateDemoActivity) getActivity();
        zebraCardTemplate = templateDemoActivity.getZebraCardTemplate();

        templatePreviewAdapter = new TemplatePreviewAdapter(getActivity(), new ArrayList<TemplatePreview>());

        ListView previewList = rootView.findViewById(R.id.previewList);

        previewList.setEmptyView(rootView.findViewById(R.id.noPreviewAvailableMessage));
        previewList.setAdapter(templatePreviewAdapter);

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (getTemplatePreviewTask != null) {
            getTemplatePreviewTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public void onGetTemplatePreviewStarted() {
        templateDemoActivity.showProgressOverlay(getString(R.string.refreshing_preview));
        templatePreviewAdapter.clearTemplatePreviewList();
    }

    @Override
    public void onGetTemplatePreviewFinished(Exception exception, List<TemplatePreview> templatePreviewList) {
        templateDemoActivity.hideProgressOverlay();

        if (exception != null) {
            if (templateDemoActivity.getCurrentFragment() == this) {
                DialogHelper.showErrorDialog(getActivity(), getString(R.string.error_getting_template_preview_message, exception.getMessage()));
            }
        } else {
            templatePreviewAdapter.setTemplatePreviewList(templatePreviewList);
        }
    }

    public void generatePreview(String templateName, Map<String, String> variableData) {
        if (getTemplatePreviewTask != null) {
            getTemplatePreviewTask.cancel(true);
        }

        getTemplatePreviewTask = new GetTemplatePreviewTask(getActivity(), zebraCardTemplate, templateName, variableData);
        getTemplatePreviewTask.setOnGetTemplatePreviewListener(this);
        getTemplatePreviewTask.execute();
    }
}
