import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/Layout.vue'),
      children: [
        {
          path: 'monitor',
          component: () => import('@/views/Monitor.vue')
        }
      ]
    }
  ]
})

export default router
