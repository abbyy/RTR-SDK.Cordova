// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript;

import android.app.Application;

import com.abbyy.mobile.rtr.Engine;

import java.io.IOException;

import androidx.annotation.NonNull;

/**
 * Shared Engine holder which reuses engine instance.
 * There is no need to create engine every time.
 */
public class SharedEngine {

	private static final String ENGINE_INIT_ERROR_CODE = "EngineInit";

	private static Engine ENGINE = null;

	private SharedEngine()
	{
		// Utility class
	}

	/**
	 * Initializes Engine. If Engine is already initialized nothing happens.
	 * <p>
	 * Returns true an engine is created successfully, false otherwise.
	 *
	 * @param context Application context.
	 * @param licenseFileName License filename.
	 */
	public static synchronized boolean initializeEngineIfNeeded( Application context, String licenseFileName, JSCallback callback )
	{
		try {
			if( ENGINE == null ) {
				// An engine should be initialized only once during Application lifecycle.
				ENGINE = Engine.load( context, licenseFileName );
			}
			return true;
		} catch( IOException exception ) {
			callback.onError(
				ENGINE_INIT_ERROR_CODE,
				"Could not load some required resource files. Make sure to configure " +
					"'assets' directory in your application and specify correct 'license file name'. " +
					"See logcat for details.",
				exception
			);
			return false;
		} catch( Engine.LicenseException exception ) {
			callback.onError(
				ENGINE_INIT_ERROR_CODE,
				"License not valid. Make sure you have a valid license file in the " +
					"'assets' directory and specify correct 'license file name' and 'application id'. " +
					"See logcat for details." + exception.getMessage(),
				exception
			);
			return false;
		} catch( Throwable throwable ) {
			callback.onError(
				ENGINE_INIT_ERROR_CODE,
				"Unspecified error while loading the engine. See logcat for details.",
				throwable
			);
			return false;
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
