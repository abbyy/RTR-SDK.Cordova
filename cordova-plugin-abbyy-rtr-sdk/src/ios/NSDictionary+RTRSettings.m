/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "NSDictionary+RTRSettings.h"
#import "RTRPluginConstants.h"
#import <AbbyyUI/AbbyyUI.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

@implementation RTRTextBlock (rtr_Text)

- (NSString*)rtr_text
{
	if(self.textLines.count == 0) {
		return @"";
	}
	return [[self.textLines rtr_map:^id(RTRTextLine* line) {
		return line.text;
	}] componentsJoinedByString:@"\n"];
}

@end

@implementation NSDictionary (rtr_Mapping)

#pragma mark - enum mappings

+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToCameraResolution
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"hd": @(AUICameraResolutionHD),
			@"fullhd": @(AUICameraResolutionFullHD),
			@"4k": @(AUICameraResolution4K),
		};
	}
	return predefined;
}

+ (NSDictionary<NSString*, NSNumber*>*)rtr_cameraResolutionToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(AUICameraResolutionHD): @"hd",
			@(AUICameraResolutionFullHD): @"fullhd",
			@(AUICameraResolution4K): @"4k",
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_stringToOrientationMask
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"portrait": @(UIInterfaceOrientationMaskPortrait),
			@"landscape": @(UIInterfaceOrientationMaskLandscape),
			@"default": @(UIInterfaceOrientationMaskAll)
		};
	}
	return predefined;
}

+(NSDictionary<NSNumber*,NSString*>*)rtr_orientationMaskToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(UIInterfaceOrientationMaskPortrait): @"Portrait",
			@(UIInterfaceOrientationMaskLandscape): @"Landscape",
			@(UIInterfaceOrientationMaskAll): @"Default"
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_stringToExportCompressionLevel
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"low": @(RTRCoreAPIExportCompressionLowLevel),
			@"normal": @(RTRCoreAPIExportCompressionNormalLevel),
			@"high": @(RTRCoreAPIExportCompressionHighLevel),
			@"extrahigh": @(RTRCoreAPIExportCompressionExtraHighLevel)
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

+ (NSDictionary*)rtr_stringToExportCompressionType
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

+ (NSDictionary*)rtr_stringToDetectionMode
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"Fast": @(RTRDetectDocumentBoundaryModeFast),
			@"Default": @(RTRDetectDocumentBoundaryModeDefault)
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_detectionModeToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(RTRDetectDocumentBoundaryModeFast): @"Fast",
			@(RTRDetectDocumentBoundaryModeDefault): @"Default"
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_destinationTypeToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(RTRImageDestinationTypeBase64): @"base64",
			@(RTRImageDestinationTypeFile): @"file"
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_stringToDestinationType
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"base64": @(RTRImageDestinationTypeBase64),
			@"file": @(RTRImageDestinationTypeFile)
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_exportTypeToString
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@(RTRImageCaptureEncodingTypeJpeg2000): @"jpeg2000",
			@(RTRImageCaptureEncodingTypeJpg): @"jpg",
			@(RTRImageCaptureEncodingTypePng): @"png",
			@(RTRImageCaptureEncodingTypePdf): @"pdf"
		};
	}
	return predefined;
}

+ (NSDictionary*)rtr_stringToExportType
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
	return predefined;
}

#pragma mark - ctors

+ (instancetype)rtr_dictionaryFromDataField:(RTRDataField*)dataField
{
	return @{
		@"id" : dataField.id ?: @"",
		@"name" : dataField.name ?: @"",
		@"text" : dataField.text ?: @"",
		@"quadrangle" : [dataField.quadrangle rtr_transformToDictionaryQuadrangle:nil] ?: @[],
		@"rect": [NSDictionary rtr_dictionaryFromRect:dataField.rect],
		@"charInfo" : [dataField.charsInfo rtr_map:^id(RTRCharInfo* info) {
			return [NSDictionary rtr_dictionaryWithCharInfo:info];
		}] ?: @[],
		@"components" : [dataField.components rtr_map:^id(RTRDataField* field) {
			return [NSDictionary rtr_dictionaryFromDataField:field];
		}] ?: @[]
	};
}

