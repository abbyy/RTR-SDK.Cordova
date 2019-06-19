// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

// Utility class to asynchronously load bitmap from file
public class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

	public interface Callback {
		Bitmap loadBitmap( @NonNull File file ) throws IOException;

		@UiThread
		void onImageReady( @NonNull Bitmap image );

		@UiThread
		void onError( @NonNull Exception error );
	}

	private String filePath;
	private Exception exception = null;
	private Callback callback;

	public ImageLoader( String filePath, Callback callback )
	{
		this.filePath = filePath;
		this.callback = callback;
	}

	@Override
	protected Bitmap doInBackground( Void... args )
	{
		Bitmap image = null;
		try {
			File file = new File( filePath );
			if( !file.exists() ) {
				exception = new FileNotFoundException();
				return null;
			}
			image = callback.loadBitmap( file );
		} catch( IOException exception ) {
			this.exception = exception;
		}
		return image;
	}

	@Override
	protected void onPostExecute( Bitmap image )
	{
		if( exception != null ) {
			callback.onError( exception );
		} else {
			callback.onImageReady( image );
		}
	}
}
