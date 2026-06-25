import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import './Login.css';

//environment variables
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const GOOGLE_AUTH_URL = import.meta.env.VITE_GOOGLE_AUTH_URL || 'http://localhost:8080/login/oauth2/authorization/google';

export default function Login() {
  const { login, loading, error, setError, isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [schoolId, setSchoolId] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  // redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      if (user?.firstTimeLogin) {
        navigate('/change-password', { replace: true });
        return;
      }
      if (user?.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else if (user?.role === 'SCHOLAR') {
        navigate('/scholar', { replace: true });
      } else if (user?.role === 'DEPARTMENT_HEAD') {
        navigate('/depthead', { replace: true });
      }
    }
  }, [isAuthenticated, user, navigate]);

  // display OAuth error forwarded via ?error query param
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const oauthError = params.get('error');
    if (oauthError) {
      setError(oauthError);
    }
  }, [location.search]);

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
      e.preventDefault();
      setError(null);
      
      try {
          const result = await login(schoolId, password);
          
          if (result.mustChangePassword) {
              navigate('/change-password', { state: { schoolId } });
              return;
          }
          
          sessionStorage.setItem('showLoginToast', 'true');
          
          if (result.user.role === 'ADMIN') {
              navigate('/admin', { replace: true });
          } else if (result.user.role === 'SCHOLAR') {
              navigate('/scholar', { replace: true });
          } else if (result.user.role === 'DEPARTMENT_HEAD') {
              navigate('/depthead', { replace: true });
          } else {
              navigate(from, { replace: true });
          }
      } catch (err) {
      }
  };

  const handleGoogleLogin = () => {
  const googleAuthUrl = import.meta.env.VITE_GOOGLE_AUTH_URL || 'http://localhost:8080/oauth2/authorization/google';
  window.location.href = googleAuthUrl;
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
          <div className="login-brand-features">
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Real-time duty tracking
            </div>
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Department head approvals
            </div>
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Automated late & absent monitoring
            </div>
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Makeup hour computation
            </div>
          </div>
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

          {error && (
            <div className="alert alert-error">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd"/>
              </svg>
              <div>
                <strong>Login Failed:</strong> {error}
              </div>
            </div>
          )}

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
                disabled={loading || googleLoading}
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
                  disabled={loading || googleLoading}
                  required
                />
                <button
                  type="button"
                  className="pw-toggle"
                  onClick={() => setShowPw(v => !v)}
                  tabIndex={-1}
                  disabled={loading || googleLoading}
                >
                  {showPw ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-lg login-button"
              disabled={loading || !schoolId || !password || googleLoading}
            >
              {loading ? 'Signing In...' : 'Sign In'}
            </button>
          </form>

          <div className="login-divider">
            <span>or</span>
          </div>

          <button
            type="button"
            className="btn btn-ghost btn-lg google-btn"
            onClick={handleGoogleLogin}
            disabled={loading || googleLoading}
          >
            {googleLoading ? (
              <>
                <span className="spinner"></span>
                Redirecting to Google...
              </>
            ) : (
              <>
                <svg width="18" height="18" viewBox="0 0 48 48">
                  <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                  <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                  <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
                  <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.36-8.16 2.36-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                </svg>
                Continue with Google
              </>
            )}
          </button>

          <div className="login-roles-hint">
            <p>Access is role-based. Contact OAS Admin if you need account assistance.</p>
            <div className="login-roles">
              <span className="role-chip role-admin">Admin</span>
              <span className="role-chip role-scholar">Scholar</span>
              <span className="role-chip role-depthead">Dept Head</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}