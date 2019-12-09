/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIButton (rtr_RecognitionLanguages)

/// [English, French, X] -> @"En Fr X"
- (void)rtr_setTitleWithLanguages:(NSSet<NSString*>*)languages forState:(UIControlState)state;

@end

NS_ASSUME_NONNULL_END
