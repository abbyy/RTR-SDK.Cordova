/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Cordova/CDVPluginResult.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

#import "RTRTextCaptureViewController.h"
#import "RTRDataCaptureViewController.h"

@interface CDVPluginResult (RTRPluginResult)

+ (CDVPluginResult*)rtrResultWithError:(NSError*)error;
+ (CDVPluginResult*)rtrResultForTextCapture:(RTRTextCaptureViewController*)textCaptureController
	stoppedByUser:(BOOL)stoppedByUser;
+ (CDVPluginResult*)rtrResultForDataCapture:(RTRDataCaptureViewController*)dataCaptureController
	stoppedByUser:(BOOL)stoppedByUser;

+ (CDVPluginResult*)rtrResultForCoreApiTextCapture:(NSArray<RTRTextBlock*>*)blocks;
+ (CDVPluginResult*)rtrResultForCoreApiDataCapture:(NSArray<RTRDataField*>*)fields;

@end
