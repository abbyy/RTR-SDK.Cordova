/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import <UIKit/UIKit.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>
#import <Cordova/CDVPlugin.h>
#import "RTRManager.h"
#import "RTRSelectedAreaView.h"
#import "RTRProgressView.h"

extern void performBlockOnMainThread(NSInteger delay, void(^block)());

@interface RTRViewController : UIViewController <RTRRecognitionServiceDelegate, UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, weak) RTRManager* rtrManager;
@property (nonatomic) id<RTRRecognitionService> service;

/// Image size.
@property (nonatomic, assign) CGSize imageBufferSize;

@property (nonatomic, copy) void (^onCancel)();
@property (nonatomic, copy) void (^onSuccess)(BOOL isManuallyStopped);

@property (nonatomic) BOOL stopWhenStable;
@property (nonatomic, getter=isFlashlightVisible) BOOL isFlashlightVisible;
@property (nonatomic, getter=isStopButtonVisible) BOOL stopButtonVisible;
@property (nonatomic, getter=isLanguageSelectionEnabled) BOOL languageSelectionEnabled;
@property (nonatomic) CGSize areaOfInterestRatio;

@property (nonatomic) NSArray* settingsTableContent;

@property (nonatomic, assign) RTRResultStabilityStatus currentStabilityStatus;
@property (nonatomic, strong) NSString* errorOccurred;

/// Is recognition running.
@property (atomic, assign, getter=isRunning) BOOL running;

/// Capture settings table.
@property (nonatomic, weak) IBOutlet UITableView* settingsTableView;
/// Button for show / hide table with recognition languages.
@property (nonatomic, weak) IBOutlet UIButton* settingsButton;
/// Button for switching flash mode.
@property (nonatomic, weak) IBOutlet UIButton* flashButton;

/// View with camera preview layer.
@property (nonatomic, weak) IBOutlet UIView* previewView;
/// Stop/Start capture button
@property (nonatomic, weak) IBOutlet UIButton* captureButton;

/// View for displaying current area of interest.
@property (nonatomic, weak) IBOutlet RTRSelectedAreaView* overlayView;
/// White view for highlight recognition results.
@property (nonatomic, weak) IBOutlet UIView* whiteBackgroundView;

/// Label for current scenario description.
@property (nonatomic, weak) IBOutlet UILabel* descriptionLabel;
/// Label for error or warning info.
@property (nonatomic, weak) IBOutlet UILabel* infoLabel;
/// Progress indicator view.
@property (nonatomic, weak) IBOutlet RTRProgressView* progressIndicatorView;

- (IBAction)capturePressed;
- (IBAction)toggleSettingsTableVisibility;
- (IBAction)toggleFlashMode;

- (void)drawTextLines:(NSArray*)textLines progress:(RTRResultStabilityStatus)progress;
- (void)drawTextRegionsFromDataFields:(NSArray*)dataFields progress:(RTRResultStabilityStatus)progress;

- (void)prepareUIForRecognition;
- (void)updateAreaOfInterest;

- (UITableViewCell*)tableViewCellWithConfiguration:(void (^)(UITableViewCell* cell))configurationHandler;

- (void)updateLogMessage:(NSString*)message;
- (UIColor*)progressColor:(RTRResultStabilityStatus)progress;

@end
