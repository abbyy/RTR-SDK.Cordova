# ABBYY Mobile Capture Cordova Plugin

ABBYY Mobile Capture Cordova Plugin allows to use the Text Capture, Data Capture and Image Capture features of ABBYY Mobile Capture in apps based on the [Apache Cordova](https://cordova.apache.org) framework.

This plugin requires the ABBYY Mobile Capture native libraries which are available for Android and iOS. You can request ABBYY Mobile Capture trial version on the [ABBYY website](http://www.abbyy.com/mobile-capture-sdk/#request-demo).

The native libraries support the following systems:

* Android version: 5.0 or later for ARMv7 (armeabi-v7a) and ARMv8 (arm64-v8a) processors
* iOS: versions 11.x and later


## Getting started

1. Add the plugin to your project.
    ```sh
    cd MyProject
    cordova plugin add cordova-plugin-abbyy-rtr-sdk
    ```
2. Request ABBYY Mobile Capture trial version on the [ABBYY website](http://www.abbyy.com/mobile-capture-sdk/#request-demo) and add it to your project:
    * Create the `www/rtr_assets` subdirectory in the project.
    * Copy Mobile Capture SDK assets and license file (`MobileCapture.License`) to `www/rtr_assets`.
    * Copy Android libraries  (`abbyy-rtr-sdk-1.0.aar` and `abbyy-ui-1.0.aar`) to `libs/android`.
    * Copy iOS frameworks (`AbbyyRtrSDK.framework` and other modules) to `libs/ios`.
    * The `libs/android` and `libs/ios` paths should be added to the linker search paths. This step is performed automatically during the plugin installation.
3. To build and run your project:
    * Android:
        ```sh
        cordova build android
        cordova run android
        ```
    * iOS (don't forget to specify your Development Team):
        ```sh
        cordova build ios --buildFlag="-UseModernBuildSystem=0" --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        cordova run ios --buildFlag="-UseModernBuildSystem=0" --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        ```

## ABBYY Mobile Capture Cordova Plugin Example

### How to run this app

1. Install [Cordova](https://cordova.apache.org).
2. Get the example project and navigate to the project directory.
    ```sh
    cd <path>/cordova-plugin-abbyy-rtr-sdk-sample
    ```
3. Add platforms and plugins from the project `config.xml`.
    ```sh
    cordova prepare
    ```
4. Check installed platforms and plugins.
    ```sh
    cordova platform ls
    cordova plugin ls
    ```
    You should see `android` and `ios` in platforms and `cordova-plugin-abbyy-rtr-sdk` in plugins.
5. Request ABBYY Mobile Capture trial version on the [ABBYY website](http://www.abbyy.com/mobile-capture-sdk/#request-demo) and add it to the example:
    * Copy RTR SDK assets (patterns and dictionaries) and your license file (`MobileCapture.License`) to `www/rtr_assets`.
    * Copy the Android library (`abbyy-rtr-sdk-1.0.aar`) to `libs/android`.
    * Copy iOS frameworks (`AbbyyRtrSDK.framework` and other modules) to `libs/ios`.
    These paths already exist in the example project.
    * The `libs/android` and `libs/ios` paths should be added to the linker search paths. This step is performed automatically during the plugin installation.
6. Connect a device via USB, build and run.
    * Android:
        ```sh
        cordova build android
        cordova run android
        ```
    * iOS (don't forget to specify your Development Team):
        ```sh
        cordova build ios --buildFlag="-UseModernBuildSystem=0" --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        cordova run ios --buildFlag="-UseModernBuildSystem=0" --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        ```



## Documentation

Developer documentation for this plugin is available from the packages for iOS and Android.

