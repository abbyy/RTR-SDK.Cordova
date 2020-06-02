// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

module.exports = function (context) {
	var fs = require('fs'),
		path = require('path');

	var platformRoot = path.join(context.opts.projectRoot, 'platforms/android');
	
	var propertiesFile = path.join(platformRoot, 'gradle.properties');

	if(fs.existsSync(propertiesFile)) {
		fs.readFile(propertiesFile, 'utf8', function (error, data) {
			if(error) {
				throw new Error('Unable to read gradle.properties: ' + error);
			}
			
			if(data.indexOf("useAndroidX") == -1) {
				var result = data+"\nandroid.useAndroidX=true\nandroid.enableJetifier=true";
				fs.writeFile(propertiesFile, result, 'utf8', function (error) {
					if(error) {
						throw new Error('Unable to write into gradle.properties: ' + error);
					}
				});
			}
		});
	}
};
