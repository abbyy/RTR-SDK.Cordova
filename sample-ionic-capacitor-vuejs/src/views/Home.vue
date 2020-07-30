<template>
  <ion-page>
    <ion-header>

      <ion-toolbar class="toolbar-md-primary">
        <ion-title>Sample</ion-title>
      </ion-toolbar>

    </ion-header>
    <ion-content class="content">
      <ion-button @click="startTextExtraction">Start Scan</ion-button>
      <ion-item>
        <ion-label class="primary">{{text}}</ion-label>
      </ion-item>
    </ion-content>
  </ion-page>
</template>

<script>
// import { AbbyyRTR } from '@ionic-native/abbyy-rtr'
import { AbbyyRTR } from '@ionic-native/abbyy-rtr'
export default {
  name: 'Home',
  data  () {
    return {
      text: ''
    }
  },
  methods: {
    startTextExtraction () {
      AbbyyRTR.startTextCapture({
        isFlashlightVisible: true,
        isStopButtonVisible: true,
        stopWhenStable: true,
        recognitionLanguages: ['English', 'German'],
        areaOfInterest: '0.9 0.1'
      }).then(res => {
        console.log('detected: ' + JSON.stringify(res))
        console.log(res.textLines[0].text)
        this.text = res.textLines[0].text
      }).catch(err => {
        console.log('error has occured')
        console.log(JSON.stringify(err))
        if (err === undefined) {
          console.error('undefined error?!')
        }
      })
    }
  }
}
</script>
