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

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.PrinterHelper;
import com.zebra.developerdemocard.util.UriHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.enumerations.CardSide;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.GraphicType;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.graphics.ZebraCardGraphics;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.enumerations.RotationType;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendPrintJobTask extends AsyncTask<Void, Void, Integer> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private PrintOptions printOptions;
    private OnSendPrintJobListener onSendPrintJobListener;
    private Exception exception;
    private CardSource cardSource;

    public interface OnSendPrintJobListener extends PrinterHelper.OnPrinterReadyListener {
        void onSendPrintJobStarted();
        void onSendPrintJobFinished(Exception exception, Integer jobId, CardSource cardSource);
    }

    SendPrintJobTask(Context context, DiscoveredPrinter printer, PrintOptions printOptions) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.printOptions = printOptions;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onSendPrintJobListener != null) {
            onSendPrintJobListener.onSendPrintJobStarted();
        }
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        ZebraCardGraphics graphics = null;
        ZebraCardPrinter zebraCardPrinter = null;
        Connection connection = null;
        Integer jobId = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            if (PrinterHelper.isPrinterReady(weakContext.get(), zebraCardPrinter, onSendPrintJobListener)) {
                graphics = new ZebraCardGraphics(zebraCardPrinter);

                Map<PrintType, Uri> frontSideImageUriMap = printOptions.getFrontSideImageUriMap();
                Map<PrintType, Uri> backSideImageUriMap = printOptions.getBackSideImageUriMap();

                PrintType printType = null;
                Uri frontSideImageUri = null;
                Uri frontSideOverlayImageUri = frontSideImageUriMap.containsKey(PrintType.Overlay) ? frontSideImageUriMap.get(PrintType.Overlay) : null;
                Uri backSideImageUri = backSideImageUriMap.containsKey(PrintType.MonoK) ? backSideImageUriMap.get(PrintType.MonoK) : null;

                if (frontSideImageUriMap.containsKey(PrintType.Color)) {
                    frontSideImageUri = frontSideImageUriMap.get(PrintType.Color);
                    printType = PrintType.Color;
                } else if (frontSideImageUriMap.containsKey(PrintType.MonoK)) {
                    frontSideImageUri = frontSideImageUriMap.get(PrintType.MonoK);
                    printType = PrintType.MonoK;
                }

                byte[] frontSideImageData = frontSideImageUri != null ? UriHelper.getByteArrayFromUri(weakContext.get(), frontSideImageUri) : null;
                byte[] frontSideOverlayImageData = frontSideOverlayImageUri != null ? UriHelper.getByteArrayFromUri(weakContext.get(), frontSideOverlayImageUri) : null;
                byte[] backSideImageData = backSideImageUri != null ? UriHelper.getByteArrayFromUri(weakContext.get(), backSideImageUri) : null;

                List<GraphicsInfo> graphicsData = new ArrayList<>();

                if (frontSideImageData != null) {
                    graphics.initialize(weakContext.get().getApplicationContext(), 0, 0, OrientationType.Landscape, printType, Color.WHITE);
                    graphics.drawImage(frontSideImageData, 0, 0, 0, 0, RotationType.RotateNoneFlipNone);
                    graphicsData.add(buildGraphicsInfo(graphics.createImage(), CardSide.Front, printType));
                    graphics.clear();
                }

                if (frontSideOverlayImageData != null) {
                    graphics.initialize(0, 0, OrientationType.Landscape, PrintType.Overlay, Color.WHITE);
                    graphics.drawImage(frontSideOverlayImageData, 0, 0, 0, 0, RotationType.RotateNoneFlipNone);
                    graphicsData.add(buildGraphicsInfo(graphics.createImage(), CardSide.Front, PrintType.Overlay));
                    graphics.clear();
                } else if (frontSideImageUriMap.containsKey(PrintType.Overlay)) {
                    graphicsData.add(buildGraphicsInfo(null, CardSide.Front, PrintType.Overlay));
                    graphics.clear();
                }

                if (backSideImageData != null) {
                    graphics.initialize(0, 0, OrientationType.Landscape, PrintType.MonoK, Color.WHITE);
                    graphics.drawImage(backSideImageData, 0, 0, 0, 0, RotationType.RotateNoneFlipNone);
                    graphicsData.add(buildGraphicsInfo(graphics.createImage(), CardSide.Back, PrintType.MonoK));
                    graphics.clear();
                }

                cardSource = CardSource.fromString(zebraCardPrinter.getJobSettingValue(ZebraCardJobSettingNames.CARD_SOURCE));

                jobId = zebraCardPrinter.print(printOptions.getQuantity(), graphicsData);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            if (graphics != null) {
                graphics.close();
            }

            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return jobId;
    }

    @Override
    protected void onPostExecute(Integer jobId) {
        super.onPostExecute(jobId);

        if (onSendPrintJobListener != null) {
            onSendPrintJobListener.onSendPrintJobFinished(exception, jobId, cardSource);
        }
    }

    private GraphicsInfo buildGraphicsInfo(ZebraCardImageI zebraCardImage, CardSide side, PrintType printType) throws IOException {
        GraphicsInfo graphicsInfo = new GraphicsInfo();
        if (zebraCardImage != null) {
            graphicsInfo.graphicData = zebraCardImage;
            graphicsInfo.graphicType = GraphicType.BMP;
        } else {
            graphicsInfo.graphicType = GraphicType.NA;
        }
        graphicsInfo.side = side;
        graphicsInfo.printType = printType;
        return graphicsInfo;
    }

    void setOnSendPrintJobListener(OnSendPrintJobListener onSendPrintJobListener) {
        this.onSendPrintJobListener = onSendPrintJobListener;
    }
}
