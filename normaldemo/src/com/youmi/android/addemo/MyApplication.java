package com.youmi.android.addemo;

import android.app.Application;

import net.youmi.android.AdManager;

/**
 * Created by blitzfeng on 2017/7/12.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AdManager.getInstance(this).init("85aa56a59eac8b3d", "a14006f66f58d5d7", true);
    }
}
