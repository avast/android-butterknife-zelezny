package com.avast.butterknifezelezny.test;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class InheritanceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new Fragment())
                    .commit();
        }
    }

    @Override
    protected int getLayoutId() {
        // Try to generate Activity injections by clicking to R.layout.activity_main
        return R.layout.activity_main;
    }
}
