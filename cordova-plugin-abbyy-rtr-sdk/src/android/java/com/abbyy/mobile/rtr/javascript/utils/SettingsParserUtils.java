// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.utils;

import android.graphics.Point;
import android.graphics.Rect;

import com.abbyy.mobile.rtr.Language;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.abbyy.mobile.rtr.javascript.JSConstants.AREA_OF_INTEREST_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DEFAULT_LICENSE_FILENAME;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DOCUMENT_BOUNDARY_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.IMAGE_URI_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.LICENSE_FILE_NAME_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.RECOGNITION_LANGUAGES_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.TEXT_ORIENTATION_FLAG_KEY;

/**
 * Parser class for common data types.
 */
public class SettingsParserUtils {

	private SettingsParserUtils()
	{
		// Utility class
	}

	/**
	 * Throws exception if settings is null.
	 */
	public static void checkForNullSettings( @Nullable JSONObject settings )
	{
		if( settings == null ) {
			throw new IllegalArgumentException( "Settings parameter is required" );
		}
	}

	/**
	 * Parse license filename.
	 * Returns default if settings is null or if settings doesn't contain license parameter.
	 */
	public static String parseLicenseFilename( @Nullable JSONObject settings ) throws JSONException
	{
		String licenseFileName = null;
		if( settings != null && settings.has( LICENSE_FILE_NAME_KEY ) ) {
			licenseFileName = settings.getString( LICENSE_FILE_NAME_KEY );
		}

		if( licenseFileName == null ) {
			licenseFileName = DEFAULT_LICENSE_FILENAME;
		}

		return licenseFileName;
	}

	/**
	 * Parse recognition languages.
	 * Returns defaultValue if settings doesn't contain or languages is null.
	 * Throws exception if language list is empty.
	 */
	public static Language[] parseLanguages( @NonNull JSONObject settings, Language[] defaultValue ) throws JSONException
	{
		if( !settings.has( RECOGNITION_LANGUAGES_KEY ) ) {
			return defaultValue;
		}

		JSONArray languages = settings.getJSONArray( RECOGNITION_LANGUAGES_KEY );
		if( languages == null ) {
			throw new IllegalArgumentException( "Language array is null" );
		}

		if( languages.length() == 0 ) {
			throw new IllegalArgumentException( "Language array is empty" );
		}

		Language[] languageArray = new Language[languages.length()];
		for( int languageIndex = 0; languageIndex < languages.length(); ++languageIndex ) {
			String language = languages.getString( languageIndex );
			languageArray[languageIndex] = Language.valueOf( language );
		}

		return languageArray;
	}

	public static Rect parseAreaOfInterest( @NonNull JSONObject settings, Rect defaultValue ) throws JSONException
	{
		if( settings.has( AREA_OF_INTEREST_KEY ) ) {
			return SettingsParserUtils.parseRect( settings.getJSONObject( AREA_OF_INTEREST_KEY ) );
		} else {
			return defaultValue;
		}
	}

	@NonNull
	public static Point[] parseDocumentBoundary( @NonNull JSONObject settings ) throws Exception
	{
		if( settings.has( DOCUMENT_BOUNDARY_KEY ) ) {
			return SettingsParserUtils.parsePointArray( settings.getJSONArray( DOCUMENT_BOUNDARY_KEY ) );
		} else {
			throw new Exception( "Document boundary must be passed" );
		}
	}

	@Nullable
	public static Point[] parseDocumentBoundary( @NonNull JSONObject settings, @Nullable Point[] defaultValue ) throws Exception
	{
		if( settings.has( DOCUMENT_BOUNDARY_KEY ) ) {
			return SettingsParserUtils.parsePointArray( settings.getJSONArray( DOCUMENT_BOUNDARY_KEY ) );
		} else {
			return defaultValue;
		}
	}

	private static Point[] parsePointArray( @NonNull JSONArray json ) throws JSONException
	{
		Point[] points = new Point[json.length()];
		for( int i = 0; i < json.length(); i++ ) {
			points[i] = parsePoint( json.getJSONObject( i ) );
		}
		return points;
	}

	private static Point parsePoint( JSONObject jsonObject ) throws JSONException
	{
		int x = jsonObject.getInt( "x" );
		int y = jsonObject.getInt( "y" );
		return new Point( x, y );
	}

	private static Rect parseRect( @Nullable JSONObject json ) throws JSONException
	{
		if( json == null ) {
			return null;
		}

		Rect rect = new Rect();
		rect.top = json.getInt( "top" );
		rect.bottom = json.getInt( "bottom" );
		rect.left = json.getInt( "left" );
		rect.right = json.getInt( "right" );

		return rect;
	}

	public static boolean parseTextOrientationFlag( @NonNull JSONObject settings, boolean defaultValue ) throws JSONException
	{
		if( settings.has( TEXT_ORIENTATION_FLAG_KEY ) ) {
			return settings.getBoolean( TEXT_ORIENTATION_FLAG_KEY );
		} else {
			return defaultValue;
		}
	}

	@NonNull
	public static String parseImageUri( @Nullable JSONObject json ) throws JSONException
	{
		checkForNullSettings( json );

		if( !json.has( IMAGE_URI_KEY ) ) {
			throw new IllegalArgumentException( "Settings doesn't contain imageUri parameter" );
		}

		String imageUri = json.getString( IMAGE_URI_KEY );

		if( imageUri == null ) {
			throw new IllegalArgumentException( "Image is required: null passed" );
		}

		return imageUri;
	}

	public static <T> T parseEnumValue(
		JSONObject jsonObject,
		String key,
		Map<String, T> values,
		T defaultValue
	) throws Exception
	{
		if( !jsonObject.has( key ) ) {
			return defaultValue;
		}

		String value = jsonObject.getString( key );
		if( value == null ) {
			throw new Exception( "Null value for key '" + key + "'" );
		}

		// Value is case insensitive
		value = value.toLowerCase();
		if( values.containsKey( value ) ) {
			return values.get( value );
		} else {
			throw new Exception( "Unknown value '" + value + "' for key '" + key + "'" );
		}
	}

}
