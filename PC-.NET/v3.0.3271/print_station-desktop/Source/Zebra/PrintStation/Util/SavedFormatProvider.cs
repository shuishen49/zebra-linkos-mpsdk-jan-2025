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
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Data.SQLite;
using System.IO;

namespace Zebra.PrintStation.Util {

    public class SavedFormatProvider : IDisposable {

        private const string DATE_FORMAT = "d MMM yyyy HH:mm:ss";
        private static string DATABASE_PATH = $"{Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData)}\\Zebra Technologies\\PrintStation\\";
        private static string DATABASE_NAME = "saved_formats.db";
        private const int DATABASE_VERSION = 3;

        private SQLiteConnection dbConnection;
        private long formatId = 1;

        public SavedFormatProvider() {
            try {
                InitializeDatabase();
                InitializeDatabaseConnection();
                InsertSampleFormats();
            } finally {
                CloseDataBase();
            }
        }

        private void InitializeDatabaseConnection() {
            dbConnection = new SQLiteConnection($"Data Source={DATABASE_PATH + DATABASE_NAME}; Version={DATABASE_VERSION};");
            dbConnection.Open();
        }

        private static void InitializeDatabase() {
            try {
                if (!Directory.Exists(DATABASE_PATH)) {
                    Directory.CreateDirectory(DATABASE_PATH);
                }

                if (!File.Exists(DATABASE_PATH + DATABASE_NAME)) {
                    SQLiteConnection.CreateFile(DATABASE_PATH + DATABASE_NAME);
                }
            } catch (Exception e) {
                PrinterErrorsWindow errorWindow = new PrinterErrorsWindow($"Error creating database: {e.Message}.");
                errorWindow.ShowDialog();
            }
        }

