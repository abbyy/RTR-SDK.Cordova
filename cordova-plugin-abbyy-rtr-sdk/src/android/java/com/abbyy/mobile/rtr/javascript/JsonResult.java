// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Size;
import android.util.SizeF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.abbyy.mobile.rtr.javascript.JSConstants.HEIGHT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.WIDTH;

public class JsonResult {

	private JsonResult()
	{
		// Utility
	}

	public static JSONObject getRect( Rect rect ) throws JSONException
	{
		JSONObject rectObject = new JSONObject();
		rectObject.put( "top", rect.top );
		rectObject.put( "bottom", rect.bottom );
		rectObject.put( "left", rect.left );
		rectObject.put( "right", rect.right );
		return rectObject;
	}

	public static JSONArray getPointArray( Point[] points ) throws JSONException
	{
		JSONArray pointArray = new JSONArray();
		for( Point point : points ) {
			pointArray.put( getPoint( point ) );
		}
		return pointArray;
	}

	private static JSONObject getPoint( Point point ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( "x", point.x );
		json.put( "y", point.y );
		return json;
	}

	public static JSONObject getSize( Size size ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( WIDTH, size.getWidth() );
		json.put( HEIGHT, size.getHeight() );
		return json;
	}

	public static JSONObject getSizeFloat( SizeF size ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( WIDTH, size.getWidth() );
		json.put( HEIGHT, size.getHeight() );
		return json;
	}

	// Add only true values to decrease json size
	public static void addFlagIfTrue( JSONObject writableMap, String key, int value, int flag ) throws JSONException
	{
		if( ( value & flag ) == flag ) {
			writableMap.put( key, true );
		}
	}

}
