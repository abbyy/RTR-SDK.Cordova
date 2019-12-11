/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>
#import <AbbyyUI/AbbyyUI.h>
#import "RTRPlugin.h"
//#import "RTRDocumentManager.h"
//#import "RTRMultipageScenarioStorage.h"
#import "RTRManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface RTRMultipageScenarioConfiguration : UIView

@property (nonatomic, readonly) BOOL isFlashlightButtonVisible;
@property (nonatomic, readonly) BOOL isCaptureButtonVisible;
@property (nonatomic, readonly) BOOL shouldShowPreview;
@property (nonatomic, readonly) NSInteger maxImagesCount;
@property (nonatomic, readonly) UIInterfaceOrientationMask supportedOrientations;
@property (nonatomic, readonly) AUICameraResolution cameraResolution;
@property (nonatomic, readonly) RTRImageCaptureDestintationType destination;

@property (nonatomic, readonly) RTRImageCaptureEncodingType exportType;
@property (nonatomic, readonly) RTRCoreAPIPdfExportCompressionType compressionType;
@property (nonatomic, readonly) RTRCoreAPIExportCompressionLevel compressionLevel;
// defaultImageSettings
@property (nonatomic, readonly) CGFloat minimumDocumentToViewRatio;
@property (nonatomic, readonly) CGSize documentSize;
@property (nonatomic, readonly) BOOL cropEnabled;

- (instancetype)initWithManager:(RTRManager*)manager args:(NSDictionary*)args;

- (RTREngine*)engine;
- (AUIMultiPageImageCaptureScenario*)scenario;
- (void)exportResult:(id<AUIMultiPageImageCaptureResult>)result withCompletion:(void(^)(NSDictionary*, NSError*))completion;

@end

NS_ASSUME_NONNULL_END
