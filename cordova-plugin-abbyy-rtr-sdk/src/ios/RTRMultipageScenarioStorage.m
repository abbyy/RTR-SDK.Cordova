// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRMultipageScenarioStorage.h"
#import "RTRDocumentManager.h"
#import <AbbyyUI/AbbyyUI.h>

@interface RTRMultipageScenarioStorage ()

@property (nonatomic, strong) NSMutableDictionary* memoryImage;
@property (nonatomic, assign) NSInteger currentImageIndex;
@property (nonatomic, assign) BOOL retake;
@property (nonatomic, strong) RTRDocumentManager* sessionDocumentManager;
@property (nonatomic, strong) NSMutableArray<NSDictionary*>* shouldDelete;
@property (nonatomic, strong) NSCache* imageCache;
@property (nonatomic, strong) RTREngine* engine;

@end

@implementation RTRMultipageScenarioStorage

- (instancetype)initWithEngine:(RTREngine*)engine manager:(RTRDocumentManager*)manager
{
	self = [super init];
	if(self != nil) {
		_engine = engine;
		_shouldDelete = @[].mutableCopy;
		_shouldShow = @[].mutableCopy;
		_sessionDocumentManager = manager;
		_sessionDocumentManager.rtrEngine = engine;
		_imageCache = [NSCache new];
	}
	return self;
}

- (void)generatePdfWithCompletion:(void (^)(NSString * _Nonnull))completion
{
	[self.sessionDocumentManager generatePdfWithCompletion:completion];
}

- (void)imageCaptured:(AUIImageCaptureResult*)result forceCaptured:(BOOL)force
{
	self.memoryImage = [self imageResultDictFrom:result forceCaptured:force];
	if(!self.retake) {
		self.currentImageIndex = self.imagesCount - 1;
	}
}

- (NSMutableDictionary*)imageResultDictFrom:(AUIImageCaptureResult*)result forceCaptured:(BOOL)force
{
	NSMutableDictionary* info = @{}.mutableCopy;
	NSMutableDictionary* additionalInfo = @{}.mutableCopy;
	info[@"nativeImage"] = result.image;
	if(result.documentBoundary != nil) {
		NSMutableString* quadrangleString = @"".mutableCopy;
		for(NSValue* value in result.documentBoundary) {
			CGPoint point = value.CGPointValue;
			[quadrangleString appendFormat:@"%d %d ", (int)point.x, (int)point.y];
		}
		additionalInfo[@"documentBoundary"] = [quadrangleString substringToIndex:quadrangleString.length - 1];
		additionalInfo[@"frameSize"] = [NSString stringWithFormat:@"%@ %@", @(result.image.size.width), @(result.image.size.height)];
	}
	additionalInfo[@"cropped"] = @(result.documentBoundary == nil);
	additionalInfo[@"autoCaptured"] = @(!force);
	info[@"resultInfo"] = additionalInfo;
	return info;
}

- (void)deleteImageAt:(NSInteger)index
{
	[self confirmChanges];
	
	NSDictionary* pathToDelete = self.shouldShow[index];
	[self.shouldDelete addObject:pathToDelete];
	[self.shouldShow removeObjectAtIndex:index];
}

- (void)retakeImageAt:(NSInteger)index
{
	[self confirmChanges];
	
	self.retake = YES;
	self.currentImageIndex = index;
}

- (UIImage*)imageForIndex:(NSInteger)index
{
	if(self.memoryImage != nil && index == self.currentImageIndex) {
		return self.memoryImage[@"nativeImage"];
	}
	NSString* imagePath = self.shouldShow[index][@"filePath"];
	UIImage* image = [self.imageCache objectForKey:imagePath];

	return image ?: [UIImage imageWithContentsOfFile:imagePath];
}

- (NSInteger)imagesCount
{
	if(self.memoryImage == nil) {
		return self.shouldShow.count;
	}
	return self.shouldShow.count + (self.retake ? 0 : 1);
}

- (void)confirmChanges
{
	if(self.memoryImage == nil) {
		return;
	}
	NSString* path = [self.sessionDocumentManager saveImage:self.memoryImage[@"nativeImage"]];
	[self.imageCache setObject:self.memoryImage[@"nativeImage"] forKey:path];
	self.memoryImage[@"filePath"] = path;
	[self.memoryImage removeObjectForKey:@"nativeImage"];
	if(self.retake) {
		[self.shouldShow replaceObjectAtIndex:self.currentImageIndex withObject:self.memoryImage];
	} else {
		[self.shouldShow addObject:self.memoryImage];
	}
	self.memoryImage = nil;
	self.retake = NO;
}

- (void)sessionCancelled
{
	[self.sessionDocumentManager removeAllFiles];
}

- (void)sessionClosedSuccessfully
{
	[self confirmChanges];
	for(NSDictionary* path in self.shouldDelete) {
		[self.sessionDocumentManager removeFileAt:path[@"filePath"]];
	}
}

@end
