/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRImageCapturePluginAdapter.h"
#import "NSDictionary+RTRSettings.h"
#import "RTRExportUtilities.h"
#import "RTRImageCaptureViewController.h"

@implementation RTRImageCapturePluginAdapter

- (instancetype)initWithEngine:(RTREngine*)engine
{
	self = [super init];
	if(self != nil) {
		_engine = engine;
	}
	return self;
}

- (void)startImageCapture:(NSDictionary*)query
	rootController:(UIViewController*)rootController
	localizer:(id<RTRLocalizer>)localizer
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess
{
	NSError* error;
	RTRImageCaptureSettings* settings = [[RTRImageCaptureSettings alloc] initWithDictionary:query error:&error];
	if(settings == nil) {
		if(onError != nil) {
			onError(error);
		}
		return;
	}
	RTRImageCaptureViewController* imageCaptureController = [[RTRImageCaptureViewController alloc]
		initWithEngine:self.engine
		localizer:localizer
		settings:settings
		error:&error];
	if(imageCaptureController == nil) {
		onError(error);
		return;
	}
	__weak RTRImageCaptureViewController* weakImageCaptureController = imageCaptureController;
	imageCaptureController.onError = ^(id<AUIMultiPageImageCaptureResult> result, NSError* error) {
		NSError* clearError;
		if(![result clearWithError:&clearError]) {
			NSLog(@"Error occured while clear image capture internal storage: %@", clearError);
		}
		[weakImageCaptureController dismissViewControllerAnimated:YES completion:^{
			onError(error);
		}];
	};
	imageCaptureController.onCancel = ^(id<AUIMultiPageImageCaptureResult> result) {
		dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
			NSError* error;
			NSDictionary* response = [self exportWithSettings:settings result:result error:&error];
			response = [self specifyResultInfoWithSettings:settings userAction:@"Canceled" inResponse:response];
			dispatch_async(dispatch_get_main_queue(), ^{
				[weakImageCaptureController dismissViewControllerAnimated:YES completion:^{
					if(response == nil) {
						onError(error);
					} else {
						onSuccess(response);
					}
				}];
			});
		});
	};
	imageCaptureController.onSuccess = ^(id<AUIMultiPageImageCaptureResult> result) {
		dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
			NSError* error;
			NSDictionary* response = [self exportWithSettings:settings result:result error:&error];
			response = [self specifyResultInfoWithSettings:settings userAction:@"Manually stopped" inResponse:response];
			dispatch_async(dispatch_get_main_queue(), ^{
				[weakImageCaptureController dismissViewControllerAnimated:YES completion:^{
					if(response == nil) {
						onError(error);
					} else {
						onSuccess(response);
					}
				}];
			});
		});
	};
	imageCaptureController.modalPresentationStyle = UIModalPresentationFullScreen;
	[rootController presentViewController:imageCaptureController animated:YES completion:nil];
}

- (NSDictionary*)specifyResultInfoWithSettings:(RTRImageCaptureSettings*)settings userAction:(NSString*)userAction inResponse:(NSDictionary*)response
{
	NSString* uriPrefix = [RTRExportUtilities
		uriPrefixForDestination:NSDictionary.rtr_destinationTypeToString[@(settings.destination)]
		extension:NSDictionary.rtr_exportTypeToString[@(settings.exportType)]];
	NSDictionary* userInfo = @{
		RTRCallbackUserActionKey: userAction,
		@"uriPrefix": uriPrefix
	};
	NSMutableDictionary* mutableResponse = response.mutableCopy;
	mutableResponse[RTRCallbackResultInfoKey] = userInfo;
	return mutableResponse;
}

- (NSDictionary*)exportWithSettings:(RTRImageCaptureSettings*)settings
	result:(id<AUIMultiPageImageCaptureResult>)result
	error:(NSError**)error
{
	if(settings.exportType == RTRImageCaptureEncodingTypePdf) {
		return [self exportAsPdf:settings result:result error:error];
	} else {
		return [self exportAsImages:settings result:result error:error];
	}
}

