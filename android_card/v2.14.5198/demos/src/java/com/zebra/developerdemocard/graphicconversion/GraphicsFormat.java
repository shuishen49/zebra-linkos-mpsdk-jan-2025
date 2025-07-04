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
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.graphics.enumerations.MonochromeConversion;

import java.util.ArrayList;
import java.util.List;

public enum GraphicsFormat {
    GRAY_HALFTONE_8X8(R.string.gray_halftone_8x8, MonochromeConversion.HalfTone_8x8, PrintType.GrayDye),
    GRAY_HALFTONE_6X6(R.string.gray_halftone_6x6, MonochromeConversion.HalfTone_6x6, PrintType.GrayDye),
    GRAY_DIFFUSION(R.string.gray_diffusion, MonochromeConversion.Diffusion, PrintType.GrayDye),
    MONO_HALFTONE_8X8(R.string.mono_halftone_8x8, MonochromeConversion.HalfTone_8x8, PrintType.MonoK),
    MONO_HALFTONE_6X6(R.string.mono_halftone_6x6, MonochromeConversion.HalfTone_6x6, PrintType.MonoK),
    MONO_DIFFUSION(R.string.mono_diffusion, MonochromeConversion.Diffusion, PrintType.MonoK),
    COLOR(R.string.color, MonochromeConversion.None, PrintType.Color);

    @StringRes
    private int stringResourceId;
    private MonochromeConversion monochromeConversion;
    private PrintType printType;

    GraphicsFormat(int stringResourceId, MonochromeConversion monochromeConversion, PrintType printType) {
        this.stringResourceId = stringResourceId;
        this.monochromeConversion = monochromeConversion;
        this.printType = printType;
    }

    public String getString(Context context) {
        return context.getString(stringResourceId);
    }

    public static GraphicsFormat getGraphicsFormat(Context context, String graphicsFormatString) {
        GraphicsFormat foundGraphicsFormat = null;
        if (graphicsFormatString != null) {
            for (GraphicsFormat graphicsFormat : GraphicsFormat.values()) {
                if (graphicsFormatString.equals(context.getString(graphicsFormat.getStringResourceId()))) {
                    foundGraphicsFormat = graphicsFormat;
                }
            }
        }
        return foundGraphicsFormat;
    }

    public static List<String> getAllGraphicsFormatStrings(Context context) {
        List<String> graphicsFormatStrings = new ArrayList<>();
        for (GraphicsFormat graphicsFormat : GraphicsFormat.values()) {
            graphicsFormatStrings.add(context.getString(graphicsFormat.getStringResourceId()));
        }
        return graphicsFormatStrings;
    }

    public int getStringResourceId() {
        return stringResourceId;
    }

    public MonochromeConversion getMonochromeConversion() {
        return monochromeConversion;
    }

    public PrintType getPrintType() {
        return printType;
    }
}
