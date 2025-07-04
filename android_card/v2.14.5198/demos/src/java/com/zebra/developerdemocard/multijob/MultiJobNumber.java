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

public enum MultiJobNumber {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4);

    private int jobNumberInt;

    MultiJobNumber(int jobNumberInt) {
        this.jobNumberInt = jobNumberInt;
    }

    public int toInteger() {
        return jobNumberInt;
    }
}
