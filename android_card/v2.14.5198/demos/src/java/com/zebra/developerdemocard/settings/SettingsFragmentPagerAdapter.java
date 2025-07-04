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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private Map<SettingsGroup, Fragment> fragmentMap = new LinkedHashMap<>();

    SettingsFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;

        fragmentMap.put(SettingsGroup.DEVICE, new SettingsDeviceFragment());
        fragmentMap.put(SettingsGroup.PRINT, new SettingsPrintFragment());
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
        List<SettingsGroup> titleList = new ArrayList<>(fragmentMap.keySet());
        return titleList.get(position).getString(context);
    }
}
