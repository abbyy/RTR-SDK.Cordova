/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>
#import <AbbyyUI/AbbyyUI.h>
#import "RTRPluginConstants.h"

NS_ASSUME_NONNULL_BEGIN

@protocol RTRLocalizer;

@interface RTRImageCapturePluginAdapter : NSObject

/// ABBYY Mobile Capture SDK Engine.
@property (nonatomic, strong, readonly) RTREngine* engine;

- (instancetype)initWithEngine:(RTREngine*)engine;

- (void)startImageCapture:(NSDictionary*)query
	rootController:(UIViewController*)rootController
	localizer:(id<RTRLocalizer>)localizer
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

@end

@interface RTRImageCaptureSettings : NSObject

@property (nonatomic, readonly) BOOL isFlashlightButtonVisible;
@property (nonatomic, readonly) BOOL isCaptureButtonVisible;
@property (nonatomic, readonly) BOOL isGalleryButtonVisible;
@property (nonatomic, readonly) BOOL shouldShowPreview;
@property (nonatomic, readonly) NSInteger requiredPageCount;
@property (nonatomic, readonly) UIInterfaceOrientationMask supportedOrientations;
@property (nonatomic, readonly) AUICameraResolution cameraResolution;
@property (nonatomic, readonly) RTRImageDestinationType destination;

@property (nonatomic, readonly) RTRImageCaptureEncodingType exportType;
@property (nonatomic, readonly) RTRCoreAPIExportCompressionLevel compressionLevel;
// defaultImageSettings
@property (nonatomic, readonly) CGSize documentSize;
@property (nonatomic, readonly) CGFloat aspectRatioMin;
@property (nonatomic, readonly) CGFloat aspectRatioMax;
@property (nonatomic, readonly) CGFloat minimumDocumentToViewRatio;
@property (nonatomic, readonly) NSInteger imageFromGalleryMaxSize;

- (nullable instancetype)initWithDictionary:(NSDictionary*)dict error:(NSError**)error;

@end


@protocol RTRLocalizer <NSObject>

- (NSString*)localizedStringForKey:(NSString*)key;

@end

NS_ASSUME_NONNULL_END
