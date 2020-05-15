// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class ImageCaptureResult implements Parcelable {

	private Page[] pages;
	private File pdfFile;

	public ImageCaptureResult() { }

	private ImageCaptureResult( Parcel source )
	{
		pages = source.createTypedArray( Page.CREATOR );
		pdfFile = (File) source.readSerializable();
	}

	public void setPages( Page[] pages )
	{
		this.pages = pages;
	}

	public Page[] getPages()
	{
		return pages;
	}

	public File getPdfFile()
	{
		return pdfFile;
	}

	public void setPdfFile( File pdfFile )
	{
		this.pdfFile = pdfFile;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		dest.writeTypedArray( pages, flags );
		dest.writeSerializable( pdfFile );
	}

	public static final Creator<ImageCaptureResult> CREATOR = new Creator<ImageCaptureResult>() {
		@Override
		public ImageCaptureResult createFromParcel( Parcel source )
		{
			return new ImageCaptureResult( source );
		}

		@Override
		public ImageCaptureResult[] newArray( int size )
		{
			return new ImageCaptureResult[size];
		}
	};

}
