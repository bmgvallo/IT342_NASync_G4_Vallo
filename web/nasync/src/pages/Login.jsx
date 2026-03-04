import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Login.css';

export default function Login() {
  const { login, loading, error, setError, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [schoolId, setSchoolId] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      if (user?.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else if (user?.role === 'SCHOLAR') {
        navigate('/scholar', { replace: true });
      } else if (user?.role === 'DEPARTMENT_HEAD') {
        navigate('/depthead', { replace: true });
      }
    }
  }, [isAuthenticated, user, navigate]);

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    
    try {
      const user = await login(schoolId, password);
      
      sessionStorage.setItem('showLoginToast', 'true');
      
      if (user.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else if (user.role === 'SCHOLAR') {
        navigate('/scholar', { replace: true });
      } else if (user.role === 'DEPARTMENT_HEAD') {
        navigate('/depthead', { replace: true });
      } else {
        navigate(from, { replace: true });
      }
      
    } catch (err) {
    }
  };

  if (isAuthenticated) {
    return null;
  }

  return (
    <div className="login-root">
      <div className="login-brand">
        <div className="login-brand-inner">
          <div className="login-logo">
            <span className="login-logo-text">NASync</span>
          </div>
          <h1 className="login-brand-title">
            NAS Duty<br/>Management System
          </h1>
          <p className="login-brand-sub">
            Office of Admission and Scholarship — CIT University
          </p>
        </div>
        <div className="login-brand-footer">
          Cebu Institute of Technology - University
        </div>
      </div>

      <div className="login-form-panel">
        <div className="login-form-container">
          <div className="login-form-header">
            <h2>Sign In</h2>
            <p>Enter your school credentials to continue</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form" noValidate>
            <div className="form-group">
              <label className="form-label">School ID</label>
              <input
                type="text"
                className="form-control"
                placeholder="e.g. 21-0001"
                value={schoolId}
                onChange={e => setSchoolId(e.target.value)}
                autoComplete="username"
                disabled={loading}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="pw-wrapper">
                <input
                  type={showPw ? 'text' : 'password'}
                  className="form-control"
                  placeholder="Enter your password"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  autoComplete="current-password"
                  disabled={loading}
                  required
                />
                <button
                  type="button"
                  className="pw-toggle"
                  onClick={() => setShowPw(v => !v)}
                  tabIndex={-1}
                  disabled={loading}
                >
                  {showPw ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-lg login-button"
              disabled={loading || !schoolId || !password}
            >
              {loading ? 'Signing In...' : 'Sign In'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}