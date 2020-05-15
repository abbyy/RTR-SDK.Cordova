// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.utils;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RectUtils {

	private RectUtils()
	{
		// Utils
	}

	public static Rect fromPoints( Point[] points )
	{
		int left = 0, right = 0, top = 0, bottom = 0;
		if (points.length > 0) {
			left = right = points[0].x;
			top = bottom = points[0].y;
			for (Point point : points) {
				if (left > point.x) {
					left = point.x;
				} else if (right < point.x) {
					right = point.x;
				}
				if (top > point.y) {
					top = point.y;
				} else if (bottom < point.y) {
					bottom = point.y;
				}
			}
		}

		return new Rect( left, top, right, bottom );
	}

}
