import React, { createContext, useState, useContext, useEffect } from 'react';
import { authApi } from '../api';
import { jwtDecode } from 'jwt-decode';

export const DEFAULT_PASSWORD = 'default123';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('accessToken');
      const storedUser = localStorage.getItem('user');
      
      if (token && storedUser) {
        try {
          const decoded = jwtDecode(token);
          const currentTime = Date.now() / 1000;
          
          if (decoded.exp > currentTime) {
            setUser(JSON.parse(storedUser));
          } else {
            localStorage.clear();
            setUser(null);
          }
        } catch (err) {
          localStorage.clear();
          setUser(null);
        }
      }
      setLoading(false);
    };

    checkAuth();
  }, []);

  const login = async (schoolId, password) => {
    setError(null);
    try {
      const response = await authApi.login(schoolId, password);
      const { accessToken, user } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('user', JSON.stringify(user));
      setUser(user);
      
      return user;
    } catch (err) {
      const message = err.response?.data?.error || 'Login failed';
      setError(message);
      throw new Error(message);
    }
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      localStorage.clear();
      setUser(null);
    }
  };

  const value = {
    user,
    login,
    logout,
    loading,
    error,
    setError,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    isScholar: user?.role === 'SCHOLAR',
    isDepartmentHead: user?.role === 'DEPARTMENT_HEAD',
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};