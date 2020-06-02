// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import React, { useState } from 'react';
import './DataCapture.css';
import { camera } from 'ionicons/icons';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar,
  IonFab, IonFabButton, IonIcon, IonGrid, IonRow, IonCol,
  IonButton } from '@ionic/react';

import { DataField } from '../hooks/coreApi_types';
import { Profile, UseDataCapture } from '../hooks/dataCapture';

function renderDataField(dataField: DataField) {
  return (
    <IonGrid>
      <IonRow>
        <IonCol>
          {dataField.name}
        </IonCol>
        <IonCol>
          {dataField.text}
        </IonCol>
      </IonRow>
      {dataField.components?.map((component) => (
        <IonRow>
          <IonCol></IonCol>
          <IonCol>{component.text}</IonCol>
        </IonRow>
      ))}
    </IonGrid>
  );
}

const DataCapture: React.FC = () => {
  const [profile, setProfile] = useState<Profile>(Profile.BusinessCards);
  const { dataCaptureResult, captureData } = UseDataCapture();
  const highlight = (p: Profile): string => {
    if (p === profile) {
      return 'primary';
    } else {
      return 'light';
    }
  };
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Data Capture</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Data Capture</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonButton
            class='ion-text-uppercase'
            color={highlight(Profile.BusinessCards)}
            onClick={() => setProfile(Profile.BusinessCards)}
            expand='block'>
          Business Cards
        </IonButton>
        <IonButton
            color={highlight(Profile.CustomRegex)}
            onClick={() => setProfile(Profile.CustomRegex)}
            expand='block'>
          ([a-zA-Z]+[0-9]+|[0-9]+[a-zA-Z]+)[0-9a-zA-Z]*
        </IonButton>
        {dataCaptureResult.map((dataField: DataField) => renderDataField(dataField))}
        <IonFab vertical="bottom" horizontal="center" slot="fixed">
          <IonFabButton onClick={() => captureData(profile)}>
            <IonIcon icon={camera}></IonIcon>
         </IonFabButton>
        </IonFab>
      </IonContent>
    </IonPage>
  );
};

export default DataCapture;
