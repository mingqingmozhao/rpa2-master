import { defineStore } from 'pinia'
import { ref } from 'vue'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', () => {
  // 初始化时从 Cookie 读取 token
  const token = ref(Cookies.get('rpa_token') || '')
  const userInfo = ref(null)

  // 初始化函数，确保在应用启动时加载 token
  function init() {
    const savedToken = Cookies.get('rpa_token')
    if (savedToken) {
      token.value = savedToken
      console.log('[UserStore] Token loaded from cookie:', savedToken ? 'present' : 'none')
    }
  }

  // 立即调用 init
  init()

  function setToken(newToken) {
    token.value = newToken
    // 设置 Cookie，过期时间 7 天
    Cookies.set('rpa_token', newToken, { expires: 7 })
    console.log('[UserStore] Token saved to cookie')
  }

  function setUserInfo(info) {
    userInfo.value = info
    console.log('[UserStore] User info set:', info?.username)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    Cookies.remove('rpa_token')
    console.log('[UserStore] Logged out, cookie removed')
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout,
    init
  }
})
