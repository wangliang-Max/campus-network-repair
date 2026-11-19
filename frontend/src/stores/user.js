import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '@/api/auth'

const PROFILE_KEY = 'cnr_profile'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('cnr_token') || '',
    profile: JSON.parse(localStorage.getItem(PROFILE_KEY) || 'null'),
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
    role: (state) => state.profile?.roleCode || '',
    isStaff: (state) => ['TECHNICIAN', 'ADMIN'].includes(state.profile?.roleCode),
    isAdmin: (state) => state.profile?.roleCode === 'ADMIN',
    displayName: (state) => state.profile?.realName || state.profile?.username || '用户',
  },
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.setAuth(data)
      await this.fetchProfile()
      return data
    },
    setAuth(data) {
      this.token = data.token
      localStorage.setItem('cnr_token', data.token)
      this.profile = {
        username: data.username,
        realName: data.realName,
        roleCode: data.roleCode,
      }
      localStorage.setItem(PROFILE_KEY, JSON.stringify(this.profile))
    },
    async fetchProfile() {
      const data = await getMe()
      this.profile = data
      localStorage.setItem(PROFILE_KEY, JSON.stringify(data))
      return data
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('cnr_token')
      localStorage.removeItem(PROFILE_KEY)
    },
  },
})
