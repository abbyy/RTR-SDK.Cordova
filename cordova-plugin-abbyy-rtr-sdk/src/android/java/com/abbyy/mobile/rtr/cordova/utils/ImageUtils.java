// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.abbyy.mobile.rtr.cordova.ImageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for handling & storing images
 */
public class ImageUtils {

	public static String convertFileToBase64( File file ) throws IOException
	{
		try( FileInputStream fis = new FileInputStream( file ) ) {
			byte[] bytes = new byte[(int) file.length()];

			int offset = 0;
			int bytesRead = 0;
			while( offset < bytes.length && bytesRead >= 0 ) {
				bytesRead = fis.read( bytes, offset, bytes.length - offset );
				offset += bytesRead;
			}
			return Base64.encodeToString( bytes, Base64.DEFAULT );
		}
	}

	public static Bitmap getBitmap( String image, ImageType imageType, Context context ) throws IOException
	{
		switch( imageType ) {
			case Base64:
				byte[] decodedString = Base64.decode( image, Base64.DEFAULT );
				return BitmapFactory.decodeByteArray( decodedString, 0, decodedString.length );
			case URI:
				Uri uri = Uri.parse( image );
				try( InputStream inputStream = context.getContentResolver().openInputStream( uri ) ) {
					Bitmap bitmap = BitmapFactory.decodeStream( inputStream );
					if( bitmap == null ) {
						throw new IOException( "Could not load image from URI: " + uri );
					}
					return bitmap;
				}
			case FilePath:
				Bitmap bitmap = BitmapFactory.decodeFile( image );
				if( bitmap == null ) {
					throw new IOException( "Could not load image from file: " + image );
				}
				return bitmap;
		}
		return null;
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
