// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import React from 'react';
import './TextCapture.css';
import { camera } from 'ionicons/icons';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar,
  IonFab, IonFabButton, IonIcon, IonGrid, IonRow } from '@ionic/react';

import { TextLine, UseTextCapture } from '../hooks/textCapture';

const TextCapture: React.FC = () => {
  const { textCaptureResult, captureText } = UseTextCapture();
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Text Capture</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Text Capture</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonGrid>
          {textCaptureResult.map((textLine: TextLine) => (
            <IonRow>
              {textLine.text}
            </IonRow>
          ))}
        </IonGrid>
        <IonFab vertical="bottom" horizontal="center" slot="fixed">
          <IonFabButton onClick={() => captureText()}>
            <IonIcon icon={camera}></IonIcon>
         </IonFabButton>
        </IonFab>
      </IonContent>
    </IonPage>
  );
};

export default TextCapture;
