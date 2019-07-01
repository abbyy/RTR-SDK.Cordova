/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "NSDictionary+RTRSettings.h"
#import <AbbyyUI/AbbyyUI.h>

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

+ (NSDictionary*)rtr_stringToExportCompressionLevel
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
	return predefined;
}

+ (NSDictionary*)rtr_exportCompressionLevelToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(RTRCoreAPIExportCompressionLowLevel) : @"Low",
			@(RTRCoreAPIExportCompressionNormalLevel) : @"Normal",
			@(RTRCoreAPIExportCompressionHighLevel) : @"High",
			@(RTRCoreAPIExportCompressionExtraHighLevel) : @"ExtraHigh"
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_stringToexportCompressionType
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"jpg": @(RTRCoreAPIPdfExportJpgCompression),
			@"jpeg2000": @(RTRCoreAPIPdfExportJpeg2000Compression)
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_exportCompressionTypeToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(RTRCoreAPIPdfExportJpgCompression): @"jpg",
			@(RTRCoreAPIPdfExportJpeg2000Compression): @"jpeg2000"
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_stringToauiCameraResolution
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"HD": @(AUICameraResolutionHD),
			@"FullHD": @(AUICameraResolutionFullHD),
			@"4K": @(AUICameraResolution4K),
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_auiCameraResolutionToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(AUICameraResolutionHD) : @"HD",
			@(AUICameraResolutionFullHD) : @"FullHD",
			@(AUICameraResolution4K) : @"4K",
		};
	}
	return predefined;
}

- (RTRCoreAPIExportCompressionLevel)rtr_exportCompressionLevelForKey:(NSString*)key
{
	NSDictionary* predefined = [NSDictionary rtr_stringToExportCompressionLevel];
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
	NSDictionary* predefined = [NSDictionary rtr_stringToexportCompressionType];
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
			@"jpeg2000": @(RTRImageCaptureEncodingTypeJpeg2000),
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
