/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

#import "UIButton+RecognitionLanguages.h"

@implementation UIButton (rtr_RecognitionLanguages)

- (void)rtr_setTitleWithLanguages:(NSSet<NSString*>*)languages forState:(UIControlState)state
{
	if(languages.count == 0) {
		[self setTitle:@"" forState:state];
		return;
	}
	if(languages.count == 1) {
		[self setTitle:languages.anyObject forState:state];
		return;
	}
	NSMutableString* resultTitle = [@"" mutableCopy];
	for(NSString* language in languages) {
		[resultTitle appendFormat:@"%@ ", [language substringToIndex:MIN(2, language.length)].uppercaseString];
	}
	[self setTitle:resultTitle forState:state];
}

@end
