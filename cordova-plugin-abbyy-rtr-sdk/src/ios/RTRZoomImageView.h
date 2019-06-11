// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRZoomImageViewBase.h"

@protocol RTRZoomImageViewDelegate;

@interface RTRZoomImageView : RTRZoomImageViewBase <UIScrollViewDelegate, UIGestureRecognizerDelegate>

@property(nonatomic, weak) id<RTRZoomImageViewDelegate> delegate;

+(RTRZoomImageView*)RTRZoomImageView;
-(void)updateImage:(UIImage*)image resetPosition:(BOOL)resetPosition;

@end

// ---------------------------------------------------------------------------------------------------------------------

@protocol RTRZoomImageViewDelegate

-(void)RTRZoomImageView:(RTRZoomImageView*)imageView changedRotation:(CGFloat)angle;

@end
