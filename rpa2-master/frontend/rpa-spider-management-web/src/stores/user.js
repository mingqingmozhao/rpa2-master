import { defineStore } from 'pinia'
import { ref } from 'vue'
import Cookies from 'js-cookie'

const USER_INFO_KEY = 'rpa_user_info'

function normalizeUserInfo(info) {
  if (!info) return null

  const normalized = { ...info }
  if (normalized.role && !normalized.roles) {
    normalized.roles = [normalized.role]
  } else if (!Array.isArray(normalized.roles)) {
    normalized.roles = normalized.roles ? [normalized.roles] : []
  }
  normalized.roles = normalized.roles.map(role => String(role).replace(/^ROLE_/, '').toUpperCase())
  if (normalized.role) {
    normalized.role = String(normalized.role).replace(/^ROLE_/, '').toUpperCase()
  }

  return normalized
}

function getSavedUserInfo() {
  try {
    return normalizeUserInfo(JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null'))
  } catch (error) {
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  // 初始化时从 Cookie 读取 token
  const token = ref(Cookies.get('rpa_token') || '')
  const userInfo = ref(token.value ? getSavedUserInfo() : null)

  // 初始化函数，确保在应用启动时加载 token
  function init() {
    const savedToken = Cookies.get('rpa_token')
    if (savedToken) {
      token.value = savedToken
      userInfo.value = getSavedUserInfo()
      console.log('[UserStore] Token loaded from cookie:', savedToken ? 'present' : 'none')
    } else {
      userInfo.value = null
      localStorage.removeItem(USER_INFO_KEY)
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
    const normalizedInfo = normalizeUserInfo(info)
    userInfo.value = normalizedInfo
    if (normalizedInfo) {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(normalizedInfo))
    } else {
      localStorage.removeItem(USER_INFO_KEY)
    }
    console.log('[UserStore] User info set:', normalizedInfo?.username)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    Cookies.remove('rpa_token')
    localStorage.removeItem(USER_INFO_KEY)
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
