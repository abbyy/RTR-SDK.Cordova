// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRImageCaptureViewController.h"
#import "RTRExportUtilities.h"
#import <AbbyyUI/AbbyyUI.h>

#pragma mark - ViewController

@interface RTRImageCaptureViewController() <AUIMultiPageCaptureSettings, AUIMultiPageImageCaptureScenarioDelegate>

@property (nonatomic, strong) RTRImageCaptureSettings* settings;
@property (nonatomic, strong) id<RTRLocalizer> localizer;

@end

@implementation RTRImageCaptureViewController

- (instancetype)initWithEngine:(RTREngine*)engine
	localizer:(id<RTRLocalizer>)localizer
	settings:(RTRImageCaptureSettings*)settings
	error:(NSError**)error
{
	self = [super init];
	if(self != nil) {
		AUIMultiPageImageCaptureScenario* scenario = [[AUIMultiPageImageCaptureScenario alloc]
			initWithEngine:engine
			storagePath:[RTRExportUtilities exportDirectory]
			error:error];
		if(scenario == nil) {
			return nil;
		}
		_settings = settings;
		scenario.isShowPreviewEnabled = settings.shouldShowPreview;
		scenario.requiredPageCount = settings.requiredPageCount;
		if(![scenario.result clearWithError:error]) {
			return nil;
		}
		scenario.captureSettings = self;
		self.captureScenario = scenario;
		self.captureScenario.delegate = self;
		self.cameraSettings.preferredResolution = self.settings.cameraResolution;
	}
	return self;
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	self.flashButton.hidden = !self.settings.isFlashlightButtonVisible;
	self.captureButton.hidden = !self.settings.isCaptureButtonVisible;
	self.galleryButton.hidden = !self.settings.isGalleryButtonVisible;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
	return self.settings.supportedOrientations;
}

#pragma mark - Abbyy Mobile UI

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	onCloseWithResult:(id<AUIMultiPageImageCaptureResult>)result
{
	NSError* error;
	NSArray<AUIPageId>* ids = [result pagesWithError:&error];
	if(ids == nil) {
		self.onError(result, error);
		return;
	}
	if(ids.count > 0) {
		UIAlertController* confirmation = [UIAlertController
			alertControllerWithTitle:[self.localizer localizedStringForKey:@"DectructiveActionWarning"]
			message:[self.localizer localizedStringForKey:@"AllPagesOnCurrentSessionWillBeDeletedWarning"]
			preferredStyle:UIAlertControllerStyleAlert];

		UIAlertAction* cancel = [UIAlertAction actionWithTitle:[self.localizer localizedStringForKey:@"Cancel"]
			style:UIAlertActionStyleCancel
			handler:^(UIAlertAction* action) {
				self.closeButton.enabled = YES;
				[self setPaused:NO animated:YES];
		}];
		UIAlertAction* ok = [UIAlertAction
			actionWithTitle:[self.localizer localizedStringForKey:@"Confirm"]
			style:UIAlertActionStyleDestructive
			handler:^(UIAlertAction* action) {
			self.onCancel(result);
		}];
		[confirmation addAction:ok];
		[confirmation addAction:cancel];
		[self presentViewController:confirmation animated:YES completion:nil];
	} else {
		self.onCancel(result);
	}
}

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	didFinishWithResult:(id<AUIMultiPageImageCaptureResult>)result
{
	self.onSuccess(result);
}

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	didFailWithError:(NSError*)error
	result:(id<AUIMultiPageImageCaptureResult>)result
{
	self.onError(result, error);
}

- (void)captureScenario:(AUIMultiPageImageCaptureScenario*)captureScenario
	onConfigureImageCaptureSettings:(id<AUIImageCaptureSettings>)settings
	forPageAtIndex:(NSUInteger)index
{
	settings.minimumDocumentToViewRatio = self.settings.minimumDocumentToViewRatio;
	settings.documentSize = self.settings.documentSize;
	settings.imageFromGalleryMaxSize = self.settings.imageFromGalleryMaxSize;
	settings.aspectRatioMin = self.settings.aspectRatioMin;
	settings.aspectRatioMax = self.settings.aspectRatioMax;
}

@end
