/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRViewController.h"
#import <AVFoundation/AVFoundation.h>

/// Cell ID for languagesTableView.
static NSString* const RTRTableCellID = @"RTRTableCellID";
/// Name for text region layers.
static NSString* const RTRTextRegionLayerName = @"RTRTextRegionLayerName";

/// Shortcut. Perform block asynchronously on main thread.
void performBlockOnMainThread(NSInteger delay, void(^block)())
{
	dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delay * NSEC_PER_SEC)), dispatch_get_main_queue(), block);
}

@interface RTRViewController () <AVCaptureVideoDataOutputSampleBufferDelegate>

@property (nonatomic, weak) IBOutlet UIVisualEffectView* topBar;

@property (nonatomic) AVCaptureDevice* captureDevice;

@end

#pragma mark -

@implementation RTRViewController {
	/// Camera session.
	AVCaptureSession* _session;
	/// Video preview layer.
	AVCaptureVideoPreviewLayer* _previewLayer;
	/// Session Preset.
	NSString* _sessionPreset;

	/// Area of interest in view coordinates.
	CGRect _selectedArea;
}

#pragma mark - UIView LifeCycle

- (instancetype)init
{
	return [self initWithNibName:NSStringFromClass([RTRViewController class]) bundle:NSBundle.mainBundle];
}

- (void)viewDidLoad
{
	[super viewDidLoad];
	// Recommended session preset.
	_sessionPreset = AVCaptureSessionPreset1280x720;
	_imageBufferSize = CGSizeMake(720.f, 1280.f);

	[self.settingsTableView registerClass:[UITableViewCell class] forCellReuseIdentifier:RTRTableCellID];
	self.settingsTableView.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];

	[self prepareUIForRecognition];

	self.captureButton.hidden = !self.isStopButtonVisible;
	self.captureButton.selected = NO;

	self.flashButton.hidden = !self.isFlashlightVisible;
	if(!self.isFlashlightVisible && !self.isLanguageSelectionEnabled) {
		self.topBar.hidden = YES;
	}

	self.settingsTableView.hidden = YES;
}


- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	__weak RTRViewController* weakSelf = self;
	[self authorizeCameraDeviceWithCompletion:^(BOOL isGranted) {
		performBlockOnMainThread(0, ^{
			[weakSelf configureCompletionAccessGranted:isGranted];
		});
	}];

	_currentStabilityStatus = RTRResultStabilityNotReady;
}

- (void)viewWillTransitionToSize:(CGSize)size withTransitionCoordinator:(id<UIViewControllerTransitionCoordinator>)coordinator
{
	[super viewWillTransitionToSize:size withTransitionCoordinator:coordinator];

	BOOL wasRunning = self.isRunning;
	self.running = NO;
	[self.service stopTasks];
	[self clearScreenFromRegions];

	[coordinator animateAlongsideTransition:nil completion:^(id<UIViewControllerTransitionCoordinatorContext> context)
	{
		_imageBufferSize = CGSizeMake(MIN(_imageBufferSize.width, _imageBufferSize.height),
			MAX(_imageBufferSize.width, _imageBufferSize.height));
		if(UIInterfaceOrientationIsLandscape([UIApplication sharedApplication].statusBarOrientation)) {
			_imageBufferSize = CGSizeMake(_imageBufferSize.height, _imageBufferSize.width);
		 }

		[self updateAreaOfInterest];
		self.running = wasRunning;
	}];
}

- (void)authorizeCameraDeviceWithCompletion:(void (^)(BOOL isGranted))completion
{
	AVAuthorizationStatus status = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
	switch(status) {
		case AVAuthorizationStatusAuthorized:
			completion(YES);
			break;
		case AVAuthorizationStatusNotDetermined:
			[AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:completion];
			break;
		case AVAuthorizationStatusRestricted:
		case AVAuthorizationStatusDenied:
			completion(NO);
			break;
	}
}

