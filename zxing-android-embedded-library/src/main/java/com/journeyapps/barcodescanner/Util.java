package com.journeyapps.barcodescanner;

import android.os.Looper;

/**
 *
 */
public class Util {
    public static void validateMainThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Must be called from the main thread.");
        }
    }

    public static String dataTransfer_Key = "DataTransfer";
    public static String dataNotFound_Key = "4 1 20 1 14 15 20 6 15 21 14 4";
}