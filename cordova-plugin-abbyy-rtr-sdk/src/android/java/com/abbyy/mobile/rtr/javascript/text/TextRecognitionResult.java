// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.text;

import android.text.TextUtils;

import com.abbyy.mobile.rtr.TextCapture.CharInfo;
import com.abbyy.mobile.rtr.IRecognitionCoreAPI.TextBlock;
import com.abbyy.mobile.rtr.TextCapture.TextLine;
import com.abbyy.mobile.rtr.IRecognitionCoreAPI.Warning;
import com.abbyy.mobile.rtr.javascript.JsonResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_BOLD;
import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_ITALIC;
import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_SMALLCAPS;
import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_STRIKETHROUGH;
import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_SUPERSCRIPT;
import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_UNCERTAIN;
import static com.abbyy.mobile.rtr.IRecognitionCoreAPI.CHAR_ATTRIBUTE_UNDERLINED;
import static com.abbyy.mobile.rtr.javascript.JSConstants.CHAR_INFO;
import static com.abbyy.mobile.rtr.javascript.JSConstants.IS_UNCERTAIN;
import static com.abbyy.mobile.rtr.javascript.JSConstants.ORIENTATION;
import static com.abbyy.mobile.rtr.javascript.JSConstants.QUADRANGLE;
import static com.abbyy.mobile.rtr.javascript.JSConstants.RECT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.TEXT;
import static com.abbyy.mobile.rtr.javascript.JSConstants.TEXT_BLOCKS;
import static com.abbyy.mobile.rtr.javascript.JSConstants.TEXT_LINES;
import static com.abbyy.mobile.rtr.javascript.JSConstants.WARNINGS;
import static com.abbyy.mobile.rtr.javascript.JsonResult.addFlagIfTrue;

class TextRecognitionResult {

	private TextRecognitionResult()
	{
		// Utility
	}

	public static JSONObject getResult( TextBlock[] textBlocks, Set<Warning> warnings, int orientation ) throws JSONException
	{
		JSONObject result = new JSONObject();
		JSONArray textBlocksArray = new JSONArray();

		for( TextBlock textBlock : textBlocks ) {
			textBlocksArray.put( getTextBlock( textBlock ) );
		}

		result.put( TEXT_BLOCKS, textBlocksArray );
		result.put( TEXT, getRecognizedText( textBlocks ) );
		result.put( ORIENTATION, orientation );
		if( !warnings.isEmpty() ) {
			JSONArray warningArray = new JSONArray();
			for( Warning warning : warnings ) {
				warningArray.put( warning.name() );
			}
			result.put( WARNINGS, warningArray );
		}
		return result;
	}

	private static JSONObject getTextBlock( TextBlock textBlock ) throws JSONException
	{
		JSONObject textBlockMap = new JSONObject();
		JSONArray textLinesArray = new JSONArray();

		for( TextLine textLine : textBlock.TextLines ) {
			textLinesArray.put( getTextLine( textLine ) );
		}

		textBlockMap.put( TEXT_LINES, textLinesArray );
		return textBlockMap;
	}

	private static JSONObject getTextLine( TextLine textLine ) throws JSONException
	{
		JSONObject textLineMap = new JSONObject();

		textLineMap.put( TEXT, textLine.Text );
		textLineMap.put( QUADRANGLE, JsonResult.getPointArray( textLine.Quadrangle ) );
		textLineMap.put( RECT, JsonResult.getRect( textLine.Rect ) );

		JSONArray charInfoArray = new JSONArray();
		for( CharInfo charInfo : textLine.CharInfo ) {
			charInfoArray.put( getCharInfo( charInfo ) );
		}
		textLineMap.put( CHAR_INFO, charInfoArray );

		return textLineMap;
	}

	private static JSONObject getCharInfo( CharInfo charInfo ) throws JSONException
	{
		JSONObject charInfoMap = new JSONObject();

		charInfoMap.put( QUADRANGLE, JsonResult.getPointArray( charInfo.Quadrangle ) );
		charInfoMap.put( RECT, JsonResult.getRect( charInfo.Rect ) );
		addFlagIfTrue( charInfoMap, "isItalic", charInfo.Attributes, CHAR_ATTRIBUTE_ITALIC );
		addFlagIfTrue( charInfoMap, "isBold", charInfo.Attributes, CHAR_ATTRIBUTE_BOLD );
		addFlagIfTrue( charInfoMap, "isUnderlined", charInfo.Attributes, CHAR_ATTRIBUTE_UNDERLINED );
		addFlagIfTrue( charInfoMap, "isStrikethrough", charInfo.Attributes, CHAR_ATTRIBUTE_STRIKETHROUGH );
		addFlagIfTrue( charInfoMap, "isSmallcaps", charInfo.Attributes, CHAR_ATTRIBUTE_SMALLCAPS );
		addFlagIfTrue( charInfoMap, "isSuperscript", charInfo.Attributes, CHAR_ATTRIBUTE_SUPERSCRIPT );
		addFlagIfTrue( charInfoMap, IS_UNCERTAIN, charInfo.Attributes, CHAR_ATTRIBUTE_UNCERTAIN );

		return charInfoMap;
	}

	private static String getRecognizedText( TextBlock[] textBlocks )
	{
		List<String> textBlockStrings = new ArrayList<>();
		for( TextBlock textBlock : textBlocks ) {
			textBlockStrings.add( TextUtils.join( "\n", getTextLineStrings( textBlock.TextLines ) ) );
		}
		return TextUtils.join( "\n\n", textBlockStrings );
	}

	private static List<String> getTextLineStrings( TextLine[] textLines )
	{
		List<String> strings = new ArrayList<>();
		for( TextLine textLine : textLines ) {
			strings.add( textLine.Text );
		}
		return strings;
	}

}