- (NSDictionary*)exportAsImages:(RTRImageCaptureSettings*)settings result:(id<AUIMultiPageImageCaptureResult>)result error:(NSError**)error
{
	NSParameterAssert(settings.exportType != RTRImageCaptureEncodingTypePdf);
	NSArray* pages = [result pagesWithError:error];
	if(pages == nil) {
		return nil;
	}
	NSMutableArray* imagesResponse = @[].mutableCopy;
	for(AUIPageId identifier in pages) {
		@autoreleasepool {
			RTRImageCaptureEncodingType singleImageEncoding = settings.exportType;
			if(singleImageEncoding == RTRImageCaptureEncodingTypePdf) {
				singleImageEncoding = RTRImageCaptureEncodingTypeJpg;
			}
			UIImage* image = [result loadImageWithId:identifier error:error];
			if(image == nil) {
				return nil;
			}
			if(settings.destination == RTRImageDestinationTypeFile) {
				NSString* filePath = [RTRExportUtilities generatePathWithExtension:NSDictionary.rtr_exportTypeToString[@(settings.exportType)]];
				RTRFileOutputStream* stream = [[RTRFileOutputStream alloc] initWithFilePath:filePath];
				NSDictionary* imageResponse = [self exportImage:image toFile:stream settings:settings error:error];
				if(imageResponse == nil) {
					return nil;
				}
				[imagesResponse addObject:imageResponse];
			} else {
				RTRMemoryOutputStream* stream = [RTRMemoryOutputStream new];
				NSDictionary* imageResponse = [self exportImage:image toBase64:stream settings:settings error:error];
				if(imageResponse == nil) {
					return nil;
				}
				[imagesResponse addObject:imageResponse];
			}
		}
	}
	return @{
		@"images": imagesResponse
	};
}

- (NSDictionary*)exportAsPdf:(RTRImageCaptureSettings*)settings result:(id<AUIMultiPageImageCaptureResult>)result error:(NSError**)error
{
	NSParameterAssert(settings.exportType == RTRImageCaptureEncodingTypePdf);
	NSArray* pages = [result pagesWithError:error];
	if(pages == nil) {
		return nil;
	}
	NSDictionary* pdfResponse;
	if(settings.destination == RTRImageDestinationTypeBase64 && pages.count <= 1) {
		RTRMemoryOutputStream* stream = [RTRMemoryOutputStream new];
		pdfResponse = [self exportPdfToBase64:stream settings:settings result:result error:error];
		if(pdfResponse == nil) {
			return nil;
		}
	} else {
		NSString* filePath = [RTRExportUtilities generatePathWithExtension:NSDictionary.rtr_exportTypeToString[@(settings.exportType)]];
		RTRFileOutputStream* stream = [[RTRFileOutputStream alloc] initWithFilePath:filePath];
		pdfResponse = [self exportPdfToFile:stream settings:settings result:result error:error];
		if(pdfResponse == nil) {
			return nil;
		}
	}
	return @{
		@"pdfInfo": pdfResponse
	};
}

- (NSDictionary*)exportImage:(UIImage*)image
	toFile:(RTRFileOutputStream*)stream
	settings:(RTRImageCaptureSettings*)settings
	error:(NSError**)error
{
	if(![self exportImage:image stream:stream settings:settings error:error]) {
		return nil;
	}
	return @{
		@"resultInfo": @{
			@"exportType": [NSDictionary.rtr_exportTypeToString[@(settings.exportType)] capitalizedString],
			@"imageSize": [NSDictionary rtr_dictionaryFromSize:image.size]
		},
		@"filePath": stream.filePath
	};
}

