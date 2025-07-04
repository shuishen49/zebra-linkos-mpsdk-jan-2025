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

import android.content.DialogInterface;
import android.content.Intent;
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
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsDemoActivity extends AppCompatActivity implements RetrieveSettingsTask.OnRetrieveSettingsListener,
        SaveSettingsTask.OnSaveSettingsListener {

    public static final String KEY_RESET_PRINTER = "KEY_RESET_PRINNTER";

    private DiscoveredPrinter printer;
    private SettingsAdapter settingsDeviceAdapter;
    private SettingsAdapter settingsPrintAdapter;
    private Animation rotation;
    private RetrieveSettingsTask retrieveSettingsTask;
    private SaveSettingsTask saveSettingsTask;

    private View refreshActionView;
    private EditText filterEditText;
    private ZebraButton saveChangesButton;
    private LinearLayout progressOverlay;
    private TextView progressMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_settings);
        UIHelper.setLogoOnActionBar(this);
        setTitle(R.string.settings_demo);

        printer = SelectedPrinterManager.getSelectedPrinter();

        settingsDeviceAdapter = new SettingsAdapter(this, new ArrayList<PrinterSetting>());
        settingsPrintAdapter = new SettingsAdapter(this, new ArrayList<PrinterSetting>());
        rotation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        SettingsFragmentPagerAdapter fragmentPagerAdapter = new SettingsFragmentPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        filterEditText = findViewById(R.id.filterEditText);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        ZebraButton cancelButton = findViewById(R.id.cancelButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

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
                settingsDeviceAdapter.filter(text);
                settingsPrintAdapter.filter(text);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWaitingForAsyncTask()) {
                    saveChangesButton.setEnabled(false);

                    UIHelper.hideSoftKeyboard(SettingsDemoActivity.this);

                    final List<String> changedResetRequiredSettings = new ArrayList<>();
                    final Map<String, String> changedSettingsMap = getSettingsDeviceAdapter().getChangedSettingsMap();
                    if (changedSettingsMap != null) {
                        for (String setting : changedSettingsMap.keySet()) {
                            if (SaveSettingsTask.SGD_LIST_NETWORK_RESET_REQUIRED.contains(setting) || SaveSettingsTask.SGD_LIST_PRINTER_RESET_REQUIRED.contains(setting)) {
                                changedResetRequiredSettings.add(setting);
                            }
                        }
                    }

                    // Check if any changed reset-required settings are contained in the printer reset-required settings list
                    final boolean resetPrinter = !Collections.disjoint(SaveSettingsTask.SGD_LIST_PRINTER_RESET_REQUIRED, changedResetRequiredSettings);

                    if (changedResetRequiredSettings.size() > 0) {
                        DialogHelper.createContinuePrinterResetDialog(SettingsDemoActivity.this, changedResetRequiredSettings, resetPrinter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (saveSettingsTask != null) {
                                    saveSettingsTask.cancel(true);
                                }

                                saveSettingsTask = new SaveSettingsTask(printer, changedSettingsMap, resetPrinter);
                                saveSettingsTask.setOnSaveSettingsListener(SettingsDemoActivity.this);
                                saveSettingsTask.execute();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveChangesButton.setEnabled(true);
                                dialogInterface.dismiss();
                            }
                        }).show();
                    } else {
                        if (saveSettingsTask != null) {
                            saveSettingsTask.cancel(true);
                        }

                        saveSettingsTask = new SaveSettingsTask(printer, changedSettingsMap, false);
                        saveSettingsTask.setOnSaveSettingsListener(SettingsDemoActivity.this);
                        saveSettingsTask.execute();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (retrieveSettingsTask != null) {
            retrieveSettingsTask.cancel(true);
        }

        if (saveSettingsTask != null) {
            saveSettingsTask.cancel(true);
        }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_demo_settings, menu);

        MenuItem refreshItem = menu.findItem(R.id.refreshSettings);
        refreshItem.setActionView(R.layout.action_view_refresh);
        refreshActionView = refreshItem.getActionView();
        refreshActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWaitingForAsyncTask()) {
                    refreshSettings();
                }
            }
        });
        refreshItem.expandActionView();

        refreshSettings();

        return true;
    }

    @Override
    public void onRetrieveSettingsStarted() {
        refreshActionView.startAnimation(rotation);
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.refreshing_settings));
    }

    @Override
    public void onRetrieveSettingsFinished(Exception exception, Map<SettingsGroup, List<PrinterSetting>> printerSettings) {
        saveChangesButton.setEnabled(true);
        refreshActionView.clearAnimation();
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_retrieving_settings_message, exception.getMessage()));

            settingsDeviceAdapter.setSettingsList(null);
            settingsPrintAdapter.setSettingsList(null);
        } else {
            settingsDeviceAdapter.setSettingsList(printerSettings.get(SettingsGroup.DEVICE));
            settingsPrintAdapter.setSettingsList(printerSettings.get(SettingsGroup.PRINT));
        }

    }

    @Override
    public void onSaveSettingsStarted() {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, getString(R.string.saving_settings));
    }

    @Override
    public void onSaveSettingsFinished(Exception exception, List<String> resetRequiredSettings, final boolean resetPrinter) {
        saveChangesButton.setEnabled(true);
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);

        if (exception != null) {
            DialogHelper.showErrorDialog(this, getString(R.string.error_saving_settings_message, exception.getMessage()));
        } else if (resetRequiredSettings != null && resetRequiredSettings.size() > 0) {
            DialogHelper.createPrinterResetDialog(this, resetRequiredSettings, resetPrinter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.putExtra(KEY_RESET_PRINTER, resetPrinter);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }).show();
        } else {
            UIHelper.showSnackbar(this, getString(R.string.settings_successfully_saved_message));

            refreshSettings();
        }
    }

    private boolean isWaitingForAsyncTask() {
        return AsyncTaskHelper.isAsyncTaskRunning(retrieveSettingsTask) || AsyncTaskHelper.isAsyncTaskRunning(saveSettingsTask);
    }

    private void refreshSettings () {
        saveChangesButton.setEnabled(false);

        if (retrieveSettingsTask != null) {
            retrieveSettingsTask.cancel(true);
        }

        retrieveSettingsTask = new RetrieveSettingsTask(printer);
        retrieveSettingsTask.setOnRetrieveSettingsListener(this);
        retrieveSettingsTask.execute();
    }

    public SettingsAdapter getSettingsDeviceAdapter() {
        return settingsDeviceAdapter;
    }

    public SettingsAdapter getSettingsPrintAdapter() {
        return settingsPrintAdapter;
    }
}
