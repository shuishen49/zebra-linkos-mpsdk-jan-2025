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

namespace ZCSCDevDemo {

    public class ContactlessLFTests {

        /// HID LF Prox Test. Get card UID
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error description</param>
        /// <returns>true if UID successful read</returns>
        public bool HIDProxTest(string readerName, ref string errMsg) {
            bool passed = false;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contactless.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                if (!atrBuf[0].Equals(0x3B) || !atrBuf[1].Equals(0x06)) {
                    throw new Exception("Not a LF Prox Smart Card");
                }

                scl.CardATR.GetProxFormatAndCnFromATR(atrBuf, out byte proxFormat, out byte[] cn);

                byte[] cmd = new byte[] { 0xFF, 0xCA, 0x00, 0x00, 0x00 };
                if (!scl.Contactless.CardTransmitAndReceive(cmd, out byte[] uidData)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                if (uidData == null || uidData.Length.Equals(0)) {
                    throw new Exception("No UID returned");
                }
                passed = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            } finally {
                scl.Contactless.CardDisconnect();
                scl = null;
            }
            return passed;
        }

        /// <summary>
        /// HITAG 1 and 2 Prox Test. Get card ID
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error description</param>
        /// <returns>true if UID successful read</returns>
        public bool HitagTest(string readerName, ref string errMsg) {
            bool passed = false;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contactless.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("Card ATR not found");
                }

                if (!cardType.Equals("HITAG")) {
                    throw new Exception("Not a HITAG Smart Card");
                }

                byte[] cmd = new byte[] { 0xFF, 0xCA, 0x00, 0x00, 0x00 };
                if (!scl.Contactless.CardTransmitAndReceive(cmd, out byte[] cardID)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                if (cardID == null || cardID.Length.Equals(0)) {
                    throw new Exception("Card ID not returned");
                }
                passed = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            } finally {
                scl.Contactless.CardDisconnect();
                scl = null;
            }
            return passed;
        }
    }
}
