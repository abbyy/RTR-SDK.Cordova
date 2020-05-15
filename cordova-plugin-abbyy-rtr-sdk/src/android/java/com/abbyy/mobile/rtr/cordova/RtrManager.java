// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova;

import com.abbyy.mobile.rtr.IDataCaptureService;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RtrManager {

	private static String dataCaptureProfile;
	private static DataCaptureScenario customDataCaptureScenario;
	private static List<Language> languages = new ArrayList<>();
	private static List<Language> selectedLanguages;
	private static boolean stopWhenStable;
	private static boolean stopButtonVisible;
	private static boolean flashlightVisible;
	private static boolean languageSelectionEnabled;
	private static float ratioHeight;
	private static float ratioWidth;
	private static HashMap<String, String> extendedSettings;
	private static int orientation;

	public static void setDataCaptureProfile( String profile )
	{
		RtrManager.dataCaptureProfile = profile;
	}

	public static void setCustomDataCaptureScenario( DataCaptureScenario scenario )
	{
		RtrManager.customDataCaptureScenario = scenario;
	}

	public static void setLanguages( List<Language> languages )
	{
		RtrManager.languages = languages;
	}

	public static void setSelectedLanguages( List<Language> languages )
	{
		RtrManager.selectedLanguages = languages;
	}

	public static void setExtendedSettings( HashMap<String, String> extendedSettings )
	{
		RtrManager.extendedSettings = extendedSettings;
	}

	public static String getDataCaptureProfile()
	{
		return dataCaptureProfile;
	}

	public static DataCaptureScenario getCustomDataCaptureScenario()
	{
		return customDataCaptureScenario;
	}

	public static ITextCaptureService createTextCaptureService( ITextCaptureService.Callback captureCallback )
	{
		return SharedEngine.get().createTextCaptureService( captureCallback );
	}

	public static IDataCaptureService createDataCaptureService( String name, IDataCaptureService.Callback captureCallback )
	{
		return SharedEngine.get().createDataCaptureService( name, captureCallback );
	}

	public static List<Language> getLanguages()
	{
		return languages;
	}

	public static List<Language> getSelectedLanguages()
	{
		return selectedLanguages;
	}

	public static HashMap<String, String> getExtendedSettings()
	{
		return extendedSettings;
	}

	public static boolean stopWhenStable()
	{
		return stopWhenStable;
	}

	public static boolean isStopButtonVisible()
	{
		return stopButtonVisible;
	}

	public static boolean isFlashlightVisible()
	{
		return flashlightVisible;
	}

	public static boolean isLanguageSelectionEnabled()
	{
		return languageSelectionEnabled;
	}

	public static float getRatioHeight()
	{
		return ratioHeight;
	}

	public static float getRatioWidth()
	{
		return ratioWidth;
	}

	public static int getOrientation()
	{
		return orientation;
	}

	public static void setStopWhenStable( boolean autoStop )
	{
		RtrManager.stopWhenStable = autoStop;
	}

	public static void setStopButtonVisible( boolean value ) { RtrManager.stopButtonVisible = value; }

	public static void setFlashlightVisible( boolean value )
	{
		RtrManager.flashlightVisible = value;
	}

	public static void setLanguageSelectionEnabled( boolean value )
	{
		RtrManager.languageSelectionEnabled = value;
	}

	public static void setRatioHeight( float height )
	{
		ratioHeight = height;
	}

	public static void setRatioWidth( float width )
	{
		ratioWidth = width;
	}

	public static void setOrientation( int orientation )
	{
		RtrManager.orientation = orientation;
	}
}
