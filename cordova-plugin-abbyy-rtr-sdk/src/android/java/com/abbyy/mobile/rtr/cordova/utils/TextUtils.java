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
	public static ArrayList<HashMap<String, String>> toJsonTextLines( ITextCaptureService.TextLine[] lines )
	{
		ArrayList<HashMap<String, String>> lineList = new ArrayList<>();
		for( ITextCaptureService.TextLine line : lines ) {
			HashMap<String, String> lineInfo = new HashMap<>();
			lineInfo.put( "text", line.Text );
			String quadrangle = convertQuadrangleToString( line.Quadrangle );
			lineInfo.put( "quadrangle", quadrangle );
			lineList.add( lineInfo );
		}
		return lineList;
	}

	@NonNull
	static String convertQuadrangleToString( Point[] quadrangle )
	{
		StringBuilder builder = new StringBuilder();
		for( int i = 0; i < quadrangle.length; i++ ) {
			builder.append( quadrangle[i].x );
			builder.append( ' ' );
			builder.append( quadrangle[i].y );
			if( i != quadrangle.length - 1 ) {
				builder.append( ' ' );
			}
		}
		return builder.toString();
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
