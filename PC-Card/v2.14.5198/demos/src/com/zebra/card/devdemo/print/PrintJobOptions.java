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

import java.util.HashMap;
import java.util.Map;

import com.zebra.sdk.common.card.enumerations.PrintType;

public class PrintJobOptions {

	private boolean frontSelected = false;
	private boolean backSelected = false;
	private Map<PrintType, String> frontImageInfo = new HashMap<PrintType, String>();
	private Map<PrintType, String> backImageInfo = new HashMap<PrintType, String>();
	private String track1Data = "";
	private String track2Data = "";
	private String track3Data = "";
	private int quantity = 1;

	public boolean hasTrackData() {
		return hasTrack1Data() || hasTrack2Data() || hasTrack3Data();
	}

	private boolean hasTrack1Data() {
		return track1Data != null && !track1Data.isEmpty();
	}

	private boolean hasTrack2Data() {
		return track2Data != null && !track2Data.isEmpty();
	}

	private boolean hasTrack3Data() {
		return track3Data != null && !track3Data.isEmpty();
	}

	public void setFrontSelected(boolean frontSelected) {
		this.frontSelected = frontSelected;
	}

	public void setBackSelected(boolean backSelected) {
		this.backSelected = backSelected;
	}

	public void setFrontImageInfo(Map<PrintType, String> frontImageInfo) {
		this.frontImageInfo = frontImageInfo;
	}

	public void setBackImageInfo(Map<PrintType, String> backImageInfo) {
		this.backImageInfo = backImageInfo;
	}

	public void setTrack1Data(String track1Data) {
		this.track1Data = track1Data;
	}

	public void setTrack2Data(String track2Data) {
		this.track2Data = track2Data;
	}

	public void setTrack3Data(String track3Data) {
		this.track3Data = track3Data;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public boolean isFrontSelected() {
		return frontSelected;
	}

	public boolean isBackSelected() {
		return backSelected;
	}

	public Map<PrintType, String> getFrontImageInfo() {
		return frontImageInfo;
	}

	public Map<PrintType, String> getBackImageInfo() {
		return backImageInfo;
	}

	public String getTrack1Data() {
		return track1Data;
	}

	public String getTrack2Data() {
		return track2Data;
	}

	public String getTrack3Data() {
		return track3Data;
	}

	public int getQuantity() {
		return quantity;
	}
}
