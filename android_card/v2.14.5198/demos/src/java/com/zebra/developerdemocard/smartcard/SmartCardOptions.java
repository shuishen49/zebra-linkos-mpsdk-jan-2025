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

package com.zebra.developerdemocard.smartcard;

import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;

public class SmartCardOptions {
    private CardSource cardSource;
    private CardDestination cardDestination;
    private String cardType;

    SmartCardOptions(CardSource cardSource, CardDestination cardDestination, String cardType) {
        this.cardSource = cardSource;
        this.cardDestination = cardDestination;
        this.cardType = cardType;
    }

    public CardSource getCardSource() {
        return cardSource;
    }

    CardDestination getCardDestination() {
        return cardDestination;
    }

    String getCardType() {
        return cardType;
    }
}
