package com.app.securitysystemalerts;

import android.util.Log;

public class DebugLog {

    private static final boolean debug = false;

    public static void writeLog(String msg) {
        if (debug) {
            Log.d("alert", msg);
        }
    }
}
