// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Utility class for handling & storing images
 */
public class ImageUtils {

	// Synchronously save bitmap to file
	public static void saveBitmap( Bitmap image, File file ) throws IOException
	{
		try( FileOutputStream fos = new FileOutputStream( file ) ) {
			image.compress( Bitmap.CompressFormat.PNG, 100, fos );
			fos.flush();
		}
	}

	// Load bitmap and scale it to fit required size
	public static Bitmap loadThumbnail( File file, int requiredDimension ) throws IOException
	{
		int ratio = getThumbnailRatio( file, requiredDimension );

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = ratio;
		options.inJustDecodeBounds = false;

		Bitmap image;
		try( FileInputStream fis = new FileInputStream( file ) ) {
			image = BitmapFactory.decodeStream( fis, null, options );
		}
		return ThumbnailUtils.extractThumbnail( image, requiredDimension, requiredDimension );
	}

	private static int getThumbnailRatio( File file, int requiredDimension ) throws IOException
	{
		int ratio = 1;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try( FileInputStream fis = new FileInputStream( file ) ) {
			BitmapFactory.decodeStream( fis, null, options );
		}
		int height = options.outHeight;
		int width = options.outWidth;

		if( height > requiredDimension || width > requiredDimension ) {
			int heightRatio = Math.round( (float) height / (float) requiredDimension );
			int widthRatio = Math.round( (float) width / (float) requiredDimension );

			ratio = Math.min( heightRatio, widthRatio );
		}
		return ratio;
	}

	public static File getCaptureSessionPageFile( int pageIndex, Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		if( !captureSessionDir.exists() ) {
			captureSessionDir.mkdir();
		}
		return new File( captureSessionDir, "page_" + ( pageIndex + 1 ) + ".png" );
	}

	public static File[] getCaptureSessionPages( Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		File[] files = null;
		if (captureSessionDir.exists()) {
			files = captureSessionDir.listFiles();
		}
		return files != null ? files : new File[] {};
	}

	private static File getCaptureSessionDir( Context context )
	{
		File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File( root, "session_pages" );
	}

	public static int getCaptureSessionPageCount( Context context )
	{
		return getCaptureSessionPages( context ).length;
	}

	public static void clearCaptureSessionPages( Context context )
	{
		for( File captureSessionPage : getCaptureSessionPages( context ) ) {
			captureSessionPage.delete();
		}
	}

	// Constructs drawable with rounded corners from bitmap
	public static RoundedBitmapDrawable getRoundedImage( Context context, Bitmap originalImage )
	{
		if( originalImage == null ) {
			return null;
		}
		RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create( context.getResources(), originalImage );
		roundedBitmapDrawable.setCornerRadius( 10 );
		return roundedBitmapDrawable;
	}

	public static Bitmap getMiniature( Bitmap page, int miniatureSize )
	{
		int width = page.getWidth();
		int height = page.getHeight();
		float scaleWidth = ( (float) miniatureSize ) / width;
		float scaleHeight = ( (float) miniatureSize ) / height;

		Matrix matrix = new Matrix();
		matrix.postScale( scaleWidth, scaleHeight );

		return Bitmap.createBitmap( page, 0, 0, width, height, matrix, false );
	}
}
