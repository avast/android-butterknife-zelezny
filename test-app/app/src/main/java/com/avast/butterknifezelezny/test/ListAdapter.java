package com.avast.butterknifezelezny.test;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class ListAdapter extends ArrayAdapter<String> {

    public ListAdapter(Context context, List<String> objects) {
        // Try to generate ViewHolder by clicking to R.layout.list_item
        super(context, R.layout.list_item, objects);
    }
}
