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

package com.zebra.card.devdemo.multijob;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.zebra.card.devdemo.PrinterModel;
import com.zebra.card.devdemo.utils.FileHelper;
import com.zebra.sdk.common.card.enumerations.CardDestination;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.CoercivityType;
import com.zebra.sdk.common.card.enumerations.PrintOptimizationMode;
import com.zebra.sdk.common.card.enumerations.PrintType;

public class Job {

	private JDialog demoDialog;

	public Map<PrintType, String> frontImageInfo;
	public Map<PrintType, String> backImageInfo;

	private JPanel panel;

	private JButton frontSubmitButton;
	private JButton backSubmitButton;
	private JButton frontImageBrowseButton;
	private JButton backImageBrowseButton;

	public JCheckBox frontCheckBox;
	public JCheckBox backCheckBox;
	private JCheckBox frontOverlayCheckBox;
	private JCheckBox backOverlayCheckBox;
	public JCheckBox magEncodeCheckBox;

	public JComboBox sourceComboBox;
	public JComboBox destinationComboBox;
	public JComboBox printOptimizationComboBox;
	public JComboBox quantityComboBox;
	public JComboBox coercivityTypeComboBox;
	private JComboBox frontPrintTypeComboBox;
	private JComboBox backPrintTypeComboBox;

	public JLabel printOptimizationLabel;
	public JLabel jobStatusLabel;

	public JTextField jobStatus;
	private JTextField frontFileNameTextField;
	private JTextField backFileNameTextField;
	public JTextField track1DataTextField;
	public JTextField track2DataTextField;
	public JTextField track3DataTextField;

	private ActionListener frontSubmitButtonListener;
	private ActionListener frontPrintTypeComboBoxListener;
	private ActionListener backSubmitButtonListener;
	private ActionListener backPrintTypeComboBoxListener;

	private ChangeListener frontCheckBoxListener;
	private ChangeListener frontOverlayCheckBoxListener;
	private ChangeListener backCheckBoxListener;
	private ChangeListener backOverlayCheckBoxListener;
	private ChangeListener magEncodeCheckBoxListener;

	public JPanel createJob(JDialog dialog) {
		demoDialog = dialog;
		panel = new JPanel();

		frontImageInfo = new HashMap<PrintType, String>();
		backImageInfo = new HashMap<PrintType, String>();

		frontFileNameTextField = new JTextField();
		backFileNameTextField = new JTextField();
		track1DataTextField = new JTextField();
		track2DataTextField = new JTextField();
		track3DataTextField = new JTextField();

		sourceComboBox = new JComboBox();
		destinationComboBox = new JComboBox();
		printOptimizationComboBox = new JComboBox();
		quantityComboBox = new JComboBox();
		frontPrintTypeComboBox = new JComboBox();
		backPrintTypeComboBox = new JComboBox();
		coercivityTypeComboBox = new JComboBox();

		frontSubmitButton = new JButton("Add Image");
		backSubmitButton = new JButton("Add Image");

		frontCheckBox = new JCheckBox();
		backCheckBox = new JCheckBox();
		frontOverlayCheckBox = new JCheckBox();
		backOverlayCheckBox = new JCheckBox();
		magEncodeCheckBox = new JCheckBox();

		setUpPrintPanels();
		return panel;
	}

	public void cleanUp() {
		frontCheckBox.setSelected(false);
		backCheckBox.setSelected(false);
		frontOverlayCheckBox.setSelected(false);
		backOverlayCheckBox.setSelected(false);
		magEncodeCheckBox.setSelected(false);

		frontFileNameTextField.setText("");
		backFileNameTextField.setText("");
		track1DataTextField.setText("");
		track2DataTextField.setText("");
		track3DataTextField.setText("");

		frontImageInfo.clear();
		backImageInfo.clear();

		frontSubmitButton.removeActionListener(frontSubmitButtonListener);
		backSubmitButton.removeActionListener(backSubmitButtonListener);
		frontPrintTypeComboBox.removeActionListener(frontPrintTypeComboBoxListener);
		backPrintTypeComboBox.removeActionListener(backPrintTypeComboBoxListener);

		frontCheckBox.removeChangeListener(frontCheckBoxListener);
		backCheckBox.removeChangeListener(backCheckBoxListener);
		frontOverlayCheckBox.removeChangeListener(frontOverlayCheckBoxListener);
		backOverlayCheckBox.removeChangeListener(backOverlayCheckBoxListener);
		magEncodeCheckBox.removeChangeListener(magEncodeCheckBoxListener);
	}

