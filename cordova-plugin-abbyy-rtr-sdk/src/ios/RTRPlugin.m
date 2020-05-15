/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRPlugin.h"
#import "CDVPluginResult+RTRPluginResult.h"
#import "RTRTextCaptureViewController.h"
#import "RTRDataCaptureViewController.h"
#import "RTRDataCaptureScenario.h"
#import "NSDictionary+RTRSettings.h"

#import "RTRCoreApiPluginAdapter.h"
#import "RTRImageCapturePluginAdapter.h"
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>
#import <AbbyyUI/AbbyyUI.h>

#import "NSString+RTRPluginLocalization.h"
#import "RTRPluginConstants.h"

@interface RTRLocalizer : NSObject<RTRLocalizer>
@end

@implementation RTRLocalizer

- (NSString*)localizedStringForKey:(NSString*)key
{
	return key.rtr_localized;
}

@end

@interface RTRPlugin () <AUIImageCaptureScenarioDelegate>

@property (nonatomic) RTRViewController* rtrViewController;
@property (nonatomic) RTRManager* rtrManager;

@property (nonatomic) CDVInvokedUrlCommand* currentCommand;

@end

@implementation RTRPlugin

#pragma mark - Public

- (void)startTextCapture:(CDVInvokedUrlCommand*)command
{
	dispatch_async(dispatch_get_main_queue(), ^{
		if(![self initializeRtrManager:command]) {
			return;
		}

		NSDictionary* params = command.arguments.firstObject;

		NSDictionary* extendedSettings = params[RTRExtendedSettingsKey];
		NSArray<NSString*>* languages;
		NSArray<NSString*>* selectedLanguagesArray;
		if(extendedSettings[@"CustomRecognitionLanguages"] == nil) {
			languages = params[RTRSelectableRecognitionLanguagesKey];
			selectedLanguagesArray = params[RTRRecognitionLanguagesKey];
			if(selectedLanguagesArray.count == 0) {
				selectedLanguagesArray = @[RTRDefaultRecognitionLanguage];
			}
		}

		NSSet* selectedLanguages = [NSSet setWithArray:selectedLanguagesArray];

		RTRTextCaptureViewController* rtrViewController = [RTRTextCaptureViewController new];
		NSInteger enumValue;
		NSError* error;
		if(![params rtr_parseEnum:RTROrientationPolicy defaultValue:UIInterfaceOrientationMaskAll variants:NSDictionary.rtr_stringToOrientationMask outValue:&enumValue error:&error]) {
			[self.commandDelegate
				sendPluginResult:[CDVPluginResult rtrResultWithError:error]
				callbackId:command.callbackId];
			return;
		}
		rtrViewController.modalPresentationStyle = UIModalPresentationFullScreen;
		rtrViewController.supportedInterfaceOrientations = enumValue;
		rtrViewController.settingsTableContent = languages;
		rtrViewController.selectedRecognitionLanguages = [selectedLanguages mutableCopy];
		rtrViewController.languageSelectionEnabled = languages.count != 0;

		__weak RTRPlugin* weakSelf = self;
		__weak RTRTextCaptureViewController* textCaptureController = rtrViewController;
		rtrViewController.onSuccess = ^(BOOL isManuallyStopped) {
			CDVPluginResult* pluginResult = [CDVPluginResult rtrResultForTextCapture:textCaptureController stoppedByUser:isManuallyStopped];
			[textCaptureController.presentingViewController dismissViewControllerAnimated:YES completion:^{
				[weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
			}];
		};
		rtrViewController.extendedSettings = extendedSettings;

		[self presentCaptureViewController:rtrViewController command:command];
	});
}

- (BOOL)profileSupportsLanguageCustomization:(NSString*)profile manager:(RTRManager*)manager
{
	id<RTRDataCaptureService> service = [manager.engine createDataCaptureServiceWithDelegate:(id)self profile:profile];
	return [service configureDataCaptureProfile] != nil;
}

- (void)startDataCapture:(CDVInvokedUrlCommand*)command
{
	dispatch_async(dispatch_get_main_queue(), ^{
		if(![self initializeRtrManager:command]) {
			return;
		}

		NSDictionary* params = command.arguments.firstObject;
		NSDictionary* scenarioParams = params[RTRCustomDataCaptureScenarioKey];

		RTRDataCaptureViewController* rtrViewController = [RTRDataCaptureViewController new];
		NSInteger enumValue;
		NSError* error;
		if(![params rtr_parseEnum:RTROrientationPolicy defaultValue:UIInterfaceOrientationMaskAll variants:NSDictionary.rtr_stringToOrientationMask outValue:&enumValue error:&error]) {
			[self.commandDelegate
			 sendPluginResult:[CDVPluginResult rtrResultWithError:error]
			 callbackId:command.callbackId];
			return;
		}
		rtrViewController.supportedInterfaceOrientations = enumValue;
		rtrViewController.modalPresentationStyle = UIModalPresentationFullScreen;

		NSString* errorDescription = nil;

		if(scenarioParams != nil) {
			NSString* name = scenarioParams[RTRCustomDataCaptureScenarioNameKey];
			NSArray<NSString*>* languages = scenarioParams[RTRRecognitionLanguagesKey];
			if(languages.count == 0) {
				languages = @[RTRDefaultRecognitionLanguage];
			}
			NSString* description = scenarioParams[RTRScenarioDescriptionKey] ?: name;
			NSArray* fields = scenarioParams[RTRCustomDataCaptureFieldsKey];
			NSString* regEx = fields.firstObject[RTRCustomDataCaptureRegExKey];
			if(regEx.length == 0) {
				errorDescription = @"Invalid Data Capture scenario settings. Specify Reg Ex for Custom Data Capture scenario.";
			}

			rtrViewController.selectedScenario = [RTRDataCaptureScenario dataCaptureScenarioWithName:name regEx:regEx
				languages:[NSSet setWithArray:languages] description:description];
		} else if(params[RTRDataCaptureProfileKey] != nil) {
			rtrViewController.profile = params[RTRDataCaptureProfileKey];
			rtrViewController.settingsTableContent = params[RTRRecognitionLanguagesKey];
			BOOL hasLanguages = rtrViewController.settingsTableContent != nil;
			BOOL languagesUnsupported = ![self profileSupportsLanguageCustomization:rtrViewController.profile manager:self.rtrManager];
			if(hasLanguages && languagesUnsupported) {
				errorDescription = @"Language customization is not available for this profile.";
			}
		} else {
			errorDescription = @"Invalid Data Capture scenario settings. Specify Data Capture profile or params for Custom Data Capture Scenario.";
		}

		if(errorDescription.length != 0) {
			NSError* error = [NSError errorWithDomain:RTRPluginErrorDomain code:3 userInfo:@{
				NSLocalizedDescriptionKey : errorDescription
			}];
			CDVPluginResult* result = [CDVPluginResult rtrResultWithError:error];
			[self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
			return;
		}

		__weak RTRPlugin* weakSelf = self;
		__weak RTRDataCaptureViewController* dataCaptureController = rtrViewController;
		rtrViewController.onSuccess = ^(BOOL isManuallyStopped) {
			CDVPluginResult* pluginResult = [CDVPluginResult rtrResultForDataCapture:dataCaptureController stoppedByUser:isManuallyStopped];
			[dataCaptureController.presentingViewController dismissViewControllerAnimated:YES completion:^{
				[weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
			}];
		};

		rtrViewController.extendedSettings = params[RTRExtendedSettingsKey];

		[self presentCaptureViewController:rtrViewController command:command];
	});
}

- (void)startImageCapture:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	dispatch_async(dispatch_get_main_queue(), ^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		RTRImageCapturePluginAdapter* imageCapture = [[RTRImageCapturePluginAdapter alloc] initWithEngine:self.rtrManager.engine];
		[imageCapture
			startImageCapture:command.arguments.firstObject
			rootController:self.viewController
			localizer:[RTRLocalizer new]
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
		 	onSuccess:^(NSDictionary* response) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response]
					callbackId:command.callbackId];
			}
		];
	});
}

