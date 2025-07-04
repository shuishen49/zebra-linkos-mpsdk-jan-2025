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

package com.zebra.developerdemocard.print;

import android.net.Uri;

import com.zebra.sdk.common.card.enumerations.PrintType;

import java.util.HashMap;
import java.util.Map;

class PrintOptions {
    private Map<PrintType, Uri> frontSideImageUriMap = new HashMap<>();
    private Map<PrintType, Uri> backSideImageUriMap = new HashMap<>();
    private int quantity = 1;

    PrintOptions(Map<PrintType, Uri> frontSideImageUriMap, Map<PrintType, Uri> backSideImageUriMap, int quantity) {
        this.frontSideImageUriMap = frontSideImageUriMap;
        this.backSideImageUriMap = backSideImageUriMap;
        this.quantity = quantity;
    }

    Map<PrintType, Uri> getFrontSideImageUriMap() {
        return frontSideImageUriMap;
    }

    Map<PrintType, Uri> getBackSideImageUriMap() {
        return backSideImageUriMap;
    }

    int getQuantity() {
        return quantity;
    }
}
