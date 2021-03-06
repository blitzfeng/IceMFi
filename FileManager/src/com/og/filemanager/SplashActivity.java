package com.og.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.og.filemanager.db.DBDao;
import com.og.util.NetUtil;
import com.og.util.PermissionHelper;
import com.og.util.Util;

import net.youmi.android.AdManager;
import net.youmi.android.nm.sp.SpotListener;
import net.youmi.android.nm.sp.SpotManager;
import net.youmi.android.nm.vdo.VideoAdManager;

import java.util.concurrent.Executor;

public class SplashActivity extends Activity {

    private PermissionHelper mPermissionHelper;
    DBDao dbDao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /*Util.threadPool.execute(new Runnable() {
            @Override
            public void run() {
                dbDao = new DBDao(SplashActivity.this);
                int lo = dbDao.queryLocation();
                if(lo == 0){
                    Util.list = NetUtil.getIp();
                    if(Util.list.size()>=0){
                        dbDao.insertIPInfo(Util.list);
                    }
                }


            }
        });*/

        // 当系统为6.0以上时，需要申请权限
        mPermissionHelper = new PermissionHelper(this);
        mPermissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener() {
            @Override
            public void onAfterApplyAllPermission() {
    //            AdManager.getInstance(SplashActivity.this).init("1204a74cefdec234", "c04e0d119fa30fdb", true);
            }
        });
        if (Build.VERSION.SDK_INT < 23) {
            // 如果系统版本低于23，直接跑应用的逻辑
    //        AdManager.getInstance(this).init("1204a74cefdec234", "c04e0d119fa30fdb", true);
        } else {
            // 如果权限全部申请了，那就直接跑应用逻辑
            if (mPermissionHelper.isAllRequestedPermissionGranted()) {
     //           AdManager.getInstance(this).init("1204a74cefdec234", "c04e0d119fa30fdb", true);
            } else {
                // 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
                mPermissionHelper.applyPermissions();
            }
        }

        //    SplashViewSettings splashViewSettings = new SplashViewSettings();
       /* SpotManager.getInstance(this).setImageType(SpotManager.IMAGE_TYPE_VERTICAL);
        SpotManager.getInstance(this).setAnimationType(SpotManager.ANIMATION_TYPE_NONE);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
       //         dbDao.close();
              startActivity(new Intent(SplashActivity.this,ShowActivity.class));
                finish();
            }
        },4000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionHelper.onActivityResult(requestCode, resultCode, data);
    }
}
