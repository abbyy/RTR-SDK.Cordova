// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.SharedEngine;
import com.abbyy.mobile.rtr.cordova.image.ImageCaptureResult;
import com.abbyy.mobile.rtr.cordova.image.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.image.MultiPagesProcessor;
import com.abbyy.mobile.rtr.cordova.utils.BackgroundWorker;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.MultiPageImageCaptureScenario;
import com.abbyy.mobile.uicomponents.scenario.MultiPageImageCaptureScenario.Result;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * This activity uses multipage UI component to capture pages, shows a dialog to confirm or discard
 * the result
 */
public class ImageCaptureActivity extends AppCompatActivity implements MultiPageImageCaptureScenario.Callback {

	public static final String ERROR_DESCRIPTION_RESULT_KEY = "error_description_result_key";
	public static final String IMAGE_CAPTURE_RESULT_KEY = "image_capture_result_key";

	private static final String SETTINGS_ARGUMENT_KEY = "settings_argument_key";

	// Capture view component
	private CaptureView captureView;
	private MultiPageImageCaptureScenario imageCaptureScenario;

	// This dialog is shown when user wants to leave without saving
	private Dialog discardPagesDialog;
	private Dialog pagesProcessingDialog;

	private ImageCaptureSettings imageCaptureSettings;

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
				exitWithException( exception );
			} else {
				captureView.setCaptureScenario( imageCaptureScenario );
			}
		}
	};

	private BackgroundWorker.Callback<Void, ImageCaptureResult> multiPagesProcessorCallback = new BackgroundWorker.Callback<Void, ImageCaptureResult>() {
		@Override
		public ImageCaptureResult doWork( Void aVoid, BackgroundWorker<Void, ImageCaptureResult> worker ) throws Exception
		{
			return ( (MultiPagesProcessor) worker ).processPages();
		}

		@Override
		public void onDone(
			@Nullable ImageCaptureResult imageCaptureResult,
			@Nullable Exception exception,
			@NonNull BackgroundWorker<Void, ImageCaptureResult> worker
		)
		{
			if( exception != null ) {
				exitWithException( exception );
			} else {
				exitWithSuccess( Objects.requireNonNull( imageCaptureResult ) );
			}
		}
	};

	private BackgroundWorker.Callback<Result, Pair<Result, Boolean>> closeCallback = new BackgroundWorker.Callback<Result, Pair<Result, Boolean>>() {
		@Override
		public Pair<Result, Boolean> doWork( Result result, BackgroundWorker<Result, Pair<Result, Boolean>> worker ) throws Exception
		{
			return new Pair<>( result, result.getPages().isEmpty() );
		}

		@Override
		public void onDone( Pair<Result, Boolean> result, Exception exception, BackgroundWorker<Result, Pair<Result, Boolean>> worker )
		{
			if( result.second /* isPagesEmpty */ ) {
				exitWithCancelledEvent();
			} else {
				showDiscardPagesDialog( result.first );
			}
		}
	};

	public static Intent newImageCaptureIntent(
		@NonNull Context context,
		@NonNull ImageCaptureSettings settings
	)
	{
		Intent intent = new Intent( context, ImageCaptureActivity.class );
		intent.putExtra( SETTINGS_ARGUMENT_KEY, settings );
		return intent;
	}

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState )
	{
		imageCaptureSettings = getIntent().getParcelableExtra( SETTINGS_ARGUMENT_KEY );
		if( imageCaptureSettings.orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ) {
			setRequestedOrientation( imageCaptureSettings.orientation );
		}

		super.onCreate( savedInstanceState );

		requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

		setContentView( ResourcesUtils.getResId( "layout", "activity_image_capture", this ) );

		try {
			initCaptureView();
		} catch( Exception exception ) {
			exception.printStackTrace();
			exitWithException( exception );
		}
	}

	private void initCaptureView() throws Exception
	{
		captureView = findViewById( ResourcesUtils.getResId( "id", "captureView", this ) );
		captureView.getUISettings().setCaptureButtonVisible( imageCaptureSettings.isCaptureButtonVisible );
		captureView.getUISettings().setFlashlightButtonVisible( imageCaptureSettings.isFlashlightButtonVisible );
		captureView.getUISettings().setGalleryButtonVisible( imageCaptureSettings.isGalleryButtonVisible );
		captureView.getCameraSettings().setResolution( imageCaptureSettings.cameraResolution );

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
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		hideDiscardPagesDialog();
		hidePagesProcessingDialog();
	}

	private void hideDiscardPagesDialog()
	{
		if( discardPagesDialog != null ) {
			discardPagesDialog.dismiss();
			discardPagesDialog = null;
		}
	}

	private void startCapture() throws Exception
	{
		MultiPageImageCaptureScenario scenario = (MultiPageImageCaptureScenario) getLastCustomNonConfigurationInstance();
		if( scenario != null ) {
			imageCaptureScenario = scenario;
			imageCaptureScenario.setCallback( this );
			captureView.setCaptureScenario( imageCaptureScenario );
		} else {
			imageCaptureScenario = createImageCaptureScenario();
			imageCaptureScenario.setCallback( this );
			// Clearing pages in background
			new BackgroundWorker<>( new WeakReference<>( clearPagesCallback ) ).execute();
		}
	}

	private MultiPageImageCaptureScenario createImageCaptureScenario() throws Exception
	{
		Engine engine = SharedEngine.get();
		MultiPageImageCaptureScenario.Builder builder = new MultiPageImageCaptureScenario.Builder( engine, this );
		builder.setShowPreviewEnabled( imageCaptureSettings.isShowPreviewEnabled );
		builder.setRequiredPageCount( imageCaptureSettings.requiredPageCount );
		builder.setCaptureSettings( new MultiPageImageCaptureScenario.CaptureSettings() {
			@Override
			public void onConfigureImageCaptureSettings( @NonNull com.abbyy.mobile.uicomponents.scenario.ImageCaptureSettings settings, int index )
			{
				ImageCaptureActivity.this.onConfigureImageCaptureSettings( settings );
			}
		} );
		return builder.build();
	}

	private void onConfigureImageCaptureSettings( @NonNull com.abbyy.mobile.uicomponents.scenario.ImageCaptureSettings settings )
	{
		try {
			settings.setDocumentSize( imageCaptureSettings.documentSize );
			settings.setMinimumDocumentToViewRatio( imageCaptureSettings.minimumDocumentToViewRatio );
			settings.setAspectRatioMin( imageCaptureSettings.aspectRatioMin );
			settings.setAspectRatioMax( imageCaptureSettings.aspectRatioMax );
			settings.setImageFromGalleryMaxSize( imageCaptureSettings.imageFromGalleryMaxSize );
		} catch( Exception exception ) {
			exitWithException( exception );
		}
	}

	private void exitWithException( @NonNull Exception exception )
	{
		Intent intent = new Intent();
		intent.putExtra(
			ERROR_DESCRIPTION_RESULT_KEY,
			"Capture error: " + exception.getClass().getSimpleName() + " " + exception.getMessage()
		);
		setResult( Activity.RESULT_CANCELED, intent );
		finish();
	}

	private void exitWithCancelledEvent()
	{
		setResult( Activity.RESULT_CANCELED, null );
		finish();
	}

	private void exitWithSuccess( @NonNull ImageCaptureResult imageCaptureResult )
	{
		Intent data = new Intent();
		data.putExtra( IMAGE_CAPTURE_RESULT_KEY, imageCaptureResult );
		setResult( RtrPlugin.RESULT_OK, data );
		finish();
	}

	private void showDiscardPagesDialog( final Result result )
	{
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( ResourcesUtils.getResId( "string", "ic_delete_on_cancel_warning_message", this ) )
			.setTitle( ResourcesUtils.getResId( "string", "ic_delete_on_cancel_warning_title", this ) )
			.setPositiveButton( ResourcesUtils.getResId( "string", "ic_delete_on_cancel_warning_positive_button", this ),
			new DialogInterface.OnClickListener() {
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
					ImageCaptureActivity.this.exitWithCancelledEvent();
				}
			} )
			.setNegativeButton( ResourcesUtils.getResId( "string", "ic_delete_on_cancel_warning_negative_button", this ), null )
			.setOnDismissListener( new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss( DialogInterface dialog )
				{
					imageCaptureScenario.start();
				}
			} );

		discardPagesDialog = builder.show();
	}

	@Override
	public void onClose( @NonNull Result result )
	{
		new BackgroundWorker<>( new WeakReference<>( closeCallback ) ).execute( result );
	}

	// Error handler for UI Component
	@Override
	public void onError( @NonNull Exception exception, @NonNull Result result )
	{
		exception.printStackTrace();
		exitWithException( exception );
	}

	@Override
	public void onFinished( @NonNull Result result )
	{
		showPagesProcessingDialog();
		new MultiPagesProcessor(
			result,
			getApplication(),
			new WeakReference<>( multiPagesProcessorCallback ),
			imageCaptureSettings
		).execute();
	}

	private void showPagesProcessingDialog()
	{
		@SuppressLint( "InflateParams" )
		View dialogView = getLayoutInflater().inflate( ResourcesUtils.getResId( "layout", "ic_dialog_progress", this ), null );

		pagesProcessingDialog = new AlertDialog
			.Builder( this )
			.setCancelable( false )
			.setTitle( ResourcesUtils.getResId( "string", "ic_saving_pages_warning_message", this ) )
			.setView( dialogView )
			.create();
		pagesProcessingDialog.show();
	}

	private void hidePagesProcessingDialog()
	{
		if( pagesProcessingDialog != null ) {
			pagesProcessingDialog.dismiss();
			pagesProcessingDialog = null;
		}
	}
}
