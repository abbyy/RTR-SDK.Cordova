/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRCoreApiPluginAdapter.h"
#import "RTRPluginConstants.h"
#import "NSDictionary+RTRSettings.h"
#import "RTRExportUtilities.h"
#import "NSDictionary+RTRSettings.h"
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

@interface NSError (rtr_Shortcuts)

+ (NSError*)rtr_missingKeyError:(NSString*)key;
+ (NSError*)rtr_errorWithDescription:(NSString*)description;

@end

@implementation NSError (rtr_Shortcuts)

+ (NSError*)rtr_missingKeyError:(NSString*)key
{
	NSDictionary* userInfo = @{
		NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Missing %@ key.", key]
	};
	return [NSError
		errorWithDomain:RTRPluginErrorDomain
		code:-1
		userInfo:userInfo];
}

+ (NSError*)rtr_errorWithDescription:(NSString*)description
{
	NSDictionary* userInfo = @{
		NSLocalizedDescriptionKey: description
	};
	return [NSError
		 errorWithDomain:RTRPluginErrorDomain
		 code:-1
		 userInfo:userInfo];
}

@end

@implementation RTRCoreApiPluginAdapter

- (instancetype)initWithEngine:(RTREngine*)engine
{
	self = [super init];
	if(self != nil) {
		_engine = engine;
	}
	return self;
}

- (void)extractData:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	NSString* profile = query[RTRDataCaptureProfileKey] ?: @"BusinessCards";

	BOOL isTextOrientationEnabled = [(query[RTRCAEnableTextOrientationDetection] ?: @YES) boolValue];
    NSError* error;
    NSArray* recognitionLanguages = [query rtr_parseArray:RTRRecognitionLanguagesKey defaultValue:@[RTRLanguageNameEnglish] error:&error];
    if(recognitionLanguages == nil) {
        if(onError != nil) {
            onError(error);
        }
        return;
    }
	
	CGRect areaOfInterest = CGRectZero;
	if(query[RTRAreaOfInterestKey] != nil) {
		if(![query[RTRAreaOfInterestKey] rtr_asRect:&areaOfInterest error:&error]) {
			if(onError != nil) {
				onError(error);
			}
			return;
		}
	}
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	coreApi.dataCaptureSettings.profile = profile;
	coreApi.dataCaptureSettings.textOrientationDetectionEnabled = isTextOrientationEnabled;
	[coreApi.dataCaptureSettings setAreaOfInterest:areaOfInterest];

	error = [[coreApi.dataCaptureSettings.configureDataCaptureProfile
		setRecognitionLanguages:[NSSet setWithArray:recognitionLanguages]]
	 	checkAndApply];
	if(error != nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	NSString* imageUri = query[RTRCAImageUri];
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	__block NSInteger orientationAngle = 0;
	NSMutableArray* warnings = @[].mutableCopy;
	NSArray<RTRDataField*>* result = [coreApi
		extractDataFromImage:image
		onProgress:^BOOL(NSInteger percentage, RTRCallbackWarningCode warningCode) {
			if(warningCode != RTRCallbackWarningNoWarning) {
				[warnings addObject:[NSString rtr_stringFromWarningCode:warningCode]];
			}
			return YES;
		}
		onTextOrientationDetected:^(NSInteger angle) {
			orientationAngle = angle;
		}
		error:&error];

	if(result == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	NSMutableDictionary* jsonResult = @{
		@"dataFields": [result rtr_map:^id(RTRDataField* field) {
			return [NSDictionary rtr_dictionaryFromDataField:field];
		}],
		@"orientation": @(orientationAngle)
	}.mutableCopy;
	if(warnings.count > 0) {
		jsonResult[@"warnings"] = warnings;
	}
	onSuccess(jsonResult);
}

- (void)recognizeText:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	BOOL isTextOrientationEnabled = [(query[RTRCAEnableTextOrientationDetection] ?: @YES) boolValue];
    NSError* error;
	NSArray* recognitionLanguages = [query rtr_parseArray:RTRRecognitionLanguagesKey defaultValue:@[RTRLanguageNameEnglish] error:&error];
    if(recognitionLanguages == nil) {
        if(onError != nil) {
            onError(error);
        }
        return;
    }

	CGRect areaOfInterest = CGRectZero;
	if(query[RTRAreaOfInterestKey] != nil) {
		if(![query[RTRAreaOfInterestKey] rtr_asRect:&areaOfInterest error:&error]) {
			if(onError != nil) {
				onError(error);
			}
			return;
		}
	}
	NSString* imageUri = query[RTRCAImageUri];
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	[coreApi.textRecognitionSettings setRecognitionLanguages:[NSSet setWithArray:recognitionLanguages]];
	[coreApi.textRecognitionSettings setAreaOfInterest:areaOfInterest];
	coreApi.textRecognitionSettings.textOrientationDetectionEnabled = isTextOrientationEnabled;

	NSMutableArray* warnings = @[].mutableCopy;

	__block NSInteger orientationAngle = 0;
	NSArray<RTRTextBlock*>* results = [coreApi
		recognizeTextOnImage:image
		onProgress:^BOOL(NSInteger percentage, RTRCallbackWarningCode warningCode) {
			if(warningCode != RTRCallbackWarningNoWarning) {
				[warnings addObject:[NSString rtr_stringFromWarningCode:warningCode]];
			}
			return YES;
		}
		onTextOrientationDetected:^(NSInteger angle) {
			orientationAngle = angle;
		}
		error:&error];
	if(results == nil) {
		if(onError != nil) {
			onError(error);
		}
        return;
	}
	NSMutableDictionary* jsonResult = @{
		@"text": [[results rtr_map:^id(RTRTextBlock* block) {
			return block.rtr_text;
		}] componentsJoinedByString:@"\n\n"],
		@"textBlocks": [results rtr_map:^id(RTRTextBlock* block) {
			return [NSDictionary rtr_dictionaryFromTextBlock:block];
		}] ?: @[],
		@"orientation": @(orientationAngle)
	}.mutableCopy;
	if(warnings.count > 0) {
		jsonResult[@"warnings"] = warnings;
	}
	onSuccess(jsonResult);
}

