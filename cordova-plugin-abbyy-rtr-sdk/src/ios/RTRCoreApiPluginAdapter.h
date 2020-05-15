/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

NS_ASSUME_NONNULL_BEGIN

@interface RTRCoreApiPluginAdapter : NSObject

/// ABBYY Mobile Capture SDK Engine.
@property (nonatomic, strong, readonly) RTREngine* engine;

- (instancetype)initWithEngine:(RTREngine*)engine;

- (void)extractData:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)recognizeText:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)detectBoundaryOnImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)cropImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)rotateImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)assessOCRQualityOnImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)exportImage:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

- (void)exportImagesToPdf:(NSDictionary*)query
	onError:(void(^)(NSError*))onError
	onSuccess:(void(^)(NSDictionary*))onSuccess;

@end

NS_ASSUME_NONNULL_END
