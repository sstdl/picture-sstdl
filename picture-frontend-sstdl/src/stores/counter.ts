import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// 一个状态就存储一类要共享的数据
export const useCounterStore = defineStore('counter', () => {
  // 定义状态初始值
  const count = ref(0)

  // 定义计算属性
  const doubleCount = computed(() => count.value * 2)

  // 定义状态更改方法
  function increment() {
    count.value++
  }

  // 返回状态和方法
  return { count, doubleCount, increment }
})
