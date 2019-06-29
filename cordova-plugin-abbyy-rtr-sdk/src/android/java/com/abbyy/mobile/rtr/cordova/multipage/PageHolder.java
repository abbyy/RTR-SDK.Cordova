package com.abbyy.mobile.rtr.cordova.multipage;

import android.graphics.Bitmap;

import java.io.File;

public class PageHolder {
	private File pageFile;
	private Bitmap pageImage;

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
}
