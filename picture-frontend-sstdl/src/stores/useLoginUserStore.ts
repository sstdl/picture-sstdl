import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

/**
 * 存储登录用户信息状态
 */
export const useLoginUserStore = defineStore('counter', () => {
  // 定义状态初始值
  const loginUser = ref<any>({
    userName: '未登录',
  })

  async function fetchLoginUser() {
    // todo 由于后端还没提供接口，暂时注释
    // const res = await getCurrentUser();
    // if (res.data.code === 0 && res.data.data) {
    //   loginUser.value = res.data.data;
    // }
    // 测试
    setTimeout(() => {
      loginUser.value = { id: 1, username: 'sstdl' }
    }, 3000)
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
