// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.text;

import android.app.Application;
import android.graphics.Bitmap;

import com.abbyy.mobile.rtr.Engine;
import com.abbyy.mobile.rtr.IRecognitionCoreAPI;
import com.abbyy.mobile.rtr.IRecognitionCoreAPI.Warning;
import com.abbyy.mobile.rtr.javascript.JSCallback;
import com.abbyy.mobile.rtr.javascript.SharedEngine;
import com.abbyy.mobile.rtr.javascript.utils.ImageUri;
import com.abbyy.mobile.rtr.javascript.utils.SettingsParserUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextRecognition {

	private static final String ERROR_TAG = "TextRecognition";

	private TextRecognition()
	{
		// Utility
	}

	public static void recognizeTextSync(
		@NonNull final Application application,
		@Nullable final JSONObject jsonSettings,
		@NonNull final JSCallback callback
	)
	{
		final Settings settings;
		final Bitmap image;
		try {
			SettingsParserUtils.checkForNullSettings( jsonSettings );
			settings = SettingsParser.parse( jsonSettings );
			String licenseFilename = SettingsParserUtils.parseLicenseFilename( jsonSettings );
			if( !SharedEngine.initializeEngineIfNeeded( application, licenseFilename, callback ) ) {
				return;
			}

			image = ImageUri.fromUri( application, SettingsParserUtils.parseImageUri( jsonSettings ) );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Parse error: " + exception.getMessage(), exception );
			return;
		}

		Engine engine = SharedEngine.get();
		try( final IRecognitionCoreAPI api = engine.createRecognitionCoreAPI() ) {
			IRecognitionCoreAPI.TextRecognitionSettings textRecognitionSettings = api.getTextRecognitionSettings();
			textRecognitionSettings.setAreaOfInterest( settings.areaOfInterest );
			textRecognitionSettings.setTextOrientationDetectionEnabled( settings.isTextOrientationDetectionEnabled );
			textRecognitionSettings.setRecognitionLanguage( settings.languages );

			recognizeTextOnImage( image, api, callback );
		} catch( Exception exception ) {
			callback.onError( ERROR_TAG, "Error: " + exception.getMessage(), exception );
		}
	}

	private static void recognizeTextOnImage( Bitmap image, IRecognitionCoreAPI configuredApi, final JSCallback callback )
	{
		final Set<Warning> warnings = new LinkedHashSet<>();
		// Use atomic wrappers to change final variables content
		final AtomicBoolean isRejected = new AtomicBoolean( false );
		final AtomicReference<Integer> orientationAtomicReference = new AtomicReference<>( 0 );
		IRecognitionCoreAPI.TextBlock[] textBlocks = configuredApi.recognizeText( image,
			new IRecognitionCoreAPI.TextRecognitionCallback() {
				@Override
				public boolean onProgress( int progress, Warning warning )
				{
					if( warning != null ) {
						warnings.add( warning );
					}
					return false;
				}

				@Override
				public void onTextOrientationDetected( int orientation )
				{
					orientationAtomicReference.set( orientation );
				}

				@Override
				public void onError( Exception e )
				{
					isRejected.set( true );
					callback.onError( ERROR_TAG, "exception during recognition: " + e.getMessage(), e );
				}
			}
		);

		if( isRejected.get() ) {
			return;
		}

		if( textBlocks == null ) {
			callback.onError( ERROR_TAG, "Recognition error: null text blocks", null );
			return;
		}
		JSONObject result;
		try {
			result = TextRecognitionResult.getResult(
				textBlocks,
				warnings,
				orientationAtomicReference.get()
			);
		} catch( JSONException e ) {
			callback.onError( ERROR_TAG, "Result JSON error", e );
			return;
		}
		callback.onSuccess( result );
	}
}
