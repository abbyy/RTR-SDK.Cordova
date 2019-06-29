package com.abbyy.mobile.rtr.cordova.multipage;

public class CaptureTask {
	private int pageNumber;
	private CaptureMode captureMode;

	public CaptureTask( int pageNumber, CaptureMode captureMode )
	{
		this.pageNumber = pageNumber;
		this.captureMode = captureMode;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public CaptureMode getCaptureMode()
	{
		return captureMode;
	}
}
