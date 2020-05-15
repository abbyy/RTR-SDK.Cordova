// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.
package com.abbyy.mobile.rtr.javascript.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

import static com.abbyy.mobile.rtr.javascript.utils.UriScheme.BASE_64_JPEG;
import static com.abbyy.mobile.rtr.javascript.utils.UriScheme.BASE_64_PNG;

public class ImageUri {

	// image/jpg is used in some libraries, but it is not an official mime type
	private static final String BASE_64_JPG_SCHEME = "data:image/jpg;base64,";

	private ImageUri()
	{
		// Utility
	}

	/**
	 * Load {@link Bitmap} from string uri.
	 * Supports schemes which are supported by {@link Uri}.
	 * Planned support for:
	 * - data uri with jpg and png mime type
	 *
	 * @throws java.io.IOException if can't load Bitmap.
	 */
	public static Bitmap fromUri( @NonNull Context context, @NonNull String stringUri ) throws IOException
	{
		boolean isBase64Scheme = stringUri.startsWith( BASE_64_JPEG ) ||
			stringUri.startsWith( BASE_64_JPG_SCHEME ) ||
			stringUri.startsWith( BASE_64_PNG );
		if( isBase64Scheme ) {
			return fromBase64( stringUri );
		} else {
			return fromContentOrFileScheme( context, stringUri );
		}
	}

	private static Bitmap fromBase64( String stringUri ) {
		// indexOf is byte offset, because scheme contains chars which are represented with one byte in getBytes().
		int offset = stringUri.indexOf( ',' ) + 1;
		byte[] uriBytes = stringUri.getBytes();
		int length = uriBytes.length - offset;
		byte[] decodedString = Base64.decode( uriBytes, offset, length, Base64.DEFAULT );
		return BitmapFactory.decodeByteArray( decodedString, 0, decodedString.length );
	}

	private static Bitmap fromContentOrFileScheme( Context context, String stringUri ) throws IOException
	{
		Uri uri = Uri.parse( stringUri );
		try( InputStream inputStream = context.getContentResolver().openInputStream( uri ) ) {
			Bitmap bitmap = BitmapFactory.decodeStream( inputStream );
			if( bitmap == null ) {
				throw new IOException( "Could not load image from URI: " + uri );
			}

			int orientation = getImageOrientationFromUri( context, uri );

			return rotateBitmapIfNeeded( bitmap, orientation );
		}
	}

	private static int getImageOrientationFromUri( Context context, Uri uri ) throws IOException
	{
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
			try( InputStream inputStream = context.getContentResolver().openInputStream( uri ) ) {
				if( inputStream == null) {
					return ExifInterface.ORIENTATION_NORMAL;
				}
				ExifInterface exifInterface = new ExifInterface( inputStream );
				return exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );
			}
		} else {
			if( uri.getPath() == null) {
				return ExifInterface.ORIENTATION_NORMAL;
			}
			ExifInterface exifInterface = new ExifInterface( uri.getPath() );
			return exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );
		}
	}

	private static Bitmap rotateBitmapIfNeeded( Bitmap bitmap, int orientation )
	{
		switch( orientation ) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return rotateImage( bitmap, 90 );
			case ExifInterface.ORIENTATION_ROTATE_180:
				return rotateImage( bitmap, 180 );
			case ExifInterface.ORIENTATION_ROTATE_270:
				return rotateImage( bitmap, 270 );
			default:
				return bitmap;
		}
	}

	private static Bitmap rotateImage( Bitmap bitmap, float rotationAngle )
	{
		Matrix matrix = new Matrix();
		matrix.postRotate( rotationAngle );
		return Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true );
	}

}
