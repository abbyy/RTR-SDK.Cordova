package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;

/**
 * Auxiliary class for the view that displays the current session's page count
 * and the miniature of the last captured page (loads it if necessary)
 * and serves as a button to finish current capture session
 */
public class MultiPageCounter extends FrameLayout {
	private static Bitmap imageCaptureMiniature;
	public static Bitmap getImageCaptureMiniature()
	{
		return imageCaptureMiniature;
	}

	public static void setImageCaptureMiniature( Bitmap imageCaptureMiniature )
	{
		MultiPageCounter.imageCaptureMiniature = imageCaptureMiniature;
	}

	private ImageView lastPageMiniatureView;
	private TextView pageCounter;

	public MultiPageCounter( Context context )
	{
		super( context );
		init();
	}

	public MultiPageCounter( Context context, AttributeSet attrs )
	{
		super( context, attrs );
		init();
	}

	public MultiPageCounter( Context context, AttributeSet attrs, int defStyleAttr )
	{
		super( context, attrs, defStyleAttr );
		init();
	}

	public MultiPageCounter( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( context, attrs, defStyleAttr, defStyleRes );
		init();
	}

	private void init()
	{
		inflate( getContext(), ResourcesUtils.getResId( "layout", "page_counter", getContext()), this );
		lastPageMiniatureView = findViewById( ResourcesUtils.getResId( "id", "lastPage", getContext()) );
		pageCounter = findViewById( ResourcesUtils.getResId( "id", "pageCounter", getContext()) );
	}

	public void updatePageCount( int captureSessionPageCount, Bitmap lastPageMiniature )
	{
		if( captureSessionPageCount > 0 ) {
			if( lastPageMiniature == null && getImageCaptureMiniature() != null ) {
				lastPageMiniature = getImageCaptureMiniature();
			}
			if( lastPageMiniature != null ) {
				imageCaptureMiniature = lastPageMiniature;
				lastPageMiniatureView.setImageDrawable( ImageUtils.getRoundedImage( getContext(), lastPageMiniature ) );
			}
			pageCounter.setText( String.valueOf( captureSessionPageCount ) );
			setVisibility( View.VISIBLE );
		} else {
			setVisibility( View.GONE );
		}
	}
}
