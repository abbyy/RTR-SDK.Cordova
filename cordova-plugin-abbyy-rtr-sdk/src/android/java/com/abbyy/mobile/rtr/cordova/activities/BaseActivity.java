// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abbyy.mobile.rtr.IRecognitionService;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.RtrManager;
import com.abbyy.mobile.rtr.cordova.RtrPlugin;
import com.abbyy.mobile.rtr.cordova.surfaces.BaseSurfaceView;
import com.abbyy.mobile.rtr.javascript.utils.BackgroundWorker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;

@SuppressWarnings( "deprecation" )
abstract class BaseActivity extends Activity {

	///////////////////////////////////////////////////////////////////////////////
	// Some application settings that can be changed to modify application behavior:
	// The camera zoom. Optically zooming with a good camera often improves results
	// even at close range and it might be required at longer ranges.
	protected static final int cameraZoom = 1;
	// The default behavior in this sample is to start recognition when application is started or
	// resumed. You can turn off this behavior or remove it completely to simplify the application
	protected static final boolean startRecognitionOnAppStart = true;
	// Area of interest specified through margin sizes relative to camera preview size
	protected static int areaOfInterestMargin_PercentOfWidth = 4;
	protected static int areaOfInterestMargin_PercentOfHeight = 15;

	// The camera and the preview surface
	protected Camera camera;
	protected SurfaceHolder previewSurfaceHolder;

	protected IRecognitionService.ResultStabilityStatus currentStabilityStatus = IRecognitionService.ResultStabilityStatus.NotReady;
	protected String errorOccurred;

	//The flashlight
	protected boolean isFlashLightOn = false;

	// Actual preview size and orientation
	protected Camera.Size cameraPreviewSize;
	protected int orientation;
	protected boolean cameraInitialisation = false;

	// Auxiliary variables
	protected boolean inPreview = false; // Camera preview is started
	protected boolean stableResultHasBeenReached; // Stable result has been reached
	protected boolean startRecognitionWhenReady; // Start recognition next time when ready (and reset this flag)
	protected Handler handler = new Handler(); // Posting some delayed actions;
	protected DisplayManager.DisplayListener orientationEventListener;

	// UI components
	protected Button startButton; // The start button
	protected Button preferenceButton;
	protected TextView scenarioDescription;
	protected ImageButton flashButton;
	protected TextView warningTextView; // Show warnings from recognizer
	protected TextView errorTextView; // Show errors from recognizer

	enum State {
		Start, Starting, Stop
	}

	protected State state = State.Start;

