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

import java.util.Comparator;

public class MultiJobInfoComparator implements Comparator<MultiJobInfo> {
    @Override
    public int compare(MultiJobInfo multiJobInfo1, MultiJobInfo multiJobInfo2) {
        MultiJobNumber jobNumber1 = multiJobInfo1.getJobNumber();
        MultiJobNumber jobNumber2 = multiJobInfo2.getJobNumber();
        int jobNumberInt1 = jobNumber1 != null ? jobNumber1.toInteger() : -1;
        int jobNumberInt2 = jobNumber2 != null ? jobNumber2.toInteger() : -1;
        return jobNumberInt1 - jobNumberInt2;
    }
}
