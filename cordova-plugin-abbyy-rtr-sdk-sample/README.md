# ABBYY Real-Time Recognition SDK Cordova Plugin Example

This example app shows how to integrate the ABBYY Real-Time Recognition SDK Cordova Plugin.

## How to run this app

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
5. Download ABBYY Real-Time Recognition SDK (RTR SDK) from https://rtrsdk.com/ and add it to the example:
    * Copy RTR SDK assets (patterns and dictionaries) and your license file (`AbbyyRtrSdk.license`) to `www/rtr_assets`.
    * Copy the Android library (`abbyy-rtr-sdk-1.0.aar`) to `libs/android`.
    * Copy the iOS framework (`AbbyyRtrSDK.framework`) to `libs/ios`.
    These paths already exist in the example project.
6. Add `libs/android` and `libs/ios` to the linker search paths.
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
7. Connect a device via USB, build and run.
    * Android:
        ```sh
        cordova build android
        cordova run android
        ```
    * iOS (don't forget to specify your Development Team):
        ```sh
        cordova build ios --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        cordova run ios --buildFlag="DEVELOPMENT_TEAM=<YOUR_TEAM>"
        ```