+ (instancetype)rtr_dictionaryFromTextBlock:(RTRTextBlock*)textBlock
{
	return @{
//		@"text": textBlock.rtr_text ?: @"",
		@"textLines": [textBlock.textLines rtr_map:^id(RTRTextLine* line) {
			return [NSDictionary rtr_dictionaryFromTextLine:line];
		}] ?: @[]
	};
}

+ (instancetype)rtr_dictionaryFromTextLine:(RTRTextLine*)textLine
{
	return @{
		@"text" : textLine.text ?: @"",
		@"rect" : [NSDictionary rtr_dictionaryFromRect:textLine.rect],
		@"quadrangle" : [textLine.quadrangle rtr_transformToDictionaryQuadrangle:nil] ?: @[],
		@"charInfo" : [textLine.charsInfo rtr_map:^id(RTRCharInfo* info) {
			return [NSDictionary rtr_dictionaryWithCharInfo:info];
		}] ?: @[]
	};
}

+ (instancetype)rtr_dictionaryWithCharInfo:(RTRCharInfo*)charInfo
{
	NSMutableDictionary* dict = @{
		@"quadrangle": [charInfo.quadrangle rtr_transformToDictionaryQuadrangle:nil] ?: @[],
		@"rect": [NSDictionary rtr_dictionaryFromRect:charInfo.rect]
	}.mutableCopy;
	if(charInfo.isItalic) {
		dict[@"isItalic"] = @YES;
	}
	if(charInfo.isBold) {
		dict[@"isBold"] = @YES;
	}
	if(charInfo.isSmallcaps) {
		dict[@"isSmallcaps"] = @YES;
	}
	if(charInfo.isUncertain) {
		dict[@"isUncertain"] = @YES;
	}
	if(charInfo.isUnderlined) {
		dict[@"isUnderlined"] = @YES;
	}
	if(charInfo.isSuperscript) {
		dict[@"isSuperscript"] = @YES;
	}
	if(charInfo.isStrikethrough) {
		dict[@"isStrikethrough"] = @YES;
	}
	return dict;
}

+ (instancetype)rtr_dictionaryFromRect:(CGRect)areaOfInterest
{
	areaOfInterest = CGRectIntegral(areaOfInterest);
	NSInteger left = CGRectGetMinX(areaOfInterest);
	NSInteger top = CGRectGetMinY(areaOfInterest);
	NSInteger right = CGRectGetMaxX(areaOfInterest);
	NSInteger bottom = CGRectGetMaxY(areaOfInterest);

	return @{
		@"top": @(top),
		@"bottom": @(bottom),
		@"left": @(left),
		@"right": @(right)
	};
}

+ (instancetype)rtr_dictionaryFromPoint:(CGPoint)point
{
	return @{
		@"x": @((NSInteger)point.x),
		@"y": @((NSInteger)point.y)
	};
}

+ (instancetype)rtr_imageSizeDictionaryFromSize:(CGSize)size
{
	return [self rtr_dictionaryFromSize:CGSizeMake(
		(NSInteger)size.width, (NSInteger)size.height)];
}

+ (instancetype)rtr_resolutionDictionaryFromSize:(CGSize)size
{
	return @{
		@"x": @((NSInteger)size.width),
		@"y": @((NSInteger)size.height),
	};
}

+ (instancetype)rtr_dictionaryFromSize:(CGSize)size
{
	return @{
		@"width": @(size.width),
		@"height": @(size.height),
	};
}

