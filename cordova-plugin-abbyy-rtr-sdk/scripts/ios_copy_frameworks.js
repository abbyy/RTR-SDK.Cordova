// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

var xcode = require('xcode');
var fs = require('fs');
var path = require('path');

const xcodeprojPath = findInDirectory('platforms/ios', '.xcodeproj');
const pbxprojPath = xcodeprojPath + '/project.pbxproj';
const pbxproj = xcode.project(pbxprojPath);

var options = { shellPath: '/bin/sh', shellScript: '/bin/sh "../../libs/ios/copy_frameworks.sh"' };

var frameworksPath = {
	path:'../../libs/ios',
	dirname: '../../libs/ios',
	customFramework: true
}

pbxproj.parse(function(err) {
	pbxproj.addBuildPhase([], 'PBXShellScriptBuildPhase', 'Copy Frameworks', pbxproj.getFirstTarget().uuid, options);
	pbxproj.addToFrameworkSearchPaths(frameworksPath);
	fs.writeFileSync(pbxprojPath, pbxproj.writeSync());
})

function findInDirectory(startPath, filter) {
	if(!fs.existsSync(startPath)) {
		console.log("No directory: ", startPath);
		return;
	}
	const files = fs.readdirSync(startPath);
	var resultFiles = [];
	for(var i = 0; i < files.length; i++) {
		var filename = path.join(startPath, files[i]);
		if(filename.indexOf(filter) >= 0) {
			return filename;
		}
	}
}
