import { useState, useRef } from 'react';
import Layout from '../../shared/components/Layout';
import { useAuth } from '../auth/AuthContext';
import { authApi, userApi } from '../../shared/api';
import '../../shared/styles/admin.css';
import '../../shared/styles/Modal.css';

export default function ProfilePage() {
  const { user, updateUser } = useAuth();
  const fileInputRef = useRef(null);

  const [photoUrl, setPhotoUrl] = useState(user?.profilePhotoUrl || null);
  const [photoLoading, setPhotoLoading] = useState(false);
  const [photoError, setPhotoError] = useState('');

  const [gmail, setGmail] = useState(user?.personalGmail || '');
  const [gmailLoading, setGmailLoading] = useState(false);
  const [gmailMsg, setGmailMsg] = useState({ text: '', type: '' });

  const [pwForm, setPwForm] = useState({ current: '', newPw: '', confirm: '' });
  const [pwLoading, setPwLoading] = useState(false);
  const [pwMsg, setPwMsg] = useState({ text: '', type: '' });

  const initials = `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || '?';

  const handlePhotoChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setPhotoLoading(true);
    setPhotoError('');
    try {
      const res = await userApi.uploadSelfPhoto(file);
      const newUrl = res.data.photoUrl;
      setPhotoUrl(newUrl);
      updateUser({ ...user, profilePhotoUrl: newUrl });
    } catch (err) {
      setPhotoError(err.response?.data?.error || 'Photo upload failed.');
    } finally {
      setPhotoLoading(false);
      e.target.value = '';
    }
  };

  const handleGmailSave = async () => {
    setGmailLoading(true);
    setGmailMsg({ text: '', type: '' });
    try {
      await authApi.updateGmail(gmail);
      updateUser({ ...user, personalGmail: gmail });
      setGmailMsg({ text: 'Gmail saved.', type: 'success' });
    } catch (err) {
      setGmailMsg({ text: err.response?.data?.error || 'Failed to save Gmail.', type: 'error' });
    } finally {
      setGmailLoading(false);
    }
  };

  const handlePwChange = async (e) => {
    e.preventDefault();
    setPwMsg({ text: '', type: '' });
    if (pwForm.newPw !== pwForm.confirm) {
      setPwMsg({ text: 'New passwords do not match.', type: 'error' });
      return;
    }
    if (pwForm.newPw.length < 8) {
      setPwMsg({ text: 'New password must be at least 8 characters.', type: 'error' });
      return;
    }
    setPwLoading(true);
    try {
      await authApi.changePassword(pwForm.current, pwForm.newPw);
      setPwForm({ current: '', newPw: '', confirm: '' });
      setPwMsg({ text: 'Password changed successfully.', type: 'success' });
    } catch (err) {
      setPwMsg({ text: err.response?.data?.error || 'Failed to change password.', type: 'error' });
    } finally {
      setPwLoading(false);
    }
  };

  return (
    <Layout pageTitle="Profile">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Profile</h1>
            <p className="text-muted">Manage your account information.</p>
          </div>
        </div>

        <div style={{ maxWidth: '560px', display: 'flex', flexDirection: 'column', gap: '16px' }}>

          {/* Avatar */}
          <div className="card">
            <div className="card-body" style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
              <div
                style={{ position: 'relative', cursor: photoLoading ? 'default' : 'pointer', flexShrink: 0 }}
                onClick={() => !photoLoading && fileInputRef.current?.click()}
              >
                {photoUrl ? (
                  <img
                    src={photoUrl}
                    alt="Profile"
                    onError={() => setPhotoUrl(null)}
                    style={{ width: 80, height: 80, borderRadius: '50%', objectFit: 'cover', border: '2px solid #e5e7eb', display: 'block' }}
                  />
                ) : (
                  <div style={{
                    width: 80, height: 80, borderRadius: '50%', background: '#1e3a5f',
                    color: '#fff', fontSize: '1.75rem', fontWeight: 700,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    {initials}
                  </div>
                )}
                {photoLoading && (
                  <div style={{
                    position: 'absolute', inset: 0, borderRadius: '50%',
                    background: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <span className="btn-spinner" />
                  </div>
                )}
              </div>
              <div>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => fileInputRef.current?.click()}
                  disabled={photoLoading}
                >
                  {photoLoading ? 'Uploading…' : 'Change Photo'}
                </button>
                <p className="text-muted" style={{ fontSize: '0.75rem', marginTop: '4px' }}>JPG or PNG, max 5 MB</p>
                {photoError && (
                  <p style={{ color: 'var(--danger, #c62828)', fontSize: '0.8rem', marginTop: '4px' }}>{photoError}</p>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png"
                style={{ display: 'none' }}
                onChange={handlePhotoChange}
              />
            </div>
          </div>

          {/* User info (read-only) */}
          <div className="card">
            <div className="card-header"><h3>User Info</h3></div>
            <div className="card-body">
              <div className="info-row"><label>Full Name</label><span>{user?.firstName} {user?.lastName}</span></div>
              <div className="info-row"><label>School ID</label><span><code>{user?.schoolId}</code></span></div>
              <div className="info-row"><label>Department</label><span>{user?.departmentName || '—'}</span></div>
              <div className="info-row"><label>Branch</label><span>{user?.branchName || '—'}</span></div>
            </div>
          </div>

          {/* Personal Gmail */}
          <div className="card">
            <div className="card-header"><h3>Personal Gmail</h3></div>
            <div className="card-body">
              {gmailMsg.text && (
                <div className={`alert alert-${gmailMsg.type}`} style={{ marginBottom: '12px' }}>{gmailMsg.text}</div>
              )}
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Gmail Address</label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <input
                    type="email"
                    className="form-control"
                    value={gmail}
                    onChange={e => setGmail(e.target.value)}
                    placeholder="your.email@gmail.com"
                    disabled={gmailLoading}
                  />
                  <button className="btn btn-primary" onClick={handleGmailSave} disabled={gmailLoading}>
                    {gmailLoading ? 'Saving…' : 'Save'}
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Change Password */}
          <div className="card">
            <div className="card-header"><h3>Change Password</h3></div>
            <div className="card-body">
              {pwMsg.text && (
                <div className={`alert alert-${pwMsg.type}`} style={{ marginBottom: '12px' }}>{pwMsg.text}</div>
              )}
              <form onSubmit={handlePwChange}>
                <div className="form-group">
                  <label className="form-label">Current Password</label>
                  <input
                    type="password"
                    className="form-control"
                    value={pwForm.current}
                    onChange={e => setPwForm(p => ({ ...p, current: e.target.value }))}
                    disabled={pwLoading}
                    autoComplete="current-password"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">New Password</label>
                  <input
                    type="password"
                    className="form-control"
                    value={pwForm.newPw}
                    onChange={e => setPwForm(p => ({ ...p, newPw: e.target.value }))}
                    disabled={pwLoading}
                    placeholder="Minimum 8 characters"
                    autoComplete="new-password"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Confirm New Password</label>
                  <input
                    type="password"
                    className="form-control"
                    value={pwForm.confirm}
                    onChange={e => setPwForm(p => ({ ...p, confirm: e.target.value }))}
                    disabled={pwLoading}
                    autoComplete="new-password"
                  />
                </div>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={pwLoading || !pwForm.current || !pwForm.newPw || !pwForm.confirm}
                >
                  {pwLoading ? 'Changing…' : 'Change Password'}
                </button>
              </form>
            </div>
          </div>

        </div>
      </div>
    </Layout>
  );
}
