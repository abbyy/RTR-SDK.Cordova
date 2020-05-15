/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "RTRPluginConstants.h"

@interface RTRPlugin : CDVPlugin

- (void)startTextCapture:(CDVInvokedUrlCommand*)command;
- (void)startDataCapture:(CDVInvokedUrlCommand*)command;
- (void)startImageCapture:(CDVInvokedUrlCommand*)command;

@end

@interface NSDictionary (rtr_Plugin)

- (RTRImageCaptureEncodingType)rtr_exportTypeForKey:(NSString*)key;
- (RTRImageDestinationType)rtr_destinationTypeForKey:(NSString*)key;

@end
