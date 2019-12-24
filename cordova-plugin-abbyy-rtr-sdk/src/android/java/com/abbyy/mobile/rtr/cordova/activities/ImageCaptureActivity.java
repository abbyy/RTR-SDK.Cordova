// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.multipage.MultiCaptureResult;
import com.abbyy.mobile.rtr.cordova.utils.BackgroundWorker;
import com.abbyy.mobile.rtr.cordova.utils.MultiPagesProcessor;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.MultiPageImageCaptureScenario;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import static com.abbyy.mobile.rtr.cordova.RtrPlugin.INTENT_RESULT_KEY;

/**
 * This activity uses multipage UI component to capture pages, shows a dialog to confirm or discard
 * the result
 */
public class ImageCaptureActivity extends AppCompatActivity implements MultiPageImageCaptureScenario.Callback {

	// Capture view component
	private CaptureView captureView;
	private volatile MultiPageImageCaptureScenario imageCaptureScenario;

	// This dialog is shown when user wants to leave without saving
	private AlertDialog discardPagesDialog;

	private BackgroundWorker.Callback<Void, Void> clearPagesCallback = new BackgroundWorker.Callback<Void, Void>() {
		@Override
		public Void doWork( Void aVoid, BackgroundWorker<Void, Void> worker ) throws Exception
		{
			imageCaptureScenario.getResult().clear();
			return null;
		}

		@Override
		public void onDone( Void result, Exception exception, BackgroundWorker<Void, Void> worker )
		{
			if( exception != null ) {
				exception.printStackTrace();
				finishWithEmptyResult( exception );
			} else {
				captureView.setCaptureScenario( imageCaptureScenario );
			}
		}
	};

	private BackgroundWorker.Callback<Boolean, Boolean> multiPagesProcessorCallback = new BackgroundWorker.Callback<Boolean, Boolean>() {
		@Override
		public Boolean doWork( Boolean finishedSuccessfully, BackgroundWorker<Boolean, Boolean> worker )
		{
			return ( (MultiPagesProcessor) worker ).processPages( finishedSuccessfully );
		}

		@Override
		public void onDone( Boolean shouldConfirmFinish, Exception exception, BackgroundWorker<Boolean, Boolean> worker )
		{
			if( exception != null ) {
				exception.printStackTrace();
				finishWithEmptyResult( exception );
			} else {
				if( shouldConfirmFinish ) {
					showDiscardPagesDialog( ( (MultiPagesProcessor) worker ).getResult() );
				} else {
					finishCaptureResult();
				}
			}
		}
	};

	public static Intent newImageCaptureIntent( Context context )
	{
		return new Intent( context, ImageCaptureActivity.class );
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		if( RtrManager.getOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ) {
			setRequestedOrientation( RtrManager.getOrientation() );
		}

		requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN );

		setContentView( ResourcesUtils.getResId( "layout", "activity_image_capture", this ) );

		try {
			initCaptureView();
		} catch( Exception e ) {
			e.printStackTrace();
			finishWithEmptyResult( e );
		}
	}

	private void initCaptureView() throws Exception
	{
		captureView = findViewById( ResourcesUtils.getResId( "id", "captureView", this ) );
		captureView.getUISettings().setCaptureButtonVisible( ImageCaptureSettings.manualCaptureVisible );
		captureView.getUISettings().setFlashlightButtonVisible( ImageCaptureSettings.flashlightVisible );
		captureView.getCameraSettings().setResolution( ImageCaptureSettings.cameraResolution );

		startCapture();
	}

	@Override
	public Object onRetainCustomNonConfigurationInstance()
	{
		// Scenario retaining is necessary to save user actions during Activity recreation
		return imageCaptureScenario;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		captureView.startCamera();
	}

	@Override
	protected void onPause()
	{
		captureView.stopCamera();
		if( discardPagesDialog != null ) {
			discardPagesDialog.dismiss();
		}
		super.onPause();
	}

	private void startCapture() throws Exception
	{
		MultiPageImageCaptureScenario scenario = (MultiPageImageCaptureScenario) getLastCustomNonConfigurationInstance();
		if( scenario != null ) {
			imageCaptureScenario = scenario;
			imageCaptureScenario.setCallback( this );
			captureView.setCaptureScenario( imageCaptureScenario );
		} else {
			imageCaptureScenario = RtrManager.getImageCaptureScenario( this );
			imageCaptureScenario.setCallback( this );
			// Clearing pages in background
			new BackgroundWorker<>( new WeakReference<>( clearPagesCallback ) ).execute();
		}
	}

	private void finishWithEmptyResult( Exception error )
	{
		Intent intent = new Intent();

		HashMap<String, Object> json = MultiCaptureResult.getErrorJsonResult( error, this );
		intent.putExtra( "result", json );
		setResult( RtrPlugin.RESULT_FAIL, intent );
		RtrManager.setImageCaptureResult( null );
		finish();
	}

	private void finishCapture( MultiPageImageCaptureScenario.Result result, boolean successfulFinish )
	{
		processMultiPages( result, successfulFinish );
	}

	private void processMultiPages( MultiPageImageCaptureScenario.Result result, boolean successfulFinish )
	{
		new MultiPagesProcessor( result, this, new WeakReference<>( multiPagesProcessorCallback ) )
			.execute( successfulFinish );
	}

	private void showDiscardPagesDialog( final MultiPageImageCaptureScenario.Result result )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( ResourcesUtils.getResId( "string", "captured_pages_delete_warning", this ) )
			.setTitle( ResourcesUtils.getResId( "string", "discard_pages", this ) )
			.setPositiveButton( ResourcesUtils.getResId( "string", "discard", this ), new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which )
				{
					AsyncTask.execute( new Runnable() {
						@Override
						public void run()
						{
							try {
								result.clear();
							} catch( Exception e ) {
								e.printStackTrace();
							}
						}
					} );
					finishWithEmptyResult( null );
				}
			} )
			.setNegativeButton( ResourcesUtils.getResId( "string", "cancel", this ), null )
			.setOnDismissListener( new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss( DialogInterface dialog )
				{
					imageCaptureScenario.start();
				}
			} );
		discardPagesDialog = builder.show();
	}

	private void finishCaptureResult()
	{
		Intent intent = new Intent();

		HashMap<String, Object> json = new HashMap<>();

		intent.putExtra( INTENT_RESULT_KEY, json );
		setResult( RtrPlugin.RESULT_OK, intent );
		finish();
	}

	@Override
	public void onClose( @NonNull MultiPageImageCaptureScenario.Result result )
	{
		finishCapture( result, false );
	}

	// Error handler for UI Component
	@Override
	public void onError( @NonNull Exception e, @NonNull MultiPageImageCaptureScenario.Result result )
	{
		e.printStackTrace();
		finishWithEmptyResult( e );
	}

	@Override
	public void onFinished( @NonNull MultiPageImageCaptureScenario.Result result )
	{
		finishCapture( result, true );
	}
}