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

package com.zebra.developerdemocard.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.zebra.developerdemocard.R;

import static android.app.Activity.RESULT_CANCELED;

public class StorageHelper {
    public static final String KEY_STORAGE_PERMISSIONS_RESULT = "KEY_STORAGE_PERMISSIONS_RESULT";

    public static final int PERMISSION_DENIED = 0;
    public static final int PERMISSION_NEVER_ASK_AGAIN_SET = 1;

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static void requestStoragePermissionIfNotGranted(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, requestCode);
        }
    }

    public static void handleRequestStoragePermissionsResult(Activity activity, @NonNull String permission, int grantResult) {
        if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                UIHelper.showSnackbar(activity, activity.getString(R.string.storage_permissions_granted));
            } else if (Build.VERSION.SDK_INT >= 23 && !activity.shouldShowRequestPermissionRationale(permission)) {
                Intent intent = new Intent();
                intent.putExtra(StorageHelper.KEY_STORAGE_PERMISSIONS_RESULT, StorageHelper.PERMISSION_NEVER_ASK_AGAIN_SET);
                activity.setResult(RESULT_CANCELED, intent);
                activity.finish();
            } else {
                Intent intent = new Intent();
                intent.putExtra(StorageHelper.KEY_STORAGE_PERMISSIONS_RESULT, StorageHelper.PERMISSION_DENIED);
                activity.setResult(RESULT_CANCELED, intent);
                activity.finish();
            }
        }
    }
}
