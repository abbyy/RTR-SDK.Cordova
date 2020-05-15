// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript.image;

import java.util.Objects;

public class RotateSettings {
	int angle = 0;

	ExportSettings exportSettings = null;

	@Override
	public boolean equals( Object o )
	{
		if( this == o ) { return true; }
		if( o == null || getClass() != o.getClass() ) { return false; }
		RotateSettings settings = (RotateSettings) o;
		return angle == settings.angle &&
			Objects.equals( exportSettings, settings.exportSettings );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( angle, exportSettings );
	}
}
