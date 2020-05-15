// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>

#import <AbbyyRtrSDK/AbbyyRtrSDK.h>
#import <AbbyyUI/AbbyyUI.h>
#import "RTRImageCapturePluginAdapter.h"

NS_ASSUME_NONNULL_BEGIN

/// Root controller
@interface RTRImageCaptureViewController : AUICaptureController

@property (nonatomic, copy) void (^onCancel)(id<AUIMultiPageImageCaptureResult>);
@property (nonatomic, copy) void (^onSuccess)(id<AUIMultiPageImageCaptureResult>);
@property (nonatomic, copy) void (^onError)(id<AUIMultiPageImageCaptureResult>, NSError* error);

- (nullable instancetype)initWithEngine:(RTREngine*)engine
	localizer:(id<RTRLocalizer>)localizer
	settings:(RTRImageCaptureSettings*)settings
	error:(NSError**)error;

@end

NS_ASSUME_NONNULL_END
