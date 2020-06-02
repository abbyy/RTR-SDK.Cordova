// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

var xcode = require('xcode');
var fs = require('fs');
var path = require('path');

const xcodeprojPath = findInDirectory('platforms/ios', '.xcodeproj');
const pbxprojPath = xcodeprojPath + '/project.pbxproj';
const pbxproj = xcode.project(pbxprojPath);

var optionsLibs = { shellPath: '/bin/sh', shellScript: '/bin/sh "../../libs/ios/copy_frameworks.sh"' };
var optionsAssets = { shellPath: '/bin/sh', shellScript: '[ -d "$BUILT_PRODUCTS_DIR/$FULL_PRODUCT_NAME/www/rtr_assets" ] && rsync -Lrav "$BUILT_PRODUCTS_DIR/$FULL_PRODUCT_NAME/www/rtr_assets/" "$TARGET_BUILD_DIR/$WRAPPER_NAME"' };

var frameworksPath = {
	path:'../../libs/ios',
	dirname: '../../libs/ios',
	customFramework: true
}

pbxproj.parse(function(err) {
	let copyFrameworksPhaseName = 'Copy ABBYY Frameworks';
	let copyAssetsPhaseName = 'Copy ABBYY Assets';
	let requiredBuildPhases = new Set();
	requiredBuildPhases.add(copyAssetsPhaseName);
	requiredBuildPhases.add(copyFrameworksPhaseName)

	var buildPhases = pbxproj.getFirstTarget().firstTarget.buildPhases;
	buildPhases.forEach(phase => requiredBuildPhases.delete(phase.comment));

	if(requiredBuildPhases.has(copyFrameworksPhaseName)) {
		pbxproj.addBuildPhase([], 'PBXShellScriptBuildPhase', copyFrameworksPhaseName, pbxproj.getFirstTarget().uuid, optionsLibs);	
		pbxproj.addToFrameworkSearchPaths(frameworksPath);
		console.log(copyFrameworksPhaseName, ' successfully added');
	} else {
		console.log(copyFrameworksPhaseName, ' phase already added. Skipping');
	}
	if(requiredBuildPhases.has(copyAssetsPhaseName)) {
		pbxproj.addBuildPhase([], 'PBXShellScriptBuildPhase', copyAssetsPhaseName, pbxproj.getFirstTarget().uuid, optionsAssets);
		console.log(copyAssetsPhaseName, ' successfully added');
	} else {
		console.log(copyAssetsPhaseName, ' phase already added. Skipping');
	}
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
