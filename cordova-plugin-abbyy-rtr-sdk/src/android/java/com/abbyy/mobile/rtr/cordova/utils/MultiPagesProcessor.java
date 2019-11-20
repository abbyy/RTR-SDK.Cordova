// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Pair;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.multipage.MultiCaptureResult;
import com.abbyy.mobile.rtr.cordova.multipage.PageHolder;
import com.abbyy.mobile.uicomponents.scenario.MultiPageImageCaptureScenario;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Utility class to save the result of the multipage image capture scenario to files
 */
public class MultiPagesProcessor extends BackgroundWorker<Boolean, Boolean> {
	private final MultiPageImageCaptureScenario.Result result;

	private WeakReference<Context> contextRef;

	public MultiPagesProcessor( MultiPageImageCaptureScenario.Result result, Context context, WeakReference<Callback<Boolean, Boolean>> callback )
	{
		super( callback );
		this.result = result;
		this.contextRef = new WeakReference<>( context );
	}

	public Boolean processPages( boolean finishedSuccessfully )
	{
		try {
			List<String> pages = result.getPages();
			if( !finishedSuccessfully ) {
				return !pages.isEmpty();
			}
			if( ImageCaptureSettings.exportType == ImageCaptureSettings.ExportType.PDF ) {
				File pdfFile = ImageUtils.getCaptureSessionPdfFile( contextRef.get() );
				try( IImagingCoreAPI api = RtrManager.getImagingCoreAPI() ) {
					try( FileOutputStream fos = new FileOutputStream( pdfFile ) ) {
						storePagesAndAddToPdf( pages, api, fos );
					}
				} catch( Exception e ) {
					e.printStackTrace();
				}
			} else {
				int pageNumber = 0;
				for( String pageId : pages ) {
					Pair<PageHolder, Bitmap> page = getPageFromResult( pageNumber++, pageId );
					page.first.saveToFile( page.second, contextRef.get() );
					page.second.recycle();
					pageNumber++;
				}
			}
			if( MultiCaptureResult.shouldReturnBase64() && pages.size() == 1 ) {
				PageHolder page = RtrManager.getImageCaptureResult().valueAt( 0 );
				page.setBase64( ImageUtils.convertFileToBase64( page.getPageFile() ) );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return false;
	}

	private void storePagesAndAddToPdf( List<String> pages, IImagingCoreAPI api, FileOutputStream fos ) throws Exception
	{
		try( IImagingCoreAPI.ExportToPdfOperation operation = api.createExportToPdfOperation( fos ) ) {
			operation.Compression = ImageCaptureSettings.compressionLevel;
			operation.CompressionType = ImageCaptureSettings.pdfCompressionType;
			int pageNumber = 0;
			for( String pageId : pages ) {
				Pair<PageHolder, Bitmap> page = getPageFromResult( pageNumber++, pageId );
				operation.addPage( page.second );
				page.first.saveToFile( page.second, contextRef.get() );
				page.second.recycle();
			}
		}
	}

	private Pair<PageHolder, Bitmap> getPageFromResult( int pageNumber, String pageId ) throws Exception
	{
		Bitmap pageImage = result.loadImage( pageId );
		PageHolder pageHolder = new PageHolder( pageNumber );
		pageHolder.setDocumentBoundary( result.loadBoundary( pageId ) );
		if( pageImage != null ) {
			pageHolder.setFrameSize( new Point( pageImage.getWidth(), pageImage.getHeight() ) );
		}
		RtrManager.getImageCaptureResult().append( pageNumber, pageHolder );
		return new Pair<>( pageHolder, pageImage );
	}

	public MultiPageImageCaptureScenario.Result getResult()
	{
		return result;
	}
}
