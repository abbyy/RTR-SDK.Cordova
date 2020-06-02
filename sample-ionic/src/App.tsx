// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

import React from 'react';
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonIcon, IonLabel, IonRouterOutlet,
  IonTabBar, IonTabButton, IonTabs } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { images, text, card, scan } from 'ionicons/icons';
import ImageCapture from './pages/ImageCapture';
import DataCapture from './pages/DataCapture';
import TextCapture from './pages/TextCapture';
import CoreAPI from './pages/CoreAPI';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';

/* Basic CSS for apps built with Ionic */
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

/* Theme variables */
import './theme/variables.css';

const App: React.FC = () => (
  <IonApp>
    <IonReactRouter>
      <IonTabs>
        <IonRouterOutlet>
          <Route path="/imageCapture" component={ImageCapture} exact={true} />
          <Route path="/textCapture" component={TextCapture} exact={true} />
          <Route path="/dataCapture" component={DataCapture} exact={true} />
          <Route path="/coreApi" component={CoreAPI} />
          <Route path="/" render={() => <Redirect to="/imageCapture" />} exact={true} />
        </IonRouterOutlet>
        <IonTabBar slot="bottom">
          <IonTabButton tab="imageCapture" href="/imageCapture">
            <IonIcon icon={images} />
            <IonLabel>Image Capture</IonLabel>
          </IonTabButton>
          <IonTabButton tab="textCapture" href="/textCapture">
            <IonIcon icon={text} />
            <IonLabel>Text Capture</IonLabel>
          </IonTabButton>
          <IonTabButton tab="dataCapture" href="/dataCapture">
            <IonIcon icon={card} />
            <IonLabel>Data Capture</IonLabel>
          </IonTabButton>
          <IonTabButton tab="coreApi" href="/coreApi">
            <IonIcon icon={scan} />
            <IonLabel>Core API</IonLabel>
          </IonTabButton>
        </IonTabBar>
      </IonTabs>
    </IonReactRouter>
  </IonApp>
);

export default App;
