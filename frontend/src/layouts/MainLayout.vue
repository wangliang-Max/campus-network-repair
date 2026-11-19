<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  DataBoard,
  Tickets,
  Cpu,
  User,
  TrendCharts,
  Setting,
  Expand,
  Fold,
  SwitchButton,
  Plus,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const app = useAppStore()

const menus = computed(() => {
  const items = [
    { path: '/dashboard', label: '工作台', icon: DataBoard },
    { path: '/orders', label: '报修单', icon: Tickets },
    { path: '/devices', label: '设备管理', icon: Cpu },
  ]
  if (['TECHNICIAN', 'ADMIN'].includes(user.role)) {
    items.push({ path: '/stats', label: '统计报表', icon: TrendCharts })
  }
  if (user.role === 'ADMIN') {
    items.push({ path: '/users', label: '用户管理', icon: User })
  }
  items.push({ path: '/profile', label: '个人中心', icon: Setting })
  return items
})

const activeMenu = computed(() => {
  if (route.path.startsWith('/orders')) return '/orders'
  return route.path
})

const canCreate = computed(() => ['REPORTER', 'ADMIN'].includes(user.role))

async function handleLogout() {
  await ElMessageBox.confirm('确定退出登录吗？', '退出登录', { type: 'warning' })
  user.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside :width="app.sidebarCollapsed ? '64px' : '220px'" class="aside">
      <div class="brand">
        <span class="brand-mark">T05</span>
        <span v-if="!app.sidebarCollapsed" class="brand-text">校园报修平台</span>
      </div>
      <el-menu :default-active="activeMenu" router class="menu" :collapse="app.sidebarCollapsed">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-button text circle @click="app.toggleSidebar">
          <el-icon><component :is="app.sidebarCollapsed ? Expand : Fold" /></el-icon>
        </el-button>
        <div class="crumb">
          <span class="muted">校园网络与机房故障报修平台</span>
          <span class="sep">/</span>
          <strong>{{ route.meta.title }}</strong>
        </div>
        <div class="spacer" />
        <el-button v-if="canCreate" type="primary" :icon="Plus" @click="router.push('/orders/create')">
          新建报修
        </el-button>
        <el-dropdown trigger="click">
          <span class="user">
            <el-avatar :size="30">{{ user.displayName.slice(0, 1) }}</el-avatar>
            <span class="name">{{ user.displayName }}</span>
            <el-tag size="small" effect="plain">{{ user.profile?.roleName || user.role }}</el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #0b2545;
  transition: width 0.2s ease;
  overflow: hidden;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  font-size: 18px;
  font-weight: 800;
  color: #7fd1cc;
  letter-spacing: 1px;
}

.brand-text {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.menu {
  border-right: none;
  background: transparent;
  padding: 10px 8px;
}

.menu :deep(.el-menu-item) {
  color: #c9d6e3;
  border-radius: 8px;
  margin-bottom: 4px;
}

.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu :deep(.el-menu-item.is-active) {
  background: #0e7c86;
  color: #fff;
}

.header {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-bottom: 1px solid #d9e2ec;
  height: 64px;
}

.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.crumb .sep {
  color: #b8c4d0;
}

.spacer {
  flex: 1;
}

.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user .name {
  font-size: 14px;
}

.main {
  padding: 0;
  background: #f2f5f9;
  overflow-y: auto;
}
</style>