- (void)detectBoundaryOnImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	CGSize documentSize;
	NSError* error;
	if(![query rtr_parseDocumentSize:RTRICDocumentSizeKey defaultValue:CGSizeZero outValue:&documentSize error:&error]) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	CGRect areaOfInterest = CGRectZero;
	if(query[RTRAreaOfInterestKey] != nil) {
		if(![query[RTRAreaOfInterestKey] rtr_asRect:&areaOfInterest error:&error]) {
			if(onError != nil) {
				onError(error);
			}
			return;
		}
	}
	NSString* detectionModeString = query[RTRCABoundaryDetectionMode];
	RTRDetectDocumentBoundaryMode detectionMode = RTRDetectDocumentBoundaryModeDefault;
	if(detectionModeString != nil) {
		NSNumber* detectionModeValue = NSDictionary.rtr_stringToDetectionMode[detectionModeString];
		if(detectionModeValue == nil) {
			if(onError != nil) {
				error = [NSError errorWithDomain:RTRPluginErrorDomain code:-1 userInfo:@{
					NSLocalizedDescriptionKey: [NSString stringWithFormat:@"Invalid boundary detection mode `%@`. Avaliable values: %@", detectionModeString, NSDictionary.rtr_stringToDetectionMode.allKeys]
				}];
				onError(error);
			}
			return;
		}
		detectionMode = detectionModeValue.integerValue;
	}
	NSString* imageUri = query[RTRCAImageUri];
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPIDetectDocumentBoundaryOperation> operation = [coreApi createDetectDocumentBoundaryOperation];
	operation.areaOfInterest = areaOfInterest;
	operation.documentSize = documentSize;
	operation.mode = detectionMode;
	id<RTRCoreAPIImage> coreApiImage = [coreApi loadImage:image error:&error];
	if(coreApiImage == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	if(![operation applyToImage:coreApiImage]) {
		if(onError != nil) {
			onError(operation.error);
		}
		return;
	}
	NSMutableDictionary* result = @{
		RTRICDocumentSizeKey: [NSDictionary rtr_dictionaryFromSize:operation.documentSize]
	}.mutableCopy;
	if(operation.documentBoundary.count > 0) {
		result[RTRCADocumentBoundary] = [operation.documentBoundary rtr_map:^id(NSValue* pointValue) {
			CGPoint point = pointValue.CGPointValue;
			return [NSDictionary rtr_dictionaryFromPoint:point];
		}];
	}
	onSuccess(result);
}

