// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.abbyy.mobile.rtr.IDataCaptureProfileBuilder;
import com.abbyy.mobile.rtr.IDataCaptureService;
import com.abbyy.mobile.rtr.IRecognitionService;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.DataCaptureScenario;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.surfaces.BaseSurfaceView;
import com.abbyy.mobile.rtr.cordova.surfaces.DataCaptureSurfaceView;
import com.abbyy.mobile.rtr.cordova.utils.DataUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DataCaptureActivity extends BaseActivity {
	private DataCaptureScenario currentScenario;

	private DataCaptureSurfaceView surfaceView;

	protected IDataCaptureService captureService;

	private IDataCaptureService.DataField[] currentFields;
	private IDataCaptureService.DataScheme currentScheme;
	protected IDataCaptureService.Callback captureCallback = new IDataCaptureService.Callback() {

		@Override
		public void onRequestLatestFrame( byte[] buffer )
		{
			// The service asks to fill the buffer with image data for a latest frame in NV21 format.
			// Delegate this task to the camera. When the buffer is filled we will receive
			// Camera.PreviewCallback.onPreviewFrame (see below)
			camera.addCallbackBuffer( buffer );
		}

		@Override
		public void onFrameProcessed( IDataCaptureService.DataScheme scheme, final IDataCaptureService.DataField[] fields,
			final IDataCaptureService.ResultStabilityStatus resultStatus, IDataCaptureService.Warning warning )
		{
			currentScheme = scheme;
			currentFields = fields;
			currentStabilityStatus = resultStatus;
			// Frame has been processed. Here we process recognition results. In this sample we
			// stop when we get stable result. This callback may continue being called for some time
			// even after the service has been stopped while the calls queued to this thread (UI thread)
			// are being processed. Just ignore these calls:
			if( !stableResultHasBeenReached ) {
				if( resultStatus.ordinal() >= 3 ) {
					// The result is stable enough to show something to the user
					surfaceView.setLines( fields, resultStatus );
				} else {
					// The result is not stable. Show nothing
					surfaceView.setLines( null, IDataCaptureService.ResultStabilityStatus.NotReady );
				}

				// Show the warning from the service if any. These warnings are intended for the user
				// to take some action (zooming in, checking recognition languages, etc.)
				warningTextView.setText( warning != null ? warning.name() : "" );

				if( scheme != null && resultStatus == IDataCaptureService.ResultStabilityStatus.Stable && RtrManager.stopWhenStable() ) {
					// Stable result has been reached. Stop the service
					stopRecognition();
					stableResultHasBeenReached = true;

					// Show result to the user. In this sample we whiten screen background and play
					// the same sound that is used for pressing buttons
					surfaceView.setFillBackground( true );
					startButton.playSoundEffect( android.view.SoundEffectConstants.CLICK );

					dispatchResults( scheme, fields, resultStatus, false );
				}
			}
		}

		@Override
		public void onError( Exception e )
		{
			// An error occurred while processing. Log it. Processing will continue
			Log.e( getString( ResourcesUtils.getResId( "string", "app_name", DataCaptureActivity.this ) ), "Error: " + e.getMessage() );
			//Make the error easily visible to the developer
			String message = e.getMessage();
			errorTextView.setText( message );
			errorOccurred = e.getMessage();
		}
	};

	private void dispatchResults( final IDataCaptureService.DataScheme scheme, final IDataCaptureService.DataField[] fields,
		final IRecognitionService.ResultStabilityStatus resultStatus, final boolean wasStoppedByUser )
	{
		new Handler().postDelayed( new Runnable() {
			public void run()
			{
				Intent intent = new Intent();

				HashMap<String, String> resultInfo = new HashMap<>();
				resultInfo.put( "stabilityStatus", resultStatus.toString() );
				resultInfo.put( "frameSize", String.format( Locale.US, "%d %d", cameraPreviewSize.width, cameraPreviewSize.height ) );

				if( wasStoppedByUser ) {
					resultInfo.put( "userAction", "Manually Stopped" );
				}

				ArrayList<HashMap<String, Object>> fieldList = new ArrayList<>();
				if( fields != null ) {
					fieldList = DataUtils.toJsonDataFields( fields );
				}

				HashMap<String, String> dataSchemeInfo = new HashMap<>();
				dataSchemeInfo.put( "id", scheme != null ? scheme.Id : "" );
				dataSchemeInfo.put( "name", scheme != null ? scheme.Name : "" );

				HashMap<String, Object> json = new HashMap<>();
				json.put( "resultInfo", resultInfo );
				json.put( "dataFields", fieldList );
				json.put( "dataScheme", dataSchemeInfo );

				putErrorIfEssential( json );

				intent.putExtra( RtrPlugin.INTENT_RESULT_KEY, json );
				DataCaptureActivity.this.setResult( RtrPlugin.RESULT_OK, intent );
				DataCaptureActivity.this.finish();
			}
		}, 0 );
	}

	@Override
	protected boolean createCaptureService()
	{
		captureService = createConfigureDataCaptureService();
		return captureService != null;
	}

	@Override
	protected boolean needMaximumAvailableResolution()
	{
		// Most of data capture scenarios requires maximum available resolution
		// Exceptions are IBAN and custom data capture
		String profile = RtrManager.getDataCaptureProfile();
		if( profile != null ) {
			if( profile.equals( "IBAN" ) ) {
				return false;
			}
			return true;
		}
		// Custom DC
		return false;
	}

	// Create and configure data capture service
	private IDataCaptureService createConfigureDataCaptureService()
	{
		String profile = RtrManager.getDataCaptureProfile();
		if( profile != null ) {
			try {
				captureService = RtrManager.createDataCaptureService( profile, captureCallback );
				if( !RtrManager.getLanguages().isEmpty() ) {
					try {
						Language[] languages = new Language[RtrManager.getLanguages().size()];
						languages = RtrManager.getLanguages().toArray( languages );
						IDataCaptureProfileBuilder profileBuilder = captureService.configureDataCaptureProfile()
							.setRecognitionLanguage( languages );
						profileBuilder.checkAndApply();
					} catch( IDataCaptureProfileBuilder.ProfileCheckException e ) {
						onProfileError();
					}
				}
			} catch( Exception e ) {
				onStartupError( e );
			}
		} else {
			// Custom data capture scenarios
			captureService = null;
			try {
				captureService = RtrManager.createDataCaptureService( null, captureCallback );
				Language[] languages = new Language[currentScenario.languages.size()];
				for( int i = 0; i < languages.length; i++ ) {
					languages[i] = currentScenario.languages.get( i );
				}
				IDataCaptureProfileBuilder profileBuilder = captureService.configureDataCaptureProfile()
					.setRecognitionLanguage( languages );
				IDataCaptureProfileBuilder.IFieldBuilder field = profileBuilder.addScheme( currentScenario.name ).addField( currentScenario.name );

				field.setRegEx( currentScenario.regEx );

				profileBuilder.checkAndApply();
			} catch( Exception e ) {
				onStartupError( e );
			}
		}

		return captureService;
	}

	@Override
	protected void clearRecognitionResults()
	{
		stableResultHasBeenReached = false;
		surfaceView.setLines( null, IDataCaptureService.ResultStabilityStatus.NotReady );
		surfaceView.setFillBackground( false );
	}

	@Override
	protected BaseSurfaceView getSurfaceViewWithOverlay()
	{
		return surfaceView;
	}

	@Override
	protected IRecognitionService getCaptureService()
	{
		return captureService;
	}

	public void onStartButtonClick( View view )
	{
		if( startButton.getText().equals( BUTTON_TEXT_STOP ) ) {
			stopRecognition();
			dispatchResults( currentScheme, currentFields, currentStabilityStatus, true );
		} else {
			errorOccurred = null;
			clearRecognitionResults();
			startButton.setEnabled( false );
			startButton.setText( BUTTON_TEXT_STARTING );
			if( !isContinuousVideoFocusModeEnabled( camera ) ) {
				autoFocus( startRecognitionCameraAutoFocusCallback );
			} else {
				startRecognition();
			}
		}
	}

	@Override
	protected void checkPreferences()
	{
		currentScenario = RtrManager.getCustomDataCaptureScenario();
	}

	@Override
	protected void setPreferenceButtonText()
	{
		preferenceButton.setClickable( false );
		String displayName = RtrManager.getDataCaptureProfile();
		if( currentScenario != null ) {
			displayName = currentScenario.name;
			scenarioDescription.setText( currentScenario.usageDescription );
		}

		preferenceButton.setText( displayName );
	}

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState )
	{
		surfaceView = new DataCaptureSurfaceView( this );
		super.onCreate( savedInstanceState );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );
		createCaptureService();
	}

	private void onProfileError()
	{
		Log.e( getString( ResourcesUtils.getResId( "string", "app_name", this ) ), "Cannot set recognition languages" );
		Intent intent = new Intent();

		HashMap<String, Object> json = new HashMap<>();
		HashMap<String, String> errorInfo = new HashMap<>();
		errorInfo.put( "description", "Language customization is not available for this profile" );

		json.put( "error", errorInfo );

		intent.putExtra( RtrPlugin.INTENT_RESULT_KEY, json );
		setResult( RtrPlugin.RESULT_FAIL, intent );
		finish();
	}

	public static Intent newDataCaptureIntent( Context context )
	{
		return new Intent( context, DataCaptureActivity.class );
	}

}
