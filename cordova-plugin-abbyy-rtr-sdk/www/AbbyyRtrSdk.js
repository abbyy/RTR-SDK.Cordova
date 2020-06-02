/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

var exec = require('cordova/exec');

module.exports = {

	// Opens a screen for the text capture scenario
	//
	// Default settings:
	// {
	//  licenseFileName: 'MobileCapture.License',
	//  selectableRecognitionLanguages: [],
	//  areaOfInterest: '0.8 0.3',
	//  orientation: 'Default',
	//  stopWhenStable: true,
	//  isFlashlightVisible: true,
	//  isStopButtonVisible: true,
	//  recognitionLanguages: ['English']
	// }
	//
	// Result example:
	// {
	//   textLines: [
	//     {
	//       text: "John",
	//       quadrangle: [{ x: 100, y: 100 }, ...],
	//       rect: { left: 100, ... },
	//       charInfo: [
	//         { // For 'J'
	//           quadrangle: ...,
	//           rect: ...,
	//           isUncertain: false
	//         },
	//         ...
	//       ]
	//     },
	//     ...
	//   ],
	//   resultInfo : {
	//     stabilityStatus : "Available",
	//     recognitionLanguages : ["English"],
	//     frameSize : "720 1280"
	//   }
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	startTextCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startTextCapture", [options]);
	},

	// Opens a screen for the data capture scenario
	//
	// Default settings:
	// {
	//  licenseFileName: 'MobileCapture.License',
	//  areaOfInterest: '0.8 0.3',
	//  orientation: 'Default',
	//  stopWhenStable: true,
	//  isFlashlightVisible: true,
	//  isStopButtonVisible: true,
	//  recognitionLanguages: ['English']
	// }
	//
	// Result example:
	// {
	//   dataScheme : {
	//     id : "Hello",
	//     name : "Hello"
	//   },
	//   dataFields: [
	//     {
	//       id: "FirstName",
	//       name: "First Name",
	//       text: "John",
	//       quadrangle: [{ x: 100, y: 100 }, ...],
	//       rect: { left: 100, ... },
	//       charInfo: [
	//         { // For 'J'
	//           quadrangle: ...,
	//           rect: ...,
	//           isUncertain: false
	//         },
	//         ...
	//       ],
	//       components: [
	//         { id: ..., name: ..., ... }
	//       ]
	//     },
	//     ...
	//   ],
	//   resultInfo : {
	//     stabilityStatus : "Available",
	//     userAction : "Manually Stopped",
	//     frameSize : "720 1280"
	//   }
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	startDataCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startDataCapture", [options]);
	},

	// Opens a screen for the image capture scenario
	//
	// Default settings:
	// {
	//  licenseFileName: 'MobileCapture.License',
	//  cameraResolution: 'FullHD',
	//  isFlashlightButtonVisible: true,
	//  isCaptureButtonVisible: true,
	//  isGalleryButtonVisible: true,
	//  orientation: 'Default',
	//  isShowPreviewEnabled: false,
	//  requiredPageCount: 0,
	//  destination: 'File',
	//  exportType: 'Jpg',
	//  compressionLevel: 'Low',
	//  defaultImageSettings: {
	//    aspectRatioMin: 0.0,
	//    aspectRatioMax: 0.0,
	//    imageFromGalleryMaxSize: 4096,
	//    minimumDocumentToViewRatio: 0.15,
	//    documentSize: 'Any'
	//  }
	// }
	//
	// Result example:
	// {
	//  "images": [
	//    {
	//      "resultInfo": {
	//        size: { width: 658, height: 1187 },
	//        "exportType": "Jpg"
	//      },
	//      "filePath": "/data/user/0/com.abbyy.rtr.cordova.sample/files/pages/page_334fd281-f472-4756-a4a0-d1f8d1857a0c.jpg"
	//    }
	//  ],
	//  "resultInfo": {
	//    uriPrefix: "file://"
	//  }
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	startImageCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startImageCapture", [options]);
	},

	// Starts a text capture scenario for a single image
	//
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'file:///data/user/0/com.abbyy.rtr.cordova.sample/files/pages/page_334fd281-f472-4756-a4a0-d1f8d1857a0c.jpg',
	//   isTextOrientationDetectionEnabled: true,
	//   recognitionLanguages: ['English']
	// }
	//
	// Result example:
	// {
	//   orientation: 90,
	//   warnings: ['ProbablyLowQualityImage'],
	//   text: "Test\ntext",
	//   textBlocks: [
	//     {
	//       textLines: [
	//         {
	//           text: "Test",
	//           quadrangle: [{x: 100, y: 200}, {x: 100, y: 50}, {x: 300, y: 50}, {x: 300, y: 200}],
	//           rect: {left: 100, top: 50, right: 300, bottom: 200},
	//           charInfo: [
	//             { // For 'T'
	//               quadrangle: ...,
	//               rect: ...,
	//               isItalic: true,
	//               isBold: true,
	//               isUnderlined: true,
	//               isStrikethrough: true,
	//               isSmallcaps: true,
	//               isSuperscript: true,
	//               isUncertain: true
	//             },
	//             { // For 'e' },
	//             { // For 's' },
	//             { // For 't' },
	//           ]
	//         },
	//         ...
	//       ]
	//     },
	//     ...
	//   ]
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	recognizeText: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "recognizeText", [options]);
	},

	// Starts a data capture scenario for a single image
	// 
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'data:image/jpeg;base64,...', // Works with base64 as well as with files.
	//   profile: 'BusinessCards',
	//   isTextOrientationDetectionEnabled: true,
	//   recognitionLanguages: ['English']
	// }
	//
	// Result example:
	// {
	//   orientation: 0,
	//   warnings: ['ProbablyWrongLanguage'],
	//   dataFields: [
	//     {
	//       id: "FirstName",
	//       name: "First Name",
	//       text: "John",
	//       quadrangle: [{ x: 100, y: 100 }, ...],
	//       rect: { left: 100, ... },
	//       charInfo: [
	//         { // For 'J'
	//           quadrangle: ...,
	//           rect: ...,
	//           isUncertain: false
	//         },
	//         ...
	//       ],
	//       components: [
	//         { id: ..., name: ..., ... }
	//       ]
	//     },
	//     ...
	//   ]
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	extractData: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "extractData", [options]);
	},

	// Estimates if an image quality is suitable for OCR
	//
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'file:///data/user/0/...',
	// }
	//
	// Result example:
	// {
	//   qualityAssessmentForOcrBlocks: [
	//     {
	//       type: 'Text',
	//       quality: 90,
	//       rect: {
	//         top: 100,
	//         bottom: 200,
	//         left: 100,
	//         right: 200
	//       }
	//     },
	//     {
	//       type: 'Unknown',
	//       quality: 70,
	//       rect: {
	//         top: 100,
	//         bottom: 200,
	//         left: 200,
	//         right: 300
	//       }
	//     },
	//     ...
	//   ]
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	assessQualityForOcr: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "assessQualityForOcr", [options]);
	},

	// Detects a quadrangle representing document boundary on an image
	//
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'file:///data/user/0/...',
	// }
	// 
	// Result example:
	// {
	//   documentBoundary: [
	//     {x: 50, y: 600},
	//     {x: 50, y: 100},
	//     {x: 340, y: 120},
	//     {x: 330, y: 590},
	//   ]
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	detectDocumentBoundary: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "detectDocumentBoundary", [options]);
	},

	// Crops image according to the document boundary and size
	//
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'file:///data/user/0/...',
	//   documentBoundary: [
	//     {x: 50, y: 600},
	//     {x: 50, y: 100},
	//     {x: 340, y: 120},
	//     {x: 330, y: 590},
	//   ],
	//   result: {
	//     destination: 'Base64',
	//     compressionLevel: 'Low',
	//     exportType: 'Jpg'
	//   }
	// }
	//
	// Result example:
	// {
	//   imageUri: 'data:image/jpeg;base64,...',
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	cropImage: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "cropImage", [options]);
	},

	// Rotates image by specified angle
	//
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'data:image/png;base64,...',
	//   angle: 180,
	//   result: {
	//     destination: 'File',
	//   }
	// }
	//
	// Result example:
	// {
	//   imageUri: 'file:///data/user/0/...',
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	rotateImage: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "rotateImage", [options]);
	},

	// Exports an image to JPG or PNG format
	// 
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   imageUri: 'file:///data/user/0/...',
	//   result: {
	//     destination: 'Base64',
	//     compressionLevel: 'Low',
	//     exportType: 'Png'
	//   }
	// }
	//
	// Result example:
	// {
	//   imageUri: 'data:image/png;base64,...',
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	exportImage: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "exportImage", [options]);
	},

	// Exports images to PDF format
	// 
	// Settings example:
	// {
	//   licenseFileName: 'MobileCapture.License',
	//   images: [
	//     {
	//       imageUri: 'file:///data/user/0/...',
	//       compressionLevel: 'Low',
	//       pageSize: {width: 100, height:200},
	//     },
	//     {
	//       imageUri: 'file:///data/user/0/...',
	//       compressionLevel: 'Normal',
	//       pageSize: {width: 200, height:300},
	//     }
	//   ],
	//   result: {
	//     destination: 'File',
	//     filePath: "/data/user/0/.../dir1/file.pdf",
	//     pdfInfo: {
	//       title: 'Title',
	//       author: 'Author'
	//     }
	//   }
	// }
	//
	// Result example:
	// {
	//   pdfUri: 'file:///data/user/0/.../dir1/file.pdf',
	// }
	// 
	// See full documentation at https://help.abbyy.com/en-us/mobilecapturesdk/1/cordova_help/cordova-abbyyrtrsdk-module
	exportImagesToPdf: function (options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "AbbyyRtrSdk", "exportImagesToPdf", [options]);
    },
}
