// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import React, { useState } from 'react';
import './ImageCapture.css';
import { camera, trash, close } from 'ionicons/icons';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar,
  IonFab, IonFabButton, IonIcon, IonGrid, IonRow,
  IonCol, IonImg, IonActionSheet } from '@ionic/react';

import { UseImageCapture, Image } from '../hooks/imageCapture';

const ImageCapture: React.FC = () => {
  const { images, takeImage, deleteImage } = UseImageCapture(()=>{});
  const [ imageToDelete, setImageToDelete ] = useState<Image>();
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Image Capture</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Image Capture</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonGrid>
          <IonRow>
            {images.map((image: Image, index: number) => (
              <IonCol size="6" key={index}>
                <IonImg onClick={() => setImageToDelete(image)} src={image.base64} />
              </IonCol>
            ))}
          </IonRow>
        </IonGrid>
        <IonFab vertical="bottom" horizontal="center" slot="fixed">
          <IonFabButton onClick={() => takeImage(false, 0)}>
            <IonIcon icon={camera}></IonIcon>
         </IonFabButton>
        </IonFab>
        <IonActionSheet
          isOpen={!!imageToDelete}
          buttons={[
            {
              text: 'Delete',
              role: 'destructive',
              icon: trash,
              handler: () => {
                if (imageToDelete) {
                  deleteImage(imageToDelete);
                  setImageToDelete(undefined);
                }
              }
            },
            {
              text: 'Cancel',
              icon: close,
              role: 'cancel'
            }]}
          onDidDismiss={() => setImageToDelete(undefined)}
        />
      </IonContent>
    </IonPage>
  );
};

export default ImageCapture;
