// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

import java.io.File;

public class Page implements Parcelable {

	private File file;
	private Size frameSize;
	private Point[] documentBoundary;

	public Page() {}

	private Page( Parcel source )
	{
		file = (File) source.readSerializable();
		frameSize = source.readSize();

		int[] boundary = new int[8];
		source.readIntArray( boundary );
		documentBoundary = new Point[4];
		for( int i = 0; i < 4; i++ ) {
			documentBoundary[i] = new Point( boundary[i * 2], boundary[i * 2 + 1] );
		}
	}

	public void setFile( File file )
	{
		this.file = file;
	}

	public File getFile()
	{
		return file;
	}

	public Size getFrameSize()
	{
		return frameSize;
	}

	public void setFrameSize( Size frameSize )
	{
		this.frameSize = frameSize;
	}

	public void setDocumentBoundary( Point[] documentBoundary )
	{
		this.documentBoundary = documentBoundary;
	}

	public Point[] getDocumentBoundary()
	{
		return documentBoundary;
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
		if (frameSize != null) {
			dest.writeSize( frameSize );
		} else {
			dest.writeSize( new Size(0, 0) );
		}
		int[] boundary = new int[8];
		if( documentBoundary != null ) {
			for( int i = 0; i < 4; i++ ) {
				boundary[2 * i] = documentBoundary[i].x;
				boundary[2 * i + 1] = documentBoundary[i].y;
			}
		}
		dest.writeIntArray( boundary );
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
