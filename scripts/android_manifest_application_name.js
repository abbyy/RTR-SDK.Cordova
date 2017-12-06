// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

module.exports = function (context) {
	var fs = context.requireCordovaModule('fs'),
		path = context.requireCordovaModule('path');

	var platformRoot = path.join(context.opts.projectRoot, 'platforms/android');
	var manifestFile = path.join(platformRoot, 'AndroidManifest.xml');

	if(fs.existsSync(manifestFile)) {
		fs.readFile(manifestFile, 'utf8', function (error, data) {
			if(error) {
				throw new Error('Unable to find AndroidManifest.xml: ' + error);
			}

			var appClass = 'com.abbyy.mobile.rtr.cordova.RtrManager';

			if(data.indexOf(appClass) == -1) {
				var result = data.replace(/<application/g, '<application android:name="' + appClass + '"');
				fs.writeFile(manifestFile, result, 'utf8', function (error) {
					if(error) {
						throw new Error('Unable to write into AndroidManifest.xml: ' + error);
					}
				})
			}
		});
	}
};
