// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// Capture preview button with badge count
@interface RTRThumbnailButton : UIControl

@property (nonatomic, assign) NSInteger badgeNumber;
@property (nonatomic, strong) UIImage* image;

@end

NS_ASSUME_NONNULL_END
