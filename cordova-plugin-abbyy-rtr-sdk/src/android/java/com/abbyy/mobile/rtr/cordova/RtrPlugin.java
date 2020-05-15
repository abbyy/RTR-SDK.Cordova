// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.activities.DataCaptureActivity;
import com.abbyy.mobile.rtr.cordova.activities.ImageCaptureActivity;
import com.abbyy.mobile.rtr.cordova.activities.TextCaptureActivity;
import com.abbyy.mobile.rtr.cordova.image.ImageCaptureResult;
import com.abbyy.mobile.rtr.cordova.image.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.image.MultiCaptureResult;
import com.abbyy.mobile.rtr.javascript.JSCallback;
import com.abbyy.mobile.rtr.javascript.SharedEngine;
import com.abbyy.mobile.rtr.javascript.data.DataCapture;
import com.abbyy.mobile.rtr.javascript.image.Destination;
import com.abbyy.mobile.rtr.javascript.image.ExportType;
import com.abbyy.mobile.rtr.javascript.image.ImagingCore;
import com.abbyy.mobile.rtr.javascript.text.TextRecognition;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;

import static com.abbyy.mobile.rtr.cordova.utils.TextUtils.parseLanguagesInternal;

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
	public static final String RTR_RECOGNITION_LANGUAGES_KEY = "recognitionLanguages";
	private static final String RTR_EXTENDED_SETTINGS = "extendedSettings";
	private static final String RTR_SELECTABLE_RECOGNITION_LANGUAGES_KEY = "selectableRecognitionLanguages";

	private static final String RTR_LICENSE_FILE_NAME_KEY = "licenseFileName";

	private static final String RTR_CAMERA_RESOLUTION_KEY = "cameraResolution";
	private static final String RTR_IS_SHOW_PREVIEW_KEY = "showPreview";
	private static final String RTR_DESTINATION_KEY = "destination";
	private static final String RTR_IMAGE_COUNT_KEY = "maxImagesCount";
	private static final String RTR_REQUIRED_PAGE_COUNT_KEY = "requiredPageCount";
	private static final String RTR_EXPORT_TYPE_KEY = "exportType";
	private static final String RTR_IS_MANUAL_CAPTURE_VISIBLE_KEY = "isCaptureButtonVisible";
	private static final String RTR_IS_GALLERY_BUTTON_VISIBLE_KEY = "isGalleryButtonVisible";
	private static final String RTR_COMPRESSION_LEVEL_KEY = "compressionLevel";
	private static final String RTR_DEFAULT_IMAGE_SETTINGS_KEY = "defaultImageSettings";
	private static final String RTR_ASPECT_RATIO_MIN_KEY = "aspectRatioMin";
	private static final String RTR_ASPECT_RATIO_MAX_KEY = "aspectRatioMax";
	private static final String RTR_DOCUMENT_TO_VIEW_RATIO_KEY = "minimumDocumentToViewRatio";
	private static final String RTR_DOCUMENT_SIZE_KEY = "documentSize";
	private static final String RTR_IMAGE_FROM_GALLERY_MAX_SIZE_KEY = "imageFromGalleryMaxSize";

	public static final String RTR_AREA_OF_INTEREST_KEY = "areaOfInterest";
	private static final String RTR_IS_FLASHLIGHT_VISIBLE_IMAGE_CAPTURE_KEY = "isFlashlightButtonVisible";
	private static final String RTR_IS_FLASHLIGHT_VISIBLE_KEY = "isFlashlightVisible";
	private static final String RTR_IS_STOP_BUTTON_VISIBLE_KEY = "isStopButtonVisible";
	private static final String RTR_ORIENTATION_KEY = "orientation";

	private static final String RTR_CUSTOM_DATA_CAPTURE_SCENARIO_KEY = "customDataCaptureScenario";
	private static final String RTR_CUSTOM_DATA_CAPTURE_SCENARIO_NAME_KEY = "name";
	private static final String RTR_CUSTOM_DATA_CAPTURE_FIELDS_KEY = "fields";
	private static final String RTR_CUSTOM_DATA_CAPTURE_REG_EX_KEY = "regEx";
	private static final String RTR_CUSTOM_DATA_CAPTURE_DESCRIPTION_KEY = "description";

	public static final String RTR_DATA_CAPTURE_PROFILE_KEY = "profile";

	private static final String RTR_STOP_WHEN_STABLE_KEY = "stopWhenStable";

	public static final String INTENT_RESULT_KEY = "result";

	//endregion constants for parsing

	private CallbackContext callback = null;
	private JSONObject inputParameters;

	private ImageCaptureSettings imageCaptureSettings = null;

	@Override
	public boolean execute( String action, JSONArray args, final CallbackContext callbackContext )
	{
		switch( action ) {
			case "startImageCapture":
				return performImageCapture( args, callbackContext );
			case "startTextCapture":
				return performTextCapture( args, callbackContext );
			case "startDataCapture":
				return performDataCapture( args, callbackContext );
			case "recognizeText":
				return performTextRecognition( args, callbackContext );
			case "extractData":
				return performDataExtraction( args, callbackContext );
			case "assessQualityForOcr":
				return performQualityAssessmentForOcr( args, callbackContext );
			case "detectDocumentBoundary":
				return performBoundaryDetection( args, callbackContext );
			case "cropImage":
				return performImageCrop( args, callbackContext );
			case "rotateImage":
				return performImageRotation( args, callbackContext );
			case "exportImage":
				return performImageExport( args, callbackContext );
			case "exportImagesToPdf":
				return performImageExportToPdf( args, callbackContext );
		}

		return false;
	}

	private boolean performTextRecognition( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				TextRecognition.recognizeTextSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performDataExtraction( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				DataCapture.extractDataSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performBoundaryDetection( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				ImagingCore.detectDocumentBoundarySync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performQualityAssessmentForOcr( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				ImagingCore.assessQualityForOcrSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performImageCrop( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				ImagingCore.cropImageSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performImageRotation( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				ImagingCore.rotateImageSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performImageExport( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				ImagingCore.exportImageSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performImageExportToPdf( JSONArray args, CallbackContext callbackContext )
	{
		if( !initApi( callbackContext, args ) ) {
			return false;
		}
		cordova.getThreadPool().execute( new Runnable() {
			@Override
			public void run()
			{
				ImagingCore.exportImagesToPdfSync( cordova.getActivity().getApplication(), inputParameters, new JSCallback() {
					@Override
					public void onSuccess( JSONObject result )
					{
						RtrPlugin.this.onSuccess( result, true );
					}

					@Override
					public void onError( String errorCode, String message, Throwable exception )
					{
						RtrPlugin.this.onError( message, true );
					}
				} );
			}
		} );
		return true;
	}

	private boolean performDataCapture( JSONArray args, CallbackContext callbackContext )
	{
		if( !init( callbackContext, args ) ) {
			return false;
		}
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

	private boolean performTextCapture( JSONArray args, CallbackContext callbackContext )
	{
		PreferenceManager.getDefaultSharedPreferences( cordova.getActivity().getApplicationContext() ).edit().clear().apply();
		if( !init( callbackContext, args ) ) {
			return false;
		}
		try {
			if( inputParameters.has( RTR_EXTENDED_SETTINGS ) ) {
				RtrManager.setExtendedSettings( parseExtendedSettings( inputParameters ) );
			}
			RtrManager.setLanguages( parseLanguages( inputParameters ) );
			RtrManager.setSelectedLanguages( parseSelectedLanguage( inputParameters ) );

			SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				cordova.getActivity().getApplicationContext()
			).edit();
			for( Language language : RtrManager.getSelectedLanguages() ) {
				sharedPreferences.putBoolean( language.name(), true );
			}
			sharedPreferences.apply();
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

	private boolean performImageCapture( JSONArray args, CallbackContext callbackContext )
	{
		if( !init( callbackContext, args ) ) {
			return false;
		}
		try {
			if( inputParameters.has( RTR_EXTENDED_SETTINGS ) ) {
				RtrManager.setExtendedSettings( parseExtendedSettings( inputParameters ) );
			}
			parseImageCaptureSettings( inputParameters );
		} catch( IllegalArgumentException | JSONException e ) {
			onError( e.getMessage() );
			return false;
		}
		checkPermissionAndStartImageCapture();
		return true;
	}

	private boolean init( final CallbackContext callbackContext, JSONArray args )
	{
		callback = callbackContext;

		if( args.length() <= 0 ) {
			onError( "The argument array has an invalid value." );
			return false;
		}
		String licenseName;
		try {
			inputParameters = args.getJSONObject( 0 );
			licenseName = parseLicenseName( inputParameters );
		} catch( JSONException e ) {
			onError( "JSON parse for license name failed" );
			return false;
		}
		Context applicationContext = this.cordova.getActivity().getApplicationContext();
		return SharedEngine.initializeEngineIfNeeded( (Application) applicationContext, licenseName, new JSCallback() {
			@Override
			public void onSuccess( JSONObject result )
			{
			}

			@Override
			public void onError( String errorCode, String message, Throwable exception )
			{
				if( exception instanceof IOException ) {
					RtrPlugin.this.onError( message );
				}
			}
		} );
	}

	private boolean initApi( final CallbackContext callbackContext, JSONArray args )
	{
		callback = callbackContext;

		if( args.length() <= 0 ) {
			onError( "The argument array has an invalid value." );
			return false;
		}
		try {
			inputParameters = args.getJSONObject( 0 );
		} catch( JSONException e ) {
			onError( "JSON parse for license name failed" );
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
		Intent intent = ImageCaptureActivity.newImageCaptureIntent( cordova.getActivity(), imageCaptureSettings );
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
					HashMap<String, Object> result;
					if( requestCode == REQUEST_CODE_IMAGE_CAPTURE ) {
						if (intent.hasExtra( ImageCaptureActivity.IMAGE_CAPTURE_RESULT_KEY )) {
							final ImageCaptureResult imageCaptureResult = intent.getParcelableExtra( ImageCaptureActivity.IMAGE_CAPTURE_RESULT_KEY );
							// For image capture we constuct successful result json right before passing to JS
							result = MultiCaptureResult.getJsonResult( imageCaptureResult, imageCaptureSettings, cordova.getContext() );
						} else {
							result = MultiCaptureResult.getCanceledJsonResult();
						}
					} else {
						result = (HashMap<String, Object>) intent.getSerializableExtra( INTENT_RESULT_KEY );
					}
					onSuccess( new JSONObject( result ) );
					break;
				case RESULT_FAIL:
					if( requestCode == REQUEST_CODE_IMAGE_CAPTURE ) {
						result = (HashMap<String, Object>) intent.getSerializableExtra( ImageCaptureActivity.ERROR_DESCRIPTION_RESULT_KEY );
					} else {
						result = (HashMap<String, Object>) intent.getSerializableExtra( INTENT_RESULT_KEY );
					}
					callback.error( new JSONObject( result ) );
					break;
			}
		}
	}

	private void onSuccess( final JSONObject result )
	{
		onSuccess( result, false );
	}

	private void onSuccess( final JSONObject result, boolean routeToMainThread )
	{
		if( routeToMainThread ) {
			cordova.getActivity().runOnUiThread( new Runnable() {
				@Override
				public void run()
				{
					callback.success( result );
				}
			} );
		} else {
			callback.success( result );
		}
	}

	private void onError( String description )
	{
		onError( description, false );
	}

	private void onError( String description, boolean routeToMainThread )
	{
		HashMap<String, String> info = new HashMap<>();
		info.put( "description", description );

		final HashMap<String, Object> result = new HashMap<>();
		result.put( "error", info );

		if( routeToMainThread ) {
			cordova.getActivity().runOnUiThread( new Runnable() {
				@Override
				public void run()
				{
					callback.error( new JSONObject( result ) );
				}
			} );
		} else {
			callback.error( new JSONObject( result ) );
		}
	}

	private String parseLicenseName( JSONObject arg ) throws JSONException
	{
		String licenseFileName = "AbbyyRtrSdk.License";
		if( arg.has( RTR_LICENSE_FILE_NAME_KEY ) ) {
			licenseFileName = arg.getString( RTR_LICENSE_FILE_NAME_KEY );
		}
		return licenseFileName;
	}

	private void parseCameraResolution( JSONObject arg ) throws JSONException
	{
		CaptureView.CameraSettings.Resolution resolution = imageCaptureSettings.cameraResolution;
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
		imageCaptureSettings.cameraResolution = resolution;
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
		if( arg.has( RTR_DESTINATION_KEY ) ) {
			Destination destination = imageCaptureSettings.destination;
			String value = arg.getString( RTR_DESTINATION_KEY );
			if( value.equals( "base64" ) ) {
				destination = Destination.Base64;
			} else if( value.equals( "file" ) ) {
				destination = Destination.File;
			}
			imageCaptureSettings.destination = destination;
		}
	}

	private void parseExportType( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_EXPORT_TYPE_KEY ) ) {
			com.abbyy.mobile.rtr.cordova.image.ExportType exportType = imageCaptureSettings.exportType;
			String value = arg.getString( RTR_EXPORT_TYPE_KEY );
			switch( value ) {
				case "jpg":
					exportType = com.abbyy.mobile.rtr.cordova.image.ExportType.JPG;
					break;
				case "png":
					exportType = com.abbyy.mobile.rtr.cordova.image.ExportType.PNG;
					break;
				case "pdf":
					exportType = com.abbyy.mobile.rtr.cordova.image.ExportType.PDF;
					break;
			}
			imageCaptureSettings.exportType = exportType;
		}
	}

	private void parseCompressionLevel( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_COMPRESSION_LEVEL_KEY ) ) {
			IImagingCoreAPI.ExportOperation.Compression compressionLevel = imageCaptureSettings.compressionLevel;
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
			imageCaptureSettings.compressionLevel = compressionLevel;
		}
	}

	private void parseDefaultImageSettings( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_DEFAULT_IMAGE_SETTINGS_KEY ) ) {
			JSONObject settings = arg.getJSONObject( RTR_DEFAULT_IMAGE_SETTINGS_KEY );
			parseAspectRatioMin( settings );
			parseAspectRatioMax( settings );
			parseMinimumDocumentToViewRatio( settings );
			parseDocumentSize( settings );
			parseImageFromGalleryMaxSize( settings );
		}
	}

	private void parseMinimumDocumentToViewRatio( JSONObject settings ) throws JSONException
	{
		if( settings.has( RTR_DOCUMENT_TO_VIEW_RATIO_KEY ) ) {
			imageCaptureSettings.minimumDocumentToViewRatio = Float.parseFloat( settings.getString( RTR_DOCUMENT_TO_VIEW_RATIO_KEY ) );
		}
	}

	private void parseAspectRatioMin( JSONObject settings ) throws JSONException
	{
		if( settings.has( RTR_ASPECT_RATIO_MIN_KEY ) ) {
			imageCaptureSettings.aspectRatioMin = Float.parseFloat( settings.getString( RTR_ASPECT_RATIO_MIN_KEY ) );
		}
	}

	private void parseAspectRatioMax( JSONObject settings ) throws JSONException
	{
		if( settings.has( RTR_ASPECT_RATIO_MAX_KEY ) ) {
			imageCaptureSettings.aspectRatioMax = Float.parseFloat( settings.getString( RTR_ASPECT_RATIO_MAX_KEY ) );
		}
	}

	private void parseImageFromGalleryMaxSize( JSONObject settings ) throws JSONException
	{
		if( settings.has( RTR_IMAGE_FROM_GALLERY_MAX_SIZE_KEY ) ) {
			imageCaptureSettings.imageFromGalleryMaxSize = Integer.parseInt( settings.getString( RTR_IMAGE_FROM_GALLERY_MAX_SIZE_KEY ) );
		}
	}

	private void parseDocumentSize( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_DOCUMENT_SIZE_KEY ) ) {
			ImageCaptureScenario.DocumentSize size;
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
			imageCaptureSettings.documentSize = size;
		}
	}

	private void parseStopButtonVisibility( JSONObject arg ) throws JSONException
	{
		boolean stopButtonVisible = true;
		if( arg.has( RTR_IS_STOP_BUTTON_VISIBLE_KEY ) ) {
			stopButtonVisible = arg.getBoolean( RTR_IS_STOP_BUTTON_VISIBLE_KEY );
		}
		RtrManager.setStopButtonVisible( stopButtonVisible );
	}

	private void parseFlashVisibleImageCapture( JSONObject arg ) throws JSONException
	{
		boolean isFlashlightVisible = imageCaptureSettings.isFlashlightButtonVisible;
		if( arg.has( RTR_IS_FLASHLIGHT_VISIBLE_IMAGE_CAPTURE_KEY ) ) {
			isFlashlightVisible = arg.getBoolean( RTR_IS_FLASHLIGHT_VISIBLE_IMAGE_CAPTURE_KEY );
		}
		imageCaptureSettings.isFlashlightButtonVisible = isFlashlightVisible;
	}

	private void parseToggleFlash( JSONObject arg ) throws JSONException
	{
		boolean isFlashlightVisible = RtrManager.isFlashlightVisible();
		if( arg.has( RTR_IS_FLASHLIGHT_VISIBLE_KEY ) ) {
			isFlashlightVisible = arg.getBoolean( RTR_IS_FLASHLIGHT_VISIBLE_KEY );
		}
		RtrManager.setFlashlightVisible( isFlashlightVisible );
	}

	private void parseManualCaptureVisible( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_IS_MANUAL_CAPTURE_VISIBLE_KEY ) ) {
			imageCaptureSettings.isCaptureButtonVisible = arg.getBoolean( RTR_IS_MANUAL_CAPTURE_VISIBLE_KEY );
		}
	}

	private void parseGalleryButtonVisible( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_IS_GALLERY_BUTTON_VISIBLE_KEY ) ) {
			imageCaptureSettings.isGalleryButtonVisible = arg.getBoolean( RTR_IS_GALLERY_BUTTON_VISIBLE_KEY );
		}
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

	@NonNull
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
		imageCaptureSettings = new ImageCaptureSettings();
		parseCameraResolution( arg );
		parseFlashVisibleImageCapture( arg );
		parseGalleryButtonVisible( arg );
		parseManualCaptureVisible( arg );
		parseOrientation( arg );
		parseShowPreview( arg );
		parseImageCount( arg );
		parseDestination( arg );
		parseExportType( arg );
		parseCompressionLevel( arg );
		parseDefaultImageSettings( arg );
	}

	private void parseShowPreview( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_IS_SHOW_PREVIEW_KEY ) ) {
			imageCaptureSettings.isShowPreviewEnabled = arg.getBoolean( RTR_IS_SHOW_PREVIEW_KEY );
		}
	}

	private void parseImageCount( JSONObject arg ) throws JSONException
	{
		if( arg.has( RTR_IMAGE_COUNT_KEY ) ) {
			imageCaptureSettings.requiredPageCount = arg.getInt( RTR_IMAGE_COUNT_KEY );
		} else if( arg.has( RTR_REQUIRED_PAGE_COUNT_KEY ) ) {
			imageCaptureSettings.requiredPageCount = arg.getInt( RTR_REQUIRED_PAGE_COUNT_KEY );
		}
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
