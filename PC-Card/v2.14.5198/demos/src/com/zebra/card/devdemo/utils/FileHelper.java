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

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileHelper {

	private static JFileChooser templateFileChooser;
	private static JFileChooser imageFileChooser;
	private static JFileChooser bmpFileChooser;

	static {
		templateFileChooser = new JFileChooser();
		templateFileChooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Template File (*.xml)";
			}

			@Override
			public boolean accept(File file) {
				String filename = file.getName().toLowerCase();
				return filename.endsWith(".xml") || file.isDirectory();
			}
		});

		imageFileChooser = new JFileChooser();
		imageFileChooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Image Files (*.bmp, *.jpeg, *.jpg, *.png)";
			}

			@Override
			public boolean accept(File file) {
				String filename = file.getName().toLowerCase();
				return filename.endsWith(".bmp") || filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png") || file.isDirectory();
			}
		});

		bmpFileChooser = new JFileChooser();
		bmpFileChooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "Image Files (*.bmp)";
			}

			@Override
			public boolean accept(File file) {
				String filename = file.getName().toLowerCase();
				return filename.endsWith(".bmp") || file.isDirectory();
			}
		});
	}

	public static JFileChooser getTemplateFileChooser() {
		return templateFileChooser;
	}

	public static JFileChooser getImageFileChooser() {
		return imageFileChooser;
	}

	public static JFileChooser getBmpFileChooser() {
		return bmpFileChooser;
	}

	public static File[] getBmpFilesInDirectory(String directoryPath) {
		return new File(directoryPath).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".bmp");
			}
		});
	}

	public static String removeFileExtension(File file) {
		return file.getName().split("\\.")[0];
	}
}
