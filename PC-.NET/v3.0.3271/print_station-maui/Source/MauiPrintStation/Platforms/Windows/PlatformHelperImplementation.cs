﻿/***********************************************
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

using System.Runtime.InteropServices;
using System.Text.RegularExpressions;
using MauiPrintStation;

namespace MauiPrintStation
{
    public class PlatformHelperImplementation : IPlatformHelper
    {

        private const int Windows10MajorVersion = 10;

        public string GetIOSBundleIdentifier() {
            throw new NotImplementedException();
        }

        public bool IsWindows10() {
            bool isWindows10 = false;
            Regex pattern = new Regex("(\\d+)");
            MatchCollection match = pattern.Matches(RuntimeInformation.OSDescription);
            if (match.Count > 0) {
                isWindows10 = Convert.ToInt32(match[0].Value) >= Windows10MajorVersion;
            }
            return isWindows10;
        }
    }
}
