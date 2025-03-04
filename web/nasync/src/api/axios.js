import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const adminApi = {
  getUsers: () => axiosInstance.get('/admin/users'),
  getScholars: () => axiosInstance.get('/admin/users/scholars'),
  getDepartmentHeads: () => axiosInstance.get('/admin/users/department-heads'),
  registerUser: (data) => axiosInstance.post('/admin/users/register', data),
  toggleActive: (userId) => axiosInstance.put(`/admin/users/${userId}/toggle-active`),
  
  getDepartments: () => axiosInstance.get('/admin/departments'),
  createDepartment: (name) => axiosInstance.post('/admin/departments', { name }),
  
  getBranches: () => axiosInstance.get('/admin/branches'),
  createBranch: (name, deptId) => axiosInstance.post('/admin/branches', { name, deptId }),
};

export const authApi = {
  login: (schoolId, password) => axiosInstance.post('/auth/login', { schoolId, password }),
  logout: () => axiosInstance.post('/auth/logout'),
};

export default axiosInstance;