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

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.discovery.SelectedPrinterManager;
import com.zebra.developerdemocard.util.AsyncTaskHelper;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.ProgressOverlayHelper;
import com.zebra.developerdemocard.util.StorageHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.sdk.common.card.template.ZebraCardTemplate;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.zebraui.ZebraButton;

import static com.zebra.developerdemocard.util.StorageHelper.handleRequestStoragePermissionsResult;

public class TemplateDemoActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2001;

    private ZebraCardTemplate zebraCardTemplate = new ZebraCardTemplate(this, null);
    private DiscoveredPrinter printer;
    private TemplateFragmentPagerAdapter templateFragmentPagerAdapter;
    private Fragment currentFragment;
    private SendTemplateJobTask sendTemplateJobTask;

    private LinearLayout progressOverlay;
    private TextView progressMessage;
    private ZebraButton printButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_template);
        setTitle(R.string.template_demo);
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

        templateFragmentPagerAdapter = new TemplateFragmentPagerAdapter(this, getSupportFragmentManager());
        currentFragment = templateFragmentPagerAdapter.getItem(0);

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressMessage = findViewById(R.id.progressMessage);
        ZebraButton cancelButton = findViewById(R.id.cancelButton);
        printButton = findViewById(R.id.printButton);

        viewPager.setAdapter(templateFragmentPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                if (templateFragmentPagerAdapter.getItem(position) == templateFragmentPagerAdapter.getTemplatePreviewFragment()) {
                    UIHelper.hideSoftKeyboard(TemplateDemoActivity.this);

                    templateFragmentPagerAdapter.getTemplatePreviewFragment().generatePreview(templateFragmentPagerAdapter.getTemplateFragment().getTemplateName(), templateFragmentPagerAdapter.getTemplateFragment().getVariableData());
                }

                currentFragment = templateFragmentPagerAdapter.getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        tabLayout.setupWithViewPager(viewPager);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWaitingForAsyncTask()) {
                    UIHelper.hideSoftKeyboard(TemplateDemoActivity.this);
                    printButton.setEnabled(false);

                    TemplateJobFragment templateFragment = templateFragmentPagerAdapter.getTemplateFragment();

                    sendTemplateJobTask = new SendTemplateJobTask(TemplateDemoActivity.this, printer, zebraCardTemplate, templateFragment.getTemplateName(), templateFragment.getVariableData());
                    sendTemplateJobTask.setOnSendTemplateJobListener(templateFragment);
                    sendTemplateJobTask.execute();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (sendTemplateJobTask != null) {
            sendTemplateJobTask.cancel(true);
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

    public boolean isWaitingForAsyncTask() {
        return AsyncTaskHelper.isAsyncTaskRunning(sendTemplateJobTask);
    }

    public void showProgressOverlay(String message) {
        ProgressOverlayHelper.showProgressOverlay(progressMessage, progressOverlay, message);
    }

    public void hideProgressOverlay() {
        ProgressOverlayHelper.hideProgressOverlay(progressMessage, progressOverlay);
    }

    public void enablePrintButton() {
        printButton.setEnabled(true);
    }

    public ZebraCardTemplate getZebraCardTemplate() {
        return zebraCardTemplate;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}
