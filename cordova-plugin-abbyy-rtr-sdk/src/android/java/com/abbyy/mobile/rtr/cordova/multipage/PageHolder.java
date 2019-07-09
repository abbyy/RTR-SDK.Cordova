package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

import com.abbyy.mobile.rtr.cordova.utils.ImageSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.rtrcordovasample.R;

import java.io.File;

public class PageHolder {
	private int pageNumber;
	private File pageFile;
	private Bitmap pageImage;
	private String pageBase64;
	private boolean cropped = false;
	private Point[] documentBoundary;
	private Point frameSize;
	private String base64;

	public PageHolder( int capturePageNumber, Bitmap pageImage )
	{
		pageNumber = capturePageNumber;
		this.pageImage = pageImage;
	}

	public File getPageFile()
	{
		return pageFile;
	}

	public void setPageFile( File pageFile )
	{
		this.pageFile = pageFile;
	}

	public Bitmap getPageImage()
	{
		return pageImage;
	}

	public void setPageImage( Bitmap pageImage )
	{
		this.pageImage = pageImage;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public Bitmap saveToFile( Context context, ImageSaver.Callback callback )
	{
		if (pageImage == null) {
			return null;
		}
		this.pageFile = ImageUtils.getCaptureSessionPageFile( pageNumber, context );

		// Create page miniature
		Bitmap pageMiniature = ImageUtils.getMiniature(
			pageImage,
			context.getResources().getDimensionPixelSize( R.dimen.miniature_size )
		);

		ImageSaver tempImageSaver = new ImageSaver( pageImage, pageFile, callback );
		pageImage = null;
		tempImageSaver.execute();
		return pageMiniature;
	}

	public boolean isCropped()
	{
		return cropped;
	}

	public void setCropped( boolean isCropped )
	{
		cropped = isCropped;
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

	public boolean isSaved()
	{
		return pageImage == null && pageFile != null;
	}
}
