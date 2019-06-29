// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.multipage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.abbyy.mobile.rtr.cordova.utils.ImageSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.rtrcordovasample.R;

import java.io.File;

/**
 * Fullscreen dialog to confirm or discard capture result. If confirmed, the page is saved into a file.
 * It is retained across orientation changes and stores the captured page bitmap
 */
public class CaptureResultDialogFragment extends DialogFragment implements ImageSaver.Callback,
	View.OnClickListener {

	public interface ResultListener {
		void onCaptureResult( CaptureResult result, CaptureTask nextPageTask );
	}

	private TextView pageNumberText;
	private SparseArray<PageHolder> pages = new SparseArray<>();
	private PagesPreviewAdapter pagesAdapter;

	// Dialog's result callback
	private ResultListener resultListener;

	private int selectedPageIndex;
	private int capturedPageNumber;
	private int capturePageNumber;

	// Whether to finish capture scenario and return straight to main activity
	private boolean finishCapture = false;
	private boolean cancel = true;
	private CaptureMode captureMode;

	public static CaptureResultDialogFragment newInstance( CaptureMode captureMode, Bitmap capturedImage, int capturedPageNumber, SparseArray<PageHolder> pages )
	{
		if( captureMode != CaptureMode.View ) {
			PageHolder pageHolder = new PageHolder();
			pageHolder.setPageImage( capturedImage );
			pages.put( capturedPageNumber, pageHolder );
		}
		CaptureResultDialogFragment dialog = new CaptureResultDialogFragment();
		dialog.setPages( captureMode, capturedPageNumber, pages );
		return dialog;
	}

	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setRetainInstance( true );
	}

	@NonNull
	@Override
	public Dialog onCreateDialog( @Nullable Bundle savedInstanceState )
	{
		Dialog dialog = super.onCreateDialog( savedInstanceState );
		dialog.getWindow().requestFeature( Window.FEATURE_NO_TITLE );
		return dialog;
	}

	@Nullable
	@Override
	public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
	{
		super.onCreateView( inflater, container, savedInstanceState );

		View view = inflater.inflate( R.layout.fragment_result, container, false );
		initPagesPreview( view );

		Button done = view.findViewById( R.id.done );
		done.setOnClickListener( this );
		Button addPage = view.findViewById( R.id.addPage );
		addPage.setOnClickListener( this );
		ImageButton retakePage = view.findViewById( R.id.retakePage );
		retakePage.setOnClickListener( this );
		ImageButton deletePage = view.findViewById( R.id.deletePage );
		deletePage.setOnClickListener( this );

		pageNumberText = view.findViewById( R.id.pageText );
		// Page number is a sum of saved and additional pages
		updatePageText();

		return view;
	}

	@Override
	public void onDestroyView()
	{
		if( getDialog() != null && getRetainInstance() ) {
			getDialog().setDismissMessage( null );
		}
		super.onDestroyView();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Dialog dialog = getDialog();
		if( dialog != null ) {
			// Make dialog fullscreen
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setLayout( width, height );
		}
	}

	@Override
	public void onAttach( Context context )
	{
		super.onAttach( context );
		if( context instanceof ResultListener ) {
			resultListener = (ResultListener) context;
		} else {
			throw new ClassCastException( context.toString() + " must implement ResultListener" );
		}
	}

	private void initPagesPreview( View view )
	{
		RecyclerView pagesPreview = view.findViewById( R.id.pagesPreview );
		pagesPreview.setHasFixedSize( true );

		LinearLayoutManager layoutManager = new LinearLayoutManager( getContext() );
		layoutManager.setOrientation( LinearLayoutManager.HORIZONTAL );
		pagesPreview.setLayoutManager( layoutManager );

		pagesAdapter = new PagesPreviewAdapter( pages );
		pagesPreview.setAdapter( pagesAdapter );

		SnapHelper snapHelper = new PagerSnapHelper();
		snapHelper.attachToRecyclerView( pagesPreview );

		pagesPreview.addOnScrollListener( new PagesPreviewAdapter.SnapOnScrollListener( snapHelper ) {
			@Override public void onPositionChanged( int position )
			{
				selectedPageIndex = position;
				updatePageText();
			}
		} );
		selectedPageIndex = pages.indexOfKey( capturedPageNumber );
		if (selectedPageIndex < 0) {
			selectedPageIndex = 0;
		}
		pagesPreview.scrollToPosition( selectedPageIndex );
	}

	private void setPages( CaptureMode captureMode, int capturedPageNumber, SparseArray<PageHolder> pages )
	{
		this.captureMode = captureMode;
		this.capturedPageNumber = capturedPageNumber;
		this.pages = pages;
	}

	private void saveInMemoryPageAndDismiss( boolean addMode )
	{
		CaptureMode mode = captureMode;
		if( addMode ) {
			captureMode = CaptureMode.Add;
			if (pages.size() == 0) {
				capturePageNumber = 1;
			} else {
				capturePageNumber = pages.keyAt( pages.size() - 1 ) + 1;
			}
		}
		if( mode != CaptureMode.View ) {
			PageHolder pageHolder = pages.get( capturedPageNumber );
			if (pageHolder != null) {
				File pageFile = ImageUtils.getCaptureSessionPageFile( capturedPageNumber, getContext() );
				pageHolder.setPageFile( pageFile );
				String pageFilePath = pageFile.getPath();
				ImageSaver tempImageSaver = new ImageSaver( pageHolder.getPageImage(), pageFilePath, this );
				tempImageSaver.execute();
				return;
			}
		}
		dismiss();
	}

	private void deletePage()
	{
		if (pages.size() == 0) {
			dismiss();
			return;
		}
		PageHolder pageHolder = pages.valueAt( selectedPageIndex );
		if( pageHolder.getPageFile() != null ) {
			pageHolder.getPageFile().delete();
		}
		updatePageText();
		pages.removeAt( selectedPageIndex );
		pagesAdapter.deletePage( selectedPageIndex );
		if (pages.size() == 0) {
			dismiss();
		}
	}

	private void updatePageText()
	{
		pageNumberText.setText( getString( R.string.page, selectedPageIndex + 1, pages.size() ) );
	}

	private void deletePageAndDismiss()
	{
		capturePageNumber = pages.keyAt( selectedPageIndex );
		PageHolder pageHolder = pages.valueAt( selectedPageIndex );
		if( pageHolder.getPageImage() != null ) {
			pages.removeAt( selectedPageIndex );
			dismiss();
		} else {
			pageHolder.getPageFile().delete();
			pages.removeAt( selectedPageIndex );
			saveInMemoryPageAndDismiss(false);
		}
	}

	@Override
	public void onImageSaved( @NonNull String filePath )
	{
		dismiss();
	}

	@Override
	public void onError( @NonNull Exception error )
	{
		String errorMessage;
		if( error.getMessage() != null ) {
			errorMessage = error.getMessage();
		} else {
			errorMessage = getString( R.string.unknown_error );
		}
		Toast.makeText( getContext(), errorMessage, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onClick( View v )
	{
		switch( v.getId() ) {
			case R.id.addPage:
				finishCapture = false;
				cancel = false;
				saveInMemoryPageAndDismiss(true);
				break;
			case R.id.done:
				finishCapture = true;
				cancel = false;
				saveInMemoryPageAndDismiss(false);
				break;
			case R.id.deletePage:
				deletePage();
				break;
			case R.id.retakePage:
				finishCapture = false;
				captureMode = CaptureMode.Retake;
				cancel = false;
				deletePageAndDismiss();
				break;
		}
	}

	@Override
	public void onDismiss( DialogInterface dialog )
	{
		CaptureResult result;
		CaptureTask task;
		if (cancel) {
			pages.remove( capturedPageNumber );
			result = null;
			task = null;
		} else {
			result = new CaptureResult( pages, capturedPageNumber, captureMode, finishCapture );
			task = new CaptureTask( capturePageNumber, captureMode );
		}
		resultListener.onCaptureResult( result, task );
		super.onDismiss( dialog );
	}

}
