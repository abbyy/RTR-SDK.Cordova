package com.abbyy.mobile.rtr.cordova;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.activities.DataCaptureActivity;
import com.abbyy.mobile.rtr.cordova.activities.TextCaptureActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RtrPlugin extends CordovaPlugin {

	public static final int RESULT_OK = 0;
	public static final int RESULT_FAIL = 2;

	private static final int REQUEST_CODE_TEXT_CAPTURE = 1;
	private static final int REQUEST_CODE_DATA_CAPTURE = 2;

	private static final int REQUEST_CODE_PERMISSIONS_TEXT_CAPTURE = 401;
	private static final int REQUEST_CODE_PERMISSIONS_DATA_CAPTURE = 402;

	private static final String REQUIRED_PERMISSION = Manifest.permission.CAMERA;

	//region constants for parsing
	private static final String RTR_RECOGNITION_LANGUAGES_KEY = "recognitionLanguages";
	private static final String RTR_SELECTABLE_RECOGNITION_LANGUAGES_KEY = "selectableRecognitionLanguages";

	private static final String RTR_LICENSE_FILE_NAME_KEY = "licenseFileName";

	private static final String RTR_AREA_OF_INTEREST_KEY = "areaOfInterest";
	private static final String RTR_IS_FLASHLIGHT_VISIBLE_KEY = "isFlashlightVisible";
	private static final String RTR_IS_STOP_BUTTON_VISIBLE_KEY = "isStopButtonVisible";

	private static final String RTR_CUSTOM_DATA_CAPTURE_SCENARIO_KEY = "customDataCaptureScenario";
	private static final String RTR_CUSTOM_DATA_CAPTURE_SCENARIO_NAME_KEY = "name";
	private static final String RTR_CUSTOM_DATA_CAPTURE_FIELDS_KEY = "fields";
	private static final String RTR_CUSTOM_DATA_CAPTURE_REG_EX_KEY = "regEx";
	private static final String RTR_CUSTOM_DATA_CAPTURE_DESCRIPTION_KEY = "description";

	private static final String RTR_DATA_CAPTURE_PROFILE_KEY = "profile";

	private static final String RTR_STOP_WHEN_STABLE_KEY = "stopWhenStable";

	private static final String INTENT_RESULT_KEY = "result";

	//endregion constants for parsing

	private CallbackContext callback = null;
	private JSONObject inputParameters;

	@Override
	public boolean execute( String action, JSONArray args, final CallbackContext callbackContext ) throws JSONException
	{
		if( "startTextCapture".equals( action ) ) {
			PreferenceManager.getDefaultSharedPreferences( cordova.getActivity().getApplicationContext() ).edit().clear().apply();
			if( init( callbackContext, args ) ) {
				try {
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
				} catch( IllegalArgumentException e ) {
					onError( e.getMessage() );
					return false;
				} catch( JSONException e ) {
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
			RtrManager.getInstance().initWithLicense();
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
		int[] grantResults ) throws JSONException
	{
		for( int result : grantResults ) {
			if( result == PackageManager.PERMISSION_DENIED ) {
				Toast.makeText( cordova.getActivity(), ResourcesUtils.getResId( "string", "runtime_permissions_txt", cordova.getActivity() ), Toast.LENGTH_SHORT ).show();
				onError( "Camera access denied" );
				return;
			}
		}

		switch( requestCode ) {
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

	private void parseAutoStop( JSONObject arg ) throws JSONException
	{
		boolean stopWhenStable = true;
		if( arg.has( RTR_STOP_WHEN_STABLE_KEY ) ) {
			stopWhenStable = arg.getBoolean( RTR_STOP_WHEN_STABLE_KEY );
		}
		RtrManager.setStopWhenStable( stopWhenStable );
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
		boolean isFlashlightVisible = true;
		if( arg.has( RTR_IS_FLASHLIGHT_VISIBLE_KEY ) ) {
			isFlashlightVisible = arg.getBoolean( RTR_IS_FLASHLIGHT_VISIBLE_KEY );
		}
		RtrManager.setFlashlightVisible( isFlashlightVisible );
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
		List<Language> languages = parseLanguagesInternal( arg, RTR_SELECTABLE_RECOGNITION_LANGUAGES_KEY );
		RtrManager.setLanguageSelectionEnabled( languages.size() > 0 );
		return languages;
	}

	private List<Language> parseSelectedLanguage( JSONObject arg ) throws JSONException
	{
		List<Language> languages = parseLanguagesInternal( arg, RTR_RECOGNITION_LANGUAGES_KEY );

		if( languages.size() == 0 ) {
			languages.add( Language.English );
		}

		return languages;
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

	private List<Language> parseLanguagesInternal( JSONObject arg, String key ) throws JSONException
	{
		List<Language> languages = new ArrayList<>();
		if( arg.has( key ) ) {
			JSONArray array = arg.getJSONArray( key );
			for( int i = 0; i < array.length(); i++ ) {
				languages.add( parseLanguageName( array.getString( i ) ) );
			}
		}

		return languages;
	}

	private void parseUiSettings( JSONObject arg ) throws JSONException
	{
		parseAutoStop( arg );
		parseStopButtonVisibility( arg );
		parseToggleFlash( arg );
		parseAreaOfInterest( arg );
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
		} else {
			throw new JSONException( "Invalid Data Capture scenario settings." );
		}

		RtrManager.setCustomDataCaptureScenario( customScenario );
		RtrManager.setDataCaptureProfile( profile );
	}

}