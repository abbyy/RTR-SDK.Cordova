// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

// Due to folder hierarchy in android project it is necessary to move assets from 'platforms/android/assets/www/rtr_assets/' to 'platforms/android/assets/'.

module.exports = function (ctx) {
	if(ctx.opts.platforms.indexOf('android') < 0) {
		return;
	}

	var fs = require('fs'),
		rimraf = require('rimraf'),
		path = require('path');

	var assetsPath = 'platforms/android/app/src/main/assets/'; // cordova-android >= 7

	return fs.stat(path.join(ctx.opts.projectRoot, assetsPath), function (error) {
		if(error) {
			assetsPath = 'platforms/android/assets/'; // cordova-android < 7
		}

		var src = path.join(ctx.opts.projectRoot, assetsPath, '/www/rtr_assets/');
		var dst = path.join(ctx.opts.projectRoot, assetsPath);

		var callback = function (message) {
			if(message) {
				console.log('assets error' + message);
			}
		};

		rimraf.sync(path.join(dst, '!(www)*'));

		fs.readdirSync(src).forEach(function (file, index) {
			fs.rename(path.join(src, file), path.join(dst, file), callback);
		});
	});
};
