// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.data;

import com.abbyy.mobile.rtr.Language;

import java.util.Arrays;
import java.util.Objects;

public class Settings {

	public String profile = "BusinessCards";
	public Language[] languages = new Language[] { Language.English };
	public boolean isTextOrientationDetectionEnabled = true;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		Settings settings = (Settings) o;
		return isTextOrientationDetectionEnabled == settings.isTextOrientationDetectionEnabled &&
			profile.equals( settings.profile ) &&
			Arrays.equals( languages, settings.languages );
	}

	@Override
	public int hashCode()
	{
		int result = Objects.hash( profile, isTextOrientationDetectionEnabled );
		result = 31 * result + Arrays.hashCode( languages );
		return result;
	}
}