        private void InsertSampleFormats() {
            int tableCreated = -1;
            string createTableSql = "CREATE TABLE IF NOT EXISTS " + SavedFormat.TABLE_NAME + " ("
                    + SavedFormat._ID + " INTEGER PRIMARY KEY,"
                    + SavedFormat.FORMAT_DRIVE + " TEXT,"
                    + SavedFormat.FORMAT_NAME + " TEXT,"
                    + SavedFormat.FORMAT_EXTENSION + " TEXT,"
                    + SavedFormat.SOURCE_PRINTER_NAME + " TEXT,"
                    + SavedFormat.FORMAT_TEXT + " TEXT,"
                    + SavedFormat.TIMESTAMP + " INTEGER"
                    + ");";

            using (SQLiteCommand createTableCommand = new SQLiteCommand(createTableSql, dbConnection)) {
                tableCreated = createTableCommand.ExecuteNonQuery();
            }

            if (tableCreated == 0) {
                string oilchangeFormat = "^XA" +
                                            "^CI28" +
                                            "^DFE:OILCHANGE.ZPL^FS" +
                                            "^FT181,184^A0N,28,28^FH\\^FN1\"Date\"^FS" +
                                            "^FT181,282^A0N,28,28^FH\\^FN2\"Mileage\"^FS" +
                                            "^FT32,106^GFA,1024,1024,16::::::::::::::L0F8,L0HFI0IFC0,L0HFC00FHFC0K0380,K01FHFC0FHFC0J01FC0,K03E7FF00780K03FE0,K03E1FF00780J01FHF0,K03C03F00780J07FDF8,K07800700780I01FF8F0,K078007FKF807FF870,K07F007FKFC1FHF0,K07FF07FLF7FDF0,K03FFC7FNF3E0,L0JFL0HFC3E010,M0IFL07F07C010,M03FF0K03C078018,N03F0N0F8018,O0F0M01F0038,O070M01E003C,O070M03E003C,O070M07C003C,O070M0780030,O07FNF8,O07FNF0,:O07FMFE0,,:::::::::::::::::::::::^FT60,177^A0N,34,33^FH\\^FDDATE^FS" +
                                            "^FT33,282^A0N,34,33^FH\\^FDMILEAGE^FS" +
                                            "^FO172,239^GB287,64,8^FS" +
                                            "^FO172,139^GB287,64,8^FS" +
                                            "^FT153,77^A0N,56,55^FH\\^FDOIL CHANGE^FS" +
                                            "^FO27,110^GB432,0,8^FS" +
                                            "^XZ";

                string oilChangeSql = "INSERT OR IGNORE INTO " + SavedFormat.TABLE_NAME + " (" +
                                                SavedFormat._ID + ", " +
                                                SavedFormat.FORMAT_DRIVE + ", " +
                                                SavedFormat.FORMAT_NAME + ", " +
                                                SavedFormat.FORMAT_EXTENSION + ", " +
                                                SavedFormat.SOURCE_PRINTER_NAME + ", " +
                                                SavedFormat.FORMAT_TEXT + ", " +
                                                SavedFormat.TIMESTAMP + ") " +
                                            "VALUES (" + formatId++ + ", 'E:', 'OILCHANGE', '.ZPL', 'Sample', '" + oilchangeFormat + "', " + System.DateTime.Now.Ticks + ")";

                using (SQLiteCommand insertOilChangeCommand = new SQLiteCommand(oilChangeSql, dbConnection)) {
                    insertOilChangeCommand.ExecuteNonQuery();
                }

                string addressFormat = "^XA" +
                                        "^CI28" +
                                        "^DFE:ADDRESS.ZPL^FS" +
                                        "^FT80,80^A0N,28,28^FH\\^FN1\"Name\"^FS" +
                                        "^FT80,110^A0N,28,28^FH\\^FN2\"Address1\"^FS" +
                                        "^FT80,140^A0N,28,28^FH\\^FN3\"Address2\"^FS" +
                                        "^FT80,170^A0N,28,28^FH\\^FN4\"CityStateZip\"^FS" +
                                        "^XZ";

                string addressFormatSql = "INSERT OR IGNORE INTO " + SavedFormat.TABLE_NAME + " (" +
                                                SavedFormat._ID + ", " +
                                                SavedFormat.FORMAT_DRIVE + ", " +
                                                SavedFormat.FORMAT_NAME + ", " +
                                                SavedFormat.FORMAT_EXTENSION + ", " +
                                                SavedFormat.SOURCE_PRINTER_NAME + ", " +
                                                SavedFormat.FORMAT_TEXT + ", " +
                                                SavedFormat.TIMESTAMP + ") " +
                                            "VALUES (" + formatId++ + ", 'E:', 'ADDRESS', '.ZPL', 'Sample', '" + addressFormat + "', " + System.DateTime.Now.Ticks + ")";

                using (SQLiteCommand insertAddressFormatCommand = new SQLiteCommand(addressFormatSql, dbConnection)) {
                    insertAddressFormatCommand.ExecuteNonQuery();
                }
            } else {
                string sql = "SELECT * FROM " + SavedFormat.TABLE_NAME + " ORDER BY id DESC LIMIT 1";
                using (SQLiteCommand getLargestIdCommand = new SQLiteCommand(sql, dbConnection)) {
                    using (SQLiteDataReader dr = getLargestIdCommand.ExecuteReader()) {
                        while (dr.Read()) {
                            NameValueCollection formatValues = dr.GetValues();
                            formatId = long.Parse(formatValues[SavedFormat._ID]) + 1;
                        }
                    }
                }
            }
        }

