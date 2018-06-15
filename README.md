# ABBYY Real-Time Recognition SDK Cordova Plugin

ABBYY Real-Time Recognition SDK Cordova Plugin allows to use the Text Capture and Data Capture features of ABBYY Real-Time Recognition SDK (RTR SDK) in apps based on the [Apache Cordova](https://cordova.apache.org) framework.

This plugin requires the ABBYY RTR SDK native library which is available for Android and iOS. You can download its free version from the [ABBYY RTR SDK website](https://rtrsdk.com/). Extended versions of the native libraries are also available, with more recognition languages, more Data Capture features, and full Text Capture scenario support. If you are interested in the extended version, please contact the [ABBYY Sales Team](https://rtrsdk.com/contacts/).

The native libraries support the following systems:

* Android: version 4.4 or later for ARMv7 processors
* iOS: versions 8.x and later


## Getting started

1. Add the plugin to your project.
    ```sh
    cd MyProject
    cordova plugin add cordova-plugin-abbyy-rtr-sdk
    ```
2. Download ABBYY RTR SDK from https://rtrsdk.com/ (or get the extended version from the [ABBYY Sales Team](https://rtrsdk.com/contacts/)) and add it to your project:
    * Create the `www/rtr_assets` subdirectory in the project.
    * Copy RTR SDK assets (patterns and dictionaries) and license file (`AbbyyRtrSdk.license`) to `www/rtr_assets`.
    * For cordova-android >= 7.0 users additionally need to place assets mentioned above to `platforms/android/app/src/main/assets` manually or by specify resource-file in config.xml
    * Copy the Android library (`abbyy-rtr-sdk-1.0.aar`) to `libs/android`.
    * Copy the iOS framework (`AbbyyRtrSDK.framework`) to `libs/ios`.
3. Add `libs/android` and `libs/ios` to the linker search paths.
    * For Android, add the following settings to `platforms/android/build.gradle`:
        ```gradle
        allprojects {
          repositories {
            flatDir {
              dirs '../../../libs/android' // cordova-android >= 7
              dirs '../../libs/android' // cordova-android <= 6
            }
          }
        }
        ```
    * For iOS, add the following to `platforms/ios/cordova/build.xcconfig`:
        ```xcode
        FRAMEWORK_SEARCH_PATHS = "../../libs/ios"
        ```
4. To build and run your project:
    * For Android:
        ```sh
        cordova build android
        cordova run android
        ```
    * For iOS, specify your Development Team:
        ```sh
        cordova build ios --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        cordova run ios --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        ```


## Documentation

Developer documentation for this plugin is available from the [npm package](https://www.npmjs.com/package/cordova-plugin-abbyy-rtr-sdk) (see `RtrSdkCordovaDevelopersGuide.pdf`).

Developer documentation for the free version of ABBYY RTR SDK is available at https://rtrsdk.com/documentation/.

Developer documentation for the extended version is available from the extended version packages for iOS and Android.
