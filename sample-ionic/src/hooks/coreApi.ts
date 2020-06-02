// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import { useState } from "react";
import { ApiError, TextRecognitionOptions, TextRecognitionResult,
  DataExtractionOptions, DataExtractionResult,
  DetectDocumentBoundaryOptions, DetectDocumentBoundaryResult,
  AssessQualityForOcrOptions, AssessQualityForOcrResult,
  CropImageOptions, CropImageResult,
  RotateImageOptions, RotateImageResult,
  ExportImageOptions, ExportImageResult,
  ExportImagePdfOptions, ExportImagePdfResult } from "./coreApi_types";

declare let AbbyyRtrSdk: any;

export function UseCoreAPI() {
  const [result, setResult] = useState<any>();
  const [imageResult, setImageResult] = useState<string>();
  const [inProgress, setInProgress] = useState<boolean>();

  const onError = (_error: ApiError) => {
    setResult(_error);
    setImageResult(undefined);
    setInProgress(false);
  };

  const recognizeText = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.recognizeText({
        imageUri: imageUri,
        licenseFileName: "MobileCapture.License",
        recognitionLanguages: ['English', 'Russian']
      } as TextRecognitionOptions,
      (result: TextRecognitionResult) => {
        setResult(result);
        setImageResult(imageUri);
        setInProgress(false);
      }, onError
    );
  };

  const extractData = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.extractData({
        imageUri: imageUri,
        profile: 'BusinessCards',
        licenseFileName: "MobileCapture.License",
        recognitionLanguages: ['English', 'Russian']
      } as DataExtractionOptions,
      (result: DataExtractionResult) => {
        setResult(result);
        setImageResult(imageUri);
        setInProgress(false);
      }, onError
    );
  };

  const detectDocumentBoundary = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.detectDocumentBoundary({
        imageUri: imageUri,
        detectionMode: 'Fast',
        licenseFileName: "MobileCapture.License"
      } as DetectDocumentBoundaryOptions,
      (result: DetectDocumentBoundaryResult) => {
        setResult(result);
        setImageResult(imageUri);
        setInProgress(false);
      }, onError
    );
  };

  const assessQualityForOcr = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.assessQualityForOcr({
        imageUri: imageUri,
        licenseFileName: "MobileCapture.License"
      } as AssessQualityForOcrOptions,
      (result: AssessQualityForOcrResult) => {
        setResult(result);
        setImageResult(imageUri);
        setInProgress(false);
      }, onError
    );
  };

  const cropImage = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.detectDocumentBoundary({
        imageUri: imageUri
      } as DetectDocumentBoundaryOptions,
      (result: DetectDocumentBoundaryResult) => {
        AbbyyRtrSdk.cropImage({
            imageUri: imageUri,
            documentBoundary: result.documentBoundary,
            licenseFileName: "MobileCapture.License"
          } as CropImageOptions,
          (result: CropImageResult) => {
            setResult(result);
            setImageResult(result.imageUri);
            setInProgress(false);
          }, onError
        );
      }, onError
    );
  };

  const rotateImage = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.rotateImage({
        imageUri: imageUri,
        angle: 90,
        licenseFileName: "MobileCapture.License"
      } as RotateImageOptions,
      (result: RotateImageResult) => {
        setResult(result);
        setImageResult(result.imageUri);
        setInProgress(false);
      }, onError
    );
  };

  const exportImage = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.exportImage({
        imageUri: imageUri,
        licenseFileName: "MobileCapture.License"
      } as ExportImageOptions,
      (result: ExportImageResult) => {
        setResult(result);
        setImageResult(result.imageUri);
        setInProgress(false);
      }, onError
    );
  };

  const exportImagesToPdf = async (imageUri: string) => {
    setInProgress(true);
    AbbyyRtrSdk.exportImagesToPdf({
        images: [{
          imageUri: imageUri
        }],
        result: {
          destination: 'file'
        },
        licenseFileName: "MobileCapture.License"
      } as ExportImagePdfOptions,
      (result: ExportImagePdfResult) => {
        setResult(result);
        setImageResult(imageUri);
        setInProgress(false);
      }, onError
    );
  };

  return {
    result,
    setResult,
    imageResult,
    setImageResult,
    inProgress,
    recognizeText,
    extractData,
    detectDocumentBoundary,
    assessQualityForOcr,
    cropImage,
    rotateImage,
    exportImage,
    exportImagesToPdf
  };
}

export function truncateBase64String(dict: any): any {
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