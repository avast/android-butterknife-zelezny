package com.avast.butterknifezelezny.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class CustomView extends LinearLayout {

    public CustomView(Context context) {
        super(context);
        // Try to generate custom view injections by clicking to R.layout.custom_view
        LayoutInflater.from(context).inflate(R.layout.custom_view, this);
    }
}
