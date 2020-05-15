/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRExportUtilities.h"
#import "RTRPluginConstants.h"
#import <MobileCoreServices/MobileCoreServices.h>

@implementation RTRExportUtilities

+ (NSString*)exportDirectory
{
	return NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES).firstObject;
}

+ (NSString*)mimeForFileExtension:(NSString*)extension
{
	NSString* UTI = (__bridge_transfer NSString*)UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension,
		(__bridge CFStringRef)extension, 0);
	NSString* mimeType = (__bridge_transfer NSString*)UTTypeCopyPreferredTagWithClass((__bridge CFStringRef)UTI,
		kUTTagClassMIMEType);
	return mimeType;
}

+ (NSString*)uriPrefixForDestination:(NSString*)destination extension:(NSString*)extension
{
	if([destination.lowercaseString isEqualToString:@"base64"]) {
		return [NSString stringWithFormat:@"data:%@;base64,", [self mimeForFileExtension:extension]];
	} else if([destination.lowercaseString isEqualToString:@"file"]){
		return @"file://";
	} else {
		NSParameterAssert([destination isEqualToString:@"base64"] || [destination isEqualToString:@"file"]);
		return @"";
	}
}

+ (NSString*)generatePathWithExtension:(NSString*)extension
{
	NSString* filename = [NSString stringWithFormat:@"%@.%@", NSUUID.UUID.UUIDString, extension];
	return [[self exportDirectory] stringByAppendingPathComponent:filename];
}

@end
