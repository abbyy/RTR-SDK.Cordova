package com.abbyy.mobile.rtr.cordova.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.abbyy.mobile.rtr.IRecognitionService;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.settings.LanguagesSettingActivity;
import com.abbyy.mobile.rtr.cordova.exceptions.InitializationException;
import com.abbyy.mobile.rtr.cordova.surfaces.BaseSurfaceView;
import com.abbyy.mobile.rtr.cordova.surfaces.TextCaptureSurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TextCaptureActivity extends BaseActivity {
	private TextCaptureSurfaceView surfaceViewWithOverlay;

	protected ITextCaptureService captureService;
	private ITextCaptureService.TextLine[] currentLines;
	protected ITextCaptureService.Callback captureCallback = new ITextCaptureService.Callback() {

		@Override
		public void onRequestLatestFrame( byte[] buffer )
		{
			// The service asks to fill the buffer with image data for the latest frame in NV21 format.
			// Delegate this task to the camera. When the buffer is filled we will receive
			// Camera.PreviewCallback.onPreviewFrame (see below)
			camera.addCallbackBuffer( buffer );
		}

		@Override
		public void onFrameProcessed( final ITextCaptureService.TextLine[] lines,
			final ITextCaptureService.ResultStabilityStatus resultStatus, ITextCaptureService.Warning warning )
		{
			currentLines = lines;
			currentStabilityStatus = resultStatus;

			// Frame has been processed. Here we process recognition results. In this sample we
			// stop when we get stable result. This callback may continue being called for some time
			// even after the service has been stopped while the calls queued to this thread (UI thread)
			// are being processed. Just ignore these calls:
			if( !stableResultHasBeenReached ) {
				if( resultStatus.ordinal() >= 3 ) {
					// The result is stable enough to show something to the user
					surfaceViewWithOverlay.setLines( lines, resultStatus );
				} else {
					// The result is not stable. Show nothing
					surfaceViewWithOverlay.setLines( null, ITextCaptureService.ResultStabilityStatus.NotReady );
				}

				// Show the warning from the service if any. The warnings are intended for the user
				// to take some action (zooming in, checking recognition languages, etc.)
				warningTextView.setText( warning != null ? warning.name() : "" );

				if( resultStatus == ITextCaptureService.ResultStabilityStatus.Stable && RtrManager.stopWhenStable() ) {
					// Stable result has been reached. Stop the service
					stopRecognition();
					stableResultHasBeenReached = true;

					// Show result to the user. In this sample we whiten screen background and play
					// the same sound that is used for pressing buttons
					surfaceViewWithOverlay.setFillBackground( true );
					startButton.playSoundEffect( android.view.SoundEffectConstants.CLICK );
					dispatchResults( lines, resultStatus, false );
				}
			}
		}

		@Override
		public void onError( Exception e )
		{
			// An error occurred while processing. Log it. Processing will continue
			Log.e( getString( ResourcesUtils.getResId( "string", "app_name", TextCaptureActivity.this ) ), "Error: " + e.getMessage() );

			// Make the error easily visible to the developer
			String message = e.getMessage();
			errorTextView.setText( message );
			errorOccurred = e.getMessage();
		}

	};

	private void dispatchResults( final ITextCaptureService.TextLine[] lines, final IRecognitionService.ResultStabilityStatus resultStatus, final boolean wasStoppedByUser )
	{
		new Handler().postDelayed( new Runnable() {
			public void run()
			{
				Intent intent = new Intent();

				HashMap<String, Object> resultInfo = new HashMap<>();
				resultInfo.put( "stabilityStatus", resultStatus.toString() );
				resultInfo.put( "frameSize", String.format( Locale.US, "%d %d", cameraPreviewSize.width, cameraPreviewSize.height ) );

				String[] languageNames = new String[currentLanguages.length];

				for( int i = 0; i < currentLanguages.length; i++ ) {
					languageNames[i] = currentLanguages[i].name();
				}

				HashMap<String, String> map = RtrManager.getExtendedSettings();
				if( map == null ) {
					resultInfo.put( "recognitionLanguages", languageNames );
				} else {
					if( map.get( RtrPlugin.RTR_CUSTOM_RECOGNITION_LANGUAGES ) == null ) {
						resultInfo.put( "recognitionLanguages", languageNames );
					}
				}

				if( wasStoppedByUser ) {
					resultInfo.put( "userAction", "Manually Stopped" );
				}

				ArrayList<HashMap<String, String>> lineList = new ArrayList<>();
				if( lines != null ) {
					for( ITextCaptureService.TextLine line : lines ) {
						HashMap<String, String> lineInfo = new HashMap<>();
						lineInfo.put( "text", line.Text );
						StringBuilder builder = new StringBuilder();
						for( int i = 0; i < line.Quadrangle.length; i++ ) {
							builder.append( line.Quadrangle[i].x );
							builder.append( ' ' );
							builder.append( line.Quadrangle[i].y );
							if( i != line.Quadrangle.length - 1 ) {
								builder.append( ' ' );
							}
						}
						lineInfo.put( "quadrangle", builder.toString() );
						lineList.add( lineInfo );
					}
				}

				HashMap<String, Object> json = new HashMap<>();
				json.put( "resultInfo", resultInfo );
				json.put( "textLines", lineList );

				putErrorIfEssential( json );

				intent.putExtra( "result", json );
				TextCaptureActivity.this.setResult( RtrPlugin.RESULT_OK, intent );
				TextCaptureActivity.this.finish();
			}
		}, 0 );
	}

	private Language[] currentLanguages;

	@Override
	protected boolean createCaptureService()
	{
		try {
			captureService = RtrManager.createTextCaptureService( captureCallback );
			captureService.setRecognitionLanguage( currentLanguages );

			if( RtrManager.getExtendedSettings() != null ) {
				HashMap<String, String> map = RtrManager.getExtendedSettings();
				for( Map.Entry<String, String> entry : map.entrySet() ) {
					String key = entry.getKey();
					String value = entry.getValue();
					captureService.getExtendedSettings().setNamedProperty( key, value );
				}
			}
			return true;
		} catch( InitializationException e ) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void clearRecognitionResults()
	{
		stableResultHasBeenReached = false;
		surfaceViewWithOverlay.setLines( null, ITextCaptureService.ResultStabilityStatus.NotReady );
		surfaceViewWithOverlay.setFillBackground( false );
	}

	protected BaseSurfaceView getSurfaceViewWithOverlay()
	{
		return surfaceViewWithOverlay;
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
			dispatchResults( currentLines, currentStabilityStatus, true );
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

	public void onPreferenceButtonClick( View view )
	{
		// Clear error text, e.g. for errors depending on current selected languages.
		errorTextView.setText( "" );

		Intent intent = LanguagesSettingActivity.newIntent( this );
		ActivityOptions options =
			ActivityOptions.makeCustomAnimation( this, ResourcesUtils.getResId( "anim", "from_left_to_right", this ), 0 );
		startActivityForResult( intent, 0, options.toBundle() ); //don't need any codes
	}

	@Override
	protected void checkPreferences()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );

		List<Language> tempList = new ArrayList<>();
		for( Language language : RtrManager.getLanguages() ) {
			if( sharedPreferences.getBoolean( language.name(), false ) ) {
				tempList.add( language );
			}
		}
		if( tempList.isEmpty() ) {
			tempList.add( RtrManager.getLanguages().get( 0 ) );
			sharedPreferences.edit().putBoolean( RtrManager.getLanguages().get( 0 ).name(), true ).apply();
		}
		currentLanguages = tempList.toArray( new Language[tempList.size()] );

		if( captureService != null ) {
			captureService.setRecognitionLanguage( currentLanguages );
		}
	}

	@Override
	protected void setPreferenceButtonText()
	{
		StringBuilder sb = new StringBuilder();
		if( currentLanguages.length == 1 ) {
			sb.append( currentLanguages[0].name() );
		} else {
			for( Language currentLanguage : currentLanguages ) {
				sb.append( currentLanguage.name().substring( 0, 2 ).toUpperCase() );
				sb.append( ", " );
			}
			sb.delete( sb.length() - 2, sb.length() );
		}
		preferenceButton.setText( sb.toString() );
	}

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState )
	{
		surfaceViewWithOverlay = new TextCaptureSurfaceView( this );

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
		for( Language language : RtrManager.getSelectedLanguages() ) {
			sharedPreferences.edit().putBoolean( language.name(), true ).apply();
		}

		super.onCreate( savedInstanceState );
	}

	public static Intent newTextCaptureIntent( Context context )
	{
		return new Intent( context, TextCaptureActivity.class );
	}

}
