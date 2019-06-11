// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRZoomImageViewBase.h"

@implementation RTRZoomImageViewBase

- (void)updateImage:(UIImage*)image
{
	if( image == nil ) {
		self.scrollView.alpha = 0;
		self.imageView.image = nil;
		return;
	}
	
	self.imageView.image = image;
	[self.imageView sizeToFit];
	
	CGSize imageSize = image.size;
	_imageView.bounds = CGRectMake(0, 0, imageSize.width, imageSize.height);
	
	[self setupZoom];
	self.scrollView.alpha = 1;
}

- (void)prepareForRotation
{
}

- (void)performRotation
{
	UIImage* image = _imageView.image;
	if( image == nil ) {
		return;
	}
	
	CGSize imageSize = image.size;
	_imageView.bounds = CGRectMake(0, 0, imageSize.width, imageSize.height);
	
	[self setupZoom];
}

#pragma mark - UIScrollViewDelegate

- (UIView*)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
	return self.imageView;
}

- (void)scrollViewDidZoom:(UIScrollView *)aScrollView
{
	CGFloat fittingWidth = CGRectGetWidth(self.scrollView.bounds);
	CGFloat offsetX = (fittingWidth > self.scrollView.contentSize.width)
	? (fittingWidth - self.scrollView.contentSize.width) * 0.5 : 0.0;
	
	CGFloat fittingHeight = CGRectGetHeight(_scrollView.bounds);
	CGFloat offsetY = (fittingHeight > self.scrollView.contentSize.height)
	? (fittingHeight - self.scrollView.contentSize.height) * 0.5 : 0.0;
	
	UIView* zoomedView = [self viewForZoomingInScrollView:aScrollView];
	zoomedView.center = CGPointMake(
		self.scrollView.contentSize.width * 0.5 + offsetX,
		self.scrollView.contentSize.height * 0.5 + offsetY);
	
	self.scrollView.scrollEnabled = self.scrollView.zoomScale > self.scrollView.minimumZoomScale;
}

static const CGFloat MaxZoomScaleForPdfPages = 5.f;

- (void)setupZoom
{
	CGSize imageSize = [self viewForZoomingInScrollView:_scrollView].bounds.size;
	
	self.scrollView.contentOffset = CGPointZero;
	self.scrollView.contentSize = imageSize;
	
	CGSize fittingSize = self.scrollView.bounds.size;
	self.scrollView.minimumZoomScale = MIN( fittingSize.width / imageSize.width, fittingSize.height / imageSize.height );
	self.scrollView.maximumZoomScale = MaxZoomScaleForPdfPages;
	
	self.scrollView.zoomScale = self.scrollView.minimumZoomScale;
	[self scrollViewDidZoom:self.scrollView];
}

@end
