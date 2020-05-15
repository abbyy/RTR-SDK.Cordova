// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.text;

import android.graphics.Rect;

import com.abbyy.mobile.rtr.Language;

import java.util.Arrays;
import java.util.Objects;

class Settings {

	Language[] languages = new Language[] { Language.English };
	Rect areaOfInterest = null;
	boolean isTextOrientationDetectionEnabled = true;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		Settings settings = (Settings) o;
		return isTextOrientationDetectionEnabled == settings.isTextOrientationDetectionEnabled &&
			Arrays.equals( languages, settings.languages ) &&
			Objects.equals( areaOfInterest, settings.areaOfInterest );
	}

	@Override
	public int hashCode()
	{
		int result = Objects.hash( areaOfInterest, isTextOrientationDetectionEnabled );
		result = 31 * result + Arrays.hashCode( languages );
		return result;
	}

	@Override
	public String toString()
	{
		return "Settings{" +
			"languages=" + Arrays.toString( languages ) +
			", areaOfInterest=" + areaOfInterest +
			", isTextOrientationDetectionEnabled=" + isTextOrientationDetectionEnabled +
			'}';
	}
}
