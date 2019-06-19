// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.fragments.CaptureResultDialogFragment;
import com.abbyy.mobile.rtr.cordova.fragments.MultiPageCounter;
import com.abbyy.mobile.rtr.cordova.utils.ImageLoader;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;
import com.abbyy.rtrcordovasample.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This activity uses UI component to capture a page, shows a dialog to confirm or discard
 * the result, and keeps count
 */
public class ImageCaptureActivity extends AppCompatActivity implements ImageCaptureScenario.Callback,
	CaptureResultDialogFragment.ResultListener {

	public static final String RESULT_SHOWN_EXTRA = "result_shown";

	//Finish capture view and its children
	private MultiPageCounter multiPageCounter;

	// Capture view component
	private CaptureView captureView;
	private ImageCaptureScenario imageCaptureScenario;

	// This dialog is shown when user wants to leave without saving
	private AlertDialog discardPagesDialog;

	// Whether the result dialog is currently shown
	private boolean resultDialogShown = false;

	private Bitmap lastPageMiniature = null;
	private ImageLoader lastPageMiniatureLoader;

	public static Intent newImageCaptureIntent( Context context )
	{
		return new Intent( context, ImageCaptureActivity.class );
	}

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		if( savedInstanceState != null ) {
			resultDialogShown = savedInstanceState.getBoolean( RESULT_SHOWN_EXTRA, false );
		}

		setContentView( R.layout.activity_image_capture );

		initCaptureView();

		// Initialize page counter view
		initPageCounterView();

		ImageButton cancelCapture = (ImageButton) findViewById( R.id.cancelCapture );
		cancelCapture.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				finishWithWarning();
			}
		} );
	}

	@Override protected void onDestroy()
	{
		if( lastPageMiniatureLoader != null ) {
			lastPageMiniatureLoader.cancel( true );
		}
		super.onDestroy();
	}

	// Supporting correct behavior on activity re-creation
	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		outState.putBoolean( RESULT_SHOWN_EXTRA, resultDialogShown );
		super.onSaveInstanceState( outState );
	}

	private void initPageCounterView()
	{
		multiPageCounter = (MultiPageCounter) findViewById( R.id.finishCapture );
		multiPageCounter.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				finishCapture();
			}
		} );

		final int captureSessionPageCount = ImageUtils.getCaptureSessionPageCount( this );
		if( captureSessionPageCount > 0 && lastPageMiniature == null ) {
			loadLastPageMiniature( captureSessionPageCount );
		} else {
			multiPageCounter.updatePageCount( captureSessionPageCount, lastPageMiniature );
		}
	}

	private void initCaptureView()
	{
		captureView = (CaptureView) findViewById( R.id.captureView );
		imageCaptureScenario = RtrManager.getImageCaptureScenario();
		imageCaptureScenario.setCallback( this );

		captureView.setCaptureScenario( imageCaptureScenario );

		startCapture();
	}

	private void startCapture()
	{
		if( !resultDialogShown ) {
			imageCaptureScenario.start();
		}
	}

	private void stopCapture()
	{
		if( !resultDialogShown ) {
			imageCaptureScenario.stop();
		}
	}

	private void loadLastPageMiniature( final int captureSessionPageCount )
	{
		String lastPagePath = ImageUtils.getCaptureSessionPageFile( captureSessionPageCount - 1, this ).getPath();
		lastPageMiniatureLoader = new ImageLoader( lastPagePath, new ImageLoader.Callback() {
			@Override
			public Bitmap loadBitmap( @NonNull File file ) throws IOException
			{
				return ImageUtils.loadThumbnail(
					file,
					getResources().getDimensionPixelSize( R.dimen.miniature_size )
				);
			}

			@Override
			public void onImageReady( @NonNull Bitmap image )
			{
				lastPageMiniature = image;
				multiPageCounter.updatePageCount( captureSessionPageCount, lastPageMiniature );
			}

			@Override
			public void onError( @NonNull Exception error )
			{
				lastPageMiniature = null;
				multiPageCounter.updatePageCount( captureSessionPageCount, null );
			}
		} );
		lastPageMiniatureLoader.execute();
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

	// This is a callback from UI component on successful image capture
	@Override
	public void onImageCaptured( @NonNull ImageCaptureScenario.Result documentCaptureResult )
	{
		// New page number is equal to the capture session page count
		int newPageNumber = ImageUtils.getCaptureSessionPageCount( this );
		CaptureResultDialogFragment resultFragment = CaptureResultDialogFragment.newInstance(
			newPageNumber, 0, documentCaptureResult.getBitmap()
		);
		resultFragment.show( getSupportFragmentManager(), "result" );

		resultDialogShown = true;
	}

	// Error handler for UI Component
	@Override
	public void onError( @NonNull Exception error )
	{
		String errorMessage;
		if( error.getMessage() != null ) {
			errorMessage = error.getMessage();
		} else {
			errorMessage = getString( R.string.unknown_error );
		}
		error.printStackTrace();
		Toast.makeText( this, errorMessage, Toast.LENGTH_SHORT ).show();
	}

	// Callback methods for CaptureResultDialogFragment

	@Override
	public void onCaptureResultConfirmed( Bitmap capturedPage, boolean confirmed, boolean shouldFinishCapture )
	{
		resultDialogShown = false;
		if( confirmed ) {
			// Create last page miniature
			lastPageMiniature = ImageUtils.getMiniature(
				capturedPage,
				getResources().getDimensionPixelSize( R.dimen.miniature_size )
			);
			int captureSessionPageCount = ImageUtils.getCaptureSessionPageCount( this );
			multiPageCounter.updatePageCount( captureSessionPageCount, lastPageMiniature );

			if( shouldFinishCapture ) { // Finish capture session & return to the main activity
				finishCapture();
				return;
			}
		}
		startCapture(); // Resume capture
	}

	private void finishWithWarning()
	{
		int addPageCount = ImageUtils.getCaptureSessionPageCount( this );
		if( addPageCount > 0 ) {
			// There are pages that would be discarded, needs confirmation
			showDiscardPagesDialog();
		} else {
			Intent intent = new Intent();

			HashMap<String, Object> json = new HashMap<>();
			intent.putExtra( "result", json );
			setResult( RtrPlugin.RESULT_FAIL, intent );
			finish();
		}
	}

	private void showDiscardPagesDialog()
	{
		stopCapture(); // Pause capture until dialog is dismissed
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.captured_pages_delete_warning )
			.setTitle( R.string.discard_pages )
			.setPositiveButton( R.string.discard, new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which )
				{
					ImageUtils.clearCaptureSessionPages( ImageCaptureActivity.this );
					Intent intent = new Intent();

					HashMap<String, Object> json = new HashMap<>();
					intent.putExtra( "result", json );
					setResult( RtrPlugin.RESULT_FAIL, intent );
					finish();
				}
			} )
			.setNegativeButton( R.string.cancel, null )
			.setOnDismissListener( new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss( DialogInterface dialog )
				{
					startCapture();
				}
			} );
		discardPagesDialog = builder.show();
	}

	@Override
	public void onBackPressed()
	{
		finishWithWarning();
	}

	private void finishCapture()
	{
		Intent intent = new Intent();

		HashMap<String, Object> json = new HashMap<>();
		ArrayList<String> pagesPaths = new ArrayList<>();
		File[] pages = ImageUtils.getCaptureSessionPages( this );
		for (File page : pages) {
			pagesPaths.add( page.getPath() );
		}
		json.put( "pages", pagesPaths );

		intent.putExtra( "result", json );
		setResult( RtrPlugin.RESULT_OK, intent );
		finish();
	}
}