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
import android.util.SparseArray;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;

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
			if ( ImageCaptureSettings.exportType == ImageCaptureSettings.ExportType.PNG ) {
				image.compress( Bitmap.CompressFormat.PNG, 0, fos );
			} else {
				int quality = 0;
				switch( ImageCaptureSettings.compressionLevel ) {
					case Low:
						quality = 100;
						break;
					case Normal:
						quality = 66;
						break;
					case High:
						quality = 33;
						break;
					case ExtraHigh:
						quality = 0;
						break;
				}
				image.compress( Bitmap.CompressFormat.JPEG, quality, fos );
			}
			fos.flush();
		}
	}

	// Synchronously load bitmap from file
	public static Bitmap loadBitmap( File file ) throws IOException
	{
		Bitmap image;
		try( FileInputStream fis = new FileInputStream( file ) ) {
			image = BitmapFactory.decodeStream( fis, null, null );
		}
		return image;
	}

	public static File getCaptureSessionPageFile( int pageIndex, Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		if( !captureSessionDir.exists() ) {
			captureSessionDir.mkdir();
		}
		return new File( captureSessionDir, "page_" + ( pageIndex + 1 ) + ".png" );
	}

	public static File getCaptureSessionPdfFile( Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		if( !captureSessionDir.exists() ) {
			captureSessionDir.mkdir();
		}
		return new File( captureSessionDir, "pages.pdf" );
	}

	public static File[] getCaptureSessionPages( Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		File[] files = null;
		if( captureSessionDir.exists() ) {
			files = captureSessionDir.listFiles();
		}
		return files != null ? files : new File[] {};
	}

	private static File getCaptureSessionDir( Context context )
	{
		File root = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );
		return new File( root/*context.getFilesDir()*/, "session_pages" );
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
