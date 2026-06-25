import axios from './axios';

export const authApi = {
  login: (schoolId, password) =>
    axios.post('/auth/login', { schoolId, password }),

  logout: () =>
    axios.post('/auth/logout'),

  getMe: () =>
    axios.get('/auth/me'),

  checkEmail: (email) =>
    axios.get('/auth/check-email', { params: { email } }),

  checkUsername: (username) =>
    axios.get('/auth/check-username', { params: { username } }),

  changePassword: (currentPassword, newPassword) =>
    axios.put('/auth/change-password', { currentPassword, newPassword }),

  updateGmail: (gmail) =>
    axios.put('/auth/me/gmail', { gmail }),
};

export const adminApi = {
  getAllUsers: () =>
    axios.get('/admin/users'),

  getScholars: () =>
    axios.get('/admin/users/scholars'),

  getDepartmentHeads: () =>
    axios.get('/admin/users/department-heads'),

  registerUser: (userData) =>
    axios.post('/admin/users/register', userData),

  toggleUserActive: (userId) =>
    axios.put(`/admin/users/${userId}/toggle-active`),

  updateUser: (userId, data) =>
    axios.put(`/admin/users/${userId}`, data),

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

  reactivateDepartment: async (id) => {
    const response = await axios.put(`/admin/departments/${id}/reactivate`);
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

  reactivateBranch: async (id) => {
    const response = await axios.put(`/admin/branches/${id}/reactivate`);
    return response;
  },

  getDashboardStats: () =>
    axios.get('/admin/dashboard/stats'),

  setScholarSchedule: (schoolId, entries) =>
    axios.post(`/admin/scholars/${schoolId}/schedule`, entries),

  getScholarSchedule: (schoolId) =>
    axios.get(`/admin/scholars/${schoolId}/schedule`),

  uploadUserPhoto: (userId, file) => {
    const form = new FormData();
    form.append('file', file);
    return axios.post(`/admin/users/${userId}/photo`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  getScholarDutySummary: (schoolId) =>
    axios.get(`/admin/scholars/${schoolId}/duty-summary`),

  getScholarDutyHistory: (schoolId) =>
    axios.get(`/admin/scholars/${schoolId}/duties`),

  sendScholarReminder: (schoolId) =>
    axios.post(`/admin/scholars/${schoolId}/send-reminder`),
};

export const semesterApi = {
  getAll: () => axios.get('/admin/semesters'),
  create: (data) => axios.post('/admin/semesters', data),
  activate: (id) => axios.put(`/admin/semesters/${id}/activate`),
  getActive: () => axios.get('/admin/semesters/active'),
  deactivate: (id) => axios.put(`/admin/semesters/${id}/deactivate`),
  getActive: () => axios.get('/admin/semesters/active'),
};

export const dutyDayApi = {
  getBySemester: (semesterId) =>
    axios.get('/admin/duty-days', { params: { semesterId } }),
  create: (data) => axios.post('/admin/duty-days', data),
  delete: (id) => axios.delete(`/admin/duty-days/${id}`),
};

export const scholarDutyApi = {
  clockIn: (dutyType = 'REGULAR') =>
    axios.post('/scholar/duties/clock-in', { dutyType }),
  clockOut: () => axios.put('/scholar/duties/clock-out'),
  getMyDuties: () => axios.get('/scholar/duties'),
  getSummary: () => axios.get('/scholar/duties/summary'),
  getTodaySchedule: () => axios.get('/scholar/schedule/today'),
};

export const userApi = {
  uploadSelfPhoto: (file) => {
    const form = new FormData();
    form.append('file', file);
    return axios.post('/users/me/photo', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export const deptHeadApi = {
  getPendingDuties: () => axios.get('/depthead/duties/pending'),
  getAllDuties: () => axios.get('/depthead/duties'),
  getScholars: () => axios.get('/depthead/duties/scholars'),
  getScholarDetails: (schoolId) =>
    axios.get(`/depthead/duties/scholars/${schoolId}/details`),
  approveDuty: (id, remarks) =>
    axios.put(`/depthead/duties/${id}/approve`, { remarks }),
  rejectDuty: (id, remarks) =>
    axios.put(`/depthead/duties/${id}/reject`, { remarks }),
  changeDutyDecision: (id, newStatus, remarks) =>
    axios.put(`/depthead/duties/${id}/decision`, { newStatus, remarks }),
};

export default axios;