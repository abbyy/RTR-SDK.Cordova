package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.cordova.ImageCaptureSettings;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.utils.ImagePdfSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiCaptureResult {

	public static HashMap<String, Object> getJsonResult( SparseArray<PageHolder> pages, Context context )
	{
		HashMap<String, Object> json = new HashMap<>();
		int pageCount = pages.size();
		if( shouldReturnBase64() ) {

		} else {
			List<HashMap<String, Object>> pagesPaths = new ArrayList<>();
			for( int i = 0; i < pageCount; ++i ) {
				PageHolder pageHolder = pages.valueAt( i );
				HashMap<String, Object> pageJson = new HashMap<>();
				pageJson.put( "path", pageHolder.getPageFile().getPath() );
				pagesPaths.add( pageJson );
			}
			json.put( "pages", pagesPaths );
		}
		if( ImageCaptureSettings.exportType == ImageCaptureSettings.ExportType.PDF ) {
			File pdfFile = ImageUtils.getCaptureSessionPdfFile( context );
			HashMap<String, Object> pdfInfo = new HashMap<>();
			pdfInfo.put( "path", pdfFile.getPath() );
			pdfInfo.put( "pagesCount", pageCount );
			pdfInfo.put( "compressionType", ImageCaptureSettings.compressionType.toString() );
			pdfInfo.put( "compressionLevel", ImageCaptureSettings.compressionLevel.toString() );
			json.put( "pdfinfo", pdfInfo );
		}
		return json;
	}

	private static boolean shouldReturnBase64()
	{
		return ImageCaptureSettings.destination == ImageCaptureSettings.Destination.BASE64 && ImageCaptureSettings.pageCount == 1;
	}
}
