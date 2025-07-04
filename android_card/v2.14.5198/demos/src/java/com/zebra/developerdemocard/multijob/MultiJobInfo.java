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

import android.net.Uri;

import com.zebra.developerdemocard.jobstatus.JobInfo;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;
import com.zebra.sdk.common.card.enumerations.PrintOptimizationMode;
import com.zebra.sdk.common.card.enumerations.PrintType;

import java.util.HashMap;
import java.util.Map;

class MultiJobInfo extends JobInfo {
    private MultiJobNumber jobNumber;
    private boolean isFrontSideEnabled = false;
    private boolean isBackSideEnabled = false;
    private CardDestination cardDestination;
    private boolean showPrintOptimization = false;
    private PrintOptimizationMode printOptimizationMode;
    private Map<PrintType, Uri> frontImageInfo = new HashMap<>();
    private Map<PrintType, Uri> backImageInfo = new HashMap<>();
    private CoercivityType coercivityType;
    private boolean showMagEncoding = false;
    private boolean isMagEncodingEnabled = false;
    private String track1Data = "";
    private String track2Data = "";
    private String track3Data = "";
    private int quantity = 1;

    MultiJobInfo(CardSource cardSource) {
        super(cardSource);
    }

    boolean hasPrintableFrontSide() {
        return isFrontSideEnabled && !frontImageInfo.isEmpty();
    }

    boolean hasPrintableBackSide() {
        return isBackSideEnabled && !backImageInfo.isEmpty();
    }

    boolean hasEncodableMagData() {
        boolean hasMagData = !track1Data.isEmpty() || !track2Data.isEmpty() || !track3Data.isEmpty();
        return isMagEncodingEnabled && hasMagData;
    }

    MultiJobNumber getJobNumber() {
        return jobNumber;
    }

    CardDestination getCardDestination() {
        return cardDestination;
    }

    boolean getShowPrintOptimization() {
        return showPrintOptimization;
    }

    PrintOptimizationMode getPrintOptimizationMode() {
        return printOptimizationMode;
    }

    Map<PrintType, Uri> getFrontImageInfo() {
        return frontImageInfo;
    }

    Map<PrintType, Uri> getBackImageInfo() {
        return backImageInfo;
    }

    CoercivityType getCoercivityType() {
        return coercivityType;
    }

    boolean getShowMagEncoding() {
        return showMagEncoding;
    }

    boolean isMagEncodingEnabled() {
        return isMagEncodingEnabled;
    }

    String getTrack1Data() {
        return track1Data;
    }

    String getTrack2Data() {
        return track2Data;
    }

    String getTrack3Data() {
        return track3Data;
    }

    int getQuantity() {
        return quantity;
    }

    void setJobNumber(MultiJobNumber jobNumber) {
        this.jobNumber = jobNumber;
    }

    void setFrontSideEnabled(boolean frontSideEnabled) {
        isFrontSideEnabled = frontSideEnabled;
    }

    void setBackSideEnabled(boolean backSideEnabled) {
        isBackSideEnabled = backSideEnabled;
    }

    void setCardDestination(CardDestination cardDestination) {
        this.cardDestination = cardDestination;
    }

    void setShowPrintOptimization(boolean showPrintOptimization) {
        this.showPrintOptimization = showPrintOptimization;
    }

    void setPrintOptimizationMode(PrintOptimizationMode printOptimizationMode) {
        this.printOptimizationMode = printOptimizationMode;
    }

    void setFrontImageInfo(Map<PrintType, Uri> frontImageInfo) {
        this.frontImageInfo = frontImageInfo;
    }

    void setBackImageInfo(Map<PrintType, Uri> backImageInfo) {
        this.backImageInfo = backImageInfo;
    }

    void setCoercivityType(CoercivityType coercivityType) {
        this.coercivityType = coercivityType;
    }

    void setShowMagEncoding(boolean showMagEncoding) {
        this.showMagEncoding = showMagEncoding;
    }

    void setMagEncodingEnabled(boolean magEncodingEnabled) {
        isMagEncodingEnabled = magEncodingEnabled;
    }

    void setTrack1Data(String track1Data) {
        this.track1Data = track1Data;
    }

    void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    void setTrack3Data(String track3Data) {
        this.track3Data = track3Data;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
