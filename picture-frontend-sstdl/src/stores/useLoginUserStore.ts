import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

/**
 * 存储登录用户信息状态
 */
export const useLoginUserStore = defineStore('counter', () => {
  // 定义状态初始值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 获取
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  /**
   * 获取登录用户信息
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  // 返回状态和方法
  return { loginUser, setLoginUser, fetchLoginUser }
})
