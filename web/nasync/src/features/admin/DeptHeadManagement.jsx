import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { adminApi } from '../../shared/api';
import '../../shared/styles/admin.css';

const DEFAULT_REG_FORM = {
  schoolId: '', firstName: '', lastName: '',
  email: '', personalGmail: '', deptId: '', branchId: '',
};

export default function DeptHeadManagement() {
  const [deptHeads, setDeptHeads] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [success, setSuccess] = useState('');

  // Register modal
  const [showReg, setShowReg] = useState(false);
  const [regForm, setRegForm] = useState(DEFAULT_REG_FORM);
  const [regError, setRegError] = useState('');
  const [regSubmitting, setRegSubmitting] = useState(false);

  // Info modal
  const [infoDH, setInfoDH] = useState(null);

  // Photo state — register form
  const [regPhotoFile, setRegPhotoFile] = useState(null);
  const [regPhotoPreview, setRegPhotoPreview] = useState(null);
  const [regPhotoError, setRegPhotoError] = useState('');

  // Photo state — edit form
  const [editPhotoFile, setEditPhotoFile] = useState(null);
  const [editPhotoPreview, setEditPhotoPreview] = useState(null);
  const [editPhotoError, setEditPhotoError] = useState('');

  // Edit modal
  const [editDH, setEditDH] = useState(null);
  const [editForm, setEditForm] = useState(null);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const [dhRes, deptsRes, branchesRes] = await Promise.all([
        adminApi.getDepartmentHeads(),
        adminApi.getDepartments(),
        adminApi.getBranches(),
      ]);
      setDeptHeads(dhRes.data?.data || dhRes.data || []);
      setDepartments(deptsRes.data?.data || deptsRes.data || []);
      setBranches(branchesRes.data?.data || branchesRes.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const branchesFor = (deptId) =>
    branches.filter(b => b.deptId === Number(deptId) || b.departmentId === Number(deptId));

  const filtered = deptHeads.filter(dh => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (
      dh.firstName?.toLowerCase().includes(q) ||
      dh.lastName?.toLowerCase().includes(q) ||
      dh.schoolId?.toLowerCase().includes(q) ||
      dh.email?.toLowerCase().includes(q)
    );
  });

  // ── Register modal ──────────────────────────────────────────────
  const openReg = () => {
    setRegForm(DEFAULT_REG_FORM);
    setRegError('');
    setSuccess('');
    setShowReg(true);
  };
  const closeReg = () => {
    setShowReg(false);
    setRegForm(DEFAULT_REG_FORM);
    setRegPhotoFile(null); setRegPhotoPreview(null); setRegPhotoError('');
  };

  const handleRegChange = (e) => {
    const { name, value } = e.target;
    setRegForm(prev => ({ ...prev, [name]: value, ...(name === 'deptId' ? { branchId: '' } : {}) }));
  };

  const handleRegSubmit = async (e) => {
    e.preventDefault();
    setRegError('');
    if (!regForm.schoolId || !regForm.firstName || !regForm.lastName || !regForm.email || !regForm.deptId || !regForm.branchId) {
      setRegError('Please fill in all required fields.');
      return;
    }
    setRegSubmitting(true);
    try {
      const regRes = await adminApi.registerUser({
        schoolId: regForm.schoolId,
        firstName: regForm.firstName,
        lastName: regForm.lastName,
        email: regForm.email,
        personalGmail: regForm.personalGmail || undefined,
        password: regForm.schoolId,
        role: 'DEPARTMENT_HEAD',
        deptId: Number(regForm.deptId),
        branchId: Number(regForm.branchId),
      });
      if (regPhotoFile && regRes.data?.userId) {
        try { await adminApi.uploadUserPhoto(regRes.data.userId, regPhotoFile); } catch { /* non-fatal */ }
      }
      setSuccess(`Department Head ${regForm.firstName} ${regForm.lastName} registered successfully.`);
      closeReg();
      loadData();
    } catch (err) {
      setRegError(err.response?.data?.error || err.message || 'Registration failed');
    } finally {
      setRegSubmitting(false);
    }
  };

  // ── Photo helpers ───────────────────────────────────────────────
  const validateAndPreview = (file, setFile, setPreview, setError) => {
    if (!file) return;
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      setError('Only JPG and PNG files are allowed.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setError('File must be 5 MB or smaller.');
      return;
    }
    setError('');
    setFile(file);
    setPreview(URL.createObjectURL(file));
  };

  const handleRegPhotoChange = (e) =>
    validateAndPreview(e.target.files[0], setRegPhotoFile, setRegPhotoPreview, setRegPhotoError);

  const handleEditPhotoChange = (e) =>
    validateAndPreview(e.target.files[0], setEditPhotoFile, setEditPhotoPreview, setEditPhotoError);

  // ── Info modal ──────────────────────────────────────────────────
  const openInfo = (dh) => setInfoDH(dh);
  const closeInfo = () => setInfoDH(null);

  // ── Edit modal ──────────────────────────────────────────────────
  const openEdit = (dh) => {
    setEditDH(dh);
    setEditError('');
    setEditPhotoFile(null);
    setEditPhotoPreview(dh.profilePhotoUrl || null);
    setEditPhotoError('');
    setEditForm({
      firstName: dh.firstName || '',
      lastName: dh.lastName || '',
      email: dh.email || '',
      personalGmail: dh.personalGmail || '',
      deptId: dh.departmentId ? String(dh.departmentId) : '',
      branchId: dh.branchId ? String(dh.branchId) : '',
    });
  };
  const closeEdit = () => {
    setEditDH(null); setEditForm(null); setEditError('');
    setEditPhotoFile(null); setEditPhotoPreview(null); setEditPhotoError('');
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({ ...prev, [name]: value, ...(name === 'deptId' ? { branchId: '' } : {}) }));
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditError('');
    if (!editForm.firstName || !editForm.lastName || !editForm.email || !editForm.deptId || !editForm.branchId) {
      setEditError('Please fill in all required fields.');
      return;
    }
    setEditSubmitting(true);
    try {
      if (editPhotoFile) {
        try { await adminApi.uploadUserPhoto(editDH.userId, editPhotoFile); } catch { /* non-fatal */ }
      }
      await adminApi.updateUser(editDH.userId, {
        schoolId: editDH.schoolId,
        firstName: editForm.firstName,
        lastName: editForm.lastName,
        email: editForm.email,
        personalGmail: editForm.personalGmail || null,
        password: editDH.schoolId,
        role: 'DEPARTMENT_HEAD',
        deptId: Number(editForm.deptId),
        branchId: Number(editForm.branchId),
      });
      setSuccess(`Department Head ${editForm.firstName} ${editForm.lastName} updated successfully.`);
      closeEdit();
      loadData();
    } catch (err) {
      setEditError(err.response?.data?.error || err.message || 'Update failed');
    } finally {
      setEditSubmitting(false);
    }
  };

  const toggleActive = async (userId) => {
    try {
      await adminApi.toggleUserActive(userId);
      loadData();
    } catch {
      alert('Failed to update user status');
    }
  };

  const AvatarPicker = ({ preview, initials, onChange, error }) => (
    <div style={{ textAlign: 'center', marginBottom: '1.25rem' }}>
      <label style={{ cursor: 'pointer', display: 'inline-block' }} title="Click to upload photo">
        {preview ? (
          <img src={preview} alt="Preview"
            style={{ width: 88, height: 88, borderRadius: '50%', objectFit: 'cover', border: '2px solid #6366f1', display: 'block' }} />
        ) : (
          <div style={{ width: 88, height: 88, borderRadius: '50%', background: '#1e3a5f', color: '#fff', fontSize: '2rem', fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px dashed #6366f1' }}>
            {initials || '?'}
          </div>
        )}
        <div style={{ fontSize: '0.75rem', color: '#6366f1', marginTop: '4px' }}>Click to upload photo</div>
        <input type="file" accept="image/jpeg,image/png" onChange={onChange} style={{ display: 'none' }} />
      </label>
      <div style={{ fontSize: '0.7rem', color: '#9ca3af' }}>JPG or PNG · max 5 MB</div>
      {error && <div className="alert alert-error" style={{ textAlign: 'left', marginTop: '4px', fontSize: '0.8rem' }}>{error}</div>}
    </div>
  );

  const IconBtn = ({ onClick, title, color, children }) => (
    <button
      className="btn btn-sm"
      style={{ border: `1px solid ${color}`, background: 'transparent', color, cursor: 'pointer' }}
      onClick={onClick}
      title={title}
    >
      {children}
    </button>
  );

  return (
    <Layout pageTitle="Department Head Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Department Heads</h1>
            <p className="text-muted">{deptHeads.length} department heads</p>
          </div>
          <button className="btn btn-primary" onClick={openReg}>+ Register Dept Head</button>
        </div>

        {success && (
          <div className="alert alert-success">
            {success}
            <button className="alert-close" onClick={() => setSuccess('')}>×</button>
          </div>
        )}

        <div className="card">
          <div className="card-body">
            <div className="search-bar">
              <input
                type="text"
                className="form-control"
                placeholder="Search by name or school ID..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <div className="table-responsive">
              {loading ? (
                <div className="loading-state"><div className="spinner"></div></div>
              ) : filtered.length === 0 ? (
                <div className="empty-state"><p>No department heads found</p></div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>School ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Department</th>
                      <th>Branch</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map(dh => (
                      <tr key={dh.userId}>
                        <td><code>{dh.schoolId}</code></td>
                        <td>{dh.lastName}, {dh.firstName}</td>
                        <td>{dh.email}</td>
                        <td>{dh.departmentName || '—'}</td>
                        <td>{dh.branchName || '—'}</td>
                        <td>
                          <span className={`badge ${dh.isActive ? 'badge-success' : 'badge-danger'}`}>
                            {dh.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '4px' }}>
                            <IconBtn onClick={() => openInfo(dh)} title="View Details" color="#6366f1">ℹ</IconBtn>
                            <IconBtn onClick={() => openEdit(dh)} title="Edit Dept Head" color="#0ea5e9">🖍</IconBtn>
                            <IconBtn
                              onClick={() => toggleActive(dh.userId)}
                              title={dh.isActive ? 'Deactivate' : 'Activate'}
                              color={dh.isActive ? '#6b7280' : '#16a34a'}
                            >
                              {dh.isActive ? '⊘' : '●'}
                            </IconBtn>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* ── Register Modal ─────────────────────────────────────────── */}
      {showReg && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeReg()}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <h3>Register Department Head</h3>
              <button className="modal-close" onClick={closeReg}>×</button>
            </div>
            <form onSubmit={handleRegSubmit}>
              <div className="modal-body">
                {regError && <div className="alert alert-error">{regError}</div>}
                <AvatarPicker
                  preview={regPhotoPreview}
                  initials={`${regForm.firstName?.[0]?.toUpperCase() ?? ''}${regForm.lastName?.[0]?.toUpperCase() ?? ''}`}
                  onChange={handleRegPhotoChange}
                  error={regPhotoError}
                />
                <div className="alert alert-info">
                  <strong>Default password</strong> will be set to the user's <strong>School ID</strong>.
                </div>
                <div className="form-group">
                  <label className="form-label">School ID *</label>
                  <input type="text" name="schoolId" className="form-control" value={regForm.schoolId}
                    onChange={handleRegChange} placeholder="e.g. 21-0001" required />
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">First Name *</label>
                    <input type="text" name="firstName" className="form-control" value={regForm.firstName}
                      onChange={handleRegChange} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Last Name *</label>
                    <input type="text" name="lastName" className="form-control" value={regForm.lastName}
                      onChange={handleRegChange} required />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">CIT Email *</label>
                    <input type="email" name="email" className="form-control" value={regForm.email}
                      onChange={handleRegChange} placeholder="name@cit.edu" required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Personal Gmail</label>
                    <input type="email" name="personalGmail" className="form-control" value={regForm.personalGmail}
                      onChange={handleRegChange} placeholder="For Google login" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Department *</label>
                    <select name="deptId" className="form-control" value={regForm.deptId}
                      onChange={handleRegChange} required>
                      <option value="">Select department</option>
                      {departments.map(d => (
                        <option key={d.departmentId} value={d.departmentId}>{d.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Branch *</label>
                    <select name="branchId" className="form-control" value={regForm.branchId}
                      onChange={handleRegChange} disabled={!regForm.deptId} required>
                      <option value="">Select branch</option>
                      {branchesFor(regForm.deptId).map(b => (
                        <option key={b.branchId} value={b.branchId}>{b.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={closeReg}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={regSubmitting}>
                  {regSubmitting ? <><span className="btn-spinner" />Registering...</> : 'Register Dept Head'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Info Modal ────────────────────────────────────────────── */}
      {infoDH && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeInfo()}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                {infoDH.profilePhotoUrl ? (
                  <img
                    src={infoDH.profilePhotoUrl}
                    alt="Profile"
                    onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                    style={{ width: 60, height: 60, borderRadius: '50%', objectFit: 'cover', border: '2px solid #e5e7eb', flexShrink: 0, display: 'block' }}
                  />
                ) : null}
                <div style={{
                  width: 60, height: 60, borderRadius: '50%', background: '#1e3a5f',
                  color: '#fff', fontSize: '1.25rem', fontWeight: 700, flexShrink: 0,
                  display: infoDH.profilePhotoUrl ? 'none' : 'flex',
                  alignItems: 'center', justifyContent: 'center',
                }}>
                  {`${infoDH.firstName?.[0]?.toUpperCase() ?? ''}${infoDH.lastName?.[0]?.toUpperCase() ?? ''}`}
                </div>
                <div>
                  <h3 style={{ margin: 0 }}>{infoDH.lastName}, {infoDH.firstName}</h3>
                  <p style={{ margin: '2px 0 0', fontSize: '0.875rem', color: '#6b7280', fontWeight: 400 }}>
                    {infoDH.schoolId} &nbsp;·&nbsp; {infoDH.email}
                  </p>
                </div>
              </div>
              <button className="modal-close" onClick={closeInfo}>×</button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 1.5rem', fontSize: '0.875rem' }}>
                <div>
                  <span style={{ color: '#6b7280' }}>Department</span><br />
                  <strong>{infoDH.departmentName || '—'}</strong>
                </div>
                <div>
                  <span style={{ color: '#6b7280' }}>Branch</span><br />
                  <strong>{infoDH.branchName || '—'}</strong>
                </div>
                {infoDH.personalGmail && (
                  <div>
                    <span style={{ color: '#6b7280' }}>Personal Gmail</span><br />
                    <strong>{infoDH.personalGmail}</strong>
                  </div>
                )}
                <div>
                  <span style={{ color: '#6b7280' }}>Status</span><br />
                  <span className={`badge ${infoDH.isActive ? 'badge-success' : 'badge-danger'}`}>
                    {infoDH.isActive ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <div>
                  <span style={{ color: '#6b7280' }}>First-Time Login</span><br />
                  <span className={`badge ${infoDH.firstTimeLogin ? 'badge-warning' : 'badge-muted'}`}>
                    {infoDH.firstTimeLogin ? 'Pending password change' : 'Password set'}
                  </span>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={closeInfo}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ────────────────────────────────────────────── */}
      {editDH && editForm && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeEdit()}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <div>
                <h3 style={{ margin: 0 }}>Edit Department Head</h3>
                <p style={{ margin: '2px 0 0', fontSize: '0.875rem', color: '#6b7280', fontWeight: 400 }}>
                  {editDH.schoolId}
                </p>
              </div>
              <button className="modal-close" onClick={closeEdit}>×</button>
            </div>
            <form onSubmit={handleEditSubmit}>
              <div className="modal-body">
                {editError && <div className="alert alert-error">{editError}</div>}
                <AvatarPicker
                  preview={editPhotoPreview}
                  initials={`${editForm.firstName?.[0]?.toUpperCase() ?? ''}${editForm.lastName?.[0]?.toUpperCase() ?? ''}`}
                  onChange={handleEditPhotoChange}
                  error={editPhotoError}
                />
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">First Name *</label>
                    <input type="text" name="firstName" className="form-control"
                      value={editForm.firstName} onChange={handleEditChange} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Last Name *</label>
                    <input type="text" name="lastName" className="form-control"
                      value={editForm.lastName} onChange={handleEditChange} required />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">CIT Email *</label>
                    <input type="email" name="email" className="form-control"
                      value={editForm.email} onChange={handleEditChange} required />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Personal Gmail</label>
                    <input type="email" name="personalGmail" className="form-control"
                      value={editForm.personalGmail} onChange={handleEditChange} placeholder="For Google login" />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Department *</label>
                    <select name="deptId" className="form-control" value={editForm.deptId}
                      onChange={handleEditChange} required>
                      <option value="">Select department</option>
                      {departments.map(d => (
                        <option key={d.departmentId} value={d.departmentId}>{d.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Branch *</label>
                    <select name="branchId" className="form-control" value={editForm.branchId}
                      onChange={handleEditChange} disabled={!editForm.deptId} required>
                      <option value="">Select branch</option>
                      {branchesFor(editForm.deptId).map(b => (
                        <option key={b.branchId} value={b.branchId}>{b.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={closeEdit}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={editSubmitting}>
                  {editSubmitting ? <><span className="btn-spinner" />Saving...</> : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </Layout>
  );
}
