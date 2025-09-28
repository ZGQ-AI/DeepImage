import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 50000,
    withCredentials: true,
});

axiosInstance.interceptors.request.use(function (config) {
    return config;
}, function (error) {
    return Promise.reject(error);
},
    { synchronous: true, runWhen: () => true }
);

axiosInstance.interceptors.response.use(function onFulfilled(response) {
    return response;
}, function onRejected(error) {
    return Promise.reject(error);
});


export default axiosInstance;
