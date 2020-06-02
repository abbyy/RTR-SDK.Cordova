// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

export interface ApiError {
  error: {
    description: string
  }
}

export interface AreaOfInterest {
  top: number;
  bottom: number;
  left: number;
  right: number;
}

export interface Point {
  x: number;
  y: number;
}

export interface Rect {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface DocumentSize {
  width: number;
  height: number;
}

export interface CharInfo {
  quadrangle: Point[];
  rect?: Rect;
  isItalic?: boolean;
  isBold?: boolean;
  isUnderlined?: boolean;
  isStrikethrough?: boolean;
  isSmallcaps?: boolean;
  isSuperscript?: boolean;
  isUncertain?: boolean;
}

export interface TextLine {
  text: string;
  quadrangle: Point[];
  rect?: Rect;
  charInfo?: CharInfo[];
}

export interface TextRecognitionOptions {
  licenseFileName?: string;
  imageUri: string;
  areaOfInterest?: AreaOfInterest;
  isTextOrientationDetectionEnabled?: boolean;
  recognitionLanguages?: string[];
}

export interface TextBlock {
  textLines: TextLine[];
}

export interface TextRecognitionResult {
  orientation: number;
  warnings: string[];
  text: string;
  textBlocks: TextBlock[];
}

export interface DataField {
  id: string;
  name: string;
  text: string;
  quadrangle: Point[];
  rect?: Rect;
  charInfo?: CharInfo[];
  components?: {
    text: string;
    quadrangle: Point[];
    rect?: Rect;
  }[];
}

export interface DataExtractionOptions {
  licenseFileName?: string;
  imageUri: string;
  profile: string;
  isTextOrientationDetectionEnabled?: boolean;
  recognitionLanguages?: string[];
}

export interface DataExtractionResult {
  orientation: number;
  warnings: string[];
  dataField: DataField[];
}

export interface ResultOptions {
  compressionLevel?: string;
  exportType?: string;
  destination?: string;
  filePath?: string;
}

export interface CropImageOptions {
  licenseFileName?: string;
  imageUri: string;
  result: ResultOptions;
  documentSize?: DocumentSize;
  documentBoundary?: Point[];
}

export interface CropImageResult {
  imageUri: string;
  imageSize: DocumentSize;
  resolution: Point;
}

export interface DetectDocumentBoundaryOptions {
  licenseFileName?: string;
  imageUri: string;
  areaOfInterest?: AreaOfInterest;
  detectionMode?: string;
  documentSize?: DocumentSize;
}

export interface DetectDocumentBoundaryResult {
  documentSize: DocumentSize;
  documentBoundary?: Point[];
}

export interface AssessQualityForOcrOptions {
  licenseFileName?: string;
  imageUri: string;
  documentBoundary?: Point[];
}

export interface QualityAssessmentForOcrBlock {
  type: string;
  quality: number;
  rect: Rect;
}

export interface AssessQualityForOcrResult {
  qualityAssessmentForOcrBlocks: QualityAssessmentForOcrBlock[];
}

export interface RotateImageOptions {
  licenseFileName?: string;
  imageUri: string;
  angle: number;
  result: ResultOptions;
}

export type RotateImageResult = ExportImageResult;

export interface ExportImageOptions {
  licenseFileName?: string;
  imageUri: string;
  result: ResultOptions;
}

export interface ExportImageResult {
  imageUri: string;
  imageSize: DocumentSize;
}

export interface ImagePdfOptions {
  imageUri: string;
  compressionLevel?: string;
  pageSize?: DocumentSize;
}

export interface PdfInfoOptions {
  title?: string;
  subject?: string;
  keywords?: string;
  author?: string;
  company?: string;
  creator?: string;
  producer?: string;
}

export interface ExportImagePdfOptions {
  licenseFileName?: string;
  images: ImagePdfOptions[];
  result: {
    destination?: string;
    filePath?: string;
  };
  pdfInfo: PdfInfoOptions;
}

export interface ExportImagePdfResult {
  pdfUri: string;
}