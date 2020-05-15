// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.javascript;

import com.abbyy.mobile.rtr.IImagingCoreAPI;
import com.abbyy.mobile.rtr.javascript.image.Destination;
import com.abbyy.mobile.rtr.javascript.image.ExportType;

import java.util.HashMap;
import java.util.Map;

public class JSConstants {

	public static final String LICENSE_FILE_NAME_KEY = "licenseFileName";
	public static final String DEFAULT_LICENSE_FILENAME = "MobileCapture.License";
	public static final String RECOGNITION_LANGUAGES_KEY = "recognitionLanguages";
	public static final String AREA_OF_INTEREST_KEY = "areaOfInterest";
	public static final String DOCUMENT_BOUNDARY_KEY = "documentBoundary";
	public static final String TEXT_ORIENTATION_FLAG_KEY = "isTextOrientationDetectionEnabled";
	public static final String IMAGE_URI_KEY = "imageUri";
	public static final String FILE_PATH = "filePath";

	public static final String TEXT_BLOCKS = "textBlocks";
	public static final String TEXT_LINES = "textLines";

	public static final String TEXT = "text";
	public static final String QUADRANGLE = "quadrangle";
	public static final String RECT = "rect";
	public static final String ORIENTATION = "orientation";
	public static final String WARNINGS = "warnings";
	public static final String CHAR_INFO = "charInfo";
	public static final String IS_UNCERTAIN = "isUncertain";

	public static final String DETECTION_MODE = "detectionMode";
	public static final Map<String, IImagingCoreAPI.DetectDocumentBoundaryOperation.DetectionMode> DETECTION_MODE_VALUES =
		new HashMap<String, IImagingCoreAPI.DetectDocumentBoundaryOperation.DetectionMode>() {{
			put( "default", IImagingCoreAPI.DetectDocumentBoundaryOperation.DetectionMode.Default );
			put( "fast", IImagingCoreAPI.DetectDocumentBoundaryOperation.DetectionMode.Fast );
		}};

	public static final String DESTINATION = "destination";
	public static final Map<String, Destination> DESTINATION_VALUES = new HashMap<String, Destination>() {{
		put( "file", Destination.File );
		put( "base64", Destination.Base64 );
	}};

	public static final String EXPORT_TYPE = "exportType";
	public static final Map<String, ExportType> EXPORT_TYPE_VALUES = new HashMap<String, ExportType>() {{
		put( "jpg", ExportType.JPG );
		put( "png", ExportType.PNG );
	}};

	public static final String COMPRESSION_LEVEL = "compressionLevel";
	public static final Map<String, IImagingCoreAPI.ExportOperation.Compression> COMPRESSION_LEVEL_VALUES = new HashMap<String, IImagingCoreAPI.ExportOperation.Compression>() {{
		put( "low", IImagingCoreAPI.ExportOperation.Compression.Low );
		put( "normal", IImagingCoreAPI.ExportOperation.Compression.Normal );
		put( "high", IImagingCoreAPI.ExportOperation.Compression.High );
		put( "extrahigh", IImagingCoreAPI.ExportOperation.Compression.ExtraHigh );
	}};

	public static final String DOCUMENT_SIZE = "documentSize";
	public static final String PAGE_SIZE = "pageSize";

	public static final String ANGLE = "angle";

	public static final String PROFILE_KEY = "profile";

	public static final String QUALITY_ASSESSMENT_FOR_OCR_BLOCKS = "qualityAssessmentForOcrBlocks";

	public static final String RESULT = "result";

	public static final String IMAGE_SIZE = "imageSize";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";

	public static final String PDF_INFO = "pdfInfo";
}
