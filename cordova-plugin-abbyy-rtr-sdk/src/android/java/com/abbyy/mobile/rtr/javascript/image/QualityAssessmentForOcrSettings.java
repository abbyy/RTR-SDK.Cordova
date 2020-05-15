// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import android.graphics.Point;

import java.util.Arrays;

public class QualityAssessmentForOcrSettings {

	Point[] documentBoundary = null;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		QualityAssessmentForOcrSettings settings = (QualityAssessmentForOcrSettings) o;
		return Arrays.equals( documentBoundary, settings.documentBoundary );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( documentBoundary );
	}
}
