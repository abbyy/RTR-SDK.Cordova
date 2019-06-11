// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>

@interface RTRZoomImageViewBase : UIView

@property(nonatomic, strong) IBOutlet UIScrollView* scrollView;
@property(nonatomic, strong) IBOutlet UIImageView* imageView;

- (void)updateImage:(UIImage*)image;
- (void)prepareForRotation;
- (void)performRotation;
- (void)setupZoom;

@end
