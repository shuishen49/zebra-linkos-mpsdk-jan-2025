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

package com.zebra.developerdemocard.graphicconversion;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.StringRes;

import com.zebra.developerdemocard.R;
import com.zebra.developerdemocard.util.BitmapHelper;
import com.zebra.developerdemocard.util.PrinterModelInfo;
import com.zebra.sdk.common.card.enumerations.OrientationType;
import com.zebra.sdk.common.card.enumerations.PrintType;
import com.zebra.sdk.common.card.graphics.ZebraCardGraphics;
import com.zebra.sdk.common.card.graphics.ZebraGraphics;
import com.zebra.sdk.common.card.graphics.enumerations.MonochromeConversion;
import com.zebra.sdk.common.card.graphics.enumerations.RotationType;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ProcessImageTask extends AsyncTask<Void, Void, Void> {

    private static final String DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_STRING = "ZebraDeveloperDemoCard";

    public static final File DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_FILE = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_STRING);

    private WeakReference<Context> weakContext;
    private GraphicConversionOptions graphicConversionOptions;
    private OnProcessImageListener onProcessImageListener;
    private Exception exception;

    public interface OnProcessImageListener {
        void onProcessImageStarted();
        void onProcessImageUpdate(String message);
        void onProcessImageFinished(Exception exception);
    }

    ProcessImageTask(Context context, GraphicConversionOptions graphicConversionOptions) {
        weakContext = new WeakReference<>(context);
        this.graphicConversionOptions = graphicConversionOptions;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (onProcessImageListener != null) {
            onProcessImageListener.onProcessImageStarted();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ZebraGraphics graphics = null;
        FileOutputStream fileOutputStream = null;
        InputStream inputStream = null;

        PrinterModelInfo printerModelInfo = graphicConversionOptions.getPrinterModelInfo();

        try {
            graphics = new ZebraCardGraphics(null);
            graphics.setPrinterModel(printerModelInfo.getPrinterModel());

            ContentResolver contentResolver = weakContext.get().getContentResolver();
            inputStream = contentResolver.openInputStream(graphicConversionOptions.getSourceGraphicUri());
            if (inputStream != null) {
                byte[] imageData = IOUtils.toByteArray(inputStream);
                BitmapHelper.Dimensions dimensions = BitmapHelper.getBitmapDimensions(imageData); // Get dimensions of unmodified input image

                int width; // Width of final output image
                int height; // Height of final output image

                switch (graphicConversionOptions.getDimensionOption()) {
                    case CROP:
                        int croppedWidth = constrainDimension(graphicConversionOptions.getWidth(), printerModelInfo.getMaxWidth(), R.string.width_greater_than_max_message, R.string.min_width_message);
                        int croppedHeight = constrainDimension(graphicConversionOptions.getHeight(), printerModelInfo.getMaxHeight(), R.string.height_greater_than_max_message, R.string.min_height_message);
                        imageData = cropImage(graphics, imageData, croppedWidth, croppedHeight);

                        width = croppedWidth;
                        height = croppedHeight;
                        break;
                    case RESIZE:
                        width = constrainDimension(graphicConversionOptions.getWidth(), printerModelInfo.getMaxWidth(), R.string.width_greater_than_max_message, R.string.min_width_message);
                        height = constrainDimension(graphicConversionOptions.getHeight(), printerModelInfo.getMaxHeight(), R.string.height_greater_than_max_message, R.string.min_height_message);

                        if (onProcessImageListener != null) {
                            onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.resizing_image_message, width, height));
                        }
                        break;
                    case ORIGINAL:
                    default:
                        width = constrainDimension(dimensions.getWidth(), printerModelInfo.getMaxWidth(), R.string.width_greater_than_max_message, R.string.min_width_message);
                        height = constrainDimension(dimensions.getHeight(), printerModelInfo.getMaxHeight(), R.string.height_greater_than_max_message, R.string.min_height_message);

                        if (onProcessImageListener != null) {
                            onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.keeping_current_image_dimensions_message));
                        }
                        break;
                }

                MonochromeConversion monochromeConversionType = graphicConversionOptions.getGraphicsFormat().getMonochromeConversion();
                PrintType printType = graphicConversionOptions.getGraphicsFormat().getPrintType();
                OrientationType orientationType = OrientationType.Landscape;

                if (onProcessImageListener != null) {
                    onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.setting_orientation_message, orientationType));
                }

                graphics.initialize(weakContext.get().getApplicationContext(), width, height, orientationType, printType, Color.WHITE);
                graphics.drawImage(imageData,0,0, width, height, RotationType.RotateNoneFlipNone);
                applyMonochromeConversion(graphics, printType, monochromeConversionType, onProcessImageListener);

                if (onProcessImageListener != null) {
                    onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.writing_graphic_file_to_directory, getStorageDirectory().getAbsolutePath()));
                }

                File output = new File(getStorageDirectory(), graphicConversionOptions.getConvertedGraphicFilename());
                if (output.exists()){
                    boolean isDeleteSuccessful = output.delete();
                    if (!isDeleteSuccessful) {
                        throw new FileExistsException(weakContext.get().getString(R.string.unable_to_overwrite_existing_file));
                    }
                }

                fileOutputStream = new FileOutputStream(output, false);
                fileOutputStream.write(graphics.createImage().getImageData());

                if (onProcessImageListener != null) {
                    onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.finished_converting_graphic));
                }
            } else {
                throw new NullPointerException(weakContext.get().getString(R.string.unable_to_create_source_graphic_stream_message));
            }
        } catch (Exception e) {
            exception = e;
        } finally {
            if (graphics != null) {
                graphics.close();
            }

            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (onProcessImageListener != null) {
            onProcessImageListener.onProcessImageFinished(exception);
        }
    }

    private File getStorageDirectory() throws FileNotFoundException {
        if (!DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_FILE.exists() && !DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_FILE.mkdirs()) {
            throw new FileNotFoundException(weakContext.get().getString(R.string.unable_to_create_application_storage_directory));
        }
        return DEFAULT_DIRECTORY_CONVERTED_GRAPHIC_FILE;
    }

    private int constrainDimension(int value, int maxValue, @StringRes int maxExceededMessage, @StringRes int minExceededMessage) {
        if (value > maxValue) {
            if (onProcessImageListener != null) {
                onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(maxExceededMessage, value, maxValue));
            }

            value = maxValue;
        } else if (value < 1) {
            if (onProcessImageListener != null) {
                onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(minExceededMessage));
            }

            value = 1;
        }
        return value;
    }

    private byte[] cropImage(ZebraGraphics graphics, byte[] imageData, int croppedWidth, int croppedHeight) throws IOException {
        int xOffset = graphicConversionOptions.getXOffset() < 0 ? 0 : graphicConversionOptions.getXOffset();
        int yOffset = graphicConversionOptions.getYOffset() < 0 ? 0 : graphicConversionOptions.getYOffset();

        if (onProcessImageListener != null) {
            onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.cropping_image_message, xOffset, yOffset, croppedWidth, croppedHeight));
        }

        byte[] croppedImage = graphics.cropImage(imageData, xOffset, yOffset, croppedWidth, croppedHeight);

        if (onProcessImageListener != null) {
            onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.finished_cropping_image));
        }

        return croppedImage;
    }

    private void applyMonochromeConversion(ZebraGraphics graphics, PrintType printType, MonochromeConversion monochromeConversionType, OnProcessImageListener onProcessImageListener) {
        if (onProcessImageListener != null) {
            onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.converting_graphic));
        }

        if (printType != PrintType.MonoK && printType != PrintType.GrayDye) {
            switch (monochromeConversionType) {
                case Diffusion:
                    if (onProcessImageListener != null) {
                        onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.ignoring_diffusion_option));
                    }
                    break;
                case HalfTone_6x6:
                case HalfTone_8x8:
                    if (onProcessImageListener != null) {
                        onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.ignoring_halftone_option));
                    }
                    break;
            }
        } else {
            switch (monochromeConversionType) {
                case Diffusion:
                    graphics.monochromeConversionType(MonochromeConversion.Diffusion);
                    if (onProcessImageListener != null) {
                        onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.applying_diffusion_algorithm));
                    }
                    break;
                case HalfTone_6x6:
                    graphics.monochromeConversionType(MonochromeConversion.HalfTone_6x6);
                    if (onProcessImageListener != null) {
                        onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.applying_6x6_halftone_algorithm));
                    }
                    break;
                case HalfTone_8x8:
                    graphics.monochromeConversionType(MonochromeConversion.HalfTone_8x8);
                    if (onProcessImageListener != null) {
                        onProcessImageListener.onProcessImageUpdate(weakContext.get().getString(R.string.applying_8x8_halftone_algorithm));
                    }
                    break;
            }
        }
    }

    void setOnProcessImageListener(OnProcessImageListener onProcessImageListener) {
        this.onProcessImageListener = onProcessImageListener;
    }
}