- (void)assessOCRQualityOnImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
    NSError* error;
	NSArray* boundaryWithDicts = [query rtr_parseArray:RTRCADocumentBoundary defaultValue:nil error:&error];
    
	NSArray* boundary;
	if(boundaryWithDicts != nil) {
		boundary = [boundaryWithDicts rtr_transformToNSValuesQuadrangle:&error];
		if(boundary == nil) {
			if(onError != nil) {
				onError(error);
			}
			return;
		}
	}

	NSString* imageUri = query[RTRCAImageUri];
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPIQualityAssessmentForOCROperation> operation = [coreApi createQualityAssessmentForOCROperation];
	if(boundary != nil) {
		operation.documentBoundary = boundary;
	}
	id<RTRCoreAPIImage> coreApiImage = [coreApi loadImage:image error:&error];
	if(coreApiImage == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	if(![operation applyToImage:coreApiImage]) {
		if(onError != nil) {
			onError(operation.error);
		}
		return;
	}
	onSuccess(@{
		@"qualityAssessmentForOcrBlocks": [operation.qualityAssessmentBlocks rtr_map:^NSDictionary*(RTRQualityAssessmentForOCRBlock* block) {
			return @{
				@"quality": @(block.quality),
				@"type": [NSString rtr_stringFromOcrQualityBlockType:block.type],
				@"rect": [NSDictionary rtr_dictionaryFromRect:block.rect]
			};
		}] ?: @[]
	});
}

- (void)rotateImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	NSNumber* angleNumber = query[RTRCARotationAngleDegrees];
	if(angleNumber == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCARotationAngleDegrees]);
		}
		return;
	}
	NSError* error;
	NSString* imageUri = query[RTRCAImageUri];
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPIRotateOperation> operation = [coreApi createRotateOperation];
	operation.angle = angleNumber.integerValue;

	id<RTRCoreAPIImage> coreApiImage = [coreApi loadImage:image error:&error];
	if(coreApiImage == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	if(![operation applyToImage:coreApiImage]) {
		if(onError != nil) {
			onError(operation.error);
		}
		return;
	}
	UIImage* destinationImage = coreApiImage.UIImage;
	NSMutableDictionary* exportResult = [self exportImage:destinationImage withSettings:query[RTRCAExportResultOptions] onError:onError].mutableCopy;
	if(exportResult == nil) {
		return;
	}
	onSuccess(exportResult);
}

