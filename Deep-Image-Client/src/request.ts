import axios from 'axios';
import { getAccessToken } from './utils/token';
import { useAuthStore } from './stores/useAuthStore';
import router from './router';

const axiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    timeout: 50000,
    withCredentials: true,
});

axiosInstance.interceptors.request.use(function (config) {
    const token = getAccessToken();
    if (token) {
        config.headers = config.headers || {};
        (config.headers as any).Authorization = `Bearer ${token}`;
    }
    return config;
}, function (error) {
    return Promise.reject(error);
},
    { synchronous: true, runWhen: () => true }
);

let isRefreshing = false;
let pendingQueue: Array<() => void> = [];

axiosInstance.interceptors.response.use(function onFulfilled(response) {
    return response;
}, async function onRejected(error) {
    const { response, config } = error || {};
    if (response && response.status === 401 && !config._retry) {
        config._retry = true;
        const auth = useAuthStore();
        if (!isRefreshing) {
            isRefreshing = true;
            try {
                await auth.refresh();
                pendingQueue.forEach(fn => fn());
            } catch (e) {
                pendingQueue = [];
                await auth.logout();
                try { router.replace({ name: 'login' }); } catch (e) {}
            } finally {
                isRefreshing = false;
            }
        }
        return new Promise((resolve, reject) => {
            pendingQueue.push(() => {
                axiosInstance(config).then(resolve).catch(reject);
            });
        });
    }
    return Promise.reject(error);
});


export default axiosInstance;
