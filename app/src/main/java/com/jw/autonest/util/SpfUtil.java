package com.jw.autonest.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SpfUtil {

    private static SpfUtil util;
    private static SharedPreferences sp;

    private SpfUtil(Context context) {
        sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
    }

    public static SpfUtil getInstance(Context context) {
        if (util == null) {
            util = new SpfUtil(context);
        }
        return util;
    }

    public void putData(String key, boolean value) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key,  value);
        editor.apply();
    }

    public boolean getData(String key) {
        return sp.getBoolean(key, false);
    }


}

