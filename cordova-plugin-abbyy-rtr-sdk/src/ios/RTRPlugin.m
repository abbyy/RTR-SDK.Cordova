/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRPlugin.h"
#import "CDVPluginResult+RTRPluginResult.h"
#import "RTRTextCaptureViewController.h"
#import "RTRDataCaptureViewController.h"
#import "RTRDataCaptureScenario.h"

static NSString* const RTRCordovaPluginErrorDomain = @"com.abbyy.rtr-cordova-plugin";

static NSString* const RTRRecognitionLanguagesKey = @"recognitionLanguages";
static NSString* const RTRSelectableRecognitionLanguagesKey = @"selectableRecognitionLanguages";

static NSString* const RTRLicenseFileNameKey = @"licenseFileName";
static NSString* const RTRStopWhenStableKey = @"stopWhenStable";
static NSString* const RTRIsStopButtonVisibleKey = @"isStopButtonVisible";
static NSString* const RTRAreaOfInterestKey = @"areaOfInterest";
static NSString* const RTRIsFlashlightVisibleKey = @"isFlashlightVisible";

static NSString* const RTRCustomDataCaptureScenarioKey = @"customDataCaptureScenario";
static NSString* const RTRCustomDataCaptureScenarioNameKey = @"name";
static NSString* const RTRCustomDataCaptureFieldsKey = @"fields";
static NSString* const RTRCustomDataCaptureRegExKey = @"regEx";
static NSString* const RTRScenarioDescriptionKey = @"description";

static NSString* const RTRDataCaptureProfileKey = @"profile";

static NSString* const RTRExtendedSettingsKey = @"extendedSettings";

static NSString* const RTRDefaultRecognitionLanguage = @"English";

NSString* const RTRCallbackErrorKey = @"error";
NSString* const RTRCallbackErrorDescriptionKey = @"description";
NSString* const RTRCallbackResultInfoKey = @"resultInfo";
NSString* const RTRCallbackUserActionKey = @"userAction";

@interface RTRPlugin ()

@property (nonatomic) RTRViewController* rtrViewController;
@property (nonatomic) RTRManager* rtrManager;

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

		RTRDataCaptureViewController* rtrViewController = [RTRDataCaptureViewController new];

		NSDictionary* params = command.arguments.firstObject;
		NSDictionary* scenarioParams = params[RTRCustomDataCaptureScenarioKey];

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

@end
