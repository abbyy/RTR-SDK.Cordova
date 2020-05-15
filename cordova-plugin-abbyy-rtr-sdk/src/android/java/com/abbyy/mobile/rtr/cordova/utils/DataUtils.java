package com.abbyy.mobile.rtr.cordova.utils;

import com.abbyy.mobile.rtr.IDataCaptureService;

import java.util.ArrayList;
import java.util.HashMap;

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
				fieldInfo.put( "quadrangle", TextUtils.getPointArray( field.Quadrangle ) );
			}

			ArrayList<HashMap<String, Object>> lineList = new ArrayList<>();
			IDataCaptureService.DataField[] components = field.Components;
			if( components != null ) {
				for( IDataCaptureService.DataField line : field.Components ) {
					HashMap<String, Object> lineInfo = new HashMap<>();
					lineInfo.put( "text", line.Text );
					if( line.Quadrangle != null ) {
						lineInfo.put( "quadrangle", TextUtils.getPointArray( line.Quadrangle ) );
					}
					lineList.add( lineInfo );
				}
			} else {
				HashMap<String, Object> lineInfo = new HashMap<>();
				lineInfo.put( "text", field.Text );
				if( field.Quadrangle != null ) {
					lineInfo.put( "quadrangle", TextUtils.getPointArray( field.Quadrangle ) );
				}
				lineList.add( lineInfo );
			}
			fieldInfo.put( "components", lineList );

			fieldList.add( fieldInfo );
		}
		return fieldList;
	}
}
