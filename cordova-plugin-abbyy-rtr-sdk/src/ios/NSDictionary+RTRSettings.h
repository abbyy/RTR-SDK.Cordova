/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <AbbyyUI/AbbyyUI.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>
#import "RTRPluginConstants.h"

NS_ASSUME_NONNULL_BEGIN

@interface NSDictionary (rtr_Mapping)

// enum mappings
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToCameraResolution;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_cameraResolutionToString;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_orientationMaskToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToOrientationMask;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_exportCompressionLevelToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToExportCompressionLevel;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_exportCompressionTypeToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToExportCompressionType;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_auiCameraResolutionToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToauiCameraResolution;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_detectionModeToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToDetectionMode;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_exportTypeToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToExportType;

+ (NSDictionary<NSNumber*, NSString*>*)rtr_destinationTypeToString;
+ (NSDictionary<NSString*, NSNumber*>*)rtr_stringToDestinationType;

// key parsing
- (BOOL)rtr_parseInteger:(NSString*)key defaultValue:(NSInteger)defaultValue outValue:(NSInteger*)outValue error:(NSError**)error;
- (BOOL)rtr_parseBool:(NSString*)key defaultValue:(BOOL)defaultValue outValue:(BOOL*)outValue error:(NSError**)error;
- (BOOL)rtr_parseFloat:(NSString*)key defaultValue:(CGFloat)defaultValue outValue:(CGFloat*)outValue error:(NSError**)error;
- (BOOL)rtr_parseEnum:(NSString*)key defaultValue:(NSInteger)defaultValue variants:(NSDictionary<NSString*, NSNumber*>*)variants outValue:(NSInteger*)outValue error:(NSError**)error;
- (BOOL)rtr_parseDocumentSize:(NSString*)key defaultValue:(AUIDocumentSize)defaultValue outValue:(AUIDocumentSize*)outValue error:(NSError**)error;

// ctors
+ (instancetype)rtr_dictionaryFromRect:(CGRect)areaOfInterest;
+ (instancetype)rtr_dictionaryFromPoint:(CGPoint)point;
+ (instancetype)rtr_imageSizeDictionaryFromSize:(CGSize)size;
+ (instancetype)rtr_resolutionDictionaryFromSize:(CGSize)size;
+ (instancetype)rtr_dictionaryFromSize:(CGSize)size;
+ (instancetype)rtr_dictionaryFromDataField:(RTRDataField*)dataField;
+ (instancetype)rtr_dictionaryFromTextBlock:(RTRTextBlock*)textBlock;
+ (instancetype)rtr_dictionaryFromTextLine:(RTRTextLine*)textLine;
+ (instancetype)rtr_dictionaryWithCharInfo:(RTRCharInfo*)charInfo;

- (BOOL)rtr_asSize:(CGSize*)size error:(NSError**)error;
- (BOOL)rtr_asRect:(CGRect*)outRect error:(NSError**)error;

@end

@interface NSString (rtr_Mapping)

+ (instancetype)rtr_stringFromStabilityStatus:(RTRResultStabilityStatus)status;
+ (instancetype)rtr_stringFromWarningCode:(RTRCallbackWarningCode)warningCode;
+ (instancetype)rtr_stringFromOcrQualityBlockType:(RTRQualityAssessmentForOCRBlockType)blockType;

@end

@interface NSArray (rtr_Mapping)

- (nullable NSArray*)rtr_tryMap:(nullable id(^)(id, NSError**))transform error:(NSError**)error;
- (NSArray*)rtr_map:(id(^)(id))transform;

/// mapping from @{x:y:} NSDictionary to NSValues with elements count check. required count: 4
- (NSArray*)rtr_transformToNSValuesQuadrangle:(NSError**)error;

/// mapping from NSValues to @{x:y:} NSDictionary with elements count check. required count: 4
- (NSArray*)rtr_transformToDictionaryQuadrangle:(NSError**)error;

@end

@interface RTRTextBlock (rtr_Text)

@property (nonatomic, readonly) NSString* rtr_text;

@end

NS_ASSUME_NONNULL_END
