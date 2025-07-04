/*
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
 */

package com.zebra.developerdemocard.discovery;

import android.os.AsyncTask;

import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.developerdemocard.util.ThreadSleeper;
import com.zebra.sdk.comm.CardConnectionReestablisher;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.printer.ZebraPrinterZmotif;
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork;
import com.zebra.sdk.printer.CardPrinterReconnectionHandler;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.util.Map;

public class ReconnectPrinterTask extends AsyncTask<Void, Void, Void> {

    private static final int WAIT_TIME_PRINTER_RESET = 5000;
    private static final long TIMEOUT_NETWORK_REESTABLISHMENT = 60000;

    private DiscoveredPrinter printer;
    private OnReconnectPrinterListener onPrinterDiscoveryListener;
    private Exception exception;
    private boolean resetPrinter;
    private boolean isBackgroundTaskFinished = false;

    public interface OnReconnectPrinterListener {
        void onReconnectPrinterStarted();
        void onReconnectPrinterFinished(Exception exception);
    }

    public ReconnectPrinterTask(DiscoveredPrinter printer, boolean resetPrinter) {
        this.printer = printer;
        this.resetPrinter = resetPrinter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onPrinterDiscoveryListener != null) {
            onPrinterDiscoveryListener.onReconnectPrinterStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ZebraPrinterZmotif zebraPrinterZmotif = null;
        Connection connection = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraPrinterZmotif = ZebraCardPrinterFactory.getZmotifPrinter(connection);

            if (resetPrinter) {
                zebraPrinterZmotif.reset();
            } else {
                zebraPrinterZmotif.resetNetwork();
            }

            ThreadSleeper.sleep(WAIT_TIME_PRINTER_RESET);

            if (printer instanceof DiscoveredCardPrinterNetwork) {
                ReconnectionHandler reconnectionHandler = new ReconnectionHandler();
                CardConnectionReestablisher reestablisher = (CardConnectionReestablisher) connection.getConnectionReestablisher(TIMEOUT_NETWORK_REESTABLISHMENT);
                reestablisher.reestablishConnection(reconnectionHandler);

                ConnectionHelper.cleanUpQuietly(zebraPrinterZmotif, connection);

                while (!reconnectionHandler.isPrinterOnline()) {
                    ThreadSleeper.sleep(100);
                }

                connection = reconnectionHandler.getZebraCardPrinter().getConnection();
                connection.open();

                Map<String, String> discoveryDataMap = printer.getDiscoveryDataMap();
                discoveryDataMap.put("ADDRESS", ((TcpConnection) connection).getAddress());
                discoveryDataMap.put("PORT_NUMBER", ((TcpConnection) connection).getPortNumber());

                DiscoveredPrinter newPrinter = new DiscoveredCardPrinterNetwork(discoveryDataMap);
                SelectedPrinterManager.setSelectedPrinter(newPrinter);
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraPrinterZmotif, connection);
            isBackgroundTaskFinished = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onPrinterDiscoveryListener != null) {
            onPrinterDiscoveryListener.onReconnectPrinterFinished(exception);
        }
    }

    public void setOnPrinterDiscoveryListener(OnReconnectPrinterListener onPrinterDiscoveryListener) {
        this.onPrinterDiscoveryListener = onPrinterDiscoveryListener;
    }

    public boolean isBackgroundTaskFinished() {
        return isBackgroundTaskFinished;
    }

    class ReconnectionHandler implements CardPrinterReconnectionHandler {

        private ZebraCardPrinter zebraCardPrinter;
        private boolean isPrinterOnline = false;

        @Override
        public void progressUpdate(String status, int percentComplete) {
            // Do nothing
        }

        @Override
        public void printerOnline(ZebraCardPrinter printer, String firmwareVersion) {
            zebraCardPrinter = printer;
            isPrinterOnline = true;
        }

        public ZebraCardPrinter getZebraCardPrinter() {
            return zebraCardPrinter;
        }

        boolean isPrinterOnline() {
            return isPrinterOnline;
        }
    }
}
