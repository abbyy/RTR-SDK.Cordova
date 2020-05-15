/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, RTRImageDestinationType) {
	RTRImageDestinationTypeBase64,
	RTRImageDestinationTypeFile,
};

typedef NS_ENUM(NSUInteger, RTRImageCaptureEncodingType) {
	RTRImageCaptureEncodingTypeJpeg2000,
	RTRImageCaptureEncodingTypeJpg,
	RTRImageCaptureEncodingTypePng,
	RTRImageCaptureEncodingTypePdf,
};

#pragma mark - common constants

extern NSString* const RTRPluginErrorDomain;
extern NSString* const RTRRecognitionLanguagesKey;
extern NSString* const RTRSelectableRecognitionLanguagesKey;
extern NSString* const RTRLicenseFileNameKey;
extern NSString* const RTRStopWhenStableKey;
extern NSString* const RTRIsStopButtonVisibleKey;
extern NSString* const RTRAreaOfInterestKey;
extern NSString* const RTRIsFlashlightVisibleKey;
extern NSString* const RTRCustomDataCaptureScenarioKey;
extern NSString* const RTRCustomDataCaptureScenarioNameKey;
extern NSString* const RTRCustomDataCaptureFieldsKey;
extern NSString* const RTRCustomDataCaptureRegExKey;
extern NSString* const RTRScenarioDescriptionKey;
extern NSString* const RTRDataCaptureProfileKey;
extern NSString* const RTRExtendedSettingsKey;
extern NSString* const RTRDefaultRecognitionLanguage;
extern NSString* const RTROrientationPolicy;

extern NSString* const RTRCallbackErrorKey;
extern NSString* const RTRCallbackErrorDescriptionKey;
extern NSString* const RTRCallbackResultInfoKey;
extern NSString* const RTRCallbackUserActionKey;

#pragma mark - image capture constants

extern NSString* const RTRICCameraResolutionKey;
extern NSString* const RTRICFlashlightButtonVisibleKey;
extern NSString* const RTRICCaptureButtonVisibleKey;
extern NSString* const RTRICGalleryButtonVisibleKey;
extern NSString* const RTRICImageFromGalleryMaxSize;
extern NSString* const RTRICAspectRatioMin;
extern NSString* const RTRICAspectRatioMax;
extern NSString* const RTRICShowPreviewKey;
extern NSString* const RTRICImagesCountKey;
extern NSString* const RTRICRequiredPageCountKey;
extern NSString* const RTRICCompressionLevelKey;
extern NSString* const RTRICExportTypeKey;
extern NSString* const RTRICDestinationKey;
extern NSString* const RTRICDefaultImageSettingsKey;
extern NSString* const RTRICDocumentSizeKey;
extern NSString* const RTRICMinimumDocumentToViewRatioKey;

#pragma mark - core api constangs

extern NSString* const RTRCAProgressPercentage;
extern NSString* const RTRCAProgressWarning;
extern NSString* const RTRCAEnableTextOrientationDetection;
extern NSString* const RTRCAImageUri;
extern NSString* const RTRCAImageSize;
extern NSString* const RTRCARotationAngleDegrees;
extern NSString* const RTRCADocumentBoundary;
extern NSString* const RTRCAImageResolution;
extern NSString* const RTRCABoundaryDetectionMode;
extern NSString* const RTRCAExportResultOptions;
extern NSString* const RTRCAExportFilePath;
extern NSString* const RTRCAExportPdfImages;
extern NSString* const RTRCAPdfUri;

NS_ASSUME_NONNULL_END
