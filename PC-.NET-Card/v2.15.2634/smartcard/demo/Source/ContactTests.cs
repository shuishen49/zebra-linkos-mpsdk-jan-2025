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
using System.Linq;
using System.Threading;

namespace ZCSCDevDemo {

    public class ContactTests {

        /// <summary>
        /// Atmel AT88SC0104C Contact Memory Card Test
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error description</param>
        /// <returns>true if memory test is successful</returns>
        public bool MemoryTestAT88SC0104C(string readerName, ref string errMsg) {
            bool passed = false;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contact.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("ATR not Found");
                }

                if (!cardType.Equals("AT88SC0104C")) {
                    throw new Exception("Not an AT88SC0104C Smart Card");
                }

                byte zone = 0x00;
                byte[] setZone = new byte[] { 0x00, 0xB4, 0x03, 0x00, 0x00 };
                setZone[3] = zone;
                scl.Contact.CardTransmit(setZone);

                byte baseData = 0x00;
                byte[] dataWrote = new byte[8];
                for (int i = 0; i < dataWrote.Length; i++) {
                    dataWrote[i] = baseData++;
                }

                byte offset = 0x00;
                byte[] writeZoneData = new byte[] { 0x00, 0xB0, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
                writeZoneData[3] = offset;
                Array.Copy(dataWrote, offset, writeZoneData, 5, 8);

                if (!scl.Contact.CardTransmit(writeZoneData)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                byte[] readZoneData = new byte[] { 0x00, 0xB2, 0x00, 0x00, 0x08 };
                readZoneData[3] = offset;
                if (!scl.Contact.CardTransmitAndReceive(readZoneData, out byte[] dataOut)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                byte[] dataRead = new byte[8];
                Array.Copy(dataOut, 0, dataRead, offset, 8);
                if (!dataWrote.SequenceEqual(dataRead)) {
                    throw new Exception(" Zone " + zone.ToString() + " Read does not equal Written");
                }
                passed = true;
            } catch (Exception ex) {
                Thread.Sleep(1000);
                errMsg = "AT88SC0104C Test Error: " + ex.Message;
            } finally {
                scl.Contact.CardDisconnect();
                scl = null;
            }
            return passed;
        }

        /// <summary>
        /// SLE5528 and SLE4428 Memory Test
        /// </summary>
        /// <param name="key">authentication key</param>
        /// <param name="errMsg">error description</param>
        public bool MemoryTestSLEXX28(string readerName, ref string errMsg) {
            bool passed = false;
            IntPtr conn = IntPtr.Zero;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contact.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("Card ATR not found");
                }

                if (!cardType.Equals("SLEXX28")) {
                    throw new Exception("Not a SLEXX28 Smart Card");
                }

                if (!scl.Contact.CardSelect(cardType)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                byte[] key = { 0xFF, 0xFF };
                if (!scl.Contact.Authenticate(cardType, key)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                byte data = 0x00;
                byte[] dataIn = new byte[16];
                for (int i = 0; i < dataIn.Length; i++) {
                    dataIn[i] = data++;
                }

                byte addr = 31;
                if (!scl.Contact.MemoryWrite(cardType, addr, (byte)dataIn.Length, dataIn)) {
                    throw new Exception(" Address " + addr.ToString() + " " + scl.Contact.WinSCardError);
                }

                byte[] dataOut = null;
                if (!scl.Contact.MemoryRead(addr, (byte)dataIn.Length, ref dataOut)) {
                    throw new Exception(" Address " + addr.ToString() + " " + scl.Contact.WinSCardError);
                }

                if (dataOut == null || dataOut.Length <= 0) {
                    throw new Exception(" Address " + addr.ToString() + " " + "No Data Read");
                }

                if (!dataIn.SequenceEqual(dataOut)) {
                    throw new Exception(" Address " + addr.ToString() + " Read does not equal Written");
                }
                passed = true;
            } catch (Exception ex) {
                Thread.Sleep(1000);
                errMsg = "SLEXX28 Memory Test Error: " + ex.Message;
            } finally {
                scl.Contact.CardDisconnect();
                scl = null;
            }
            return passed;
        }

        /// <summary>
        /// SLE5542 and SLE5542 Contact Memory Card Test
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error description</param>
        /// <returns>true if memory test is successful</returns>
        public bool MemoryTestSLEXX42(string readerName, ref string errMsg) {
            bool passed = false;
            IntPtr conn = IntPtr.Zero;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contact.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("ATR not Found");
                }

                if (cardType.Equals("NO CARD PRESENT")) {
                    throw new Exception("No Card Present");
                }

                if (!cardType.Equals("SLEXX42")) {
                    throw new Exception("Not a SLEXX42 Smart Card");
                }

                if (!scl.Contact.CardSelect(cardType)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                byte[] key = { 0xFF, 0xFF, 0xFF };
                if (!scl.Contact.Authenticate(cardType, key)) {
                    throw new Exception(scl.Contact.WinSCardError);
                }

                byte data = 0x00;
                int dataLen = 16;
                byte[] dataIn = new byte[dataLen];
                for (int i = 0; i < dataLen; i++) {
                    dataIn[i] = data++;
                }

                byte addr = 31;
                if (!scl.Contact.MemoryWrite(cardType, addr, (byte)dataLen, dataIn)) {
                    throw new Exception(" Address " + addr.ToString() + scl.Contact.WinSCardError);
                }

                byte[] dataOut = null;
                if (!scl.Contact.MemoryRead(addr, (byte)dataLen, ref dataOut)) {
                    throw new Exception(" Address " + addr.ToString() + scl.Contact.WinSCardError);
                }

                if (dataOut == null || dataOut.Length == 0) {
                    throw new Exception(" Address " + addr.ToString() + " No Data Read");
                }

                if (!dataIn.SequenceEqual(dataOut)) {
                    throw new Exception(" Address " + addr.ToString() + " Read does not equal Written");
                }
                passed = true;
            } catch (Exception ex) {
                Thread.Sleep(1000);
                errMsg = "SLEXX42 Memory Test Error: " + ex.Message;
            } finally {
                scl.Contact.CardDisconnect();
                scl = null;
            }
            return passed;
        }
    }
}
