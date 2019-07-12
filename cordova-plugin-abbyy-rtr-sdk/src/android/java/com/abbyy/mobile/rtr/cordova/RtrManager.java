package com.abbyy.mobile.rtr.cordova;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.IDataCaptureService;
import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.exceptions.InitializationException;
import com.abbyy.mobile.rtr.cordova.multipage.PageHolder;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class RtrManager {

	private static Engine engine;
	private static String licenseFileName;
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

	private static SparseArray<PageHolder> imageCaptureResult;

	public static void initWithLicense( Context context ) throws IOException, Engine.LicenseException
	{
		engine = Engine.load( context, licenseFileName );
	}

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

	public static ITextCaptureService createTextCaptureService( ITextCaptureService.Callback captureCallback ) throws InitializationException
	{
		if( engine == null ) {
			throw new InitializationException( "Initialize Engine first" );
		}
		return engine.createTextCaptureService( captureCallback );
	}

	public static IDataCaptureService createDataCaptureService( String name, IDataCaptureService.Callback captureCallback ) throws InitializationException
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
		ImageCaptureSettings.flashlightVisible = value;
	}

	public static void setManualCaptureVisible( boolean value )
	{
		ImageCaptureSettings.manualCaptureVisible = value;
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

	public static ImageCaptureScenario getImageCaptureScenario()
	{
		ImageCaptureScenario scenario = new ImageCaptureScenario( engine );
		scenario.setCropEnabled( ImageCaptureSettings.cropEnabled );
		scenario.setDocumentSize( ImageCaptureSettings.documentSize );
		scenario.setMinimumDocumentToViewRatio( ImageCaptureSettings.documentToViewRatio );
		return scenario;
	}

	public static void setCameraResolution( CaptureView.CameraSettings.Resolution resolution )
	{
		ImageCaptureSettings.cameraResolution = resolution;
	}

	public static void setDestination( ImageCaptureSettings.Destination destination )
	{
		ImageCaptureSettings.destination = destination;
	}

	public static void setExportType( ImageCaptureSettings.ExportType exportType )
	{
		ImageCaptureSettings.exportType = exportType;
	}

	public static void setCompressionType( IImagingCoreAPI.ExportOperation.CompressionType compressionType )
	{
		ImageCaptureSettings.pdfCompressionType = compressionType;
	}

	public static void setCompressionLevel( IImagingCoreAPI.ExportOperation.Compression compressionLevel )
	{
		ImageCaptureSettings.compressionLevel = compressionLevel;
	}

	public static void setCropEnabled( boolean cropEnabled )
	{
		ImageCaptureSettings.cropEnabled = cropEnabled;
	}

	public static void setDocumentToViewRatio( float documentToViewRatio )
	{
		ImageCaptureSettings.documentToViewRatio = documentToViewRatio;
	}

	public static void setDocumentSize( ImageCaptureScenario.DocumentSize documentSize )
	{
		ImageCaptureSettings.documentSize = documentSize;
	}

	public static void setShowPreview( boolean isShowPreview )
	{
		ImageCaptureSettings.showResultOnCapture = isShowPreview;
	}

	public static void setImageCount( int imageCount )
	{
		ImageCaptureSettings.pageCount = imageCount;
	}

	public static SparseArray<PageHolder> getImageCaptureResult()
	{
		return imageCaptureResult;
	}

	public static void setImageCaptureResult( SparseArray<PageHolder> pages )
	{
		RtrManager.imageCaptureResult = pages;
	}

	public static IImagingCoreAPI getImagingCoreAPI() {
		return engine.createImagingCoreAPI();
	}
}
