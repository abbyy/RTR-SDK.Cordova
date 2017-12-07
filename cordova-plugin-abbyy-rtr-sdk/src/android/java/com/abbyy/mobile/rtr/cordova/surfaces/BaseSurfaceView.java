package com.abbyy.mobile.rtr.cordova.surfaces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.abbyy.mobile.rtr.IRecognitionService;

public abstract class BaseSurfaceView extends SurfaceView {

	protected Point[] quads;
	protected Rect areaOfInterest;
	protected int stability;
	protected int scaleNominatorX = 1;
	protected int scaleDenominatorX = 1;
	protected int scaleNominatorY = 1;
	protected int scaleDenominatorY = 1;
	protected Paint textPaint;
	protected Paint lineBoundariesPaint;
	protected Paint backgroundPaint;
	protected Paint areaOfInterestPaint;

	protected Rect textBounds;
	protected Path path;

	protected BaseSurfaceView( Context context )
	{
		super( context );

		this.setWillNotDraw( false );

		lineBoundariesPaint = new Paint();
		lineBoundariesPaint.setStyle( Paint.Style.STROKE );
		lineBoundariesPaint.setARGB( 255, 128, 128, 128 );
		textPaint = new Paint();

		areaOfInterestPaint = new Paint();
		areaOfInterestPaint.setARGB( 100, 0, 0, 0 );
		areaOfInterestPaint.setStyle( Paint.Style.FILL );

		// Preallocate
		textBounds = new Rect();
		path = new Path();
	}

	public void setScaleX( int nominator, int denominator )
	{
		scaleNominatorX = nominator;
		scaleDenominatorX = denominator;
	}

	public void setScaleY( int nominator, int denominator )
	{
		scaleNominatorY = nominator;
		scaleDenominatorY = denominator;
	}

	public void setFillBackground( Boolean newValue )
	{
		if( newValue ) {
			backgroundPaint = new Paint();
			backgroundPaint.setStyle( Paint.Style.FILL );
			backgroundPaint.setARGB( 100, 255, 255, 255 );
		} else {
			backgroundPaint = null;
		}
		invalidate();
	}

	public void setAreaOfInterest( Rect newValue )
	{
		areaOfInterest = newValue;
		invalidate();
	}

	public Rect getAreaOfInterest()
	{
		return areaOfInterest;
	}

	protected void setTextPaint( IRecognitionService.ResultStabilityStatus resultStatus )
	{

		switch( resultStatus ) {
			case NotReady:
				textPaint.setARGB( 255, 128, 0, 0 );
				break;
			case Tentative:
				textPaint.setARGB( 255, 128, 0, 0 );
				break;
			case Verified:
				textPaint.setARGB( 255, 128, 64, 0 );
				break;
			case Available:
				textPaint.setARGB( 255, 128, 128, 0 );
				break;
			case TentativelyStable:
				textPaint.setARGB( 255, 64, 128, 0 );
				break;
			case Stable:
				textPaint.setARGB( 255, 0, 128, 0 );
				break;
		}

	}

	// Transforms point to canvas coordinates
	protected Point transformPoint( Point point )
	{
		return new Point(
			( scaleNominatorX * point.x ) / scaleDenominatorX,
			( scaleNominatorY * point.y ) / scaleDenominatorY
		);
	}

	protected void drawBoundary( Canvas canvas, int j, Point[] quads )
	{
		path.reset();
		Point p = quads[j];
		path.moveTo( p.x, p.y );
		p = quads[j + 1];
		path.lineTo( p.x, p.y );
		p = quads[j + 2];
		path.lineTo( p.x, p.y );
		p = quads[j + 3];
		path.lineTo( p.x, p.y );
		path.close();
		canvas.drawPath( path, lineBoundariesPaint );
	}

	protected void drawProgress( int width, int height, Canvas canvas )
	{
		if( stability > 0 ) {
			int r = width / 50;
			int y = height - 175 - 2 * r;
			for( int i = 0; i < stability; i++ ) {
				int x = width / 2 + 3 * r * ( i - 2 );
				canvas.drawCircle( x, y, r, textPaint );
			}
		}
	}

	// Draws recognized text of field name, depending on 'drawName' param
	protected void drawText( Canvas canvas, Point p0, Point p1, Point p3, Paint paint, boolean drawName, String line )
	{
		canvas.save();

		int dx1 = p1.x - p0.x;
		int dy1 = p1.y - p0.y;
		int dx2 = p3.x - p0.x;
		int dy2 = p3.y - p0.y;

		int sqrLength1 = dx1 * dx1 + dy1 * dy1;
		int sqrLength2 = dx2 * dx2 + dy2 * dy2;

		double angle = 180 * Math.atan2( dy2, dx2 ) / Math.PI;
		double xskew = ( dx1 * dx2 + dy1 * dy2 ) / Math.sqrt( sqrLength2 );
		double yskew = Math.sqrt( sqrLength1 - xskew * xskew );

		canvas.translate( p0.x, p0.y );
		canvas.rotate( (float) angle );
		canvas.skew( -(float) ( xskew / yskew ), 0.0f );

		if( drawName ) {
			paint.setTextSize( 30 );
			canvas.drawText( line, 0, (float) ( -yskew ), paint );
		} else {
			paint.setTextSize( (float) yskew );
			paint.getTextBounds( line, 0, line.length(), textBounds );
			double xscale = Math.sqrt( sqrLength2 ) / textBounds.width();
			canvas.scale( (float) xscale, 1.0f );
			canvas.drawText( line, 0, 0, paint );
		}
		canvas.restore();
	}

	@Override
	@SuppressWarnings( "all" )
	protected void onDraw( Canvas canvas )
	{

		super.onDraw( canvas );
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		canvas.save();

		if( areaOfInterest != null ) {
			// Shading and clipping the area of interest
			int left = ( areaOfInterest.left * scaleNominatorX ) / scaleDenominatorX;
			int right = ( areaOfInterest.right * scaleNominatorX ) / scaleDenominatorX;
			int top = ( areaOfInterest.top * scaleNominatorY ) / scaleDenominatorY;
			int bottom = ( areaOfInterest.bottom * scaleNominatorY ) / scaleDenominatorY;
			canvas.drawRect( 0, 0, width, top, areaOfInterestPaint );
			canvas.drawRect( 0, bottom, width, height, areaOfInterestPaint );
			canvas.drawRect( 0, top, left, bottom, areaOfInterestPaint );
			canvas.drawRect( right, top, width, bottom, areaOfInterestPaint );
			canvas.drawRect( left, top, right, bottom, lineBoundariesPaint );
			canvas.clipRect( left, top, right, bottom );
		}

		drawBoundaryAndText( canvas, width, height );
		canvas.restore();
		drawProgress( width, height, canvas );
	}

	protected abstract void drawBoundaryAndText( Canvas canvas, int width, int height );

}
