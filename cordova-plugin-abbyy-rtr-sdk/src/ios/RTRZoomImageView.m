// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRZoomImageView.h"

@implementation RTRZoomImageView {
	CGFloat _pinnedRotationValue;
	BOOL _zoomBlocked;
}

+(RTRZoomImageView*)RTRZoomImageView
{
	return [[[NSBundle mainBundle] loadNibNamed:@"RTRZoomImageView" owner:self options:nil] lastObject];
}

-(void)awakeFromNib
{
	[super awakeFromNib];
	
	self.scrollView.delegate = self;
	self.scrollView.decelerationRate = UIScrollViewDecelerationRateFast;
	//self.scrollView.bouncesZoom = NO;
	//self.scrollView.bounces = NO;
	
	UITapGestureRecognizer* tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onDoubleTap:)];
	tapRecognizer.delegate = self;
	tapRecognizer.numberOfTapsRequired = 2;
	[self addGestureRecognizer:tapRecognizer];
	
	UIRotationGestureRecognizer* rotationRecognizer = [[UIRotationGestureRecognizer alloc] initWithTarget:self action:@selector(onRotate:)];
	rotationRecognizer.delegate = self;
	[self.scrollView addGestureRecognizer:rotationRecognizer];
}

#pragma mark -
#pragma mark Public methods

-(void)updateImage:(UIImage*)image resetPosition:(BOOL)resetPosition
{
	self.imageView.image = image;
	
	if( !resetPosition ) {
		return;
	}
	
	[super updateImage:image];
}

#pragma mark -
#pragma mark Actions

-(void)onDoubleTap:(UIGestureRecognizer*)recognizer
{
	BOOL zoomOut = self.scrollView.zoomScale > self.scrollView.minimumZoomScale;
	
	CGFloat zoomScale = zoomOut ? self.scrollView.minimumZoomScale : self.scrollView.maximumZoomScale;
	[self.scrollView setZoomScale:zoomScale animated:YES];
}

-(void)onRotate:(UIRotationGestureRecognizer*)gestureRecognizer
{
	UIRotationGestureRecognizer* rotationRecognizer = (UIRotationGestureRecognizer*)gestureRecognizer;
	
	if( rotationRecognizer.state == UIGestureRecognizerStateBegan ) {
		self.scrollView.zoomScale = self.scrollView.minimumZoomScale;
		_zoomBlocked = YES;
	}
	
	_pinnedRotationValue = (int)roundf(rotationRecognizer.rotation / M_PI_2);
	CGFloat currentAngleValue = (rotationRecognizer.state == UIGestureRecognizerStateEnded
		? _pinnedRotationValue * M_PI_2
		: rotationRecognizer.rotation);
	
	self.imageView.transform = CGAffineTransformMakeScale(self.scrollView.minimumZoomScale, self.scrollView.minimumZoomScale);
	self.imageView.transform = CGAffineTransformRotate(self.imageView.transform, currentAngleValue);
	
	if (rotationRecognizer.state == UIGestureRecognizerStateEnded
		|| rotationRecognizer.state == UIGestureRecognizerStateCancelled)
	{
		_zoomBlocked = NO;
		self.imageView.layer.transform = CATransform3DMakeRotation(0, 0, 0, 1.0);
		[_delegate RTRZoomImageView:self changedRotation:currentAngleValue];
	}
}

#pragma mark -
#pragma mark UIScrollViewDelegate

-(UIView*)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
	return _zoomBlocked ? nil : self.imageView;
}

@end
