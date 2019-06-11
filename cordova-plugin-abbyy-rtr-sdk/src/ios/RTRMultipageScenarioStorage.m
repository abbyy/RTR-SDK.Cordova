// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRMultipageScenarioStorage.h"
#import "RTRDocumentManager.h"

@interface RTRMultipageScenarioStorage ()

@property (nonatomic, strong) UIImage* memoryImage;
@property (nonatomic, assign) NSInteger currentImageIndex;
@property (nonatomic, assign) BOOL retake;
@property (nonatomic, strong) RTRDocumentManager* sessionDocumentManager;
@property (nonatomic, strong) NSMutableArray<NSString*>* shouldShowPaths;
@property (nonatomic, strong) NSMutableArray<NSString*>* shouldDeletePaths;
@property (nonatomic, strong) RTREngine* engine;

@end

@implementation RTRMultipageScenarioStorage

- (instancetype)initWithEngine:(RTREngine*)engine manager:(RTRDocumentManager*)manager
{
	self = [super init];
	if(self != nil) {
		_engine = engine;
		_shouldDeletePaths = @[].mutableCopy;
		_shouldShowPaths = @[].mutableCopy;
		_sessionDocumentManager = manager;
		_sessionDocumentManager.rtrEngine = engine;
	}
	return self;
}

- (RTRDocumentManager*)manager
{
	return self.sessionDocumentManager;
}

- (void)imageCaptured:(UIImage*)image
{
	self.memoryImage = image;
	if(!self.retake) {
		self.currentImageIndex = self.imagesCount - 1;
	}
}

- (void)deleteImageAt:(NSInteger)index
{
	[self confirmChanges];
	
	NSString* pathToDelete = self.shouldShowPaths[index];
	[self.shouldDeletePaths addObject:pathToDelete];
	[self.shouldShowPaths removeObjectAtIndex:index];
	self.currentImageIndex = MAX(0, MIN(self.imagesCount - 1, self.currentImageIndex));
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
		return self.memoryImage;
	}
	return [UIImage imageWithContentsOfFile:self.shouldShowPaths[index]];
}

- (NSInteger)imagesCount
{
	if(self.memoryImage == nil) {
		return self.shouldShowPaths.count;
	}
	return self.shouldShowPaths.count + (self.retake ? 0 : 1);
}

- (void)confirmChanges
{
	if(self.memoryImage == nil) {
		return;
	}
	NSString* path = [self.sessionDocumentManager saveImage:self.memoryImage];
	if(self.retake) {
		[self.shouldShowPaths replaceObjectAtIndex:self.currentImageIndex withObject:path];
	} else {
		[self.shouldShowPaths addObject:path];
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
	for(NSString* path in self.shouldDeletePaths) {
		[self.sessionDocumentManager removeFileAt:path];
	}
}

@end
