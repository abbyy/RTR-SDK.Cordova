/// ABBYY® Real-Time Recognition SDK 1 © 2016 ABBYY Production LLC.
/// ABBYY is either a registered trademark or a trademark of ABBYY Software Ltd.

var app = {
	initialize: function() {
		document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
	},

	onDeviceReady: function() {
		this.receivedEvent('deviceready');
	},

	receivedEvent: function(id) {
		var parentElement = document.getElementById(id);
		var listeningElement = parentElement.querySelector('.listening');
		var receivedElement = parentElement.querySelector('.received');

		listeningElement.setAttribute('style', 'display: none;');
		receivedElement.setAttribute('style', 'display: inline-block;');

		console.log('Received Event: ' + id);
	}
};

function Slider(elementId, labelId) {
	this.element = document.getElementById(elementId);
	this.label = document.getElementById(labelId);

	this.current = function() {
		return Number(this.element.value);
	};
	this.update = function() {
		this.label.innerText = this.current().toFixed(2);
	};

	this.element.addEventListener('input', this.update.bind(this));
	this.update();
}

function Button(buttonId, action) {
	this.element = document.getElementById(buttonId);
	this.onTouch = function () {
		action();
	}
	this.element.addEventListener('touchstart', this.onTouch.bind(this));
}

function Orientation() {
	element = document.getElementById('orientation')
	return element.options[element.selectedIndex].text
}

function maxImagesCount() {
	element = document.getElementById('maxImagesCount')
	return element.value
}

function orientationIC() {
	element = document.getElementById('orientationIC')
	return element.options[element.selectedIndex].text
}

function cameraResolution() {
	element = document.getElementById('cameraResolution')
	return element.options[element.selectedIndex].text
}

function exportType() {
	element = document.getElementById('exportType')
	return element.options[element.selectedIndex].text
}

function compressionLevel() {
	element = document.getElementById('compressionLevel')
	return element.options[element.selectedIndex].text
}

function pdfCompressionType() {
	element = document.getElementById('pdfCompressionType')
	return element.options[element.selectedIndex].text
}

function documentSize() {
	element = document.getElementById('documentSize')
	return element.options[element.selectedIndex].text
}

function destination() {
	element = document.getElementById('destination')
	return element.options[element.selectedIndex].text
}

var isFlashlightButtonVisibleIC = document.getElementById('isFlashlightButtonVisibleIC');
var showPreviewIC = document.getElementById('showPreviewIC');
var isCaptureButtonVisible = document.getElementById('isCaptureButtonVisible');
var cropEnabled = document.getElementById('cropEnabled');
var minimumDocumentToViewRatio = new Slider('minimumDocumentToViewRatio', 'minimumDocumentToViewRatioValue');

var isFlashlightVisible = document.getElementById('isFlashlightVisible');
var isStopButtonVisible = document.getElementById('isStopButtonVisible');
var stopWhenStable = document.getElementById('stopWhenStable');

var imageCaptureTab = document.getElementById('tab_settings_ic');
var textCaptureTab = document.getElementById('tab_settings_tc');
var customDataCaptureTab = document.getElementById('tab_settings_regex');
var dataCaptureTab = document.getElementById('tab_settings_mrz');

var areaOfInterestWidth = new Slider('areaOfInterestWidth', 'widthValue');
var areaOfInterestHeight = new Slider('areaOfInterestHeight', 'heightValue');

var abbyyRtrSdkPluginImageCaptureCallback = function(result) {
	console.log(result);
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
	var imageView = document.getElementById('AbbyyRtrSdkPluginCapturedImage')
	if(result.images && result.images[0] && result.images[0].base64) {
		imageView.style.display = "block";
		imageView.src = "data:image/jpeg;base64," + result.images[0].base64;
	} else {
		imageView.style.display = "none";
		imageView.src = null;
	}
}

var abbyyRtrSdkPluginCallback = function(result) {
	console.log(result);
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
}

