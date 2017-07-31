package com.og.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.og.util.NetUtil;

public class ChooseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

      /*  new Thread(){
            @Override
            public void run() {
                NetUtil.getIp();
            }
        }.start();*/
    }


    public void enterClick(View v){

        startActivity(new Intent(this,FileManagerActivity.class));
        finish();
    }

    public void enterShow(View v){
        startActivity(new Intent(this,ShowActivity.class));
        finish();
    }
}
