// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

module.exports = function (context) {
	var fs = require('fs'),
		path = require('path');

	var platformRoot = path.join(context.opts.projectRoot, 'platforms/android');
	var buildScriptFile = path.join(platformRoot, 'build.gradle');

	if(fs.existsSync(buildScriptFile)) {
		fs.readFile(buildScriptFile, 'utf8', function (error, data) {
			if(error) {
				throw new Error('Unable to find build.gradle: ' + error);
			}
			
			if(data.indexOf("cordova-android >= 7") == -1) {
				var repos = /repositories\s{/g;
				var occurrence = 0;
				var result = data.replace(repos, function (match) {
					occurrence++;
					if (occurrence === 2) {
						return match + "\n\t\tflatDir {\n\t\t\tdirs '../../../libs/android' // cordova-android >= 7 \n\t\t\tdirs '../../libs/android' // cordova-android <= 6\n\t\t}";
					} else {
						return match;
					}
				});
				fs.writeFile(buildScriptFile, result, 'utf8', function (error) {
					if(error) {
						throw new Error('Unable to write into build.gradle: ' + error);
					}
				});
			}
		});
	} else {
		throw new Error('Unable to find build.gradle');
	}
	
	var propertiesFile = path.join(platformRoot, 'gradle.properties');

	if(fs.existsSync(propertiesFile)) {
		fs.readFile(propertiesFile, 'utf8', function (error, data) {
			if(error) {
				throw new Error('Unable to find gradle.properties: ' + error);
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