	public void setUpPrintPanels(JobSettingOptions printOptions) {
		panel.removeAll();
		panel.add(createLowerPanel(printOptions), BorderLayout.PAGE_END);
	}

	private void setUpPrintPanels() {
		panel.add(createLowerPanel(new JobSettingOptions()), BorderLayout.PAGE_END);
	}

	private JPanel createLowerPanel(JobSettingOptions printOptions) {
		JPanel lowerPart = new JPanel();
		lowerPart.setLayout(new BoxLayout(lowerPart, BoxLayout.PAGE_AXIS));
		lowerPart.add(createJobSettingPanel(printOptions));
		lowerPart.add(createFrontSidePanel(printOptions));
		lowerPart.add(createBackSidePanel(printOptions));
		lowerPart.add(createMagEncodingPanel(printOptions.isMagneticEncodingShown()));
		lowerPart.setBorder(new EmptyBorder(10, 10, 10, 10));
		return lowerPart;
	}

	private JPanel createJobSettingPanel(JobSettingOptions printOptions) {
		JPanel jobSettingPanel = new JPanel();
		jobSettingPanel.setLayout(new BoxLayout(jobSettingPanel, BoxLayout.PAGE_AXIS));
		jobSettingPanel.setBorder(new TitledBorder("Job Settings"));

		JPanel jobSettingsArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		JLabel sourceLabel = new JLabel("Source");
		jobSettingsArea.add(sourceLabel);

		sourceComboBox.removeAllItems();
		for (CardSource source : CardSource.values()) {
			if (!printOptions.getCardSourceRange().isEmpty() && printOptions.getCardSourceRange().contains(source.name())) {
				sourceComboBox.addItem(source.name());
			}
		}

		sourceComboBox.setSelectedItem(CardSource.Feeder.name());
		jobSettingsArea.add(sourceComboBox);

		JLabel destinationLabel = new JLabel("Destination");
		jobSettingsArea.add(destinationLabel);

		destinationComboBox.removeAllItems();
		for (CardDestination destination : CardDestination.values()) {
			if (!printOptions.getCardDestinationRange().isEmpty() && printOptions.getCardDestinationRange().contains(destination.name())) {
				if (!destination.name().contains("Laminator") || printOptions.areLaminatorDestinationsShown()) {
					destinationComboBox.addItem(destination.name());
				}
			}
		}

		destinationComboBox.setSelectedItem(CardDestination.Eject.name());
		jobSettingsArea.add(destinationComboBox);

		printOptimizationLabel = new JLabel("Print Optimization");
		jobSettingsArea.add(printOptimizationLabel);
		printOptimizationLabel.setVisible(printOptions.isPrintOptimizationShown());

		printOptimizationComboBox.removeAllItems();
		for (PrintOptimizationMode mode : PrintOptimizationMode.values()) {
			printOptimizationComboBox.addItem(mode.name());
		}

		printOptimizationComboBox.setSelectedItem(PrintOptimizationMode.Speed.name());
		printOptimizationComboBox.setVisible(printOptions.isPrintOptimizationShown());
		jobSettingsArea.add(printOptimizationComboBox);

		JLabel quantityLabel = new JLabel("Quantity");
		jobSettingsArea.add(quantityLabel);
		quantityComboBox.removeAllItems();

		quantityComboBox.addItem(1);
		quantityComboBox.addItem(2);
		quantityComboBox.addItem(3);
		quantityComboBox.addItem(4);
		quantityComboBox.addItem(5);
		jobSettingsArea.add(quantityComboBox);

		jobSettingPanel.add(jobSettingsArea);
		return jobSettingPanel;
	}