- (void)configureCompletionAccessGranted:(BOOL)accessGranted
{
	NSString* error;
	if(![UIImagePickerController isCameraDeviceAvailable:UIImagePickerControllerCameraDeviceRear]) {
		error = @"Device has no camera";
	} else if(!accessGranted) {
		error = @"Camera access denied";
	}
	if(error != nil) {
		self.errorOccurred = error;
		[self updateLogMessage:error];
		self.captureButton.enabled = NO;
		self.captureButton.hidden = YES;
		return;
	}

	[self configureAVCaptureSession];
	[self configurePreviewLayer];
	[_session startRunning];
	[self capturePressed];

	[[NSNotificationCenter defaultCenter] addObserver:self selector: @selector(avSessionFailed:)
		name: AVCaptureSessionRuntimeErrorNotification object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector: @selector(applicationDidEnterBackground)
		name: UIApplicationDidEnterBackgroundNotification object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector: @selector(applicationWillEnterForeground)
		name: UIApplicationWillEnterForegroundNotification object:nil];
}

- (void)updateImageBufferSize
{
	CGFloat smallerSide = MIN(_imageBufferSize.width, _imageBufferSize.height);
	CGFloat biggerSide = MAX(_imageBufferSize.width, _imageBufferSize.height);
	if(UIInterfaceOrientationIsLandscape([UIApplication sharedApplication].statusBarOrientation)) {
		_imageBufferSize = CGSizeMake(biggerSide, smallerSide);
	} else {
		_imageBufferSize = CGSizeMake(smallerSide, biggerSide);
	}
}

- (void)updateAreaOfInterest
{
	[self updateImageBufferSize];
	// Scale area of interest from view coordinate system to image coordinates.
	CGRect selectedRect = CGRectApplyAffineTransform(_selectedArea,
		CGAffineTransformMakeScale(_imageBufferSize.width * 1.f / CGRectGetWidth(_overlayView.frame),
		_imageBufferSize.height * 1.f / CGRectGetHeight(_overlayView.frame)));

	[self.service setAreaOfInterest:selectedRect];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[_session stopRunning];
	self.running = NO;
	self.captureButton.selected = NO;
	[_service stopTasks];

	[super viewWillDisappear:animated];
}

- (BOOL)prefersStatusBarHidden
{
	return YES;
}

- (void)viewDidLayoutSubviews
{
	[super viewDidLayoutSubviews];

	[self updatePreviewLayerFrame];
}

- (void)updatePreviewLayerFrame
{
	UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
	[_previewLayer.connection setVideoOrientation:[self videoOrientationFromInterfaceOrientation:orientation]];

	CGRect viewBounds = self.view.bounds;

	_previewLayer.frame = viewBounds;

	CGFloat widthMargin = CGRectGetWidth(viewBounds) * (1 - self.areaOfInterestRatio.width) / 2;
	CGFloat heigthMargin = CGRectGetHeight(viewBounds) * (1 - self.areaOfInterestRatio.height) / 2;
	self.selectedArea = CGRectInset(viewBounds, widthMargin, heigthMargin);

	[self updateAreaOfInterest];
}

- (void)setSelectedArea:(CGRect)selectedArea
{
	_selectedArea = selectedArea;
	_overlayView.selectedArea = _selectedArea;
}

- (AVCaptureVideoOrientation)videoOrientationFromInterfaceOrientation:(UIInterfaceOrientation)orientation
{
	AVCaptureVideoOrientation result = AVCaptureVideoOrientationPortrait;
	switch(orientation) {
		case UIInterfaceOrientationPortrait:
			result = AVCaptureVideoOrientationPortrait;
			break;
		case UIInterfaceOrientationPortraitUpsideDown:
			result = AVCaptureVideoOrientationPortraitUpsideDown;
			break;
		case UIInterfaceOrientationLandscapeLeft:
			result = AVCaptureVideoOrientationLandscapeLeft;
			break;
		case UIInterfaceOrientationLandscapeRight:
			result = AVCaptureVideoOrientationLandscapeRight;
			break;
		default:
			break;
	}

	return result;
}

