package com.fision.tel2_0;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

public class aboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
//        ActionBar bar = getActionBar();
//        bar.setDisplayHomeAsUpEnabled(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            bar.setHomeAsUpIndicator(R.drawable.ic_launcher);
//        }
    }
}
