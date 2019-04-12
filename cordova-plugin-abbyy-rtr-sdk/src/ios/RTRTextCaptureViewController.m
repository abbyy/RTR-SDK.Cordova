/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRTextCaptureViewController.h"

@interface RTRTextCaptureViewController () <RTRTextCaptureServiceDelegate>

@end

@implementation RTRTextCaptureViewController

- (void)viewDidLoad
{
	[super viewDidLoad];

	[self.settingsButton setTitle:[self languagesButtonTitle] forState:UIControlStateNormal];

	RTRExtendedSettings* extendedSettings = [[RTRExtendedSettings alloc] init];
	[extendedSettings setValue:self.extendedSettings forKey:@"properties"];
	self.service = [self.rtrManager textCaptureServiceWithLanguages:self.selectedRecognitionLanguages delegate:self extendedSettings:extendedSettings];
}

#pragma mark -

- (IBAction)toggleSettingsTableVisibility
{
	[super toggleSettingsTableVisibility];
	if(self.settingsTableView.hidden) {
		[(id<RTRTextCaptureService>)self.service setRecognitionLanguages:_selectedRecognitionLanguages];
	}
}

#pragma mark - RTRTextCaptureServiceDelegate

- (void)onBufferProcessedWithTextLines:(NSArray*)textLines resultStatus:(RTRResultStabilityStatus)resultStatus
{
	__weak RTRTextCaptureViewController* weakSelf = self;
	performBlockOnMainThread(0, ^{
		if(!weakSelf.isRunning) {
			return;
		}

		[weakSelf.progressIndicatorView setProgress:resultStatus color:[weakSelf progressColor:resultStatus]];
		weakSelf.textLines = textLines;
		weakSelf.currentStabilityStatus = resultStatus;

		if(resultStatus == RTRResultStabilityStable && weakSelf.stopWhenStable) {
			weakSelf.running = NO;
			weakSelf.captureButton.selected = NO;
			weakSelf.whiteBackgroundView.hidden = NO;
			[weakSelf.service stopTasks];

			if(weakSelf.onSuccess != nil) {
				const BOOL AutomaticallyStopped = NO;
				weakSelf.onSuccess(AutomaticallyStopped);
			}
		}

		[weakSelf drawTextLines:textLines progress:resultStatus];
	});
}

- (NSString*)languagesButtonTitle
{
	if(_selectedRecognitionLanguages.count == 1) {
		return _selectedRecognitionLanguages.anyObject;
	}

	NSMutableString* resultTitle = [@"" mutableCopy];
	for(NSString* language in _selectedRecognitionLanguages) {
		[resultTitle appendFormat:@"%@ ", [language substringToIndex:MIN(2, language.length)].uppercaseString];
	}
	
	return resultTitle;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath
{
	NSString* language = self.settingsTableContent[indexPath.row];
	BOOL isSelected = ![_selectedRecognitionLanguages containsObject:language];
	if(isSelected) {
		[_selectedRecognitionLanguages addObject:language];
	} else {
		[_selectedRecognitionLanguages removeObject:language];
	}
	
	// Configure settings button
	self.settingsButton.enabled = _selectedRecognitionLanguages.count > 0;
	[self.settingsButton setTitle:[self languagesButtonTitle] forState:UIControlStateNormal];
	
	[tableView reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
}

#pragma mark - UITableViewDatasource

- (UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
	NSString* language = self.settingsTableContent[indexPath.row];
	
	return [self tableViewCellWithConfiguration:^(UITableViewCell* cell) {
		cell.textLabel.text = language;
		cell.accessoryType = [self.selectedRecognitionLanguages containsObject:language]
		? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
	}];
}

- (NSString*)presetTitle:(NSString*)preset
{
	NSRange fstRange = [preset rangeOfCharacterFromSet:[NSCharacterSet decimalDigitCharacterSet]];
	if(fstRange.location == NSNotFound) {
		return preset;
	}
	
	return [preset substringFromIndex:fstRange.location];
}

@end
