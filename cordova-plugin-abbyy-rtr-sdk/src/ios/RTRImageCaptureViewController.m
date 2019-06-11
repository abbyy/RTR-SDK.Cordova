// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRImageCaptureViewController.h"
#import "RTRThumbnailButton.h"
#import "RTRMultiImagePreviewController.h"
#import "RTRMultipageScenarioStorage.h"
#import "NSString+RTRPluginLocalization.h"
#import <AbbyyUI/AbbyyUI.h>

/// Capture controller overlay view to add preview button
@interface OverlayView : UIView
/// Preview button with badge count
@property (nonatomic, weak) IBOutlet RTRThumbnailButton* previewButton;

@end

@implementation OverlayView

@end

#pragma mark - ViewController

@interface RTRImageCaptureViewController () <AUIImageCaptureScenarioDelegate, PreviewViewControllerDelegate>
@end

@implementation RTRImageCaptureViewController

- (void)viewDidLoad
{
	[super viewDidLoad];
	
	[self.overlay.previewButton addTarget:self action:@selector(didPressPreview:) forControlEvents:UIControlEventTouchUpInside];
	self.overlay.hidden = YES;
	self.flashButton.hidden = !self.config.isFlashlightButtonVisible;
	self.captureButton.hidden = !self.config.isCaptureButtonVisible;

	self.captureScenario = self.config.scenario;
	self.captureScenario.delegate = self;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
	return self.config.supportedOrientations;
}

- (OverlayView*)overlay
{
	return (id)self.cameraOverlayView;
}

- (void)updateOverlay
{
	self.overlay.hidden = self.config.storage.imagesCount == 0;
	if(!self.overlay.isHidden) {
		self.overlay.previewButton.badgeNumber = self.config.storage.imagesCount;
		self.overlay.previewButton.image = [self.config.storage imageForIndex:self.config.storage.imagesCount - 1];
	}
}

- (void)didPressPreview:(id)sender
{
	[self.config.storage sessionClosedSuccessfully];
	[self.config exportResult:^(NSDictionary* dict) {
		self.onSuccess(@[dict]);
	}];
}

#pragma mark - Abbyy Mobile UI

- (void)captureScenarioDidCancel:(AUICaptureScenario*)scenario
{
	if(self.config.storage.imagesCount > 0) {
		UIAlertController* confirmation = [UIAlertController alertControllerWithTitle:@"DectructiveActionWarning".rtr_localized message:@"AllPagesOnCurrentSessionWillBeDeletedWarning".rtr_localized preferredStyle:UIAlertControllerStyleAlert];
		UIAlertAction* cancel = [UIAlertAction actionWithTitle:@"Cancel".rtr_localized style:UIAlertActionStyleCancel handler:^(UIAlertAction* action) {
			self.closeButton.enabled = YES;
			[self setPaused:NO animated:YES];
		}];
		UIAlertAction* ok = [UIAlertAction actionWithTitle:@"Confirm".rtr_localized style:UIAlertActionStyleDestructive handler:^(UIAlertAction* action) {
			[self dismissViewControllerAnimated:YES completion:^{
				dispatch_async(dispatch_get_main_queue(), ^{
					[self.config.storage sessionCancelled];
					self.onCancel();
				});
			}];
		}];
		[confirmation addAction:ok];
		[confirmation addAction:cancel];
		[self presentViewController:confirmation animated:YES completion:nil];
	} else {
		self.onCancel();
	}
}

- (void)captureScenario:(AUIImageCaptureScenario*)scenario didFailWithError:(nonnull NSError*)error
{
	NSLog(@"Capture controller error: %@", error);
	UIAlertController* alert = [UIAlertController
		alertControllerWithTitle:@"SomethingWentWrong".rtr_localized
		message:error.localizedDescription
		preferredStyle:UIAlertControllerStyleAlert];
	[alert addAction:[UIAlertAction
		actionWithTitle:@"Ok".rtr_localized
		style:UIAlertActionStyleCancel
		handler:nil]];
	[self presentViewController:alert animated:YES completion:^{
		[self setPaused:YES];
	}];
}

- (void)captureScenario:(AUIImageCaptureScenario*)captureScenario didCaptureImageWithResult:(AUIImageCaptureResult*)result
{
	NSLog(@"did capture result %@", result);
	[self.config.storage imageCaptured:result.image];

	if(self.config.shouldShowPreview) {
		RTRMultiImagePreviewController* viewController = [RTRMultiImagePreviewController new];
		viewController.pageIndex = self.config.storage.currentImageIndex;
		viewController.imagesCount = self.config.storage.imagesCount;
		viewController.delegate = self;
		UINavigationController* navigationController = [[UINavigationController alloc] initWithRootViewController:viewController];
		[self presentViewController:navigationController animated:YES completion:nil];
	} else {
		[self.config.storage confirmChanges];
		[self updateOverlay];
		if(!self.config.shouldShowPreview && self.config.maxImagesCount == self.config.storage.imagesCount) {
			[self.config exportResult:^(NSDictionary* dict) {
				self.onSuccess(@[dict]);
			}];
		} else {
			captureScenario.active = YES;
		}
	}
}

#pragma mark - Preview Controller

- (UIImage*)previewViewController:(RTRMultiImagePreviewController *)viewController requestImageAtIndex:(NSInteger)index
{
	return [self.config.storage imageForIndex:index];
}

- (void)previewControllerDidAdd:(RTRMultiImagePreviewController*)viewController
{
	[self.config.storage confirmChanges];
	[self updateOverlay];
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void)previewControllerDidDone:(RTRMultiImagePreviewController*)viewController
{
	[self.config.storage sessionClosedSuccessfully];
	[self.config exportResult:^(NSDictionary* dict) {
		self.onSuccess(@[dict]);
	}];
}

- (void)previewControllerDidDelete:(RTRMultiImagePreviewController*)viewController atIndex:(NSInteger)index
{
	[self.config.storage deleteImageAt:index];
	viewController.pageIndex = self.config.storage.currentImageIndex;
	viewController.imagesCount = self.config.storage.imagesCount;
	[self updateOverlay];
}

- (void)previewControllerDidRetake:(RTRMultiImagePreviewController*)viewController atIndex:(NSInteger)index
{
	[self.config.storage retakeImageAt:index];
	[self updateOverlay];
	[self dismissViewControllerAnimated:YES completion:nil];
}

@end
