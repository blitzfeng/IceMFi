package com.og.filemanager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import net.youmi.android.nm.sp.SpotManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class SAccessService extends AccessibilityService {
    private static final String TAG = "SAccessService";
    private String[] packages = {"com.og.filemanager"};

    public SAccessService() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "config success!");
        AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
        //指定包名
        accessibilityServiceInfo.packageNames = packages;
        //指定事件类型
        accessibilityServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        accessibilityServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        if (Build.VERSION.SDK_INT >= 16)//Just in case this helps
            accessibilityServiceInfo.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        setServiceInfo(accessibilityServiceInfo);


    }


    private void getImageViewByReflect(){
        SpotManager spotManager = SpotManager.getInstance(this);
        try {
            Class tuCls = Class.forName("x.y.a.tu");
            Constructor constructor;
            Method method = tuCls.getMethod("a",int.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
