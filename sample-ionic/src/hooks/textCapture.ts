// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import { useState } from "react";

declare let AbbyyRtrSdk: any;

export interface TextLine {
  quadrangle: {
    x: number;
    y: number;
  }[];
  text: string;
}

export function UseTextCapture() {
  const [textCaptureResult, setTextCaptureResult] = useState<TextLine[]>([]);

  const captureText = async () => {
    AbbyyRtrSdk.startTextCapture( (
      result: {
        textLines?: TextLine[];
        error?: {
          description: string;
        }
        resultInfo: {
          stabilityStatus: string;
          recognitionLanguages: string[];
          frameSize: string;
          userAction?: string;
        };
      }
    ) => {
      if (result.textLines) {
        setTextCaptureResult(result.textLines);
      } else {
        setTextCaptureResult([]);
        if (result.error) {
          console.log(result.error);
        }
      }
    },
      {
        licenseFileName: "MobileCapture.License",
        isStopButtonVisible: true,
        selectableRecognitionLanguages: ['Russian', 'English']
      }
    );
  };
  return {
    textCaptureResult,
    captureText
  };
}
