// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRMultiImagePreviewController.h"
#import "RTRSingleImageViewController.h"
#import "NSString+RTRPluginLocalization.h"

@interface RTRMultiImagePreviewController () <UIPageViewControllerDelegate, UIPageViewControllerDataSource>

@property (nonatomic, strong) UIPageViewController* pages;
@property (weak, nonatomic) IBOutlet UIView *pagesContainer;
@property (nonatomic, weak) IBOutlet UILabel* pagesLabel;

@end

@implementation RTRMultiImagePreviewController

+ (instancetype)new
{
	UIStoryboard* sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
	return [sb instantiateViewControllerWithIdentifier:@"RTRMultiImagePreviewController"];
}

- (void)viewDidLoad {
	[super viewDidLoad];
	
	[self setupPagesPreview];
	[self setupNavigationBar];
	[self updatePagesLabel];
}

- (void)setupNavigationBar
{
	self.navigationController.navigationBar.barStyle = UIBarStyleBlack;
	UIBarButtonItem* cancelButton = [[UIBarButtonItem alloc]
		initWithTitle:@"Delete".rtr_localized
		style:UIBarButtonItemStylePlain
		target:self
		action:@selector(didPressCancel:)];
	cancelButton.tintColor = [UIColor whiteColor];
	UIBarButtonItem* retakeButton = [[UIBarButtonItem alloc]
		initWithTitle:@"Retake".rtr_localized
		style:UIBarButtonItemStylePlain
		target:self
		action:@selector(didPressRetake:)];
	retakeButton.tintColor = [UIColor whiteColor];
	self.navigationItem.title = @"SinglePagePreviewTitle".rtr_localized;
	self.navigationItem.leftBarButtonItem = cancelButton;
	self.navigationItem.rightBarButtonItem = retakeButton;
}

- (void)setupPagesPreview
{
	self.pages = [[UIPageViewController alloc]
		initWithTransitionStyle:UIPageViewControllerTransitionStyleScroll
		navigationOrientation:UIPageViewControllerNavigationOrientationHorizontal
		options:nil];
	self.pages.delegate = self;
	self.pages.dataSource = self;
	[self.pages
		setViewControllers:@[[self controllerWithIndex:self.pageIndex]]
		direction:UIPageViewControllerNavigationDirectionForward
		animated:NO
		completion:nil];
	self.pages.view.frame = self.pagesContainer.bounds;
	[self addChildViewController:self.pages];
	[self.pagesContainer addSubview:self.pages.view];
	[self.pages didMoveToParentViewController:self];
}

- (RTRSingleImageViewController*)controllerWithIndex:(NSInteger)index
{
	UIImage* imageToPreview = [self.delegate previewViewController:self requestImageAtIndex:index];
	if(imageToPreview == nil) {
		return nil;
	}
	RTRSingleImageViewController* viewController = [RTRSingleImageViewController new];
	viewController.image = imageToPreview;
	viewController.index = index;
	return viewController;
}

- (RTRSingleImageViewController*)currentPageViewController
{
	return self.pages.viewControllers[0];
}

- (UIViewController*)pageViewController:(UIPageViewController*)pageViewController
	viewControllerAfterViewController:(UIViewController*)viewController
{
	if(self.pageIndex >= self.imagesCount - 1) {
		return nil;
	}
	UIViewController* cachedPage = [self controllerWithIndex:self.pageIndex + 1];
	return cachedPage;
}

- (UIViewController*)pageViewController:(UIPageViewController*)pageViewController
	viewControllerBeforeViewController:(UIViewController*)viewController
{
	if(self.pageIndex == 0) {
		return nil;
	}
	UIViewController* cachedPage = [self controllerWithIndex:self.pageIndex - 1];
	return cachedPage;
}

- (void)pageViewController:(UIPageViewController*)pageViewController
	didFinishAnimating:(BOOL)finished
	previousViewControllers:(NSArray<UIViewController*>*)previousViewControllers
	transitionCompleted:(BOOL)completed
{
	if(!completed) {
		return;
	}
	self.pageIndex = [self currentPageViewController].index;
	[self updatePagesLabel];
}

- (void)updatePagesLabel
{
	self.pagesLabel.text = [NSString
		stringWithFormat:@"Page of".rtr_localized,
			@(self.pageIndex + 1).intValue,
			@(self.imagesCount).intValue];
}

- (void)didPressCancel:(id)sender
{
	if([self.delegate respondsToSelector:@selector(previewControllerDidDelete:atIndex:)]) {
		[self.delegate previewControllerDidDelete:self atIndex:self.pageIndex];
	}
	if(self.imagesCount == 0) {
		[self dismissViewControllerAnimated:YES completion:nil];
		return;
	}
	UIViewController* pageToPresent = [self controllerWithIndex:self.pageIndex];
	__weak RTRMultiImagePreviewController* weakSelf = self;
	[self.pages
		setViewControllers:@[pageToPresent]
		direction:UIPageViewControllerNavigationDirectionForward
		animated:NO
	 completion:^(BOOL finished) {
		 if(finished) {
			 [weakSelf updatePagesLabel];
		 }
	 }];
}

- (void)didPressRetake:(id)sender
{
	if([self.delegate respondsToSelector:@selector(previewControllerDidRetake:atIndex:)]) {
		[self.delegate previewControllerDidRetake:self atIndex:self.pageIndex];
	}
}

- (IBAction)didPressAddButton:(UIButton*)sender
{
	if([self.delegate respondsToSelector:@selector(previewControllerDidAdd:)]) {
		[self.delegate previewControllerDidAdd:self];
	}
}

- (IBAction)didPressCloseButton:(UIButton*)sender
{
	if([self.delegate respondsToSelector:@selector(previewControllerDidDone:)]) {
		[self.delegate previewControllerDidDone:self];
	}
}

@end