        public List<SavedFormat> GetSavedFormats() {
            List<SavedFormat> savedFormats = new List<SavedFormat>();
            try {
                if (dbConnection != null) {
                    dbConnection.Open();
                } else {
                    InitializeDatabaseConnection();
                }

                string sql = "SELECT * FROM " + SavedFormat.TABLE_NAME;
                using (SQLiteCommand command = new SQLiteCommand(sql, dbConnection)) {
                    using (SQLiteDataReader dr = command.ExecuteReader()) {
                        while (dr.Read()) {
                            NameValueCollection formatValues = dr.GetValues();
                            SavedFormat format = new SavedFormat(long.Parse(formatValues[SavedFormat._ID]), formatValues[SavedFormat.FORMAT_DRIVE], formatValues[SavedFormat.FORMAT_NAME], 
                                formatValues[SavedFormat.FORMAT_EXTENSION], formatValues[SavedFormat.SOURCE_PRINTER_NAME], long.Parse(formatValues[SavedFormat.TIMESTAMP]), formatValues[SavedFormat.FORMAT_TEXT]);
                            savedFormats.Add(format);
                        }
                    }
                }
            } finally {
                CloseDataBase();
            }
            return savedFormats;
        }

        public string GetFormatContents(long id) {
            string formatContents = "";
            try {
                if (dbConnection != null) {
                    dbConnection.Open();
                } else {
                    InitializeDatabaseConnection();
                }

                string sql = "select * from " + SavedFormat.TABLE_NAME + " where id = " + id;
                using (SQLiteCommand command = new SQLiteCommand(sql, dbConnection)) {
                    using (SQLiteDataReader dr = command.ExecuteReader()) {
                        while (dr.Read()) {
                            NameValueCollection formatValues = dr.GetValues();
                            formatContents = formatValues[SavedFormat.FORMAT_TEXT];
                        }
                    }
                }
            } finally {
                CloseDataBase();
            }
            return formatContents;
        }

        public void Delete(long id) {
            try {
                if (dbConnection != null) {
                    dbConnection.Open();
                } else {
                    InitializeDatabaseConnection();
                }

                string deleteSql = "delete from " + SavedFormat.TABLE_NAME + " where id = " + id;
                using (SQLiteCommand deleteSavedFormatCommand = new SQLiteCommand(deleteSql, dbConnection)) {
                    deleteSavedFormatCommand.ExecuteNonQuery();
                }
            } finally {
                CloseDataBase();
            }
        }

        public void Insert(string formatName, string extension, string format) {
            try {
                string addressFormatSql = "INSERT OR IGNORE INTO " + SavedFormat.TABLE_NAME + " (" +
                                                    SavedFormat._ID + ", " +
                                                    SavedFormat.FORMAT_DRIVE + ", " +
                                                    SavedFormat.FORMAT_NAME + ", " +
                                                    SavedFormat.FORMAT_EXTENSION + ", " +
                                                    SavedFormat.SOURCE_PRINTER_NAME + ", " +
                                                    SavedFormat.FORMAT_TEXT + ", " +
                                                    SavedFormat.TIMESTAMP + ") " +
                                                "VALUES (" + formatId++ + ", 'E:', '" + formatName + "', '" + extension + "', 'Sample', '" + format + "', " + System.DateTime.Now.Ticks + ")";

                if (dbConnection != null) {
                    dbConnection.Open();
                    using (SQLiteCommand insertAddressFormatCommand = new SQLiteCommand(addressFormatSql, dbConnection)) {
                        insertAddressFormatCommand.ExecuteNonQuery();
                    }
                } else {
                    PrinterErrorsWindow errorWindow = new PrinterErrorsWindow("The database could not be found.");
                    errorWindow.ShowDialog();
                }
            } finally {
                CloseDataBase();
            }
        }

        private void CloseDataBase() {
            if (dbConnection != null) {
                dbConnection.Close();
            }
        }

        #region IDisposable Support
        private bool disposedValue = false; // To detect redundant calls

        protected virtual void Dispose(bool disposing) {
            if (!disposedValue) {
                if (disposing) {
                    if (dbConnection != null) {
                        CloseDataBase();
                        ((IDisposable)dbConnection).Dispose();
                        dbConnection = null;
                    }
                }

                disposedValue = true;
            }
        }

        // This code added to correctly implement the disposable pattern.
        public void Dispose() {
            // Do not change this code. Put cleanup code in Dispose(bool disposing) above.
            Dispose(true);
        }
        #endregion
    }
}
