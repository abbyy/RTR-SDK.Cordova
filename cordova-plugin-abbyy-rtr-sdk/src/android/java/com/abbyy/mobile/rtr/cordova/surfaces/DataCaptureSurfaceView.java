package com.abbyy.mobile.rtr.cordova.surfaces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.abbyy.mobile.rtr.IDataCaptureService;

public class DataCaptureSurfaceView extends BaseSurfaceView {

	//special fields for DataCapture
	private Point[] fieldsQuads;
	private String[] fieldValues;
	private String[] fieldNames;
	private Paint fieldPaint;

	public DataCaptureSurfaceView( Context context )
	{
		super( context );

		fieldPaint = new Paint();
		fieldPaint.setStyle( Paint.Style.STROKE );
		fieldPaint.setARGB( 255, 128, 128, 128 );
	}

	public void setLines( IDataCaptureService.DataField fields[],
		IDataCaptureService.ResultStabilityStatus resultStatus )
	{
		if( fields != null && scaleDenominatorX > 0 && scaleDenominatorY > 0 ) {
			int count = 0;
			fieldsQuads = new Point[fields.length * 4];
			fieldNames = new String[fields.length];
			for( int i = 0; i < fields.length; i++ ) {
				count += fields[i].Components.length;
				fieldNames[i] = fields[i].Name;
				Point[] srcQuad = fields[i].Quadrangle;
				for( int j = 0; j < 4; j++ ) {
					fieldsQuads[i * 4 + j] = ( srcQuad != null ? transformPoint( srcQuad[j] ) : null );
				}
			}
			this.quads = new Point[count * 4];
			this.fieldValues = new String[count];
			int index = 0;
			for( IDataCaptureService.DataField field : fields ) {
				for( IDataCaptureService.DataField component : field.Components ) {
					Point[] srcQuad = component.Quadrangle;
					for( int j = 0; j < 4; j++ ) {
						this.quads[4 * index + j] = ( srcQuad != null ? transformPoint( srcQuad[j] ) : null );
					}
					this.fieldValues[index] = component.Text;
					index++;
				}
			}
			setTextPaint( resultStatus );
			stability = resultStatus.ordinal();
		} else {
			stability = 0;
			this.fieldValues = null;
			this.quads = null;
		}

		this.invalidate();
	}

	@Override
	protected void drawBoundaryAndText( Canvas canvas, int width, int height )
	{
		if( fieldValues != null ) {

			// Shade (whiten) the background when stable
			if( backgroundPaint != null ) {
				canvas.drawRect( 0, 0, width, height, backgroundPaint );
			}

			// Draw fields boundaries and names
			for( int i = 0; i < fieldNames.length; i++ ) {
				int j = 4 * i;
				if( fieldsQuads[j] != null ) {
					drawBoundary( canvas, j, fieldsQuads );
					String fieldName = fieldNames[i];
					if( fieldName != null ) {
						drawText( canvas, fieldsQuads[j], fieldsQuads[j + 1], fieldsQuads[j + 3], fieldPaint, true,
							fieldName );
					}
				}
			}

			// Draw the fields values
			for( int i = 0; i < fieldValues.length; i++ ) {
				// The boundaries
				int j = 4 * i;
				if( quads[j] != null ) {
					drawBoundary( canvas, j, quads );
					// The skewed text (drawn by coordinate transform)
					drawText( canvas, quads[j], quads[j + 1], quads[j + 3], textPaint, false, fieldValues[i] );
				} else {
					// Field geometry not defined, just dumping all the text
					int left = ( areaOfInterest.left * scaleNominatorX ) / scaleDenominatorX;
					int top = ( areaOfInterest.top * scaleNominatorY ) / scaleDenominatorY;
					fieldPaint.setTextSize( 30 );
					String label = fieldNames[i] + ": ";
					fieldPaint.getTextBounds( label, 0, label.length(), textBounds );
					canvas.drawText( label, left + 35, top + ( i + 1 ) * 35 + 35, fieldPaint );
					textPaint.setTextSize( 30 );
					canvas.drawText( fieldValues[i], left + 35 + textBounds.right, top + ( i + 1 ) * 35 + 35, textPaint );
				}
			}
		}
	}

}
