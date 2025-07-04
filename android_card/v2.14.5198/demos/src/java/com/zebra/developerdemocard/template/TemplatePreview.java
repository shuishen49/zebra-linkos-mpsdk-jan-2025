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

import android.graphics.Bitmap;

class TemplatePreview {
    private String label;
    private Bitmap bitmap;
    private String message;

    TemplatePreview(String label, Bitmap bitmap) {
        this(label, bitmap, null);
    }

    TemplatePreview(String label, String message) {
        this(label, null, message);
    }

    private TemplatePreview(String label, Bitmap bitmap, String message) {
        this.label = label;
        this.bitmap = bitmap;
        this.message = message;
    }

    String getLabel() {
        return label;
    }

    Bitmap getBitmap() {
        return bitmap;
    }

    String getMessage() {
        return message;
    }
}
