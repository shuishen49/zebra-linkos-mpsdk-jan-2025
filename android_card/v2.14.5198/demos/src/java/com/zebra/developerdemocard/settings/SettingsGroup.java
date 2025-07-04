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
import android.support.annotation.StringRes;

import com.zebra.developerdemocard.R;

public enum SettingsGroup {
    DEVICE(R.string.device),
    PRINT(R.string.print);

    @StringRes
    private int stringRes;

    SettingsGroup(@StringRes int stringRes) {
        this.stringRes = stringRes;
    }

    public String getString(Context context) {
        return context.getString(stringRes);
    }
}
