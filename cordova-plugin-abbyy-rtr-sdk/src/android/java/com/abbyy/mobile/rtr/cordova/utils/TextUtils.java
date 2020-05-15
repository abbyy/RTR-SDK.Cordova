package com.abbyy.mobile.rtr.cordova.utils;

import android.graphics.Point;
import android.support.annotation.NonNull;

import com.abbyy.mobile.rtr.IRecognitionCoreAPI;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.text.TextRecognitionSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	private static JSONArray toJsonTextLines( IRecognitionCoreAPI.TextLine[] lines ) throws JSONException
	{
		JSONArray lineList = new JSONArray();
		for( IRecognitionCoreAPI.TextLine line : lines ) {
			JSONObject lineInfo = new JSONObject();
			lineInfo.put( "text", line.Text );
			String rectInfo = line.Rect.left + " " + line.Rect.bottom + " " +
				line.Rect.width() + " " + line.Rect.height();
			lineInfo.put( "rect", rectInfo );
			String quadrangle = convertQuadrangleToString( line.Quadrangle );
			lineInfo.put( "quadrangle", quadrangle );
			lineList.put( lineInfo );
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

	public static TextRecognitionSettings parseTextRecognitionSettings( JSONObject arg ) throws JSONException
	{
		TextRecognitionSettings textRecognitionSettings = new TextRecognitionSettings();
		List<Language> languages = parseLanguagesInternal( arg, RtrPlugin.RTR_RECOGNITION_LANGUAGES_KEY, false );
		textRecognitionSettings.recognitionLanguages = languages.toArray( new Language[0] );
		if( arg.has( RtrPlugin.RTR_IS_TEXT_ORIENTATION_DETECTION_ENABLED_KEY ) ) {
			textRecognitionSettings.isTextOrientationDetectionEnabled = arg.getBoolean( RtrPlugin.RTR_IS_TEXT_ORIENTATION_DETECTION_ENABLED_KEY );
		}

		return textRecognitionSettings;
	}

	public static JSONObject toTextRecognitionResult( IRecognitionCoreAPI.TextBlock[] blocks, Integer orientation ) throws JSONException
	{
		JSONObject json = new JSONObject();
		JSONArray jsonBlocks = new JSONArray();
		for( IRecognitionCoreAPI.TextBlock block : blocks ) {
			jsonBlocks.put( toJsonTextBlock( block ) );
		}

		json.put( "resultInfo", jsonBlocks );
		if( orientation != null ) {
			json.put( "orientation", orientation.intValue() );
		}
		return json;
	}

	private static JSONObject toJsonTextBlock( IRecognitionCoreAPI.TextBlock block ) throws JSONException
	{
		JSONObject jsonBlock = new JSONObject();
		JSONArray jsonTextLines = TextUtils.toJsonTextLines( block.TextLines );
		jsonBlock.put( "textLines", jsonTextLines );
		return jsonBlock;
	}
}
