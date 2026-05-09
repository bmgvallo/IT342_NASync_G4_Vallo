import { useState, useEffect } from 'react';
import { adminApi } from '../api';
import '../styles/Modal.css';

export default function UserDetailModal({ user, onClose, onUpdate }) {
  const [departments, setDepartments] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    personalGmail: user?.personalGmail || '',
    deptId: user?.departmentId || '',
    branchId: user?.branchId || '',
    shift: user?.shift || 'MORNING',
    expectedTimeIn: user?.expectedTimeIn || '08:00',
    expectedTimeOut: user?.expectedTimeOut || '12:00'
  });
  const [message, setMessage] = useState({ text: '', type: '' });

  useEffect(() => {
    const loadData = async () => {
      try {
        const [deptsRes, branchesRes] = await Promise.all([
          adminApi.getDepartments(),
          adminApi.getBranches()
        ]);
        setDepartments(deptsRes.data?.data || []);
        setBranches(branchesRes.data?.data || []);
      } catch (err) {
        console.error(err);
      }
    };
    loadData();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleUpdate = async () => {
    setLoading(true);
    try {
      await adminApi.updateUser(user.userId, form);
      setMessage({ text: 'User updated successfully!', type: 'success' });
      setEditing(false);
      if (onUpdate) onUpdate();
      setTimeout(() => setMessage({ text: '', type: '' }), 3000);
    } catch (err) {
      setMessage({ text: err.response?.data?.error || 'Update failed', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async () => {
    if (!confirm(`Reset password for ${user.firstName} ${user.lastName} to their School ID?`)) return;
    setLoading(true);
    try {
      await adminApi.resetPassword(user.userId);
      setMessage({ text: 'Password reset to School ID', type: 'success' });
      setTimeout(() => setMessage({ text: '', type: '' }), 3000);
    } catch (err) {
      setMessage({ text: err.response?.data?.error || 'Reset failed', type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const filteredBranches = branches.filter(b => b.deptId === Number(form.deptId));

  if (!user) return null;

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal modal-lg">
        <div className="modal-header">
          <h3>
            {user.firstName} {user.lastName}
            <span className={`badge ${user.role === 'ADMIN' ? 'badge-maroon' : user.role === 'SCHOLAR' ? 'badge-info' : 'badge-gold'}`}>
              {user.role}
            </span>
          </h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>

        <div className="modal-body">
          {message.text && (
            <div className={`alert alert-${message.type}`}>
              {message.text}
              <button className="alert-close" onClick={() => setMessage({ text: '', type: '' })}>×</button>
            </div>
          )}

          <div className="user-detail-grid">
            <div className="info-card">
              <h4>Basic Information</h4>
              <div className="info-row">
                <label>School ID:</label>
                <span><code>{user.schoolId}</code></span>
              </div>
              <div className="info-row">
                <label>Email:</label>
                <span>{user.email}</span>
              </div>
              <div className="info-row">
                <label>Personal Gmail:</label>
                <span>{user.personalGmail || '—'}</span>
              </div>
              <div className="info-row">
                <label>Status:</label>
                <span className={`badge ${user.active ? 'badge-success' : 'badge-danger'}`}>
                  {user.active ? 'Active' : 'Inactive'}
                </span>
              </div>
            </div>

            <div className="info-card">
              <h4>Assignment</h4>
              <div className="info-row">
                <label>Department:</label>
                <span>{user.departmentName || '—'}</span>
              </div>
              <div className="info-row">
                <label>Branch:</label>
                <span>{user.branchName || '—'}</span>
              </div>
              {user.role === 'SCHOLAR' && (
                <>
                  <div className="info-row">
                    <label>Shift:</label>
                    <span>{user.shift || '—'}</span>
                  </div>
                  <div className="info-row">
                    <label>Expected Time In:</label>
                    <span>{user.expectedTimeIn || '—'}</span>
                  </div>
                  <div className="info-row">
                    <label>Expected Time Out:</label>
                    <span>{user.expectedTimeOut || '—'}</span>
                  </div>
                </>
              )}
            </div>
          </div>

          {editing ? (
            <div className="edit-form">
              <h4>Edit User</h4>
              <div className="form-row">
                <div className="form-group">
                  <label>First Name</label>
                  <input type="text" name="firstName" className="form-control" value={form.firstName} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Last Name</label>
                  <input type="text" name="lastName" className="form-control" value={form.lastName} onChange={handleChange} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Email</label>
                  <input type="email" name="email" className="form-control" value={form.email} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Personal Gmail</label>
                  <input type="email" name="personalGmail" className="form-control" value={form.personalGmail} onChange={handleChange} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Department</label>
                  <select name="deptId" className="form-control" value={form.deptId} onChange={handleChange}>
                    <option value="">Select department</option>
                    {departments.map(d => (
                      <option key={d.departmentId} value={d.departmentId}>{d.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Branch</label>
                  <select name="branchId" className="form-control" value={form.branchId} onChange={handleChange} disabled={!form.deptId}>
                    <option value="">Select branch</option>
                    {filteredBranches.map(b => (
                      <option key={b.branchId} value={b.branchId}>{b.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              {user.role === 'SCHOLAR' && (
                <div className="form-row">
                  <div className="form-group">
                    <label>Shift</label>
                    <select name="shift" className="form-control" value={form.shift} onChange={handleChange}>
                      <option value="MORNING">Morning</option>
                      <option value="AFTERNOON">Afternoon</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Expected Time In</label>
                    <input type="time" name="expectedTimeIn" className="form-control" value={form.expectedTimeIn} onChange={handleChange} />
                  </div>
                  <div className="form-group">
                    <label>Expected Time Out</label>
                    <input type="time" name="expectedTimeOut" className="form-control" value={form.expectedTimeOut} onChange={handleChange} />
                  </div>
                </div>
              )}
            </div>
          ) : null}
        </div>

        <div className="modal-footer">
          {!editing ? (
            <>
              <button className="btn btn-secondary" onClick={() => setEditing(true)}>Edit</button>
              <button className="btn btn-ghost" onClick={handleResetPassword} disabled={loading}>Reset Password</button>
            </>
          ) : (
            <>
              <button className="btn btn-ghost" onClick={() => setEditing(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleUpdate} disabled={loading}>
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
            </>
          )}
          <button className="btn btn-ghost" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}