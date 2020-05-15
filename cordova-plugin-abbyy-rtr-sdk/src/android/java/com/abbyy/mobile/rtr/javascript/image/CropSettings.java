// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import android.graphics.Point;

import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;

public class CropSettings {
	@NonNull
	public Point[] documentBoundary;
	@NonNull
	public ExportSettings exportSettings;
	public float documentWidth = 0f;
	public float documentHeight = 0f;

	public CropSettings( @NonNull Point[] documentBoundary, @NonNull ExportSettings exportSettings )
	{
		this.documentBoundary = documentBoundary;
		this.exportSettings = exportSettings;
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		CropSettings that = (CropSettings) o;
		return Arrays.equals( documentBoundary, that.documentBoundary ) &&
			exportSettings.equals( that.exportSettings ) &&
			Objects.equals( documentWidth, that.documentWidth ) &&
			Objects.equals( documentHeight, that.documentHeight );
	}

	@Override
	public int hashCode()
	{
		int result = Objects.hash( exportSettings, documentWidth, documentHeight );
		result = 31 * result + Arrays.hashCode( documentBoundary );
		return result;
	}
}
