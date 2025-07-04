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

package com.zebra.card.devdemo.graphicconversion;

import com.zebra.card.devdemo.graphicconversion.GraphicConverter.DimensionOption;

public class GraphicsContainer {
	private String format;
	private String outputFilePath;
	private String inputFilePath;
	private PrinterModelInfo printerModelInfo;
	private String width;
	private String height;
	private String xOffset;
	private String yOffset;
	private DimensionOption dimensionOption;

	public String getFormat() {
		return format;
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}

	public PrinterModelInfo getPrinterModelInfo() {
		return printerModelInfo;
	}

	public String getWidth() {
		return width;
	}

	public String getHeight() {
		return height;
	}

	public String getXOffset() {
		return xOffset;
	}

	public String getYOffset() {
		return yOffset;
	}

	public DimensionOption getDimensionOption() {
		return dimensionOption;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}

	public void setPrinterModelInfo(PrinterModelInfo printerModelInfo) {
		this.printerModelInfo = printerModelInfo;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public void setXOffset(String xOffset) {
		this.xOffset = xOffset;
	}

	public void setYOffset(String yOffset) {
		this.yOffset = yOffset;
	}

	public void setDimensionOption(DimensionOption dimensionOption) {
		this.dimensionOption = dimensionOption;
	}
}
