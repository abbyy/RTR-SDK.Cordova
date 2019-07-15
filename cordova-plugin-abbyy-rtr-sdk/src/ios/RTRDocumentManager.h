// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^PdfCompletion)(NSString* path);
@class RTRImageContainer;
@class RTRPdfContainer;

/// Utility class for working with captured documents
@interface RTRDocumentManager : NSObject

@property (nonatomic, strong) RTREngine* rtrEngine;

/// Create new manager instance
+ (instancetype)defaultManagerWithImageContainer:(RTRImageContainer*)imageContainer pdfContainer:(RTRPdfContainer*)pdfContainer;

/// Find all previously saved images. Returns images paths array
- (NSArray<NSString*>*)imagePaths;
/// Store image to file system
- (NSString*)saveImage:(UIImage*)image;
/// Copy files. Returns new path array
- (NSArray<NSString*>*)saveImageFiles:(NSArray<NSString*>*)pathArray;
/// Create PDF document from saved images
- (void)generatePdfWithCompletion:(PdfCompletion)completion;
/// Create PDF with external filepaths
- (void)generatePdfWithFiles:(NSArray*)files completion:(PdfCompletion)completion;
/// Remove previously saved files
- (void)removeAllFiles;
/// Remove specified file
- (void)removeFileAt:(NSString*)path;

@end

/// Base functionality for files containers with various types
@interface RTRFileContainer : NSObject

@property (nonatomic, strong) RTREngine* engine;
@property (nonatomic, copy, nullable) void(^operationCustomization)(id<RTRCoreAPIExportOperation>);

/// Returns root directory path
@property (nonatomic, strong) NSString* directory;
/// File Name of controlled files
@property (nonatomic, readonly) NSString* filename;
/// Creates container with custom storage path
- (instancetype)initWithDirectory:(NSString*)directory;
/// Add images to container
- (NSString*)addImages:(NSArray<UIImage*>*)images;
/// Add image to container
- (NSString*)addImage:(UIImage*)image;
/// Remove all files from container
- (void)clear;

@end

@interface RTRImageContainer : RTRFileContainer
/// Saved images
@property (nonatomic, readonly) NSArray<NSString*>* imagePaths;
@end

@interface RTRPdfContainer : RTRFileContainer
- (NSString*)generatePdfFrom:(NSArray<UIImage*>*)images;
@end

@interface RTRJpgImageContainer : RTRImageContainer
@end

@interface RTRJpeg2000ImageContainer : RTRImageContainer
@end

@interface RTRPngImageContainer : RTRImageContainer
@end

NS_ASSUME_NONNULL_END