- (void)cropImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	CGSize documentSize;
	NSError* error;
	if(![query rtr_parseDocumentSize:RTRICDocumentSizeKey defaultValue:CGSizeZero outValue:&documentSize error:&error]) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	NSArray* boundaryWithDicts = [query rtr_parseArray:RTRCADocumentBoundary defaultValue:nil error:&error];
	if(boundaryWithDicts == nil) {
		if(onError != nil) {
            if(error == nil) {
                error = [NSError rtr_missingKeyError:RTRCADocumentBoundary];
            }
			onError(error);
		}
		return;
	}
	NSArray* boundary = [boundaryWithDicts rtr_transformToNSValuesQuadrangle:&error];
	if(boundary == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	NSString* imageUri = query[RTRCAImageUri];
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPICropOperation> operation = [coreApi createCropOperation];
	operation.documentBoundary = boundary;
	operation.documentSize = documentSize;

	id<RTRCoreAPIImage> coreApiImage = [coreApi loadImage:image error:&error];
	if(coreApiImage == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	if(![operation applyToImage:coreApiImage]) {
		if(onError != nil) {
			onError(operation.error);
		}
		return;
	}
	UIImage* destinationImage = coreApiImage.UIImage;
	NSMutableDictionary* exportResult = [self exportImage:destinationImage withSettings:query[RTRCAExportResultOptions] onError:onError].mutableCopy;
	if(exportResult == nil) {
		return;
	}
	exportResult[RTRCAImageResolution] = [NSDictionary rtr_resolutionDictionaryFromSize:operation.imageResolution];
	onSuccess(exportResult);
}

- (void)exportImagesToPdf:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	NSArray* images = query[RTRCAExportPdfImages];
	if(images == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAExportPdfImages]);
		}
		return;
	}
	NSDictionary* result = query[RTRCAExportResultOptions];
	if(result == nil) {
		result = @{
			RTRICDestinationKey: @"file"
		};
	}
	NSString* destinationTypeString = [result[RTRICDestinationKey] lowercaseString] ?: @"file";
	NSNumber* destinationTypeValue = NSDictionary.rtr_stringToDestinationType[destinationTypeString];
	if(destinationTypeValue == nil) {
		if(onError != nil) {
			NSString* description = [NSString stringWithFormat:@"Invalid destination type `%@`. Avaliable values: %@", destinationTypeString, NSDictionary.rtr_stringToDestinationType.allKeys];
			onError([NSError rtr_errorWithDescription:description]);
		}
		return;
	}
	RTRImageDestinationType destinationType = (RTRImageDestinationType)destinationTypeValue.integerValue;

	if(destinationType == RTRImageDestinationTypeBase64 && images.count > 1) {
		if(onError != nil) {
			NSString* description = [NSString stringWithFormat:@"Base64 export is available for single page documents only. Found %@ pages.", @(images.count)];
			onError([NSError rtr_errorWithDescription:description]);
		}
		return;
	}

	NSDictionary<NSString*, NSString*>* pdfInfo = query[@"pdfInfo"];
	NSString* pdfUriSuffix;
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	NSError* error;
	switch(destinationType) {
		case RTRImageDestinationTypeBase64: {
			RTRMemoryOutputStream* stream = [RTRMemoryOutputStream new];
			id<RTRCoreAPIExportToPdfOperation> operation = [coreApi createExportToPdfOperation:stream];
			[self configurePdfOperation:operation withMetadataFrom:pdfInfo];
			if(![self exportImages:images intoPdfUsingOperation:operation error:&error]) {
				if(onError != nil) {
					onError(error);
				}
				return;
			}
			pdfUriSuffix = [stream.data base64EncodedStringWithOptions:0];
			break;
		}
		case RTRImageDestinationTypeFile: {
			NSString* destinationPath = result[RTRCAExportFilePath];
			if(destinationPath == nil) {
				destinationPath = [RTRExportUtilities generatePathWithExtension:@"pdf"];
			}
			RTRFileOutputStream* stream = [[RTRFileOutputStream alloc] initWithFilePath:destinationPath];
			id<RTRCoreAPIExportToPdfOperation> operation = [coreApi createExportToPdfOperation:stream];
			[self configurePdfOperation:operation withMetadataFrom:pdfInfo];
			if(![self exportImages:images intoPdfUsingOperation:operation error:&error]) {
				if(onError != nil) {
					onError(error);
				}
				return;
			}
			pdfUriSuffix = destinationPath;
			break;
		}
	}
	NSString* uriPrefix = [RTRExportUtilities uriPrefixForDestination:destinationTypeString extension:@"pdf"];
	onSuccess(@{
		RTRCAPdfUri: [uriPrefix stringByAppendingString:pdfUriSuffix]
	});
}

- (void)configurePdfOperation:(id<RTRCoreAPIExportToPdfOperation>)operation withMetadataFrom:(NSDictionary*)metadata
{
	NSString* pdfInfoTitle = metadata[@"title"];
	if(pdfInfoTitle != nil) {
		operation.pdfInfoTitle = pdfInfoTitle;
	}
	NSString* pdfInfoSubject = metadata[@"subject"];
	if(pdfInfoSubject != nil) {
		operation.pdfInfoSubject = pdfInfoSubject;
	}
	NSString* pdfInfoKeywords = metadata[@"keywords"];
	if(pdfInfoKeywords != nil) {
		operation.pdfInfoKeywords = pdfInfoKeywords;
	}
	NSString* pdfInfoAuthor = metadata[@"author"];
	if(pdfInfoAuthor != nil) {
		operation.pdfInfoAuthor = pdfInfoAuthor;
	}
	NSString* pdfInfoCompany = metadata[@"company"];
	if(pdfInfoCompany != nil) {
		operation.pdfInfoCompany = pdfInfoCompany;
	}
	NSString* pdfInfoCreator = metadata[@"creator"];
	if(pdfInfoCreator != nil) {
		operation.pdfInfoCreator = pdfInfoCreator;
	}
	NSString* pdfInfoProducer = metadata[@"producer"];
	if(pdfInfoProducer != nil) {
		operation.pdfInfoProducer = pdfInfoProducer;
	}
}

