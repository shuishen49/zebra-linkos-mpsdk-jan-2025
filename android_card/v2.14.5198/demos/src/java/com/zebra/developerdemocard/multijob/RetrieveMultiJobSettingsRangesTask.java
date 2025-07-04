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

package com.zebra.developerdemocard.multijob;

import android.content.Context;
import android.os.AsyncTask;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.ConnectionHelper;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;
import com.zebra.sdk.common.card.enumerations.TransferType;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.jobSettings.ZebraCardJobSettingNames;
import com.zebra.sdk.common.card.printer.ZebraCardPrinter;
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory;
import com.zebra.sdk.common.card.settings.ZebraCardSettingNames;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class RetrieveMultiJobSettingsRangesTask extends AsyncTask<Void, Void, MultiJobPrinterSettings> {

    private static final String COLOR_OPTION = "ymc";
    private static final List<String> MONO_RIBBON_OPTIONS = Arrays.asList("k", "mono", "black", "white", "red", "blue", "silver", "gold");
    private static final List<String> OVERLAY_RIBBON_OPTIONS = Arrays.asList("ymcko", "kro", "kdo");

    private WeakReference<Context> weakContext;
    private DiscoveredPrinter printer;
    private OnRetrieveMultiJobSettingsRangesListener onRetrieveMultiJobSettingsRangesListener;
    private Exception exception;

    public interface OnRetrieveMultiJobSettingsRangesListener {
        void onRetrieveMultiJobSettingsRangesStarted();
        void onRetrieveMultiJobSettingsRangesFinished(Exception exception, MultiJobPrinterSettings multiJobPrinterSettings);
    }

    RetrieveMultiJobSettingsRangesTask(Context context, DiscoveredPrinter printer) {
        weakContext = new WeakReference<>(context);
        this.printer = printer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onRetrieveMultiJobSettingsRangesListener != null) {
            onRetrieveMultiJobSettingsRangesListener.onRetrieveMultiJobSettingsRangesStarted();
        }
    }

    @Override
    protected MultiJobPrinterSettings doInBackground(Void... voids) {
        Connection connection = null;
        ZebraCardPrinter zebraCardPrinter = null;
        MultiJobPrinterSettings multiJobPrinterSettings = null;

        try {
            connection = printer.getConnection();
            connection.open();

            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection);

            boolean allowsColorOption = false;
            boolean allowsMonoOption = false;
            boolean allowsOverlayOption = false;
            List<String> sourceList = new ArrayList<>();
            List<String> destinationList = new ArrayList<>();
            List<String> coercivityTypeList = new ArrayList<>();

            String installedRibbon = zebraCardPrinter.getSettingValue(ZebraCardSettingNames.RIBBON_DESCRIPTION);
            if (installedRibbon != null && !installedRibbon.isEmpty()) {
                installedRibbon = installedRibbon.toLowerCase(Locale.US);

                if (installedRibbon.contains(COLOR_OPTION)) {
                    allowsColorOption = true;
                }

                if (isPrintTypeSupported(installedRibbon, MONO_RIBBON_OPTIONS)) {
                    allowsMonoOption = true;
                }

                if (isPrintTypeSupported(installedRibbon, OVERLAY_RIBBON_OPTIONS)) {
                    allowsOverlayOption = true;
                }

                String cardSourceRange = zebraCardPrinter.getJobSettingRange(ZebraCardJobSettingNames.CARD_SOURCE);
                for (CardSource source : CardSource.values()) {
                    if (cardSourceRange != null && cardSourceRange.contains(source.name())) {
                        sourceList.add(source.name());
                    }
                }

                String cardDestinationRange = zebraCardPrinter.getJobSettingRange(ZebraCardJobSettingNames.CARD_DESTINATION);
                for (CardDestination destination : CardDestination.values()) {
                    if (cardDestinationRange != null && cardDestinationRange.contains(destination.name())) {
                        if (!destination.name().contains("Laminator") || zebraCardPrinter.hasLaminator()) {
                            destinationList.add(destination.name());
                        }
                    }
                }

                boolean hasMagneticEncoder = zebraCardPrinter.hasMagneticEncoder();
                if (hasMagneticEncoder) {
                    String coercivityTypeRange = zebraCardPrinter.getJobSettingRange(ZebraCardJobSettingNames.MAG_COERCIVITY);
                    for (CoercivityType coercivity : CoercivityType.values()) {
                        if (coercivityTypeRange != null && coercivityTypeRange.contains(coercivity.name())) {
                            coercivityTypeList.add(coercivity.name());
                        }
                    }
                }

                MultiJobPrinterSettings settings = new MultiJobPrinterSettings();
                settings.setShowPrintOptimization(zebraCardPrinter.getJobSettings().contains(ZebraCardJobSettingNames.PRINT_OPTIMIZATION));
                settings.setShowBackSidePrint(zebraCardPrinter.getPrintCapability() == TransferType.DualSided);
                settings.setShowMagEncoding(hasMagneticEncoder);
                settings.setAllowsColorOption(allowsColorOption);
                settings.setAllowsMonoOption(allowsMonoOption);
                settings.setAllowsOverlayOption(allowsOverlayOption);
                settings.setCardSourceRange(sourceList);
                settings.setCardDestinationRange(destinationList);
                settings.setCardCoercivityTypeRange(coercivityTypeList);
                multiJobPrinterSettings = settings;
            } else {
                throw new ZebraCardException(weakContext.get().getString(R.string.no_ribbon_installed_message));
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection);
        }

        return multiJobPrinterSettings;
    }

    @Override
    protected void onPostExecute(MultiJobPrinterSettings multiJobPrinterSettings) {
        super.onPostExecute(multiJobPrinterSettings);

        if (onRetrieveMultiJobSettingsRangesListener != null) {
            onRetrieveMultiJobSettingsRangesListener.onRetrieveMultiJobSettingsRangesFinished(exception, multiJobPrinterSettings);
        }
    }

    private boolean isPrintTypeSupported(String installedRibbon, List<String> ribbonTypeOptions) {
        boolean isSupported = true;
        for (String option : ribbonTypeOptions) {
            if (!installedRibbon.contains(option)) {
                isSupported = false;
            } else {
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }

    void setOnRetrieveMultiJobSettingsRangesListener(OnRetrieveMultiJobSettingsRangesListener onRetrieveMultiJobSettingsRangesListener) {
        this.onRetrieveMultiJobSettingsRangesListener = onRetrieveMultiJobSettingsRangesListener;
    }
}
