// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>

#import <AbbyyRtrSDK/AbbyyRtrSDK.h>
#import <AbbyyUI/AbbyyUI.h>
#import "RTRMultipageScenarioConfiguration.h"

NS_ASSUME_NONNULL_BEGIN

/// Root controller
@interface RTRImageCaptureViewController : AUICaptureController

@property (nonatomic, copy) void (^onCancel)(void);
@property (nonatomic, copy) void (^onSuccess)(NSArray<NSDictionary*>* capturedData);

@property (nonatomic, strong) NSString* errorOccurred;
@property (nonatomic, strong) RTRMultipageScenarioConfiguration* config;

@end

NS_ASSUME_NONNULL_END
