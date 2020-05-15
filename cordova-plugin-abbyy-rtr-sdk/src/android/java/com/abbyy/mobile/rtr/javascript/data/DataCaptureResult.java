// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.data;

import com.abbyy.mobile.rtr.IDataCaptureCoreAPI.CharInfo;
import com.abbyy.mobile.rtr.IDataCaptureCoreAPI.DataField;
import com.abbyy.mobile.rtr.IDataCaptureCoreAPI.Warning;
import com.abbyy.mobile.rtr.javascript.JsonResult;
import com.abbyy.mobile.rtr.javascript.utils.RectUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import androidx.annotation.NonNull;

import static com.abbyy.mobile.rtr.IDataCaptureCoreAPI.CHAR_ATTRIBUTE_UNCERTAIN;
import static com.abbyy.mobile.rtr.javascript.JSConstants.CHAR_INFO;
import static com.abbyy.mobile.rtr.javascript.JSConstants.IS_UNCERTAIN;
import static com.abbyy.mobile.rtr.javascript.JSConstants.ORIENTATION;
import static com.abbyy.mobile.rtr.javascript.JSConstants.QUADRANGLE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.RECT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.TEXT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.WARNINGS;
import static com.abbyy.mobile.rtr.javascript.JsonResult.addFlagIfTrue;

public class DataCaptureResult {

	private DataCaptureResult()
	{
		// Utility
	}

	public static JSONObject getResult( DataField[] dataFields, Set<Warning> warnings, int orientation ) throws JSONException
	{
		JSONObject result = new JSONObject();
		result.put( "dataFields", getDataFieldsArray( dataFields ) );
		result.put( ORIENTATION, orientation );
		if( !warnings.isEmpty() ) {
			JSONArray warningArray = new JSONArray();
			for( Warning warning : warnings ) {
				warningArray.put( warning.name() );
			}
			result.put( WARNINGS, warningArray );
		}
		return result;
	}

	private static JSONArray getDataFieldsArray( @NonNull DataField[] dataFields ) throws JSONException
	{
		JSONArray dataFieldsArray = new JSONArray();
		for( DataField dataField : dataFields ) {
			dataFieldsArray.put( getDataFieldMap( dataField ) );
		}
		return dataFieldsArray;
	}

	private static JSONObject getDataFieldMap( DataField dataField ) throws JSONException
	{
		JSONObject dataFieldMap = new JSONObject();
		if( dataField.Id != null ) {
			dataFieldMap.put( "id", dataField.Id );
		}
		if( dataField.Name != null ) {
			dataFieldMap.put( "name", dataField.Name );
		}
		dataFieldMap.put( TEXT, dataField.Text );
		dataFieldMap.put( QUADRANGLE, JsonResult.getPointArray( dataField.Quadrangle ) );
		dataFieldMap.put( RECT, JsonResult.getRect( RectUtils.fromPoints( dataField.Quadrangle ) ) );

		if( dataField.CharInfo != null ) {
			JSONArray charInfoArray = new JSONArray();
			for( CharInfo charInfo : dataField.CharInfo ) {
				charInfoArray.put( getCharInfoMap( charInfo ) );
			}
			dataFieldMap.put( CHAR_INFO, charInfoArray );
		}

		if( dataField.Components != null ) {
			dataFieldMap.put( "components", getDataFieldsArray( dataField.Components ) );
		}

		return dataFieldMap;
	}

	private static JSONObject getCharInfoMap( CharInfo charInfo ) throws JSONException
	{
		JSONObject charInfoMap = new JSONObject();
		charInfoMap.put( QUADRANGLE, JsonResult.getPointArray( charInfo.Quadrangle ) );
		charInfoMap.put( RECT, JsonResult.getRect( charInfo.Rect ) );
		addFlagIfTrue( charInfoMap, IS_UNCERTAIN, charInfo.Attributes, CHAR_ATTRIBUTE_UNCERTAIN );
		return charInfoMap;
	}

}
