package com.avast.android.butterknifezelezny.common;

import java.util.ArrayList;
import java.util.HashMap;

public class Definitions {

    public static final HashMap<String, String> paths = new HashMap<String, String>();
    public static final ArrayList<String> adapters = new ArrayList<String>();

    static {
        // special classes; default package is android.widget.*
        paths.put("WebView", "android.webkit.WebView");
        paths.put("View", "android.view.View");
        paths.put("ViewStub", "android.view.ViewStub");
        paths.put("SurfaceView", "android.view.SurfaceView");
        paths.put("TextureView", "android.view.TextureView");

        // adapters
        adapters.add("android.widget.ListAdapter");
        adapters.add("android.widget.ArrayAdapter");
        adapters.add("android.widget.BaseAdapter");
        adapters.add("android.widget.HeaderViewListAdapter");
        adapters.add("android.widget.SimpleAdapter");
        adapters.add("android.support.v4.widget.CursorAdapter");
        adapters.add("android.support.v4.widget.SimpleCursorAdapter");
        adapters.add("android.support.v4.widget.ResourceCursorAdapter");
    }
}
