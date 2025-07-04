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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrinterStatusFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private Map<PrinterStatusGroup, Fragment> fragmentMap = new LinkedHashMap<>();

    PrinterStatusFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;

        fragmentMap.put(PrinterStatusGroup.PRINTER, new PrinterStatusPrinterFragment());
        fragmentMap.put(PrinterStatusGroup.GENERAL, new PrinterStatusGeneralFragment());
        fragmentMap.put(PrinterStatusGroup.MEDIA, new PrinterStatusMediaFragment());
        fragmentMap.put(PrinterStatusGroup.SENSORS, new PrinterStatusSensorsFragment());
    }

    @Override
    public Fragment getItem(int position) {
        List<Fragment> fragmentList = new ArrayList<>(fragmentMap.values());
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentMap.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        List<PrinterStatusGroup> titleList = new ArrayList<>(fragmentMap.keySet());
        return titleList.get(position).getString(context);
    }
}
