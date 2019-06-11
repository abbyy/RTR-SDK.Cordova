// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRThumbnailButton.h"

@interface RTRThumbnailButton ()

@property (nonatomic, strong) UIImageView* imageView;
@property (nonatomic, strong) UILabel* badgeLabel;

@end

@implementation RTRThumbnailButton

- (instancetype)initWithCoder:(NSCoder*)aDecoder
{
	self = [super initWithCoder:aDecoder];
	if(self != nil) {
		[self commonInit];
	}
	return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
	self = [super initWithFrame:frame];
	if(self != nil) {
		[self commonInit];
	}
	return self;
}

- (void)commonInit
{
	CGFloat offset = 3;
	CGFloat badgeSize = 27;
	CGFloat cornerRadius = 5;
	CGFloat borderWidth = 1;
	self.imageView = [[UIImageView alloc] initWithFrame:CGRectMake(offset, 0, self.bounds.size.width - offset, self.bounds.size.height)];
	
	self.imageView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
	self.imageView.contentMode = UIViewContentModeScaleAspectFill;
	self.imageView.layer.masksToBounds = YES;
	self.imageView.layer.cornerRadius = cornerRadius;
	self.imageView.layer.borderColor = UIColor.lightGrayColor.CGColor;
	self.imageView.layer.borderWidth = borderWidth;
	self.imageView.userInteractionEnabled = NO;
	
	UIView* badgeView = [[UIView alloc] initWithFrame:CGRectMake(-offset, self.imageView.bounds.size.height - badgeSize + offset, badgeSize, badgeSize)];
	
	badgeView.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin;
	badgeView.backgroundColor = [UIColor redColor];
	badgeView.layer.masksToBounds = YES;
	badgeView.layer.cornerRadius = cornerRadius;
	badgeView.userInteractionEnabled = NO;

	self.badgeLabel = [[UILabel alloc] initWithFrame:CGRectMake(2 * offset, 0, badgeView.bounds.size.width - 3 * offset, badgeView.bounds.size.height - offset)];
	
	self.badgeLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
	self.badgeLabel.textColor = [UIColor whiteColor];
	self.badgeLabel.adjustsFontSizeToFitWidth = YES;
	self.badgeLabel.textAlignment = NSTextAlignmentCenter;
	self.badgeLabel.text = @"0";

	[self addSubview:self.imageView];
	[self.imageView addSubview:badgeView];
	[badgeView addSubview:self.badgeLabel];
}

- (void)setImage:(UIImage*)image
{
	_image = image;
	self.imageView.image = image;
}

- (void)setBadgeNumber:(NSInteger)badgeNumber
{
	_badgeNumber = badgeNumber;
	self.badgeLabel.text = @(badgeNumber).stringValue;
}

- (BOOL)aui_canRotate
{
	return YES;
}

@end
