// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import com.abbyy.mobile.rtr.IImagingCoreAPI.ExportOperation.Compression;

import java.util.Arrays;
import java.util.Objects;

class PdfSettings {

	static class ImageSettings {
		public int pageWidth = 0;
		public int pageHeight = 0;
		public Compression compression = Compression.Low;
		public String imageUri;
	}

	public ImageSettings[] images;
	public Destination destination = Destination.File;
	public String filePath;
	public String pdfInfoTitle;
	public String pdfInfoSubject;
	public String pdfInfoKeywords;
	public String pdfInfoAuthor;
	public String pdfInfoCompany;
	public String pdfInfoCreator;
	public String pdfInfoProducer;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		PdfSettings that = (PdfSettings) o;
		return Arrays.equals( images, that.images ) &&
			destination == that.destination &&
			Objects.equals( filePath, that.filePath ) &&
			Objects.equals( pdfInfoTitle, that.pdfInfoTitle ) &&
			Objects.equals( pdfInfoSubject, that.pdfInfoSubject ) &&
			Objects.equals( pdfInfoKeywords, that.pdfInfoKeywords ) &&
			Objects.equals( pdfInfoAuthor, that.pdfInfoAuthor ) &&
			Objects.equals( pdfInfoCompany, that.pdfInfoCompany ) &&
			Objects.equals( pdfInfoCreator, that.pdfInfoCreator ) &&
			Objects.equals( pdfInfoProducer, that.pdfInfoProducer );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(
			images,
			destination,
			filePath,
			pdfInfoTitle,
			pdfInfoSubject,
			pdfInfoKeywords,
			pdfInfoAuthor,
			pdfInfoCompany,
			pdfInfoCreator,
			pdfInfoProducer
		);
	}
}
