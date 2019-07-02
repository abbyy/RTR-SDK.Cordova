package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Point;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.rtrcordovasample.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiCaptureResult {

	public static HashMap<String, Object> getJsonResult( SparseArray<PageHolder> pages, Context context, boolean automaticallyStopped )
	{
		HashMap<String, Object> json = new HashMap<>();
		int pageCount = pages.size();

		List<HashMap<String, Object>> pagesPaths = new ArrayList<>();
		for( int i = 0; i < pageCount; ++i ) {
			PageHolder pageHolder = pages.valueAt( i );
			HashMap<String, Object> pageJson = getPageInfoJson( pageHolder );
			pagesPaths.add( pageJson );
		}
		json.put( "images", pagesPaths );
		if( ImageCaptureSettings.exportType == ImageCaptureSettings.ExportType.PDF ) {
			HashMap<String, Object> pdfInfo = getPdfInfoJson( context, pageCount );
			json.put( "pdfinfo", pdfInfo );
		}
		HashMap<String, Object> resultInfo = new HashMap<>();
		if( !automaticallyStopped ) {
			resultInfo.put( "userAction", "Manually Stopped" );
		}
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static HashMap<String, Object> getPageInfoJson( PageHolder pageHolder )
	{
		HashMap<String, Object> pageJson = new HashMap<>();
		if (pageHolder.getBase64() != null) {
			pageJson.put( "base64", pageHolder.getBase64() );
		}
		pageJson.put( "filePath", pageHolder.getPageFile().getPath() );
		HashMap<String, Object> pageResultJson = new HashMap<>();
		pageResultJson.put( "cropped", pageHolder.isCropped() );
		pageResultJson.put( "exportType", ImageCaptureSettings.exportType.toString() );
		if( pageHolder.getDocumentBoundary() != null ) {
			StringBuilder boundary = new StringBuilder();
			for( Point p : pageHolder.getDocumentBoundary() ) {
				boundary.append( p.x ).append( " " ).append( p.y ).append( " " );
			}
			if( boundary.length() > 0 ) {
				boundary.deleteCharAt( boundary.length() - 1 );
			}
			pageResultJson.put( "documentBoundary", boundary.toString() );
		}
		if( pageHolder.getFrameSize() != null ) {
			String frameSize = pageHolder.getFrameSize().x + " " + pageHolder.getFrameSize().y;
			pageResultJson.put( "frameSize", frameSize );
		}
		pageJson.put( "resultInfo", pageResultJson );
		return pageJson;
	}

	private static HashMap<String, Object> getPdfInfoJson( Context context, int pageCount )
	{
		File pdfFile = ImageUtils.getCaptureSessionPdfFile( context );
		HashMap<String, Object> pdfInfo = new HashMap<>();
		pdfInfo.put( "path", pdfFile.getPath() );
		pdfInfo.put( "pagesCount", pageCount );
		pdfInfo.put( "compressionType", ImageCaptureSettings.compressionType.toString() );
		pdfInfo.put( "compressionLevel", ImageCaptureSettings.compressionLevel.toString() );
		return pdfInfo;
	}

	public static boolean shouldReturnBase64()
	{
		return ImageCaptureSettings.destination == ImageCaptureSettings.Destination.BASE64 &&
			!ImageCaptureSettings.showResultOnCapture && ImageCaptureSettings.pageCount == 1 &&
			ImageCaptureSettings.exportType != ImageCaptureSettings.ExportType.PDF;
	}

	public static HashMap<String, Object> getErrorJsonResult( Context context )
	{
		HashMap<String, Object> json = new HashMap<>();
		json.put( "error", getErrorJson( context ) );
		HashMap<String, Object> resultInfo = new HashMap<>();
		resultInfo.put( "userAction", "Canceled" );
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static HashMap<String, Object> getErrorJson( Context context )
	{
		HashMap<String, Object> pdfInfo = new HashMap<>();
		pdfInfo.put( "description", context.getString( R.string.unknown_error ) );
		return pdfInfo;
	}
}
