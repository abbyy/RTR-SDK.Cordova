/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRViewController.h"

@class RTRDataCaptureScenario;

@interface RTRDataCaptureViewController : RTRViewController

/// Selected data capture scenario.
@property (nonatomic) RTRDataCaptureScenario* selectedScenario;
@property (nonatomic, strong) NSString* profile;

@property (nonatomic, strong) RTRDataScheme* dataScheme;
@property (nonatomic, strong) NSArray<RTRDataField*>* dataFields;

@end
