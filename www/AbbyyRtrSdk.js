/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

var exec = require('cordova/exec');

module.exports = {
	/// Open modal dialog for Text Capture Scenario.
	startTextCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startTextCapture", [options]);
	},

	/// Open modal dialog for Data Capture Scenario.
	startDataCapture: function (callback, options) {
		exec(callback, callback, "AbbyyRtrSdk", "startDataCapture", [options]);
	}
}