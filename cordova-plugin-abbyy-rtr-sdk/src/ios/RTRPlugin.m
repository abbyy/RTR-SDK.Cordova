/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRPlugin.h"
#import "CDVPluginResult+RTRPluginResult.h"
#import "RTRTextCaptureViewController.h"
#import "RTRDataCaptureViewController.h"
#import "RTRDataCaptureScenario.h"
#import "NSDictionary+RTRSettings.h"

#import <AbbyyRtrSDK/AbbyyRtrSDK.h>
#import <AbbyyUI/AbbyyUI.h>

#import "RTRImageCaptureViewController.h"
#import "RTRPluginConstants.h"

@interface RTRImageCaptureOptions : NSObject

@property (nonatomic, assign) CGSize documentSize;
@property (nonatomic, assign) BOOL cropEnabled;
@property (nonatomic, assign) CGFloat minimumDocumentToViewRatio;

@property (nonatomic, assign) RTRImageCaptureDestintationType destination;
@property (nonatomic, assign) RTRImageCaptureEncodingType encodingType;
@property (nonatomic, assign) RTRCoreAPIExportCompressionLevel compression;

@end

@implementation RTRImageCaptureOptions
@end

@interface RTRPlugin () <AUIImageCaptureScenarioDelegate>

@property (nonatomic) RTRViewController* rtrViewController;
@property (nonatomic) RTRManager* rtrManager;

@property (nonatomic) CDVInvokedUrlCommand* currentCommand;

@property (nonatomic, strong) RTRImageCaptureViewController* imageCaptureHolder;

@end

@implementation RTRPlugin

#pragma mark - Public

- (void)startTextCapture:(CDVInvokedUrlCommand*)command
{
	[self.commandDelegate runInBackground:^{
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
		rtrViewController.supportedInterfaceOrientations = [params rtr_orientationMaskForKey:RTROrientationPolicy];
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
	}];
}

- (void)startDataCapture:(CDVInvokedUrlCommand*)command
{
	[self.commandDelegate runInBackground:^{
		if(![self initializeRtrManager:command]) {
			return;
		}

		NSDictionary* params = command.arguments.firstObject;
		NSDictionary* scenarioParams = params[RTRCustomDataCaptureScenarioKey];

		RTRDataCaptureViewController* rtrViewController = [RTRDataCaptureViewController new];
		rtrViewController.supportedInterfaceOrientations = [params rtr_orientationMaskForKey:RTROrientationPolicy];

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
		} else {
			errorDescription = @"Invalid Data Capture scenario settings. Specify Data Capture profile or params for Custom Data Capture Scenario.";
		}

		if(errorDescription.length != 0) {
			NSError* error = [NSError errorWithDomain:RTRCordovaPluginErrorDomain code:3 userInfo:@{
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
	}];
}

- (void)startImageCapture:(CDVInvokedUrlCommand*)command
{
	[self.commandDelegate runInBackground:^{
		self.currentCommand = command;

		if(![self initializeRtrManager:command]) {
			return;
		}

		dispatch_async(dispatch_get_main_queue(), ^{
			self.imageCaptureHolder = [RTRImageCaptureViewController new];

			__weak RTRPlugin* weakSelf = self;

			self.imageCaptureHolder.config = [[RTRMultipageScenarioConfiguration alloc] initWithManager:self.rtrManager args:command.arguments.firstObject];
			self.imageCaptureHolder.onSuccess = ^(BOOL manuallyStopped, NSDictionary* response) {
				NSMutableDictionary* mutableResponce = response.mutableCopy;
				if(manuallyStopped) {
					mutableResponce[RTRCallbackResultInfoKey] =
					@{
					  RTRCallbackUserActionKey : @"Manually Stopped"
					};
				}

				CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:mutableResponce];
				[weakSelf.viewController dismissViewControllerAnimated:YES completion:^{
					[weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:weakSelf.currentCommand.callbackId];
				}];
			};
			self.imageCaptureHolder.onCancel = ^{
				NSMutableDictionary* result = [@{
					RTRCallbackResultInfoKey : @{
						RTRCallbackUserActionKey : @"Canceled"
					}
				} mutableCopy];

				CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
				[weakSelf.viewController dismissViewControllerAnimated:YES completion:^{
					[weakSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
				}];
			};
			self.imageCaptureHolder.onError = ^(NSError* error) {
				CDVPluginResult* result = [CDVPluginResult rtrResultWithError:error];
				[weakSelf.viewController dismissViewControllerAnimated:YES completion:^{
					[weakSelf.commandDelegate sendPluginResult:result callbackId:command.callbackId];
				}];
			};
			[self.viewController presentViewController:self.imageCaptureHolder animated:YES completion:nil];
		});

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
	NSString* licenseName = command.arguments.firstObject[RTRLicenseFileNameKey] ?: @"AbbyyRtrSdk.license";
	NSError* error = nil;
	self.rtrManager = [RTRManager managerWithLicense:licenseName error:&error];

	if(self.rtrManager == nil) {
		if(error == nil) {
			error = [NSError errorWithDomain:RTRCordovaPluginErrorDomain code:2 userInfo:@{
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
