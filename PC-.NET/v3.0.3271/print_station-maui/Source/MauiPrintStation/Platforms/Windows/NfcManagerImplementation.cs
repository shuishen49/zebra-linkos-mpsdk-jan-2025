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

using Windows.Networking.Proximity;
using Windows.Storage.Streams;

namespace MauiPrintStation
{
    public class NfcManagerImplementation : INfcManager {

        public event EventHandler<string> TagScanned;

        public bool IsNfcAvailable() {
            try {
                return ProximityDevice.GetDefault() != null;
            } catch (Exception) {
                return false;
            }
        }

        public void OnNfcMessageReceived(ProximityDevice proximityDevice, ProximityMessage proximityMessage) {
            using (var reader = DataReader.FromBuffer(proximityMessage.Data)) {
                reader.UnicodeEncoding = Windows.Storage.Streams.UnicodeEncoding.Utf16LE;
                string nfcData = reader.ReadString(reader.UnconsumedBufferLength / 2 - 1);

                TagScanned?.Invoke(this, nfcData);
            }
        }
    }
}
