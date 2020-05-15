// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.content.Context;

import com.abbyy.mobile.rtr.javascript.JSConstants;
import com.abbyy.mobile.rtr.javascript.image.Destination;
import com.abbyy.mobile.rtr.javascript.utils.FileUtils;
import com.abbyy.mobile.rtr.javascript.utils.UriScheme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to build the JSON results
 */
public class MultiCaptureResult {
	private static final String URI_PREFIX = "uriPrefix";

	public static HashMap<String, Object> getJsonResult( ImageCaptureResult result, ImageCaptureSettings settings, Context context )
	{
		HashMap<String, Object> json = new HashMap<>();
		int pageCount = result.getPages().length;

		if( settings.exportType == ExportType.PDF ) {
			HashMap<String, Object> pdfInfo = getPdfInfoJson( context, result, settings );
			json.put( "pdfInfo", pdfInfo );
		} else {
			List<HashMap<String, Object>> pagesPaths = new ArrayList<>();
			for( int i = 0; i < pageCount; ++i ) {
				Page pageHolder = result.getPages()[i];
				HashMap<String, Object> pageJson = getPageInfoJson( pageHolder, settings );
				pagesPaths.add( pageJson );
			}
			json.put( "images", pagesPaths );
		}
		HashMap<String, Object> resultInfo = new HashMap<>();
		resultInfo.put( URI_PREFIX, getUriPrefix( settings ) );
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static String getUriPrefix( ImageCaptureSettings settings )
	{
		String uriPrefix;
		if( settings.destination == Destination.File ) {
			uriPrefix = UriScheme.FILE;
		} else {
			switch( settings.exportType ) {
				case JPG:
					uriPrefix = UriScheme.BASE_64_JPEG;
					break;
				case PNG:
					uriPrefix = UriScheme.BASE_64_PNG;
					break;
				case PDF:
					uriPrefix = UriScheme.BASE_64_PDF;
					break;
				default:
					throw new IllegalStateException( "Unknown export type" );
			}
		}
		return uriPrefix;
	}

	private static HashMap<String, Object> getPageInfoJson( Page pageHolder, ImageCaptureSettings imageCaptureSettings )
	{
		HashMap<String, Object> pageJson = new HashMap<>();
		if( imageCaptureSettings.destination == Destination.Base64 ) {
			try {
				pageJson.put( "base64", FileUtils.convertFileToBase64( pageHolder.getFile() ) );

				if( !pageHolder.getFile().delete() ) {
					throw new IOException( "Can't delete page file" );
				}
			} catch( IOException e ) {
				e.printStackTrace();
			}
		} else {
			pageJson.put( "filePath", pageHolder.getFile().getPath() );
		}
		HashMap<String, Object> pageResultJson = new HashMap<>();
		pageResultJson.put( "exportType", getExportTypeResultString( imageCaptureSettings.exportType) );
		if( pageHolder.getImageSize() != null ) {
			HashMap<String, Object> sizeJson = new HashMap<>();
			sizeJson.put( JSConstants.WIDTH, pageHolder.getImageSize().getWidth() );
			sizeJson.put( JSConstants.HEIGHT, pageHolder.getImageSize().getHeight() );
			pageResultJson.put( JSConstants.IMAGE_SIZE, sizeJson );
		}
		pageJson.put( "resultInfo", pageResultJson );
		return pageJson;
	}

	private static String getExportTypeResultString( ExportType exportType )
	{
		switch( exportType ) {
			case JPG:
				return "Jpg";
			case PNG:
				return "Png";
			case PDF:
				return "Pdf";
			default:
				throw new RuntimeException( "Unknown export type" );
		}
	}

	private static HashMap<String, Object> getPdfInfoJson( Context context, ImageCaptureResult result, ImageCaptureSettings imageCaptureSettings )
	{
		File pdfFile = result.getPdfFile();
		HashMap<String, Object> pdfInfo = new HashMap<>();
		if( imageCaptureSettings.destination == Destination.Base64 ) {
			try {
				pdfInfo.put( "base64", FileUtils.convertFileToBase64( pdfFile ) );
				if( !pdfFile.delete() ) {
					throw new IOException( "Can't delete page file" );
				}
			} catch( IOException e ) {
				e.printStackTrace();
			}
		} else {
			pdfInfo.put( "filePath", pdfFile.getPath() );
		}
		pdfInfo.put( "pagesCount", result.getPages().length );
		return pdfInfo;
	}

	public static HashMap<String, Object> getCanceledJsonResult()
	{
		HashMap<String, Object> json = new HashMap<>();
		HashMap<String, Object> resultInfo = new HashMap<>();
		resultInfo.put( "userAction", "Canceled" );
		json.put( "resultInfo", resultInfo );
		return json;
	}

	public static HashMap<String, Object> getErrorJsonResult( String errorMessage )
	{
		HashMap<String, Object> json = new HashMap<>();
		json.put( "error", getErrorJson( errorMessage ) );
		HashMap<String, Object> resultInfo = new HashMap<>();
		resultInfo.put( "userAction", "Canceled" );
		json.put( "resultInfo", resultInfo );
		return json;
	}

	private static HashMap<String, Object> getErrorJson( String errorMessage )
	{
		HashMap<String, Object> errorInfo = new HashMap<>();
		errorInfo.put( "description", errorMessage );
		return errorInfo;
	}
}
