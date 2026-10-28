import axios from 'axios';
import keycloak from '../config/keycloak';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost',
  headers: {
    'Content-Type': 'application/json'
  }
});

axiosClient.interceptors.request.use(
  (config) => {
    if (keycloak.token) {
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default axiosClient;
