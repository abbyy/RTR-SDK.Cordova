// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRSingleImageViewController.h"
#import "RTRZoomImageView.h"

@interface RTRSingleImageViewController () <RTRZoomImageViewDelegate>

@property (nonatomic, strong) RTRZoomImageView* imageView;

@end

@implementation RTRSingleImageViewController

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	[self.view layoutIfNeeded];
	[self.imageView updateImage:self.image resetPosition:YES];
}

- (void)setImage:(UIImage*)image
{
	_image = image;
}

- (RTRZoomImageView*)imageView
{
	if(_imageView == nil) {
		_imageView = [RTRZoomImageView RTRZoomImageView];
		_imageView.delegate = self;
	}
	if(_imageView.superview == nil) {
		_imageView.frame = self.view.bounds;
		[self.view addSubview:_imageView];
	}
	return _imageView;
}

- (void)viewDidLayoutSubviews
{
	[super viewDidLayoutSubviews];
	[self.imageView updateImage:self.imageView.imageView.image resetPosition:YES];
}

- (void)RTRZoomImageView:(RTRZoomImageView*)imageView changedRotation:(CGFloat)angle
{	
}

@end