- (void)recognizeText:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			recognizeText:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
			onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)extractData:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			extractData:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
			onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)exportImage:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			exportImage:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
			onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)exportImagesToPdf:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			exportImagesToPdf:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
			onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)detectDocumentBoundary:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			detectBoundaryOnImage:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
		 	onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)assessQualityForOcr:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			assessOCRQualityOnImage:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
		 	onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)cropImage:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			cropImage:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
		 	onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

- (void)rotateImage:(CDVInvokedUrlCommand*)command
{
	__weak RTRPlugin* weakSelf = self;
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}
		NSArray* args = command.arguments;
		NSDictionary* query = args.firstObject;
		RTRCoreApiPluginAdapter* coreApi = [[RTRCoreApiPluginAdapter alloc] initWithEngine:self.rtrManager.engine];

		[coreApi
			rotateImage:query
			onError:^(NSError* error) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult rtrResultWithError:error]
					callbackId:command.callbackId];
			}
		 	onSuccess:^(NSDictionary* result) {
				[weakSelf.commandDelegate
					sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]
					callbackId:command.callbackId];
			}];
	}];
}

#pragma mark - Helpers

- (void)presentCaptureViewController:(RTRViewController*)rtrViewController command:(CDVInvokedUrlCommand*)command
{
	NSDictionary* params = command.arguments.firstObject;

	rtrViewController.rtrManager = self.rtrManager;
	rtrViewController.stopWhenStable = YES;
	if(params[RTRStopWhenStableKey] != nil) {
		rtrViewController.stopWhenStable = [params[RTRStopWhenStableKey] boolValue];
	}

	rtrViewController.isFlashlightVisible = YES;
	if(params[RTRIsFlashlightVisibleKey] != nil) {
		rtrViewController.isFlashlightVisible = [params[RTRIsFlashlightVisibleKey] boolValue];
	}

	rtrViewController.stopButtonVisible = YES;
	if(params[RTRIsStopButtonVisibleKey] != nil) {
		rtrViewController.stopButtonVisible = [params[RTRIsStopButtonVisibleKey] boolValue];
	}

	NSArray<NSString*>* parts = [params[RTRAreaOfInterestKey] componentsSeparatedByString:@" "];
	CGFloat widthPercentage = [parts.firstObject floatValue] ?: 0.8f;
	CGFloat heightPercentage = [parts.lastObject floatValue] ?: 0.3f;
	rtrViewController.areaOfInterestRatio = CGSizeMake(widthPercentage, heightPercentage);

	__weak RTRPlugin* weakSelf = self;
	__weak RTRViewController* weakController = rtrViewController;
	rtrViewController.onCancel = ^{
		NSMutableDictionary* result = [@{
			RTRCallbackResultInfoKey : @{
				RTRCallbackUserActionKey : @"Canceled"
			}
		} mutableCopy];

		if(weakController.errorOccurred != nil) {
			NSDictionary* errorDictionary = @{
				RTRCallbackErrorDescriptionKey : weakController.errorOccurred ?: @""
			};
			result[RTRCallbackErrorKey] = errorDictionary;
		}

		CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
		[weakController.presentingViewController dismissViewControllerAnimated:YES completion:^{
			[weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		}];
	};

	dispatch_async(dispatch_get_main_queue(), ^{
		[self.viewController presentViewController:rtrViewController animated:YES completion:nil];
	});
}

- (BOOL)initializeRtrManager:(CDVInvokedUrlCommand*)command
{
	NSString* licenseName = command.arguments.firstObject[RTRLicenseFileNameKey] ?: @"MobileCapture.License";
	NSError* error = nil;
	self.rtrManager = [RTRManager managerWithLicense:licenseName error:&error];

	if(self.rtrManager == nil) {
		if(error == nil) {
			error = [NSError errorWithDomain:RTRPluginErrorDomain code:2 userInfo:@{
				NSLocalizedDescriptionKey : @"Real-Time Recognition SDK isn't initialized. Please check your license file."
			}];
		}
		CDVPluginResult* result = [CDVPluginResult rtrResultWithError:error];
		[self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
		return NO;
	}

	return YES;
}

#pragma mark - AUIImageCaptureScenarioDelegate

- (void)captureScenario:(AUICaptureScenario*)scenario didFailWithError:(NSError*)error
{
	CDVPluginResult* result = [CDVPluginResult rtrResultWithError:error];
	[self.viewController dismissViewControllerAnimated:YES completion:^{
		[self.commandDelegate sendPluginResult:result callbackId:self.currentCommand.callbackId];
	}];
}

- (void)captureScenarioDidCancel:(AUICaptureScenario*)scenario
{
	NSMutableDictionary* result = [@{
		RTRCallbackResultInfoKey : @{
			RTRCallbackUserActionKey : @"Canceled"
		}
	} mutableCopy];

	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
	[self.viewController dismissViewControllerAnimated:YES completion:^{
		[self.commandDelegate sendPluginResult:pluginResult callbackId:self.currentCommand.callbackId];
	}];
}

- (void)captureScenario:(AUIImageCaptureScenario*)captureScenario didCaptureImageWithResult:(AUIImageCaptureResult*)result
{
	UIImage* image = result.image;
	NSData* jpegData = UIImageJPEGRepresentation(image, 0.7);
	NSString* base64Str = [jpegData base64EncodedStringWithOptions:0];
	NSMutableDictionary* resultDict = [@{
		RTRCallbackResultInfoKey : @{
			RTRCallbackUserActionKey : @"Succeeded"
		},
		@"imageBase64" : base64Str,
	} mutableCopy];

	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
	[self.viewController dismissViewControllerAnimated:YES completion:^{
		[self.commandDelegate sendPluginResult:pluginResult callbackId:self.currentCommand.callbackId];
	}];
}

@end

@implementation NSDictionary (rtr_Plugin)


- (RTRImageDestinationType)rtr_destinationTypeForKey:(NSString*)key
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"base64": @(RTRImageDestinationTypeBase64),
			@"file": @(RTRImageDestinationTypeFile)
		};
	}
	NSString* value = [self valueForKey:key];
	if(![value isKindOfClass:[NSString class]]) {
		return RTRImageDestinationTypeFile;
	}
	if([predefined valueForKey:value] != nil) {
		return (RTRImageDestinationType)[predefined[value] integerValue];
	}
	return RTRImageDestinationTypeFile;
}

- (RTRImageCaptureEncodingType)rtr_exportTypeForKey:(NSString*)key
{
	NSDictionary* predefined = [NSDictionary rtr_stringToExportType];
	NSString* value = [self valueForKey:key];
	if(![value isKindOfClass:[NSString class]]) {
		return RTRImageCaptureEncodingTypeJpg;
	}
	if([predefined valueForKey:value] != nil) {
		return (RTRImageCaptureEncodingType)[predefined[value] integerValue];
	}
	return RTRImageCaptureEncodingTypeJpg;
}

@end
