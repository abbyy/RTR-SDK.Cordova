/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

#import "RTRViewController.h"

@interface RTRTextCaptureViewController : RTRViewController

@property (nonatomic) NSMutableSet* selectedRecognitionLanguages;

@property (nonatomic, strong) NSArray<RTRTextLine*>* textLines;

@end
