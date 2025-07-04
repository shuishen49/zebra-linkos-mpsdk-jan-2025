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

package com.zebra.developerdemocard.graphicconversion;

import android.content.Context;
import android.support.annotation.StringRes;

import com.zebra.developerdemocard.R;

import java.util.ArrayList;
import java.util.List;

public enum DimensionOption {
    ORIGINAL(R.string.original),
    RESIZE(R.string.resize),
    CROP(R.string.crop);

    @StringRes
    private int stringResourceId;

    DimensionOption(int stringResourceId) {
        this.stringResourceId = stringResourceId;
    }

    public String getString(Context context) {
        return context.getString(stringResourceId);
    }

    public static DimensionOption getDimensionOption(Context context, String dimensionOptionString) {
        DimensionOption foundDimensionOption = null;
        if (dimensionOptionString != null) {
            for (DimensionOption dimensionOption : DimensionOption.values()) {
                if (dimensionOptionString.equals(context.getString(dimensionOption.getStringResourceId()))) {
                    foundDimensionOption = dimensionOption;
                }
            }
        }
        return foundDimensionOption;
    }

    public static List<String> getAllDimensionOptionStrings(Context context) {
        List<String> dimensionOptionStrings = new ArrayList<>();
        for (DimensionOption dimensionOption : DimensionOption.values()) {
            dimensionOptionStrings.add(context.getString(dimensionOption.getStringResourceId()));
        }
        return dimensionOptionStrings;
    }

    public int getStringResourceId() {
        return stringResourceId;
    }
}
