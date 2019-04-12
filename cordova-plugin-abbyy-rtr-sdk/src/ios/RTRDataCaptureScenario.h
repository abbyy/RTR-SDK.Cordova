/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <Foundation/Foundation.h>

@interface RTRDataCaptureScenario : NSObject

@property (nonatomic) NSString* name;
@property (nonatomic) NSString* regEx;
@property (nonatomic) NSSet<NSString*>* languages;

+ (instancetype)dataCaptureScenarioWithName:(NSString*)name regEx:(NSString*)regEx
	languages:(NSSet<NSString*>*)languages description:(NSString*)description;

@end
