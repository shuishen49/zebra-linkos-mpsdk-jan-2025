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

package com.zebra.developerdemocard.printerstatus;

import com.zebra.sdk.common.card.containers.MediaInfo;
import com.zebra.sdk.common.card.containers.PrinterInfo;
import com.zebra.sdk.common.card.containers.PrinterStatusInfo;

import java.util.List;
import java.util.Map;

class PrinterStatus {
    private PrinterInfo printerInfo;
    private PrinterStatusInfo printerStatusInfo;
    private List<MediaInfo> mediaInfoList;
    private Map<String, String> sensorInfo;

    PrinterStatus(PrinterInfo printerInfo, PrinterStatusInfo printerStatusInfo, List<MediaInfo> mediaInfoList, Map<String, String> sensorInfo) {
        this.printerInfo = printerInfo;
        this.printerStatusInfo = printerStatusInfo;
        this.mediaInfoList = mediaInfoList;
        this.sensorInfo = sensorInfo;
    }

    PrinterInfo getPrinterInfo() {
        return printerInfo;
    }

    PrinterStatusInfo getPrinterStatusInfo() {
        return printerStatusInfo;
    }

    List<MediaInfo> getMediaInfoList() {
        return mediaInfoList;
    }

    Map<String, String> getSensorInfo() {
        return sensorInfo;
    }
}
