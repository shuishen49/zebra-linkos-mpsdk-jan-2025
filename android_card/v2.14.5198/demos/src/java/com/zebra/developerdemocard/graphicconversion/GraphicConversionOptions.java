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

import android.net.Uri;

import com.zebra.developerdemocard.util.PrinterModelInfo;

public class GraphicConversionOptions {
    private Uri sourceGraphicUri;
    private String convertedGraphicFilename;
    private GraphicsFormat graphicsFormat;
    private PrinterModelInfo printerModelInfo;
    private DimensionOption dimensionOption;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;

    GraphicConversionOptions(Uri sourceGraphicUri, String convertedGraphicFilename, GraphicsFormat graphicsFormat, PrinterModelInfo printerModelInfo, DimensionOption dimensionOption, int width, int height, int xOffset, int yOffset) {
        this.sourceGraphicUri = sourceGraphicUri;
        this.convertedGraphicFilename = convertedGraphicFilename;
        this.graphicsFormat = graphicsFormat;
        this.printerModelInfo = printerModelInfo;
        this.dimensionOption = dimensionOption;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    Uri getSourceGraphicUri() {
        return sourceGraphicUri;
    }

    String getConvertedGraphicFilename() {
        return convertedGraphicFilename;
    }

    GraphicsFormat getGraphicsFormat() {
        return graphicsFormat;
    }

    PrinterModelInfo getPrinterModelInfo() {
        return printerModelInfo;
    }

    DimensionOption getDimensionOption() {
        return dimensionOption;
    }

    int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    int getXOffset() {
        return xOffset;
    }

    int getYOffset() {
        return yOffset;
    }
}
