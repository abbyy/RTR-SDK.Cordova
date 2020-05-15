// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.text;

import android.graphics.Rect;

import com.abbyy.mobile.rtr.Language;

public class TextRecognitionSettings {
	public String imageUri;
	public Rect areaOfInterest;
	public Language[] recognitionLanguages;
	public boolean isTextOrientationDetectionEnabled = true;

	public TextRecognitionSettings() { }
}
