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

import java.io.Serializable;

public class PrinterSetting implements Serializable {
    private String setting;
    private String value;
    private boolean isSettable = false;

    public PrinterSetting(String setting, String value) {
        this.setting = setting;
        this.value = value;
    }

    PrinterSetting(String setting, String value, boolean isSettable) {
        this.setting = setting;
        this.value = value;
        this.isSettable = isSettable;
    }

    String getSetting() {
        return setting;
    }

    public String getValue() {
        return value;
    }

    boolean isSettable() {
        return isSettable;
    }
}
