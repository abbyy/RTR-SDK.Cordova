// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;

import java.io.File;
import java.io.IOException;

public class PageHolder {
	private int pageNumber;
	private File pageFile;
	private Point[] documentBoundary;
	private Point frameSize;
	private String base64;

	public PageHolder( int capturePageNumber )
	{
		pageNumber = capturePageNumber;
	}

	public File getPageFile()
	{
		return pageFile;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public void saveToFile( Bitmap pageImage, Context context ) throws IOException
	{
		this.pageFile = ImageUtils.getCaptureSessionPageFile( pageNumber, context );

		ImageUtils.saveBitmap( pageImage, pageFile );
	}

	public void setDocumentBoundary( Point[] documentBoundary )
	{
		this.documentBoundary = documentBoundary;
	}

	public Point[] getDocumentBoundary()
	{
		return documentBoundary;
	}

	public Point getFrameSize()
	{
		return frameSize;
	}

	public void setFrameSize( Point frameSize )
	{
		this.frameSize = frameSize;
	}

	public void setBase64( String base64 )
	{
		this.base64 = base64;
	}

	public String getBase64()
	{
		return base64;
	}
}
