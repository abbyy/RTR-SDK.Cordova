// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRDocumentManager.h"

@implementation RTRFileContainer

- (instancetype)initWithDirectory:(NSString*)directory
{
	self = [super init];
	if(self != nil) {
		self.directory = directory;
	}
	return self;
}

- (NSString*)addImages:(NSArray<UIImage*>*)images
{
	if(images.count == 0) {
		return nil;
	}
	// Generate container filename from current time
	NSString* path = [self.directory stringByAppendingPathComponent:self.filename];
	// Create export operation instance with file output stream
	RTRFileOutputStream* stream = [[RTRFileOutputStream alloc] initWithFilePath:path];
	id<RTRCoreAPIExportOperation> operation = [self exportOperation:stream];
	// Add all images to container
	for(UIImage* image in images) {
		[operation addPageWithImage:image];
	}
	// operation has to be closed
	return [operation close] ? path : nil;
}

- (NSString*)addImage:(UIImage*)image
{
	return [self addImages:@[image]];
}

- (void)clear
{
	// Remove all files at root directory
	[[NSFileManager defaultManager] removeItemAtPath:self.directory error:nil];
}

- (NSString*)directory
{
	if(_directory != nil) {
		return _directory;
	}
	// Temp folder
	return NSTemporaryDirectory();
}

- (NSString*)filename
{
	// override
	return nil;
}

- (id<RTRCoreAPIExportOperation>)exportOperation:(RTRFileOutputStream*)stream
{
	NSAssert(NO, nil);
	return nil;
}

@end

#pragma mark -
//----------------------------------------------------------

@implementation RTRImageContainer

- (NSString*)addImages:(NSArray<UIImage*>*)images
{
	if(images.count == 1) {
		return [super addImages:images];
	}
	for(UIImage* image in images) {
		[super addImages:@[image]];
	}
	return self.directory;
}

- (id<RTRCoreAPIExportOperation>)exportOperation:(RTRFileOutputStream*)stream
{
	NSAssert(NO, @"Override");
	return nil;
}

- (NSString*)fileExtension
{
	NSAssert(NO, @"Override");
	return nil;
}

- (NSString*)filename
{
	return [NSString stringWithFormat:@"%ld.%@", (NSInteger)([NSDate date].timeIntervalSince1970 * 1000.0), self.fileExtension];
}

- (NSArray<NSString*>*)imagePaths
{
	NSError* err;
	NSFileManager* fileManager = [NSFileManager defaultManager];
	// Get all files in root directory
	NSArray* files = [fileManager contentsOfDirectoryAtPath:self.directory error:&err];
	// Exclude extra
	NSString* format = [NSString stringWithFormat:@"self ENDSWITH '.%@'", self.fileExtension];
	files = [files filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:format]];
	// Sort by name (the same as by creation date in this case)
	files = [files sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
	NSMutableArray* paths = [NSMutableArray arrayWithCapacity:files.count];
	for(NSString* file in files) {
		NSString* path = [self.directory stringByAppendingPathComponent:file];
		[paths addObject:path];
	}
	return paths;
}

@end

//----------------------------------------------------------
#pragma mark -

@implementation RTRJpgImageContainer

- (id<RTRCoreAPIExportOperation>)exportOperation:(RTRFileOutputStream*)stream
{
	id<RTRCoreAPI> coreAPI = [self.engine createCoreAPI];
	id<RTRCoreAPIExportOperation> op = [coreAPI createExportToJpgOperation:stream];
	if(self.operationCustomization != nil) {
		self.operationCustomization(op);
	}
	return op;
}

- (NSString*)fileExtension
{
	return @"jpg";
}

@end

//----------------------------------------------------------
#pragma mark -

@implementation RTRJpeg2000ImageContainer

- (id<RTRCoreAPIExportOperation>)exportOperation:(RTRFileOutputStream*)stream
{
	id<RTRCoreAPI> coreAPI = [self.engine createCoreAPI];
	id<RTRCoreAPIExportOperation> op = [coreAPI createExportToJpeg2000Operation:stream];
	if(self.operationCustomization != nil) {
		self.operationCustomization(op);
	}
	return op;
}

- (NSString*)fileExtension
{
	return @"jp2";
}

@end

//----------------------------------------------------------
#pragma mark -

@implementation RTRPngImageContainer

