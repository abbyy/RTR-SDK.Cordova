// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.activities.DataCaptureActivity;
import com.abbyy.mobile.rtr.cordova.activities.ImageCaptureActivity;
import com.abbyy.mobile.rtr.cordova.activities.TextCaptureActivity;
import com.abbyy.mobile.rtr.cordova.multipage.MultiCaptureResult;
import com.abbyy.mobile.rtr.cordova.multipage.PageHolder;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class RtrPlugin extends CordovaPlugin {

	public static final int RESULT_OK = 0;
	public static final int RESULT_FAIL = 2;
	public static final String RTR_CUSTOM_RECOGNITION_LANGUAGES = "CustomRecognitionLanguages";

	private static final int REQUEST_CODE_TEXT_CAPTURE = 1;
	private static final int REQUEST_CODE_DATA_CAPTURE = 2;
	private static final int REQUEST_CODE_IMAGE_CAPTURE = 3;

	private static final int REQUEST_CODE_PERMISSIONS_TEXT_CAPTURE = 401;
	private static final int REQUEST_CODE_PERMISSIONS_DATA_CAPTURE = 402;
	private static final int REQUEST_CODE_PERMISSIONS_IMAGE_CAPTURE = 403;

	private static final String REQUIRED_PERMISSION = Manifest.permission.CAMERA;

	//region constants for parsing
	private static final String RTR_RECOGNITION_LANGUAGES_KEY = "recognitionLanguages";
	private static final String RTR_EXTENDED_SETTINGS = "extendedSettings";
	private static final String RTR_SELECTABLE_RECOGNITION_LANGUAGES_KEY = "selectableRecognitionLanguages";

	private static final String RTR_LICENSE_FILE_NAME_KEY = "licenseFileName";

	private static final String RTR_CAMERA_RESOLUTION_KEY = "cameraResolution";
	private static final String RTR_IS_SHOW_PREVIEW_KEY = "showPreview";
	private static final String RTR_DESTINATION_KEY = "destination";
	private static final String RTR_IMAGE_COUNT_KEY = "maxImagesCount";
	private static final String RTR_EXPORT_TYPE_KEY = "exportType";
	private static final String RTR_IS_MANUAL_CAPTURE_VISIBLE_KEY = "isCaptureButtonVisible";
	private static final String RTR_PDF_COMPRESSION_TYPE_KEY = "pdfCompressionType";
	private static final String RTR_COMPRESSION_LEVEL_KEY = "compressionLevel";
	private static final String RTR_DEFAULT_IMAGE_SETTINGS_KEY = "defaultImageSettings";
	private static final String RTR_IS_CROP_ENABLED_KEY = "cropEnabled";
	private static final String RTR_DOCUMENT_TO_VIEW_RATIO_KEY = "minimumDocumentToViewRatio";
	private static final String RTR_DOCUMENT_SIZE_KEY = "documentSize";

	private static final String RTR_AREA_OF_INTEREST_KEY = "areaOfInterest";
	private static final String RTR_IS_FLASHLIGHT_VISIBLE_KEY = "isFlashlightButtonVisible";
	private static final String RTR_IS_STOP_BUTTON_VISIBLE_KEY = "isStopButtonVisible";
	private static final String RTR_ORIENTATION_KEY = "orientation";

	private static final String RTR_CUSTOM_DATA_CAPTURE_SCENARIO_KEY = "customDataCaptureScenario";
	private static final String RTR_CUSTOM_DATA_CAPTURE_SCENARIO_NAME_KEY = "name";
	private static final String RTR_CUSTOM_DATA_CAPTURE_FIELDS_KEY = "fields";
	private static final String RTR_CUSTOM_DATA_CAPTURE_REG_EX_KEY = "regEx";
	private static final String RTR_CUSTOM_DATA_CAPTURE_DESCRIPTION_KEY = "description";

	private static final String RTR_DATA_CAPTURE_PROFILE_KEY = "profile";

	private static final String RTR_STOP_WHEN_STABLE_KEY = "stopWhenStable";

	public static final String INTENT_RESULT_KEY = "result";

	//endregion constants for parsing

	private CallbackContext callback = null;
	private JSONObject inputParameters;

	@Override
	public boolean execute( String action, JSONArray args, final CallbackContext callbackContext )
	{
		if( "startImageCapture".equals( action ) ) {
			PreferenceManager.getDefaultSharedPreferences( cordova.getActivity().getApplicationContext() ).edit().clear().apply();
			if( init( callbackContext, args ) ) {
				try {
					if( inputParameters.has( RTR_EXTENDED_SETTINGS ) ) {
						RtrManager.setExtendedSettings( parseExtendedSettings( inputParameters ) );
					}
					parseImageCaptureSettings( inputParameters );
				} catch( IllegalArgumentException | JSONException e ) {
					onError( e.getMessage() );
					return false;
				}
				RtrManager.setImageCaptureResult( new SparseArray<PageHolder>() );
				checkPermissionAndStartImageCapture();
				return true;
			}
		}
		if( "startTextCapture".equals( action ) ) {
			PreferenceManager.getDefaultSharedPreferences( cordova.getActivity().getApplicationContext() ).edit().clear().apply();
			if( init( callbackContext, args ) ) {
				try {
					if( inputParameters.has( RTR_EXTENDED_SETTINGS ) ) {
						RtrManager.setExtendedSettings( parseExtendedSettings( inputParameters ) );
					}
					RtrManager.setLanguages( parseLanguages( inputParameters ) );
					RtrManager.setSelectedLanguages( parseSelectedLanguage( inputParameters ) );
					parseUiSettings( inputParameters );
				} catch( IllegalArgumentException e ) {
					RtrManager.setLanguages( new ArrayList<Language>() );
					onError( e.getMessage() );
					return false;
				} catch( JSONException e ) {
					onError( e.getMessage() );
					return false;
				}
				checkPermissionAndStartTextCapture();
				return true;
			}
		}

		if( "startDataCapture".equals( action ) ) {
			if( init( callbackContext, args ) ) {
				RtrManager.setLanguageSelectionEnabled( true );
				try {
					parseScenario( inputParameters );
					parseUiSettings( inputParameters );
				} catch( IllegalArgumentException | JSONException e ) {
					onError( e.getMessage() );
					return false;
				}
				checkPermissionAndStartDataCapture();
				return true;
			}
		}
		return false;
	}

	private boolean init( final CallbackContext callbackContext, JSONArray args )
	{
		callback = callbackContext;

		if( args.length() <= 0 ) {
			onError( "The argument array has an invalid value." );
			return false;
		}

		try {
			inputParameters = args.getJSONObject( 0 );
			parseLicenseName( inputParameters );
			Context applicationContext = this.cordova.getActivity().getApplicationContext();
			RtrManager.initWithLicense( applicationContext );
		} catch( IOException e ) {
			Log.e( cordova.getActivity().getString( ResourcesUtils.getResId( "string", "app_name", cordova.getActivity() ) ), "Error loading ABBYY RTR SDK:", e );
			onError( "Could not load some required resource files. Make sure to configure " +
				"'assets' directory in your application and specify correct 'license file name'. See logcat for details." );
			return false;
		} catch( Engine.LicenseException e ) {
			Log.e( cordova.getActivity().getString( ResourcesUtils.getResId( "string", "app_name", cordova.getActivity() ) ), "Error loading ABBYY RTR SDK:", e );
			onError( "License not valid. Make sure you have a valid license file in the " +
				"'assets' directory and specify correct 'license file name' and 'application id'. See logcat for details." );
			return false;
		} catch( Throwable e ) {
			Log.e( cordova.getActivity().getString( ResourcesUtils.getResId( "string", "app_name", cordova.getActivity() ) ), "Error loading ABBYY RTR SDK:", e );
			onError( "Unspecified error while loading the engine. See logcat for details." );
			return false;
		}
		return true;
	}

	private void checkPermissionAndStartImageCapture()
	{
		if( !this.cordova.hasPermission( REQUIRED_PERMISSION ) ||
			!this.cordova.hasPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE ) ) {
			this.cordova.requestPermissions( this, REQUEST_CODE_PERMISSIONS_IMAGE_CAPTURE,
				new String[] { REQUIRED_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE } );
			return;
		}
		startImageCapture();
	}

	private void checkPermissionAndStartTextCapture()
	{
		if( !this.cordova.hasPermission( REQUIRED_PERMISSION ) ) {
			this.cordova.requestPermission( this, REQUEST_CODE_PERMISSIONS_TEXT_CAPTURE, REQUIRED_PERMISSION );
			return;
		}
		startTextCapture();
	}

	private void checkPermissionAndStartDataCapture()
	{
		if( !this.cordova.hasPermission( REQUIRED_PERMISSION ) ) {
			this.cordova.requestPermission( this, REQUEST_CODE_PERMISSIONS_DATA_CAPTURE, REQUIRED_PERMISSION );
			return;
		}
		startDataCapture();
	}

	private void startImageCapture()
	{
		Intent intent = ImageCaptureActivity.newImageCaptureIntent( cordova.getActivity() );
		this.cordova.setActivityResultCallback( this );
		this.cordova.startActivityForResult( this, intent, REQUEST_CODE_IMAGE_CAPTURE );
	}

	private void startTextCapture()
	{
		Intent intent = TextCaptureActivity.newTextCaptureIntent( cordova.getActivity() );
		this.cordova.setActivityResultCallback( this );
		this.cordova.startActivityForResult( this, intent, REQUEST_CODE_TEXT_CAPTURE );
	}

	private void startDataCapture()
	{
		Intent intent = DataCaptureActivity.newDataCaptureIntent( cordova.getActivity() );
		this.cordova.setActivityResultCallback( this );
		this.cordova.startActivityForResult( this, intent, REQUEST_CODE_DATA_CAPTURE );
	}

	@Override
	public void onRequestPermissionResult( int requestCode, String[] permissions,
		int[] grantResults )
	{
		for( int result : grantResults ) {
			if( result == PackageManager.PERMISSION_DENIED ) {
				Toast.makeText( cordova.getActivity(), ResourcesUtils.getResId( "string", "runtime_permissions_txt", cordova.getActivity() ), Toast.LENGTH_SHORT ).show();
				onError( "Camera access denied" );
				return;
			}
		}

		switch( requestCode ) {
			case REQUEST_CODE_PERMISSIONS_IMAGE_CAPTURE:
				startImageCapture();
				break;
			case REQUEST_CODE_PERMISSIONS_TEXT_CAPTURE:
				startTextCapture();
				break;
			case REQUEST_CODE_PERMISSIONS_DATA_CAPTURE:
				startDataCapture();
				break;
		}
	}

	@Override
	@SuppressWarnings( "all" )
	public void onActivityResult( int requestCode, int resultCode, Intent intent )
	{
		if( null != intent ) {
			switch( resultCode ) {
				case RESULT_OK:
					HashMap<String, Object> result = (HashMap<String, Object>) intent.getSerializableExtra( INTENT_RESULT_KEY );
					if( requestCode == REQUEST_CODE_IMAGE_CAPTURE ) {
						// For image capture we constuct successful result json right before passing to JS
						result = MultiCaptureResult.getJsonResult( RtrManager.getImageCaptureResult(), cordova.getContext() );
						RtrManager.setImageCaptureResult( null );
					}
					callback.success( new JSONObject( result ) );
					break;
				case RESULT_FAIL:
					result = (HashMap<String, Object>) intent.getSerializableExtra( INTENT_RESULT_KEY );
					callback.error( new JSONObject( result ) );
					break;
			}
		}
	}

	private void onError( String description )
	{
		HashMap<String, String> info = new HashMap<>();
		info.put( "description", description );

		HashMap<String, Object> result = new HashMap<>();
		result.put( "error", info );

		callback.error( new JSONObject( result ) );
	}

	private void parseLicenseName( JSONObject arg ) throws JSONException
	{
		String licenseFileName = "AbbyyRtrSdk.license";
		if( arg.has( RTR_LICENSE_FILE_NAME_KEY ) ) {
			licenseFileName = arg.getString( RTR_LICENSE_FILE_NAME_KEY );
		}
		RtrManager.setLicenseFileName( licenseFileName );
	}

	private void parseCameraResolution( JSONObject arg ) throws JSONException
	{
		CaptureView.CameraSettings.Resolution resolution = ImageCaptureSettings.cameraResolution;
		if( arg.has( RTR_CAMERA_RESOLUTION_KEY ) ) {
			String resolutionName = arg.getString( RTR_CAMERA_RESOLUTION_KEY );
			switch( resolutionName ) {
				case "HD":
					resolution = CaptureView.CameraSettings.Resolution.HD;
					break;
				case "FullHD":
					resolution = CaptureView.CameraSettings.Resolution.FULL_HD;
					break;
				case "4K":
					break;
			}
		}
		RtrManager.setCameraResolution( resolution );
	}

	private void parseAutoStop( JSONObject arg ) throws JSONException
	{
		boolean stopWhenStable = true;
		if( arg.has( RTR_STOP_WHEN_STABLE_KEY ) ) {
			stopWhenStable = arg.getBoolean( RTR_STOP_WHEN_STABLE_KEY );
		}
		RtrManager.setStopWhenStable( stopWhenStable );
	}

	private void parseOrientation( JSONObject arg ) throws JSONException
	{
		int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		if( arg.has( RTR_ORIENTATION_KEY ) ) {
			String value = arg.getString( RTR_ORIENTATION_KEY );
			if( value.equals( "portrait" ) ) {
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			} else if( value.equals( "landscape" ) ) {
				orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
			}
		}
		RtrManager.setOrientation( orientation );
	}

	private void parseDestination( JSONObject arg ) throws JSONException
	{
		ImageCaptureSettings.Destination destination = ImageCaptureSettings.destination;
		if( arg.has( RTR_DESTINATION_KEY ) ) {
			String value = arg.getString( RTR_DESTINATION_KEY );
			if( value.equals( "base64" ) ) {
				destination = ImageCaptureSettings.Destination.BASE64;
			} else if( value.equals( "file" ) ) {
				destination = ImageCaptureSettings.Destination.FILE;
			}
		}
		RtrManager.setDestination( destination );
	}

	private void parseExportType( JSONObject arg ) throws JSONException
	{
		ImageCaptureSettings.ExportType exportType = ImageCaptureSettings.exportType;
		if( arg.has( RTR_EXPORT_TYPE_KEY ) ) {
			String value = arg.getString( RTR_EXPORT_TYPE_KEY );
			switch( value ) {
				case "jpg":
					exportType = ImageCaptureSettings.ExportType.JPG;
					break;
				case "png":
					exportType = ImageCaptureSettings.ExportType.PNG;
					break;
				case "pdf":
					exportType = ImageCaptureSettings.ExportType.PDF;
					break;
			}
		}
		RtrManager.setExportType( exportType );
	}

	private void parseCompressionType( JSONObject arg ) throws JSONException
	{
		IImagingCoreAPI.ExportOperation.CompressionType pdfCompressionType = ImageCaptureSettings.pdfCompressionType;
		if( arg.has( RTR_PDF_COMPRESSION_TYPE_KEY ) ) {
			String value = arg.getString( RTR_PDF_COMPRESSION_TYPE_KEY );
			switch( value ) {
				case "jpg":
					pdfCompressionType = IImagingCoreAPI.ExportOperation.CompressionType.Jpg;
					break;
			}
		}
		RtrManager.setCompressionType( pdfCompressionType );
	}

	private void parseCompressionLevel( JSONObject arg ) throws JSONException
	{
		IImagingCoreAPI.ExportOperation.Compression compressionLevel = ImageCaptureSettings.compressionLevel;
		if( arg.has( RTR_COMPRESSION_LEVEL_KEY ) ) {
			String value = arg.getString( RTR_COMPRESSION_LEVEL_KEY );
			switch( value ) {
				case "Low":
					compressionLevel = IImagingCoreAPI.ExportOperation.Compression.Low;
					break;
				case "Normal":
					compressionLevel = IImagingCoreAPI.ExportOperation.Compression.Normal;
					break;
				case "High":
					compressionLevel = IImagingCoreAPI.ExportOperation.Compression.High;
					break;
				case "ExtraHigh":
					compressionLevel = IImagingCoreAPI.ExportOperation.Compression.ExtraHigh;
					break;
			}
		}
		RtrManager.setCompressionLevel( compressionLevel );
	}

	private void parseDefaultImageSettings( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_DEFAULT_IMAGE_SETTINGS_KEY ) ) {
			JSONObject settings = arg.getJSONObject( RTR_DEFAULT_IMAGE_SETTINGS_KEY );
			parseMinimumDocumentToViewRatio( settings );
			parseDocumentSize( settings );
			parseCropEnabled( settings );
		}
	}

	private void parseCropEnabled( JSONObject settings ) throws JSONException
	{
		boolean cropEnabled = ImageCaptureSettings.cropEnabled;
		if( settings.has( RTR_IS_CROP_ENABLED_KEY ) ) {
			cropEnabled = settings.getBoolean( RTR_IS_CROP_ENABLED_KEY );
		}
		RtrManager.setCropEnabled( cropEnabled );
	}

	private void parseMinimumDocumentToViewRatio( JSONObject settings ) throws JSONException
	{
		float documentRatio = ImageCaptureSettings.documentToViewRatio;

		if( settings.has( RTR_DOCUMENT_TO_VIEW_RATIO_KEY ) ) {
			documentRatio = Float.parseFloat( settings.getString( RTR_DOCUMENT_TO_VIEW_RATIO_KEY ) );
		}

		RtrManager.setDocumentToViewRatio( documentRatio );
	}

	private void parseDocumentSize( JSONObject arg ) throws JSONException
	{
		ImageCaptureScenario.DocumentSize size = ImageCaptureSettings.documentSize;

		if( arg.has( RTR_DOCUMENT_SIZE_KEY ) ) {
			String[] parts = arg.getString( RTR_DOCUMENT_SIZE_KEY ).split( " " );
			if( parts.length == 1 ) {
				switch( parts[0] ) {
					case "A4":
						size = ImageCaptureScenario.DocumentSize.A4;
						break;
					case "BusinessCard":
						size = ImageCaptureScenario.DocumentSize.BUSINESS_CARD;
						break;
					case "Letter":
						size = ImageCaptureScenario.DocumentSize.LETTER;
						break;
					case "Any":
					default:
						size = ImageCaptureScenario.DocumentSize.ANY;
				}
			} else {
				float width = Float.parseFloat( parts[0] );
				float height = Float.parseFloat( parts[1] );
				size = new ImageCaptureScenario.DocumentSize( width, height );
			}
		}

		RtrManager.setDocumentSize( size );
	}

	private void parseStopButtonVisibility( JSONObject arg ) throws JSONException
	{
		boolean stopButtonVisible = true;
		if( arg.has( RTR_IS_STOP_BUTTON_VISIBLE_KEY ) ) {
			stopButtonVisible = arg.getBoolean( RTR_IS_STOP_BUTTON_VISIBLE_KEY );
		}
		RtrManager.setStopButtonVisible( stopButtonVisible );
	}

	private void parseToggleFlash( JSONObject arg ) throws JSONException
	{
		boolean isFlashlightVisible = RtrManager.isFlashlightVisible();
		if( arg.has( RTR_IS_FLASHLIGHT_VISIBLE_KEY ) ) {
			isFlashlightVisible = arg.getBoolean( RTR_IS_FLASHLIGHT_VISIBLE_KEY );
		}
		RtrManager.setFlashlightVisible( isFlashlightVisible );
	}

	private void parseToggleManualCapture( JSONObject arg ) throws JSONException
	{
		boolean isManualCaptureVisible = ImageCaptureSettings.manualCaptureVisible;
		if( arg.has( RTR_IS_MANUAL_CAPTURE_VISIBLE_KEY ) ) {
			isManualCaptureVisible = arg.getBoolean( RTR_IS_MANUAL_CAPTURE_VISIBLE_KEY );
		}
		RtrManager.setManualCaptureVisible( isManualCaptureVisible );
	}

	private static void checkValidAreaOfInterest( float value )
	{
		if( value > 1.0f || value < 0.01f ) {
			throw new IllegalArgumentException( "Area of interest parts have to be from 0.01 to 1" );
		}
	}

	private void parseAreaOfInterest( JSONObject arg ) throws JSONException
	{
		float ratioWidth;
		float ratioHeight;

		if( arg.has( RTR_AREA_OF_INTEREST_KEY ) ) {
			String[] parts = arg.getString( RTR_AREA_OF_INTEREST_KEY ).split( " " );
			ratioWidth = Float.parseFloat( parts[0] );
			ratioHeight = Float.parseFloat( parts[1] );
		} else {
			ratioWidth = 0.8f;
			ratioHeight = 0.3f;
		}

		checkValidAreaOfInterest( ratioWidth );
		checkValidAreaOfInterest( ratioHeight );

		RtrManager.setRatioWidth( ratioWidth );
		RtrManager.setRatioHeight( ratioHeight );
	}

	private List<Language> parseLanguages( JSONObject arg ) throws JSONException
	{
		HashMap<String, String> extendedSettings = RtrManager.getExtendedSettings();
		List<Language> languages = parseLanguagesInternal( arg, RTR_SELECTABLE_RECOGNITION_LANGUAGES_KEY, true );

		if( extendedSettings != null ) {
			if( extendedSettings.containsKey( RTR_CUSTOM_RECOGNITION_LANGUAGES ) ) {
				RtrManager.setLanguageSelectionEnabled( false );
			} else {
				RtrManager.setLanguageSelectionEnabled( !languages.isEmpty() );
			}
		} else {
			RtrManager.setLanguageSelectionEnabled( !languages.isEmpty() );
		}
		return languages;
	}

	private List<Language> parseSelectedLanguage( JSONObject arg ) throws JSONException
	{
		List<Language> languages = parseLanguagesInternal( arg, RTR_RECOGNITION_LANGUAGES_KEY, true );

		if( languages.isEmpty() ) {
			languages.add( Language.English );
		}

		return languages;
	}

	private HashMap<String, String> parseExtendedSettings( JSONObject arg ) throws JSONException
	{
		HashMap<String, String> map = new HashMap<>();
		JSONObject jsonObject = arg.getJSONObject( RTR_EXTENDED_SETTINGS );
		Iterator<?> keys = jsonObject.keys();
		while( keys.hasNext() ) {
			String key = (String) keys.next();
			String value = jsonObject.getString( key );
			map.put( key, value );
		}
		return map;
	}

	private void parseImageCaptureSettings( JSONObject arg ) throws JSONException
	{
		parseCameraResolution( arg );
		parseToggleFlash( arg );
		parseToggleManualCapture( arg );
		parseOrientation( arg );
		parseShowPreview( arg );
		parseImageCount( arg );
		parseDestination( arg );
		parseExportType( arg );
		parseCompressionType( arg );
		parseCompressionLevel( arg );
		parseDefaultImageSettings( arg );
	}

	private void parseShowPreview( JSONObject arg ) throws JSONException
	{
		boolean isShowPreview = false;
		if( arg.has( RTR_IS_SHOW_PREVIEW_KEY ) ) {
			isShowPreview = arg.getBoolean( RTR_IS_SHOW_PREVIEW_KEY );
		}
		RtrManager.setShowPreview( isShowPreview );
	}

	private void parseImageCount( JSONObject arg ) throws JSONException
	{
		int imageCount = ImageCaptureSettings.pageCount;
		if( arg.has( RTR_IMAGE_COUNT_KEY ) ) {
			imageCount = arg.getInt( RTR_IMAGE_COUNT_KEY );
		}
		RtrManager.setImageCount( imageCount );
	}

	private Language parseLanguageName( String name )
	{
		for( Language language : Language.values() ) {
			if( language.name().equals( name ) ) {
				return language;
			}
		}
		throw new IllegalArgumentException( "Unknown language name" );
	}

	private List<Language> parseLanguagesInternal( JSONObject arg, String key, boolean isEnglishByDefault ) throws JSONException
	{
		List<Language> languages = new ArrayList<>();
		if( arg.has( key ) ) {
			JSONArray array = arg.getJSONArray( key );
			for( int i = 0; i < array.length(); i++ ) {
				languages.add( parseLanguageName( array.getString( i ) ) );
			}
		} else if( isEnglishByDefault ) {
			languages.add( Language.English );
		}

		return languages;
	}

	private void parseUiSettings( JSONObject arg ) throws JSONException
	{
		parseAutoStop( arg );
		parseStopButtonVisibility( arg );
		parseToggleFlash( arg );
		parseAreaOfInterest( arg );
		parseOrientation( arg );
	}

	private DataCaptureScenario parseCustomScenario( JSONObject arg ) throws JSONException
	{
		JSONObject customDataCaptureSettings = arg.getJSONObject( RTR_CUSTOM_DATA_CAPTURE_SCENARIO_KEY );

		String name = customDataCaptureSettings.getString( RTR_CUSTOM_DATA_CAPTURE_SCENARIO_NAME_KEY );
		String description = name;
		if( customDataCaptureSettings.has( RTR_CUSTOM_DATA_CAPTURE_DESCRIPTION_KEY ) ) {
			description = customDataCaptureSettings.getString( RTR_CUSTOM_DATA_CAPTURE_DESCRIPTION_KEY );
		}

		String regEx = null;

		if( customDataCaptureSettings.has( RTR_CUSTOM_DATA_CAPTURE_FIELDS_KEY ) ) {
			JSONArray fields = customDataCaptureSettings.getJSONArray( RTR_CUSTOM_DATA_CAPTURE_FIELDS_KEY );

			if( fields.length() > 0 ) {
				JSONObject field = fields.getJSONObject( 0 );

				if( field.has( RTR_CUSTOM_DATA_CAPTURE_REG_EX_KEY ) ) {
					regEx = field.getString( RTR_CUSTOM_DATA_CAPTURE_REG_EX_KEY );
				}
			}
		}

		return new DataCaptureScenario(
			name,
			description,
			parseSelectedLanguage( customDataCaptureSettings ),
			regEx
		);
	}

	private void parseScenario( JSONObject arg ) throws JSONException
	{
		String profile = null;
		DataCaptureScenario customScenario = null;

		if( arg.has( RTR_CUSTOM_DATA_CAPTURE_SCENARIO_KEY ) ) {
			customScenario = parseCustomScenario( arg );
		} else if( arg.has( RTR_DATA_CAPTURE_PROFILE_KEY ) ) {
			profile = arg.getString( RTR_DATA_CAPTURE_PROFILE_KEY );
			List<Language> languages = parseLanguagesInternal( arg, RTR_RECOGNITION_LANGUAGES_KEY, false );
			RtrManager.setLanguages( languages );
		} else {
			throw new JSONException( "Invalid Data Capture scenario settings." );
		}

		RtrManager.setCustomDataCaptureScenario( customScenario );
		RtrManager.setDataCaptureProfile( profile );
	}

}
