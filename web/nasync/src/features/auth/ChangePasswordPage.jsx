import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { authApi } from '../../shared/api';
import Layout from '../../shared/components/Layout';
import '../../shared/styles/admin.css';

export default function ChangePasswordPage() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const getRoleDashboard = () => {
    if (user?.role === 'ADMIN') return '/admin';
    if (user?.role === 'DEPARTMENT_HEAD') return '/depthead';
    return '/scholar';
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.');
      return;
    }
    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters.');
      return;
    }

    setLoading(true);
    try {
      await authApi.changePassword(currentPassword, newPassword);
      updateUser({ ...user, firstTimeLogin: false });
      setSuccess('Password changed successfully. Redirecting...');
      setTimeout(() => navigate(getRoleDashboard(), { replace: true }), 1500);
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to change password.');
    } finally {
      setLoading(false);
    }
  };

  const isFirstTime = user?.firstTimeLogin === true;

  return (
    <Layout pageTitle="Change Password">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Change Password</h1>
            <p className="text-muted">
              {isFirstTime
                ? 'You must set a new password before continuing. Your temporary password is your School ID.'
                : 'Update your account password.'}
            </p>
          </div>
        </div>

        <div className="card" style={{ maxWidth: '480px' }}>
          <div className="card-body">
            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Current Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={currentPassword}
                  onChange={e => setCurrentPassword(e.target.value)}
                  required
                  disabled={loading}
                  autoComplete="current-password"
                />
              </div>
              <div className="form-group">
                <label className="form-label">New Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={newPassword}
                  onChange={e => setNewPassword(e.target.value)}
                  required
                  disabled={loading}
                  placeholder="Minimum 8 characters"
                  autoComplete="new-password"
                />
              </div>
              <div className="form-group">
                <label className="form-label">Confirm New Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={confirmPassword}
                  onChange={e => setConfirmPassword(e.target.value)}
                  required
                  disabled={loading}
                  autoComplete="new-password"
                />
              </div>

              <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading || !currentPassword || !newPassword || !confirmPassword}
                >
                  {loading ? 'Changing...' : 'Change Password'}
                </button>
                {!isFirstTime && (
                  <button
                    type="button"
                    className="btn btn-ghost"
                    onClick={() => navigate(getRoleDashboard())}
                    disabled={loading}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      </div>
    </Layout>
  );
}
