// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

// Due to folder hierarchy in android project it is necessary to move assets from 'platforms/android/assets/www/rtr_assets/' to 'platforms/android/assets/'.
// Alseo we need to rename folders to lowercase.

module.exports = function (ctx) {
	if(ctx.opts.platforms.indexOf('android') < 0) {
		return;
	}

	var fs = ctx.requireCordovaModule('fs'),
		rimraf = ctx.requireCordovaModule('rimraf'),
		path = ctx.requireCordovaModule('path');

	var src = path.join(ctx.opts.projectRoot, 'platforms/android/assets/www/rtr_assets/');
	var dst = path.join(ctx.opts.projectRoot, 'platforms/android/assets/');

	var callback = function (message) {
		if(message) {
			console.log('assets error' + message);
		}
	}

	rimraf.sync(path.join(dst, '!(www)*'));

	fs.readdirSync(src).forEach(function (file, index) {
		if(fs.lstatSync(path.join(src, file)).isDirectory()) {
			fs.rename(path.join(src, file), path.join(dst, file.toLowerCase()), callback);
		} else {
			fs.rename(path.join(src, file), path.join(dst, file), callback);
		}
	})
};
