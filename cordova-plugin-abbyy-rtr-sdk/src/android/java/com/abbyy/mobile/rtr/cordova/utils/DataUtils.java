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
}
