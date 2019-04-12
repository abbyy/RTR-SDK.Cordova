/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

@interface RTRPlugin : CDVPlugin

- (void)startTextCapture:(CDVInvokedUrlCommand*)command;
- (void)startDataCapture:(CDVInvokedUrlCommand*)command;

@end
