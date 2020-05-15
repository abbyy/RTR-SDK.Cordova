// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.text;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseAreaOfInterest;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseLanguages;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseTextOrientationFlag;

class SettingsParser {

	private SettingsParser()
	{
		// Utility class
	}

	public static Settings parse( @NonNull JSONObject json ) throws JSONException
	{
		Settings settings = new Settings();
		settings.languages = parseLanguages( json, settings.languages );
		settings.areaOfInterest = parseAreaOfInterest( json, settings.areaOfInterest );
		settings.isTextOrientationDetectionEnabled =
			parseTextOrientationFlag( json, settings.isTextOrientationDetectionEnabled );
		return settings;
	}

}
