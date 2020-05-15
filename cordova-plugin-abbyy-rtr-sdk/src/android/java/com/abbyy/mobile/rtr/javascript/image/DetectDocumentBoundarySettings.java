// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import android.graphics.Rect;

import com.abbyy.mobile.rtr.IImagingCoreAPI;

import java.util.Objects;

public class DetectDocumentBoundarySettings {
	public Rect areaOfInterest = null;
	public IImagingCoreAPI.DetectDocumentBoundaryOperation.DetectionMode detectionMode =
		IImagingCoreAPI.DetectDocumentBoundaryOperation.DetectionMode.Default;
	public float documentWidth = 0f;
	public float documentHeight = 0f;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		DetectDocumentBoundarySettings that = (DetectDocumentBoundarySettings) o;
		return Objects.equals( areaOfInterest, that.areaOfInterest ) &&
			detectionMode == that.detectionMode &&
			Objects.equals( documentWidth, that.documentWidth ) &&
			Objects.equals( documentHeight, that.documentHeight );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( areaOfInterest, detectionMode, documentWidth, documentHeight );
	}
}
