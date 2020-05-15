// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.data;

import com.abbyy.mobile.rtr.Language;

public class DataCaptureSettings {
	public String imageUri;
	public Language[] recognitionLanguages;
	public boolean isTextOrientationDetectionEnabled = true;
	public String profile;

	public DataCaptureSettings() { }
}
