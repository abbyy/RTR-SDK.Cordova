// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRImageCaptureViewController.h"
#import "NSString+RTRPluginLocalization.h"
#import <AbbyyUI/AbbyyUI.h>

#pragma mark - ViewController

@implementation RTRImageCaptureViewController

- (void)viewDidLoad
{
	[super viewDidLoad];

	self.cameraSettings.preferredResolution = self.config.cameraResolution;
	self.captureScenario = self.config.scenario;
	self.captureScenario.delegate = self;
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	self.flashButton.hidden = !self.config.isFlashlightButtonVisible;
	self.captureButton.hidden = !self.config.isCaptureButtonVisible;
	self.galleryButton.hidden = !self.config.isGalleryButtonVisible;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
	return self.config.supportedOrientations;
}

#pragma mark - Abbyy Mobile UI

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	onCloseWithResult:(id<AUIMultiPageImageCaptureResult>)result
{
	NSError* error;
	NSArray<AUIPageId>* ids = [result pagesWithError:&error];
	if(ids == nil) {
		self.onError(error);
		return;
	}
	if(ids.count > 0) {
		UIAlertController* confirmation = [UIAlertController
			alertControllerWithTitle:@"DectructiveActionWarning".rtr_localized
			message:@"AllPagesOnCurrentSessionWillBeDeletedWarning".rtr_localized
			preferredStyle:UIAlertControllerStyleAlert];

		UIAlertAction* cancel = [UIAlertAction actionWithTitle:@"Cancel".rtr_localized
			style:UIAlertActionStyleCancel
			handler:^(UIAlertAction* action) {
				self.closeButton.enabled = YES;
				[self setPaused:NO animated:YES];
		}];
		UIAlertAction* ok = [UIAlertAction actionWithTitle:@"Confirm".rtr_localized style:UIAlertActionStyleDestructive handler:^(UIAlertAction* action) {
			NSError* error;
			if(![result clearWithError:&error]) {
				self.onError(error);
			} else {
				self.onCancel();
			}
		}];
		[confirmation addAction:ok];
		[confirmation addAction:cancel];
		[self presentViewController:confirmation animated:YES completion:nil];
	} else {
		self.onCancel();
	}
}

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	didFinishWithResult:(id<AUIMultiPageImageCaptureResult>)result
{
	[self.config exportResult:result withCompletion:^(NSDictionary* result, NSError* error) {
		if(result != nil) {
			self.onSuccess(YES, result);
		} else {
			self.onError(error);
		}
	}];
}

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	didFailWithError:(NSError*)error
	result:(id<AUIMultiPageImageCaptureResult>)result
{
	NSLog(@"Capture controller error: %@", error);
	self.onError(error);
}

- (void)captureScenario:(AUIImageCaptureScenario*)scenario didFailWithError:(nonnull NSError*)error
{
	NSLog(@"Capture controller error: %@", error);
	self.onError(error);
}

@end
