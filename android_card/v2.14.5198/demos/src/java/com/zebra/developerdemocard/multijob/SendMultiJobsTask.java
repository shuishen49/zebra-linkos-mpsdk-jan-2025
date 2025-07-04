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
import android.net.Uri;
import android.os.AsyncTask;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.PrinterHelper;
import com.zebra.developerdemocard.util.UriHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.enumerations.CardSide;
import com.zebra.sdk.common.card.enumerations.GraphicType;
import com.zebra.sdk.common.card.enumerations.MagEncodingType;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.graphics.ZebraCardGraphics;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.enumerations.RotationType;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.settings.SettingsException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SendMultiJobsTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private List<MultiJobInfo> multiJobInfoList;
    private OnSendMultiJobsListener onSendMultiJobsListener;
    private Exception exception;

    public interface OnSendMultiJobsListener extends PrinterHelper.OnPrinterReadyListener {
        void onSendMultiJobsStarted();
        void onSendMultiJobsLogUpdate(String message); // Update the job status log
        void onSendMultiJobsFinished(Exception exception, List<MultiJobInfo> multiJobInfoList);
    }

    SendMultiJobsTask(Context context, DiscoveredPrinter printer, List<MultiJobInfo> multiJobInfoList) {
        this.weakContext = new WeakReference<>(context);
        this.printer = printer;
        this.multiJobInfoList = multiJobInfoList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onSendMultiJobsListener != null) {
            onSendMultiJobsListener.onSendMultiJobsStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;

        try {
            if (areAnyJobsValid()) {
                connection = printer.getConnection();
                connection.open();

                zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

                if (PrinterHelper.isPrinterReady(weakContext.get(), zebraCardPrinter, onSendMultiJobsListener)) {
                    if (onSendMultiJobsListener != null) {
                        onSendMultiJobsListener.onSendMultiJobsLogUpdate(weakContext.get().getString(R.string.setting_up_jobs));
                    }

                    Collections.sort(multiJobInfoList, new MultiJobInfoComparator());

                    for (MultiJobInfo multiJobInfo : multiJobInfoList) {
                        setUpAndSendJob(zebraCardPrinter, multiJobInfo);
                    }
                } else {
                    multiJobInfoList = new ArrayList<>();
                }
            } else {
                throw new ZebraCardException(weakContext.get().getString(R.string.no_jobs_configured_message));
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onSendMultiJobsListener != null) {
            onSendMultiJobsListener.onSendMultiJobsFinished(exception, multiJobInfoList);
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

    private List<GraphicsInfo> createGraphicsInfo(ZebraCardGraphics graphics, Map<PrintType, Uri> imageInfo, CardSide side) throws IOException {
        List<GraphicsInfo> graphicsInfoList = new ArrayList<>();
        for (PrintType type : imageInfo.keySet()) {
            graphics.initialize(0, 0, OrientationType.Landscape, type, -1);

            if (type.equals(PrintType.Overlay) && imageInfo.get(type) == null) {
                GraphicsInfo graphicsInfo = new GraphicsInfo();
                graphicsInfo.side = side;
                graphicsInfo.printType = type;
                graphicsInfo.graphicType = GraphicType.NA;
                graphicsInfoList.add(graphicsInfo);
            } else {
                byte[] imageData = UriHelper.getByteArrayFromUri(weakContext.get(), imageInfo.get(type));
                graphics.drawImage(imageData, 0, 0, 0, 0, RotationType.RotateNoneFlipNone);
                graphicsInfoList.add(buildGraphicsInfo(graphics.createImage(), side, type));
            }

            graphics.clear();
        }

        return graphicsInfoList;
    }

    private int magEncode(ZebraCardPrinter zebraCardPrinter, MultiJobInfo multiJobInfo) throws ConnectionException, SettingsException, ZebraCardException {
        return zebraCardPrinter.magEncode(multiJobInfo.getQuantity(), multiJobInfo.getTrack1Data(), multiJobInfo.getTrack2Data(), multiJobInfo.getTrack3Data());
    }

    private int printAndMagEncode(ZebraCardPrinter zebraCardPrinter, MultiJobInfo multiJobInfo) throws IOException, ConnectionException, SettingsException, ZebraCardException {
        int jobId;

        ZebraCardGraphics graphics = null;
        try {
            graphics = new ZebraCardGraphics(zebraCardPrinter);

            List<GraphicsInfo> graphicsInfo = new ArrayList<>();

            if (multiJobInfo.hasPrintableFrontSide()) {
                graphicsInfo.addAll(createGraphicsInfo(graphics, multiJobInfo.getFrontImageInfo(), CardSide.Front));
            }

            if (multiJobInfo.hasPrintableBackSide()) {
                graphicsInfo.addAll(createGraphicsInfo(graphics, multiJobInfo.getBackImageInfo(), CardSide.Back));
            }

            if (multiJobInfo.hasEncodableMagData()) {
                jobId = zebraCardPrinter.printAndMagEncode(multiJobInfo.getQuantity(), graphicsInfo, multiJobInfo.getTrack1Data(), multiJobInfo.getTrack2Data(), multiJobInfo.getTrack3Data());
            } else {
                jobId = zebraCardPrinter.print(multiJobInfo.getQuantity(), graphicsInfo);
            }
        } finally {
            if (graphics != null) {
                graphics.close();
            }
        }

        return jobId;
    }

    private void setJobSettings(ZebraCardPrinter zebraCardPrinter, MultiJobInfo multiJobInfo) throws SettingsException {
        zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.CARD_SOURCE, multiJobInfo.getCardSource().name());
        zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.CARD_DESTINATION, multiJobInfo.getCardDestination().name());

        if (multiJobInfo.getShowPrintOptimization()) {
            zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.PRINT_OPTIMIZATION, multiJobInfo.getPrintOptimizationMode().name());
        }

        if (multiJobInfo.getShowMagEncoding()) {
            zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.MAG_ENCODING_TYPE, MagEncodingType.ISO.name());

            if (multiJobInfo.isMagEncodingEnabled()) {
                zebraCardPrinter.setJobSetting(ZebraCardJobSettingNames.MAG_COERCIVITY, multiJobInfo.getCoercivityType().name());
            }
        }
    }

    private void setUpAndSendJob(ZebraCardPrinter zebraCardPrinter, MultiJobInfo multiJobInfo) throws IOException, SettingsException, ZebraCardException, ConnectionException {
        setJobSettings(zebraCardPrinter, multiJobInfo);

        Integer jobId = null;
        if (multiJobInfo.hasPrintableFrontSide() || multiJobInfo.hasPrintableBackSide()) {
            jobId = printAndMagEncode(zebraCardPrinter, multiJobInfo);
        } else if (multiJobInfo.hasEncodableMagData()) {
            jobId = magEncode(zebraCardPrinter, multiJobInfo);
        }

        if (jobId != null) {
            multiJobInfo.setJobId(jobId);
        }
    }

    private boolean areAnyJobsValid() {
        for (MultiJobInfo multiJobInfo : multiJobInfoList) {
            if (multiJobInfo.hasPrintableFrontSide() || multiJobInfo.hasPrintableBackSide() || multiJobInfo.hasEncodableMagData()) {
                return true;
            }
        }
        return false;
    }

    void setOnSendMultiJobsListener(OnSendMultiJobsListener onSendMultiJobsListener) {
        this.onSendMultiJobsListener = onSendMultiJobsListener;
    }
}
