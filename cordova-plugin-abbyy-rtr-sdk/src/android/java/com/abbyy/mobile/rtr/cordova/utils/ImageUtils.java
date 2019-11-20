// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for handling & storing images
 */
public class ImageUtils {

	// Synchronously save bitmap to file
	public static void saveBitmap( Bitmap image, File file ) throws IOException
	{
		try( FileOutputStream fos = new FileOutputStream( file ) ) {
			if( ImageCaptureSettings.exportType == ImageCaptureSettings.ExportType.PNG ) {
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

	public static File getCaptureSessionPageFile( int pageIndex, Context context )
	{
		File captureSessionDir = getCaptureSessionDir( context );
		if( !captureSessionDir.exists() ) {
			captureSessionDir.mkdir();
		}
		return new File( captureSessionDir, "page_" + ( pageIndex + 1 ) + "." + getFileExtension() );
	}

	private static String getFileExtension()
	{
		switch( ImageCaptureSettings.exportType ) {
			case JPG:
			case PDF:
				return "jpg";
			case PNG:
				return "png";
		}
		return "jpg";
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
