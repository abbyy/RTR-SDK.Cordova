package com.abbyy.mobile.rtr.cordova.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.abbyy.mobile.rtr.cordova.exceptions.InitializationException;
import com.abbyy.mobile.rtr.cordova.surfaces.BaseSurfaceView;
import com.abbyy.mobile.rtr.cordova.surfaces.DataCaptureSurfaceView;

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
			Log.e( getString( ResourcesUtils.getResId( "string", "app_name", context ) ), "Error: " + e.getMessage() );
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
					for( IDataCaptureService.DataField field : fields ) {
						HashMap<String, Object> fieldInfo = new HashMap<>();
						fieldInfo.put( "id", field.Id != null ? field.Id : "" );
						fieldInfo.put( "name", field.Name != null ? field.Name : "" );

						fieldInfo.put( "text", field.Text );
						if( field.Quadrangle != null ) {
							StringBuilder builder = new StringBuilder();
							for( int i = 0; i < field.Quadrangle.length; i++ ) {
								builder.append( field.Quadrangle[i].x );
								builder.append( ' ' );
								builder.append( field.Quadrangle[i].y );
								if( i != field.Quadrangle.length - 1 ) {
									builder.append( ' ' );
								}
							}
							fieldInfo.put( "quadrangle", builder.toString() );
						}

						ArrayList<HashMap<String, String>> lineList = new ArrayList<>();
						for( IDataCaptureService.DataField line : field.Components ) {
							HashMap<String, String> lineInfo = new HashMap<>();
							lineInfo.put( "text", line.Text );
							if( line.Quadrangle != null ) {
								StringBuilder lineBuilder = new StringBuilder();
								for( int i = 0; i < line.Quadrangle.length; i++ ) {
									lineBuilder.append( line.Quadrangle[i].x );
									lineBuilder.append( ' ' );
									lineBuilder.append( line.Quadrangle[i].y );
									if( i != line.Quadrangle.length - 1 ) {
										lineBuilder.append( ' ' );
									}
								}
								lineInfo.put( "quadrangle", lineBuilder.toString() );
							}
							lineList.add( lineInfo );
						}
						fieldInfo.put( "components", lineList );

						fieldList.add( fieldInfo );
					}
				}

				HashMap<String, String> dataSchemeInfo = new HashMap<>();
				dataSchemeInfo.put( "id", scheme != null ? scheme.Id : "" );
				dataSchemeInfo.put( "name", scheme != null ? scheme.Name : "" );

				HashMap<String, Object> json = new HashMap<>();
				json.put( "resultInfo", resultInfo );
				json.put( "dataFields", fieldList );
				json.put( "dataScheme", dataSchemeInfo );

				putErrorIfEssential( json );

				intent.putExtra( "result", json );
				( (Activity) context ).setResult( RtrPlugin.RESULT_OK, intent );
				( (Activity) context ).finish();
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
			} catch( InitializationException e ) {
				onStartupError( e );
			} catch( IllegalArgumentException e ) {
				onStartupError( e );
			}
		} else {

			// Custom data capture scenarios
			captureService = null;
			try {
				captureService = RtrManager.createDataCaptureService( null, captureCallback );
				IDataCaptureProfileBuilder profileBuilder = captureService.configureDataCaptureProfile();
				IDataCaptureProfileBuilder.IFieldBuilder field = profileBuilder.addScheme( currentScenario.name ).addField( currentScenario.name );

				Language[] languages = new Language[currentScenario.languages.size()];
				for( int i = 0; i < languages.length; i++ ) {
					languages[i] = currentScenario.languages.get( i );
				}
				profileBuilder.setRecognitionLanguage( languages );
				field.setRegEx( currentScenario.regEx );

				profileBuilder.checkAndApply();
			} catch( InitializationException e ) {
				onStartupError( e );
			} catch( IDataCaptureProfileBuilder.ProfileCheckException e ) {
				onStartupError( e );
			} catch( IllegalArgumentException e ) {
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

	@Override
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
		surfaceView = new DataCaptureSurfaceView( context );
		super.onCreate( savedInstanceState );
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );
		createCaptureService();
	}

	public static Intent newDataCaptureIntent( Context context )
	{
		return new Intent( context, DataCaptureActivity.class );
	}

}
