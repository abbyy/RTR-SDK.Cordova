// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Point;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to build the JSON results
 */
public class MultiCaptureResult {

	public static HashMap<String, Object> getJsonResult( SparseArray<PageHolder> pages, Context context )
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
			json.put( "pdfInfo", pdfInfo );
		}
		HashMap<String, Object> resultInfo = new HashMap<>();
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static HashMap<String, Object> getPageInfoJson( PageHolder pageHolder )
	{
		HashMap<String, Object> pageJson = new HashMap<>();
		if( pageHolder.getBase64() != null ) {
			pageJson.put( "base64", pageHolder.getBase64() );
		}
		pageJson.put( "filePath", pageHolder.getPageFile().getPath() );
		HashMap<String, Object> pageResultJson = new HashMap<>();
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
		pdfInfo.put( "filePath", pdfFile.getPath() );
		pdfInfo.put( "pagesCount", pageCount );
		pdfInfo.put( "pdfCompressionType", ImageCaptureSettings.pdfCompressionType.toString() );
		pdfInfo.put( "compressionLevel", ImageCaptureSettings.compressionLevel.toString() );
		return pdfInfo;
	}

	public static boolean shouldReturnBase64()
	{
		return ImageCaptureSettings.destination == ImageCaptureSettings.Destination.BASE64 &&
			ImageCaptureSettings.exportType != ImageCaptureSettings.ExportType.PDF;
	}

	public static HashMap<String, Object> getErrorJsonResult( Exception exception, Context context )
	{
		HashMap<String, Object> json = new HashMap<>();
		if( exception != null ) {
			json.put( "error", getErrorJson( exception, context ) );
		}
		HashMap<String, Object> resultInfo = new HashMap<>();
		resultInfo.put( "userAction", "Canceled" );
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static HashMap<String, Object> getErrorJson( Exception exception, Context context )
	{
		HashMap<String, Object> pdfInfo = new HashMap<>();
		String description;
		if( exception.getMessage() != null ) {
			description = exception.getMessage();
		} else {
			description = context.getString( ResourcesUtils.getResId( "string", "unknown_error", context ) );
		}
		pdfInfo.put( "description", description );
		return pdfInfo;
	}
}
