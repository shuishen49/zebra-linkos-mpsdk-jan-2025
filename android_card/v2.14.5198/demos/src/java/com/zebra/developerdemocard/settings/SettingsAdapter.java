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

package com.zebra.developerdemocard.settings;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zebra.developerdemocard.R;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraFieldView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SettingsAdapter extends BaseAdapter {

    private Context context;
    private List<PrinterSetting> completeSettingsList = new ArrayList<>();
    private List<PrinterSetting> filteredSettingsList;
    private String filterText;
    private HashMap<String, String> changedSettingsMap = new HashMap<>();
    private boolean isSettingText;

    public SettingsAdapter(Context context, List<PrinterSetting> settingsList) {
        this.context = context;
        filteredSettingsList = settingsList;
        completeSettingsList.addAll(settingsList);
    }

    @Override
    public int getCount() {
        return filteredSettingsList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredSettingsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context, R.layout.list_item_setting, null);

            viewHolder.zebraEditText = convertView.findViewById(R.id.zebraEditText);
            viewHolder.zebraFieldView = convertView.findViewById(R.id.zebraFieldView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PrinterSetting printerSetting = filteredSettingsList.get(position);
        if (printerSetting.isSettable()) {
            viewHolder.zebraEditText.setVisibility(View.VISIBLE);
            viewHolder.zebraEditText.setHeaderText(printerSetting.getSetting());

            isSettingText = true;
            viewHolder.zebraEditText.setText(changedSettingsMap.containsKey(printerSetting.getSetting()) ? changedSettingsMap.get(printerSetting.getSetting()) : printerSetting.getValue());
            isSettingText = false;

            viewHolder.zebraEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!isSettingText) {
                        String changedSetting = viewHolder.zebraEditText.getHeaderText();
                        String newValue = viewHolder.zebraEditText.getText();
                        if (newValue.equals(getSettingValue(changedSetting))) {
                            if (changedSettingsMap.containsKey(changedSetting)) {
                                changedSettingsMap.remove(changedSetting);
                            }
                        } else {
                            changedSettingsMap.put(changedSetting, newValue);
                        }
                    }
                }
            });
        } else {
            viewHolder.zebraFieldView.setVisibility(View.VISIBLE);
            viewHolder.zebraFieldView.setHeaderText(printerSetting.getSetting());
            viewHolder.zebraFieldView.setBodyText(printerSetting.getValue());
        }

        return convertView;
    }

    public void setSettingsList(List<PrinterSetting> printerSettings) {
        completeSettingsList.clear();
        filteredSettingsList.clear();
        changedSettingsMap.clear();

        if (printerSettings != null) {
            completeSettingsList = printerSettings;
        }

        notifyDataSetChanged();
        filter(filterText);
    }

    public void filter(String text) {
        text = text != null ? text : "";
        filterText = text.toLowerCase(Locale.getDefault());
        filteredSettingsList.clear();

        if (filterText.length() == 0) {
            filteredSettingsList.addAll(completeSettingsList);
        } else {
            for (PrinterSetting printerSetting : completeSettingsList) {
                String setting = printerSetting.getSetting();
                String value = printerSetting.getValue();
                if ((setting != null && setting.toLowerCase(Locale.getDefault()).contains(filterText)) ||
                        (value != null && value.toLowerCase(Locale.getDefault()).contains(filterText))) {
                    filteredSettingsList.add(printerSetting);
                }
            }
        }

        notifyDataSetChanged();
    }

    HashMap<String, String> getChangedSettingsMap() {
        return changedSettingsMap;
    }

    private String getSettingValue(String setting) {
        if (setting != null) {
            for (int i = 0; i < completeSettingsList.size(); i++) {
                PrinterSetting printerSetting = completeSettingsList.get(i);
                if (setting.equals(printerSetting.getSetting())) {
                    return printerSetting.getValue();
                }
            }
        }
        return null;
    }

    private class ViewHolder {
        private ZebraEditText zebraEditText;
        private ZebraFieldView zebraFieldView;
    }
}
