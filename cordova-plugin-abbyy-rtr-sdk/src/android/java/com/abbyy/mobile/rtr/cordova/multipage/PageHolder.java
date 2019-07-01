package com.abbyy.mobile.rtr.cordova.multipage;

import android.content.Context;
import android.graphics.Bitmap;

import com.abbyy.mobile.rtr.cordova.utils.ImageSaver;
import com.abbyy.mobile.rtr.cordova.utils.ImageUtils;
import com.abbyy.rtrcordovasample.R;

import java.io.File;

public class PageHolder {
	private int pageNumber;
	private File pageFile;
	private Bitmap pageImage;

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
		String pageFilePath = pageFile.getPath();

		// Create page miniature
		Bitmap pageMiniature = ImageUtils.getMiniature(
			pageImage,
			context.getResources().getDimensionPixelSize( R.dimen.miniature_size )
		);

		ImageSaver tempImageSaver = new ImageSaver( pageImage, pageFilePath, callback );
		pageImage = null;
		tempImageSaver.execute();
		return pageMiniature;
	}
}
