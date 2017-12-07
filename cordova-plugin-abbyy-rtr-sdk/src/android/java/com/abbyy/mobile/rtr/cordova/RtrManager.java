package com.abbyy.mobile.rtr.cordova;

import android.app.Application;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.IDataCaptureService;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.exceptions.InitializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RtrManager extends Application {

	private static RtrManager instance;
	private static Engine engine;
	private static String licenseFileName;
	private static String dataCaptureProfile;
	private static DataCaptureScenario customDataCaptureScenario;
	private static List<Language> languages;
	private static List<Language> selectedLanguages;
	private static boolean stopWhenStable;
	private static boolean stopButtonVisible;
	private static boolean flashlightVisible;
	private static boolean languageSelectionEnabled;
	private static float ratioHeight;
	private static float ratioWidth;

	public static RtrManager getInstance()
	{
		return instance;
	}

	public void initWithLicense() throws IOException, Engine.LicenseException
	{
		engine = Engine.load( getApplicationContext(), licenseFileName );
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		engine = null;
		languages = new ArrayList<>();
	}

	public static void setDataCaptureProfile( String profile ) {
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

	public static String getDataCaptureProfile()
	{
		return dataCaptureProfile;
	}

	public static DataCaptureScenario getCustomDataCaptureScenario()
	{
		return customDataCaptureScenario;
	}

	public ITextCaptureService createTextCaptureService( ITextCaptureService.Callback captureCallback ) throws InitializationException
	{
		if( engine == null ) {
			throw new InitializationException( "Initialize Engine first" );
		}
		return engine.createTextCaptureService( captureCallback );
	}

	public IDataCaptureService createDataCaptureService( String name, IDataCaptureService.Callback captureCallback ) throws InitializationException
	{
		if( engine == null ) {
			throw new InitializationException( "Initialize Engine first" );
		}
		return engine.createDataCaptureService( name, captureCallback );
	}

	public static List<Language> getLanguages()
	{
		return languages;
	}

	public static List<Language> getSelectedLanguages()
	{
		return selectedLanguages;
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

	public static void setLicenseFileName( String licenseFileName )
	{
		RtrManager.licenseFileName = licenseFileName;
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

	public static void setRatioHeight( float ratioHeight )
	{
		RtrManager.ratioHeight = ratioHeight;
	}

	public static void setRatioWidth( float ratioWidth )
	{
		RtrManager.ratioWidth = ratioWidth;
	}
}