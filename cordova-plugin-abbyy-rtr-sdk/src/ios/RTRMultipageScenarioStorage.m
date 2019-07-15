// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRMultipageScenarioStorage.h"
#import "RTRDocumentManager.h"
#import <AbbyyUI/AbbyyUI.h>

@interface RTRMultipageScenarioStorage ()

@property (nonatomic, assign) NSInteger retakeImageIndex;
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
	NSMutableArray* filepaths = @[].mutableCopy;
	for(NSDictionary* dict in self.shouldShow) {
		[filepaths addObject:dict[@"filePath"]];
	}
	[self.sessionDocumentManager generatePdfWithFiles:filepaths completion:completion];
}

- (void)imageCaptured:(AUIImageCaptureResult*)result forceCaptured:(BOOL)force
{
	NSDictionary* imageInfo = [self saveImageResult:result forceCaptured:force];
	if(self.retake) {
		[self.shouldDelete addObject:self.shouldShow[self.retakeImageIndex]];
		[self.shouldShow replaceObjectAtIndex:self.retakeImageIndex withObject:imageInfo];
	} else {
		[self.shouldShow addObject:imageInfo];
	}
}

- (NSMutableDictionary*)saveImageResult:(AUIImageCaptureResult*)result forceCaptured:(BOOL)force
{
	NSMutableDictionary* info = @{}.mutableCopy;
	NSMutableDictionary* additionalInfo = @{}.mutableCopy;
	NSString* path = [self.sessionDocumentManager saveImage:result.image];
	[self.imageCache setObject:result.image forKey:path];
	info[@"filePath"] = path;
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
	self.retake = NO;
	NSDictionary* pathToDelete = self.shouldShow[index];
	[self.shouldDelete addObject:pathToDelete];
	[self.shouldShow removeObjectAtIndex:index];
}

- (NSInteger)currentImageIndex
{
	return self.retake ? self.retakeImageIndex : self.shouldShow.count - 1;
}

- (void)retakeImageAt:(NSInteger)index
{
	self.retake = YES;
	self.retakeImageIndex = index;
}

- (UIImage*)imageForIndex:(NSInteger)index
{
	NSString* imagePath = self.shouldShow[index][@"filePath"];
	UIImage* image = [self.imageCache objectForKey:imagePath];

	return image ?: [UIImage imageWithContentsOfFile:imagePath];
}

- (NSInteger)imagesCount
{
	return self.shouldShow.count;
}

- (void)confirmChanges
{
	self.retake = NO;
}

- (void)sessionCancelled
{
	[self.sessionDocumentManager removeAllFiles];
}

- (void)sessionClosedSuccessfully
{
	for(NSDictionary* path in self.shouldDelete) {
		[self.sessionDocumentManager removeFileAt:path[@"filePath"]];
	}
}

@end
