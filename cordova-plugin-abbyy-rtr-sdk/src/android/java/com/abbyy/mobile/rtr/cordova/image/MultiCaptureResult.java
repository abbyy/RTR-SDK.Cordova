// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.content.Context;
import android.graphics.Point;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.image.ImageCaptureResult;
import com.abbyy.mobile.rtr.cordova.image.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.image.Page;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to build the JSON results
 */
public class MultiCaptureResult {

	public static HashMap<String, Object> getJsonResult( ImageCaptureResult result, ImageCaptureSettings settings, Context context )
	{
		HashMap<String, Object> json = new HashMap<>();
		int pageCount = result.getPages().length;

		List<HashMap<String, Object>> pagesPaths = new ArrayList<>();
		for( int i = 0; i < pageCount; ++i ) {
			Page pageHolder = result.getPages()[i];
			HashMap<String, Object> pageJson = getPageInfoJson( pageHolder, settings );
			pagesPaths.add( pageJson );
		}
		json.put( "images", pagesPaths );
		if( settings.exportType == ImageCaptureSettings.ExportType.PDF ) {
			HashMap<String, Object> pdfInfo = getPdfInfoJson( context, settings );
			json.put( "pdfInfo", pdfInfo );
		}
		HashMap<String, Object> resultInfo = new HashMap<>();
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static HashMap<String, Object> getPageInfoJson( Page pageHolder, ImageCaptureSettings imageCaptureSettings )
	{
		HashMap<String, Object> pageJson = new HashMap<>();
		if( shouldReturnBase64( imageCaptureSettings ) ) {
			try {
				pageJson.put( "base64", ImageUtils.convertFileToBase64( pageHolder.getFile() ) );
			} catch( IOException e ) {
				e.printStackTrace();
			}
		}
		pageJson.put( "filePath", pageHolder.getFile().getPath() );
		HashMap<String, Object> pageResultJson = new HashMap<>();
		pageResultJson.put( "exportType", imageCaptureSettings.exportType.toString() );
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
			String frameSize = pageHolder.getFrameSize().getWidth() + " " + pageHolder.getFrameSize().getHeight();
			pageResultJson.put( "frameSize", frameSize );
		}
		pageJson.put( "resultInfo", pageResultJson );
		return pageJson;
	}

	private static HashMap<String, Object> getPdfInfoJson( Context context, ImageCaptureSettings imageCaptureSettings )
	{
		File pdfFile = ImageUtils.getCaptureSessionPdfFile( context );
		HashMap<String, Object> pdfInfo = new HashMap<>();
		pdfInfo.put( "filePath", pdfFile.getPath() );
		pdfInfo.put( "pagesCount", imageCaptureSettings.requiredPageCount );
		pdfInfo.put( "compressionLevel", imageCaptureSettings.compressionLevel.toString() );
		return pdfInfo;
	}

	public static boolean shouldReturnBase64( ImageCaptureSettings imageCaptureSettings )
	{
		return imageCaptureSettings.destination == ImageCaptureSettings.Destination.BASE64 &&
			imageCaptureSettings.exportType != ImageCaptureSettings.ExportType.PDF;
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
