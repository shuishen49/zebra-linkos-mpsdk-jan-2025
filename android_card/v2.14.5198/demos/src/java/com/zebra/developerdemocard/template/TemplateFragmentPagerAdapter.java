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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zebra.developerdemocard.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateFragmentPagerAdapter extends FragmentPagerAdapter {

    private Map<String, Fragment> fragmentMap = new LinkedHashMap<>();
    private TemplateJobFragment templateFragment;
    private TemplatePreviewFragment templatePreviewFragment;

    TemplateFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        templateFragment = new TemplateJobFragment();
        templatePreviewFragment = new TemplatePreviewFragment();

        fragmentMap.put(context.getString(R.string.template), templateFragment);
        fragmentMap.put(context.getString(R.string.preview), templatePreviewFragment);
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
        List<String> titleList = new ArrayList<>(fragmentMap.keySet());
        return titleList.get(position);
    }

    TemplateJobFragment getTemplateFragment() {
        return templateFragment;
    }

    TemplatePreviewFragment getTemplatePreviewFragment() {
        return templatePreviewFragment;
    }
}
