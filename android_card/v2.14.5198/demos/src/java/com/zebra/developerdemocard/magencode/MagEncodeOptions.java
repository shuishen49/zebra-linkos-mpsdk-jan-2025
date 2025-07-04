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

package com.zebra.developerdemocard.magencode;

import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;

public class MagEncodeOptions {
    private CardSource cardSource;
    private CardDestination cardDestination;
    private CoercivityType coercivityType;
    private boolean verifyEncoding;
    private String track1Data;
    private String track2Data;
    private String track3Data;

    MagEncodeOptions(CardSource cardSource, CardDestination cardDestination, CoercivityType coercivityType, boolean verifyEncoding, String track1Data, String track2Data, String track3Data) {
        this.cardSource = cardSource;
        this.cardDestination = cardDestination;
        this.coercivityType = coercivityType;
        this.verifyEncoding = verifyEncoding;
        this.track1Data = track1Data;
        this.track2Data = track2Data;
        this.track3Data = track3Data;
    }

    public CardSource getCardSource() {
        return cardSource;
    }

    CardDestination getCardDestination() {
        return cardDestination;
    }

    CoercivityType getCoercivityType() {
        return coercivityType;
    }

    boolean getVerifyEncoding() {
        return verifyEncoding;
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
}
