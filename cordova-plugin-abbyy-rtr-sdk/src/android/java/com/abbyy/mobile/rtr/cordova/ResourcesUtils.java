package com.abbyy.mobile.rtr.cordova;

import android.content.Context;
import android.content.res.Resources;

/**
 * Utils to get resource Ids in the Cordova plugin environment (since we don't have the generated R class).
 */
public final class ResourcesUtils {

	private ResourcesUtils() {}

	public static int getResId( final String defType, final String name, final Context context )
	{
		final int id = context.getResources().getIdentifier( name, defType, context.getPackageName() );
		if( id == 0 ) {
			throw new Resources.NotFoundException( "Resource not found: " + defType + "/" + name );
		}
		return id;
	}

}