- (id<RTRCoreAPIExportOperation>)exportOperation:(RTRFileOutputStream*)stream
{
	id<RTRCoreAPI> coreAPI = [self.engine createCoreAPI];
	id<RTRCoreAPIExportOperation> op = [coreAPI createExportToPngOperation:stream];
	if(self.operationCustomization != nil) {
		self.operationCustomization(op);
	}
	return op;
}

- (NSString*)fileExtension
{
	return @"png";
}

@end

//----------------------------------------------------------
#pragma mark -

@implementation RTRPdfContainer

- (id<RTRCoreAPIExportOperation>)exportOperation:(RTRFileOutputStream*)stream
{
	id<RTRCoreAPI> coreAPI = [self.engine createCoreAPI];
	id<RTRCoreAPIExportToPdfOperation> op = [coreAPI createExportToPdfOperation:stream];
	if(self.operationCustomization != nil) {
		self.operationCustomization(op);
	}
	return op;
}

- (NSString*)filename
{
	NSDateFormatter* formatter = [NSDateFormatter new];
	[formatter setDateFormat:@"yyyy-MM-dd HH_mm_ss"];
	NSString* dateString = [formatter stringFromDate:[NSDate date]];
	return [NSString stringWithFormat:@"ImageCapture - %@.pdf", dateString];
}

- (NSString*)generatePdfFrom:(NSArray<UIImage*>*)images
{
	return [self addImages:images];
}

@end

//----------------------------------------------------------
#pragma mark -

@interface RTRDocumentManager ()

@property (nonatomic, strong) RTRImageContainer* imageContainer;
@property (nonatomic, strong) RTRPdfContainer* pdfContainer;

@end

@implementation RTRDocumentManager

+ (instancetype)defaultManagerWithImageContainer:(RTRImageContainer*)imageContainer pdfContainer:(RTRPdfContainer*)pdfContainer
{
	NSArray* tmpDirectory = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:NSTemporaryDirectory() error:NULL];
	for (NSString* file in tmpDirectory) {
		[[NSFileManager defaultManager] removeItemAtPath:[NSString stringWithFormat:@"%@%@", NSTemporaryDirectory(), file] error:NULL];
	}
	RTRDocumentManager* manager = [[self class] new];
	manager.imageContainer = imageContainer;
	manager.pdfContainer = pdfContainer;
	return manager;
}

- (RTRImageContainer*)imageContainer
{
	if(_imageContainer == nil) {
		_imageContainer = [RTRImageContainer new];
	}
	_imageContainer.engine = self.rtrEngine;
	return _imageContainer;
}

- (RTRPdfContainer*)pdfContainer
{
	if(_pdfContainer == nil) {
		_pdfContainer = [RTRPdfContainer new];
	}
	_pdfContainer.engine = self.rtrEngine;
	return _pdfContainer;
}

- (NSArray<NSString*>*)imagePaths
{
	return self.imageContainer.imagePaths;
}

- (NSString*)saveImage:(UIImage*)image
{
	return [self.imageContainer addImage:image];
}

- (NSArray<NSString*>*)saveImageFiles:(NSArray<NSString*>*)pathArray
{
	NSMutableArray* results = [NSMutableArray arrayWithCapacity:pathArray.count];
	for(NSString* filePath in pathArray) {
		NSError* error = nil;
		NSString* destination = [self.imageContainer.directory stringByAppendingPathComponent:filePath.lastPathComponent];
		if([[NSFileManager defaultManager] copyItemAtPath:filePath toPath:destination error:&error]) {
			[results addObject:destination];
		}
	}
	NSAssert(results.count == pathArray.count, @"Copy files failed");
	return results;
}

- (void)generatePdfWithCompletion:(PdfCompletion)completion
{
	dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^{
		NSArray* pathArray = self.imageContainer.imagePaths;
		NSMutableArray* images = [NSMutableArray arrayWithCapacity:pathArray.count];
		for(NSString* path in pathArray) {
			UIImage* image = [UIImage imageWithContentsOfFile:path];
			if(image != nil) {
				[images addObject:image];
			}
		}
		NSString* path = [self.pdfContainer generatePdfFrom:images];
		dispatch_async(dispatch_get_main_queue(), ^{
			if(completion != nil) {
				completion(path);
			}
		});
	});
}

- (void)removeAllFiles
{
	[self.imageContainer clear];
	[self.pdfContainer clear];
}

- (void)removeFileAt:(NSString*)path
{
	[[NSFileManager defaultManager] removeItemAtPath:path error:nil];
}

@end
