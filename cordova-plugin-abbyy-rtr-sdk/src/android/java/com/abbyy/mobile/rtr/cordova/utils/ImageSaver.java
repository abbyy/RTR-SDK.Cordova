// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.io.File;
import java.io.IOException;

// Utility class to asynchronously save bitmap to file
public class ImageSaver extends AsyncTask<Void, Void, Void> {
	public interface Callback {
		@UiThread
		void onImageSaved( @NonNull String filePath );

		@UiThread
		void onError( @NonNull Exception error );
	}

	private Bitmap image;
	private Exception exception = null;
	private String filePath;
	private Callback callback;

	public ImageSaver( Bitmap image, String filePath, Callback callback )
	{
		this.image = image;
		this.filePath = filePath;
		this.callback = callback;
	}

	@Override
	protected Void doInBackground( Void... args )
	{
		try {
			File file = new File( filePath );
			ImageUtils.saveBitmap( image, file );
		} catch( IOException exception ) {
			this.exception = exception;
		}
		return null;
	}

	@Override
	protected void onPostExecute( Void arg )
	{
		if( exception != null ) {
			callback.onError( exception );
		} else {
			callback.onImageSaved( filePath );
		}
	}
}
