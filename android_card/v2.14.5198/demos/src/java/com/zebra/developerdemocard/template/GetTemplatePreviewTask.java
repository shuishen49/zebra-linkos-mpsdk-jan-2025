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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.zebra.developerdemocard.R;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GetTemplatePreviewTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> weakContext;
    private ZebraCardTemplate zebraCardTemplate;
    private String templateName;
    private Map<String, String> variableData;
    private OnGetTemplatePreviewListener onGetTemplatePreviewListener;
    private List<TemplatePreview> templatePreviewList = new LinkedList<>();
    private Exception exception;

    public interface OnGetTemplatePreviewListener {
        void onGetTemplatePreviewStarted();
        void onGetTemplatePreviewFinished(Exception exception, List<TemplatePreview> templatePreviewList);
    }

    GetTemplatePreviewTask(Context context, ZebraCardTemplate zebraCardTemplate, String templateName, Map<String, String> variableData) {
        weakContext = new WeakReference<>(context);
        this.zebraCardTemplate = zebraCardTemplate;
        this.templateName = templateName;
        this.variableData = variableData;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onGetTemplatePreviewListener != null) {
            onGetTemplatePreviewListener.onGetTemplatePreviewStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (templateName != null) {
                List<GraphicsInfo> graphicsData = zebraCardTemplate.generateTemplateJob(templateName, variableData).graphicsData;
                for (GraphicsInfo info : graphicsData) {
                    if (isCancelled()) {
                        break;
                    }

                    if (info.graphicData != null) {
                        byte[] imageData = info.graphicData.getImageData();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                        templatePreviewList.add(new TemplatePreview(weakContext.get().getString(R.string.card_side_and_type, info.side, info.printType), bitmap));
                        bitmap.recycle();
                    } else {
                        templatePreviewList.add(new TemplatePreview(weakContext.get().getString(R.string.card_side_and_type, info.side, info.printType), weakContext.get().getString(R.string.no_image_data_found)));
                    }
                }
            } else {
                throw new IllegalArgumentException(weakContext.get().getString(R.string.no_template_file_selected));
            }
        } catch (ZebraCardException e) {
            exception = new ZebraCardException(weakContext.get().getString(R.string.invalid_template_file_selected), e);
        } catch (Exception e) {
            exception = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onGetTemplatePreviewListener != null) {
            onGetTemplatePreviewListener.onGetTemplatePreviewFinished(exception, templatePreviewList);
        }
    }

    void setOnGetTemplatePreviewListener(OnGetTemplatePreviewListener onGetTemplatePreviewListener) {
        this.onGetTemplatePreviewListener = onGetTemplatePreviewListener;
    }
}
