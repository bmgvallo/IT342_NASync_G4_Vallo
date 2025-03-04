import axios from './axios';

export const authApi = {
  login: (schoolId, password) => 
    axios.post('/auth/login', { schoolId, password }),
  
  logout: () => 
    axios.post('/auth/logout'),
  
  checkEmail: (email) => 
    axios.get('/auth/check-email', { params: { email } }),
  
  checkUsername: (username) => 
    axios.get('/auth/check-username', { params: { username } }),
};

export const adminApi = {
  getAllUsers: () => axios.get('/admin/users'),
  getScholars: () => axios.get('/admin/users/scholars'),
  getDepartmentHeads: () => axios.get('/admin/users/department-heads'),
  registerUser: (userData) => axios.post('/admin/users/register', userData),
  toggleUserActive: (userId) => axios.put(`/admin/users/${userId}/toggle-active`),

  getDepartments: async () => {
    const response = await axios.get('/admin/departments');
    return { data: response.data.data || [] };
  },
  createDepartment: async (name) => {
    const response = await axios.post('/admin/departments', { name });
    return response;
  },
  updateDepartment: async (id, name) => {
    const response = await axios.put(`/admin/departments/${id}`, { name });
    return response;
  },
  deleteDepartment: async (id) => {
    const response = await axios.delete(`/admin/departments/${id}`);
    return response;
  },

  getBranches: async () => {
    const response = await axios.get('/admin/branches');
    return { data: response.data.data || [] };
  },
  createBranch: async (name, deptId) => {
    const response = await axios.post('/admin/branches', { name, deptId });
    return response;
  },
  updateBranch: async (id, name, deptId) => {
    const response = await axios.put(`/admin/branches/${id}`, { name, deptId });
    return response;
  },
  deleteBranch: async (id) => {
    const response = await axios.delete(`/admin/branches/${id}`);
    return response;
  },
};

export default axios;