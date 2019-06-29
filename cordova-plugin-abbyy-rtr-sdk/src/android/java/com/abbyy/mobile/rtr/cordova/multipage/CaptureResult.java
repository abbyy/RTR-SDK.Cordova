package com.abbyy.mobile.rtr.cordova.multipage;

import android.util.SparseArray;

public class CaptureResult {
	private SparseArray<PageHolder> pages;
	private int capturedPageNumber;
	private CaptureMode captureMode;
	private boolean finishCapture;

	public CaptureResult( SparseArray<PageHolder> pages, int capturedPageNumber, CaptureMode captureMode, boolean finishCapture )
	{
		this.pages = pages;
		this.capturedPageNumber = capturedPageNumber;
		this.captureMode = captureMode;
		this.finishCapture = finishCapture;
	}

	public SparseArray<PageHolder> getPages()
	{
		return pages;
	}

	public boolean isFinishCapture()
	{
		return finishCapture;
	}

	public int getCapturedPageNumber()
	{
		return capturedPageNumber;
	}

	public CaptureMode getCaptureMode()
	{
		return captureMode;
	}
}
