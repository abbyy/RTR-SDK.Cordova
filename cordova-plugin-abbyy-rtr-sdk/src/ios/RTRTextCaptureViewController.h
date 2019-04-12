/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRViewController.h"

@interface RTRTextCaptureViewController : RTRViewController

@property (nonatomic) NSMutableSet* selectedRecognitionLanguages;

@property (nonatomic, strong) NSArray<RTRTextLine*>* textLines;

@end
