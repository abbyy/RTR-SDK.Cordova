// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova;

import com.abbyy.mobile.rtr.Language;

import java.util.List;

public class DataCaptureScenario {

	public final String name;
	public final String usageDescription;
	public final List<Language> languages;
	public final String regEx;

	DataCaptureScenario( String name, String usageDescription, List<Language> languages, String regEx )
	{
		this.name = name;
		this.usageDescription = usageDescription;
		this.languages = languages;
		this.regEx = regEx;
	}

}
