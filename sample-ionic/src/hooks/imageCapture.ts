// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import { useState } from "react";

declare let AbbyyRtrSdk: any;

export interface Image {
  id: number;
  base64: string;
}

export function UseImageCapture(onImagesTaken:() => void) {
  const [images, setImages] = useState<Image[]>([]);

  const takeImage = async (replace: boolean, count: number) => {
    AbbyyRtrSdk.startImageCapture( (
      result: {
        images?: {
          base64: string;
        }[];
        error?: {
          description: string;
        }
        resultInfo: {
          uriPrefix: string;
        };
      }) => {
        if (result.images) {
          let newImages = result.images.map( (image: { base64: string; }) => {
            return ({
              id: new Date().getTime(),
              base64: result.resultInfo.uriPrefix + image.base64
            });
          });
          if (!replace) {
            newImages = newImages.concat(images);
          }
          setImages(newImages);
          onImagesTaken();
        } else {
          setImages([]);
          if (result.error) {
            console.log(result.error);
          }
        }
      },
      {
        licenseFileName: "MobileCapture.License",
        destination: 'base64',
        isCaptureButtonVisible: true,
        requiredPageCount: count
      }
    );
  };

  const deleteImage = async (image: Image) => {
    const newImages = images.filter(i => i.id !== image.id);
    setImages(newImages);
  };
  
  return {
    images,
    takeImage,
    deleteImage
  };
}
