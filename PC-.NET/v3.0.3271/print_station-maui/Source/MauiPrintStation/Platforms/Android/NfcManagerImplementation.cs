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

using System.Text;
using Android.Content;
using Android.Nfc;
using Android.OS;

namespace MauiPrintStation
{
    public class NfcManagerImplementation : INfcManager
    {

        public event EventHandler<string> TagScanned;

        public void OnNewIntent(object sender, Intent e, bool launchScreen) {
            IParcelable[] tags = e.GetParcelableArrayExtra(NfcAdapter.ExtraNdefMessages);
            if (tags?.Length > 0) {
                NdefMessage message = (NdefMessage)tags[0];
                string nfcData = Encoding.UTF8.GetString(message.GetRecords()[0].GetPayload());
                if (launchScreen)
                {
                    nfcData += "launch_screen";
                } else
                {
                    nfcData += "app_screen";
                }
                TagScanned?.Invoke(this, nfcData);
            }
        }

        public bool IsNfcAvailable() {
            try {
                return ((NfcManager)Android.App.Application.Context.GetSystemService(Context.NfcService)).DefaultAdapter.IsEnabled;
            } catch (Exception) {
                return false;
            }
        }
    }
}