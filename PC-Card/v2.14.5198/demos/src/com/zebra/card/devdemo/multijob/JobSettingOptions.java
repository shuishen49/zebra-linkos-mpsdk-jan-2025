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

public class JobSettingOptions {

	private boolean printOptimizationShown = false;
	private boolean backSidePrintShown = false;
	private boolean magneticEncodingShown = false;
	private boolean laminatorDestinationsShown = false;
	private boolean colorAllowed = false;
	private boolean monoAllowed = false;
	private boolean overlayAllowed = false;
	private String cardSourceRange = "";
	private String cardDestinationRange = "";

	public boolean isPrintOptimizationShown() {
		return printOptimizationShown;
	}

	public boolean isBackSidePrintShown() {
		return backSidePrintShown;
	}

	public boolean isMagneticEncodingShown() {
		return magneticEncodingShown;
	}

	public boolean areLaminatorDestinationsShown() {
		return laminatorDestinationsShown;
	}

	public boolean isColorAllowed() {
		return colorAllowed;
	}

	public boolean isMonoAllowed() {
		return monoAllowed;
	}

	public boolean isOverlayAllowed() {
		return overlayAllowed;
	}

	public String getCardSourceRange() {
		return cardSourceRange;
	}

	public String getCardDestinationRange() {
		return cardDestinationRange;
	}

	public void setPrintOptimizationShown(boolean printOptimizationShown) {
		this.printOptimizationShown = printOptimizationShown;
	}

	public void setBackSidePrintShown(boolean backSidePrintShown) {
		this.backSidePrintShown = backSidePrintShown;
	}

	public void setMagneticEncodingShown(boolean magneticEncodingShown) {
		this.magneticEncodingShown = magneticEncodingShown;
	}

	public void setLaminatorDestinationsShown(boolean laminatorDestinationsShown) {
		this.laminatorDestinationsShown = laminatorDestinationsShown;
	}

	public void setColorAllowed(boolean colorAllowed) {
		this.colorAllowed = colorAllowed;
	}

	public void setMonoAllowed(boolean monoAllowed) {
		this.monoAllowed = monoAllowed;
	}

	public void setOverlayAllowed(boolean overlayAllowed) {
		this.overlayAllowed = overlayAllowed;
	}

	public void setCardSourceRange(String cardSourceRange) {
		this.cardSourceRange = cardSourceRange;
	}

	public void setCardDestinationRange(String cardDestinationRange) {
		this.cardDestinationRange = cardDestinationRange;
	}
}
