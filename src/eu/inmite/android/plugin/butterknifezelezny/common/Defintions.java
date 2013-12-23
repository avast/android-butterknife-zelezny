package eu.inmite.android.plugin.butterknifezelezny.common;

import java.util.ArrayList;
import java.util.HashMap;

public class Defintions {

	public static final HashMap<String, String> paths = new HashMap<String, String>();
	public static final ArrayList<String> adapters = new ArrayList<String>();

	static {
		// special classes; default package is android.widget.*
		paths.put("WebView", "android.webkit.WebView");

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
