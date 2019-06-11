/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "NSDictionary+RTRSettings.h"

@implementation NSDictionary (RTRSettings)

- (UIInterfaceOrientationMask)rtr_orientationMaskForKey:(NSString*)key;
{
	NSString* value = [self valueForKey:key];
	if(![value isKindOfClass:[NSString class]]) {
		return UIInterfaceOrientationMaskAll;
	}
	if([value isEqualToString:@"portrait"]) {
		return UIInterfaceOrientationMaskPortrait;
	}
	if([value isEqualToString:@"landscape"]) {
		return UIInterfaceOrientationMaskLandscape;
	}
	return UIInterfaceOrientationMaskAll;
}

- (RTRCoreAPIExportCompressionLevel)rtr_exportCompressionLevelForKey:(NSString *)key
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"Low": @(RTRCoreAPIExportCompressionLowLevel),
			@"Normal": @(RTRCoreAPIExportCompressionNormalLevel),
			@"High": @(RTRCoreAPIExportCompressionHighLevel),
			@"ExtraHigh": @(RTRCoreAPIExportCompressionExtraHighLevel)
		};
	}
	NSString* value = [self valueForKey:key];
	if(![value isKindOfClass:[NSString class]]) {
		return RTRCoreAPIExportCompressionNormalLevel;
	}
	if([predefined valueForKey:value] != nil) {
		return (RTRCoreAPIExportCompressionLevel)[predefined[value] integerValue];
	}
	return RTRCoreAPIExportCompressionNormalLevel;
}

- (RTRCoreAPIPdfExportCompressionType)rtr_exportCompressionTypeForKey:(NSString*)key
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"jpg": @(RTRCoreAPIPdfExportJpgCompression),
			@"jpeg2000": @(RTRCoreAPIPdfExportJpeg2000Compression)
		};
	}
	NSString* value = [self valueForKey:key];
	if(![value isKindOfClass:[NSString class]]) {
		return RTRCoreAPIPdfExportJpgCompression;
	}
	if([predefined valueForKey:value] != nil) {
		return (RTRCoreAPIPdfExportCompressionType)[predefined[value] integerValue];
	}
	return RTRCoreAPIPdfExportJpgCompression;
}

- (RTRImageCaptureDestintationType)rtr_destinationTypeForKey:(NSString*)key
{
	return RTRImageCaptureDestintationUri;
}

- (RTRImageCaptureEncodingType)rtr_exportTypeForKey:(NSString*)key
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"jpg": @(RTRImageCaptureEncodingTypeJpg),
			@"png": @(RTRImageCaptureEncodingTypePng),
			@"pdf": @(RTRImageCaptureEncodingTypePdf)
		};
	}
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
