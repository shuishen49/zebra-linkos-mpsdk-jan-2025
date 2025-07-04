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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.DialogHelper;
import com.zebra.developerdemocard.util.UIHelper;
import com.zebra.developerdemocard.util.UriHelper;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSide;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;
import com.zebra.sdk.common.card.enumerations.PrintOptimizationMode;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.zebraui.ZebraChevronButton;
import com.zebra.zebraui.ZebraEditText;
import com.zebra.zebraui.ZebraSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class MultiJobFormFragment extends Fragment {

    private static final int REQUEST_SELECT_FRONT_SIDE_IMAGE = 3001;
    private static final int REQUEST_SELECT_BACK_SIDE_IMAGE = 3002;

    private boolean isInitialized = false;
    private MultiJobNumber jobNumber;
    private MultiJobPrinterSettings multiJobPrinterSettings;
    private Map<CardSide, Map<PrintType, Uri>> cardSidePrintTypeUriMap = new HashMap<>();
    private Map<CardSide, Uri> cardSideCurrentUriMap = new HashMap<>();

    private ZebraSpinnerView multiJobSources;
    private ZebraSpinnerView multiJobDestinations;
    private ZebraSpinnerView multiJobPrintOptimizationSpinner;
    private ZebraSpinnerView multiJobQuantity;

    private Switch printFrontSideSwitch;
    private ZebraEditText frontSideImageFileEditText;
    private ImageView frontSideImageFileBrowseButton;
    private ZebraSpinnerView frontSideTypesSpinner;
    private LinearLayout frontSideFullOverlayContainer;
    private Switch frontSideFullOverlaySwitch;
    private ZebraChevronButton addFrontSideImageButton;

    private LinearLayout backSideLayout;
    private Switch printBackSideSwitch;
    private ZebraEditText backSideImageFileEditText;
    private ImageView backSideImageFileBrowseButton;
    private ZebraSpinnerView backSideTypesSpinner;
    private LinearLayout backSideFullOverlayContainer;
    private Switch backSideFullOverlaySwitch;
    private ZebraChevronButton addBackSideImageButton;

    private LinearLayout magneticEncodeLayout;
    private Switch magneticEncodeSwitch;
    private ZebraSpinnerView multiJobCoercivityTypes;
    private ZebraEditText track1DataInput;
    private ZebraEditText track2DataInput;
    private ZebraEditText track3DataInput;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cardSidePrintTypeUriMap.put(CardSide.Front, new HashMap<PrintType, Uri>());
        cardSidePrintTypeUriMap.put(CardSide.Back, new HashMap<PrintType, Uri>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_multijob_setup_job, container, false);

        multiJobSources = rootView.findViewById(R.id.multiJobSources);
        multiJobDestinations = rootView.findViewById(R.id.multiJobDestinations);
        multiJobPrintOptimizationSpinner = rootView.findViewById(R.id.multiJobPrintOptimizationSpinner);
        multiJobQuantity = rootView.findViewById(R.id.multiJobQuantity);

        printFrontSideSwitch = rootView.findViewById(R.id.printFrontSideSwitch);
        frontSideImageFileEditText = rootView.findViewById(R.id.frontSideImageFileEditText);
        frontSideImageFileBrowseButton = rootView.findViewById(R.id.frontSideImageFileBrowseButton);
        frontSideTypesSpinner = rootView.findViewById(R.id.frontSideTypesSpinner);
        frontSideFullOverlayContainer = rootView.findViewById(R.id.frontSideFullOverlayContainer);
        frontSideFullOverlaySwitch = rootView.findViewById(R.id.frontSideFullOverlaySwitch);
        addFrontSideImageButton = rootView.findViewById(R.id.addFrontSideImageButton);

        backSideLayout = rootView.findViewById(R.id.backSideLayout);
        printBackSideSwitch = rootView.findViewById(R.id.printBackSideSwitch);
        backSideImageFileEditText = rootView.findViewById(R.id.backSideImageFileEditText);
        backSideImageFileBrowseButton = rootView.findViewById(R.id.backSideImageFileBrowseButton);
        backSideTypesSpinner = rootView.findViewById(R.id.backSideTypesSpinner);
        backSideFullOverlayContainer = rootView.findViewById(R.id.backSideFullOverlayContainer);
        backSideFullOverlaySwitch = rootView.findViewById(R.id.backSideFullOverlaySwitch);
        addBackSideImageButton = rootView.findViewById(R.id.addBackSideImageButton);

        magneticEncodeLayout = rootView.findViewById(R.id.magneticEncodeLayout);
        magneticEncodeSwitch = rootView.findViewById(R.id.magneticEncodeSwitch);
        multiJobCoercivityTypes = rootView.findViewById(R.id.multiJobCoercivityTypes);
        track1DataInput = rootView.findViewById(R.id.track1DataInput);
        track2DataInput = rootView.findViewById(R.id.track2DataInput);
        track3DataInput = rootView.findViewById(R.id.track3DataInput);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeSpinnerEntries();
        updateViewVisibilities();

        setCardSideChangeListeners(REQUEST_SELECT_FRONT_SIDE_IMAGE,
                CardSide.Front,
                printFrontSideSwitch,
                frontSideImageFileEditText,
                frontSideImageFileBrowseButton,
                frontSideTypesSpinner,
                frontSideFullOverlayContainer,
                frontSideFullOverlaySwitch,
                addFrontSideImageButton);

        setCardSideChangeListeners(REQUEST_SELECT_BACK_SIDE_IMAGE,
                CardSide.Back,
                printBackSideSwitch,
                backSideImageFileEditText,
                backSideImageFileBrowseButton,
                backSideTypesSpinner,
                backSideFullOverlayContainer,
                backSideFullOverlaySwitch,
                addBackSideImageButton);

        magneticEncodeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                updateMagneticEncodeEnabled(isChecked);
            }
        });

        updateCardSideLayoutEnabled(CardSide.Front, printFrontSideSwitch, frontSideImageFileEditText, frontSideImageFileBrowseButton, frontSideTypesSpinner, frontSideFullOverlayContainer, frontSideFullOverlaySwitch, addFrontSideImageButton);
        updateCardSideLayoutEnabled(CardSide.Back, printBackSideSwitch, backSideImageFileEditText, backSideImageFileBrowseButton, backSideTypesSpinner, backSideFullOverlayContainer, backSideFullOverlaySwitch, addBackSideImageButton);
        updateMagneticEncodeEnabled(magneticEncodeSwitch.isChecked());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_FRONT_SIDE_IMAGE:
                    updateSelectedImage(data, CardSide.Front, frontSideImageFileEditText);
                    break;
                case REQUEST_SELECT_BACK_SIDE_IMAGE:
                    updateSelectedImage(data, CardSide.Back, backSideImageFileEditText);
                    break;
            }
        }
    }

    public void initializeSpinnerEntries() {
        if (!isInitialized && multiJobPrinterSettings != null) {
            isInitialized = true;

            multiJobSources.setSpinnerEntries(multiJobPrinterSettings.getCardSourceRange());
            multiJobDestinations.setSpinnerEntries(multiJobPrinterSettings.getCardDestinationRange());

            List<String> printOptimizationModes = new ArrayList<>();
            for (PrintOptimizationMode mode : PrintOptimizationMode.values()) {
                printOptimizationModes.add(mode.name());
            }

            multiJobPrintOptimizationSpinner.setSpinnerEntries(printOptimizationModes);

            List<String> printTypes = new ArrayList<>();
            if (multiJobPrinterSettings.getAllowsColorOption()) { printTypes.add(PrintType.Color.name()); }
            if (multiJobPrinterSettings.getAllowsMonoOption()) { printTypes.add(PrintType.MonoK.name()); }
            if (multiJobPrinterSettings.getAllowsOverlayOption()) { printTypes.add(PrintType.Overlay.name()); }
            frontSideTypesSpinner.setSpinnerEntries(printTypes);
            backSideTypesSpinner.setSpinnerEntries(printTypes);

            multiJobCoercivityTypes.setSpinnerEntries(multiJobPrinterSettings.getCardCoercivityTypeRange());
        }
    }

    public void updateViewVisibilities() {
        if (multiJobPrinterSettings != null) {
            multiJobPrintOptimizationSpinner.setVisibility(multiJobPrinterSettings.getShowPrintOptimization() ? View.VISIBLE : View.GONE);
            backSideLayout.setVisibility(multiJobPrinterSettings.getShowBackSidePrint() ? View.VISIBLE : View.GONE);
            magneticEncodeLayout.setVisibility(multiJobPrinterSettings.getShowMagEncoding() ? View.VISIBLE : View.GONE);
        }
    }

    public MultiJobInfo buildMultiJobInfo() {
        try {
            MultiJobInfo multiJobInfo = new MultiJobInfo(CardSource.fromString((String) multiJobSources.getSelectedItem()));
            multiJobInfo.setJobNumber(jobNumber);
            multiJobInfo.setFrontSideEnabled(printFrontSideSwitch.isChecked());
            multiJobInfo.setBackSideEnabled(printBackSideSwitch.isChecked());
            multiJobInfo.setCardDestination(multiJobDestinations.getSelectedItem() != null ? CardDestination.valueOf((String) multiJobDestinations.getSelectedItem()) : null);
            multiJobInfo.setShowPrintOptimization(multiJobPrinterSettings.getShowPrintOptimization());
            multiJobInfo.setPrintOptimizationMode(multiJobPrintOptimizationSpinner.getSelectedItem() != null ? PrintOptimizationMode.fromString((String) multiJobPrintOptimizationSpinner.getSelectedItem()) : null);
            multiJobInfo.setFrontImageInfo(cardSidePrintTypeUriMap.get(CardSide.Front));
            multiJobInfo.setBackImageInfo(cardSidePrintTypeUriMap.get(CardSide.Back));
            multiJobInfo.setCoercivityType(multiJobCoercivityTypes.getSelectedItem() != null ? CoercivityType.fromString((String) multiJobCoercivityTypes.getSelectedItem()) : null);
            multiJobInfo.setShowMagEncoding(multiJobPrinterSettings.getShowMagEncoding());
            multiJobInfo.setMagEncodingEnabled(magneticEncodeSwitch.isChecked());
            multiJobInfo.setTrack1Data(track1DataInput.getText());
            multiJobInfo.setTrack2Data(track2DataInput.getText());
            multiJobInfo.setTrack3Data(track3DataInput.getText());
            multiJobInfo.setQuantity(multiJobQuantity.getSelectedItem() != null ? Integer.parseInt((String) multiJobQuantity.getSelectedItem()) : 1);
            return multiJobInfo;
        } catch (Exception e) {
            return null;
        }
    }

    private void setCardSideChangeListeners(final int graphicSelectRequestCode,
                                            final CardSide cardSide,
                                            final Switch printCardSideSwitch,
                                            final ZebraEditText imageFileEditText,
                                            final ImageView imageFileBrowseButton,
                                            final ZebraSpinnerView typesSpinner,
                                            final LinearLayout fullOverlayContainer,
                                            final Switch fullOverlaySwitch,
                                            final ZebraChevronButton addImageButton) {
        printCardSideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                updateCardSideLayoutEnabled(cardSide, printCardSideSwitch, imageFileEditText, imageFileBrowseButton, typesSpinner, fullOverlayContainer, fullOverlaySwitch, addImageButton);
            }
        });

        imageFileBrowseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(createGraphicSelectIntent(), graphicSelectRequestCode);
            }
        });

        typesSpinner.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateTypeSelection(cardSide, printCardSideSwitch, imageFileEditText, imageFileBrowseButton, typesSpinner, fullOverlayContainer, fullOverlaySwitch);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        fullOverlaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                updateImageInputEnabled(cardSide, printCardSideSwitch, imageFileEditText, imageFileBrowseButton, fullOverlayContainer, fullOverlaySwitch);
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCardSideImage(cardSide, typesSpinner, fullOverlaySwitch);
            }
        });
    }

    private void updateSelectedImage(Intent data, CardSide cardSide, ZebraEditText imageFileEditText) {
        Uri uri = data.getData();
        String filename = UriHelper.getFilename(getActivity(), uri);
        if (filename != null && !filename.trim().isEmpty()) {
            cardSideCurrentUriMap.put(cardSide, uri);
            imageFileEditText.setText(filename);
        } else {
            cardSideCurrentUriMap.remove(cardSide);
            imageFileEditText.setText(null);
        }
    }

    private void addCardSideImage(CardSide cardSide, ZebraSpinnerView typesSpinner, Switch fullOverlaySwitch) {
        String type = typesSpinner.getSelectedItem().toString();
        boolean successfullyAdded = false;
        Uri currentCardSideImageUri = cardSideCurrentUriMap.get(cardSide);

        if (type.equals(PrintType.Overlay.name()) && fullOverlaySwitch.isChecked()) {
            cardSidePrintTypeUriMap.get(cardSide).remove(PrintType.valueOf(type));
            successfullyAdded = true;
        } else if (currentCardSideImageUri != null) {
            cardSidePrintTypeUriMap.get(cardSide).put(PrintType.valueOf(type), currentCardSideImageUri);
            successfullyAdded = true;
        }

        if (successfullyAdded) {
            UIHelper.showSnackbar(getActivity(), getString(R.string.image_added_successfully_message, type));
        } else {
            DialogHelper.showErrorDialog(getActivity(), getString(R.string.unable_to_add_image_message, type));
        }
    }

    private void updateTypeSelection(CardSide cardSide, Switch printCardSideSwitch, ZebraEditText imageFileEditText, ImageView imageFileBrowseButton, ZebraSpinnerView typesSpinner, LinearLayout fullOverlayContainer, Switch fullOverlaySwitch) {
        String selectedItem = typesSpinner.getSelectedItem().toString();
        if (selectedItem != null) {
            boolean isOverlaySelected = selectedItem.equalsIgnoreCase(PrintType.Overlay.name());
            fullOverlayContainer.setVisibility(isOverlaySelected ? View.VISIBLE : View.GONE);

            updateImageInputEnabled(cardSide, printCardSideSwitch, imageFileEditText, imageFileBrowseButton, fullOverlayContainer, fullOverlaySwitch);
        }
    }

    private void updateCardSideLayoutEnabled(CardSide cardSide, Switch printCardSideSwitch, ZebraEditText imageFileEditText, ImageView imageFileBrowseButton, ZebraSpinnerView typesSpinner, LinearLayout fullOverlayContainer, Switch fullOverlaySwitch, ZebraChevronButton addImageButton) {
        updateImageInputEnabled(cardSide, printCardSideSwitch, imageFileEditText, imageFileBrowseButton, fullOverlayContainer, fullOverlaySwitch);

        boolean enabled = printCardSideSwitch.isChecked();
        typesSpinner.setEnabled(enabled);
        fullOverlaySwitch.setEnabled(enabled);
        addImageButton.setEnabled(enabled);
    }

    private void updateImageInputEnabled(CardSide cardSide, Switch printCardSideSwitch, ZebraEditText imageFileEditText, ImageView imageFileBrowseButton, LinearLayout fullOverlayContainer, Switch fullOverlaySwitch) {
        boolean fullOverlayDisabled = fullOverlayContainer.getVisibility() != View.VISIBLE || !fullOverlaySwitch.isChecked();
        boolean enabled = fullOverlayDisabled && printCardSideSwitch.isChecked();

        cardSideCurrentUriMap.remove(cardSide);

        imageFileEditText.setText(null);
        imageFileEditText.setEnabled(enabled);

        imageFileBrowseButton.setEnabled(enabled);
        imageFileBrowseButton.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void updateMagneticEncodeEnabled(boolean enabled) {
        multiJobCoercivityTypes.setEnabled(enabled);
        track1DataInput.setEnabled(enabled);
        track2DataInput.setEnabled(enabled);
        track3DataInput.setEnabled(enabled);
    }

    private Intent createGraphicSelectIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        return Intent.createChooser(getIntent, getString(R.string.select_graphic));
    }

    public void updateMultiJobPrinterSettings(MultiJobPrinterSettings multiJobPrinterSettings) {
        this.multiJobPrinterSettings = multiJobPrinterSettings;
        initializeSpinnerEntries();
        updateViewVisibilities();
    }

    public MultiJobFormFragment setJobNumber(MultiJobNumber jobNumber) {
        this.jobNumber = jobNumber;
        return this;
    }
}
