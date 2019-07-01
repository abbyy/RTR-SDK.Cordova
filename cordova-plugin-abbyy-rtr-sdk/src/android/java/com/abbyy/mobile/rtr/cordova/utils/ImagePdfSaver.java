// ABBYY ® Mobile Imaging UI Components © 2018 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.multipage.PageHolder;

import java.io.File;
import java.io.FileOutputStream;

// Utility class to asynchronously save bitmap to file
public class ImagePdfSaver extends AsyncTask<Void, Void, Void> {
	public interface Callback {
		void onSaved( File pdfFile );
	}

	private SparseArray<PageHolder> pages;
	private File pdfFile;
	private Callback callback;

	public ImagePdfSaver( SparseArray<PageHolder> pages, File pdfFile, Callback callback )
	{
		this.pages = pages;
		this.pdfFile = pdfFile;
		this.callback = callback;
	}

	@Override
	protected Void doInBackground( Void... args )
	{
		try( IImagingCoreAPI api = RtrManager.getImagingCoreAPI() ) {
			try( FileOutputStream fos = new FileOutputStream( pdfFile ) ) {
				try( IImagingCoreAPI.ExportToPdfOperation operation = api.createExportToPdfOperation( fos ) ) {
					operation.Compression = ImageCaptureSettings.compressionLevel;
					operation.CompressionType = ImageCaptureSettings.compressionType;
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
		return null;
	}

	@Override
	protected void onPostExecute( Void arg )
	{
		callback.onSaved( pdfFile );
	}
}
