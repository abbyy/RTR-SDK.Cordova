// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for handling & storing images
 */
public class ImageUtils {

	public static Bitmap getBitmap( String imageUri, Context context ) throws IOException
	{
		Uri uri = Uri.parse( imageUri );
		try( InputStream inputStream = context.getContentResolver().openInputStream( uri ) ) {
			Bitmap bitmap = BitmapFactory.decodeStream( inputStream );
			if( bitmap == null ) {
				throw new IOException( "Could not load image from URI: " + uri );
			}
			return bitmap;
		}
	}

	public static File getCaptureSessionPdfFile( Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		if( !captureSessionDir.exists() ) {
			captureSessionDir.mkdir();
		}
		return new File( captureSessionDir, "document.pdf" );
	}

	private static File getCaptureSessionDir( Context context )
	{
		return new File( context.getFilesDir(), "pages" );
	}
}
