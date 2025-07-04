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

package com.zebra.developerdemocard.printerstatus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.sdk.common.card.containers.MediaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrinterStatusMediaAdapter extends BaseAdapter {

    private Context context;
    private List<MediaInfo> completeMediaInfoList;
    private List<MediaInfo> filteredMediaInfoList;
    private String filterText;

    PrinterStatusMediaAdapter(Context context, @NonNull List<MediaInfo> mediaInfoList) {
        this.context = context;
        filteredMediaInfoList = mediaInfoList;
        completeMediaInfoList = new ArrayList<>();
        completeMediaInfoList.addAll(mediaInfoList);
    }

    @Override
    public int getCount() {
        return filteredMediaInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredMediaInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.list_item_printer_status_media, null);

            viewHolder = new ViewHolder();
            viewHolder.type = convertView.findViewById(R.id.type);
            viewHolder.initialSize = convertView.findViewById(R.id.initialSize);
            viewHolder.panelsRemaining = convertView.findViewById(R.id.panelsRemaining);
            viewHolder.description = convertView.findViewById(R.id.description);
            viewHolder.oemCode = convertView.findViewById(R.id.oemCode);
            viewHolder.partNumber = convertView.findViewById(R.id.partNumber);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        MediaInfo mediaInfo = filteredMediaInfoList.get(i);

        viewHolder.type.setText(mediaInfo.type != null ? mediaInfo.type.name() : null);
        viewHolder.initialSize.setText(formatSize(mediaInfo.initialSize));
        viewHolder.panelsRemaining.setText(formatSize(mediaInfo.panelsRemaining));
        viewHolder.description.setText(mediaInfo.description);
        viewHolder.oemCode.setText(mediaInfo.oemCode);
        viewHolder.partNumber.setText(mediaInfo.partNumber);

        return convertView;
    }

    void setMediaInfoList(List<MediaInfo> mediaInfoList) {
        completeMediaInfoList.clear();
        filteredMediaInfoList.clear();

        if (mediaInfoList != null) {
            completeMediaInfoList = mediaInfoList;
        }

        notifyDataSetChanged();
        filter(filterText);
    }

    void filter(String text) {
        text = text != null ? text : "";
        filterText = text.toLowerCase(Locale.getDefault());
        filteredMediaInfoList.clear();

        if (filterText.length() == 0) {
            filteredMediaInfoList.addAll(completeMediaInfoList);
        } else {
            for (MediaInfo mediaInfo : completeMediaInfoList) {
                if (matchesFilterText(mediaInfo)) {
                    filteredMediaInfoList.add(mediaInfo);
                }
            }
        }

        notifyDataSetChanged();
    }

    private boolean matchesFilterText(MediaInfo mediaInfo) {
        return context.getString(R.string.type).toLowerCase(Locale.getDefault()).contains(filterText) ||
                context.getString(R.string.initial_size).toLowerCase(Locale.getDefault()).contains(filterText) ||
                context.getString(R.string.panels_remaining).toLowerCase(Locale.getDefault()).contains(filterText) ||
                context.getString(R.string.description).toLowerCase(Locale.getDefault()).contains(filterText) ||
                context.getString(R.string.oem_code).toLowerCase(Locale.getDefault()).contains(filterText) ||
                context.getString(R.string.part_number).toLowerCase(Locale.getDefault()).contains(filterText) ||
                (mediaInfo.type != null && mediaInfo.type.name().toLowerCase(Locale.getDefault()).contains(filterText)) ||
                formatSize(mediaInfo.initialSize).toLowerCase(Locale.getDefault()).contains(filterText) ||
                formatSize(mediaInfo.panelsRemaining).toLowerCase(Locale.getDefault()).contains(filterText) ||
                (mediaInfo.description != null && mediaInfo.description.toLowerCase(Locale.getDefault()).contains(filterText)) ||
                (mediaInfo.oemCode != null && mediaInfo.oemCode.toLowerCase(Locale.getDefault()).contains(filterText)) ||
                (mediaInfo.partNumber != null && mediaInfo.partNumber.toLowerCase(Locale.getDefault()).contains(filterText));
    }

    private String formatSize(Integer size) {
        return size != null ? String.format(Locale.getDefault(), "%d", size) : "";
    }

    private class ViewHolder {
        TextView type;
        TextView initialSize;
        TextView panelsRemaining;
        TextView description;
        TextView oemCode;
        TextView partNumber;
    }
}
