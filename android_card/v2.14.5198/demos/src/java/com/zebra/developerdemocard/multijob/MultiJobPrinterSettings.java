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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class MultiJobPrinterSettings implements Serializable {
    private boolean showPrintOptimization = false;
    private boolean showBackSidePrint = false;
    private boolean showMagEncoding = false;
    private boolean allowsColorOption = false;
    private boolean allowsMonoOption = false;
    private boolean allowsOverlayOption = false;
    private List<String> cardSourceRange = new ArrayList<>();
    private List<String> cardDestinationRange = new ArrayList<>();
    private List<String> cardCoercivityTypeRange = new ArrayList<>();

    boolean getShowPrintOptimization() {
        return showPrintOptimization;
    }

    boolean getShowBackSidePrint() {
        return showBackSidePrint;
    }

    boolean getShowMagEncoding() {
        return showMagEncoding;
    }

    boolean getAllowsColorOption() {
        return allowsColorOption;
    }

    boolean getAllowsMonoOption() {
        return allowsMonoOption;
    }

    boolean getAllowsOverlayOption() {
        return allowsOverlayOption;
    }

    List<String> getCardSourceRange() {
        return cardSourceRange;
    }

    List<String> getCardDestinationRange() {
        return cardDestinationRange;
    }

    List<String> getCardCoercivityTypeRange() {
        return cardCoercivityTypeRange;
    }

    void setShowPrintOptimization(boolean showPrintOptimization) {
        this.showPrintOptimization = showPrintOptimization;
    }

    void setShowBackSidePrint(boolean showBackSidePrint) {
        this.showBackSidePrint = showBackSidePrint;
    }

    void setShowMagEncoding(boolean showMagEncoding) {
        this.showMagEncoding = showMagEncoding;
    }

    void setAllowsColorOption(boolean allowsColorOption) {
        this.allowsColorOption = allowsColorOption;
    }

    void setAllowsMonoOption(boolean allowsMonoOption) {
        this.allowsMonoOption = allowsMonoOption;
    }

    void setAllowsOverlayOption(boolean allowsOverlayOption) {
        this.allowsOverlayOption = allowsOverlayOption;
    }

    void setCardSourceRange(List<String> cardSourceRange) {
        this.cardSourceRange = cardSourceRange;
    }

    void setCardDestinationRange(List<String> cardDestinationRange) {
        this.cardDestinationRange = cardDestinationRange;
    }

    void setCardCoercivityTypeRange(List<String> cardCoercivityTypeRange) {
        this.cardCoercivityTypeRange = cardCoercivityTypeRange;
    }
}
