// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.util.Size;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.IImagingCoreAPI.ExportOperation.CompressionType;
import com.abbyy.mobile.rtr.javascript.SharedEngine;
import com.abbyy.mobile.rtr.javascript.utils.BackgroundWorker;
import com.abbyy.mobile.uicomponents.scenario.MultiPageImageCaptureScenario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

/**
 * Utility class to save the result of the multipage image capture scenario to files
 */
public class MultiPagesProcessor extends BackgroundWorker<Void, ImageCaptureResult> {

	private final MultiPageImageCaptureScenario.Result scenarioResult;
	@SuppressLint( "StaticFieldLeak" ) // Application context
	private final Context context;
	private final ImageCaptureSettings imageCaptureSettings;

	public MultiPagesProcessor(
		MultiPageImageCaptureScenario.Result scenarioResult,
		Application application,
		WeakReference<Callback<Void, ImageCaptureResult>> callback,
		ImageCaptureSettings imageCaptureSettings
	)
	{
		super( callback );
		this.scenarioResult = scenarioResult;
		this.context = application;
		this.imageCaptureSettings = imageCaptureSettings;
	}

	public ImageCaptureResult processPages() throws Exception
	{
		List<String> pages = scenarioResult.getPages();
		ImageCaptureResult imageCaptureResult = new ImageCaptureResult();
		imageCaptureResult.setPages( new Page[pages.size()] );

		if( imageCaptureSettings.exportType == ExportType.PDF ) {
			File pdfFile = exportPagesToPdf( pages, imageCaptureResult );
			imageCaptureResult.setPdfFile( pdfFile );
		} else {
			copyPagesToFiles( pages, imageCaptureResult );
		}

		return imageCaptureResult;
	}

	private void copyPagesToFiles( List<String> pages, ImageCaptureResult imageCaptureResult ) throws Exception
	{
		int pageNumber = 0;
		for( String pageId : pages ) {
			Pair<Page, Bitmap> page = getPageFromScenarioResult( imageCaptureResult, pageNumber, pageId );

			File file = getCaptureSessionPageFile( pageId );
			saveBitmap( page.second, file );
			page.first.setFile( file );
			page.second.recycle();
			pageNumber++;
		}
	}

	private File exportPagesToPdf(
		List<String> pages,
		ImageCaptureResult imageCaptureResult
	) throws Exception
	{
		File pdfFile = getCaptureSessionPdfFile();
		try( IImagingCoreAPI api = SharedEngine.get().createImagingCoreAPI() ) {
			try( FileOutputStream fos = new FileOutputStream( pdfFile ) ) {
				exportPagesToPdfFileOutputStream( imageCaptureResult, pages, api, fos );
			}
		}
		return pdfFile;
	}

	private void exportPagesToPdfFileOutputStream(
		@NonNull ImageCaptureResult imageCaptureResult,
		@NonNull List<String> pages,
		@NonNull IImagingCoreAPI api,
		@NonNull FileOutputStream pdfFos
	) throws Exception
	{
		try( IImagingCoreAPI.ExportToPdfOperation operation = api.createExportToPdfOperation( pdfFos ) ) {
			operation.Compression = imageCaptureSettings.compressionLevel;
			operation.CompressionType = CompressionType.Jpg;
			int pageNumber = 0;
			for( String pageId : pages ) {
				Pair<Page, Bitmap> page = getPageFromScenarioResult( imageCaptureResult, pageNumber, pageId );
				operation.addPage( page.second );
				page.second.recycle();
				pageNumber++;
			}
		}
	}

	private File getCaptureSessionPdfFile() throws IOException
	{
		File captureSessionDir = getCaptureSessionDir();
		if( !captureSessionDir.exists() ) {
			if( !captureSessionDir.mkdir() ) {
				throw new IOException( "Can't create directory" );
			}
		}
		return new File( captureSessionDir, UUID.randomUUID().toString() + ".pdf" );
	}

	private File getCaptureSessionPageFile( String pageId ) throws IOException
	{
		File captureSessionDir = getCaptureSessionDir();
		if( !captureSessionDir.exists() ) {
			if( !captureSessionDir.mkdir() ) {
				throw new IOException( "Can't create directory" );
			}
		}
		return new File( captureSessionDir, "page_" + pageId + "." + getFileExtension() );
	}

	private File getCaptureSessionDir()
	{
		return new File( context.getFilesDir(), "pages" );
	}

	private String getFileExtension()
	{
		switch( imageCaptureSettings.exportType ) {
			case JPG:
			case PDF:
				return "jpg";
			case PNG:
				return "png";
		}
		return "jpg";
	}

	private void saveBitmap( @NonNull Bitmap image, @NonNull File file ) throws Exception
	{
		try( FileOutputStream fos = new FileOutputStream( file ) ) {
			try( IImagingCoreAPI api = SharedEngine.get().createImagingCoreAPI() ) {
				saveBitmapToFileWithApi( image, fos, api );
			}
		}
	}

	private void saveBitmapToFileWithApi(
		@NonNull Bitmap image,
		@NonNull FileOutputStream fos,
		@NonNull IImagingCoreAPI api
	) throws Exception
	{
		if( imageCaptureSettings.exportType == ExportType.PNG ) {
			try( IImagingCoreAPI.ExportToPngOperation operation = api.createExportToPngOperation( fos ) ) {
				operation.addPage( image );
			}
		} else {
			try( IImagingCoreAPI.ExportToJpgOperation operation = api.createExportToJpgOperation( fos ) ) {
				operation.Compression = imageCaptureSettings.compressionLevel;
				operation.addPage( image );
			}
		}
	}

	private Pair<Page, Bitmap> getPageFromScenarioResult(
		@NonNull ImageCaptureResult imageCaptureResult,
		int pageNumber,
		@NonNull String pageId
	) throws Exception
	{
		Bitmap pageImage = scenarioResult.loadImage( pageId );
		Page page = new Page();
		if( pageImage != null ) {
			page.setImageSize( new Size( pageImage.getWidth(), pageImage.getHeight() ) );
		}
		imageCaptureResult.getPages()[pageNumber] = page;
		return new Pair<>( page, pageImage );
	}
}
