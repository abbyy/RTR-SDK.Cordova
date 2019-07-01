package com.abbyy.mobile.rtr.cordova.multipage;

import android.util.SparseArray;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.RtrManager;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiCaptureResult {
	public void addPage( PageHolder pageHolder )
	{

	}

	public HashMap<String, Object> getJsonResult( SparseArray<PageHolder> pages )
	{
		HashMap<String, Object> json = new HashMap<>();
		int pageCount = pages.size();
		if ( ImageCaptureSettings.destination == ImageCaptureSettings.Destination.FILE ) {
			ArrayList<String> pagesPaths = new ArrayList<>();
			for( int i = 0; i < pageCount; ++i ) {
				PageHolder pageHolder = pages.valueAt( i );
				pagesPaths.add( pageHolder.getPageFile().getPath() );
			}
			json.put( "pages", pagesPaths );
		}
		return json;
	}
}
