<template>
  <div class="user-info">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="user-profile">
            <div class="avatar-container">
              <el-avatar :size="120" :src="avatarUrl" @error="handleAvatarError">
                <el-icon :size="60"><User /></el-icon>
              </el-avatar>
              <div class="avatar-overlay" @click="showAvatarDialog = true">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <div class="user-details">
              <div class="detail-item">
                <el-icon><User /></el-icon>
                <span class="label">用户名：</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Avatar /></el-icon>
                <span class="label">姓名：</span>
                <span class="value">{{ userInfo.realName || '未设置' }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Message /></el-icon>
                <span class="label">邮箱：</span>
                <span class="value">{{ userInfo.email || '未设置' }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Phone /></el-icon>
                <span class="label">手机号：</span>
                <span class="value">{{ userInfo.phone || '未设置' }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Timer /></el-icon>
                <span class="label">注册时间：</span>
                <span class="value">{{ userInfo.createTime }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 修改信息表单 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>修改基本信息</span>
          </template>
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="profileForm.realName" placeholder="请输入姓名" maxlength="20" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>修改密码</span>
          </template>
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword" :loading="passwordLoading">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 头像上传对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="500px">
      <div class="avatar-upload">
        <el-upload
          ref="avatarUploadRef"
          class="avatar-uploader"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="uploadAvatar"
          :auto-upload="true"
          accept="image/*"
        >
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-preview" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="upload-tips">
          <p>支持 JPG、PNG 格式，大小不超过 5MB</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Avatar, Message, Phone, Timer, Camera, Plus } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, updatePassword, uploadAvatar as uploadAvatarApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711.png'

const profileLoading = ref(false)
const passwordLoading = ref(false)
const showAvatarDialog = ref(false)
const avatarUploadRef = ref(null)

const userInfo = reactive({
  userId: null,
  username: '',
  realName: '',
  avatar: '',
  email: '',
  phone: '',
  createTime: ''
})

const profileForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileFormRef = ref(null)
const passwordFormRef = ref(null)

// 邮箱正则
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
// 手机号正则
const phoneRegex = /^1[3-9]\d{9}$/

const profileRules = reactive({
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 1, max: 30, message: '姓名长度不能超过 30 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && !emailRegex.test(value)) {
          callback(new Error('请输入正确的邮箱格式'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (rule, value, callback) => {
        if (value && !phoneRegex.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 计算头像 URL
const avatarUrl = computed(() => {
  if (userInfo.avatar) {
    // 如果是完整 URL 则直接返回
    if (userInfo.avatar.startsWith('http://') || userInfo.avatar.startsWith('https://')) {
      return userInfo.avatar
    }
    // 如果是以 / 开头的相对路径，直接返回（Vite 代理会自动转发到后端）
    if (userInfo.avatar.startsWith('/')) {
      return userInfo.avatar
    }
    // 其他情况直接返回
    return userInfo.avatar
  }
  return defaultAvatar
})

// 头像加载失败处理
const avatarLoadError = ref(false)
const handleAvatarError = () => {
  avatarLoadError.value = true
  console.warn('头像加载失败，使用默认头像')
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    const data = res.data
    
    const nextUserInfo = {
      userId: data.userId,
      username: data.username,
      realName: data.realName || '',
      avatar: data.avatar || '',
      email: data.email || '',
      phone: data.phone || '',
      createTime: data.createTime || new Date().toLocaleString()
    }

    Object.assign(userInfo, nextUserInfo)
    userStore.setUserInfo({
      ...(userStore.userInfo || {}),
      ...nextUserInfo
    })

    // 填充表单
    profileForm.realName = userInfo.realName
    profileForm.email = userInfo.email
    profileForm.phone = userInfo.phone
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败：' + (error.message || '未知错误'))
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    profileLoading.value = true
    try {
      await updateUserInfo({
        realName: profileForm.realName,
        email: profileForm.email,
        phone: profileForm.phone
      })
      ElMessage.success('个人信息更新成功')
      await loadUserInfo()
    } catch (error) {
      console.error('更新失败:', error)
      
      // 根据错误类型显示不同的提示信息
      const errorMsg = error.response?.data?.message || error.message || '更新失败：未知错误'
      
      // 检查是否是手机号重复的错误
      if (errorMsg.includes('手机号') || errorMsg.includes('一致') || errorMsg.includes('未修改')) {
        ElMessage.warning({
          message: errorMsg,
          duration: 3000
        })
      } else if (errorMsg.includes('邮箱')) {
        ElMessage.warning({
          message: errorMsg,
          duration: 3000
        })
      } else {
        ElMessage.error({
          message: errorMsg,
          duration: 3000
        })
      }
    } finally {
      profileLoading.value = false
    }
  })
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await updatePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      // 退出登录
      userStore.logout()
      // 跳转到登录页
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
    } catch (error) {
      console.error('修改密码失败:', error)
      
      // 根据错误类型显示不同的提示信息
      const errorMsg = error.response?.data?.message || error.message || '修改密码失败：未知错误'
      
      // 检查是否是旧密码错误的提示
      if (errorMsg.includes('密码') || errorMsg.includes('不正确')) {
        ElMessage.warning({
          message: errorMsg,
          duration: 3000
        })
      } else {
        ElMessage.error({
          message: errorMsg,
          duration: 3000
        })
      }
    } finally {
      passwordLoading.value = false
    }
  })
}

const beforeAvatarUpload = (file) => {
  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  // 验证文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  
  return true
}

const uploadAvatar = async (options) => {
  const { file } = options
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await uploadAvatarApi(formData)
    
    console.log('头像上传响应:', res)
    
    ElMessage.success('头像上传成功')
    showAvatarDialog.value = false
    await loadUserInfo()
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败：' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.user-info {
  .user-profile {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;

    .avatar-container {
      position: relative;
      margin-bottom: 30px;
      cursor: pointer;

      .avatar-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: white;
        opacity: 0;
        transition: opacity 0.3s;
        font-size: 12px;

        .el-icon {
          font-size: 24px;
          margin-bottom: 5px;
        }
      }

      &:hover .avatar-overlay {
        opacity: 1;
      }
    }

    .user-details {
      width: 100%;

      .detail-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .el-icon {
          margin-right: 10px;
          color: #909399;
        }

        .label {
          color: #606266;
          font-weight: 500;
        }

        .value {
          color: #303133;
          margin-left: 5px;
        }
      }
    }
  }

  .avatar-upload {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;

    .avatar-uploader {
      .avatar-preview {
        width: 150px;
        height: 150px;
        border-radius: 50%;
        object-fit: cover;
      }

      .avatar-uploader-icon {
        width: 150px;
        height: 150px;
        border-radius: 50%;
        border: 1px dashed #d9d9d9;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #8c939d;
        cursor: pointer;
        transition: border-color 0.3s;

        &:hover {
          border-color: #409eff;
        }
      }
    }

    .upload-tips {
      margin-top: 15px;
      text-align: center;
      color: #909399;
      font-size: 12px;

      p {
        margin: 5px 0;
      }
    }
  }
}
</style>
