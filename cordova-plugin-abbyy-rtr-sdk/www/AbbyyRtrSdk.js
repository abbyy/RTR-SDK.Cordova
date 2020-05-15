/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

var exec = require('cordova/exec');

module.exports = {

	/// Open modal dialog for Text Capture Scenario.
	startTextCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startTextCapture", [options]);
	},

	/// Open modal dialog for Data Capture Scenario.
	startDataCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startDataCapture", [options]);
	},

	/// Open modal dialog for Image Capture Scenario.
	startImageCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startImageCapture", [options]);
	},

	recognizeText: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "recognizeText", [options]);
	},

	extractData: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "extractData", [options]);
	},

	assessQualityForOcr: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "assessQualityForOcr", [options]);
	},

	detectDocumentBoundary: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "detectDocumentBoundary", [options]);
	},

	cropImage: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "cropImage", [options]);
	},

	rotateImage: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "rotateImage", [options]);
	},

	exportImage: function (options, successCallback, errorCallback) {
		exec(successCallback, errorCallback, "AbbyyRtrSdk", "exportImage", [options]);
	},

	exportImagesToPdf: function (options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "AbbyyRtrSdk", "exportImagesToPdf", [options]);
    },
}