- (NSDictionary*)exportImage:(UIImage*)image
	toBase64:(RTRMemoryOutputStream*)stream
	settings:(RTRImageCaptureSettings*)settings
	error:(NSError**)error
{
	if(![self exportImage:image stream:stream settings:settings error:error]) {
		return nil;
	}
	return @{
		@"resultInfo": @{
			@"exportType": [NSDictionary.rtr_exportTypeToString[@(settings.exportType)] capitalizedString],
			@"imageSize": [NSDictionary rtr_dictionaryFromSize:image.size]
		},
		@"base64": [stream.data base64EncodedStringWithOptions:0]
	};
}

- (BOOL)exportImage:(UIImage*)image
	stream:(id<RTROutputStream>)stream
	settings:(RTRImageCaptureSettings*)settings
	error:(NSError**)error
{
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPIExportOperation> operation;
	switch(settings.exportType) {
		case RTRImageCaptureEncodingTypePng:
			operation = [coreApi createExportToPngOperation:stream];
			break;
		case RTRImageCaptureEncodingTypeJpg: {
			id<RTRCoreAPIExportToJpgOperation> jpgOperation = [coreApi createExportToJpgOperation:stream];
			jpgOperation.compression = settings.compressionLevel;
			operation = jpgOperation;
			break;
		}
		case RTRImageCaptureEncodingTypeJpeg2000: {
			id<RTRCoreAPIExportToJpeg2000Operation> jpeg2000Operation = [coreApi createExportToJpeg2000Operation:stream];
			jpeg2000Operation.compression = settings.compressionLevel;
			operation = jpeg2000Operation;
			break;
		}
		case RTRImageCaptureEncodingTypePdf:
			NSAssert(NO, @"Internal inconsistency");
	}
	if(![operation addPageWithImage:image]) {
		if(error != nil) {
			*error = operation.error;
		}
		[operation close];
		return NO;
	}
	if(![operation close]) {
		if(error != nil) {
			*error = operation.error;
		}
		return NO;
	}
	return YES;
}

- (NSDictionary*)exportPdfToBase64:(RTRMemoryOutputStream*)stream
	settings:(RTRImageCaptureSettings*)settings
	result:(id<AUIMultiPageImageCaptureResult>)result
	error:(NSError**)error
{
	if(![self exportPdfToStream:stream settings:settings result:result error:error]) {
		return nil;
	}
	NSArray* pages = [result pagesWithError:error];
	if(pages == nil) {
		return nil;
	}
	return @{
		@"pagesCount": @(pages.count),
		@"base64": [stream.data base64EncodedStringWithOptions:0]
	};
}

- (NSDictionary*)exportPdfToFile:(RTRFileOutputStream*)stream
	settings:(RTRImageCaptureSettings*)settings
	result:(id<AUIMultiPageImageCaptureResult>)result
	error:(NSError**)error
{
	if(![self exportPdfToStream:stream settings:settings result:result error:error]) {
		return nil;
	}
	NSArray* pages = [result pagesWithError:error];
	if(pages == nil) {
		return nil;
	}
	return @{
		@"pagesCount": @(pages.count),
		@"filePath": stream.filePath
	};
}

