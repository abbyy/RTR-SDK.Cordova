// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import com.abbyy.mobile.rtr.IImagingCoreAPI;

import java.util.Objects;

public class ExportSettings {

	public Destination destination = Destination.Base64;
	public ExportType exportType = ExportType.JPG;
	public IImagingCoreAPI.ExportOperation.Compression compression = IImagingCoreAPI.ExportOperation.Compression.Low;
	public String filePath = null;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		ExportSettings that = (ExportSettings) o;
		return destination == that.destination &&
			exportType == that.exportType &&
			compression == that.compression &&
			Objects.equals( filePath, that.filePath );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( destination, exportType, compression, filePath );
	}
}
