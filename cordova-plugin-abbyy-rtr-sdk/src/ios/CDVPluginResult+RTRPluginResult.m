/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import "CDVPluginResult+RTRPluginResult.h"

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

static NSString* stringFromPoint(CGPoint point)
{
	return [NSString stringWithFormat:@"%.lf %.lf", point.x, point.y];
}

static NSString* stringFromCGRect(CGRect rect)
{
	if(CGRectIsEmpty(rect)) {
		return @"";
	}

	return [NSString stringWithFormat:@"%.lf %.lf %.lf %.lf", CGRectGetMinX(rect), CGRectGetMinY(rect), CGRectGetWidth(rect), CGRectGetHeight(rect)];
}

static NSString* strinfFromQuadrangle(NSArray* quadrangle)
{
	if(quadrangle.count == 0) {
		return @"";
	}

	NSMutableArray* quadrangleParts = [NSMutableArray arrayWithCapacity:quadrangle.count];
	for(NSValue* point in quadrangle) {
		[quadrangleParts addObject:stringFromPoint([point CGPointValue])];
	}

	return [quadrangleParts componentsJoinedByString:@" "];
}

static NSDictionary* dictionaryFromTextLine(RTRTextLine* textLine)
{
	return @{
		@"text" : textLine.text ?: @"",
		@"rect" : stringFromCGRect(textLine.rect),
		@"quadrangle" : strinfFromQuadrangle(textLine.quadrangle),
	};
}

static NSArray* arrayFromTextLines(NSArray<RTRTextLine*>* textLines)
{
	if(textLines.count == 0) {
		return @[];
	}

	NSMutableArray* result = [NSMutableArray arrayWithCapacity:textLines.count];
	for(RTRTextLine* line in textLines) {
		[result addObject:dictionaryFromTextLine(line)];
	}

	return result;
}

static NSDictionary* dictionaryFromDataField(RTRDataField* field)
{
	return @{
		@"id" : field.id ?: @"",
		@"name" : field.name ?: @"",
		@"text" : field.text ?: @"",
		@"quadrangle" : strinfFromQuadrangle(field.quadrangle),
		@"components" : arrayFromDataFields(field.components),
	};
}

static NSArray* arrayFromDataFields(NSArray<RTRDataField*>* dataFields)
{
	if(dataFields.count == 0) {
		return @[];
	}

	NSMutableArray* result = [NSMutableArray arrayWithCapacity:dataFields.count];
	for(RTRDataField* field in dataFields) {
		[result addObject:dictionaryFromDataField(field)];
	}

	return result;
}

static NSString* NSStringFromStabilityStatus(RTRResultStabilityStatus stabilityStatus)
{
	switch(stabilityStatus) {
		case RTRResultStabilityNotReady:
			return @"NotReady";
			break;
		case RTRResultStabilityTentative:
			return @"Tentative";
			break;
		case RTRResultStabilityVerified:
			return @"Verified";
			break;
		case RTRResultStabilityAvailable:
			return @"Available";
			break;
		case RTRResultStabilityTentativelyStable:
			return @"TentativelyStable";
			break;
		case RTRResultStabilityStable:
			return @"Stable";
			break;
	}
}

static NSDictionary* resultInfo(RTRViewController* controller, BOOL stoppedByUser)
{
	NSMutableDictionary* info = [@{
		@"stabilityStatus" : NSStringFromStabilityStatus(controller.currentStabilityStatus),
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
		@"textLines" : arrayFromTextLines(textCaptureController.textLines),
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
		@"dataFields" : arrayFromDataFields(dataCaptureController.dataFields),
	};
	
	return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
}

@end
