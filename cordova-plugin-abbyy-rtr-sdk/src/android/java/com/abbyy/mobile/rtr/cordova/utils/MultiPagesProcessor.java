// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.multipage.MultiCaptureResult;
import com.abbyy.mobile.rtr.cordova.multipage.PageHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

// Utility class to asynchronously save bitmap to file
public class MultiPagesProcessor extends AsyncTask<Void, Void, Void> {
	public interface Callback {
		void onProcessed();
	}

	private SparseArray<PageHolder> pages;
	private WeakReference<Context> contextRef;
	private Callback callback;

	public MultiPagesProcessor( SparseArray<PageHolder> pages, Context context, Callback callback )
	{
		this.pages = pages;
		this.contextRef = new WeakReference<>( context );
		this.callback = callback;
	}

	@Override
	protected Void doInBackground( Void... args )
	{
		if( ImageCaptureSettings.exportType == ImageCaptureSettings.ExportType.PDF ) {
			File pdfFile = ImageUtils.getCaptureSessionPdfFile( contextRef.get() );
			try( IImagingCoreAPI api = RtrManager.getImagingCoreAPI() ) {
				try( FileOutputStream fos = new FileOutputStream( pdfFile ) ) {
					try( IImagingCoreAPI.ExportToPdfOperation operation = api.createExportToPdfOperation( fos ) ) {
						operation.Compression = ImageCaptureSettings.compressionLevel;
						operation.CompressionType = ImageCaptureSettings.pdfCompressionType;
						for( int i = 0; i < pages.size(); ++i ) {
							PageHolder page = pages.valueAt( i );
							Bitmap pageImage = ImageUtils.loadBitmap( page.getPageFile() );
							operation.addPage( pageImage );
						}
					}
				}
			} catch( Exception e ) {
				e.printStackTrace();
			}
		}
		if( MultiCaptureResult.shouldReturnBase64() && pages.size() == 1 ) {
			PageHolder page = pages.valueAt( 0 );
			try {
				page.setBase64( ImageUtils.convertFileToBase64( page.getPageFile() ) );
			} catch( IOException e ) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute( Void arg )
	{
		callback.onProcessed();
	}
}