	private JPanel createPrintFrontSideCheckboxPanel() {
		frontCheckBox.addChangeListener(frontCheckBoxListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setFrontSideEnabled(frontCheckBox.isSelected());
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		panel.add(frontCheckBox);
		panel.add(new JLabel("Print Front Side"));
		return panel;
	}

	private JPanel createFrontSideImageSelectionPanel() {
		frontFileNameTextField.setPreferredSize(new Dimension(350, 25));

		frontImageBrowseButton = new JButton("Browse");
		frontImageBrowseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (FileHelper.getBmpFileChooser().showOpenDialog(demoDialog) == JFileChooser.APPROVE_OPTION) {
					frontFileNameTextField.setText(FileHelper.getBmpFileChooser().getSelectedFile().getAbsolutePath());
				}
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		panel.add(new JLabel("Image File"));
		panel.add(frontFileNameTextField);
		panel.add(frontImageBrowseButton);
		return panel;
	}

	private void addFrontSideImage() {
		String type = frontPrintTypeComboBox.getSelectedItem().toString();
		String imageLocation = frontFileNameTextField.getText();
		boolean successfullyAdded = false;

		File file = new File(imageLocation);
		if (file.exists() && !file.isDirectory()) {
			if (!imageLocation.isEmpty()) {
				frontImageInfo.put(PrintType.valueOf(type), imageLocation);
				successfullyAdded = true;
			}
		} else if (type.equals(PrintType.Overlay.name()) && frontOverlayCheckBox.isSelected()) {
			frontImageInfo.put(PrintType.valueOf(type), null);
			successfullyAdded = true;
		}

		if (successfullyAdded) {
			PrinterModel.showInformationDialog("Front Image", "The " + type + " image was added successfully.");
		} else {
			PrinterModel.showInformationDialog("Front Image", "Unable to add the " + type + " image.\nCheck your filepath and try again.");
		}
	}

	private void selectFrontSidePrintType() {
		String selectedItem = frontPrintTypeComboBox.getSelectedItem().toString();
		if (selectedItem != null) {
			if (selectedItem.equalsIgnoreCase(PrintType.Overlay.name())) {
				frontOverlayCheckBox.setVisible(true);
				if (frontOverlayCheckBox.isSelected()) {
					frontFileNameTextField.setText("");
					frontFileNameTextField.setEditable(false);
					frontImageBrowseButton.setEnabled(false);
				}
			} else {
				frontOverlayCheckBox.setVisible(false);
				frontFileNameTextField.setEditable(true);
				frontImageBrowseButton.setEnabled(true);
			}
		}
	}

	private JPanel createFrontSidePrintTypePanel(JobSettingOptions jobSettingOptions) {
		frontOverlayCheckBox = new JCheckBox("Full Overlay");

		frontSubmitButton.addActionListener(frontSubmitButtonListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addFrontSideImage();
			}
		});

		frontPrintTypeComboBox.removeAllItems();

		if (jobSettingOptions.isColorAllowed()) {
			frontPrintTypeComboBox.addItem(PrintType.Color.name());
		}

		if (jobSettingOptions.isMonoAllowed()) {
			frontPrintTypeComboBox.addItem(PrintType.MonoK.name());
		}

		if (jobSettingOptions.isOverlayAllowed()) {
			frontPrintTypeComboBox.addItem(PrintType.Overlay.name());
		}

