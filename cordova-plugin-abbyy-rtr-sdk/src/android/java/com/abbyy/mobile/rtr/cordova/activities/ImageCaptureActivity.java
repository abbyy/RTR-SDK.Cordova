// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.multipage.CaptureResult;
import com.abbyy.mobile.rtr.cordova.multipage.CaptureResultDialogFragment;
import com.abbyy.mobile.rtr.cordova.multipage.MultiCaptureResult;
import com.abbyy.mobile.rtr.cordova.multipage.MultiPageCounter;
import com.abbyy.mobile.rtr.cordova.multipage.PageHolder;
import com.abbyy.mobile.rtr.cordova.utils.ImageSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.rtr.cordova.utils.MultiPagesProcessor;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;
import com.abbyy.rtrcordovasample.R;

import java.io.File;
import java.util.HashMap;

import static com.abbyy.mobile.rtr.cordova.RtrPlugin.INTENT_RESULT_KEY;

/**
 * This activity uses UI component to capture a page, shows a dialog to confirm or discard
 * the result, and keeps count
 */
public class ImageCaptureActivity extends AppCompatActivity implements ImageCaptureScenario.Callback,
	CaptureResultDialogFragment.ResultListener {

	public static final String RESULT_SHOWN_EXTRA = "result_shown";
	public static final String CAPTURE_PAGE_EXTRA = "capture_page";

	private MultiPageCounter multiPageCounter;

	// Capture view component
	private CaptureView captureView;
	private ImageCaptureScenario imageCaptureScenario;

	// This dialog is shown when user wants to leave without saving
	private AlertDialog discardPagesDialog;

	// Whether the result dialog is currently shown
	private boolean resultDialogShown = false;

	private int capturePageNumber = 0;

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
			capturePageNumber = savedInstanceState.getInt( CAPTURE_PAGE_EXTRA, 0 );
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

	// Supporting correct behavior on activity re-creation
	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		outState.putBoolean( RESULT_SHOWN_EXTRA, resultDialogShown );
		outState.putInt( CAPTURE_PAGE_EXTRA, capturePageNumber );
		super.onSaveInstanceState( outState );
	}

	private void initPageCounterView()
	{
		multiPageCounter = (MultiPageCounter) findViewById( R.id.finishCapture );
		multiPageCounter.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				showResultFragment();
			}
		} );

		int pageCount = getPages().size();
		multiPageCounter.updatePageCount( pageCount, null );
	}

	private void initCaptureView()
	{
		captureView = (CaptureView) findViewById( R.id.captureView );
		captureView.getUISettings().setCaptureButtonVisible( ImageCaptureSettings.manualCaptureVisible );
		captureView.getUISettings().setFlashlightButtonVisible( ImageCaptureSettings.flashlightVisible );
		captureView.getCameraSettings().setResolution( ImageCaptureSettings.cameraResolution );

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

	private SparseArray<PageHolder> getPages()
	{
		return RtrManager.getImageCaptureResult();
	}

	// This is a callback from UI component on successful image capture
	@Override
	public void onImageCaptured( @NonNull ImageCaptureScenario.Result documentCaptureResult )
	{
		PageHolder pageHolder = updatePage( documentCaptureResult );
		if( ImageCaptureSettings.showResultOnCapture ) {
			showResultFragment();
		} else {
			// Proceed automatically
			Bitmap pageMiniature = pageHolder.saveToFile( this, new ImageSaver.Callback() {
				@Override public void onImageSaved( @NonNull File file )
				{
					if( ImageCaptureSettings.pageCount > 0 && getPages().size() >= ImageCaptureSettings.pageCount ) {
						finishCapture( true );
					} else {
						capturePageNumber = CaptureResultDialogFragment.getNextPageNumber( getPages() );
						startCapture(); // Resume capture
					}
				}

				@Override public void onError( @NonNull Exception error )
				{
					startCapture();
				}
			} );
			updatePageCount( pageMiniature );
		}
	}

	private PageHolder updatePage( @NonNull ImageCaptureScenario.Result documentCaptureResult )
	{
		PageHolder pageHolder = getPages().get( capturePageNumber );
		Bitmap pageImage = documentCaptureResult.getBitmap();
		if( pageHolder == null ) {
			pageHolder = new PageHolder( capturePageNumber, pageImage );
			getPages().append( capturePageNumber, pageHolder );
		} else {
			pageHolder.setPageImage( pageImage );
		}
		if( documentCaptureResult.getDocumentBoundary() != null ) {
			pageHolder.setCropped( false );
			pageHolder.setDocumentBoundary( documentCaptureResult.getDocumentBoundary() );
			pageHolder.setFrameSize( new Point( pageImage.getWidth(), pageImage.getHeight() ) );
		} else {
			pageHolder.setCropped( true );
		}
		return pageHolder;
	}

	private void showResultFragment()
	{
		if( resultDialogShown ) {
			return;
		}
		CaptureResultDialogFragment resultFragment = CaptureResultDialogFragment.newInstance( capturePageNumber );
		resultFragment.show( getSupportFragmentManager(), "result" );

		resultDialogShown = true;
	}

	// Error handler for UI Component
	@Override
	public void onError( @NonNull Exception error )
	{
		error.printStackTrace();
		finishWithEmptyResult( error );
	}

	// Callback methods for CaptureResultDialogFragment

	@Override
	public void onCaptureResult( CaptureResult result, int nextPageNumber, Bitmap lastPageMiniature )
	{
		resultDialogShown = false;
		if( nextPageNumber > 0 ) {
			updatePageCount( lastPageMiniature );
		}

		if( result.isFinishCapture() ) { // Finish capture session and return to the main activity
			finishCapture( false );
			return;
		}
		this.capturePageNumber = nextPageNumber;
		startCapture(); // Resume capture
	}

	private void updatePageCount( Bitmap lastPageMiniature )
	{
		int pageCount = getPages().size();
		multiPageCounter.updatePageCount( pageCount, lastPageMiniature );
	}

	private void finishWithWarning()
	{
		int pageCount = getPages().size();
		if( pageCount > 0 ) {
			// There are pages that would be discarded, needs confirmation
			showDiscardPagesDialog();
		} else {
			finishWithEmptyResult( null );
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
					finishWithEmptyResult( null );
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

	private void finishWithEmptyResult( Exception error )
	{
		Intent intent = new Intent();

		HashMap<String, Object> json = MultiCaptureResult.getErrorJsonResult( error, context );
		intent.putExtra( "result", json );
		setResult( RtrPlugin.RESULT_FAIL, intent );
		RtrManager.setImageCaptureResult( null );
		finish();
	}

	@Override
	public void onBackPressed()
	{
		finishWithWarning();
	}

	private void finishCapture( boolean isAutomaticCapture )
	{
		processMultiPages( isAutomaticCapture );
	}

	private void processMultiPages( final boolean isAutomaticCapture )
	{
		new MultiPagesProcessor( getPages(), this, new MultiPagesProcessor.Callback() {
			@Override public void onProcessed()
			{
				finishCaptureResult( isAutomaticCapture );
			}
		} ).execute();
	}

	private void finishCaptureResult( boolean isAutomaticCapture )
	{
		Intent intent = new Intent();

		HashMap<String, Object> json = new HashMap<>(  );
		json.put( "captureFinishAutomatically", isAutomaticCapture );

		intent.putExtra( INTENT_RESULT_KEY, json );
		setResult( RtrPlugin.RESULT_OK, intent );
		finish();
	}
}