function imageCapture() {
	AbbyyRtrSdk.startImageCapture(abbyyRtrSdkPluginImageCaptureCallback, {
		licenseFileName : "AbbyyRtrSdk.license", // optional, default=AbbyyRtrSdk.license

		cameraResolution : cameraResolution(), // optional, default=FullHD (HD, FullHD, 4K)
		isFlashlightButtonVisible : isFlashlightButtonVisibleIC.checked, // optional, default=true
		isCaptureButtonVisible : isCaptureButtonVisible.checked, // optional, default=false
		orientation: orientationIC(), // optional, default=default
		showPreview: showPreviewIC.checked, // optional, default=false
		maxImagesCount: maxImagesCount(), // optional, default=0

		destination : destination(), // optional, captured image will be saved to corresponding file ("file") or returned as encode base64 image string ("base64"). default=file
		exportType : exportType(), // optional, default=jpg (jpg, png, pdf).
		pdfCompressionType : pdfCompressionType(), // optional, default=jpg (jpg)
		compressionLevel : compressionLevel(), // optional, default=Normal (Low, Normal, High, ExtraHigh)

		defaultImageSettings : {
			minimumDocumentToViewRatio : minimumDocumentToViewRatio.current(), // optional, minimum document area relative to frame area - 0...1. Default 0.15.
			documentSize : documentSize(), // optional, document size in millimeters. default=Any.
			cropEnabled : cropEnabled.checked, // optional, default=true
		},
	});
}

function textCapture() {
	AbbyyRtrSdk.startTextCapture(abbyyRtrSdkPluginCallback, {
		selectableRecognitionLanguages : ["English", "French", "German", "Italian", "Polish", "PortugueseBrazilian",
			"Russian", "ChineseSimplified", "ChineseTraditional", "Japanese", "Korean", "Spanish"],
		recognitionLanguages : ["English"],

		licenseFileName : "AbbyyRtrSdk.license",
		isFlashlightVisible : isFlashlightVisible.checked,
		stopWhenStable : stopWhenStable.checked,
		areaOfInterest : (areaOfInterestWidth.current() + " " + areaOfInterestHeight.current()),
		isStopButtonVisible : isStopButtonVisible.checked,
		orientation: Orientation(),
	});
}

function customDataCapture() {
	AbbyyRtrSdk.startDataCapture(abbyyRtrSdkPluginCallback, {
		customDataCaptureScenario : {
			name : "Code",
			description : "Mix of digits with letters:  X6YZ64  32VPA  zyy777",
			recognitionLanguages : ["English"],
			fields : [ {
				regEx : "([a-zA-Z]+[0-9]+|[0-9]+[a-zA-Z]+)[0-9a-zA-Z]*"
			} ]
		},

		licenseFileName : "AbbyyRtrSdk.license",
		isFlashlightVisible : isFlashlightVisible.checked,
		stopWhenStable : stopWhenStable.checked,
		areaOfInterest : areaOfInterestWidth.current() + " " + areaOfInterestHeight.current(),
		isStopButtonVisible : isStopButtonVisible.checked,
		orientation: Orientation(),
	});
}

function dataCapture() {
	AbbyyRtrSdk.startDataCapture(abbyyRtrSdkPluginCallback, {
		profile : "MRZ",

		licenseFileName : "AbbyyRtrSdk.license",
		isFlashlightVisible : isFlashlightVisible.checked,
		stopWhenStable : stopWhenStable.checked,
		areaOfInterest : areaOfInterestWidth.current() + " " + areaOfInterestHeight.current(),
		isStopButtonVisible : isStopButtonVisible.checked,
		orientation: Orientation(),
	});
}

var startCaptureButton = new Button('startCaptureButton', function() {
	if(imageCaptureTab.checked) {
		imageCapture();
	} else if(textCaptureTab.checked) {
		textCapture();
	} else if(customDataCaptureTab.checked) {
		customDataCapture();
	} else if(dataCaptureTab.checked) {
		dataCapture();
	}
});

app.initialize();
