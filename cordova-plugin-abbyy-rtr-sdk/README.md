# ABBYY Real-Time Recognition SDK Cordova Plugin

ABBYY Real-Time Recognition SDK Cordova Plugin allows to use the Text Capture and Data Capture features of ABBYY Real-Time Recognition SDK (RTR SDK) in apps based on the [Apache Cordova](https://cordova.apache.org) framework.

This plugin requires the ABBYY RTR SDK native library which is available for Android and iOS. You can download its free version from the [ABBYY RTR SDK website](https://rtrsdk.com/). Extended versions of the native libraries are also available, with more recognition languages, more Data Capture features, and full Text Capture scenario support. If you are interested in the extended version, please contact [ABBYY sales](https://rtrsdk.com/contacts/).

## Getting started

1. Add the plugin to your project.
    ```sh
    cd MyProject
    cordova plugin add cordova-plugin-abbyy-rtr-sdk
    ```
2. Download ABBYY RTR SDK from https://rtrsdk.com/ and add it to your project:
    * Create the `www/rtr_assets` subdirectory in the project.
    * Copy RTR SDK assets (patterns and dictionaries) and license file (`AbbyyRtrSdk.license`) to `www/rtr_assets`.
    * Copy the Android library (`abbyy-rtr-sdk-1.0.aar`) to `libs/android`.
    * Copy the iOS framework (`AbbyyRtrSDK.framework`) to `libs/ios`.
3. Add `libs/android` and `libs/ios` to the linker search paths.
    * For Android, add the following settings to `platforms/android/build.gradle`:
        ```gradle
        allprojects {
          repositories {
            flatDir {
              dirs '../../libs/android'
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

Developer documentation for this plugin (`RtrSdkCordovaDevelopersGuide.pdf`) is available as the part of [this](https://www.npmjs.com/package/cordova-plugin-abbyy-rtr-sdk) npm package.

ABBYY RTR SDK developer documentation is available at https://rtrsdk.com/documentation/.
