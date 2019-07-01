package com.abbyy.mobile.rtr.cordova.multipage;

import android.util.SparseArray;

public class CaptureResult {
	private int capturedPageNumber;
	private boolean finishCapture;

	public CaptureResult( int capturedPageNumber, boolean finishCapture )
	{
		this.capturedPageNumber = capturedPageNumber;
		this.finishCapture = finishCapture;
	}

	public boolean isFinishCapture()
	{
		return finishCapture;
	}

	public int getCapturedPageNumber()
	{
		return capturedPageNumber;
	}
}
