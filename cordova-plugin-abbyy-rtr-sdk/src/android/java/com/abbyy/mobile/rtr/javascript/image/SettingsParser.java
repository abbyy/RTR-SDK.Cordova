// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import android.graphics.Point;

import com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import static com.abbyy.mobile.rtr.javascript.JSConstants.ANGLE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.COMPRESSION_LEVEL;
import static com.abbyy.mobile.rtr.javascript.JSConstants.COMPRESSION_LEVEL_VALUES;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DESTINATION;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DESTINATION_VALUES;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DETECTION_MODE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DETECTION_MODE_VALUES;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DOCUMENT_SIZE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.EXPORT_TYPE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.EXPORT_TYPE_VALUES;
import static com.abbyy.mobile.rtr.javascript.JSConstants.FILE_PATH;
import static com.abbyy.mobile.rtr.javascript.JSConstants.HEIGHT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.PAGE_SIZE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.PDF_INFO;
import static com.abbyy.mobile.rtr.javascript.JSConstants.RESULT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.WIDTH;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseAreaOfInterest;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseDocumentBoundary;
import static com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils.parseEnumValue;

class SettingsParser {

	private SettingsParser()
	{
		// Utility class
	}

	static DetectDocumentBoundarySettings parseDetectDocumentBoundarySettings(@NonNull JSONObject json) throws Exception
	{
		DetectDocumentBoundarySettings settings = new DetectDocumentBoundarySettings();
		settings.detectionMode = parseEnumValue( json, DETECTION_MODE, DETECTION_MODE_VALUES, settings.detectionMode );
		settings.areaOfInterest = parseAreaOfInterest( json, settings.areaOfInterest );
		if( json.has( DOCUMENT_SIZE ) ) {
			JSONObject docSize = json.getJSONObject( DOCUMENT_SIZE );
			settings.documentWidth = (float) docSize.getDouble( WIDTH );
			settings.documentHeight = (float) docSize.getDouble( HEIGHT );
		}
		return settings;
	}

	static ExportSettings parseExportSettings(@NonNull JSONObject json) throws Exception
	{
		ExportSettings settings = new ExportSettings();
		if( !json.has( RESULT ) ) {
			return settings;
		}
		json = json.getJSONObject( RESULT );
		if( json.has( COMPRESSION_LEVEL ) ) {
			settings.compression = parseEnumValue( json, COMPRESSION_LEVEL, COMPRESSION_LEVEL_VALUES, settings.compression );
		}
		settings.destination = parseEnumValue( json, DESTINATION, DESTINATION_VALUES, settings.destination );
		settings.exportType = parseEnumValue( json, EXPORT_TYPE, EXPORT_TYPE_VALUES, settings.exportType );
		if( json.has( FILE_PATH ) ) {
			String path = json.getString(FILE_PATH);
			if( path.equals("null") ) {
				path = null;
			}
			settings.filePath = path;
		}
		return settings;
	}

	static CropSettings parseCropSettings(@NonNull JSONObject json) throws Exception
	{
		ExportSettings exportSettings = parseExportSettings( json );
		Point[] documentBoundary = parseDocumentBoundary( json );
		CropSettings settings = new CropSettings( documentBoundary, exportSettings );
		if( json.has( DOCUMENT_SIZE ) ) {
			JSONObject docSize = json.getJSONObject( DOCUMENT_SIZE );
			settings.documentWidth = (float) docSize.getDouble( WIDTH );
			settings.documentHeight = (float) docSize.getDouble( HEIGHT );
		}
		return settings;
	}

	static RotateSettings parseRotateSettings(@NonNull JSONObject json) throws Exception
	{
		RotateSettings settings = new RotateSettings();
		if( !json.has( ANGLE ) ) {
			throw new IllegalArgumentException( "Angle is required parameter" );
		}
		settings.angle = json.getInt( ANGLE );
		settings.exportSettings = parseExportSettings( json );

		return settings;
	}

	static QualityAssessmentForOcrSettings parseQualityAssessmentForOcrSettings(@NonNull JSONObject json) throws Exception
	{
		QualityAssessmentForOcrSettings settings = new QualityAssessmentForOcrSettings();
		settings.documentBoundary = parseDocumentBoundary( json, settings.documentBoundary );
		return settings;
	}

	static PdfSettings parsePdfSettings(@NonNull JSONObject jsonObject) throws Exception
	{
		PdfSettings settings = new PdfSettings();
		settings.images = parsePdfImages( jsonObject );
		if( jsonObject.has( RESULT ) ) {
			JSONObject resultObject = jsonObject.getJSONObject( RESULT );
			settings.filePath = resultObject.optString( FILE_PATH, null );
			settings.destination = parseEnumValue( resultObject, DESTINATION, DESTINATION_VALUES, settings.destination );
		}

		if( jsonObject.has( PDF_INFO ) ) {
			JSONObject pdfInfoObject = jsonObject.getJSONObject( PDF_INFO );
			settings.pdfInfoTitle = pdfInfoObject.optString( "title", null );
			settings.pdfInfoSubject = pdfInfoObject.optString( "subject", null );
			settings.pdfInfoKeywords = pdfInfoObject.optString( "keywords", null );
			settings.pdfInfoAuthor = pdfInfoObject.optString( "author", null );
			settings.pdfInfoCompany = pdfInfoObject.optString( "company", null );
			settings.pdfInfoCreator = pdfInfoObject.optString( "creator", null );
			settings.pdfInfoProducer = pdfInfoObject.optString( "producer", null );
		}

		if( settings.destination == Destination.Base64 && settings.images.length > 1 ) {
			throw new IllegalArgumentException( "Base64 destination supports only one page. Use File destination" );
		}

		return settings;
	}

	private static PdfSettings.ImageSettings[] parsePdfImages( @NonNull JSONObject jsonObject ) throws Exception
	{
		JSONArray images = jsonObject.getJSONArray( "images" );
		PdfSettings.ImageSettings[] imageSettingsArray = new PdfSettings.ImageSettings[images.length()];
		for( int imageIndex = 0; imageIndex < images.length(); ++imageIndex ) {
			JSONObject image = images.getJSONObject( imageIndex );
			PdfSettings.ImageSettings imageSettings = new PdfSettings.ImageSettings();
			if( image.has( PAGE_SIZE ) ) {
				JSONObject docSize = image.getJSONObject( PAGE_SIZE );
				imageSettings.pageWidth = docSize.getInt( WIDTH );
				imageSettings.pageHeight = docSize.getInt( HEIGHT );
			}
			imageSettings.imageUri = SettingsParserUtils.parseImageUri( image );
			if( image.has( COMPRESSION_LEVEL ) ) {
				imageSettings.compression =
					parseEnumValue( image, COMPRESSION_LEVEL, COMPRESSION_LEVEL_VALUES, imageSettings.compression );
			}
			imageSettingsArray[imageIndex] = imageSettings;
		}

		if( images.length() == 0 ) {
			throw new IllegalArgumentException( "Images is empty" );
		}

		return imageSettingsArray;
	}
}
