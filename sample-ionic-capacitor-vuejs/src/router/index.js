import Vue from 'vue'
import { IonicVueRouter } from '@ionic/vue'
Vue.use(IonicVueRouter)

const router = new IonicVueRouter({
  routes: [
    { path: '/', redirect: '/home' },
    {
      path: '/home',
      name: 'home',
      component: () =>
        import(/* webpackChunkName: "home" */ '../views/Home.vue')
    }
  ]
})
export default router
