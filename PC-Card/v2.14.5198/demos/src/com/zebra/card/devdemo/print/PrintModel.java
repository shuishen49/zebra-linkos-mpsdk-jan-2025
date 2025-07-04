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

package com.zebra.card.devdemo.print;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import com.zebra.card.devdemo.JobInfo;
import com.zebra.card.devdemo.PollJobStatusWorker;
import com.zebra.card.devdemo.PrinterModel;
import com.zebra.card.devdemo.StatusUpdateInfo;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.common.card.containers.GraphicsInfo;
import com.zebra.sdk.common.card.enumerations.CardSide;
import com.zebra.sdk.common.card.enumerations.CardSource;
import com.zebra.sdk.common.card.enumerations.GraphicType;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.exceptions.ZebraCardException;
import com.zebra.sdk.common.card.graphics.ZebraCardGraphics;
import com.zebra.sdk.common.card.graphics.ZebraCardImageI;
import com.zebra.sdk.common.card.graphics.enumerations.RotationType;
import com.zebra.sdk.settings.SettingsException;

public class PrintModel extends PrinterModel {

	public void print(PrintJobOptions printJobOptions, final JTextArea jobStatusArea) throws IOException, SettingsException, ZebraCardException, ConnectionException {
		ZebraCardGraphics graphics = null;
		jobStatusArea.setText("");

		try {
			if (getConnection() == null) {
				throw new ConnectionException("No printer selected");
			}

			getConnection().open();

			graphics = new ZebraCardGraphics(getZebraCardPrinter()); // Initialize graphics for ZXP Series printers

			List<GraphicsInfo> graphicsData = new ArrayList<GraphicsInfo>();

			// Front side color
			if (printJobOptions.getFrontImageInfo().containsKey(PrintType.Color)) {
				String fileName = printJobOptions.getFrontImageInfo().get(PrintType.Color);
				GraphicsInfo graphicsInfo = buildGraphicsInfo(graphics, fileName, CardSide.Front, PrintType.Color);
				if (graphicsInfo != null) {
					graphicsData.add(graphicsInfo);
				}
			}

			// Front side mono
			if (printJobOptions.getFrontImageInfo().containsKey(PrintType.MonoK)) {
				String fileName = printJobOptions.getFrontImageInfo().get(PrintType.MonoK);
				GraphicsInfo graphicsInfo = buildGraphicsInfo(graphics, fileName, CardSide.Front, PrintType.MonoK);
				if (graphicsInfo != null) {
					graphicsData.add(graphicsInfo);
				}
			}

			// Front side overlay
			if (printJobOptions.getFrontImageInfo().containsKey(PrintType.Overlay)) {
				String fileName = printJobOptions.getFrontImageInfo().get(PrintType.Overlay);
				if (fileName == null || fileName.isEmpty()) {
					graphicsData.add(addImage(null, CardSide.Front, PrintType.Overlay));
				} else {
					GraphicsInfo graphicsInfo = buildGraphicsInfo(graphics, fileName, CardSide.Front, PrintType.Overlay);
					if (graphicsInfo != null) {
						graphicsData.add(graphicsInfo);
					}
				}
			}

			// Back side mono
			if (printJobOptions.getBackImageInfo().containsKey(PrintType.MonoK)) {
				String fileName = printJobOptions.getBackImageInfo().get(PrintType.MonoK);
				GraphicsInfo graphicsInfo = buildGraphicsInfo(graphics, fileName, CardSide.Back, PrintType.MonoK);
				if (graphicsInfo != null) {
					graphicsData.add(graphicsInfo);
				}
			}

			int jobId = getZebraCardPrinter().print(printJobOptions.getQuantity(), graphicsData);

			new PollJobStatusWorker(getZebraCardPrinter(), new JobInfo(jobId, CardSource.Feeder)) {
				@Override
				protected void process(final List<StatusUpdateInfo> updateList) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							StatusUpdateInfo update = updateList.get(updateList.size() - 1);
							jobStatusArea.append(update.getMessage());
						}
					});
				};
			}.execute();
		} catch (ConnectionException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} finally {
			if (graphics != null) {
				graphics.close();
			}

			cleanUpQuietly();
		}
	}

	private GraphicsInfo buildGraphicsInfo(ZebraCardGraphics graphics, String fileName, CardSide side, PrintType printType) throws IllegalArgumentException, IOException {
		GraphicsInfo graphicsInfo = null;
		try {
			byte[] imageData = FileUtils.readFileToByteArray(new File(fileName));
			if (imageData != null) {
				graphics.initialize(0, 0, OrientationType.Landscape, printType, Color.WHITE);
				graphics.drawImage(imageData, 0, 0, 0, 0, RotationType.RotateNoneFlipNone);
				graphicsInfo = addImage(graphics.createImage(), side, printType);
			}
		} finally {
			graphics.clear();
		}
		return graphicsInfo;
	}

	protected GraphicsInfo addImage(ZebraCardImageI zebraCardImage, CardSide side, PrintType printType) throws IOException {
		GraphicsInfo graphicsInfo = new GraphicsInfo();
		if (zebraCardImage != null) {
			graphicsInfo.fillColor = -1;
			graphicsInfo.graphicData = zebraCardImage;
			graphicsInfo.graphicType = GraphicType.BMP;
		} else {
			graphicsInfo.fillColor = 1;
			graphicsInfo.graphicType = GraphicType.NA;
		}

		graphicsInfo.side = side;
		graphicsInfo.printType = printType;
		return graphicsInfo;
	}
}
