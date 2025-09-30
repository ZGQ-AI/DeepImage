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
        // 保持占位：后续可在此请求后端用户资料并更新 loginUser
        return;
    }

    return { loginUser, setLoginUser, fetchLoginUser }

})
