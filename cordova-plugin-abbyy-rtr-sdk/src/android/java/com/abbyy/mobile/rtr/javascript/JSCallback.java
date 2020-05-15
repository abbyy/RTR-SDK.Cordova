// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript;

import org.json.JSONObject;

public interface JSCallback {
	void onSuccess( JSONObject result );
	void onError( String errorCode, String message, Throwable exception );
}
