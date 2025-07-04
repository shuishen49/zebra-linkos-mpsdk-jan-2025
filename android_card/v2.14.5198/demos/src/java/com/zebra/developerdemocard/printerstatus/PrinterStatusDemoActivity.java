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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.settings.PrinterSetting;
import com.zebra.developerdemocard.settings.SettingsAdapter;
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.common.card.containers.MediaInfo;
import com.zebra.sdk.common.card.containers.PrinterInfo;
import com.zebra.sdk.common.card.containers.PrinterStatusInfo;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrinterStatusDemoActivity extends AppCompatActivity implements RetrievePrinterStatusTask.OnRetrievePrinterStatusListener {

    private DiscoveredPrinter printer;
    private SettingsAdapter printerStatusPrinterAdapter;
    private SettingsAdapter printerStatusGeneralAdapter;
    private PrinterStatusMediaAdapter printerStatusMediaAdapter;
    private SettingsAdapter printerStatusSensorsAdapter;
    private RetrievePrinterStatusTask retrievePrinterStatusTask;

    private Animation rotation;
    private View refreshActionView;
    private LinearLayout progressOverlay;
    private TextView progressMessage;
    private EditText filterEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_printer_status);
        UIHelper.setLogoOnActionBar(this);
        setTitle(R.string.printer_status_demo);

        printer = SelectedPrinterManager.getSelectedPrinter();
        rotation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        printerStatusPrinterAdapter = new SettingsAdapter(this, new ArrayList<PrinterSetting>());
        printerStatusGeneralAdapter = new SettingsAdapter(this, new ArrayList<PrinterSetting>());
        printerStatusMediaAdapter = new PrinterStatusMediaAdapter(this, new ArrayList<MediaInfo>());
        printerStatusSensorsAdapter = new SettingsAdapter(this, new ArrayList<PrinterSetting>());

        PrinterStatusFragmentPagerAdapter fragmentPagerAdapter = new PrinterStatusFragmentPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        filterEditText = findViewById(R.id.filterEditText);

        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        filterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = filterEditText.getText().toString().toLowerCase(Locale.getDefault());
                printerStatusPrinterAdapter.filter(text);
                printerStatusGeneralAdapter.filter(text);
                printerStatusMediaAdapter.filter(text);
                printerStatusSensorsAdapter.filter(text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (retrievePrinterStatusTask != null) {
            retrievePrinterStatusTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo_printer_status, menu);

        MenuItem refreshItem = menu.findItem(R.id.refreshPrinterStatus);
        refreshItem.setActionView(R.layout.action_view_refresh);
        refreshActionView = refreshItem.getActionView();
        refreshActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshSettings();
            }
        });
        refreshItem.expandActionView();

        refreshSettings();

        return true;
    }

    @Override
    public void onRetrievePrinterStatusStarted() {
        refreshActionView.startAnimation(rotation);
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.refreshing_settings));
    }

    @Override
    public void onRetrievePrinterStatusFinished(Exception exception, @Nullable PrinterStatus printerStatus) {
        refreshActionView.clearAnimation();
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_retrieving_settings_message, exception.getMessage()));

            printerStatusPrinterAdapter.setSettingsList(null);
            printerStatusGeneralAdapter.setSettingsList(null);
            printerStatusMediaAdapter.setMediaInfoList(null);
            printerStatusSensorsAdapter.setSettingsList(null);
        } else if (printerStatus != null) {
            printerStatusPrinterAdapter.setSettingsList(buildPrinterSettingList(printerStatus.getPrinterInfo()));
            printerStatusGeneralAdapter.setSettingsList(buildGeneralSettingList(printerStatus.getPrinterStatusInfo()));
            printerStatusMediaAdapter.setMediaInfoList(printerStatus.getMediaInfoList());
            printerStatusSensorsAdapter.setSettingsList(buildSensorsSettingList(printerStatus.getSensorInfo()));
        } else {
            printerStatusPrinterAdapter.setSettingsList(null);
            printerStatusGeneralAdapter.setSettingsList(null);
            printerStatusMediaAdapter.setMediaInfoList(null);
            printerStatusSensorsAdapter.setSettingsList(null);
        }
    }

    private void refreshSettings() {
        if (!AsyncTaskHelper.isAsyncTaskRunning(retrievePrinterStatusTask)) {
            retrievePrinterStatusTask = new RetrievePrinterStatusTask(printer);
            retrievePrinterStatusTask.setOnRetrievePrinterStatusListener(this);
            retrievePrinterStatusTask.execute();
        }
    }

    private List<PrinterSetting> buildPrinterSettingList(PrinterInfo printerInfo) {
        List<PrinterSetting> settingList = new ArrayList<>();
        settingList.add(new PrinterSetting(getString(R.string.vendor), printerInfo.vendor));
        settingList.add(new PrinterSetting(getString(R.string.model), printerInfo.model));
        settingList.add(new PrinterSetting(getString(R.string.serial_number), printerInfo.serialNumber));
        settingList.add(new PrinterSetting(getString(R.string.mac_address), printerInfo.macAddress));
        settingList.add(new PrinterSetting(getString(R.string.printhead_serial_number), printerInfo.printheadSerialNumber));
        settingList.add(new PrinterSetting(getString(R.string.oem_code), printerInfo.oemCode));
        settingList.add(new PrinterSetting(getString(R.string.firmware_version), printerInfo.firmwareVersion));
        settingList.add(new PrinterSetting(getString(R.string.media_version), printerInfo.mediaVersion));
        settingList.add(new PrinterSetting(getString(R.string.heater_version), printerInfo.heaterVersion));
        settingList.add(new PrinterSetting(getString(R.string.zmotif_version), printerInfo.zmotifVersion));
        return settingList;
    }

    private List<PrinterSetting> buildGeneralSettingList(PrinterStatusInfo printerStatusInfo) {
        List<PrinterSetting> settingList = new ArrayList<>();
        settingList.add(new PrinterSetting(getString(R.string.status), printerStatusInfo.status));
        settingList.add(new PrinterSetting(getString(R.string.jobs_pending), Integer.toString(printerStatusInfo.jobsPending)));
        settingList.add(new PrinterSetting(getString(R.string.jobs_active), Integer.toString(printerStatusInfo.jobsActive)));
        settingList.add(new PrinterSetting(getString(R.string.jobs_complete), Integer.toString(printerStatusInfo.jobsComplete)));
        settingList.add(new PrinterSetting(getString(R.string.job_errors), Integer.toString(printerStatusInfo.jobErrors)));
        settingList.add(new PrinterSetting(getString(R.string.jobs_total), Integer.toString(printerStatusInfo.jobsTotal)));
        settingList.add(new PrinterSetting(getString(R.string.next_job_id), Integer.toString(printerStatusInfo.nextJobID)));
        settingList.add(new PrinterSetting(getString(R.string.alarm_info), printerStatusInfo.alarmInfo.value != 0 ? printerStatusInfo.alarmInfo.description : getString(R.string.none)));
        settingList.add(new PrinterSetting(getString(R.string.error_info), printerStatusInfo.errorInfo.value != 0 ? printerStatusInfo.errorInfo.description : getString(R.string.none)));
        return settingList;
    }

    private List<PrinterSetting> buildSensorsSettingList(Map<String, String> sensorInfo) {
        List<PrinterSetting> settingList = new ArrayList<>();
        for (String key : sensorInfo.keySet()) {
            settingList.add(new PrinterSetting(key, sensorInfo.get(key)));
        }
        return settingList;
    }

    public SettingsAdapter getPrinterStatusPrinterAdapter() {
        return printerStatusPrinterAdapter;
    }

    public SettingsAdapter getPrinterStatusGeneralAdapter() {
        return printerStatusGeneralAdapter;
    }

    public PrinterStatusMediaAdapter getPrinterStatusMediaAdapter() {
        return printerStatusMediaAdapter;
    }

    public SettingsAdapter getPrinterStatusSensorsAdapter() {
        return printerStatusSensorsAdapter;
    }
}
