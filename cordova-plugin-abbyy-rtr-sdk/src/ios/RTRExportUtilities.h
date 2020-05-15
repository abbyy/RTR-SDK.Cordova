/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RTRExportUtilities : NSObject

+ (NSString*)mimeForFileExtension:(NSString*)extension;

+ (NSString*)uriPrefixForDestination:(NSString*)destination extension:(NSString*)extension;

+ (NSString*)generatePathWithExtension:(NSString*)extension;

+ (NSString*)exportDirectory;

@end

NS_ASSUME_NONNULL_END
