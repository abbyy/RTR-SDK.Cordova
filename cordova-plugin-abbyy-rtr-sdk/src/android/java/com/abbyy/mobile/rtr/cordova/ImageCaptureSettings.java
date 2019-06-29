package com.abbyy.mobile.rtr.cordova;

import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;

public class ImageCaptureSettings {

	enum Destination {
		BASE64, FILE
	}
	enum ExportType {
		JPG, PNG, PDF
	}
	enum CompressionType {
		JPG
	}
	enum CompressionLevel {
		Low, Medium, High, ExtraHigh
	}

	public static boolean flashlightVisible;
	public static boolean manualCaptureVisible;
	public static int orientation;
	public static CaptureView.CameraSettings.Resolution cameraResolution;
	public static Destination destination;
	public static ExportType exportType;
	public static CompressionType compressionType;
	public static CompressionLevel compressionLevel;
	public static boolean cropEnabled;
	public static float documentToViewRatio;
	public static ImageCaptureScenario.DocumentSize documentSize;
}
