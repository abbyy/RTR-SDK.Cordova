// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.utils;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {

	private FileUtils()
	{
		// Utility class
	}

	public static String convertFileToBase64( File file ) throws IOException
	{
		try( FileInputStream fis = new FileInputStream( file ) ) {
			byte[] bytes = new byte[(int) file.length()];

			int offset = 0;
			int bytesRead = 0;
			while( offset < bytes.length && bytesRead >= 0 ) {
				bytesRead = fis.read( bytes, offset, bytes.length - offset );
				offset += bytesRead;
			}
			return Base64.encodeToString( bytes, Base64.DEFAULT );
		}
	}

}
