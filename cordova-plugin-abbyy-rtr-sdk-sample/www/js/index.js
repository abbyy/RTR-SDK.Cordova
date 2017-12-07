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
		this.label.innerText = this.current().toFixed(1);
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

var isFlashlightVisible = document.getElementById('isFlashlightVisible');
var isStopButtonVisible = document.getElementById('isStopButtonVisible');
var stopWhenStable = document.getElementById('stopWhenStable');

var areaOfInterestWidth = new Slider('areaOfInterestWidth', 'widthValue');
var areaOfInterestHeight = new Slider('areaOfInterestHeight', 'heightValue');

var abbyyRtrSdkPluginCallback = function(result) {
	console.log(result);
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
}

var textCaptureButton = new Button('textCaptureButton', function() {
	AbbyyRtrSdk.startTextCapture(abbyyRtrSdkPluginCallback, {
		selectableRecognitionLanguages : ["English", "French", "German", "Italian", "Polish", "PortugueseBrazilian",
			"Russian", "ChineseSimplified", "ChineseTraditional", "Japanese", "Korean", "Spanish"],
		recognitionLanguages : ["English"],

		licenseFileName : "AbbyyRtrSdk.license",
		isFlashlightVisible : isFlashlightVisible.checked,
		stopWhenStable : stopWhenStable.checked,
		areaOfInterest : (areaOfInterestWidth.current() + " " + areaOfInterestHeight.current()),
		isStopButtonVisible : isStopButtonVisible.checked,
	});
});

var customDataCaptureButton = new Button('customDataCaptureButton', function() {
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
	});
});

var dataCaptureButton = new Button('dataCaptureButton', function() {
	AbbyyRtrSdk.startDataCapture(abbyyRtrSdkPluginCallback, {
		profile : "MRZ",

		licenseFileName : "AbbyyRtrSdk.license",
		isFlashlightVisible : isFlashlightVisible.checked,
		stopWhenStable : stopWhenStable.checked,
		areaOfInterest : areaOfInterestWidth.current() + " " + areaOfInterestHeight.current(),
		isStopButtonVisible : isStopButtonVisible.checked,
	});
});

app.initialize();
