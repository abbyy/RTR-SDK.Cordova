/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRMultipageScenarioConfiguration.h"
#import "NSDictionary+RTRSettings.h"
#import "RTRPluginConstants.h"

@interface RTRMultipageScenarioConfiguration() <AUIMultiPageCaptureSettings>

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

- (instancetype)initWithManager:(RTRManager*)manager args:(NSDictionary*)args
{
	self = [super init];
	if(self != nil) {
		_supportedOrientations = [args rtr_orientationMaskForKey:RTROrientationPolicy];
		_cameraResolution = [self cameraResolutionFromString:args[RTRICCameraResolutionKey]];
		_isFlashlightButtonVisible = args[RTRICFlashlightButtonVisibleKey] ? [args[RTRICFlashlightButtonVisibleKey] boolValue] : YES;
		_isCaptureButtonVisible = args[RTRICCaptureButtonVisibleKey] ? [args[RTRICCaptureButtonVisibleKey] boolValue] : NO;

		_shouldShowPreview = args[RTRICShowPreviewKey] ? [args[RTRICShowPreviewKey] boolValue] : NO;
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
			badValue = ![[NSScanner scannerWithString:parts.firstObject] scanFloat:&nom];
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

#pragma mark - export

- (void)exportResult:(id<AUIMultiPageImageCaptureResult>)result withCompletion:(void(^)(NSDictionary*, NSError*))completion
{
	NSError* error;
	NSArray<AUIPageId>* ids = [result pagesWithError:&error];
	if(ids == nil) {
		completion(nil, error);
		return;
	}
	NSMutableDictionary* exportDict = @{}.mutableCopy;
	NSArray* images = [self exportImagesFrom:result error:&error];
	if(images == nil) {
		completion(nil, error);
		return;
	}
	exportDict[@"images"] = images;
	if(self.exportType == RTRImageCaptureEncodingTypePdf) {
		NSDictionary* pdfDict = [self exportAsPdf:result error:&error];
		if(pdfDict == nil) {
			completion(nil, error);
			return;
		}
		exportDict[@"pdfInfo"] = pdfDict;
	}
	completion(exportDict, nil);
}

- (NSString*)exportDirectory
{
	return NSTemporaryDirectory();
}

- (NSArray*)exportImagesFrom:(id<AUIMultiPageImageCaptureResult>)result error:(NSError**)error
{
	NSMutableArray* images = @[].mutableCopy;
	NSArray<AUIPageId>* ids = [result pagesWithError:error];
	if(ids == nil) {
		return nil;
	}
	if(self.destination == RTRImageCaptureDestintationDataUrl) {
		if(ids.count != 1) {
			NSLog(@"Warning: If more then one image are captured, base64 export option value will be ignored and the result will be saved to a file anyway");
		} else {
			return [self exportSingleImageBase64:result identifier:ids.firstObject error:error];
		}
	}
	for(AUIPageId identifier in ids) {
		UIImage* image = [result loadImageWithId:identifier error:error];
		if(image == nil) {
			return nil;
		}
		NSString* path = [[self exportDirectory] stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.jpg", identifier]];
		RTRFileOutputStream* stream = [[RTRFileOutputStream alloc] initWithFilePath:path];
		id<RTRCoreAPIExportOperation> export = [self exporterWithOutput:stream];
		if(![export addPageWithImage:image]) {
			if(error != nil) {
				*error = export.error;
			}
			return nil;
		}
		if(![export close]) {
			if(error != nil) {
				*error = export.error;
			}
			return nil;
		}
		NSArray<NSValue*>* boudnary = [result loadBoundaryWithId:identifier error:error];
		if(boudnary == nil) {
			return nil;
		}
		NSMutableString* boundaryString = @"".mutableCopy;
		for(NSValue* pointValue in boudnary) {
			CGPoint point = pointValue.CGPointValue;
			[boundaryString appendFormat:@"%@ %@ ", @((int)point.x), @((int)point.y)];
		}
		[images addObject:@{
			@"filePath": path,
			@"resultInfo": @{
				@"exportType": [NSDictionary rtr_exportTypeToString][@([self singleImageExportType])],
				@"boundary": boundaryString
			}
		}];
	}
	return images;
}

- (NSDictionary*)exportAsPdf:(id<AUIMultiPageImageCaptureResult>)result error:(NSError**)error
{
	NSArray<AUIPageId>* ids = [result pagesWithError:error];
	if(ids == nil) {
		return nil;
	}
	NSString* filepath = [[self exportDirectory] stringByAppendingPathComponent:@"somepdf.pdf"];
	RTRFileOutputStream* stream = [[RTRFileOutputStream alloc] initWithFilePath:filepath];
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	id<RTRCoreAPIExportToPdfOperation> op = [coreApi createExportToPdfOperation:stream];
	for(AUIPageId identifier in ids) {
		UIImage* image = [result loadImageWithId:identifier error:error];
		if(image == nil) {
			return nil;
		}
		if(![op addPageWithImage:image]) {
			if(error != nil) {
				*error = op.error;
			}
			return nil;
		}
	}
	if(![op close]) {
		if(error != nil) {
			*error = op.error;
		}
		return nil;
	}
	return @{
		@"filePath": filepath,
		@"pagesCount": @(ids.count),
		RTRICCompressionTypeKey: [NSDictionary rtr_exportCompressionTypeToString][@(self.compressionType)],
		RTRICCompressionLevelKey: [NSDictionary rtr_exportCompressionLevelToString][@(self.compressionLevel)]
	};
}

- (NSArray*)exportSingleImageBase64:(id<AUIMultiPageImageCaptureResult>)result identifier:(AUIPageId)identifier error:(NSError**)error
{
	UIImage* image = [result loadOriginalImageWithId:identifier error:error];
	if(image == nil) {
		return nil;
	}
	NSArray<NSValue*>* boudnary = [result loadBoundaryWithId:identifier error:error];
	if(boudnary == nil) {
		return nil;
	}
	NSMutableString* boundaryString = @"".mutableCopy;
	for(NSValue* pointValue in boudnary) {
		CGPoint point = pointValue.CGPointValue;
		[boundaryString appendFormat:@"%@ %@ ", @((int)point.x), @((int)point.y)];
	}
	return [self exportSingleImageBase64:image info:@{
		@"boundary": boundaryString,
		@"exportType": [NSDictionary rtr_exportTypeToString][@([self singleImageExportType])]
	} error:error];
}

- (NSArray*)exportSingleImageBase64:(UIImage*)image info:(NSDictionary*)info error:(NSError**)error
{
	RTRMemoryOutputStream* stream = [RTRMemoryOutputStream new];
	id<RTRCoreAPIExportOperation> exporter = [self exporterWithOutput:stream];
	[exporter addPageWithImage:image];
	if(![exporter close]) {
		if(error != nil) {
			*error = exporter.error;
		}
		return nil;
	}
	return @[@{
		@"base64": [stream.data base64EncodedStringWithOptions:0],
		@"resultInfo": info
	}];
}

- (BOOL)exportImage:(UIImage*)image stream:(id<RTROutputStream>)stream error:(NSError**)error
{
	id<RTRCoreAPIExportOperation> operation = [self exporterWithOutput:stream];
	[operation addPageWithImage:image];
	if(![operation close]) {
		if(error != nil) {
			*error = operation.error;
		}
		return NO;
	}
	return YES;
}

- (RTRImageCaptureEncodingType)singleImageExportType
{
	if(self.exportType == RTRImageCaptureEncodingTypePdf) {
		return self.compressionType == RTRCoreAPIPdfExportJpgCompression ? RTRImageCaptureEncodingTypeJpg : RTRImageCaptureEncodingTypeJpeg2000;
	}
	return self.exportType;
}

- (id<RTRCoreAPIExportOperation>)exporterWithOutput:(id<RTROutputStream>)stream
{
	return [self exporterWithEncoding:[self singleImageExportType] output:stream];
}

- (id<RTRCoreAPIExportOperation>)exporterWithEncoding:(RTRImageCaptureEncodingType)type output:(id<RTROutputStream>)stream
{
	id<RTRCoreAPI> coreApi = [self.engine createCoreAPI];
	switch(type) {
		case RTRImageCaptureEncodingTypeJpg: {
			id<RTRCoreAPIExportToJpgOperation> op = [coreApi createExportToJpgOperation:stream];
			op.compression = self.compressionLevel;
			return op;
		}
		case RTRImageCaptureEncodingTypeJpeg2000: {
			id<RTRCoreAPIExportToJpeg2000Operation> op = [coreApi createExportToJpeg2000Operation:stream];
			op.compression = self.compressionLevel;
			return op;
		}
		case RTRImageCaptureEncodingTypePng:
			return [coreApi createExportToPngOperation:stream];
		case RTRImageCaptureEncodingTypePdf:
			NSAssert(NO, @"Unexpected");
			return nil;
	}
}

#pragma mark - image scenario

- (AUIMultiPageImageCaptureScenario*)scenario
{
	NSString* documentsFolder = NSSearchPathForDirectoriesInDomains(
		NSLibraryDirectory,
		NSUserDomainMask,
		YES).firstObject;
	NSError* error;
	AUIMultiPageImageCaptureScenario* scenario = [[AUIMultiPageImageCaptureScenario alloc]
		initWithEngine:self.engine
		storagePath:documentsFolder
		error:&error];
	scenario.requiredPageCount = self.maxImagesCount;
	scenario.isShowPreviewEnabled = self.shouldShowPreview;
	[scenario.result clearWithError:&error];
	scenario.captureSettings = self;
	return scenario;
}

#pragma mark - AUIMultiPageCaptureSettings

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	onConfigureImageCaptureSettings:(id<AUIImageCaptureSettings>)settings
	forPageAtIndex:(NSUInteger)index
{
	settings.minimumDocumentToViewRatio = self.minimumDocumentToViewRatio;
	settings.documentSize = self.documentSize;
}

@end