- (BOOL)rtr_asSize:(CGSize*)size error:(NSError**)error
{
	NSArray* requiredKeys = @[@"width", @"height"];
	for(NSString* key in requiredKeys) {
		if(self[key] == nil) {
			if(error != nil) {
				NSString* message = [NSString stringWithFormat:@"Missing key %@. Required keys: %@", key, requiredKeys];
				NSDictionary* userInfo = @{
					NSLocalizedDescriptionKey: message
				};
				*error = [NSError errorWithDomain:RTRPluginErrorDomain code:-1 userInfo:userInfo];
				return NO;
			}
		}
	}
	*size = CGSizeMake([self[@"width"] floatValue], [self[@"height"] floatValue]);
	return YES;
}

- (BOOL)rtr_asRect:(CGRect*)outRect error:(NSError**)error
{
	NSArray* requiredKeys = @[@"top", @"bottom", @"left", @"right"];
	for(NSString* key in requiredKeys) {
		if(self[key] == nil) {
			if(error != nil) {
				NSString* message = [NSString stringWithFormat:@"Missing key %@. Required keys: %@", key, requiredKeys];
				NSDictionary* userInfo = @{
					NSLocalizedDescriptionKey: message
				};
				*error = [NSError errorWithDomain:RTRPluginErrorDomain code:-1 userInfo:userInfo];
			}
			return NO;
		}
		if(![self checkValueForKey:key isKindOfClass:NSNumber.class error:error]) {
			return NO;
		}
	}
	CGFloat left = [self[@"left"] floatValue];
	CGFloat right = [self[@"right"] floatValue];
	CGFloat top = [self[@"top"] floatValue];
	CGFloat bottom = [self[@"bottom"] floatValue];
	*outRect = CGRectMake(left, top, right - left, bottom - top);
	return YES;
}

#pragma mark - key parsing & typechecking

- (BOOL)checkValueForKey:(NSString*)key isKindOfClass:(Class)class error:(NSError**)error
{
	NSObject* value = self[key];
	if([value isKindOfClass:class]) {
		return YES;
	}
	if(error != nil) {
		NSString* description = [NSString stringWithFormat:@"Value '%@' for key '%@' is not of class '%@'", value, key, NSStringFromClass(class)];
		NSDictionary* userInfo = @{NSLocalizedDescriptionKey: description};
		*error = [NSError errorWithDomain:RTRPluginErrorDomain code:-1 userInfo:userInfo];
	}
	return NO;
}

- (BOOL)rtr_parseBool:(NSString*)key defaultValue:(BOOL)defaultValue outValue:(BOOL*)outValue error:(NSError**)error
{
	if(self[key] == nil) {
		*outValue = defaultValue;
		return YES;
	}
	if(![self checkValueForKey:key isKindOfClass:NSNumber.class error:error]) {
		return NO;
	}
	*outValue = [self[key] boolValue];
	return YES;
}

- (BOOL)rtr_parseFloat:(NSString*)key defaultValue:(CGFloat)defaultValue outValue:(CGFloat*)outValue error:(NSError**)error
{
	if(self[key] == nil) {
		*outValue = defaultValue;
		return YES;
	}
	if(![self checkValueForKey:key isKindOfClass:NSNumber.class error:error]) {
		return NO;
	}
	*outValue = [self[key] floatValue];
	return YES;
}

- (BOOL)rtr_parseInteger:(NSString*)key defaultValue:(NSInteger)defaultValue outValue:(NSInteger*)outValue error:(NSError**)error
{
	if(self[key] == nil) {
		*outValue = defaultValue;
		return YES;
	}
	if(![self checkValueForKey:key isKindOfClass:NSNumber.class error:error]) {
		return NO;
	}
	*outValue = [self[key] integerValue];
	return YES;
}

