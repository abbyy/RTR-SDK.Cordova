/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRManager.h"
#import "RTRDataCaptureScenario.h"

static NSString* const RTRAssetsPath = @"www/rtr_assets";

@interface RTRManager ()

@property (nonatomic) RTREngine* engine;

@end

@implementation RTRManager

+ (instancetype)managerWithLicense:(NSString*)licenseName error:(NSError**)error
{
	NSString* licensePath = [NSBundle.mainBundle pathForResource:licenseName.stringByDeletingPathExtension
		ofType:licenseName.pathExtension inDirectory:RTRAssetsPath];
	if(licensePath.length == 0) {
		licensePath = [[NSBundle.mainBundle.bundlePath stringByAppendingPathComponent:RTRAssetsPath]
			stringByAppendingPathComponent:licenseName];
	}

	NSData* licenseData = [NSData dataWithContentsOfFile:licensePath options:0 error:error];

	if(licenseData == nil) {
		return nil;
	}

	return [[RTRManager alloc] initWithLicense:licenseData];
}

- (instancetype)initWithLicense:(NSData*)licenseData
{
	self = [super init];
	if(self != nil) {
		self.engine = [RTREngine sharedEngineWithLicenseData:licenseData];
		NSString* assetsPath = [NSBundle.mainBundle.bundlePath stringByAppendingPathComponent:RTRAssetsPath];
		self.engine.extendedSettings.externalAssetsPath = assetsPath;
		
		if(self.engine == nil) {
			return nil;
		}
	}
	return self;
}

- (id<RTRTextCaptureService>)textCaptureServiceWithLanguages:(NSSet*)languages
	delegate:(id<RTRTextCaptureServiceDelegate>)delegate
{
	id<RTRTextCaptureService> service = [self.engine createTextCaptureServiceWithDelegate:delegate];
	[service setRecognitionLanguages:languages];
	
	return service;
}

- (id<RTRDataCaptureService>)dataCaptureServiceWithProfile:(NSString*)profile
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate
{
	return [self.engine createDataCaptureServiceWithDelegate:delegate profile:profile];
}

- (id<RTRDataCaptureService>)customDataCaptureServiceWithScenario:(RTRDataCaptureScenario*)scenario
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate
{
	return [self buildServiceUsingScenario:scenario delegate:delegate error:nil];
}

- (id<RTRDataCaptureService>)buildServiceUsingScenario:(RTRDataCaptureScenario*)scenario
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate error:(NSError**)error
{
	id<RTRDataCaptureService> service = [self.engine createDataCaptureServiceWithDelegate:delegate profile:nil];
	id<RTRDataCaptureProfileBuilder> builder = [service configureDataCaptureProfile];
	[builder setRecognitionLanguages:scenario.languages];
	
	id<RTRDataSchemeBuilder> scheme = [builder addScheme:scenario.name];
	[scheme setName:scenario.name];
	
	id<RTRDataFieldBuilder> field = [scheme addField:scenario.name];
	[field setName:scenario.name];
	[field setRegEx:scenario.regEx];

	@try {
		[builder checkAndApply];
	} @catch(NSException* exception) {
		if(error != nil) {
			*error = [NSError errorWithDomain:@"com.abbyy.rtr-cordova-plugin" code:3 userInfo:exception.userInfo];
		}
		return nil;
	}

	return service;
}

@end
