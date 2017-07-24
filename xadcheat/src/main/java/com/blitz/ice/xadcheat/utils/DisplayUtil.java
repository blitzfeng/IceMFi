package com.blitz.ice.xadcheat.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by blitzfeng on 2017/7/14.
 */

public class DisplayUtil {

    public static Map<String, Integer> getScreenWH(Context context){
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        Map<String,Integer> map = new HashMap<>();
        map.put("width",metrics.widthPixels);
        map.put("height",metrics.heightPixels);
        return map;
    }
}
