/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRDataCaptureScenario.h"

@interface RTRDataCaptureScenario ()

@property (nonatomic) NSString* specification;

@end

@implementation RTRDataCaptureScenario

+ (instancetype)dataCaptureScenarioWithName:(NSString*)name regEx:(NSString*)regEx
	languages:(NSSet<NSString*>*)languages description:(NSString*)description
{
	return [[RTRDataCaptureScenario alloc] initWithName:name regEx:regEx languages:languages description:description];
}

- (instancetype)initWithName:(NSString*)name regEx:(NSString*)regEx
	languages:(NSSet<NSString*>*)languages description:(NSString*)description
{
	self = [super init];
	if(self) {
		self.name = name;
		self.regEx = regEx;
		self.languages = languages;
		self.specification = description;
	}
	return self;
}

- (BOOL)isEqualToDataCaptureScenario:(RTRDataCaptureScenario*)dataCaptureScenario
{
	if(!dataCaptureScenario) {
		return NO;
	}
	
	return [self.name isEqualToString:dataCaptureScenario.name];
}

#pragma mark - NSObject

- (BOOL)isEqual:(id)object
{
	if(self == object) {
		return YES;
	}

	if(![object isKindOfClass:[RTRDataCaptureScenario class]]) {
		return NO;
	}

	return [self isEqualToDataCaptureScenario:object];
}

- (NSUInteger)hash
{
	return self.name.hash;
}

- (NSString*)description
{
	return self.specification;
}

@end
