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

import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.utils.ImageSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;

import java.io.File;

/**
 * Fullscreen dialog to confirm or discard capture result. If confirmed, the page is saved into a file.
 * It is retained across orientation changes and stores the captured page bitmap
 */
public class CaptureResultDialogFragment extends DialogFragment implements ImageSaver.Callback,
	View.OnClickListener {

	public interface ResultListener {
		void onCaptureResult( CaptureResult result, int nextPageNumber, Bitmap lastPageMiniature );
	}

	private TextView pageNumberText;
	private PagesPreviewAdapter pagesAdapter;

	// Dialog's result callback
	private ResultListener resultListener;

	private int selectedPageIndex;

	private int currentPageNumber;

	private int nextPageNumber;

	// Whether to finish capture scenario and return straight to main activity
	private boolean finishCapture = false;
	private Bitmap lastPageMiniature = null;

	public static CaptureResultDialogFragment newInstance( int pageNumber )
	{
		CaptureResultDialogFragment dialog = new CaptureResultDialogFragment();
		dialog.setPageNumber( pageNumber );
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

		View view = inflater.inflate( ResourcesUtils.getResId( "layout", "fragment_result", getContext()), container, false );
		initPagesPreview( view );

		Button done = view.findViewById( ResourcesUtils.getResId( "id", "done", getContext()) );
		done.setOnClickListener( this );
		Button addPage = view.findViewById( ResourcesUtils.getResId( "id", "addPage", getContext()) );
		addPage.setOnClickListener( this );
		Button retakePage = view.findViewById( ResourcesUtils.getResId( "id", "retakePage", getContext()) );
		retakePage.setOnClickListener( this );
		Button deletePage = view.findViewById( ResourcesUtils.getResId( "id", "deletePage", getContext()) );
		deletePage.setOnClickListener( this );

		pageNumberText = view.findViewById(  ResourcesUtils.getResId( "id", "pageText", getContext()) );
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

	private SparseArray<PageHolder> getPages() {
		return RtrManager.getImageCaptureResult();
	}

	private void initPagesPreview( View view )
	{
		RecyclerView pagesPreview = view.findViewById( ResourcesUtils.getResId( "id", "pagesPreview", getContext()) );
		pagesPreview.setHasFixedSize( true );

		LinearLayoutManager layoutManager = new LinearLayoutManager( getContext() );
		layoutManager.setOrientation( LinearLayoutManager.HORIZONTAL );
		pagesPreview.setLayoutManager( layoutManager );

		pagesAdapter = new PagesPreviewAdapter( getPages() );
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
		selectedPageIndex = getPages().indexOfKey( currentPageNumber );
		if (selectedPageIndex < 0) {
			selectedPageIndex = getPages().size() - 1;
		}
		pagesPreview.scrollToPosition( selectedPageIndex );
	}

	private void setPageNumber( int currentPageNumber )
	{
		nextPageNumber = this.currentPageNumber = currentPageNumber;
	}

	private void savePageAndDismiss( boolean addMode )
	{
		if( addMode ) {
			nextPageNumber = getNextPageNumber(getPages());
		}
		PageHolder pageHolder = getPages().get( currentPageNumber );
		if (pageHolder != null && !pageHolder.isSaved()) {
			if (addMode) {
				lastPageMiniature = ImageUtils.getMiniature(
					pageHolder.getPageImage(),
					getContext().getResources().getDimensionPixelSize(
						ResourcesUtils.getResId( "dimen", "miniature_size", getContext())
					)
				);
			}
			pageHolder.saveToFile(getContext(), this);
			return;
		}
		dismiss();
	}

	public static int getNextPageNumber( SparseArray<PageHolder> pages )
	{
		int nextPageNumber;
		if (pages.size() == 0) {
			nextPageNumber = 1;
		} else {
			nextPageNumber = pages.keyAt( pages.size() - 1 ) + 1;
		}
		return nextPageNumber;
	}

	private void deletePage()
	{
		if (getPages().size() == 0) {
			dismiss();
			return;
		}
		PageHolder pageHolder = getPages().valueAt( selectedPageIndex );
		if( pageHolder.getPageFile() != null ) {
			pageHolder.getPageFile().delete();
		}
		getPages().removeAt( selectedPageIndex );
		int pageCount = getPages().size();
		if (pageCount == 0) {
			dismiss();
		} else {
			pagesAdapter.deletePage( selectedPageIndex );
			if( selectedPageIndex >= pageCount ) {
				selectedPageIndex = pageCount - 1;
			}
			updatePageText();
		}
	}

	private void updatePageText()
	{
		pageNumberText.setText( getString( 
			ResourcesUtils.getResId( "string", "page", getContext()),
			selectedPageIndex + 1,
			getPages().size()
		) );
	}

	private void deletePageAndDismiss()
	{
		nextPageNumber = getPages().keyAt( selectedPageIndex );
		dismiss();
	}

	@Override
	public void onImageSaved( @NonNull File file )
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
			errorMessage = getString( ResourcesUtils.getResId( "string", "unknown_error", getContext()) );
		}
		Toast.makeText( getContext(), errorMessage, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onClick( View v )
	{
		if( v.getId() ==  ResourcesUtils.getResId( "id", "addPage", getContext())) {
			finishCapture = false;
			savePageAndDismiss(true);
		} else if( v.getId() ==  ResourcesUtils.getResId( "id", "done", getContext())) {
			finishCapture = true;
			savePageAndDismiss(false);
		} else if( v.getId() ==  ResourcesUtils.getResId( "id", "deletePage", getContext())) {
			deletePage();
		} else if( v.getId() ==  ResourcesUtils.getResId( "id", "retakePage", getContext())) {
			finishCapture = false;
			deletePageAndDismiss();
		}
	}

	@Override
	public void onDismiss( DialogInterface dialog )
	{
		CaptureResult result = new CaptureResult( currentPageNumber, finishCapture );
		resultListener.onCaptureResult( result, nextPageNumber, lastPageMiniature );
		super.onDismiss( dialog );
	}

}
