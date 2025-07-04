/***********************************************
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
 ***********************************************/

using System;
using Zebra.Sdk.Card.Printer;
using Zebra.Sdk.Comm;

namespace Zebra.PrintStationCard.Util {

    public static class ConnectionHelper {

        public static void CleanUpQuietly(ZebraCardPrinter zebraCardPrinter, Connection connection) {
            try {
                if (zebraCardPrinter != null) {
                    zebraCardPrinter.Destroy();
                }
            } catch (Exception) {
                // Do nothing
            }

            try {
                if (connection != null) {
                    connection.Close();
                }
            } catch (Exception) {
                // Do nothing
            }
        }
    }
}
