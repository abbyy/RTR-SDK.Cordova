/// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
/// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

var app = {
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        ShowICSettings();
        this.receivedEvent('deviceready');
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

function orientation() {
    element = document.getElementById('orientation')
    return element.options[element.selectedIndex].text
}

function requiredPageCount() {
    element = document.getElementById('requiredPageCount')
    return element.value
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

function documentSize() {
    element = document.getElementById('documentSize')
    return element.options[element.selectedIndex].text
}

function destination() {
    element = document.getElementById('destination')
    return element.options[element.selectedIndex].text
}

function angle() {
    element = document.getElementById('angle')
    return element.options[element.selectedIndex].text
}

function imageFromGalleryMaxSize() {
	element = document.getElementById('imageFromGalleryMaxSize')
	return element.value
}


var showPreview = document.getElementById('showPreview');
var isCaptureButtonVisible = document.getElementById('isCaptureButtonVisible');
var isGalleryButtonVisible = document.getElementById('isGalleryButtonVisible');
var aspectRatioMin = new Slider('aspectRatioMin', 'aspectRatioMinValue');
var aspectRatioMax = new Slider('aspectRatioMax', 'aspectRatioMaxValue');
var minimumDocumentToViewRatio = new Slider('minimumDocumentToViewRatio', 'minimumDocumentToViewRatioValue');

var isFlashlightVisible = document.getElementById('isFlashlightVisible');
var isStopButtonVisible = document.getElementById('isStopButtonVisible');
var stopWhenStable = document.getElementById('stopWhenStable');

var imageCaptureTab = document.getElementById('tab_settings_ic');
var textCaptureTab = document.getElementById('tab_settings_tc');
var customDataCaptureTab = document.getElementById('tab_settings_regex');
var dataCaptureMRZTab = document.getElementById('tab_settings_mrz');
var dataCaptureBCRTab = document.getElementById('tab_settings_bcr');
var coreApiTab = document.getElementById('tab_settings_coreapi');
var imgCoreApiTab = document.getElementById('tab_settings_imaging');
var scenarios = document.getElementsByName("scenario");
for (var i = 0; i < scenarios.length; i++) {
    scenarios[i].addEventListener('change', () => {
        changeScenario();
    });
}

function changeScenario() {
    if (scenarios[0].checked == true) {
        ShowICSettings();
    } else if (scenarios[5].checked == true) {
        ShowCoreAPISettings();
    } else if (scenarios[6].checked == true) {
        ShowImgCoreAPISettings();
    } else {
        ShowSettings();
    }
}

var areaOfInterestWidth = new Slider('areaOfInterestWidth', 'widthValue');
var areaOfInterestHeight = new Slider('areaOfInterestHeight', 'heightValue');

function ShowICSettings() {
    document.getElementById('setting_orientation').style.display = 'block';
    document.getElementById('setting_flashlight_visible').style.display = 'block';
    document.getElementById('setting_show_preview').style.display = 'block';
    document.getElementById('setting_capture_visible').style.display = 'block';
    document.getElementById('setting_gallery_visible').style.display = 'block';
    document.getElementById('setting_page_count').style.display = 'block';
    document.getElementById('setting_resolution').style.display = 'block';
    document.getElementById('setting_destination').style.display = 'block';
    document.getElementById('setting_export_type').style.display = 'block';
    document.getElementById('setting_compression').style.display = 'block';
    document.getElementById('setting_default_ic').style.display = 'block';
    document.getElementById('setting_stop_visible').style.display = 'none';
    document.getElementById('setting_stop_when_stable').style.display = 'none';
    document.getElementById('setting_area_of_interest').style.display = 'none';
    document.getElementById('setting_recognition_type').style.display = 'none';
    document.getElementById('setting_languages').style.display = 'none';
    document.getElementById('setting_text_orientation_detection_enabled').style.display = 'none';
    document.getElementById('setting_operation_type').style.display = 'none';
    document.getElementById('setting_pick_image').style.display = 'none';
    document.getElementById('setting_share').style.display = 'none';
    document.getElementById('setting_start_capture').style.display = 'block';
    document.getElementById('setting_angle').style.display = 'none';
}

function ShowSettings() {
    document.getElementById('setting_orientation').style.display = 'none';
    document.getElementById('setting_flashlight_visible').style.display = 'none';
    document.getElementById('setting_show_preview').style.display = 'none';
    document.getElementById('setting_capture_visible').style.display = 'none';
    document.getElementById('setting_gallery_visible').style.display = 'none';
    document.getElementById('setting_page_count').style.display = 'none';
    document.getElementById('setting_resolution').style.display = 'none';
    document.getElementById('setting_destination').style.display = 'none';
    document.getElementById('setting_export_type').style.display = 'none';
    document.getElementById('setting_compression').style.display = 'none';
    document.getElementById('setting_default_ic').style.display = 'none';
    document.getElementById('setting_stop_visible').style.display = 'block';
    document.getElementById('setting_stop_when_stable').style.display = 'block';
    document.getElementById('setting_area_of_interest').style.display = 'block';
    document.getElementById('setting_recognition_type').style.display = 'block';
    document.getElementById('setting_languages').style.display = 'block';
    document.getElementById('setting_text_orientation_detection_enabled').style.display = 'none';
    document.getElementById('setting_operation_type').style.display = 'none';
    document.getElementById('setting_pick_image').style.display = 'none';
    document.getElementById('setting_share').style.display = 'none';
    document.getElementById('setting_start_capture').style.display = 'block';
    document.getElementById('setting_angle').style.display = 'none';
}

function ShowCoreAPISettings() {
    document.getElementById('setting_orientation').style.display = 'none';
    document.getElementById('setting_flashlight_visible').style.display = 'none';
    document.getElementById('setting_show_preview').style.display = 'none';
    document.getElementById('setting_capture_visible').style.display = 'none';
    document.getElementById('setting_gallery_visible').style.display = 'none';
    document.getElementById('setting_page_count').style.display = 'none';
    document.getElementById('setting_resolution').style.display = 'none';
    document.getElementById('setting_destination').style.display = 'none';
    document.getElementById('setting_export_type').style.display = 'none';
    document.getElementById('setting_compression').style.display = 'none';
    document.getElementById('setting_default_ic').style.display = 'none';
    document.getElementById('setting_stop_visible').style.display = 'none';
    document.getElementById('setting_stop_when_stable').style.display = 'none';
    document.getElementById('setting_area_of_interest').style.display = 'none';
    document.getElementById('setting_recognition_type').style.display = 'block';
    document.getElementById('setting_languages').style.display = 'block';
    document.getElementById('setting_text_orientation_detection_enabled').style.display = 'block';
    document.getElementById('setting_operation_type').style.display = 'none';
    document.getElementById('setting_pick_image').style.display = 'block';
    document.getElementById('setting_share').style.display = 'block';
    document.getElementById('setting_start_capture').style.display = 'none';
    document.getElementById('setting_angle').style.display = 'none';
}

function ShowImgCoreAPISettings() {
    document.getElementById('setting_orientation').style.display = 'none';
    document.getElementById('setting_flashlight_visible').style.display = 'none';
    document.getElementById('setting_show_preview').style.display = 'none';
    document.getElementById('setting_capture_visible').style.display = 'none';
    document.getElementById('setting_gallery_visible').style.display = 'none';
    document.getElementById('setting_page_count').style.display = 'none';
    document.getElementById('setting_resolution').style.display = 'none';
    document.getElementById('setting_destination').style.display = 'block';
    document.getElementById('setting_export_type').style.display = 'block';
    document.getElementById('setting_compression').style.display = 'block';
    document.getElementById('setting_default_ic').style.display = 'none';
    document.getElementById('setting_stop_visible').style.display = 'none';
    document.getElementById('setting_stop_when_stable').style.display = 'none';
    document.getElementById('setting_area_of_interest').style.display = 'none';
    document.getElementById('setting_recognition_type').style.display = 'none';
    document.getElementById('setting_languages').style.display = 'none';
    document.getElementById('setting_text_orientation_detection_enabled').style.display = 'none';
    document.getElementById('setting_operation_type').style.display = 'block';
    document.getElementById('setting_pick_image').style.display = 'block';
    document.getElementById('setting_share').style.display = 'block';
    document.getElementById('setting_start_capture').style.display = 'none';
    document.getElementById('setting_angle').style.display = 'block';
}

const truncateBase64String = dict => {
    if (dict.imageUri && dict.imageUri.length > 300) {
        return {
            ...dict,
        imageUri:
            dict.imageUri.substring(0, 50) + ' ... length: ' + dict.imageUri.length,
        };
    }
    if (dict.base64) {
        return {
            ...dict,
        base64:
            dict.base64.substring(0, 50) + ' ... length: ' + dict.base64.length,
        };
    }

    if (dict.settings) {
        return {...dict, settings: truncateBase64String(dict.settings)};
    }
    if (dict.images) {
        let truncated = [];
        for (let image of dict.images) {
            truncated.push(truncateBase64String(image));
        }
        return {...dict, images: truncated};
    }
    return dict;
};

var abbyyRtrSdkPluginImageCaptureCallback = function(result) {
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(truncateBase64String(result), null, 2);
    var imageView = document.getElementById('AbbyyRtrSdkPluginCapturedImage')
    if (result.images && result.images[0]) {
        if (result.images[0].base64) {
            imageView.style.display = 'block';
            imageView.src = 'data:image/jpeg;base64,' + result.images[0].base64;
        } else {
            imageView.style.display = 'block';
            imageView.src = 'file://'+result.images[0].filePath;
        }
    }
}

var abbyyRtrSdkPluginCallback = function(result) {
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(truncateBase64String(result), null, 2);
}

var abbyyRtrSdkDetectBoundaryPluginCallback = function(res) {
    var result = res.result;
    document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
    if (res.imageUri) {
        var canvas = document.getElementById('AbbyyRtrSdkPluginCapturedImageCanvas');
        var ctx = canvas.getContext("2d");
        var img = new Image;
        if (result.documentBoundary) {
            img.onload = function() {
                var scale = drawRotatedImage(img, ctx, canvas, 0);
                ctx.fillStyle = "rgba(0, 255, 255, 0.2)";
                var documentBoundary = result.documentBoundary;
                var path=new Path2D();
                var size = documentBoundary.length;
                path.moveTo(documentBoundary[size-1].x * scale, documentBoundary[size-1].y * scale);
                documentBoundary.forEach(point => {
                    path.lineTo(point.x * scale, point.y * scale);
                });
                ctx.fill(path);
            };
        }
        img.src = res.imageUri;
    }
};

var abbyyRtrSdkAssessQualityPluginCallback = function(res) {
    var result = res.result;
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
	if (res.imageUri) {
        var canvas = document.getElementById('AbbyyRtrSdkPluginCapturedImageCanvas');
        var ctx = canvas.getContext("2d");
        var img = new Image;
        if (result.qualityAssessmentForOcrBlocks) {
            img.onload = function() {
                var scale = drawRotatedImage(img, ctx, canvas, 0);
                result.qualityAssessmentForOcrBlocks.forEach(block => {
                    ctx.fillStyle = "rgba(0, 255, 0, "+ 0.8 * block.quality / 100+")";
                    var x = block.rect.left;
                    var y = block.rect.top;
                    var width = block.rect.right - x;
                    var height = block.rect.bottom - y;
                    ctx.fillRect(x * scale, y * scale, width * scale, height * scale);
                })
            };
        }
        img.src = res.imageUri;
    }
};

function drawRotatedImage(img, ctx, canvas, orientation) {
    var origW = img.naturalWidth;
    var origH = img.naturalHeight;
    if (orientation % 180 == 90) {
        var temp = origW;
        origW = origH;
        origH = temp;
    }
    var width = canvas.width;
    var scale = width / origW;
    var height = origH * scale;
    canvas.height = height;
    ctx.clearRect(0, 0, width, height);
    if (orientation != 0) {
        ctx.save();
        ctx.translate(width / 2, height / 2);

        ctx.rotate(-orientation * Math.PI /180);
        if (orientation % 180 == 90) {
            ctx.drawImage(img, -height / 2, -width / 2, height, width);
        } else {
            ctx.drawImage(img, -width / 2, -height / 2, width, height);
        }
        ctx.restore();
    } else {
        ctx.drawImage(img, 0, 0, width, height);
    }
    return scale;
}

var abbyyRtrSdkRecognizeTextPluginCallback = function(res) {
    var result = res.result;
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
	if (res.imageUri) {
        var canvas = document.getElementById('AbbyyRtrSdkPluginCapturedImageCanvas');
        var ctx = canvas.getContext("2d");
        var img = new Image;
        var orientation = result.orientation;
        if (result.textBlocks) {
            img.onload = function() {
                var scale = drawRotatedImage(img, ctx, canvas, orientation);
                ctx.fillStyle = "rgba(0, 255, 0, 0.4)";
                result.textBlocks.forEach(block => {
                    if (block.textLines) {
                        block.textLines.forEach(line => {
                            var x = line.rect.left;
                            var y = line.rect.top;
                            ctx.strokeText(line.text, x * scale, y * scale);
                            var width = line.rect.right - x;
                            var height = line.rect.bottom - y;
                            ctx.fillRect(x * scale, y * scale, width * scale, height * scale);
                        });
                    }
                })
            };
        }
        img.src = res.imageUri;
    }
};

var abbyyRtrSdkExtractDataPluginCallback = function(res) {
    var result = res.result;
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(result, null, 2);
	if (res.imageUri) {
        var canvas = document.getElementById('AbbyyRtrSdkPluginCapturedImageCanvas');
        var ctx = canvas.getContext("2d");
        var img = new Image;
        var orientation = result.orientation;
        if (result.dataFields) {
            img.onload = function() {
                var scale = drawRotatedImage(img, ctx, canvas, orientation);
                ctx.fillStyle = "rgba(0, 255, 0, 0.4)";
                result.dataFields.forEach(field => {
                    var x = field.rect.left;
                    var y = field.rect.top;
                    ctx.strokeText(field.name, x * scale, y * scale);
                    ctx.strokeText(field.text, x * scale, field.rect.bottom * scale);
                    var width = field.rect.right - x;
                    var height = field.rect.bottom - y;
                    ctx.fillRect(x * scale, y * scale, width * scale, height * scale);
                });
            };
        }
        img.src = res.imageUri;
    }
};

var shareFileUri = '';

var abbyyRtrSdkPluginExportCallback = function(result) {
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(truncateBase64String(result), null, 2);
	var imageView = document.getElementById('AbbyyRtrSdkPluginCapturedImage');
	if (result.imageUri) {
		imageView.style.display = 'block';
		imageView.src = result.imageUri;
		shareFileUri = result.imageUri;
	}
}

var abbyyRtrSdkPluginExportPDFCallback = function(result) {
	document.getElementById('AbbyyRtrSdkPluginResult').innerText = JSON.stringify(truncateBase64String(result), null, 2);
	if (result.pdfUri) {
		shareFileUri = result.pdfUri;
	}
}

function imageCapture() {
    AbbyyRtrSdk.startImageCapture(abbyyRtrSdkPluginImageCaptureCallback, {
        licenseFileName : 'AbbyyRtrSdk.License', // optional, default=AbbyyRtrSdk.License

        cameraResolution : cameraResolution(), // optional, default=FullHD (HD, FullHD, 4K)
        isFlashlightButtonVisible : isFlashlightButtonVisible.checked, // optional, default=true
        isCaptureButtonVisible : isCaptureButtonVisible.checked, // optional, default=false
        isGalleryButtonVisible : isGalleryButtonVisible.checked, // optional, default=false
        orientation : orientation(), // optional, default=default
        showPreview : showPreview.checked, // optional, default=false
        requiredPageCount : requiredPageCount(), // optional, default=0

        destination : destination(), // optional, captured image will be saved to corresponding file ('file') or returned as encode base64 image string ('base64'). default=file
        exportType : exportType(), // optional, default=jpg (jpg, png, pdf).
        compressionLevel : compressionLevel(), // optional, default=Normal (Low, Normal, High, ExtraHigh)

		defaultImageSettings : {
			aspectRatioMin : aspectRatioMin.current(), // optional, minimum aspect ratio of the document. Default 0.
			aspectRatioMax : aspectRatioMax.current(), // optional, maximum aspect ratio of the document. Default 0.
			minimumDocumentToViewRatio : minimumDocumentToViewRatio.current(), // optional, minimum document area relative to frame area - 0...1. Default 0.15.
			documentSize : documentSize(), // optional, document size in millimeters. default=Any.
			imageFromGalleryMaxSize : imageFromGalleryMaxSize(), // optional, default=4096
		},
	});
}

function textCapture() {
    AbbyyRtrSdk.startTextCapture(abbyyRtrSdkPluginCallback, {
        selectableRecognitionLanguages : ['English', 'French', 'German', 'Italian', 'Polish', 'PortugueseBrazilian',
            'Russian', 'ChineseSimplified', 'ChineseTraditional', 'Japanese', 'Korean', 'Spanish'],
        recognitionLanguages : ['English'],

        licenseFileName : 'AbbyyRtrSdk.License',
        isFlashlightVisible : isFlashlightVisible.checked,
        stopWhenStable : stopWhenStable.checked,
        areaOfInterest : (areaOfInterestWidth.current() + ' ' + areaOfInterestHeight.current()),
        isStopButtonVisible : isStopButtonVisible.checked,
        orientation : orientation(),
    });
}

function customDataCapture() {
    AbbyyRtrSdk.startDataCapture(abbyyRtrSdkPluginCallback, {
        customDataCaptureScenario : {
            name : 'Code',
            description : 'Mix of digits with letters:  X6YZ64  32VPA  zyy777',
            recognitionLanguages : ['English'],
            fields : [ {
                regEx : '([a-zA-Z]+[0-9]+|[0-9]+[a-zA-Z]+)[0-9a-zA-Z]*'
            } ]
        },

        licenseFileName : 'AbbyyRtrSdk.License',
        isFlashlightVisible : isFlashlightVisible.checked,
        stopWhenStable : stopWhenStable.checked,
        areaOfInterest : areaOfInterestWidth.current() + ' ' + areaOfInterestHeight.current(),
        isStopButtonVisible : isStopButtonVisible.checked,
        orientation : orientation(),
    });
}

function dataCaptureMRZ() {
    AbbyyRtrSdk.startDataCapture(abbyyRtrSdkPluginCallback, {
        profile : 'MRZ',

        licenseFileName : 'AbbyyRtrSdk.License',
        isFlashlightVisible : isFlashlightVisible.checked,
        stopWhenStable : stopWhenStable.checked,
        areaOfInterest : areaOfInterestWidth.current() + ' ' + areaOfInterestHeight.current(),
        isStopButtonVisible : isStopButtonVisible.checked,
        orientation: orientation()
    });
}

function dataCaptureBCR() {
    AbbyyRtrSdk.startDataCapture(abbyyRtrSdkPluginCallback, {
        profile : 'BusinessCards',

        licenseFileName : 'AbbyyRtrSdk.License',
        isFlashlightVisible : isFlashlightVisible.checked,
        stopWhenStable : stopWhenStable.checked,
        areaOfInterest : areaOfInterestWidth.current() + ' ' + areaOfInterestHeight.current(),
        isStopButtonVisible : isStopButtonVisible.checked,
        orientation : orientation(),
        recognitionLanguages : ['English'],
    });
}

var startCaptureButton = new Button('startCaptureButton', function() {
    if (imageCaptureTab.checked) {
        imageCapture();
    } else if (textCaptureTab.checked) {
        textCapture();
    } else if (customDataCaptureTab.checked) {
        customDataCapture();
    } else if (dataCaptureMRZTab.checked) {
        dataCaptureMRZ();
    } else if (dataCaptureBCRTab.checked) {
        dataCaptureBCR();
    }
});

languagesSelect = document.getElementById('coreApiLanguagesSelect');
languagesSelect.addEventListener('change', (event) => {
    document.getElementById('selectedLanguagesValue').innerText = getSelectValues(languagesSelect).map(value => {
        return value.substring(0, 2);
    }).join(',');
});

operationsSelect = document.getElementById('operationType');
operationsSelect.addEventListener('change', (event) => {
    switch (event.target.value) {
        case 'crop':
        case 'rotate':
        case 'detect_boundary':
        case 'assess_quality':
        case 'export':
    }
});

function SelectedRecognitionType() {
    element = document.getElementById('recognitionType')
    return element.options[element.selectedIndex].value
}

function SelectedOperationType() {
    element = document.getElementById('operationType')
    return element.options[element.selectedIndex].value
}

function getSelectValues(select) {
    var result = [];
    var options = select && select.options;
    var opt;

    for (var i = 0; i < options.length; i++) {
        opt = options[i];

        if (opt.selected) {
            result.push(opt.value || opt.text);
        }
    }
    return result;
}

var shareButton = new Button('shareButton', function() {
    var options = {
      message: 'Export file', // not supported on some apps (Facebook, Instagram)
      files: [shareFileUri],
      chooserTitle: 'Pick an app' // Android only, you can override the default share sheet title
    };
    var onSuccess = function(result) {
       console.log("Share completed " + result.completed); // On Android apps mostly return false even while it's true
       console.log("Shared to app: " + result.app); // On Android result.app is currently empty. On iOS it's empty when sharing is cancelled (result.completed=false)
     }

     var onError = function(msg) {
       console.log("Sharing failed with message: " + msg);
     }

     window.plugins.socialsharing.shareWithOptions(options, onSuccess, onError);
});

var pickImageButton = new Button('pickImageButton', function() {
    if (imgCoreApiTab.checked) {
        var selectedOperationType = SelectedOperationType();
        if (selectedOperationType.localeCompare('exportPDF') === 0) {
            AbbyyRtrSdk.startImageCapture(function(result) {
                if (!result.images) {
                    abbyyRtrSdkPluginCallback(result);
                }
                var images = result.images.map(image => {
                    return {
                        imageUri: 'file://'+image.filePath
                    };
                });
                AbbyyRtrSdk.exportImagesToPdf({
                    images: images,
                }, abbyyRtrSdkPluginExportPDFCallback, abbyyRtrSdkPluginCallback);
            }, {
                destination : 'file'
            });
            return;
        }
    }
    AbbyyRtrSdk.startImageCapture(function(result) {
        if (!result.images) {
            abbyyRtrSdkPluginCallback(result);
        }
		var imageUri = result.resultInfo.uriPrefix + result.images[0].base64;
        if (coreApiTab.checked) {
            var recognitionLanguages = getSelectValues(languagesSelect);
            var selectedRecognitionType = SelectedRecognitionType();
            var textOrientationDetectionEnabled = document.getElementById('textOrientationDetectionEnabled').checked;

            if (selectedRecognitionType.localeCompare('text') === 0) {
                AbbyyRtrSdk.recognizeText({
                    imageUri: imageUri,
                    recognitionLanguages: recognitionLanguages,
                    textOrientationDetectionEnabled: textOrientationDetectionEnabled
                }, (result) => {
                    abbyyRtrSdkRecognizeTextPluginCallback({
                        result: result,
                        imageUri: imageUri
                    });
                }, abbyyRtrSdkPluginCallback);
            } else if (selectedRecognitionType.localeCompare('bcr') === 0) {
                AbbyyRtrSdk.extractData({
                    imageUri: imageUri,
                    profile: 'BusinessCards',
                    recognitionLanguages: recognitionLanguages,
                    textOrientationDetectionEnabled: textOrientationDetectionEnabled
                }, (result) => {
                    abbyyRtrSdkExtractDataPluginCallback({
                        result: result,
                        imageUri: imageUri
                    });
                }, abbyyRtrSdkPluginCallback);
            }
        } else if (imgCoreApiTab.checked) {
            var selectedOperationType = SelectedOperationType();

            if (selectedOperationType.localeCompare('crop') === 0) {
                AbbyyRtrSdk.detectDocumentBoundary({
                    imageUri: imageUri
                }, result => {
                    if (result.documentBoundary) {
                        AbbyyRtrSdk.cropImage({
                            imageUri: imageUri,
                            result: {
                                destination : destination(),
                                exportType : exportType(),
                                compressionLevel : compressionLevel(),
                            },
                            documentBoundary: result.documentBoundary,
                            documentSize: result.documentSize,
                        }, abbyyRtrSdkPluginExportCallback, abbyyRtrSdkPluginCallback);
                    } else {
                        abbyyRtrSdkPluginCallback(result);
                    }
                }, abbyyRtrSdkPluginCallback);
            } else if (selectedOperationType.localeCompare('rotate') === 0) {
                AbbyyRtrSdk.rotateImage({
                    imageUri: imageUri,
                    angle: angle(),
                    result: {
                        destination : destination(),
                        exportType : exportType(),
                        compressionLevel : compressionLevel(),
                    },
                }, abbyyRtrSdkPluginExportCallback, abbyyRtrSdkPluginCallback);
            } else if (selectedOperationType.localeCompare('export') === 0) {
                AbbyyRtrSdk.exportImage({
                    imageUri: imageUri,
                    result: {
                        destination : destination(),
                        exportType : exportType(),
                        compressionLevel : compressionLevel(),
                    },
                }, abbyyRtrSdkPluginExportCallback, abbyyRtrSdkPluginCallback);
            } else if (selectedOperationType.localeCompare('detect_boundary') === 0) {
                AbbyyRtrSdk.detectDocumentBoundary({
                    imageUri: imageUri
                }, (result) => {
                    abbyyRtrSdkDetectBoundaryPluginCallback({
                        result: result,
                        imageUri: imageUri
                    });
                }, abbyyRtrSdkPluginCallback);
            } else if (selectedOperationType.localeCompare('assess_quality') === 0) {
                AbbyyRtrSdk.assessQualityForOcr({
                    imageUri: imageUri
                }, (result) => {
                   abbyyRtrSdkAssessQualityPluginCallback({
                       result: result,
                       imageUri: imageUri
                   });
               }, abbyyRtrSdkPluginCallback);
            }
        }
    }, {
		requiredPageCount : 1,
		destination : 'base64'
    });
});

app.initialize();
