// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Size;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.IImagingCoreAPI.ExportOperation;
import com.abbyy.mobile.rtr.IImagingCoreAPI.ExportToPdfOperation;
import com.abbyy.mobile.rtr.javascript.JSCallback;
import com.abbyy.mobile.rtr.javascript.SharedEngine;
import com.abbyy.mobile.rtr.javascript.utils.ImageUri;
import com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils;
import com.abbyy.mobile.rtr.javascript.utils.UriScheme;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ImagingCore {

	private static final String ERROR_TAG = "ImagingCore";

	private ImagingCore()
	{
		// Utility
	}

	public static void cropImageSync(
		@NonNull final Application application,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final CropSettings settings;
		final Bitmap bitmap;
		try {
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( application, licenseFilename, callback ) ) {
				return;
			}

			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parseCropSettings( jsonSettings );
			bitmap = ImageUri.fromUri( application, SettingsParserUtils.parseImageUri( jsonSettings ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( final IImagingCoreAPI api = engine.createImagingCoreAPI() ) {
			IImagingCoreAPI.Image image = api.loadImage( bitmap );
			bitmap.recycle();
			IImagingCoreAPI.CropOperation cropOperation = api.createCropOperation();
			cropOperation.DocumentBoundary = settings.documentBoundary;
			cropOperation.DocumentWidth = settings.documentWidth;
			cropOperation.DocumentHeight = settings.documentHeight;
			cropOperation.apply( image );

			Bitmap resultBitmap = image.toBitmap();
			String imageResult = exportImage( application, resultBitmap, settings.exportSettings, api );
			Size resultSize = new Size( resultBitmap.getWidth(), resultBitmap.getHeight() );
			JSONObject result = ImagingResult.getCropResult( imageResult, cropOperation.Resolution, resultSize );

			callback.onSuccess( result );
		} catch( Exception e ) {
			callback.onError( ERROR_TAG, "Crop error: " + e.getMessage(), e );
		}
	}

	public static void detectDocumentBoundarySync(
		@NonNull final Application application,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final DetectDocumentBoundarySettings settings;
		final Bitmap bitmap;
		try {
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( application, licenseFilename, callback ) ) {
				return;
			}

			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parseDetectDocumentBoundarySettings( jsonSettings );
			bitmap = ImageUri.fromUri( application, SettingsParserUtils.parseImageUri( jsonSettings ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( final IImagingCoreAPI api = engine.createImagingCoreAPI() ) {
			IImagingCoreAPI.Image image = api.loadImage( bitmap );
			bitmap.recycle();
			IImagingCoreAPI.DetectDocumentBoundaryOperation operation = api.createDetectDocumentBoundaryOperation();
			operation.Mode = settings.detectionMode;
			operation.AreaOfInterest = settings.areaOfInterest;
			operation.DocumentWidth = settings.documentWidth;
			operation.DocumentHeight = settings.documentHeight;
			operation.apply( image );

			JSONObject result;
			result = ImagingResult.getDetectDocumentBoundaryResult( operation.DocumentBoundary,
				operation.DocumentWidth, operation.DocumentHeight );
			callback.onSuccess( result );
		} catch( Exception e ) {
			callback.onError( ERROR_TAG, "Detect document boundary error: " + e.getMessage(), e );
		}
	}

	public static void exportImageSync(
		@NonNull final Application application,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final ExportSettings settings;
		final Bitmap bitmap;
		try {
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( application, licenseFilename, callback ) ) {
				return;
			}

			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parseExportSettings( jsonSettings );
			bitmap = ImageUri.fromUri( application, SettingsParserUtils.parseImageUri( jsonSettings ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( final IImagingCoreAPI api = engine.createImagingCoreAPI() ) {
			String imageResult = exportImage( application, bitmap, settings, api );
			Size size = new Size( bitmap.getWidth(), bitmap.getHeight() );
			bitmap.recycle();
			JSONObject result = ImagingResult.getExportResult( imageResult, size );
			callback.onSuccess( result );
		} catch( Exception e ) {
			callback.onError( ERROR_TAG, "Export image error: " + e.getMessage(), e );
		}
	}

	public static void rotateImageSync(
		@NonNull final Application application,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final RotateSettings settings;
		final Bitmap bitmap;
		try {
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( application, licenseFilename, callback ) ) {
				return;
			}

			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parseRotateSettings( jsonSettings );
			bitmap = ImageUri.fromUri( application, SettingsParserUtils.parseImageUri( jsonSettings ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( final IImagingCoreAPI api = engine.createImagingCoreAPI() ) {
			IImagingCoreAPI.Image image = api.loadImage( bitmap );
			bitmap.recycle();
			IImagingCoreAPI.RotateOperation rotateOperation = api.createRotateOperation();
			rotateOperation.Angle = settings.angle;
			rotateOperation.apply( image );

			Bitmap resultBitmap = image.toBitmap();
			String imageResult = exportImage( application, resultBitmap, settings.exportSettings, api );
			Size size = new Size( resultBitmap.getWidth(), resultBitmap.getHeight() );
			resultBitmap.recycle();
			JSONObject result = ImagingResult.getExportResult( imageResult, size );

			callback.onSuccess( result );
		} catch( Exception e ) {
			callback.onError( ERROR_TAG, "Rotate image error: " + e.getMessage(), e );
		}
	}

	public static void assessQualityForOcrSync(
		@NonNull final Application application,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final QualityAssessmentForOcrSettings settings;
		final Bitmap bitmap;
		try {
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( application, licenseFilename, callback ) ) {
				return;
			}

			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parseQualityAssessmentForOcrSettings( jsonSettings );
			bitmap = ImageUri.fromUri( application, SettingsParserUtils.parseImageUri( jsonSettings ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( final IImagingCoreAPI api = engine.createImagingCoreAPI() ) {
			IImagingCoreAPI.Image image = api.loadImage( bitmap );
			bitmap.recycle();
			IImagingCoreAPI.QualityAssessmentForOcrOperation operation = api.createQualityAssessmentForOcrOperation();
			operation.DocumentBoundary = settings.documentBoundary;
			operation.apply( image );
			JSONObject result = ImagingResult.getQualityAssessmentForOcrResult( operation.QualityAssessmentForOcrBlocks );
			callback.onSuccess( result );
		} catch( Exception e ) {
			callback.onError( ERROR_TAG, "Assess quality error: " + e.getMessage(), e );
		}
	}

	public static void exportImagesToPdfSync(
		@NonNull final Application applicationContext,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final PdfSettings settings;
		try {
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( applicationContext, licenseFilename, callback ) ) {
				return;
			}

			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parsePdfSettings( jsonSettings );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( IImagingCoreAPI api = engine.createImagingCoreAPI() ) {
			String pdfUri;
			switch( settings.destination ) {
				case File:
					pdfUri = exportImagesToPdfFile( applicationContext, settings, api );
					break;
				case Base64:
					pdfUri = exportImagesToPdfAsBase64( applicationContext, settings, api );
					break;
				default:
					throw new IllegalStateException( "Unknown destination type" );
			}
			callback.onSuccess( ImagingResult.getPdfResult( pdfUri ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Export error: " + exception.getMessage(), exception );
		}
	}

	private static String exportImagesToPdfFile( Context context, PdfSettings settings, IImagingCoreAPI api ) throws Exception
	{
		File pdfFile = getFile( context, settings.filePath, "pdf" );
		try( OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( pdfFile ) ) ) {
			try( ExportToPdfOperation operation = api.createExportToPdfOperation( outputStream ) ) {
				exportImagesToPdf( context, settings, operation );
			}
		}
		return UriScheme.FILE + pdfFile.getAbsolutePath();
	}

	private static String exportImagesToPdfAsBase64( Context context, PdfSettings settings, IImagingCoreAPI api ) throws Exception
	{
		try( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
			try( ExportToPdfOperation operation = api.createExportToPdfOperation( baos ) ) {
				exportImagesToPdf( context, settings, operation );
			}
			byte[] bytes = baos.toByteArray();
			return UriScheme.BASE_64_JPEG + Base64.encodeToString( bytes, Base64.DEFAULT );
		}
	}

	private static void exportImagesToPdf( Context context, PdfSettings settings, ExportToPdfOperation operation ) throws Exception
	{
		operation.CompressionType = ExportOperation.CompressionType.Jpg;
		operation.PdfInfoTitle = settings.pdfInfoTitle;
		operation.PdfInfoSubject = settings.pdfInfoSubject;
		operation.PdfInfoKeywords = settings.pdfInfoKeywords;
		operation.PdfInfoAuthor = settings.pdfInfoAuthor;
		operation.PdfInfoCompany = settings.pdfInfoCompany;
		operation.PdfInfoCreator = settings.pdfInfoCreator;
		operation.PdfInfoProducer = settings.pdfInfoProducer;

		for( PdfSettings.ImageSettings imageSettings : settings.images ) {
			operation.PageWidth = imageSettings.pageWidth;
			operation.PageHeight = imageSettings.pageHeight;
			operation.Compression = imageSettings.compression;
			Bitmap image = ImageUri.fromUri( context, imageSettings.imageUri );
			operation.addPage( image );
		}
	}

	private static File getFile( @NonNull Context context, @Nullable String filePath, @NonNull String extension ) throws IOException
	{
		if( filePath == null ) {
			File parent = context.getFilesDir();
			if( !parent.exists() && !parent.mkdirs() ) {
				throw new IOException( "Can't create application file dir" );
			}
			return new File( parent.getAbsolutePath(), UUID.randomUUID().toString() + "." + extension );
		} else {
			return new File( filePath );
		}
	}

	private static String exportImage( @NonNull Context context, Bitmap image, ExportSettings exportSettings,
		IImagingCoreAPI api ) throws Exception
	{
		switch( exportSettings.destination ) {
			case File:
				return exportImageToFile( context, image, exportSettings, api );
			case Base64:
				return exportImageAsBase64Data( image, exportSettings, api );
			default:
				throw new IllegalStateException( "Unsupported destination: " + exportSettings.destination );
		}
	}

	private static String exportImageAsBase64Data( Bitmap image, ExportSettings exportSettings, IImagingCoreAPI api ) throws Exception
	{
		try( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
			try( ExportOperation exportOperation = getExportOperation( exportSettings.exportType, api, baos ) ) {
				exportOperation.addPage( image );
			}
			byte[] bytes = baos.toByteArray();
			String scheme = getBase64Scheme( exportSettings );
			return scheme + Base64.encodeToString( bytes, Base64.DEFAULT );
		}
	}

	private static String getBase64Scheme( ExportSettings exportSettings )
	{
		String scheme;
		switch( exportSettings.exportType ) {
			case JPG:
				scheme = UriScheme.BASE_64_JPEG;
				break;
			case PNG:
				scheme = UriScheme.BASE_64_PNG;
				break;
			default:
				throw new IllegalStateException( "Unexpected value: " + exportSettings.exportType );
		}
		return scheme;
	}

	private static String exportImageToFile( @NonNull Context context, Bitmap image, ExportSettings exportSettings,
		IImagingCoreAPI api ) throws Exception
	{
		File file = getFile( context, exportSettings.filePath, getExtension( exportSettings.exportType ) );
		try( OutputStream outputStream = new FileOutputStream( file ) ) {
			try( ExportOperation exportOperation = getExportOperation( exportSettings.exportType, api, outputStream ) ) {
				exportOperation.addPage( image );
			}
		}
		return UriScheme.FILE + file.getAbsolutePath();
	}

	private static String getExtension( ExportType exportType )
	{
		switch( exportType ) {
			case JPG:
				return "jpg";
			case PNG:
				return "png";
			default:
				throw new IllegalStateException( "Unexpected value: " + exportType );
		}
	}

	private static ExportOperation getExportOperation( ExportType exportType, IImagingCoreAPI api, OutputStream outputStream )
	{
		ExportOperation exportOperation;
		switch( exportType ) {
			case JPG:
				exportOperation = api.createExportToJpgOperation( outputStream );
				break;
			case PNG:
				exportOperation = api.createExportToPngOperation( outputStream );
				break;
			default:
				throw new IllegalStateException( "Unexpected value: " + exportType );
		}
		return exportOperation;
	}
}
