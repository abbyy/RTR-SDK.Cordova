/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRMultipageScenarioConfiguration.h"
#import "NSDictionary+RTRSettings.h"
#import "RTRPluginConstants.h"

@interface RTRMultipageScenarioConfiguration()

@property (nonatomic, strong) RTREngine* engine;

@end

@implementation RTRMultipageScenarioConfiguration
@synthesize isFlashlightButtonVisible = _isFlashlightButtonVisible;
@synthesize isCaptureButtonVisible = _isCaptureButtonVisible;
@synthesize shouldShowPreview = _shouldShowPreview;
@synthesize maxImagesCount = _maxImagesCount;
@synthesize destination = _destination;
@synthesize exportType = _exportType;
@synthesize compressionType = _compressionType;
@synthesize compressionLevel = _compressionLevel;
@synthesize minimumDocumentToViewRatio = _minimumDocumentToViewRatio;
@synthesize documentSize = _documentSize;
@synthesize cropEnabled = _cropEnabled;
@synthesize cameraResolution = _cameraResolution;
@synthesize supportedOrientations = _supportedOrientations;
@synthesize storage = _storage;

- (instancetype)initWithManager:(RTRManager*)manager args:(NSDictionary*)args
{
	self = [super init];
	if(self != nil) {
		_supportedOrientations = [args rtr_orientationMaskForKey:RTROrientationPolicy];
		_cameraResolution = [self cameraResolutionFromString:args[RTRICCameraResolutionKey]];
		_isFlashlightButtonVisible = args[RTRICFlashlightButtonVisibleKey] ? [args[RTRICFlashlightButtonVisibleKey] boolValue] : YES;
		_isCaptureButtonVisible = args[RTRICCaptureButtonVisibleKey] ? [args[RTRICCaptureButtonVisibleKey] boolValue] : NO;

		_shouldShowPreview = args[RTRICShowPreviewKey] ? [args[RTRICShowPreviewKey] boolValue] : YES;
		_maxImagesCount = args[RTRICImagesCountKey] ? [args[RTRICImagesCountKey] integerValue] : 0;
		
		_compressionType = [args rtr_exportCompressionTypeForKey:RTRICCompressionTypeKey];
		_compressionLevel = [args rtr_exportCompressionLevelForKey:RTRICCompressionLevelKey];
		_exportType = [args rtr_exportTypeForKey:RTRICExportTypeKey];
		_destination = [args rtr_destinationTypeForKey:RTRICDestinationKey];

		NSDictionary* icSettings = args[RTRICDefaultImageSettingsKey];

		_minimumDocumentToViewRatio = icSettings[RTRICMinimumDocumentToViewRatioKey] ? [icSettings[RTRICMinimumDocumentToViewRatioKey] floatValue] : 0.15;
		_documentSize = [self documentSizeFromString:icSettings[RTRICDocumentSizeKey]];
		_cropEnabled = icSettings[RTRICCropEnabledKey] ? [icSettings[RTRICCropEnabledKey] boolValue] : YES;
		
		self.engine = manager.engine;
	}
	return self;
}

#pragma mark - parsing

- (AUICameraResolution)cameraResolutionFromString:(NSString*)string
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"HD": @(AUICameraResolutionHD),
			@"FullHD": @(AUICameraResolutionFullHD),
			@"4K": @(AUICameraResolution4K),
		};
	}
	if([predefined valueForKey:string] != nil) {
		return (AUICameraResolution)[predefined[string] integerValue];
	}
	return AUICameraResolutionFullHD;
}

- (CGSize)documentSizeFromString:(NSString*)string
{
	static NSDictionary* predefined = nil;
	if(predefined == nil) {
		predefined = @{
			@"Any": @(AUIDocumentSizeAny),
			@"A4": @(AUIDocumentSizeA4),
			@"BusinessCard": @(AUIDocumentSizeBusinessCard),
			@"Letter": @(AUIDocumentSizeLetter),
		};
	}
	if(string.length == 0) {
		return AUIDocumentSizeAny;
	}
	if([predefined valueForKey:string] != nil) {
		return [predefined[string] CGSizeValue];
	} else {
		NSCharacterSet* separators = [NSCharacterSet characterSetWithCharactersInString:@"Xx/ "];
		NSArray<NSString*>* parts = [string componentsSeparatedByCharactersInSet:separators];
		float nom = 0;
		float denom = 0;
		BOOL badValue = NO;
		if(parts.count == 2) {
			badValue = ![[NSScanner scannerWithString:parts.firstObject] scanFloat:&nom];//parts.firstObject.floatValue;
			if(!badValue) {
				badValue &= ![[NSScanner scannerWithString:parts.lastObject] scanFloat:&denom];
			}
		} else {
			badValue = YES;
		}
		if(!badValue) {
			return CGSizeMake(nom, denom);
		} else {
			return AUIDocumentSizeAny;
		}
	}
}

