// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.abbyy.mobile.rtr.cordova.utils.ImageLoader;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.rtr.cordova.utils.MemoryCappedCache;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Adapter for pages recycler view, with memory cache and background loading
 */
public class PagesPreviewAdapter extends RecyclerView.Adapter<PagesPreviewAdapter.ViewHolder> {
	private int pagesCount;
	private Bitmap pageImage;

	// Cache for pages
	private MemoryCappedCache pageCache;

	public PagesPreviewAdapter( int pagesCount, Bitmap pageImage )
	{
		this.pagesCount = pagesCount;
		this.pageImage = pageImage;
		int maxMemory = (int) ( Runtime.getRuntime().maxMemory() );

		// Use 1/8th of the available memory for the memory cache.
		int cacheSize = maxMemory / 8;
		pageCache = new MemoryCappedCache( cacheSize );
	}

	public void releasePages()
	{
		pageCache.evictAll();
		pagesCount = 0;
		notifyDataSetChanged();
	}

	public void updatePagesCount( int pagesCount )
	{
		if( this.pagesCount != pagesCount ) {
			this.pagesCount = pagesCount;
			notifyDataSetChanged();
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType )
	{
		// Constructing thumbnail view
		ImageView pageView = new ImageView( parent.getContext() );
		pageView.setScaleType( ImageView.ScaleType.FIT_CENTER );
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );
		pageView.setLayoutParams( layoutParams );

		return new ViewHolder( pageView, pageCache, pagesCount - 1, pageImage );
	}

	@Override
	public void onBindViewHolder( @NonNull final ViewHolder viewHolder, int position )
	{
		viewHolder.bind( position );
	}

	@Override
	public int getItemCount()
	{
		return pagesCount;
	}

	// Holder for thumbnail view
	static class ViewHolder extends RecyclerView.ViewHolder {
		private final int pageNumber;
		private Bitmap pageImage;
		// Asynchronous loader
		private ImageLoader loader;
		// Bitmap cache held by weak reference
		private WeakReference<MemoryCappedCache> bitmapCacheRef;
		// Thumbnail is square, so one dimension is stored
		private ImageLoader.Callback callback = new ImageLoader.Callback() {
			@Override
			public Bitmap loadBitmap( @NonNull File file ) throws IOException
			{
				return ImageUtils.loadBitmap( file );
			}

			@Override
			public void onImageReady( @NonNull Bitmap thumbnail )
			{
				// Thumbnail loaded, update view and store in cache
				updateAndNotify( thumbnail );
			}

			@Override
			public void onError( @NonNull Exception error )
			{
				// Thumbnail not loaded, update view to empty
				updateAndNotify( null );
			}
		};

		ViewHolder( @NonNull ImageView pageView, MemoryCappedCache bitmapCache, int pageNumber, Bitmap pageImage )
		{
			super( pageView );
			this.bitmapCacheRef = new WeakReference<>( bitmapCache );
			this.pageNumber = pageNumber;
			this.pageImage = pageImage;
		}

		public void bind( int position )
		{
			Bitmap bitmap = null;
			if( position == pageNumber ) {
				bitmap = pageImage;
			}
			MemoryCappedCache bitmapCache = bitmapCacheRef.get();
			if( bitmapCache == null ) {
				return;
			}
			if( bitmap == null ) {
				bitmap = bitmapCache.get( position );
			}
			if( bitmap == null ) {
				if( loader != null ) {
					loader.cancel( true );
				}
				loader = getLoader( position );
				loader.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR );
			} else {
				( (ImageView) itemView ).setImageBitmap( bitmap );
			}
		}

		private ImageLoader getLoader( int position )
		{
			String pageFilePath = ImageUtils.getCaptureSessionPageFile( position, itemView.getContext() ).getPath();
			return new ImageLoader( pageFilePath, callback );
		}

		private void updateAndNotify( Bitmap image )
		{
			( (ImageView) itemView ).setImageBitmap( image );
			MemoryCappedCache bitmapCache = bitmapCacheRef.get();
			if( bitmapCache != null && image != null ) {
				bitmapCache.put( getAdapterPosition(), image );
			}
		}
	}

	public interface OnSnapPositionChangeListener {
		void onPositionChanged( int position );
	}

	public static class SnapOnScrollListener extends RecyclerView.OnScrollListener {
		private SnapHelper snapHelper;
		private int snapPosition = RecyclerView.NO_POSITION;
		private OnSnapPositionChangeListener listener;

		public SnapOnScrollListener( @NonNull SnapHelper snapHelper, OnSnapPositionChangeListener listener ) {
			this.snapHelper = snapHelper;
			this.listener = listener;
		}

		@Override public void onScrollStateChanged( @NonNull RecyclerView recyclerView, int newState )
		{
			if (newState == RecyclerView.SCROLL_STATE_IDLE) {
				notifySnapPosition(recyclerView);
			}
		}

		private void notifySnapPosition( RecyclerView recyclerView )
		{
			if (listener == null) {
				return;
			}
			RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
			if (layoutManager == null) {
				return;
			}
			View snapView = snapHelper.findSnapView( layoutManager );
			if (snapView == null) {
				return;
			}
			int position = layoutManager.getPosition( snapView );
			if (position != snapPosition) {
				snapPosition = position;
				listener.onPositionChanged( position );
			}
		}
	}
}