	// This callback will be used to obtain frames from the camera
	protected Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
		@Override
		public void onPreviewFrame( byte[] data, Camera camera )
		{
			// The buffer that we have given to the camera in ITextCaptureService.Callback.onRequestLatestFrame
			// above have been filled. Send it back to the Text Capture Service
			getCaptureService().submitRequestedFrame( data );
		}
	};

	// This callback is used to configure preview surface for the camera
	protected SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated( SurfaceHolder holder )
		{
			// When surface is created, store the holder
			previewSurfaceHolder = holder;
		}

		@Override
		public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
		{
			// When surface is changed (or created), attach it to the camera, configure camera and start preview
			if( camera != null ) {
				setCameraPreviewDisplayAndStartPreview();
			}
		}

		@Override
		public void surfaceDestroyed( SurfaceHolder holder )
		{
			// When surface is destroyed, clear previewSurfaceHolder
			previewSurfaceHolder = null;
		}
	};

	// Attach the camera to the surface holder, configure the camera and start preview
	private void setCameraPreviewDisplayAndStartPreview()
	{
		try {
			camera.setPreviewDisplay( previewSurfaceHolder );
		} catch( Throwable t ) {
			Log.e( getString( ResourcesUtils.getResId( "string", "app_name", this ) ), "Exception in setPreviewDisplay()", t );
		}
		configureCameraAndStartPreview( camera );
	}

	private void configureCameraAndStartPreview( Camera camera )
	{
		// Setting camera parameters when preview is running can cause crashes on some android devices
		stopPreview();

		// Configure camera orientation. This is needed for both correct preview orientation
		// and recognition
		orientation = getCameraOrientation();
		camera.setDisplayOrientation( orientation );

		// Configure camera parameters
		Camera.Parameters parameters = camera.getParameters();

		// Select preview size. The preferred size im most scenarios is 1280x720 or just below this
		// Other scenarios requires maximum available resolution
		cameraPreviewSize = null;
		for( Camera.Size size : parameters.getSupportedPreviewSizes() ) {
			if( size.height <= 720 || size.width <= 720 || needMaximumAvailableResolution() ) {
				if( cameraPreviewSize == null ) {
					cameraPreviewSize = size;
				} else {
					int resultArea = cameraPreviewSize.width * cameraPreviewSize.height;
					int newArea = size.width * size.height;
					if( newArea > resultArea ) {
						cameraPreviewSize = size;
					}
				}
			}
		}
		parameters.setPreviewSize( cameraPreviewSize.width, cameraPreviewSize.height );

		// Zoom
		parameters.setZoom( cameraZoom );
		// Buffer format. The only currently supported format is NV21
		parameters.setPreviewFormat( ImageFormat.NV21 );
		// Default focus mode
		parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );

		// Done
		camera.setParameters( parameters );

		// The camera will fill the buffers with image data and notify us through the callback.
		// The buffers will be sent to camera on requests from recognition service (see implementation
		// of IDataCaptureService.Callback.onRequestLatestFrame above)
		camera.setPreviewCallbackWithBuffer( cameraPreviewCallback );

		// Clear the previous recognition results if any
		clearRecognitionResults();
		configureSurfaceView( getSurfaceViewWithOverlay() );

		// Start preview
		camera.startPreview();

		setCameraFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
		autoFocus( finishCameraInitialisationAutoFocusCallback );
		cameraInitialisation = true;

		inPreview = true;
	}

	protected void configureSurfaceView( BaseSurfaceView surfaceViewWithOverlay )
	{
		// Width and height of the preview according to the current screen rotation
		int width = 0;
		int height = 0;
		switch( orientation ) {
			case 0:
			case 180:
				width = cameraPreviewSize.width;
				height = cameraPreviewSize.height;
				break;
			case 90:
			case 270:
				width = cameraPreviewSize.height;
				height = cameraPreviewSize.width;
				break;
		}

		// Configure the view scale and area of interest (camera sees it as rotated 90 degrees, so
		// there's some confusion with what is width and what is height)
		surfaceViewWithOverlay.setScaleX( surfaceViewWithOverlay.getWidth(), width );
		surfaceViewWithOverlay.setScaleY( surfaceViewWithOverlay.getHeight(), height );
		// Area of interest
		int marginWidth = ( areaOfInterestMargin_PercentOfWidth * width ) / 100;
		int marginHeight = ( areaOfInterestMargin_PercentOfHeight * height ) / 100;
		surfaceViewWithOverlay.setAreaOfInterest(
			new Rect( marginWidth, marginHeight, width - marginWidth,
				height - marginHeight ) );
	}

	// Returns orientation of camera
	protected int getCameraOrientation()
	{
		Display display = getWindowManager().getDefaultDisplay();
		int orientation = 0;
		switch( display.getRotation() ) {
			case Surface.ROTATION_0:
				orientation = 0;
				break;
			case Surface.ROTATION_90:
				orientation = 90;
				break;
			case Surface.ROTATION_180:
				orientation = 180;
				break;
			case Surface.ROTATION_270:
				orientation = 270;
				break;
		}
		for( int i = 0; i < Camera.getNumberOfCameras(); i++ ) {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			Camera.getCameraInfo( i, cameraInfo );
			if( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ) {
				return ( cameraInfo.orientation - orientation + 360 ) % 360;
			}
		}
		// If Camera.open() succeed, this point of code never reached
		return -1;
	}

	// Indicates that scenario requires maximum available camera resolution
	protected boolean needMaximumAvailableResolution()
	{
		return false;
	}

	protected abstract void clearRecognitionResults();

	// Sets camera focus mode and focus area
	@SuppressWarnings( "all" )
	protected void setCameraFocusMode( String mode )
	{
		// Camera sees it as rotated 90 degrees, so there's some confusion with what is width and what is height)
		int width = 0;
		int height = 0;
		int halfCoordinates = 1000;
		int lengthCoordinates = 2000;
		Rect area = getSurfaceViewWithOverlay().getAreaOfInterest();
		switch( orientation ) {
			case 0:
			case 180:
				height = cameraPreviewSize.height;
				width = cameraPreviewSize.width;
				break;
			case 90:
			case 270:
				width = cameraPreviewSize.height;
				height = cameraPreviewSize.width;
				break;
		}
		camera.cancelAutoFocus();
		Camera.Parameters parameters = camera.getParameters();
		// Set focus and metering area equal to the area of interest. This action is essential because by defaults camera
		// focuses on the center of the frame, while the area of interest in this sample application is at the top
		List<Camera.Area> focusAreas = new ArrayList<>();
		Rect areasRect;

		switch( orientation ) {
			case 0:
				areasRect = new Rect(
					-halfCoordinates + area.left * lengthCoordinates / width,
					-halfCoordinates + area.top * lengthCoordinates / height,
					-halfCoordinates + lengthCoordinates * area.right / width,
					-halfCoordinates + lengthCoordinates * area.bottom / height
				);
				break;
			case 180:
				areasRect = new Rect(
					halfCoordinates - area.right * lengthCoordinates / width,
					halfCoordinates - area.bottom * lengthCoordinates / height,
					halfCoordinates - lengthCoordinates * area.left / width,
					halfCoordinates - lengthCoordinates * area.top / height
				);
				break;
			case 90:
				areasRect = new Rect(
					-halfCoordinates + area.top * lengthCoordinates / height,
					halfCoordinates - area.right * lengthCoordinates / width,
					-halfCoordinates + lengthCoordinates * area.bottom / height,
					halfCoordinates - lengthCoordinates * area.left / width
				);
				break;
			case 270:
				areasRect = new Rect(
					halfCoordinates - area.bottom * lengthCoordinates / height,
					-halfCoordinates + area.left * lengthCoordinates / width,
					halfCoordinates - lengthCoordinates * area.top / height,
					-halfCoordinates + lengthCoordinates * area.right / width
				);
				break;
			default:
				throw new IllegalArgumentException();
		}

		focusAreas.add( new Camera.Area( areasRect, 800 ) );
		if( parameters.getMaxNumFocusAreas() >= focusAreas.size() ) {
			parameters.setFocusAreas( focusAreas );
		}
		if( parameters.getMaxNumMeteringAreas() >= focusAreas.size() ) {
			parameters.setMeteringAreas( focusAreas );
		}

		parameters.setFocusMode( mode );

		// Commit the camera parameters
		camera.setParameters( parameters );
	}

	protected abstract BaseSurfaceView getSurfaceViewWithOverlay();

	protected abstract IRecognitionService getCaptureService();

	protected abstract boolean createCaptureService();

	// Start autofocus (used when continuous autofocus is disabled)
	protected void autoFocus( Camera.AutoFocusCallback callback )
	{
		if( camera != null && !cameraInitialisation ) {
			try {
				setCameraFocusMode( Camera.Parameters.FOCUS_MODE_AUTO );
				camera.autoFocus( callback );
			} catch( Exception e ) {
				Log.e( getString( ResourcesUtils.getResId( "string", "app_name", this ) ), "Error: " + e.getMessage() );
			}
		}
	}

	protected void updateStartButton(State state) {
		this.state = state;
		switch( state ) {
			case Start:
				startButton.setText( ResourcesUtils.getResId( "string", "cordova_rtr_start", BaseActivity.this ) );
				break;
			case Starting:
				startButton.setText( ResourcesUtils.getResId( "string", "cordova_rtr_starting", BaseActivity.this ) );
				break;
			case Stop:
				startButton.setText( ResourcesUtils.getResId( "string", "cordova_rtr_stop", BaseActivity.this ) );
				break;
		}
	}

	// Enable 'Start' button and switching to continuous focus mode (if possible) when autofocus completes
	protected Camera.AutoFocusCallback finishCameraInitialisationAutoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus( boolean success, Camera camera )
		{
			cameraInitialisation = false;
			onAutoFocusFinished( success, camera );
			updateStartButton( State.Start );
			startButton.setEnabled( true );
			if( startRecognitionWhenReady ) {
				startRecognition();
				startRecognitionWhenReady = false;
			}
		}
	};

	protected void onAutoFocusFinished( boolean success, Camera camera )
	{
		if( isContinuousVideoFocusModeEnabled( camera ) ) {
			setCameraFocusMode( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO );
		} else {
			if( !success ) {
				autoFocus( simpleCameraAutoFocusCallback );
			}
		}
	}

	// Checks that FOCUS_MODE_CONTINUOUS_VIDEO supported
	protected boolean isContinuousVideoFocusModeEnabled( Camera camera )
	{
		return camera.getParameters().getSupportedFocusModes().contains( Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO );
	}

	// Simple autofocus callback
	protected Camera.AutoFocusCallback simpleCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus( boolean success, Camera camera )
		{
			onAutoFocusFinished( success, camera );
		}
	};

	// Start recognition when autofocus completes (used when continuous autofocus is not enabled)
	protected Camera.AutoFocusCallback startRecognitionCameraAutoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus( boolean success, Camera camera )
		{
			onAutoFocusFinished( success, camera );
			startRecognition();
		}
	};

	// Autofocus by tap
	protected View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick( View v )
		{
			// if BUTTON_TEXT_STARTING autofocus is already in progress, it is incorrect to interrupt it
			if( state != State.Starting ) {
				autoFocus( simpleCameraAutoFocusCallback );
			}
		}
	};

	// Stop preview and release the camera
	protected void stopPreviewAndReleaseCamera()
	{
		if( camera != null ) {
			camera.setPreviewCallbackWithBuffer( null );
			stopPreview();
			camera.release();
			camera = null;
		}
	}

	// Stop preview if it is running
	private void stopPreview()
	{
		if( inPreview ) {
			camera.stopPreview();
			inPreview = false;
		}
	}

	private BackgroundWorker.Callback<Void, Void> recognitionStopCallback = new BackgroundWorker.Callback<Void, Void>() {
		@Override
		public Void doWork( Void aVoid, BackgroundWorker<Void, Void> worker )
		{
			getCaptureService().stop();
			return null;
		}

		@Override
		public void onDone( Void result, Exception exception, BackgroundWorker<Void, Void> worker )
		{
			if( previewSurfaceHolder != null ) {
				// Restore normal power saving behaviour
				previewSurfaceHolder.setKeepScreenOn( false );
			}
			// Change the text on the stop button back to 'Start'
			updateStartButton( State.Start );
			startButton.setEnabled( true );
		}
	};

	// Stop recognition
	protected void stopRecognition()
	{
		// Disable the 'Stop' button
		startButton.setEnabled( false );

		// Stop the service asynchronously to make application more responsive. Stopping can take some time
		// waiting for all processing threads to stop
		new BackgroundWorker<>( new WeakReference<>( recognitionStopCallback ) ).execute();
	}

	protected void startRecognition()
	{
		// Do not switch off the screen while data capture service is running
		getSurfaceViewWithOverlay().setKeepScreenOn( true );
		// Get area of interest (in coordinates of preview frames)
		Rect areaOfInterest = new Rect( getSurfaceViewWithOverlay().getAreaOfInterest() );
		// Start the service
		getCaptureService().start( cameraPreviewSize.width, cameraPreviewSize.height, orientation, areaOfInterest );
		// Change the text on the start button to 'Stop'
		updateStartButton( State.Stop );
		startButton.setEnabled( true );
	}

	protected void putErrorIfEssential( HashMap<String, Object> json )
	{
		if( errorOccurred != null ) {
			HashMap<String, String> errorInfo = new HashMap<>();
			errorInfo.put( "description", errorOccurred );

			json.put( "error", errorInfo );
		}
	}

	public void onCancelButtonClick( View view )
	{
		stopRecognition();
		clearRecognitionResults();

		new Handler().postDelayed( new Runnable() {
			public void run()
			{
				Intent intent = new Intent();

				HashMap<String, String> resultInfo = new HashMap<>();
				resultInfo.put( "userAction", "Canceled" );

				HashMap<String, Object> json = new HashMap<>();
				json.put( "resultInfo", resultInfo );

				putErrorIfEssential( json );

				intent.putExtra( RtrPlugin.INTENT_RESULT_KEY, json );
				BaseActivity.this.setResult( RtrPlugin.RESULT_OK, intent );
				BaseActivity.this.finish();
			}
		}, 0 );
	}

	protected void onStartupError( Throwable e )
	{
		errorOccurred = e.getMessage();
		Log.e( getString( ResourcesUtils.getResId( "string", "app_name", this ) ), "Error creating recognition service", e );
		Intent intent = new Intent();

		HashMap<String, Object> json = new HashMap<>();
		putErrorIfEssential( json );

		intent.putExtra( RtrPlugin.INTENT_RESULT_KEY, json );
		setResult( RtrPlugin.RESULT_FAIL, intent );
		finish();
	}

	public void onFlashButtonClick( View view )
	{
		if( camera == null ) {
			return;
		}
		ImageButton flashButton = (ImageButton) view;

		Camera.Parameters params = camera.getParameters();
		if( !isFlashLightOn ) {
			params.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
			isFlashLightOn = true;
			flashButton.setImageResource( ResourcesUtils.getResId( "drawable", "ic_flash_off_24dp", this ) );
		} else {
			params.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
			isFlashLightOn = false;
			flashButton.setImageResource( ResourcesUtils.getResId( "drawable", "ic_flash_on_24dp", this ) );
		}
		camera.setParameters( params );
	}

	protected abstract void checkPreferences();

	protected abstract void setPreferenceButtonText();

	protected void configureUi()
	{
		if( RtrManager.isLanguageSelectionEnabled() ) {
			preferenceButton.setVisibility( View.VISIBLE );
		} else {
			preferenceButton.setVisibility( View.INVISIBLE );
		}

		boolean hasFlash = this.getPackageManager().hasSystemFeature( PackageManager.FEATURE_CAMERA_FLASH );
		if( RtrManager.isFlashlightVisible() && hasFlash ) {
			flashButton.setVisibility( View.VISIBLE );
		} else {
			flashButton.setVisibility( View.INVISIBLE );
		}

		if( RtrManager.isStopButtonVisible() ) {
			startButton.setVisibility( View.VISIBLE );
		} else {
			startButton.setVisibility( View.INVISIBLE );
		}

		areaOfInterestMargin_PercentOfHeight = convertRatio( RtrManager.getRatioHeight() );
		areaOfInterestMargin_PercentOfWidth = convertRatio( RtrManager.getRatioWidth() );
	}

	private int convertRatio( float percents )
	{
		return ( 100 - (int) ( percents * 100 ) ) / 2;
	}

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		if( RtrManager.getOrientation() != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ) {
			setRequestedOrientation( RtrManager.getOrientation() );
		}

		requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN );

		setContentView( ResourcesUtils.getResId( "layout", "activity_capture", this ) );

		// Retrieve some ui components
		warningTextView = findViewById( ResourcesUtils.getResId( "id", "warningText", this ) );
		errorTextView = findViewById( ResourcesUtils.getResId( "id", "errorText", this ) );
		startButton = findViewById( ResourcesUtils.getResId( "id", "startButton", this ) );
		preferenceButton = findViewById( ResourcesUtils.getResId( "id", "preferenceButton", this ) );
		flashButton = findViewById( ResourcesUtils.getResId( "id", "flashButton", this ) );
		scenarioDescription = findViewById( ResourcesUtils.getResId( "id", "scenarioDescription", this ) );

		configureUi();

		checkPreferences();
		setPreferenceButtonText();

		// Manually create preview surface. The only reason for this is to
		// avoid making it public top level class
		RelativeLayout layout = (RelativeLayout) startButton.getParent();

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.MATCH_PARENT,
			RelativeLayout.LayoutParams.MATCH_PARENT );
		getSurfaceViewWithOverlay().setLayoutParams( params );
		// Add the surface to the layout as the bottom-most view filling the parent
		layout.addView( getSurfaceViewWithOverlay(), 0 );

		// Create data capture service
		if( createCaptureService() ) {
			// Set the callback to be called when the preview surface is ready.
			// We specify it as the last step as a safeguard so that if there are problems
			// loading the engine the preview will never start and we will never attempt calling the service
			getSurfaceViewWithOverlay().getHolder().addCallback( surfaceCallback );
		}
		layout.setOnClickListener( clickListener );
	}

	@Override
	public void onResume()
	{
		super.onResume();

		// Reinitialize the camera, restart the preview and recognition if required
		startButton.setEnabled( false );
		clearRecognitionResults();
		startRecognitionWhenReady = startRecognitionOnAppStart;
		camera = Camera.open();
		if( previewSurfaceHolder != null ) {
			setCameraPreviewDisplayAndStartPreview();
		}

		orientationEventListener = new DisplayManager.DisplayListener() {
			int oldRotation = -5;

			void eventHandler()
			{
				int update = getWindowManager().getDefaultDisplay().getRotation();
				if( update != oldRotation ) {
					oldRotation = update;
					if( getCaptureService() != null ) {
						getCaptureService().stop();
					}
					startRecognitionWhenReady = startRecognitionOnAppStart;
					configureCameraAndStartPreview( camera );
				}
			}

			@Override
			public void onDisplayAdded( int displayId )
			{
				eventHandler();
			}

			@Override
			public void onDisplayChanged( int displayId )
			{
				eventHandler();
			}

			@Override
			public void onDisplayRemoved( int displayId )
			{
				eventHandler();
			}
		};

		DisplayManager displayManager = (DisplayManager) this.getSystemService( Context.DISPLAY_SERVICE );
		displayManager.registerDisplayListener( orientationEventListener, new Handler( getMainLooper() ) );
	}

	@Override
	public void onPause()
	{
		if( orientationEventListener != null ) {
			DisplayManager displayManager = (DisplayManager) this.getSystemService( Context.DISPLAY_SERVICE );
			displayManager.unregisterDisplayListener( orientationEventListener );
			orientationEventListener = null;
		}
		// Clear all pending actions
		handler.removeCallbacksAndMessages( null );
		// Stop the data capture service
		if( getCaptureService() != null ) {
			getCaptureService().stop();
		}
		updateStartButton( State.Start );
		// Clear recognition results
		clearRecognitionResults();
		stopPreviewAndReleaseCamera();
		super.onPause();
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );
		checkPreferences();
		setPreferenceButtonText();
	}

}
