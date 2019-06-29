// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.multipage;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.abbyy.mobile.rtr.cordova.utils.ImageLoader;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.rtr.cordova.utils.MemoryCappedCache;

import java.io.File;
import java.io.IOException;

/**
 * Adapter for pages recycler view, with memory cache and background loading
 */
public class PagesPreviewAdapter extends RecyclerView.Adapter<PagesPreviewAdapter.ViewHolder> {
	private SparseArray<PageHolder> pageFiles;

	// Cache for pages
	private MemoryCappedCache pageCache;

	public PagesPreviewAdapter( SparseArray<PageHolder> pageFiles )
	{
		this.pageFiles = pageFiles;
		int maxMemory = (int) ( Runtime.getRuntime().maxMemory() );

		// Use 1/8th of the available memory for the memory cache.
		int cacheSize = maxMemory / 8;
		pageCache = new MemoryCappedCache( cacheSize );
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

		return new ViewHolder( pageView );
	}

	@Override
	public void onBindViewHolder( @NonNull final ViewHolder viewHolder, int position )
	{
		viewHolder.bind( position );
	}

	@Override
	public int getItemCount()
	{
		return pageFiles.size();
	}

	public void deletePage( int selectedPageIndex )
	{
		pageCache.remove( selectedPageIndex );
		notifyItemRemoved( selectedPageIndex );
	}

	// Holder for thumbnail view
	class ViewHolder extends RecyclerView.ViewHolder {
		// Asynchronous loader
		private ImageLoader loader;
		private final ImageLoader.Callback callback = new ImageLoader.Callback() {
			@Override
			public Bitmap loadBitmap( @NonNull File file ) throws IOException
			{
				return ImageUtils.loadBitmap( file );
			}

			@Override
			public void onImageReady( @NonNull Bitmap image )
			{
				updateAndNotify( image );
			}

			@Override
			public void onError( @NonNull Exception error )
			{
				updateAndNotify( null );
			}
		};

		ViewHolder( @NonNull ImageView pageView )
		{
			super( pageView );
		}

		public void bind( int position )
		{
			Bitmap bitmap = null;
			PageHolder holder = pageFiles.valueAt( position );
			if( holder.getPageImage() != null ) {
				bitmap = holder.getPageImage();
			}
			if( bitmap == null ) {
				bitmap = pageCache.get( position );
			}
			if( bitmap == null && holder.getPageFile() != null ) {
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
			String pageFilePath = pageFiles.valueAt( position ).getPageFile().getPath();
			return new ImageLoader( pageFilePath, callback );
		}

		private void updateAndNotify( Bitmap image )
		{
			( (ImageView) itemView ).setImageBitmap( image );
			if( image != null ) {
				pageCache.put( getAdapterPosition(), image );
			}
		}
	}

	public static abstract class SnapOnScrollListener extends RecyclerView.OnScrollListener {
		private SnapHelper snapHelper;
		private int snapPosition = RecyclerView.NO_POSITION;

		public SnapOnScrollListener( @NonNull SnapHelper snapHelper )
		{
			this.snapHelper = snapHelper;
		}

		abstract void onPositionChanged( int position );

		@Override public void onScrollStateChanged( @NonNull RecyclerView recyclerView, int newState )
		{
			if( newState == RecyclerView.SCROLL_STATE_IDLE ) {
				notifySnapPosition( recyclerView );
			}
		}

		private void notifySnapPosition( RecyclerView recyclerView )
		{
			RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
			if( layoutManager == null ) {
				return;
			}
			View snapView = snapHelper.findSnapView( layoutManager );
			if( snapView == null ) {
				return;
			}
			int position = layoutManager.getPosition( snapView );
			if( position != snapPosition ) {
				snapPosition = position;
				onPositionChanged( position );
			}
		}
	}
}
