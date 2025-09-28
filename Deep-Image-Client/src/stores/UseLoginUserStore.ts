import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useLoginUserStore = defineStore('loginUser', () => {
    const loginUser = ref<any>({
        id: null,
        userName: 'unLogin'
    }
    )

    function setLoginUser(newLoginUser: any) {
        loginUser.value = newLoginUser;
    }

    async function fetchLoginUser() {
        //测试三秒后登录
        setTimeout(() => {
            loginUser.value = {
                id: 1,
                userName: 'test'
            }
        }, 3000)
    }

    return { loginUser, setLoginUser, fetchLoginUser }

})