#pragma mark - ui config

- (BOOL)isFlashlightButtonVisible
{
	return [UIImagePickerController isFlashAvailableForCameraDevice:UIImagePickerControllerCameraDeviceRear] & _isFlashlightButtonVisible;
}

#pragma mark - export

- (void)exportResult:(void(^)(NSDictionary*))completion
{
	NSMutableDictionary* resultDictionary =
  @{
		@"images" : @[].mutableCopy,
		@"resultInfo": @{}.mutableCopy
	}.mutableCopy;
	for(NSDictionary* dict in self.storage.shouldShow) {
		NSMutableArray* images = resultDictionary[@"images"];
		NSMutableDictionary* imageInfo = dict.mutableCopy;
		if(self.storage.shouldShow.count == 1 /*&& self.destination == RTRImageCaptureDestintationDataUrl*/) {
			UIImage* image = [UIImage imageWithContentsOfFile:imageInfo[@"filePath"]];
			NSData* jpegData = UIImageJPEGRepresentation(image, 0.7);
			imageInfo[@"base64"] = [jpegData base64EncodedStringWithOptions:0];
		}
		[images addObject:imageInfo];
		
	}
	if(self.exportType == RTRImageCaptureEncodingTypePdf) {
		[self.storage generatePdfWithCompletion:^(NSString * _Nonnull path) {
			if(completion != nil) {
				resultDictionary[@"pdfInfo"] =
				@{
				  @"filePath": path,
				  @"pagesCount": @(self.storage.shouldShow.count),
				  @"compressionType": [NSDictionary rtr_exportCompressionTypeToString][@(self.compressionType)],
				  @"compressionLevel": [NSDictionary rtr_exportCompressionLevelToString][@(self.compressionLevel)]
				  };
				completion(resultDictionary);
			};
		}];
	} else {
		completion(resultDictionary);
	}
}

#pragma mark - files storage

- (RTRMultipageScenarioStorage*)storage
{
	if(_storage == nil) {
		_storage = [[RTRMultipageScenarioStorage alloc] initWithEngine:self.engine manager:self.documentManager];
	}
	return _storage;
}

- (RTRDocumentManager*)documentManager
{
	return [RTRDocumentManager defaultManagerWithImageContainer:self.imageContainer pdfContainer:self.pdfContainer];
}

- (RTRImageContainer*)imageContainer
{
	switch(self.exportType) {
		case RTRImageCaptureEncodingTypePng:
		case RTRImageCaptureEncodingTypeJpg: {
			RTRImageContainer* container = [[RTRJpgImageContainer alloc] initWithDirectory:NSTemporaryDirectory()];
			container.operationCustomization = ^(id<RTRCoreAPIExportToJpgOperation> _Nonnull op) {
				NSAssert([op conformsToProtocol:@protocol(RTRCoreAPIExportToJpgOperation)], @"unexpected");
				op.compression = self.compressionLevel;
			};
			return container;
		}
		case RTRImageCaptureEncodingTypeJpeg2000: {
			RTRImageContainer* container = [[RTRJpeg2000ImageContainer alloc] initWithDirectory:NSTemporaryDirectory()];
			container.operationCustomization = ^(id<RTRCoreAPIExportToJpeg2000Operation> _Nonnull op) {
				NSAssert([op conformsToProtocol:@protocol(RTRCoreAPIExportToJpeg2000Operation)], @"unexpected");
				op.compression = self.compressionLevel;
			};
			return container;
		}
		case RTRImageCaptureEncodingTypePdf:
			return [[RTRPngImageContainer alloc] initWithDirectory:NSTemporaryDirectory()];
		default:
			NSAssert(NO, @"Unknown export type");
	}
}

- (RTRPdfContainer*)pdfContainer
{
	RTRPdfContainer* container = [[RTRPdfContainer alloc] initWithDirectory:NSTemporaryDirectory()];
	container.operationCustomization = ^(id<RTRCoreAPIExportToPdfOperation> op) {
		NSAssert([op conformsToProtocol:@protocol(RTRCoreAPIExportToPdfOperation)], @"unexpected");
		op.compressionType = self.compressionType;
		op.compression = self.compressionLevel;
	};
	return container;
}

#pragma mark - image scenario

- (AUIImageCaptureScenario*)scenario
{
	AUIImageCaptureScenario* scenario = [[AUIImageCaptureScenario alloc] initWithEngine:self.engine];
	scenario.minimumDocumentToViewRatio = self.minimumDocumentToViewRatio;
	scenario.cropEnabled = self.cropEnabled;
	scenario.documentSize = self.documentSize;
	return scenario;
}

@end