- (BOOL)exportPdfToStream:(id<RTROutputStream>)stream
	settings:(RTRImageCaptureSettings*)settings
	result:(id<AUIMultiPageImageCaptureResult>)result
	error:(NSError**)error
{
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPIExportToPdfOperation> operation = [coreApi createExportToPdfOperation:stream];
	operation.compression = settings.compressionLevel;
	NSArray* pages = [result pagesWithError:error];
	if(pages == nil) {
		[operation close];
		return NO;
	}
	for(AUIPageId identifier in pages) {
		@autoreleasepool {
			UIImage* image = [result loadImageWithId:identifier error:error];
			if(image == nil) {
				[operation close];
				return NO;
			}
			if(![operation addPageWithImage:image]) {
				if(error != nil) {
					*error = operation.error;
				}
				[operation close];
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

@end

@implementation RTRImageCaptureSettings

- (instancetype)initWithDictionary:(NSDictionary*)dict error:(NSError**)error
{
	self = [super init];
	if(self != nil) {
		if(![dict rtr_parseBool:RTRICFlashlightButtonVisibleKey defaultValue:YES outValue:&_isFlashlightButtonVisible error:error]) {
			return nil;
		}
		if(![dict rtr_parseBool:RTRICGalleryButtonVisibleKey defaultValue:YES outValue:&_isGalleryButtonVisible error:error]) {
			return nil;
		}
		if(![dict rtr_parseBool:RTRICCaptureButtonVisibleKey defaultValue:NO outValue:&_isCaptureButtonVisible error:error]) {
			return nil;
		}
		if(![dict rtr_parseBool:RTRICShowPreviewKey defaultValue:NO outValue:&_shouldShowPreview error:error]) {
			return nil;
		}
		if(![dict rtr_parseInteger:RTRICRequiredPageCountKey defaultValue:0 outValue:&_requiredPageCount error:error]) {
			return nil;
		}

		NSInteger enumValue;
		if(![dict rtr_parseEnum:RTROrientationPolicy defaultValue:UIInterfaceOrientationMaskAll variants:NSDictionary.rtr_stringToOrientationMask outValue:&enumValue error:error]) {
			return nil;
		}
		_supportedOrientations = enumValue;
		if(![dict rtr_parseEnum:RTRICCompressionLevelKey defaultValue:RTRCoreAPIExportCompressionNormalLevel variants:NSDictionary.rtr_stringToExportCompressionLevel outValue:&enumValue error:error]) {
			return nil;
		}
		_compressionLevel = enumValue;
		if(![dict rtr_parseEnum:RTRICCameraResolutionKey defaultValue:AUICameraResolutionFullHD variants:NSDictionary.rtr_stringToCameraResolution outValue:&enumValue error:error]) {
			return nil;
		}
		_cameraResolution = enumValue;
		if(![dict rtr_parseEnum:RTRICExportTypeKey defaultValue:RTRImageCaptureEncodingTypeJpg variants:NSDictionary.rtr_stringToExportType outValue:&enumValue error:error]) {
			return nil;
		}
		_exportType = enumValue;
		if(![dict rtr_parseEnum:RTRICDestinationKey defaultValue:RTRImageDestinationTypeFile variants:NSDictionary.rtr_stringToDestinationType outValue:&enumValue error:error]) {
			return nil;
		}
		_destination = enumValue;

		NSDictionary* icSettings = dict[RTRICDefaultImageSettingsKey];
		if(icSettings != nil) {
			if(![icSettings rtr_parseFloat:RTRICMinimumDocumentToViewRatioKey defaultValue:0.15 outValue:&_minimumDocumentToViewRatio error:error]) {
				return nil;
			}
			if(![icSettings rtr_parseInteger:RTRICImageFromGalleryMaxSize defaultValue:4096 outValue:&_imageFromGalleryMaxSize error:error]) {
				return nil;
			}
			if(![icSettings rtr_parseFloat:RTRICAspectRatioMin defaultValue:0 outValue:&_aspectRatioMin error:error]) {
				return nil;
			}
			if(![icSettings rtr_parseFloat:RTRICAspectRatioMax defaultValue:0 outValue:&_aspectRatioMax error:error]) {
				return nil;
			}
			if(![icSettings rtr_parseDocumentSize:RTRICDocumentSizeKey defaultValue:AUIDocumentSizeAny outValue:&_documentSize error:error]) {
				return nil;
			}
		} else {
			_minimumDocumentToViewRatio = 0.15;
			_imageFromGalleryMaxSize = 4096;
			_documentSize = AUIDocumentSizeAny;
			_aspectRatioMin = 0;
			_aspectRatioMax = 0;
		}
	}
	return self;
}

@end
