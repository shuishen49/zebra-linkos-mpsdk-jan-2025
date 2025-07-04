package com.zebra.desktop.devdemos.chinese;

// IMPORTANT: Ensure this Java file is saved with UTF-8 encoding to correctly handle Chinese characters in string literals.

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset; // For checking charset support

public class CpclChinesePrinter {

    private String printerIpAddress;
    private int printerPort;
    private String targetEncoding = "GBK"; // Or "GB2312". GBK is a superset. This is often implied by "COUNTRY CHINA"

    public CpclChinesePrinter(String ipAddress, int port) {
        this.printerIpAddress = ipAddress;
        this.printerPort = port;

        // Check if the target encoding is supported by this Java environment
        if (!Charset.isSupported(targetEncoding)) {
            System.err.println("WARNING: Java Charset " + targetEncoding + " is not supported by this JVM. Printing might fail.");
            // Fallback or throw error if critical, for now just warn
        }
    }

    /**
     * Constructs the CPCL command for printing Chinese text.
     * @param textToPrint The Chinese string to print.
     * @param fontName The name of the font on the printer (e.g., "SIMSUN-01").
     * @param fontSize The size of the font.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The fully formed CPCL command string.
     */
    public String buildCpclPrintCommand(String textToPrint, String fontName, int fontSize, int x, int y) {
        // Basic CPCL structure
        // ! U1 JOURNAL
        // ! U1 SETLP 7 0 24
        // ! U1 PAGE-WIDTH 550
        // ! U1 COUNTRY CHINA
        // TEXT 4 0 30 40 Hello World
        // ! U1 FORM
        // ! U1 PRINT

        // Using the user's provided command structure as a base for the TEXT command
        // TEXT FONTNAME POINT-SIZE X Y DATA
        String labelHeight = "400"; // Default label height, can be adjusted
        String labelQuantity = "1";
        String pagePrintWidth = "560"; // For a ~2 inch wide label on a 203 dpi printer (2.75 inches * 203 dpi)

        StringBuilder cpclCommands = new StringBuilder();
        // Initialize label, set units (dots), label height, quantity
        cpclCommands.append("! 0 200 200 ").append(labelHeight).append(" ").append(labelQuantity).append("\r\n");
        // Set page width (printable area)
        cpclCommands.append("PAGE-WIDTH ").append(pagePrintWidth).append("\r\n");
        // Set country to China, which often sets default encoding for Chinese characters (e.g., GB2312/GBK)
        cpclCommands.append("COUNTRY CHINA\r\n");
        // Add the text command
        cpclCommands.append(String.format("TEXT %s %d %d %d %s\r\n", fontName, fontSize, x, y, textToPrint));
        // End form and print
        cpclCommands.append("FORM\r\n");
        cpclCommands.append("PRINT\r\n");

        return cpclCommands.toString();
    }

    /**
     * Constructs CPCL commands for printing multiple lines of Chinese text.
     * @param lines An array of CpclLine objects, each defining a line of text.
     * @return The fully formed CPCL command string.
     */
    public String buildMultipleLinesCpclCommand(CpclLine[] lines) {
        String labelHeight = "400";
        String labelQuantity = "1";
        String pagePrintWidth = "560";

        StringBuilder cpclCommands = new StringBuilder();
        cpclCommands.append("! 0 200 200 ").append(labelHeight).append(" ").append(labelQuantity).append("\r\n");
        cpclCommands.append("PAGE-WIDTH ").append(pagePrintWidth).append("\r\n");
        cpclCommands.append("COUNTRY CHINA\r\n");

        for (CpclLine line : lines) {
            cpclCommands.append(String.format("TEXT %s %d %d %d %s\r\n",
                                            line.fontName, line.fontSize, line.x, line.y, line.text));
        }

        cpclCommands.append("FORM\r\n");
        cpclCommands.append("PRINT\r\n");
        return cpclCommands.toString();
    }


    /**
     * Sends the CPCL command string to the printer.
     * @param cpclData The CPCL command string.
     * @return true if successful, false otherwise.
     */
    public boolean sendCpclToPrinter(String cpclData) {
        Connection connection = new TcpConnection(this.printerIpAddress, this.printerPort);
        try {
            System.out.println("Connecting to CPCL printer: " + this.printerIpAddress + ":" + this.printerPort + "...");
            connection.open();
            System.out.println("Connected.");

            System.out.println("Sending CPCL data (encoded as " + targetEncoding + "):\n" + cpclData);

            byte[] dataToSend = cpclData.getBytes(targetEncoding);
            connection.write(dataToSend);

            System.out.println(targetEncoding + " CPCL data sent successfully.");
            return true;

        } catch (UnsupportedEncodingException e) {
            System.err.println("Encoding Error: " + targetEncoding + " is not supported. " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        catch (ConnectionException e) {
            System.err.println("Connection Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("General Error during printing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                if (connection.isConnected()) {
                    connection.close();
                    System.out.println("Connection closed.");
                }
            } catch (ConnectionException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Helper class for defining lines of text
    public static class CpclLine {
        String text;
        String fontName;
        int fontSize;
        int x;
        int y;

        public CpclLine(String text, String fontName, int fontSize, int x, int y) {
            this.text = text;
            this.fontName = fontName;
            this.fontSize = fontSize;
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        // --- Configuration ---
        String printerIp = "192.168.1.101"; // !!! IMPORTANT: Replace with your CPCL printer's IP address !!!
        int cpclPort = 6101; // Default CPCL port

        CpclChinesePrinter cpclPrinter = new CpclChinesePrinter(printerIp, cpclPort);

        String font = "SIMSUN-01"; // As per user's request
        int size = 24;

        // Define lines to print
        CpclLine line1 = new CpclLine("你好，世界！", font, size, 30, 80); // "你好，世界！"
        CpclLine line2 = new CpclLine("蓝牙打印测试", font, size, 30, 120); // "蓝牙打印测试"

        CpclLine[] linesToPrint = {line1, line2};

        System.out.println("--- Building CPCL Command for multiple lines ---");
        String cpclCommand = cpclPrinter.buildMultipleLinesCpclCommand(linesToPrint);

        System.out.println("--- Sending CPCL to Printer ---");
        boolean success = cpclPrinter.sendCpclToPrinter(cpclCommand);

        if (success) {
            System.out.println("CPCL command sent to printer successfully.");
        } else {
            System.err.println("Failed to send CPCL command to printer.");
        }
    }
}
