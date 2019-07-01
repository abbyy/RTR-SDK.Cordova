package com.abbyy.mobile.rtr.cordova;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.uicomponents.CaptureView;
import com.abbyy.mobile.uicomponents.scenario.ImageCaptureScenario;

public class ImageCaptureSettings {

	public enum Destination {
		BASE64, FILE
	}
	public enum ExportType {
		JPG, PNG, PDF
	}

	public static boolean flashlightVisible = false;
	public static boolean manualCaptureVisible = false;
	public static CaptureView.CameraSettings.Resolution cameraResolution = CaptureView.CameraSettings.Resolution.HD;
	public static boolean showResultOnCapture = false;
	public static int pageCount = 0;
	public static Destination destination = Destination.FILE;
	public static ExportType exportType = ExportType.PNG;
	public static IImagingCoreAPI.ExportOperation.CompressionType compressionType = IImagingCoreAPI.ExportOperation.CompressionType.Jpg;
	public static IImagingCoreAPI.ExportOperation.Compression compressionLevel = IImagingCoreAPI.ExportOperation.Compression.Low;
	public static boolean cropEnabled = true;
	public static float documentToViewRatio = 0.15f;
	public static ImageCaptureScenario.DocumentSize documentSize = ImageCaptureScenario.DocumentSize.ANY;
}
