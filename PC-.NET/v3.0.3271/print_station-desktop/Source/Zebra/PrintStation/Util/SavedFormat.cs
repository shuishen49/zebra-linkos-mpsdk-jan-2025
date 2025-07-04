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

namespace Zebra.PrintStation.Util {

    public class SavedFormat {

        public const string TABLE_NAME = "saved_formats_table";
        public const string _ID = "id";
        public const string FORMAT_DRIVE = "format_drive";
        public const string FORMAT_NAME = "format_name";
        public const string FORMAT_EXTENSION = "format_extension";
        public const string SOURCE_PRINTER_NAME = "source_printer_name";
        public const string TIMESTAMP = "timestamp";
        public const string FORMAT_TEXT = "format_text";

        public long id;
        public string formatDrive;
        public string formatName;
        public string formatExtension;
        public string sourcePrinterName;
        public long timestamp;
        public string formatText;

        public SavedFormat(long id, string formatDrive, string formatName, string formatExtension, string sourcePrinterName, long timestamp, string formatText) {
            this.id = id;
            this.formatDrive = formatDrive;
            this.formatName = formatName;
            this.formatExtension = formatExtension;
            this.sourcePrinterName = sourcePrinterName;
            this.timestamp = timestamp;
            this.formatText = formatText;
        }
    }
}
