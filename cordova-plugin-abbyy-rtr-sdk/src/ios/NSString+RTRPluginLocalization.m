// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "NSString+RTRPluginLocalization.h"

@implementation NSString (RTRPluginLocalization)

- (NSString*)rtr_localized
{
	NSString* preferredMappingPath = [NSBundle.mainBundle pathForResource:@"rtr_localizable_en" ofType:@"json"];
	for(NSString* locale in NSLocale.preferredLanguages) {
		NSString* currentMappingPath = [NSBundle.mainBundle
			pathForResource:[NSString 
				stringWithFormat:@"rtr_localizable_%@", locale] 
			ofType:@"json"];
		if([NSFileManager.defaultManager fileExistsAtPath:currentMappingPath]) {
			preferredMappingPath = currentMappingPath;
			break;
		}
	}
	NSData* rawContent = [[NSString
		stringWithContentsOfFile:preferredMappingPath
		encoding:NSUTF8StringEncoding error:nil]
			dataUsingEncoding:NSUTF8StringEncoding];
	NSDictionary* mapping = [NSJSONSerialization JSONObjectWithData:rawContent options:0 error:nil];
	return [mapping objectForKey:self] ?: self;
}

@end
