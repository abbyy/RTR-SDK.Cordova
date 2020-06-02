// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar,
  IonList, IonItem, IonFabButton, IonFab, IonIcon, IonImg,
  IonGrid, IonRow, IonCol, IonButton, IonProgressBar } from '@ionic/react';
import './CoreAPI.css';
import { camera } from 'ionicons/icons';

import { UseImageCapture } from '../hooks/imageCapture';
import { UseCoreAPI, truncateBase64String } from '../hooks/coreApi';

const CoreAPI: React.FC = () => {
  const { images, takeImage } = UseImageCapture(()=>{
    setImageResult(undefined);
  });
  const { result, imageResult, setImageResult, inProgress, recognizeText, extractData,
    assessQualityForOcr, detectDocumentBoundary, cropImage,
    rotateImage, exportImage, exportImagesToPdf } = UseCoreAPI();

  const displayedImage = (): string => {
    if (imageResult) return imageResult;
    if (images && images[0]) {
      return images[0].base64;
    }
    return "";
  }
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Core API</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Core API</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonList>
          {displayedImage() && 
          <IonItem>
            <IonGrid>
              <IonRow>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => recognizeText(displayedImage())}
                      expand='block'>
                    Recognize Text
                  </IonButton>
                </IonCol>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => extractData(displayedImage())}
                      expand='block'>
                    Extract Data
                  </IonButton>
                </IonCol>
              </IonRow>
              <IonRow>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => detectDocumentBoundary(displayedImage())}
                      expand='block'>
                    Detect Document Boundary
                  </IonButton>
                </IonCol>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => assessQualityForOcr(displayedImage())}
                      expand='block'>
                    Assess Quality For OCR
                  </IonButton>
                </IonCol>
              </IonRow>
              <IonRow>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => rotateImage(displayedImage())}
                      expand='block'>
                    Rotate Image
                  </IonButton>
                </IonCol>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => cropImage(displayedImage())}
                      expand='block'>
                    Crop Image
                  </IonButton>
                </IonCol>
              </IonRow>
              <IonRow>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => exportImage(displayedImage())}
                      expand='block'>
                    Export Image
                  </IonButton>
                </IonCol>
                <IonCol>
                  <IonButton
                      disabled={inProgress}
                      onClick={() => exportImagesToPdf(displayedImage())}
                      expand='block'>
                    Export Images To PDF
                  </IonButton>
                </IonCol>
              </IonRow>
            </IonGrid>
          </IonItem>
          }
          {inProgress &&
            <IonProgressBar type="indeterminate" />
          }
          {displayedImage() &&
          <IonItem>
            <IonImg src={displayedImage()} />
          </IonItem>
          }
          {result &&
          <IonItem><pre>{JSON.stringify(truncateBase64String(result), null, 2)}</pre></IonItem>
          }
        </IonList>
        <IonFab vertical="bottom" horizontal="center" slot="fixed">
          <IonFabButton onClick={() => takeImage(true, 1)}>
            <IonIcon icon={camera}></IonIcon>
         </IonFabButton>
        </IonFab>
      </IonContent>
    </IonPage>
  );
};

export default CoreAPI;
