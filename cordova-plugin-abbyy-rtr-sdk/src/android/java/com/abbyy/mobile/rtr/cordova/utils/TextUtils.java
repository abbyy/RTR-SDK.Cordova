package com.abbyy.mobile.rtr.cordova.utils;

import android.graphics.Point;

import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

public class TextUtils {
	public static ArrayList<HashMap<String, Object>> toJsonTextLines( ITextCaptureService.TextLine[] lines )
	{
		ArrayList<HashMap<String, Object>> lineList = new ArrayList<>();
		for( ITextCaptureService.TextLine line : lines ) {
			HashMap<String, Object> lineInfo = new HashMap<>();
			lineInfo.put( "text", line.Text );
			ArrayList<HashMap<String, String>> quadrangle = getPointArray( line.Quadrangle );
			lineInfo.put( "quadrangle", quadrangle );
			lineList.add( lineInfo );
		}
		return lineList;
	}

	public static ArrayList<HashMap<String, String>> getPointArray( Point[] points )
	{
		ArrayList<HashMap<String, String>> pointArray = new ArrayList<>();
		for( Point point : points ) {
			pointArray.add( getPoint( point ) );
		}
		return pointArray;
	}

	private static HashMap<String, String> getPoint( Point point )
	{
		HashMap<String, String> json = new HashMap<>();
		json.put( "x", Integer.toString( point.x ) );
		json.put( "y", Integer.toString( point.y ) );
		return json;
	}

	private static Language parseLanguageName( String name )
	{
		for( Language language : Language.values() ) {
			if( language.name().equals( name ) ) {
				return language;
			}
		}
		throw new IllegalArgumentException( "Unknown language name" );
	}

	public static List<Language> parseLanguagesInternal( JSONObject arg, String key, boolean isEnglishByDefault ) throws JSONException
	{
		List<Language> languages = new ArrayList<>();
		if( arg.has( key ) ) {
			JSONArray array = arg.getJSONArray( key );
			for( int i = 0; i < array.length(); i++ ) {
				languages.add( parseLanguageName( array.getString( i ) ) );
			}
		} else if( isEnglishByDefault ) {
			languages.add( Language.English );
		}

		return languages;
	}
}
