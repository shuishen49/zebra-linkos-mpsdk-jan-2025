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

package com.zebra.developerdemocard.multijob;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zebra.developerdemocard.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiJobFragmentPagerAdapter extends FragmentPagerAdapter {

    private Map<String, Fragment> fragmentMap = new LinkedHashMap<>();
    private MultiJobPrintFragment multiJobPrintFragment;
    private MultiJobFormFragment multiJob1Fragment;
    private MultiJobFormFragment multiJob2Fragment;
    private MultiJobFormFragment multiJob3Fragment;
    private MultiJobFormFragment multiJob4Fragment;

    MultiJobFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        multiJobPrintFragment = new MultiJobPrintFragment();
        multiJob1Fragment = new MultiJobFormFragment().setJobNumber(MultiJobNumber.ONE);
        multiJob2Fragment = new MultiJobFormFragment().setJobNumber(MultiJobNumber.TWO);
        multiJob3Fragment = new MultiJobFormFragment().setJobNumber(MultiJobNumber.THREE);
        multiJob4Fragment = new MultiJobFormFragment().setJobNumber(MultiJobNumber.FOUR);

        fragmentMap.put(context.getString(R.string.overview), multiJobPrintFragment);
        fragmentMap.put(context.getString(R.string.job_1), multiJob1Fragment);
        fragmentMap.put(context.getString(R.string.job_2), multiJob2Fragment);
        fragmentMap.put(context.getString(R.string.job_3), multiJob3Fragment);
        fragmentMap.put(context.getString(R.string.job_4), multiJob4Fragment);
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

    MultiJobPrintFragment getMultiJobPrintFragment() {
        return multiJobPrintFragment;
    }

    MultiJobFormFragment getMultiJob1Fragment() {
        return multiJob1Fragment;
    }

    MultiJobFormFragment getMultiJob2Fragment() {
        return multiJob2Fragment;
    }

    MultiJobFormFragment getMultiJob3Fragment() {
        return multiJob3Fragment;
    }

    MultiJobFormFragment getMultiJob4Fragment() {
        return multiJob4Fragment;
    }
}