- (BOOL)exportImages:(NSArray*)images intoPdfUsingOperation:(id<RTRCoreAPIExportToPdfOperation>)operation error:(NSError**)error
{
	for(NSDictionary* imageDict in images) {
		@autoreleasepool {
			operation.compression = RTRCoreAPIExportCompressionLowLevel;
			operation.pageSize = CGSizeZero;
			NSString* imageUri = imageDict[RTRCAImageUri];
			if(imageUri == nil) {
				[operation close];
				if(error != nil) {
					*error = [NSError rtr_missingKeyError:RTRCAImageUri];
				}
				return NO;
			}
			UIImage* image = [self imageFromUri:imageUri withError:error];
			if(image == nil) {
				[operation close];
				return NO;
			}
			NSInteger pageWidth = [(imageDict[@"pageWidth"] ?: @((NSInteger)image.size.width)) integerValue];
			NSInteger pageHeight = [(imageDict[@"pageHeight"] ?: @((NSInteger)image.size.height)) integerValue];
			operation.pageSize = CGSizeMake(pageWidth, pageHeight);
			NSString* compressionLevelString = [imageDict[RTRICCompressionLevelKey] lowercaseString] ?: @"low";
			NSNumber* compressionLevelValue = NSDictionary.rtr_stringToExportCompressionLevel[compressionLevelString] ?: @(RTRCoreAPIExportCompressionLowLevel);
			RTRCoreAPIExportCompressionLevel compressionLevel = (RTRCoreAPIExportCompressionLevel)(compressionLevelValue.integerValue);
			operation.compression = compressionLevel;
			if(![operation addPageWithImage:image]) {
				[operation close];
				if(error != nil) {
					*error = operation.error;
				}
				return NO;
			}
		}
	}
	if(![operation close]) {
		if(error != nil) {
			*error = operation.error;
		}
		return NO;
	}
	return YES;
}

- (void)exportImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	NSString* imageUri = query[RTRCAImageUri];
	NSError* error;
	if(imageUri == nil) {
		if(onError != nil) {
			onError([NSError rtr_missingKeyError:RTRCAImageUri]);
		}
		return;
	}
	UIImage* image = [self imageFromUri:imageUri withError:&error];
	if(image == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	NSDictionary* result = [self exportImage:image withSettings:query[RTRCAExportResultOptions] onError:onError];
	if(result != nil) {
		onSuccess(result);
	}
}

