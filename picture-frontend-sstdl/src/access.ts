import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'

let firstFetchLoginUser = true

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 保证页面刷新，首次加载时，等待后端返回用户信息后，在校验信息
  await loginUserStore.fetchLoginUser()
  if (firstFetchLoginUser) {
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }
  const url = to.fullPath
  if (url.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole != 'admin') {
      message.error('没有权限访问')
      next(`/user/login?redirect=${url}`)
      return
    }
  }
  next()
})
