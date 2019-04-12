/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

@class RTRDataCaptureScenario;

@interface RTRManager : NSObject

+ (instancetype)managerWithLicense:(NSString*)licenseName error:(NSError**)error;

- (id<RTRTextCaptureService>)textCaptureServiceWithLanguages:(NSSet*)languages
	delegate:(id<RTRTextCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings;

- (id<RTRDataCaptureService>)dataCaptureServiceWithProfile:(NSString*)profile
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings;

- (id<RTRDataCaptureService>)customDataCaptureServiceWithScenario:(RTRDataCaptureScenario*)scenario
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings;

@end
