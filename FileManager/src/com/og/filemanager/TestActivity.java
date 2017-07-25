package com.og.filemanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.youmi.android.nm.sp.SpotListener;
import net.youmi.android.nm.sp.SpotManager;

public class TestActivity extends DistributionLibraryFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        SpotManager.getInstance(this).setImageType(SpotManager.IMAGE_TYPE_VERTICAL);
        SpotManager.getInstance(this).setAnimationType(SpotManager.ANIMATION_TYPE_NONE);

        SpotManager.getInstance(this).showSpot(this,
                new SpotListener() {
                    @Override
                    public void onShowSuccess() {
                        System.out.println("onShowSuccess");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                              TestActivity.this.finish();
                            }
                        },4000);
                    }

                    @Override
                    public void onShowFailed(int i) {
                        System.out.println("onShowFailed");
                    }

                    @Override
                    public void onSpotClosed() {
                        System.out.println("onSpotClosed");
                    }

                    @Override
                    public void onSpotClicked(boolean b) {
                        System.out.println("onSpotClosed");
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                if(!TestActivity.this.isDestroyed())
                    TestActivity.this.finish();
            }
        },7000);
    }

    public void start(View v){
        SpotManager.getInstance(this).showSpot(this,
                new SpotListener() {
                    @Override
                    public void onShowSuccess() {
                        System.out.println("onShowSuccess");
                    }

                    @Override
                    public void onShowFailed(int i) {
                        System.out.println("onShowFailed");
                    }

                    @Override
                    public void onSpotClosed() {
                        System.out.println("onSpotClosed");
                    }

                    @Override
                    public void onSpotClicked(boolean b) {
                        System.out.println("onSpotClosed");
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotManager.getInstance(this).onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SpotManager.getInstance(this).onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpotManager.getInstance(this).onDestroy();
    }
}
