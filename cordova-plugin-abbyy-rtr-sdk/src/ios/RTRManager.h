/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>
#import <AbbyyRtrSdk/AbbyyRtrSdk.h>

@class RTRDataCaptureScenario;

@interface RTRManager : NSObject

+ (instancetype)managerWithLicense:(NSString*)licenseName error:(NSError**)error;

- (id<RTRTextCaptureService>)textCaptureServiceWithLanguages:(NSSet*)languages
	delegate:(id<RTRTextCaptureServiceDelegate>)delegate;

- (id<RTRDataCaptureService>)dataCaptureServiceWithProfile:(NSString*)profile
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate;

- (id<RTRDataCaptureService>)customDataCaptureServiceWithScenario:(RTRDataCaptureScenario*)scenario
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate;

@end
