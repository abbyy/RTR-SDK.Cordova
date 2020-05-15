//
//  RTRTextBlock+NSDictionary.m
//  RTRCordovaSample
//
//  Created by Никита Разумный on 1/20/20.
//

#import "RTRTextCaptureExtensions.h"

static NSString* stringFromCGRect(CGRect rect)
{
	return [NSString stringWithFormat:@"%d %d %d %d",
		(int)rect.origin.x,
		(int)rect.origin.y,
		(int)rect.size.width,
		(int)rect.size.height];
}

static NSString* stringFromQuadrangle(NSArray<NSValue*>* quadrangle)
{
	NSMutableString* quadrangleString = @"".mutableCopy;
	for(NSValue* pointValue in quadrangle) {
		CGPoint point = pointValue.CGPointValue;
		[quadrangleString appendFormat:@"%d %d ", (int)point.x, (int)point.y];
	}
	return [quadrangleString stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@" "]];
}

@implementation RTRTextBlock (rtr_Dictionary)

- (NSDictionary*)asDictionary
{
	NSMutableArray* textLines = @[].mutableCopy;
	for(RTRTextLine* line in self.textLines) {
		[textLines addObject:line.asDictionary];
	}
	return @{
		@"textLines": textLines
	};
}

@end

@implementation RTRTextLine (rtr_Dictionary)

- (NSDictionary*)asDictionary
{
	NSMutableArray* charsInfo = @[].mutableCopy;
	for(RTRCharInfo* charInfo in self.charsInfo) {
		[charsInfo addObject:charInfo.asDictionary];
	}
	return @{
		@"text": self.text,
		@"quadrangle": stringFromQuadrangle(self.quadrangle),
		@"rect": stringFromCGRect(self.rect),
		@"charsInfo": charsInfo
	};
}

@end

@implementation RTRCharInfo (rtr_Dictionary)

- (NSDictionary*)asDictionary
{
	return @{
		@"rect": stringFromCGRect(self.rect),
		@"quadrangle": stringFromQuadrangle(self.quadrangle),
		@"isItalic": @(self.isItalic),
		@"isBold": @(self.isBold),
		@"isUnderlined": @(self.isUnderlined),
		@"isStrikethrough": @(self.isStrikethrough),
		@"isSmallcaps": @(self.isSmallcaps),
		@"isSuperscript": @(self.isSuperscript),
		@"isUncertain": @(self.isUncertain)
	};
}

@end
