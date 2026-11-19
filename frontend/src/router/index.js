import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/OrderListView.vue'),
        meta: { title: '报修单' },
      },
      {
        path: 'orders/create',
        name: 'order-create',
        component: () => import('@/views/OrderCreateView.vue'),
        meta: { title: '新建报修', roles: ['REPORTER', 'ADMIN'] },
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: () => import('@/views/OrderDetailView.vue'),
        meta: { title: '报修单详情' },
      },
      {
        path: 'devices',
        name: 'devices',
        component: () => import('@/views/DeviceListView.vue'),
        meta: { title: '设备管理' },
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('@/views/UserListView.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] },
      },
      {
        path: 'stats',
        name: 'stats',
        component: () => import('@/views/StatsView.vue'),
        meta: { title: '统计报表', roles: ['TECHNICIAN', 'ADMIN'] },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} · 校园报修平台` : '校园报修平台'
  if (to.meta.public) {
    return true
  }

  const user = useUserStore()
  if (!user.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!user.profile) {
    try {
      await user.fetchProfile()
    } catch {
      user.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
  if (to.meta.roles && !to.meta.roles.includes(user.role)) {
    return { path: '/dashboard' }
  }
  return true
})

export default router
