// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Size;
import android.util.SizeF;

import com.abbyy.mobile.rtr.IImageCaptureService;
import com.abbyy.mobile.rtr.javascript.JsonResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.abbyy.mobile.rtr.javascript.JSConstants.DOCUMENT_BOUNDARY_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.DOCUMENT_SIZE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.IMAGE_SIZE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.IMAGE_URI_KEY;
import static com.abbyy.mobile.rtr.javascript.JSConstants.QUALITY_ASSESSMENT_FOR_OCR_BLOCKS;

class ImagingResult {
	private ImagingResult()
	{
		// Utility
	}

	public static JSONObject getDetectDocumentBoundaryResult(
		@Nullable Point[] documentBoundary, float documentWidth, float documentHeight
	) throws JSONException
	{
		JSONObject json = new JSONObject();

		json.put( DOCUMENT_SIZE, JsonResult.getSizeFloat( new SizeF( documentWidth, documentHeight ) ) );
		if( documentBoundary != null ) {
			json.put( DOCUMENT_BOUNDARY_KEY, JsonResult.getPointArray( documentBoundary ) );
		}
		return json;
	}

	public static JSONObject getCropResult( String imageUri, int resolution, Size size ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( IMAGE_URI_KEY, imageUri );
		JSONObject jsonResolution = new JSONObject();
		jsonResolution.put( "x", resolution );
		jsonResolution.put( "y", resolution );
		json.put( "resolution", jsonResolution );
		json.put( IMAGE_SIZE, JsonResult.getSize( size ) );
		return json;
	}

	public static JSONObject getExportResult( String imageUri, Size size ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( IMAGE_URI_KEY, imageUri );
		json.put( IMAGE_SIZE, JsonResult.getSize( size ) );
		return json;
	}

	public static JSONObject getQualityAssessmentForOcrResult(
		@Nullable IImageCaptureService.QualityAssessmentForOcrBlock[] qualityAssessmentForOcrBlocks
	) throws JSONException
	{
		JSONObject json = new JSONObject();
		JSONArray jsonBlocksArray = new JSONArray();
		if( qualityAssessmentForOcrBlocks != null ) {
			for (IImageCaptureService.QualityAssessmentForOcrBlock block : qualityAssessmentForOcrBlocks) {
				jsonBlocksArray.put(toJson(block));
			}
		}
		json.put( QUALITY_ASSESSMENT_FOR_OCR_BLOCKS, jsonBlocksArray );
		return json;
	}

	private static JSONObject toJson( IImageCaptureService.QualityAssessmentForOcrBlock block ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( "type", block.Type.name() );
		json.put( "quality", block.Quality );
		json.put( "rect", toJson( block.Rect ) );
		return json;
	}

	private static JSONObject toJson( Rect rect ) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put( "top", rect.top );
		json.put( "bottom", rect.bottom );
		json.put( "left", rect.left );
		json.put( "right", rect.right );
		return json;
	}

	public static JSONObject getPdfResult( String pdfUri ) throws Exception
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put( "pdfUri", pdfUri );
		return jsonObject;
	}
}
