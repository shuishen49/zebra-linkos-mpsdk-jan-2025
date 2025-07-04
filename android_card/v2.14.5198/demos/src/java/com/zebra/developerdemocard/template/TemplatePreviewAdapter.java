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
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zebra.developerdemocard.R;

import java.util.List;

public class TemplatePreviewAdapter extends BaseAdapter {

    private Context context;
    private List<TemplatePreview> templatePreviewList;

    TemplatePreviewAdapter(Context context, @NonNull List<TemplatePreview> templatePreviewList) {
        this.context = context;
        this.templatePreviewList = templatePreviewList;
    }

    @Override
    public int getCount() {
        return templatePreviewList.size();
    }

    @Override
    public Object getItem(int i) {
        return templatePreviewList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_template_preview, null);

            viewHolder = new ViewHolder();
            viewHolder.previewLabel = convertView.findViewById(R.id.previewLabel);
            viewHolder.previewImage = convertView.findViewById(R.id.previewImage);
            viewHolder.previewMessage = convertView.findViewById(R.id.previewMessage);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TemplatePreview templatePreview = templatePreviewList.get(i);

        viewHolder.previewLabel.setText(templatePreview.getLabel());

        Bitmap bitmap = templatePreview.getBitmap();
        viewHolder.previewImage.setImageBitmap(bitmap);
        viewHolder.previewImage.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);

        String message = templatePreview.getMessage();
        viewHolder.previewMessage.setText(message);
        viewHolder.previewMessage.setVisibility(message != null ? View.VISIBLE : View.GONE);

        return convertView;
    }

    void setTemplatePreviewList(List<TemplatePreview> templatePreviewList) {
        this.templatePreviewList.clear();

        if (templatePreviewList != null) {
            this.templatePreviewList = templatePreviewList;
        }

        notifyDataSetChanged();
    }

    void clearTemplatePreviewList() {
        this.templatePreviewList.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView previewLabel;
        ImageView previewImage;
        TextView previewMessage;
    }
}
