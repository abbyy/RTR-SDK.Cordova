// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova;

import android.app.Application;
import android.support.annotation.NonNull;

import com.abbyy.mobile.rtr.Engine;

/**
 * Shared Engine holder which reuses engine instance.
 * There is no need to create engine every time.
 */
public class SharedEngine {

	private static Engine ENGINE = null;

	private SharedEngine()
	{
		// Utility class
	}

	/**
	 * Initializes Engine. If Engine is already initialized nothing happens.
	 *
	 * @param applicationContext Application instance is required to avoid Activity Context memory leak.
	 *
	 * @throws Throwable Throwable that may be thrown during initialization.
	 */
	public static synchronized void initialize(
		@NonNull Application applicationContext,
		@NonNull String licenseFileName
	) throws Throwable
	{
		if( ENGINE == null ) {
			// An engine should be initialized only once during Application lifecycle.
			ENGINE = Engine.load( applicationContext, licenseFileName );
		}
	}

	@NonNull
	public static synchronized Engine get()
	{
		if( ENGINE == null ) {
			throw new IllegalStateException( "Engine isn't initialized" );
		} else {
			return ENGINE;
		}
	}

}
