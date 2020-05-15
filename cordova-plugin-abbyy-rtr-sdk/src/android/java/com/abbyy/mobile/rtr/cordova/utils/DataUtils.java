package com.abbyy.mobile.rtr.cordova.utils;

import com.abbyy.mobile.rtr.IDataCaptureCoreAPI;
import com.abbyy.mobile.rtr.IDataCaptureService;
import com.abbyy.mobile.rtr.IRecognitionCoreAPI;
import com.abbyy.mobile.rtr.ITextCaptureService;
import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.data.DataCaptureSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.abbyy.mobile.rtr.cordova.utils.TextUtils.parseLanguagesInternal;

public class DataUtils {

	public static ArrayList<HashMap<String, Object>> toJsonDataFields( IDataCaptureService.DataField[] fields )
	{
		ArrayList<HashMap<String, Object>> fieldList = new ArrayList<>();
		for( IDataCaptureService.DataField field : fields ) {
			HashMap<String, Object> fieldInfo = new HashMap<>();
			fieldInfo.put( "id", field.Id != null ? field.Id : "" );
			fieldInfo.put( "name", field.Name != null ? field.Name : "" );

			fieldInfo.put( "text", field.Text );
			if( field.Quadrangle != null ) {
				StringBuilder builder = new StringBuilder();
				for( int i = 0; i < field.Quadrangle.length; i++ ) {
					builder.append( field.Quadrangle[i].x );
					builder.append( ' ' );
					builder.append( field.Quadrangle[i].y );
					if( i != field.Quadrangle.length - 1 ) {
						builder.append( ' ' );
					}
				}
				fieldInfo.put( "quadrangle", builder.toString() );
			}

			ArrayList<HashMap<String, String>> lineList = new ArrayList<>();
			IDataCaptureService.DataField[] components = field.Components;
			if( components != null ) {
				for( IDataCaptureService.DataField line : field.Components ) {
					HashMap<String, String> lineInfo = new HashMap<>();
					lineInfo.put( "text", line.Text );
					if( line.Quadrangle != null ) {
						StringBuilder lineBuilder = new StringBuilder();
						for( int i = 0; i < line.Quadrangle.length; i++ ) {
							lineBuilder.append( line.Quadrangle[i].x );
							lineBuilder.append( ' ' );
							lineBuilder.append( line.Quadrangle[i].y );
							if( i != line.Quadrangle.length - 1 ) {
								lineBuilder.append( ' ' );
							}
						}
						lineInfo.put( "quadrangle", lineBuilder.toString() );
					}
					lineList.add( lineInfo );
				}
			} else {
				HashMap<String, String> lineInfo = new HashMap<>();
				lineInfo.put( "text", field.Text );
				if( field.Quadrangle != null ) {
					StringBuilder lineBuilder = new StringBuilder();
					for( int i = 0; i < field.Quadrangle.length; i++ ) {
						lineBuilder.append( field.Quadrangle[i].x );
						lineBuilder.append( ' ' );
						lineBuilder.append( field.Quadrangle[i].y );
						if( i != field.Quadrangle.length - 1 ) {
							lineBuilder.append( ' ' );
						}
					}
					lineInfo.put( "quadrangle", lineBuilder.toString() );
				}
				lineList.add( lineInfo );
			}
			fieldInfo.put( "components", lineList );

			fieldList.add( fieldInfo );
		}
		return fieldList;
	}

	private static JSONObject toJsonDataField( IDataCaptureCoreAPI.DataField field ) throws JSONException
	{
		JSONObject jsonField = new JSONObject();
		jsonField.put( "id", field.Id );
		jsonField.put( "name", field.Name );
		jsonField.put( "text", field.Text );
		jsonField.put( "quadrangle", TextUtils.convertQuadrangleToString( field.Quadrangle ) );
		JSONArray jsonComponents = new JSONArray();
		for( IDataCaptureCoreAPI.DataField component : field.Components ) {
			jsonComponents.put( toJsonDataField( component ) );
		}
		jsonField.put( "components", jsonComponents );
		return jsonField;
	}

	public static JSONObject toDataCaptureResult( IDataCaptureCoreAPI.DataField[] fields, Integer orientation ) throws JSONException
	{
		JSONObject json = new JSONObject();
		JSONArray jsonFields = new JSONArray();
		for( IDataCaptureCoreAPI.DataField field : fields ) {
			jsonFields.put( toJsonDataField( field ) );
		}

		json.put( "resultInfo", jsonFields );
		if( orientation != null ) {
			json.put( "orientation", orientation.intValue() );
		}
		return json;
	}

	public static DataCaptureSettings parseDataCaptureSettings( JSONObject arg ) throws JSONException
	{
		DataCaptureSettings dataCaptureSettings = new DataCaptureSettings();
		List<Language> languages = parseLanguagesInternal( arg, RtrPlugin.RTR_RECOGNITION_LANGUAGES_KEY, false );
		dataCaptureSettings.recognitionLanguages = languages.toArray( new Language[0] );
		if( arg.has( RtrPlugin.RTR_IS_TEXT_ORIENTATION_DETECTION_ENABLED_KEY ) ) {
			dataCaptureSettings.isTextOrientationDetectionEnabled = arg.getBoolean( RtrPlugin.RTR_IS_TEXT_ORIENTATION_DETECTION_ENABLED_KEY );
		}
		if( arg.has( RtrPlugin.RTR_DATA_CAPTURE_PROFILE_KEY ) ) {
			dataCaptureSettings.profile = arg.getString( RtrPlugin.RTR_DATA_CAPTURE_PROFILE_KEY );
		}

		return dataCaptureSettings;
	}
}