- (NSDictionary*)exportImage:(UIImage*)image withSettings:(NSDictionary*)settings onError:(void(^)(NSError*))onError
{
	if(settings == nil) {
		settings = @{
			RTRICDestinationKey: @"base64"
		};
	}
	NSError* error;
	NSInteger enumValue;
	if(![settings
		rtr_parseEnum:RTRICDestinationKey
		defaultValue:RTRImageDestinationTypeBase64
		variants:NSDictionary.rtr_stringToDestinationType
		outValue:&enumValue
		error:&error]) {
		if(onError != nil) {
			onError(error);
		}
		return nil;
	}
	RTRImageDestinationType destinationType = enumValue;
	if(![settings
		rtr_parseEnum:RTRICExportTypeKey
		defaultValue:RTRImageCaptureEncodingTypeJpg
		variants:NSDictionary.rtr_stringToExportType
		outValue:&enumValue
		error:&error]) {
		if(onError != nil) {
			onError(error);
		}
		return nil;
	}
	RTRImageCaptureEncodingType exportType = enumValue;
	NSString* extension;
	switch(exportType) {
		case RTRImageCaptureEncodingTypeJpg:
			extension = @"jpg";
			break;
		case RTRImageCaptureEncodingTypePng:
			extension = @"png";
			break;
		default: {
			if(onError != nil) {
				NSString* description = [NSString
					stringWithFormat:@"Invalid export type `%@`.", NSDictionary.rtr_exportTypeToString[@(exportType)]];
				onError([NSError rtr_errorWithDescription:description]);
			}
			return nil;
		}
	}
	NSString* uriPrefix = [RTRExportUtilities
		uriPrefixForDestination:NSDictionary.rtr_destinationTypeToString[@(destinationType)]
		extension:extension];

    if(![settings
        rtr_parseEnum:RTRICCompressionLevelKey
        defaultValue:RTRCoreAPIExportCompressionLowLevel
        variants:NSDictionary.rtr_stringToExportCompressionLevel
        outValue:&enumValue
        error:&error]) {
        if(onError != nil) {
            onError(error);
        }
        return nil;
    }
	RTRCoreAPIExportCompressionLevel compressionLevel = enumValue;

	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	switch(destinationType) {
		case RTRImageDestinationTypeBase64: {
			RTRMemoryOutputStream* stream = [RTRMemoryOutputStream new];
			id<RTRCoreAPIExportOperation> operation = [self
				operationWithStream:stream
				coreApi:coreApi
				exportType:exportType
				compressionLevel:compressionLevel];
			if(![self addImage:image toOperation:operation errorHandler:onError]) {
				return nil;
			}
			return @{
				RTRCAImageUri: [uriPrefix stringByAppendingString:[stream.data base64EncodedStringWithOptions:0]],
				RTRCAImageSize: [NSDictionary rtr_imageSizeDictionaryFromSize:image.size]
			};
		}
		case RTRImageDestinationTypeFile: {
			NSString* destinationPath = settings[RTRCAExportFilePath];
			if(destinationPath == nil) {
				destinationPath = [RTRExportUtilities generatePathWithExtension:extension];
			}
			id<RTROutputStream> stream = [[RTRFileOutputStream alloc] initWithFilePath:destinationPath error:&error];
            if(stream == nil) {
                if(onError != nil) {
                    onError(error);
                }
                return nil;
            }
			id<RTRCoreAPIExportOperation> operation = [self
				operationWithStream:stream
				coreApi:coreApi exportType:exportType
				compressionLevel:compressionLevel];
			if(![self addImage:image toOperation:operation errorHandler:onError]) {
				return nil;
			}
			return @{
				RTRCAImageUri: [uriPrefix stringByAppendingString:destinationPath],
				RTRCAImageSize: [NSDictionary rtr_imageSizeDictionaryFromSize:image.size]
			};
		}
	}
}

- (id<RTRCoreAPIExportOperation>)operationWithStream:(id<RTROutputStream>)stream
	coreApi:(id<RTRCoreAPI>)coreApi
	exportType:(RTRImageCaptureEncodingType)exportType
	compressionLevel:(RTRCoreAPIExportCompressionLevel)compressionLevel
{
	switch(exportType) {
		case RTRImageCaptureEncodingTypePng:
			return [coreApi createExportToPngOperation:stream];
		case RTRImageCaptureEncodingTypeJpg: {
			id<RTRCoreAPIExportToJpgOperation> jpgExport = [coreApi createExportToJpgOperation:stream];
			jpgExport.compression = compressionLevel;
			return jpgExport;
		}
		default:
			return nil;
	}
}

- (BOOL)addImage:(UIImage*)image
	toOperation:(id<RTRCoreAPIExportOperation>)operation
	errorHandler:(void(^)(NSError*))onError
{
	if(![operation addPageWithImage:image]) {
		if(onError != nil) {
			onError(operation.error);
		}
		return NO;
	}
	if(![operation close]) {
		if(onError != nil) {
			onError(operation.error);
		}
		return NO;
	}
	return YES;
}

- (nullable UIImage*)imageFromUri:(NSString*)uriString withError:(NSError**)error
{
	NSURL* url = [NSURL URLWithString:uriString];
	if(url == nil) {
		if(error != nil) {
			*error = [NSError rtr_errorWithDescription:[NSString stringWithFormat:@"Invalid uri format %@.", uriString]];
		}
		return nil;
	}
	NSData* imageData = [NSData dataWithContentsOfURL:url];
	if(imageData == nil) {
		if(error != nil) {
			*error = [NSError rtr_errorWithDescription:[NSString stringWithFormat:@"Missing data from url %@.", url]];
		}
		return nil;
	}
	UIImage* image = [UIImage imageWithData:imageData];
	if(image == nil) {
		if(error != nil) {
			*error = [NSError rtr_errorWithDescription:[NSString stringWithFormat:@"Could not initialize the image from %@.", url]];
		}
		return nil;
	}
	return image;
}

@end
