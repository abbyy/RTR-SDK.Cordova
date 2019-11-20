// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.utils;

import android.os.AsyncTask;
import android.support.annotation.UiThread;

import java.lang.ref.WeakReference;

/**
 * Utility class for handling work in background
 *
 * @param <Args> Input arguments
 * @param <T> Output result
 */
public class BackgroundWorker<Args, T> extends AsyncTask<Args, Void, T> {

	public interface Callback<Args, T> {
		T doWork( Args args, BackgroundWorker<Args, T> worker ) throws Exception;

		@UiThread
		void onDone( T result, Exception exception, BackgroundWorker<Args, T> worker );
	}

	private WeakReference<Callback<Args, T>> callbackRef;
	private volatile Exception exception = null;

	public BackgroundWorker( WeakReference<Callback<Args, T>> callback )
	{
		this.callbackRef = callback;
	}

	@SafeVarargs @Override
	protected final T doInBackground( Args... args )
	{
		Callback<Args, T> callback = callbackRef.get();
		if( callback == null ) { return null; }
		try {
			return callback.doWork( args.length > 0 ? args[0] : null, this );
		} catch( Exception e ) {
			exception = e;
			return null;
		}
	}

	@Override
	protected void onPostExecute( T result )
	{
		Callback<Args, T> callback = callbackRef.get();
		if( callback == null ) { return; }
		onFinished( result, callback );
	}

	private void onFinished( T result, Callback<Args, T> callback )
	{
		if( exception != null ) {
			callback.onDone( null, exception, this );
		} else {
			callback.onDone( result, null, this );
		}
	}
}
