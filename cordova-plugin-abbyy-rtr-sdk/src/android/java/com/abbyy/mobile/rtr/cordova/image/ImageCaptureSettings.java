// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.image;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.abbyy.mobile.rtr.IImagingCoreAPI.ExportOperation.Compression;
import com.abbyy.mobile.rtr.javascript.image.Destination;
import com.abbyy.mobile.uicomponents.CaptureView.CameraSettings.Resolution;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario.DocumentSize;

import java.util.Objects;

public class ImageCaptureSettings implements Parcelable {

	public boolean isFlashlightButtonVisible = true;
	public boolean isCaptureButtonVisible = true;
	public boolean isShowPreviewEnabled = false;
	public boolean isGalleryButtonVisible = true;
	public Resolution cameraResolution = Resolution.FULL_HD;
	public int requiredPageCount = 0;
	public Destination destination = Destination.File;
	public ExportType exportType = ExportType.JPG;
	public Compression compressionLevel = Compression.Low;
	public float minimumDocumentToViewRatio = 0.15f;
	public DocumentSize documentSize = DocumentSize.ANY;
	public int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	public float aspectRatioMin = 0f;
	public float aspectRatioMax = 0f;
	public int imageFromGalleryMaxSize = 4096;

	public ImageCaptureSettings() { }

	private ImageCaptureSettings( Parcel source )
	{
		boolean[] booleans = new boolean[4];
		source.readBooleanArray( booleans );
		isFlashlightButtonVisible = booleans[0];
		isCaptureButtonVisible = booleans[1];
		isShowPreviewEnabled = booleans[2];
		isGalleryButtonVisible = booleans[3];

		cameraResolution = Resolution.values()[source.readInt()];
		requiredPageCount = source.readInt();
		destination = Destination.values()[source.readInt()];
		exportType = ExportType.values()[source.readInt()];
		compressionLevel = Compression.values()[source.readInt()];
		minimumDocumentToViewRatio = source.readFloat();
		documentSize = new DocumentSize( source.readFloat(), source.readFloat() );
		orientation = source.readInt();
		aspectRatioMin = source.readFloat();
		aspectRatioMax = source.readFloat();
		imageFromGalleryMaxSize = source.readInt();
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		boolean[] booleans = {
			isFlashlightButtonVisible,
			isCaptureButtonVisible,
			isShowPreviewEnabled,
			isGalleryButtonVisible
		};
		dest.writeBooleanArray( booleans );

		dest.writeInt( cameraResolution.ordinal() );
		dest.writeInt( requiredPageCount );
		dest.writeInt( destination.ordinal() );
		dest.writeInt( exportType.ordinal() );
		dest.writeInt( compressionLevel.ordinal() );
		dest.writeFloat( minimumDocumentToViewRatio );
		dest.writeFloat( documentSize.getWidth() );
		dest.writeFloat( documentSize.getHeight() );
		dest.writeInt( orientation );
		dest.writeFloat( aspectRatioMin );
		dest.writeFloat( aspectRatioMax );
		dest.writeInt( imageFromGalleryMaxSize );
	}

	public static final Creator<ImageCaptureSettings> CREATOR = new Creator<ImageCaptureSettings>() {
		@Override
		public ImageCaptureSettings createFromParcel( Parcel source )
		{
			return new ImageCaptureSettings( source );
		}

		@Override
		public ImageCaptureSettings[] newArray( int size )
		{
			return new ImageCaptureSettings[size];
		}
	};

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		ImageCaptureSettings settings = (ImageCaptureSettings) o;
		return isFlashlightButtonVisible == settings.isFlashlightButtonVisible &&
			isCaptureButtonVisible == settings.isCaptureButtonVisible &&
			isShowPreviewEnabled == settings.isShowPreviewEnabled &&
			requiredPageCount == settings.requiredPageCount &&
			Float.compare( settings.minimumDocumentToViewRatio, minimumDocumentToViewRatio ) == 0 &&
			orientation == settings.orientation &&
			cameraResolution == settings.cameraResolution &&
			destination == settings.destination &&
			exportType == settings.exportType &&
			compressionLevel == settings.compressionLevel &&
			documentSize.equals( settings.documentSize ) &&
			aspectRatioMin == settings.aspectRatioMin &&
			aspectRatioMax == settings.aspectRatioMax &&
			imageFromGalleryMaxSize == settings.imageFromGalleryMaxSize;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(
			isFlashlightButtonVisible,
			isCaptureButtonVisible,
			isShowPreviewEnabled,
			cameraResolution,
			requiredPageCount,
			destination,
			exportType,
			compressionLevel,
			minimumDocumentToViewRatio,
			documentSize,
			orientation,
			aspectRatioMin,
			aspectRatioMax,
			imageFromGalleryMaxSize
		);
	}
}
