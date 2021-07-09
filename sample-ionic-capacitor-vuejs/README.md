# How to add ABBYY Mobile Capture SDK to your Ionic Capacitor - VueJS app

To start developing an Ionic-based app with *Capacitor instead of Cordova* using ABBYY Mobile Capture SDK features, you need to add ABBYY Mobile Capture SDK Cordova Plugin and the ionic native ABBBYY RTR SDK, then add target platforms and the Cordova plugin to your project, and thereafter copy the ABBYY Mobile Capture SDK assets and native libraries for Android and iOS, as described below.

## Adding Cordova Plugin
Add the plugin to your project from npm. It is recommended to add the 1.0.16 [cordova-plugin-abbyy-rtr-sdk](https://www.npmjs.com/package/cordova-plugin-abbyy-rtr-sdk) version, as it supports all up-to-date changes.

`npm install cordova-plugin-abbyy-rtr-sdk@1.0.16`

Add the ionic-native plugin to your project from npm. 

`npm install @ionic-native/abbyy-rtr`

Run the next command to apply changes:

`npx cap sync`

## Projects set-up

### ABBYY MobileCapture environment setup
1. Request ABBYY Mobile Capture trial version on the [ABBYY website](https://www.abbyy.com/mobile-capture-sdk/#request-demo) and add it to your project:

* Copy iOS `assets` directory to the project into `../assets` folder
* Copy your license into `../assets` with MobileCapture.License name
* Copy Android `libs` directory to the project into `../libs/android` folder 
* Copy iOS `libs` directory to the project into `../libs/ios` folder

Your folder / project strucuture should look like the following: 

```
...
- assets
- libs
    - android
    - ios
- node_modules
- ios (if already added this platform `npx cap add ios`)
- android (if already added this platform `npx cap add android`)
- src
...
```



2. Proceed setup depending on the platform:

#### Android setup

Edit the **android/build.gradle**:

```
allprojects {
    repositories {
        flatDir {
            // Search path for Mobile Capture libs
            dirs '../../libs/android'
        }
        google()
        jcenter()
    }
}
```

Add the following code to the same gradle file to fix Ionic and Cordova Java version conflict:

```
subprojects {
    afterEvaluate {
        android {
               compileOptions {
                   sourceCompatibility 1.8
                   targetCompatibility 1.8
               }
           }

    }
}
```

Edit the **android/app/build.gradle**:

```
android {
   ...
   sourceSets {
      main {
         // Mobile Capture assets
         assets.srcDirs += ['../../assets']
      }
   }
}
Add the libs to the application in the same gradle file: 
repositories {
    flatDir{
        // Search path for Mobile Capture libs
        dirs '../../libs/android'
    }
} 
```

Set minimum SDK version to '21' in the **android/variables.gradle**.

#### iOS setup

For easier setup open XCode to follow the instructions below. 

`npx cap open ios`

Add to the **ios/App/Podfile** file the paths to the lib files:

```
post_install do |installer|
   installer.pods_project.targets.each do |target|
      target.build_configurations.each do |config|

         ...

         # Settings to use Mobile Capture SDK.
         config.build_settings['ENABLE_BITCODE'] = "NO"
         config.build_settings['FRAMEWORK_SEARCH_PATHS'] = "$(inherited) ../../../libs/ios"
      end
   end
end
```

To add the license file, the resource files and set up the copying rules, in **Build Phases** add one more Run Script phase. The **copy_assets.py** script, added to the phase, will automatically copy all resource files to corresponding destinations and add necessary dictionaries.
For this select your project (not the Pods project) and select your App in **Target** group and click **Build Phases** in the tab menu. There you can add a **New Runscript** by clicking the **+**-Icon.

```
python "${SRCROOT}/../../assets/copy_assets.py"
cp "${SRCROOT}/../../assets/MobileCapture.License" "${TARGET_BUILD_DIR}/${WRAPPER_NAME}/"
```

In **Build Phases**, add a new **Run Script** phase where XCode should run the **copy_frameworks.sh** script that removes the frameworks for the non-active CPU architectures (the complete list depends on the project settings), and sign the resulting framework. This is a required step before uploading your application to App Store.

```
/bin/sh "${SRCROOT}/../../libs/ios/copy_frameworks.sh"
```

Select your project in the **Target** group and open the **Build Settings** tab. In the **Search Paths** section add to the **Framework Search Paths** the following path:
```
"../../libs/ios"
```

On the **Build Settings** tab set **Enable Bitcode** option to **No**.

In the **info.plist** file add permission for working with camera and gallery **if necessary**.

Apply the settings:

```
cd ios/App
pod install
```

Afterwards you can build your application, see instruction below. 
If you have any path errors try to delete the **Derived Data** Folder and change the paths.

**Important!** A certain folder structure is required for the setup. Make sure to change the paths if your folder structure differs from the following one:

```
assets/
   bcr/
      ...
   dictionaries/
      ...
   ...
   MobileCapture.License
libs/
   android/
      abbyy-rtr-sdk-1.0.aar
      abbyy-ui-1.0.aar
   ios/
      AbbyyRtrSDK.framework
      AbbyyUI.framework
      ...
[APPLICATION_FOLDER]/
   ...
```

## Code sample

```
...
import { AbbyyRTR } from '@ionic-native/abbyy-rtr'
...

methods: {
    startTextExtraction () {
      AbbyyRTR.startTextCapture({
        isFlashlightVisible: true,
        isStopButtonVisible: true,
        stopWhenStable: true,
        recognitionLanguages: ['English', 'German'],
        areaOfInterest: '0.9 0.1'
      }).then(res => {
        console.log('detected: ' + JSON.stringify(res))
      }).catch(err => {
        console.log('error has occured')
        console.log(JSON.stringify(err))
        if (err === undefined) {
          console.error('undefined error?!')
        }
      })
    }
  }
```

## How to run the sample from distributive

1. Request ABBYY Mobile Capture trial version on the [ABBYY website](https://www.abbyy.com/mobile-capture-sdk/#request-demo) and add it to your project:

* Copy iOS `assets` directory to the project into `../assets` folder
* Copy your license into `../assets` with MobileCapture.License name
* Copy Android `libs` directory to the project into `../libs/android` folder 
* Copy iOS `libs` directory to the project into `../libs/ios` folder

2. Add the plugin and follow the instructions above, apply changes:

```
npm install
npm run build
npx cap sync
```

3. To run application do the following:
* Android:
  * **From console:**
    Execute `npx cap open android` from the sample root 
  * **From Android Studio:**
    Click `Run app` button 
* iOS:
  * **From console:**
    Execute `npx cap open ios` from the sample root 
  * **From Xcode:**
    Click `Run` button 
    
