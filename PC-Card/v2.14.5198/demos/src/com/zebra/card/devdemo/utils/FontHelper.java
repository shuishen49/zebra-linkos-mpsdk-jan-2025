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

package com.zebra.card.devdemo.utils;

import java.awt.Font;

import javax.swing.JLabel;

public class FontHelper {

	public static JLabel setFontSize(JLabel label, int fontSize) {
		label.setFont(new Font(label.getFont().getName(), label.getFont().getStyle(), fontSize));
		return label;
	}

	public static JLabel setFontStyle(JLabel label, int fontStyle) {
		label.setFont(new Font(label.getFont().getName(), fontStyle, label.getFont().getSize()));
		return label;
	}
}
