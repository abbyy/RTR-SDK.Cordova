/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRManager.h"
#import "RTRDataCaptureScenario.h"
#import "RTRPluginConstants.h"

@implementation RTRManager

+ (instancetype)managerWithLicense:(NSString*)licenseName error:(NSError**)error
{
	NSString* licensePath = [NSBundle.mainBundle pathForResource:licenseName.stringByDeletingPathExtension ofType:licenseName.pathExtension];

	if(licensePath == nil) {
		if(error != nil) {
			NSDictionary* userInfo;

			NSString* resourcesPath = NSBundle.mainBundle.resourcePath;
			NSArray* bundleContent = [NSFileManager.defaultManager contentsOfDirectoryAtPath:resourcesPath error:error];
			if(bundleContent == nil) {
				userInfo = @{
					NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Resource path %@ not found.", resourcesPath]
				};
			} else {
				NSPredicate* filterPredicate = [NSPredicate
					predicateWithBlock:^BOOL(NSString* evaluatedObject, NSDictionary* bindings) {

					return [evaluatedObject containsString:@"license"] || [evaluatedObject containsString:@"License"];
				}];
				NSArray* avaliableLicenses = [bundleContent filteredArrayUsingPredicate:filterPredicate];
				if(avaliableLicenses.count == 0) {
					userInfo = @{
						NSLocalizedDescriptionKey: [NSString stringWithFormat:@"License %@ not found.", licenseName]
					};
				} else {
					userInfo = @{
						NSLocalizedDescriptionKey: [NSString
							stringWithFormat:@"License %@ not found. Existing licenses: %@",
								licenseName,
								avaliableLicenses]
					};
				}

			}
			*error = [NSError
					  errorWithDomain:RTRCordovaPluginErrorDomain
					  code:-1
					  userInfo:userInfo];
			return nil;
		}
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
		
		if(self.engine == nil) {
			return nil;
		}
	}
	return self;
}

- (id<RTRTextCaptureService>)textCaptureServiceWithLanguages:(NSSet*)languages
	delegate:(id<RTRTextCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings
{
	id<RTRTextCaptureService> service = [self.engine createTextCaptureServiceWithDelegate:delegate settings:extendedSettings];
	if(languages.count != 0) {
		[service setRecognitionLanguages:languages];
	}
	
	return service;
}

- (id<RTRDataCaptureService>)dataCaptureServiceWithProfile:(NSString*)profile
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings
{
	return [self.engine createDataCaptureServiceWithDelegate:delegate profile:profile settings:extendedSettings];
}

- (id<RTRDataCaptureService>)customDataCaptureServiceWithScenario:(RTRDataCaptureScenario*)scenario
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings
{
	return [self buildServiceUsingScenario:scenario delegate:delegate extendedSettings:extendedSettings error:nil];
}

- (id<RTRDataCaptureService>)buildServiceUsingScenario:(RTRDataCaptureScenario*)scenario
	delegate:(id<RTRDataCaptureServiceDelegate>)delegate extendedSettings:(RTRExtendedSettings*)extendedSettings error:(NSError**)error
{
	id<RTRDataCaptureService> service = [self.engine createDataCaptureServiceWithDelegate:delegate profile:nil settings:extendedSettings];
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