- (BOOL)rtr_parseEnum:(NSString*)key defaultValue:(NSInteger)defaultValue variants:(NSDictionary<NSString*,NSNumber*>*)variants outValue:(NSInteger*)outValue error:(NSError**)error
{
	if(self[key] == nil) {
		*outValue = defaultValue;
		return YES;
	}
	if(![self checkValueForKey:key isKindOfClass:NSString.class error:error]) {
		return NO;
	}
	NSString* string = [self[key] lowercaseString];
	NSNumber* number = variants[string];
	if(number == nil) {
		if(error != nil) {
			NSString* description = [NSString stringWithFormat:@"Value %@ is invalid. %@ is avaliable.", string, variants.allKeys];
			*error = [NSError
				errorWithDomain:RTRPluginErrorDomain
				code:-1
				userInfo:@{
					NSLocalizedDescriptionKey: description
				}];
		}
		return NO;
	}
	*outValue = number.integerValue;
	return YES;
}

- (BOOL)rtr_parseDocumentSize:(NSString*)key defaultValue:(AUIDocumentSize)defaultValue outValue:(AUIDocumentSize*)outValue error:(NSError**)error
{
	if(self[key] == nil) {
		*outValue = defaultValue;
		return YES;
	}
	if([self[key] isKindOfClass:NSString.class]) {
		NSString* string = [self[key] lowercaseString];
		NSDictionary<NSString*, NSValue*>* variants = @{
			@"a4": @(AUIDocumentSizeA4),
			@"businesscard": @(AUIDocumentSizeBusinessCard),
			@"letter": @(AUIDocumentSizeLetter),
			@"any": @(AUIDocumentSizeAny)
		};
		if([variants valueForKey:string] != nil) {
			*outValue = [variants[string] CGSizeValue];
			return YES;
		} else {
			NSCharacterSet* separators = [NSCharacterSet characterSetWithCharactersInString:@"Xx/ "];
			NSArray<NSString*>* parts = [string componentsSeparatedByCharactersInSet:separators];
			float width = 0;
			float height = 0;
			BOOL isValueValid = NO;
			if(parts.count == 2) {
				isValueValid = ![[NSScanner scannerWithString:parts.firstObject] scanFloat:&width];
				if(!isValueValid) {
					isValueValid &= ![[NSScanner scannerWithString:parts.lastObject] scanFloat:&height];
				}
			} else {
				isValueValid = YES;
			}
			if(!isValueValid) {
				*outValue = CGSizeMake(width, height);
				return YES;
			} else {
				NSString* description = @"Failed to parse document size.";
				if(error != nil) {
					NSDictionary* userInfo = @{NSLocalizedDescriptionKey: description};
					*error = [NSError errorWithDomain:RTRPluginErrorDomain code:-1 userInfo:userInfo];
				}
				return NO;
			}
		}
	} else if([self[key] isKindOfClass:NSDictionary.class]) {
		return [self[key] rtr_asSize:outValue error:error];
	} else {
		if(error != nil) {
			NSString* description = [NSString stringWithFormat:@"Value on %@ key should be %@ or %@", key, NSString.class, NSDictionary.class];
			*error = [NSError
				errorWithDomain:RTRPluginErrorDomain
				code:-1
				userInfo:@{
					NSLocalizedDescriptionKey: description
				}];
		}
		return NO;
	}
}

- (NSArray*)rtr_parseArray:(NSString*)key defaultValue:(NSArray*)defaultValue error:(NSError**)error
{
    if(self[key] == nil) {
        return defaultValue;
    }
    if(![self checkValueForKey:key isKindOfClass:NSArray.class error:error]) {
        return nil;
    }
    return self[key];
}

@end

@implementation NSString (rtr_Mapping)

+ (instancetype)rtr_stringFromStabilityStatus:(RTRResultStabilityStatus)status
{
	switch(status) {
		case RTRResultStabilityNotReady:
			return @"NotReady";
			break;
		case RTRResultStabilityTentative:
			return @"Tentative";
			break;
		case RTRResultStabilityVerified:
			return @"Verified";
			break;
		case RTRResultStabilityAvailable:
			return @"Available";
			break;
		case RTRResultStabilityTentativelyStable:
			return @"TentativelyStable";
			break;
		case RTRResultStabilityStable:
			return @"Stable";
			break;
	}
}