		frontPrintTypeComboBox.addActionListener(frontPrintTypeComboBoxListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectFrontSidePrintType();
			}
		});

		frontOverlayCheckBox.addChangeListener(frontOverlayCheckBoxListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (frontOverlayCheckBox.isSelected()) {
					frontFileNameTextField.setText("");
					frontFileNameTextField.setEditable(false);
					frontImageBrowseButton.setEnabled(false);
				} else {
					frontFileNameTextField.setEditable(true);
					frontImageBrowseButton.setEnabled(true);
				}
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		panel.add(new JLabel("Type"));
		panel.add(frontPrintTypeComboBox);
		panel.add(frontOverlayCheckBox);
		panel.add(frontSubmitButton);
		return panel;
	}

	private void setFrontSideEnabled(boolean enabled) {
		frontFileNameTextField.setEditable(enabled);
		frontImageBrowseButton.setEnabled(enabled);
		frontPrintTypeComboBox.setEnabled(enabled);
		frontSubmitButton.setEnabled(enabled);

		boolean overlaySelected = frontPrintTypeComboBox != null && PrintType.Overlay.name().equalsIgnoreCase((String) frontPrintTypeComboBox.getSelectedItem());
		frontOverlayCheckBox.setVisible(enabled && overlaySelected);
	}

	private JPanel createFrontSidePanel(JobSettingOptions jobSettingOptions) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(new TitledBorder("Front"));
		panel.add(createPrintFrontSideCheckboxPanel());
		panel.add(createFrontSideImageSelectionPanel());
		panel.add(createFrontSidePrintTypePanel(jobSettingOptions));

		setFrontSideEnabled(false);

		return panel;
	}

	private JPanel createPrintBackSideCheckboxPanel() {
		backCheckBox.addChangeListener(backCheckBoxListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setBackSideEnabled(backCheckBox.isSelected());
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		panel.add(backCheckBox);
		panel.add(new JLabel("Print Back Side"));
		return panel;
	}

	private JPanel createBackSideImageSelectionPanel() {
		backFileNameTextField.setPreferredSize(new Dimension(350, 25));

		backImageBrowseButton = new JButton("Browse");
		backImageBrowseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (FileHelper.getBmpFileChooser().showOpenDialog(demoDialog) == JFileChooser.APPROVE_OPTION) {
					backFileNameTextField.setText(FileHelper.getBmpFileChooser().getSelectedFile().getAbsolutePath());
				}
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		panel.add(new JLabel("Image File"));
		panel.add(backFileNameTextField);
		panel.add(backImageBrowseButton);
		return panel;
	}

	private JPanel createBackSidePrintTypePanel(JobSettingOptions jobSettingOptions) {
		backOverlayCheckBox = new JCheckBox("Full Overlay");

		backSubmitButton.addActionListener(backSubmitButtonListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String type = backPrintTypeComboBox.getSelectedItem().toString();
				String imageLocation = backFileNameTextField.getText();
				boolean successfullyAdded = false;

				File file = new File(imageLocation);
				if (file.exists() && !file.isDirectory()) {
					if (!imageLocation.isEmpty()) {
						backImageInfo.put(PrintType.valueOf(type), imageLocation);
						successfullyAdded = true;
					}
				} else if (type.equals(PrintType.Overlay.name()) && backOverlayCheckBox.isSelected()) {
					backImageInfo.put(PrintType.valueOf(type), null);
					successfullyAdded = true;
				}

				if (successfullyAdded) {
					PrinterModel.showInformationDialog("Back Image", "The " + type + " image was added successfully.");
				} else {
					PrinterModel.showInformationDialog("Back Image", "Unable to add the " + type + " image.\nCheck your filepath and try again.");
				}
			}
		});

		backPrintTypeComboBox.removeAllItems();
		if (jobSettingOptions.isColorAllowed()) {
			backPrintTypeComboBox.addItem(PrintType.Color.name());
		}

		if (jobSettingOptions.isMonoAllowed()) {
			backPrintTypeComboBox.addItem(PrintType.MonoK.name());
		}

		if (jobSettingOptions.isOverlayAllowed()) {
			backPrintTypeComboBox.addItem(PrintType.Overlay.name());
		}

		backPrintTypeComboBox.addActionListener(backPrintTypeComboBoxListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = backPrintTypeComboBox.getSelectedItem().toString();
				if (selectedItem != null) {
					if (backPrintTypeComboBox.getSelectedItem().toString().equalsIgnoreCase(PrintType.Overlay.name())) {
						backOverlayCheckBox.setVisible(true);
						if (backOverlayCheckBox.isSelected()) {
							backFileNameTextField.setText("");
							backFileNameTextField.setEditable(false);
							backImageBrowseButton.setEnabled(false);
						}
					} else {
						backOverlayCheckBox.setVisible(false);
						backFileNameTextField.setEditable(true);
						backImageBrowseButton.setEnabled(true);
					}
				}
			}
		});

		backOverlayCheckBox.addChangeListener(backOverlayCheckBoxListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (backOverlayCheckBox.isSelected()) {
					backFileNameTextField.setText("");
					backFileNameTextField.setEditable(false);
					backImageBrowseButton.setEnabled(false);
				} else {
					backFileNameTextField.setEditable(true);
					backImageBrowseButton.setEnabled(true);
				}
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
		panel.add(new JLabel("Type"));
		panel.add(backPrintTypeComboBox);
		panel.add(backOverlayCheckBox);
		panel.add(backSubmitButton);
		return panel;
	}

	private void setBackSideEnabled(boolean enabled) {
		backFileNameTextField.setEditable(enabled);
		backImageBrowseButton.setEnabled(enabled);
		backPrintTypeComboBox.setEnabled(enabled);
		backSubmitButton.setEnabled(enabled);

		boolean overlaySelected = backPrintTypeComboBox != null && PrintType.Overlay.name().equalsIgnoreCase((String) backPrintTypeComboBox.getSelectedItem());
		backOverlayCheckBox.setVisible(enabled && overlaySelected);
	}

	private JPanel createBackSidePanel(JobSettingOptions jobSettingOptions) {
		JPanel panel = new JPanel();

		if (jobSettingOptions.isBackSidePrintShown()) {
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setBorder(new TitledBorder("Back"));
			panel.add(createPrintBackSideCheckboxPanel());
			panel.add(createBackSideImageSelectionPanel());
			panel.add(createBackSidePrintTypePanel(jobSettingOptions));

			setBackSideEnabled(false);
		}

		return panel;
	}

	private JPanel createMagEncodingPanel(boolean showMagEncoding) {
		JPanel magEncodeArea = new JPanel();
		if (showMagEncoding) {
			magEncodeArea.setLayout(new BoxLayout(magEncodeArea, BoxLayout.PAGE_AXIS));
			magEncodeArea.setBorder(new TitledBorder("Mag - Encode"));

			JPanel selectMagArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
			selectMagArea.add(magEncodeCheckBox);
			magEncodeArea.add(selectMagArea);

			JLabel selectPrintMagLabel = new JLabel("Magnetic Encode");
			selectMagArea.add(selectPrintMagLabel);

			selectMagArea.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 120, 10)));
			JLabel selectCoercivityTypeLabel = new JLabel("Coercivity Type");
			selectMagArea.add(selectCoercivityTypeLabel);

			coercivityTypeComboBox.removeAllItems();
			coercivityTypeComboBox.addItem(CoercivityType.High.name());
			coercivityTypeComboBox.addItem(CoercivityType.Low.name());
			coercivityTypeComboBox.setEnabled(false);
			selectMagArea.add(coercivityTypeComboBox);

			JPanel track1FileArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
			JLabel track1DataLabel = new JLabel("Track 1 Data");
			track1FileArea.add(track1DataLabel);

			track1DataTextField.setPreferredSize(new Dimension(350, 25));
			track1FileArea.add(track1DataTextField);
			magEncodeArea.add(track1FileArea);

			JPanel track2FileArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
			JLabel track2DataLabel = new JLabel("Track 2 Data");
			track2FileArea.add(track2DataLabel);

			track2DataTextField.setPreferredSize(new Dimension(350, 25));
			track2FileArea.add(track2DataTextField);
			magEncodeArea.add(track2FileArea);

			JPanel track3FileArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
			JLabel track3DataLabel = new JLabel("Track 3 Data");
			track3FileArea.add(track3DataLabel);

			track3DataTextField.setPreferredSize(new Dimension(350, 25));
			track3FileArea.add(track3DataTextField);
			magEncodeArea.add(track3FileArea);

			track1DataTextField.setEnabled(false);
			track2DataTextField.setEnabled(false);
			track3DataTextField.setEnabled(false);

			magEncodeCheckBox.addChangeListener(magEncodeCheckBoxListener = new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					boolean shouldEnable = magEncodeCheckBox.isSelected();
					coercivityTypeComboBox.setEnabled(shouldEnable);
					track1DataTextField.setEnabled(shouldEnable);
					track2DataTextField.setEnabled(shouldEnable);
					track3DataTextField.setEnabled(shouldEnable);
				}
			});
		}

		return magEncodeArea;
	}
}
