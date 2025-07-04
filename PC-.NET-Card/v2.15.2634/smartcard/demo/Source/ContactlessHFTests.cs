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

    public class ContactlessHFTests {

        /// <summary>
        /// Mifare 1K or 4K Memory Test
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error description</param>
        /// <returns>true if memory test is successful</returns>
        public bool MifareTest(string readerName, ref string errMsg) {
            bool passed = false;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contactless.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("ATR not Found");
                }

                if (!cardType.Equals("MIFARE_1K") && !cardType.Equals("MIFARE_4K")) {
                    throw new Exception("Not a Mifare 1K or 4K Smart Card");
                }

                byte[] key = { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };
                if (!scl.Contactless.MifareLoadKey(0x01, key.Length, key)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                byte data = 0x00;
                byte keyNumber = 0x01;
                char keyType = 'A';
                int block = 4;

                if (!scl.Contactless.MifareAuthenticate(block, keyType, keyNumber)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                Thread.Sleep(250);

                byte[] dataIn = new byte[16];
                for (int i = 0; i < 16; i++) {
                    dataIn[i] = data++;
                }

                if (!scl.Contactless.MifareBlockWrite(block, dataIn, 16)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                if (!scl.Contactless.MifareBlockRead(block, out byte[] dataOut)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                if (dataOut == null || dataOut.Length.Equals(0)) {
                    throw new Exception("No data returned");
                }

                if (!dataIn.SequenceEqual(dataOut)) {
                    throw new Exception(" Read does not equal Written");
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
        /// Mifare Ultralight Memory Test
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error description</param>
        /// <returns>true if memory test is successful</returns>
        public bool MifareUltralightTest(string readerName, ref string errMsg) {
            bool passed = false;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Contactless.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("ATR not Found");
                }

                if (!cardType.Equals("MIFARE_ULTRALIGHT")) {
                    throw new Exception("Not a MIFARE ULTRALIGHT Smart Card");
                }

                int dataSize = 4;
                int block = 4;

                byte[] dataIn = new byte[8];
                byte[] dataOut = new byte[8];

                byte baseData = 0x00;
                for (int i = 0; i < dataSize; i++) {
                    dataIn[i] = baseData++;
                }

                if (!scl.Contactless.MifareBlockWrite(block, dataIn, dataSize)) {
                    throw new Exception(" Block " + block.ToString() + scl.Contactless.WinSCardError);
                }

                if (!scl.Contactless.MifareBlockRead(block, out dataOut)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                if (dataOut == null || dataOut.Length.Equals(0)) {
                    throw new Exception("No data returned for block " + block.ToString());
                }

                for (int i = 0; i < 4; i++) {
                    if (dataOut[i] != dataIn[i]) {
                        throw new Exception(" Block " + block.ToString() + " Read does not equal Written");
                    }
                }
                passed = true;
            } catch (Exception ex) {
                errMsg = "Mifare Ultralight Test Error: " + ex.Message;
            } finally {
                scl.Contactless.CardDisconnect();
                scl = null;
            }
            return passed;
        }

        /// <summary>
        /// Desfire Test
        /// </summary>
        /// <param name="readerName">smart card reader name</param>
        /// <param name="errMsg">error message</param>
        /// <returns>true if test was successful</returns>
        public bool DesfireTest(string readerName, ref string errMsg) {
            bool passed = false;
            ZCSmartCardLib.SmartCardLib scl = new ZCSmartCardLib.SmartCardLib();

            try {
                if (!scl.Desfire.CardConnect(readerName, out byte[] atrBuf)) {
                    throw new Exception(scl.Contactless.WinSCardError);
                }

                string cardType = string.Empty;
                if (!scl.CardATR.GetCardTypeFromATR(atrBuf, out cardType)) {
                    throw new Exception("ATR not Found");
                }

                if (!cardType.Equals("MIFARE_DESFIRE")) {
                    throw new Exception("Not a MIFARE DESFIRE Smart Card");
                }

                byte[] key = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                               0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

                if (!scl.Desfire.Authenticate(0, ref key)) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                if (!scl.Desfire.FormatCard()) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                byte[] aid = { 0xAA, 0xAA, 0xAA };
                if (!scl.Desfire.CreateApplication(ref aid)) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                if (!scl.Desfire.SelectApplication(ref aid)) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                byte fileNumber = 0x0F;
                byte comMode = 0x00;
                byte[] accessRights = new byte[] { 0xEE, 0xEE };
                int fileSize = 0x0F3C;

                if (!scl.Desfire.CreateFile(fileNumber, comMode, accessRights, fileSize)) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                byte[] dataIn = new byte[32];
                for (int i = 0; i < dataIn.Length; i++) {
                    dataIn[i] = (byte)(i + 0x41);
                }

                if (!scl.Desfire.DesfireWrite(15, 0, ref dataIn)) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                byte[] dataOut = new byte[dataIn.Length];
                if (!scl.Desfire.DesfireRead(15, 0, ref dataOut)) {
                    throw new Exception(scl.Desfire.errDesfire);
                }

                for (int i = 0; i < dataIn.Length; i++) {
                    if (dataIn[i] != dataOut[i]) {
                        throw new Exception("Read does not equal Written for Address [" + i.ToString() + "]");
                    }
                }
                passed = true;
            } catch (Exception ex) {
                errMsg = ex.Message;
            } finally {
                scl.Desfire.CardDisconnect();
                scl = null;
            }
            return passed;
        }
    }
}
