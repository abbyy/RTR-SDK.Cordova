// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>
#import "NSString+RTRPluginLocalization.h"

NS_ASSUME_NONNULL_BEGIN

@class RTRMultiImagePreviewController;

@protocol PreviewViewControllerDelegate <NSObject>

/// Image preview request, nil if index doesnt exist
- (nullable UIImage*)previewViewController:(RTRMultiImagePreviewController*)viewController requestImageAtIndex:(NSInteger)index;
/// Called when user wants to retake one of captured documents
- (void)previewControllerDidRetake:(RTRMultiImagePreviewController*)viewController atIndex:(NSInteger)index;
/// Called when user wants to add more documents
- (void)previewControllerDidAdd:(RTRMultiImagePreviewController*)viewController;
/// Called when user completes documents capture
- (void)previewControllerDidDone:(RTRMultiImagePreviewController*)viewController;
/// Called when user deleted one of captured documents
- (void)previewControllerDidDelete:(RTRMultiImagePreviewController*)viewController atIndex:(NSInteger)index;

@end

@interface UIButton (Localization)
@property (nonatomic, strong) NSString* referenceText;
@end
@implementation UIButton (Localization)
- (NSString*)referenceText
{
	return self.titleLabel.text;
}
- (void)setReferenceText:(NSString*)referenceText
{
	[self setTitle:referenceText.rtr_localized forState:UIControlStateNormal];
}
@end

/// Captured document preview controller
@interface RTRMultiImagePreviewController : UIViewController

@property (nonatomic, weak) id<PreviewViewControllerDelegate> delegate;
@property (nonatomic, assign) NSInteger pageIndex;
@property (nonatomic, assign) NSInteger imagesCount;

@end

NS_ASSUME_NONNULL_END
