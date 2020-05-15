//
//  RTRTextBlock+NSDictionary.h
//  RTRCordovaSample
//
//  Created by Никита Разумный on 1/20/20.
//

#import <UIKit/UIKit.h>
#import <AbbyyRtrSDK/AbbyyRtrSDK.h>

NS_ASSUME_NONNULL_BEGIN

@interface RTRTextBlock (rtr_Dictionary)

@property (nonatomic, readonly) NSDictionary* asDictionary;

@end

@interface RTRTextLine (rtr_Dictionary)

@property (nonatomic, readonly) NSDictionary* asDictionary;

@end

@interface RTRCharInfo (rtr_Dictionary)

@property (nonatomic, readonly) NSDictionary* asDictionary;

@end

NS_ASSUME_NONNULL_END
