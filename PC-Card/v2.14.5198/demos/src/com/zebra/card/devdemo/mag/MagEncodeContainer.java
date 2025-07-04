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

package com.zebra.card.devdemo.mag;

public class MagEncodeContainer {
	private String cardSource;
	private String cardDestination;
	private String coercivityType;
	private Boolean encodingVerified;
	private String track1Data;
	private String track2Data;
	private String track3Data;
	private Integer quantity;

	public String getCardSource() {
		return cardSource;
	}

	public String getCardDestination() {
		return cardDestination;
	}

	public String getCoercivityType() {
		return coercivityType;
	}

	public Boolean isEncodingVerified() {
		return encodingVerified;
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

	public Integer getQuantity() {
		return quantity;
	}

	public void setCardSource(String cardSource) {
		this.cardSource = cardSource;
	}

	public void setCardDestination(String cardDestination) {
		this.cardDestination = cardDestination;
	}

	public void setCoercivityType(String coercivityType) {
		this.coercivityType = coercivityType;
	}

	public void setEncodingVerified(Boolean encodingVerified) {
		this.encodingVerified = encodingVerified;
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

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
