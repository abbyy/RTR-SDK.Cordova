// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

import java.io.File;

public class Page implements Parcelable {

	private File file;
	private Size imageSize;

	public Page() {}

	private Page( Parcel source )
	{
		file = (File) source.readSerializable();
		imageSize = source.readSize();

		int[] boundary = new int[8];
		source.readIntArray( boundary );
	}

	public void setFile( File file )
	{
		this.file = file;
	}

	public File getFile()
	{
		return file;
	}

	public Size getImageSize()
	{
		return imageSize;
	}

	public void setImageSize(Size imageSize)
	{
		this.imageSize = imageSize;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		dest.writeSerializable( file );
		if (imageSize != null) {
			dest.writeSize(imageSize);
		} else {
			dest.writeSize( new Size(0, 0) );
		}
	}

	public static final Creator<Page> CREATOR = new Creator<Page>() {
		@Override
		public Page createFromParcel( Parcel source )
		{
			return new Page( source );
		}

		@Override
		public Page[] newArray( int size )
		{
			return new Page[size];
		}
	};

}