- (void)dealloc
{
	[[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark - Notifications

- (void)applicationDidEnterBackground
{
	[_session stopRunning];
	[self.service stopTasks];
}

- (void)applicationWillEnterForeground
{
	[_session startRunning];
}

#pragma mark - Actions

- (IBAction)capturePressed
{
	if(!self.captureButton.enabled) {
		return;
	}

	self.captureButton.selected = !self.captureButton.selected;
	self.running = self.captureButton.selected;

	if(self.isRunning) {
		[self prepareUIForRecognition];
		self.errorOccurred = nil;
	} else {
		[self.service stopTasks];
		if(self.onSuccess != nil) {
			const BOOL StoppedByUser = YES;
			self.onSuccess(StoppedByUser);
		}
	}
}

- (IBAction)toggleSettingsTableVisibility
{
	BOOL state = self.settingsTableView.hidden;
	self.running = !state;
	self.captureButton.selected = !state;
	if(state) {
		[self.service stopTasks];
		[self.settingsTableView reloadData];
	}

	self.settingsTableView.hidden = !state;
	[self prepareUIForRecognition];
}

- (IBAction)toggleFlashMode
{
	if([self.captureDevice isTorchModeSupported:AVCaptureTorchModeOn]) {
		self.flashButton.selected = !self.flashButton.selected;
		[self.captureDevice lockForConfiguration:nil];
		self.captureDevice.torchMode = (self.flashButton.selected) ? AVCaptureTorchModeOn : AVCaptureTorchModeOff;
		[self.captureDevice unlockForConfiguration];
	}
}

- (IBAction)closeViewController
{
	self.captureButton.selected = NO;
	[self.service stopTasks];
	self.running = NO;

	if(self.onCancel != nil) {
		self.onCancel();
	}
}

- (void)prepareUIForRecognition
{
	[self clearScreenFromRegions];
	self.whiteBackgroundView.hidden = YES;
	[self.progressIndicatorView setProgress:0 color:[self progressColor:0]];
	[self updateLogMessage:nil];
}

#pragma mark - AVCapture configuration

- (void)configureAVCaptureSession
{
	NSError* error = nil;
	_session = [[AVCaptureSession alloc] init];
	[_session setSessionPreset:_sessionPreset];

	self.captureDevice = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
	AVCaptureDeviceInput* input = [AVCaptureDeviceInput deviceInputWithDevice:self.captureDevice error:&error];
	if(error != nil) {
		NSLog(@"%@", [error localizedDescription]);
		self.errorOccurred = error.localizedDescription;
		[self updateLogMessage:error.localizedDescription];
		return;
	}
	NSAssert([_session canAddInput:input], @"impossible to add AVCaptureDeviceInput");
	[_session addInput:input];

	AVCaptureVideoDataOutput* videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
	dispatch_queue_t videoDataOutputQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
	[videoDataOutput setSampleBufferDelegate:self queue:videoDataOutputQueue];
	[videoDataOutput alwaysDiscardsLateVideoFrames];
	videoDataOutput.videoSettings = [NSDictionary dictionaryWithObject:
		[NSNumber numberWithInt:kCVPixelFormatType_32BGRA]
		forKey:(id)kCVPixelBufferPixelFormatTypeKey];
	NSAssert([_session canAddOutput:videoDataOutput], @"impossible to add AVCaptureVideoDataOutput");
	[_session addOutput:videoDataOutput];

	[[videoDataOutput connectionWithMediaType:AVMediaTypeVideo] setEnabled:YES];
	AVCaptureVideoOrientation videoOrientation = [self videoOrientationFromInterfaceOrientation:
		[UIApplication sharedApplication].statusBarOrientation];
	[[videoDataOutput connectionWithMediaType:AVMediaTypeVideo] setVideoOrientation:videoOrientation];
}

- (void)configurePreviewLayer
{
	_previewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:_session];
	_previewLayer.backgroundColor = [[UIColor blackColor] CGColor];
	_previewLayer.videoGravity = AVLayerVideoGravityResize;
	CALayer* rootLayer = [self.previewView layer];
	[rootLayer insertSublayer:_previewLayer atIndex:0];

	[self updatePreviewLayerFrame];
}

- (void)avSessionFailed:(NSNotification*)notification
{
	NSError* error = notification.userInfo[AVCaptureSessionErrorKey];
	__weak RTRViewController* weakSelf = self;
	performBlockOnMainThread(0, ^{
		weakSelf.errorOccurred = error.localizedDescription;
		[weakSelf updateLogMessage:error.localizedDescription];
	});
}

#pragma mark - AVCaptureVideoDataOutputSampleBufferDelegate

- (void)captureOutput:(AVCaptureOutput*)captureOutput didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer
	fromConnection:(AVCaptureConnection*)connection
{
	if(!self.isRunning) {
		return;
	}

	__block BOOL invalidFrameOrientation = NO;
	dispatch_sync(dispatch_get_main_queue(), ^{
		AVCaptureVideoOrientation videoOrientation = [self videoOrientationFromInterfaceOrientation:
			[UIApplication sharedApplication].statusBarOrientation];
		if(connection.videoOrientation != videoOrientation) {
			[connection setVideoOrientation:videoOrientation];
			invalidFrameOrientation = YES;
		}
	});

	if(invalidFrameOrientation) {
		return;
	}

	[self.service addSampleBuffer:sampleBuffer];
}

#pragma mark -

- (void)updateLogMessage:(NSString*)message
{
	__weak RTRViewController* weakSelf = self;
	performBlockOnMainThread(0, ^{
		weakSelf.infoLabel.text = message;
	});
}

#pragma mark - Drawing results

/// Drawing text lines.
- (void)drawTextLines:(NSArray*)textLines progress:(RTRResultStabilityStatus)progress
{
	[self clearScreenFromRegions];
	
	CALayer* textRegionsLayer = [[CALayer alloc] init];
	textRegionsLayer.frame = _previewLayer.frame;
	textRegionsLayer.name = RTRTextRegionLayerName;

	for(RTRTextLine* textLine in textLines) {
		[self drawTextLine:textLine inLayer:textRegionsLayer progress:progress];
	}
	
	[self.previewView.layer addSublayer:textRegionsLayer];
}

/// Drawing data fields.
- (void)drawTextRegionsFromDataFields:(NSArray*)dataFields progress:(RTRResultStabilityStatus)progress
{
	[self clearScreenFromRegions];
	
	CALayer* textRegionsLayer = [[CALayer alloc] init];
	textRegionsLayer.frame = _previewLayer.frame;
	textRegionsLayer.name = RTRTextRegionLayerName;
	
	for(RTRDataField* dataField in dataFields) {
		for(RTRTextLine* textLine in dataField.components) {
			[self drawTextLine:textLine inLayer:textRegionsLayer progress:progress];
		}
	}
	
	[self.previewView.layer addSublayer:textRegionsLayer];
}

/// Remove all previously visible regions.
- (void)clearScreenFromRegions
{
	// Get all visible regions.
	NSArray* sublayers = [NSArray arrayWithArray:[self.previewView.layer sublayers]];
	
	// Remove all layers with the name RTRTextRegionLayerName.
	for(CALayer* layer in sublayers) {
		if([[layer name] isEqualToString:RTRTextRegionLayerName]) {
			[layer removeFromSuperlayer];
		}
	}
}

/// Drawing the quadrangle specified by the RTRTextLine object 
/// and a separate recognized text layer, if there is any recognized text.
- (void)drawTextLine:(RTRTextLine*)textLine inLayer:(CALayer*)layer progress:(RTRResultStabilityStatus)progress
{
	[self drawQuadrangle:textLine.quadrangle inLayer:layer progress:progress];

	NSString* recognizedString = textLine.text;
	if(recognizedString == nil) {
		return;
	}
	
	CATextLayer* textLayer = [CATextLayer layer];
	// Creating the text layer rectangle: it should be close to the quadrangle drawn previously.
	CGPoint topLeft = [self scaledPointFromImagePoint:textLine.quadrangle[0]];
	CGPoint bottomLeft = [self scaledPointFromImagePoint:textLine.quadrangle[1]];
	CGPoint bottomRight = [self scaledPointFromImagePoint:textLine.quadrangle[2]];
	CGPoint topRight = [self scaledPointFromImagePoint:textLine.quadrangle[3]];
	CGRect rectForTextLayer = CGRectMake(bottomLeft.x, bottomLeft.y,
		[self distanceBetweenPoint:topLeft andPoint:topRight],
		[self distanceBetweenPoint:topLeft andPoint:bottomLeft]);

	// Selecting the initial font size to suit the rectangle size.
	UIFont* textFont = [self fontForString:recognizedString inRect:rectForTextLayer];
	textLayer.font = (__bridge CFTypeRef)textFont;
	textLayer.fontSize = textFont.pointSize;
	textLayer.foregroundColor = [[self progressColor:progress] CGColor];
	textLayer.alignmentMode = kCAAlignmentCenter;
	textLayer.string = recognizedString;
	textLayer.frame = rectForTextLayer;
	
	// Rotating the text layer.
	CGFloat angle = asin((bottomRight.y - bottomLeft.y) / [self distanceBetweenPoint:bottomLeft andPoint:bottomRight]);
	textLayer.anchorPoint = CGPointMake(0.f, 0.f);
	textLayer.position = bottomLeft;
	CATransform3D t = CATransform3DIdentity;
	t = CATransform3DRotate(t, angle, 0.f, 0.f, 1.f);
	textLayer.transform = t;
	
	[layer addSublayer:textLayer];
}

/// Drawing a UIBezierPath using the quadrangle vertices.
- (void)drawQuadrangle:(NSArray<NSValue*>*)quadrangle inLayer:(CALayer*)layer progress:(RTRResultStabilityStatus)progress
{
	if(quadrangle.count == 0) {
		return;
	}

	CAShapeLayer* area = [CAShapeLayer layer];
	UIBezierPath* recognizedAreaPath = [UIBezierPath bezierPath];
	[quadrangle enumerateObjectsUsingBlock:^(NSValue* point, NSUInteger idx, BOOL* stop) {
		CGPoint scaledPoint = [self scaledPointFromImagePoint:point];
		if(idx == 0) {
			[recognizedAreaPath moveToPoint:scaledPoint];
		} else {
			[recognizedAreaPath addLineToPoint:scaledPoint];
		}
	}];

	[recognizedAreaPath closePath];
	area.path = recognizedAreaPath.CGPath;
	area.strokeColor = [[self progressColor:progress] CGColor];
	area.fillColor = [UIColor clearColor].CGColor;
	[layer addSublayer:area];
}

- (UIFont*)fontForString:(NSString*)string inRect:(CGRect)rect
{
	// Selecting the font size by height and then fine-tuning by width.

	CGFloat minFontSize = 0.1f; // initial font size
	CGFloat maxFontSize = 72.f;
	CGFloat fontSize = minFontSize;

	CGSize rectSize = rect.size;
	for(;;) {
		CGSize labelSize = [string sizeWithAttributes:@{NSFontAttributeName:[UIFont boldSystemFontOfSize:fontSize]}];
		if(rectSize.height - labelSize.height > 0) {
			minFontSize = fontSize;

			if(0.99f * rectSize.height - labelSize.height < 0) {
				break;
			}
		} else {
			maxFontSize = fontSize;
		}

		if(ABS(minFontSize - maxFontSize) < 0.01) {
			break;
		}

		fontSize = (minFontSize + maxFontSize) / 2;
	}

	return [UIFont boldSystemFontOfSize:fontSize];
}

/// Calculate the distance between points.
- (CGFloat)distanceBetweenPoint:(CGPoint)p1 andPoint:(CGPoint)p2
{
	CGVector vector = CGVectorMake(p2.x - p1.x, p2.y - p1.y);
	return sqrt(vector.dx * vector.dx + vector.dy * vector.dy);
}

/// Scale the point coordinates.
- (CGPoint)scaledPointFromImagePoint:(NSValue*)pointValue
{
	CGFloat layerWidth = _previewLayer.bounds.size.width;
	CGFloat layerHeight = _previewLayer.bounds.size.height;
	
	CGFloat widthScale = layerWidth / _imageBufferSize.width;
	CGFloat heightScale = layerHeight / _imageBufferSize.height;
	
	CGPoint point = [pointValue CGPointValue];
	point.x *= widthScale;
	point.y *= heightScale;
	
	return point;
}

/// Human-readable descriptions for the RTRCallbackWarningCode constants.
- (NSString*)stringFromWarningCode:(RTRCallbackWarningCode)warningCode
{
	NSString* warningString;
	switch(warningCode) {
		case RTRCallbackWarningTextTooSmall:
			warningString = @"Text is too small";
			break;

		default:
			break;
	}

	return warningString;
}

#pragma mark - Utils

#define RTRUIColorFromRGB(rgbValue) [UIColor \
	colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
	green:((float)((rgbValue & 0xFF00) >> 8))/255.0 \
	blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

- (UIColor*)progressColor:(RTRResultStabilityStatus)progress
{
	switch(progress) {
		case RTRResultStabilityNotReady:
		case RTRResultStabilityTentative:
			return RTRUIColorFromRGB(0xFF6500);
		case RTRResultStabilityVerified:
			return RTRUIColorFromRGB(0xC96500);
		case RTRResultStabilityAvailable:
			return RTRUIColorFromRGB(0x886500);
		case RTRResultStabilityTentativelyStable:
			return RTRUIColorFromRGB(0x4B6500);
		case RTRResultStabilityStable:
			return RTRUIColorFromRGB(0x006500);

		default:
			return [UIColor redColor];
			break;
	}
}

- (UITableViewCell*)tableViewCellWithConfiguration:(void (^)(UITableViewCell* cell))configurationHandler
{
	UITableViewCell* cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:nil];
	cell.backgroundColor = [UIColor colorWithWhite:0 alpha:0.4f];
	cell.textLabel.textColor = [UIColor whiteColor];
	cell.detailTextLabel.textColor = [UIColor lightGrayColor];
	cell.detailTextLabel.adjustsFontSizeToFitWidth = YES;
	cell.tintColor = [UIColor whiteColor];

	configurationHandler(cell);
	
	return cell;
}

#pragma mark - RTRRecognitionServiceDelegate

- (void)onWarning:(RTRCallbackWarningCode)warningCode
{
	NSString* message = [self stringFromWarningCode:warningCode];
	if(message.length > 0) {
		if(!self.isRunning) {
			return;
		}

		[self updateLogMessage:message];

		// Clear message after 2 seconds.
		__weak RTRViewController* weakSelf = self;
		performBlockOnMainThread(2, ^{
			[weakSelf updateLogMessage:nil];
		});
	}
}

- (void)onError:(NSError*)error
{
	NSLog(@"Error: %@", error);
	__weak RTRViewController* weakSelf = self;
	performBlockOnMainThread(0, ^{
		if(!weakSelf.isRunning) {
			return;
		}

		weakSelf.captureButton.selected = NO;
		weakSelf.running = NO;
		[weakSelf.service stopTasks];

		weakSelf.errorOccurred = error.localizedDescription;
		[weakSelf updateLogMessage:error.localizedDescription];
	});
}

#pragma mark - UITableViewDataSource caps

- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
	return self.settingsTableContent.count;
}

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
	return [UITableViewCell new];
}

@end
