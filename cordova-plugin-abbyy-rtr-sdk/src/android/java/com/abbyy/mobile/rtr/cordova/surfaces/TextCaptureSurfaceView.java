package com.abbyy.mobile.rtr.cordova.surfaces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;

import com.abbyy.mobile.rtr.ITextCaptureService;

public class TextCaptureSurfaceView extends BaseSurfaceView {

	//special field for TextCapture
	private String[] lines;

	public TextCaptureSurfaceView( Context context )
	{
		super( context );
	}

	public void setLines( ITextCaptureService.TextLine[] lines,
		ITextCaptureService.ResultStabilityStatus resultStatus )
	{
		if( lines != null && scaleDenominatorX > 0 && scaleDenominatorY > 0 ) {
			this.quads = new Point[lines.length * 4];
			this.lines = new String[lines.length];
			for( int i = 0; i < lines.length; i++ ) {
				ITextCaptureService.TextLine line = lines[i];
				for( int j = 0; j < 4; j++ ) {
					this.quads[4 * i + j] = new Point(
						( scaleNominatorX * line.Quadrangle[j].x ) / scaleDenominatorX,
						( scaleNominatorY * line.Quadrangle[j].y ) / scaleDenominatorY
					);
				}
				this.lines[i] = line.Text;
			}
			setTextPaint( resultStatus );
			stability = resultStatus.ordinal();
		} else {
			stability = 0;
			this.lines = null;
			this.quads = null;
		}
		this.invalidate();
	}

	@Override
	protected void drawBoundaryAndText( Canvas canvas, int width, int height )
	{
		if( lines != null ) {
			// Shade (whiten) the background when stable
			if( backgroundPaint != null ) {
				canvas.drawRect( 0, 0, width, height, backgroundPaint );
			}

			for( int i = 0; i < lines.length; i++ ) {
				// The boundaries
				int j = 4 * i;
				drawBoundary( canvas, j, quads );

				drawText( canvas, quads[j], quads[j + 1], quads[j + 3], textPaint, false, lines[i] );
			}
		}
	}

}
