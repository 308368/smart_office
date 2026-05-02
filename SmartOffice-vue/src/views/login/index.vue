<template>
  <div class="login-container">
    <div class="login-left">
      <div class="left-content">
        <h1 class="title">企业智能办公助手</h1>
        <p class="subtitle">SmartOffice - 让办公更智能</p>
        <div class="features">
          <div class="feature-item">
            <el-icon>
              <ChatDotRound />
            </el-icon>
            <span>AI智能问答</span>
          </div>
          <div class="feature-item">
            <el-icon>
              <Collection />
            </el-icon>
            <span>企业知识库</span>
          </div>
          <div class="feature-item">
            <el-icon>
              <Tickets />
            </el-icon>
            <span>工单管理</span>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-box">
        <h2 class="login-title">欢迎登录</h2>

        <el-tabs v-model="activeTab" class="login-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="账号密码" name="password">
            <el-form ref="formRef" :model="loginForm" :rules="rules" class="login-form">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large"
                  show-password @keyup.enter="handleLogin" />
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" :loading="loading" class="login-button" @click="handleLogin">
                  登 录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="微信扫码" name="wechat">
            <div class="wechat-login">
              <div class="qrcode-wrapper">
                <div id="login_container" class="qrcode-container"></div>
                <div class="qrcode-cover">“智能办公”</div>
              </div>
              <p class="tips">请使用微信扫描二维码登录</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <p class="copyright">© 2026 SmartOffice</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '../../api/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const activeTab = ref('password')

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  authType: 'password'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const loginData = {
          username: loginForm.username,
          password: loginForm.password,
          authType: loginForm.authType
        };
        const loginRequest = {
          username: JSON.stringify(loginData),
          password: loginForm.password
        }

        await login(loginRequest).then((res: any) => {
          if (res.data) {
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('userId', String(res.data.userId))
          }
        })
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 生成微信二维码
const generateWxQrcode = () => {
  const container = document.getElementById('login_container')
  console.log('container:', container)
  console.log('container size:', container?.offsetWidth, container?.offsetHeight)

  // 动态加载微信 JS SDK
  const script = document.createElement('script')
  script.src = 'https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js'
  script.onload = () => {
    console.log('wxLogin.js loaded, creating WxLogin...')
    const wxObj = new (window as any).WxLogin({
      self_redirect: false,
      id: 'login_container',
      // appid: 'wx17655f8047b85150',
      appid: 'wxed9954c01bb89b47',
      scope: 'snsapi_login',
      // redirect_uri: 'http://tjxt-user-t.itheima.net/xuecheng/auth/wxLogin',
      redirect_uri: 'http://localhost:8160/api/auth/wxLogin',
      state: 'wxlogin',
      style: 'black',
      href: 'data:text/css;,.wxqrcode_info{display:none !important}'
    })
    console.log('WxLogin created:', wxObj)
  }
  script.onerror = (e) => {
    console.error('Failed to load wxLogin.js', e)
  }
  document.head.appendChild(script)
  console.log('script added to head')
}

// 监听 tab 切换，切换到微信扫码时生成二维码
const handleTabChange = (tabName: string) => {
  console.log('tab changed to:', tabName)
  if (tabName === 'wechat') {
    // 清空容器避免重复生成
    const container = document.getElementById('login_container')
    if (container) {
      container.innerHTML = ''
    }
    setTimeout(() => generateWxQrcode(), 100)
  }
}

onMounted(() => {
  if (activeTab.value === 'wechat') {
    generateWxQrcode()
  }
  // 检查是否是微信扫码回调，带有code说明扫码成功
  const urlParams = new URLSearchParams(window.location.search)
  const code = urlParams.get('code')
  if (code) {
    const cover = document.querySelector('.qrcode-cover') as HTMLElement
    if (cover) {
      cover.style.display = 'none'
    }
  }
  // 检查URL是否有登录参数（username + authType）
  const username = urlParams.get('username')
  const authType = urlParams.get('authType')
  if (username && authType === 'wx') {
    // 自动触发登录
    loginForm.username = username
    loginForm.password = '112'
    loginForm.authType = authType
    handleLogin()
  }
})
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  min-height: 100vh;

  .login-left {
    flex: 1;
    background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      width: 600px;
      height: 600px;
      border-radius: 50%;
      background: rgba(16, 185, 129, 0.1);
      top: -200px;
      right: -200px;
    }

    &::after {
      content: '';
      position: absolute;
      width: 400px;
      height: 400px;
      border-radius: 50%;
      background: rgba(16, 185, 129, 0.08);
      bottom: -150px;
      left: -150px;
    }

    .left-content {
      text-align: center;
      z-index: 1;

      .title {
        font-size: 36px;
        font-weight: 600;
        color: #059669;
        margin-bottom: 12px;
      }

      .subtitle {
        font-size: 18px;
        color: #047857;
        margin-bottom: 48px;
      }

      .features {
        display: flex;
        flex-direction: column;
        gap: 24px;

        .feature-item {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 16px;
          color: #047857;

          .el-icon {
            font-size: 24px;
            color: #10B981;
          }
        }
      }
    }
  }

  .login-right {
    width: 500px;
    background: #FFFFFF;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;

    .login-box {
      width: 100%;
      max-width: 380px;

      .login-title {
        font-size: 28px;
        font-weight: 600;
        color: #1F2937;
        text-align: center;
        margin-bottom: 24px;
      }

      .login-tabs {
        .wechat-login {
          padding: 20px 0;
          text-align: center;

          .qrcode-container {
            display: flex;
            justify-content: center;
            margin-bottom: 16px;
            background: #F9FAFB;
            padding: 16px;
            border-radius: 8px;
          }

          .qrcode-wrapper {
            position: relative;
            display: inline-block;
          }

          .qrcode-cover {
            position: absolute;
            bottom: 10px;
            left: 0;
            right: 0;
            height: 80px;
            background: #F9FAFB;
            z-index: 10;
            font-size: 13px;
            font-weight: 400;
            font-family: Microsoft Yahei;
            color: #373737;
          }

          .tips {
            font-size: 13px;
            color: #9CA3AF;
          }
        }

        .login-form {
          padding-top: 20px;

          .login-button {
            width: 100%;
            font-size: 16px;
            font-weight: 500;
            background: linear-gradient(135deg, #10B981 0%, #059669 100%);
            border: none;
            height: 48px;

            &:hover {
              background: linear-gradient(135deg, #059669 0%, #047857 100%);
            }
          }
        }
      }
    }

    .copyright {
      margin-top: 40px;
      color: #9CA3AF;
      font-size: 13px;
    }
  }
}
</style>
