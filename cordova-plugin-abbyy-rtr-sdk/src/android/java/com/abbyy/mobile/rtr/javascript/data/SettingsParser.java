// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.data;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import static com.abbyy.mobile.rtr.javascript.JSConstants.PROFILE_KEY;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseLanguages;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseTextOrientationFlag;

class SettingsParser {

	private SettingsParser()
	{
		// Utility
	}

	public static Settings parse( @NonNull JSONObject jsonSettings ) throws JSONException
	{
		Settings settings = new Settings();
		settings.profile = parseDataProfile( jsonSettings, settings.profile );
		settings.languages = parseLanguages( jsonSettings, settings.languages );
		settings.isTextOrientationDetectionEnabled =
			parseTextOrientationFlag( jsonSettings, settings.isTextOrientationDetectionEnabled );
		return settings;
	}

	private static String parseDataProfile( @NonNull JSONObject settings, String defaultValue ) throws JSONException
	{
		if( settings.has( PROFILE_KEY ) ) {
			return settings.getString( PROFILE_KEY );
		} else {
			return defaultValue;
		}
	}

}
