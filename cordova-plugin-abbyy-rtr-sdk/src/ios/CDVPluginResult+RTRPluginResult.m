/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "CDVPluginResult+RTRPluginResult.h"
#import "NSDictionary+RTRSettings.h"

extern NSString* const RTRCallbackErrorKey;
extern NSString* const RTRCallbackErrorDescriptionKey;
extern NSString* const RTRCallbackResultInfoKey;
extern NSString* const RTRCallbackUserActionKey;

@implementation CDVPluginResult (RTRPluginResult)

+ (CDVPluginResult*)rtrResultWithError:(NSError*)error
{
	NSDictionary* errorDictionary = @{
		RTRCallbackErrorDescriptionKey : error.localizedDescription ?: @""
	};

	NSDictionary* pluginResult = @{RTRCallbackErrorKey : errorDictionary};

	return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:pluginResult];
}

static NSDictionary* resultInfo(RTRViewController* controller, BOOL stoppedByUser)
{
	NSMutableDictionary* info = [@{
		@"stabilityStatus" : [NSString rtr_stringFromStabilityStatus:controller.currentStabilityStatus],
		@"frameSize" : [NSString stringWithFormat:@"%.lf %.lf", controller.imageBufferSize.width, controller.imageBufferSize.height],
	} mutableCopy];
	if(stoppedByUser) {
		info[RTRCallbackUserActionKey] = @"Manually Stopped";
	}

	return info;
}

+ (CDVPluginResult*)rtrResultForTextCapture:(RTRTextCaptureViewController*)textCaptureController
	stoppedByUser:(BOOL)stoppedByUser
{
	NSMutableDictionary* info = [resultInfo(textCaptureController, stoppedByUser) mutableCopy];
	info[@"recognitionLanguages"] = textCaptureController.selectedRecognitionLanguages.allObjects ?: @[];
	NSDictionary* result = @{
		RTRCallbackResultInfoKey : info,
		@"textLines" :  [textCaptureController.textLines rtr_map:^id _Nonnull(RTRTextLine* line) {
			return [NSDictionary rtr_dictionaryFromTextLine:line];
		}] ?: @[]
	};

	return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
}

+ (CDVPluginResult*)rtrResultForDataCapture:(RTRDataCaptureViewController*)dataCaptureController
	stoppedByUser:(BOOL)stoppedByUser
{
	NSDictionary* result = @{
		RTRCallbackResultInfoKey : resultInfo(dataCaptureController, stoppedByUser),
		@"dataScheme" : @{
			@"id" : dataCaptureController.dataScheme.id ?: @"",
			@"name" : dataCaptureController.dataScheme.name ?: @"",
		},
		@"dataFields" : [dataCaptureController.dataFields rtr_map:^id(RTRDataField* field) {
			return [NSDictionary rtr_dictionaryFromDataField:field];
		}] ?: @[]
	};
	
	return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
}

@end
