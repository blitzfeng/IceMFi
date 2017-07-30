package com.og.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ChooseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
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
