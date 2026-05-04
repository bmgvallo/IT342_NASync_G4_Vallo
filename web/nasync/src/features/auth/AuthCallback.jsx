import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export default function AuthCallback() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');
    const error = params.get('error');

    console.log('AuthCallback - Token:', token ? 'present' : 'missing');
    console.log('AuthCallback - Error:', error);

    if (error) {
      navigate('/login?error=' + encodeURIComponent(error));
      return;
    }

    if (!token) {
      navigate('/login');
      return;
    }

    // store token from local storage
    localStorage.setItem('accessToken', token);
    
    // fetch user info from /me endpoint (auth controller)
    fetch('http://localhost:8080/api/v1/auth/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(res => {
      if (!res.ok) throw new Error('Failed to fetch user info');
      return res.json();
    })
    .then(user => {
      console.log('User fetched:', user);
      
      // store user in localStorage
      localStorage.setItem('user', JSON.stringify(user));
      sessionStorage.setItem('showLoginToast', 'true');
      
      if (user.role === 'ADMIN') {
        window.location.href = '/admin';
      } else if (user.role === 'SCHOLAR') {
        window.location.href = '/scholar';
      } else if (user.role === 'DEPARTMENT_HEAD') {
        window.location.href = '/depthead';
      } else {
        window.location.href = '/';
      }
    })
    .catch(err => {
      console.error('Error fetching user:', err);
      navigate('/login?error=' + encodeURIComponent(err.message));
    });
  }, [location, navigate]);

  return (
    <div className="auth-callback" style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      alignItems: 'center', 
      justifyContent: 'center', 
      height: '100vh' 
    }}>
      <div className="spinner" style={{ width: '40px', height: '40px' }}></div>
      <p style={{ marginTop: '20px' }}>Completing login...</p>
    </div>
  );
}