+ (instancetype)rtr_stringFromWarningCode:(RTRCallbackWarningCode)warningCode
{
	switch(warningCode) {
		case RTRCallbackWarningTextTooSmall:
			return @"TextTooSmall";
		case RTRCallbackWarningWrongLanguage:
			return @"WrongLanguage";
		case RTRCallbackWarningRecognitionIsSlow:
			return @"RecognitionIsSlow";
		case RTRCallbackWarningProbablyWrongLanguage:
			return @"ProbablyWrongLanguage";
		case RTRCallbackWarningProbablyLowQualityImage:
			return @"ProbablyLowQualityImage";
		case RTRCallbackWarningNoWarning:
			return @"NoWarning";
	}
}

+ (instancetype)rtr_stringFromOcrQualityBlockType:(RTRQualityAssessmentForOCRBlockType)blockType
{
	switch(blockType) {
		case RTRQualityAssessmentForOCRTextBlock:
			return @"Text";
		case RTRQualityAssessmentForOCRUnknownBlock:
			return @"Unknown";
	}
}

@end

@implementation NSArray (rtr_Mapping)

- (nullable NSArray*)rtr_tryMap:(nullable id(^)(id, NSError**))transform error:(NSError**)error
{
	NSMutableArray* result = [NSMutableArray arrayWithCapacity:self.count];
	__block NSError* internalError = nil;
	[self enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL* stop) {
		id transformed = transform(obj, &internalError);
		if(transformed != nil) {
			[result addObject:transformed];
		} else {
			*stop = YES;
		}
	}];
	if(result.count != self.count) {
		if(error != nil) {
			*error = internalError;
		}
		return nil;
	}
	return result;
}

- (NSArray*)rtr_map:(id(^)(id))transform
{
	NSMutableArray* result = [NSMutableArray arrayWithCapacity:self.count];
	[self enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL* stop) {
		[result addObject:transform(obj)];
	}];
	return result;
}

- (NSArray*)rtr_transformToNSValuesQuadrangle:(NSError**)error
{
	if(self.count != 4) {
		if(error != nil) {
			*error = [NSError
				errorWithDomain:RTRPluginErrorDomain
				code:-1
				userInfo:@{
					NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Quadrangle must have 4 points. Found %@", self]
				}];
		}
		return nil;
	}
	return [self rtr_tryMap:^id(NSDictionary* pointDict, NSError** error) {
        if(![pointDict isKindOfClass:NSDictionary.class]) {
            if(error != nil) {
				*error = [NSError
					errorWithDomain:RTRPluginErrorDomain
					code:-1
					userInfo:@{
						NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Point dictionary must be NSDictionary. Found %@ instead", pointDict.class]
					}];
			}
            return nil;
        }
		NSNumber* x = pointDict[@"x"];
		NSNumber* y = pointDict[@"y"];
		if(x == nil || y == nil) {
			if(error != nil) {
				*error = [NSError
					errorWithDomain:RTRPluginErrorDomain
					code:-1
					userInfo:@{
						NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Point dictionary must have `x` and `y` keys. found %@", self]
					}];
			}
			return nil;
		}
		return @(CGPointMake(x.doubleValue, y.doubleValue));
	} error:error];
}

- (NSArray*)rtr_transformToDictionaryQuadrangle:(NSError**)error
{
	if(self.count != 4) {
		if(error != nil) {
			*error = [NSError
				errorWithDomain:RTRPluginErrorDomain
				code:-1
				userInfo:@{
					NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Quadrangle must have 4 points. Found %@", self]
				}];
		}
		return nil;
	}
	return [self rtr_map:^id(NSValue* pointValue) {
		CGPoint point = pointValue.CGPointValue;
		return @{
			@"x": @((NSInteger)point.x),
			@"y": @((NSInteger)point.y)
		};
	}];
}

@end
