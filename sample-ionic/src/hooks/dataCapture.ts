// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import { useState } from "react";
import { DataField } from '../hooks/coreApi_types';

declare let AbbyyRtrSdk: any;

export enum Profile {
  BusinessCards, CustomRegex
}

export function UseDataCapture() {
  const [dataCaptureResult, setDataCaptureResult] = useState<DataField[]>([]);

  const captureData = async (profile: Profile) => {
    let options: any;
    switch (profile) {
      case Profile.BusinessCards:
        options = {
          profile: 'BusinessCards',
          licenseFileName: "MobileCapture.License",
          isStopButtonVisible: true,
          areaOfInterest: '0.9 0.9',
          recognitionLanguages: ['English', 'Russian']
        };
        break;
      case Profile.CustomRegex: 
        options = {
          customDataCaptureScenario : {
            name: 'Code',
            description: 'Mix of digits with letters:  X6YZ64  32VPA  zyy777',
            recognitionLanguages: ['English'],
            fields: [{
                regEx: '([a-zA-Z]+[0-9]+|[0-9]+[a-zA-Z]+)[0-9a-zA-Z]*'
            }]
          },
          licenseFileName: "MobileCapture.License",
          isStopButtonVisible: true
        };
        break;
    }
    AbbyyRtrSdk.startDataCapture( (
      result: {
        dataScheme: {
          id: string;
          name: string;
        };
        dataFields?: DataField[];
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
      if (result.dataFields) {
        setDataCaptureResult(result.dataFields);
      } else {
        setDataCaptureResult([]);
        if (result.error) {
          console.log(result.error);
        }
      }
    },
      options
    );
  };
  return {
    dataCaptureResult,
    captureData
  };
}
