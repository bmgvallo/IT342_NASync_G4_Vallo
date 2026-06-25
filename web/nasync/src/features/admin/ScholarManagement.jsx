import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { adminApi } from '../../shared/api';
import { formatDate, formatTime } from '../../shared/utils/formatters';
import '../../shared/styles/admin.css';

const DAYS = [
  { label: 'MON', value: 'MONDAY' },
  { label: 'TUE', value: 'TUESDAY' },
  { label: 'WED', value: 'WEDNESDAY' },
  { label: 'THU', value: 'THURSDAY' },
  { label: 'FRI', value: 'FRIDAY' },
  { label: 'SAT', value: 'SATURDAY' },
];

const DAY_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const DEFAULT_REG_FORM = {
  schoolId: '', firstName: '', lastName: '',
  email: '', personalGmail: '', deptId: '', branchId: '',
};

const DEFAULT_SCHEDULE = {
  selectedDays: [], primaryTimeIn: '08:00', primaryTimeOut: '12:00',
  secondaryTimeIn: '', secondaryTimeOut: '',
};

export default function ScholarManagement() {
  const [scholars, setScholars] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [success, setSuccess] = useState('');

  // Register modal
  const [showReg, setShowReg] = useState(false);
  const [regForm, setRegForm] = useState(DEFAULT_REG_FORM);
  const [regSchedule, setRegSchedule] = useState(DEFAULT_SCHEDULE);
  const [regError, setRegError] = useState('');
  const [regSubmitting, setRegSubmitting] = useState(false);

  // Info modal
  const [infoScholar, setInfoScholar] = useState(null);
  const [infoSchedule, setInfoSchedule] = useState(null);
  const [infoLoading, setInfoLoading] = useState(false);

  // Photo state — register form
  const [regPhotoFile, setRegPhotoFile] = useState(null);
  const [regPhotoPreview, setRegPhotoPreview] = useState(null);
  const [regPhotoError, setRegPhotoError] = useState('');

  // Photo state — edit form
  const [editPhotoFile, setEditPhotoFile] = useState(null);
  const [editPhotoPreview, setEditPhotoPreview] = useState(null);
  const [editPhotoError, setEditPhotoError] = useState('');

  // Duty modal
  const [dutyScholar, setDutyScholar] = useState(null);
  const [dutySummary, setDutySummary] = useState(null);
  const [dutyHistory, setDutyHistory] = useState([]);
  const [dutyLoading, setDutyLoading] = useState(false);
  const [dutyFilter, setDutyFilter] = useState('ALL');
  const [reminderSending, setReminderSending] = useState(false);
  const [reminderMsg, setReminderMsg] = useState('');

  // Edit modal
  const [editScholar, setEditScholar] = useState(null);
  const [editForm, setEditForm] = useState(null);
  const [editSchedule, setEditSchedule] = useState(null);
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState('');
  const [schedLoading, setSchedLoading] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [scholarsRes, deptsRes, branchesRes] = await Promise.all([
        adminApi.getScholars(),
        adminApi.getDepartments(),
        adminApi.getBranches(),
      ]);
      setScholars(scholarsRes.data?.data || scholarsRes.data || []);
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

  const filtered = scholars.filter(s => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (
      s.firstName?.toLowerCase().includes(q) ||
      s.lastName?.toLowerCase().includes(q) ||
      s.schoolId?.toLowerCase().includes(q) ||
      s.email?.toLowerCase().includes(q)
    );
  });

  // ── Register modal ──────────────────────────────────────────────
  const openReg = () => {
    setRegForm(DEFAULT_REG_FORM);
    setRegSchedule(DEFAULT_SCHEDULE);
    setRegError('');
    setSuccess('');
    setShowReg(true);
  };
  const closeReg = () => {
    setShowReg(false);
    setRegForm(DEFAULT_REG_FORM);
    setRegSchedule(DEFAULT_SCHEDULE);
    setRegPhotoFile(null); setRegPhotoPreview(null); setRegPhotoError('');
  };

  const handleRegChange = (e) => {
    const { name, value } = e.target;
    setRegForm(prev => ({ ...prev, [name]: value, ...(name === 'deptId' ? { branchId: '' } : {}) }));
  };

  const toggleRegDay = (day) =>
    setRegSchedule(prev => ({
      ...prev,
      selectedDays: prev.selectedDays.includes(day)
        ? prev.selectedDays.filter(d => d !== day)
        : [...prev.selectedDays, day],
    }));

  const handleRegSchedChange = (e) => {
    const { name, value } = e.target;
    setRegSchedule(prev => ({ ...prev, [name]: value }));
  };

  const handleRegSubmit = async (e) => {
    e.preventDefault();
    setRegError('');
    if (!regForm.schoolId || !regForm.firstName || !regForm.lastName || !regForm.email || !regForm.deptId) {
      setRegError('Please fill in all required fields.');
      return;
    }
    if (regSchedule.selectedDays.length > 0 && (!regSchedule.primaryTimeIn || !regSchedule.primaryTimeOut)) {
      setRegError('Primary time in and time out are required when days are selected.');
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
        role: 'SCHOLAR',
        deptId: Number(regForm.deptId),
        branchId: regForm.branchId ? Number(regForm.branchId) : undefined,
      });
      if (regPhotoFile && regRes.data?.userId) {
        try { await adminApi.uploadUserPhoto(regRes.data.userId, regPhotoFile); } catch { /* non-fatal */ }
      }
      if (regSchedule.selectedDays.length > 0) {
        const entries = regSchedule.selectedDays.map(day => ({
          dayOfWeek: day,
          primaryTimeIn: regSchedule.primaryTimeIn,
          primaryTimeOut: regSchedule.primaryTimeOut,
          secondaryTimeIn: regSchedule.secondaryTimeIn || null,
          secondaryTimeOut: regSchedule.secondaryTimeOut || null,
        }));
        try {
          await adminApi.setScholarSchedule(regForm.schoolId, entries);
        } catch (schedErr) {
          setSuccess(`Scholar registered. Warning: schedule could not be saved — ${schedErr.response?.data?.error || schedErr.message}`);
          closeReg();
          loadData();
          return;
        }
      }
      setSuccess(`Scholar ${regForm.firstName} ${regForm.lastName} registered successfully.`);
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
  const openInfo = async (scholar) => {
    setInfoScholar(scholar);
    setInfoSchedule(null);
    setInfoLoading(true);
    try {
      const res = await adminApi.getScholarSchedule(scholar.schoolId);
      setInfoSchedule(Array.isArray(res.data) ? res.data : []);
    } catch {
      setInfoSchedule([]);
    } finally {
      setInfoLoading(false);
    }
  };
  const closeInfo = () => { setInfoScholar(null); setInfoSchedule(null); };

  // ── Edit modal ──────────────────────────────────────────────────
  const openEdit = async (scholar) => {
    setEditScholar(scholar);
    setEditError('');
    setEditPhotoFile(null);
    setEditPhotoPreview(scholar.profilePhotoUrl || null);
    setEditPhotoError('');
    setEditForm({
      firstName: scholar.firstName || '',
      lastName: scholar.lastName || '',
      email: scholar.email || '',
      personalGmail: scholar.personalGmail || '',
      deptId: scholar.departmentId ? String(scholar.departmentId) : '',
      branchId: scholar.branchId ? String(scholar.branchId) : '',
    });
    setSchedLoading(true);
    try {
      const res = await adminApi.getScholarSchedule(scholar.schoolId);
      const entries = Array.isArray(res.data) ? res.data : [];
      if (entries.length > 0) {
        const first = entries[0];
        setEditSchedule({
          selectedDays: entries.map(e => e.dayOfWeek),
          primaryTimeIn: (first.primaryTimeIn || '08:00').substring(0, 5),
          primaryTimeOut: (first.primaryTimeOut || '12:00').substring(0, 5),
          secondaryTimeIn: first.secondaryTimeIn ? first.secondaryTimeIn.substring(0, 5) : '',
          secondaryTimeOut: first.secondaryTimeOut ? first.secondaryTimeOut.substring(0, 5) : '',
        });
      } else {
        setEditSchedule(DEFAULT_SCHEDULE);
      }
    } catch {
      setEditSchedule(DEFAULT_SCHEDULE);
    } finally {
      setSchedLoading(false);
    }
  };
  const closeEdit = () => {
    setEditScholar(null); setEditForm(null); setEditSchedule(null); setEditError('');
    setEditPhotoFile(null); setEditPhotoPreview(null); setEditPhotoError('');
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({ ...prev, [name]: value, ...(name === 'deptId' ? { branchId: '' } : {}) }));
  };

  const toggleEditDay = (day) =>
    setEditSchedule(prev => ({
      ...prev,
      selectedDays: prev.selectedDays.includes(day)
        ? prev.selectedDays.filter(d => d !== day)
        : [...prev.selectedDays, day],
    }));

  const handleEditSchedChange = (e) => {
    const { name, value } = e.target;
    setEditSchedule(prev => ({ ...prev, [name]: value }));
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setEditError('');
    if (!editForm.firstName || !editForm.lastName || !editForm.email || !editForm.deptId) {
      setEditError('Please fill in all required fields.');
      return;
    }
    setEditSubmitting(true);
    try {
      if (editPhotoFile) {
        try { await adminApi.uploadUserPhoto(editScholar.userId, editPhotoFile); } catch { /* non-fatal */ }
      }
      await adminApi.updateUser(editScholar.userId, {
        schoolId: editScholar.schoolId,
        firstName: editForm.firstName,
        lastName: editForm.lastName,
        email: editForm.email,
        personalGmail: editForm.personalGmail || null,
        password: editScholar.schoolId,
        role: 'SCHOLAR',
        deptId: Number(editForm.deptId),
        branchId: editForm.branchId ? Number(editForm.branchId) : null,
      });
      if (editSchedule.selectedDays.length > 0) {
        const entries = editSchedule.selectedDays.map(day => ({
          dayOfWeek: day,
          primaryTimeIn: editSchedule.primaryTimeIn,
          primaryTimeOut: editSchedule.primaryTimeOut,
          secondaryTimeIn: editSchedule.secondaryTimeIn || null,
          secondaryTimeOut: editSchedule.secondaryTimeOut || null,
        }));
        await adminApi.setScholarSchedule(editScholar.schoolId, entries);
      }
      setSuccess(`Scholar ${editForm.firstName} ${editForm.lastName} updated successfully.`);
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

  // ── Duty modal ──────────────────────────────────────────────────
  const openDuty = async (scholar) => {
    setDutyScholar(scholar);
    setDutySummary(null);
    setDutyHistory([]);
    setDutyLoading(true);
    setDutyFilter('ALL');
    setReminderMsg('');
    try {
      const [summaryRes, historyRes] = await Promise.all([
        adminApi.getScholarDutySummary(scholar.schoolId),
        adminApi.getScholarDutyHistory(scholar.schoolId),
      ]);
      setDutySummary(summaryRes.data);
      setDutyHistory(Array.isArray(historyRes.data) ? historyRes.data : []);
    } catch {
      /* show whatever loaded */
    } finally {
      setDutyLoading(false);x
    }
  };
  const closeDuty = () => { setDutyScholar(null); setDutySummary(null); setDutyHistory([]); setReminderMsg(''); };

  const sendReminder = async () => {
    setReminderSending(true);
    setReminderMsg('');
    try {
      await adminApi.sendScholarReminder(dutyScholar.schoolId);
      setReminderMsg('success');
    } catch (err) {
      setReminderMsg('error:' + (err.response?.data?.error || err.message || 'Failed to send'));
    } finally {
      setReminderSending(false);
    }
  };

  const calcHours = (timeIn, timeOut) => {
    if (!timeIn || !timeOut) return '—';
    const [h1, m1] = timeIn.split(':').map(Number);
    const [h2, m2] = timeOut.split(':').map(Number);
    const mins = (h2 * 60 + m2) - (h1 * 60 + m1);
    return mins > 0 ? (mins / 60).toFixed(1) + ' hrs' : '—';
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
    <Layout pageTitle="Scholar Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>NAS Scholars</h1>
            <p className="text-muted">{scholars.length} registered scholars</p>
          </div>
          <button className="btn btn-primary" onClick={openReg}>+ Register Scholar</button>
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
                <div className="empty-state"><p>No scholars found</p></div>
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
                    {filtered.map(s => (
                      <tr key={s.userId}>
                        <td><code>{s.schoolId}</code></td>
                        <td>{s.lastName}, {s.firstName}</td>
                        <td>{s.email}</td>
                        <td>{s.departmentName || '—'}</td>
                        <td>{s.branchName || '—'}</td>
                        <td>
                          <span className={`badge ${s.isActive ? 'badge-success' : 'badge-danger'}`}>
                            {s.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '4px' }}>
                            <IconBtn onClick={() => openInfo(s)} title="View Details" color="#6366f1">ℹ</IconBtn>
                            <IconBtn onClick={() => openDuty(s)} title="Duty Summary" color="#7c3aed">📊</IconBtn>
                            <IconBtn onClick={() => openEdit(s)} title="Edit Scholar" color="#0ea5e9">🖍</IconBtn>
                            <IconBtn
                              onClick={() => toggleActive(s.userId)}
                              title={s.isActive ? 'Deactivate' : 'Activate'}
                              color={s.isActive ? '#6b7280' : '#16a34a'}
                            >
                              {s.isActive ? '⊘' : '●'}
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
              <h3>Register NAS Scholar</h3>
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
                      {departments.filter(d => d.isActive !== false).map(dept => (
                        <option key={dept.departmentId} value={dept.departmentId}>{dept.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Branch</label>
                    <select name="branchId" className="form-control" value={regForm.branchId}
                      onChange={handleRegChange} disabled={!regForm.deptId}>
                      <option value="">Select branch</option>
                      {branchesFor(regForm.deptId).filter(b => b.isActive !== false).map(b => (
                        <option key={b.branchId} value={b.branchId}>{b.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div style={{ marginTop: '1rem' }}>
                  <label className="form-label">Duty Schedule</label>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
                    {DAYS.map(({ label, value }) => (
                      <button key={value} type="button" onClick={() => toggleRegDay(value)}
                        className={`btn btn-sm ${regSchedule.selectedDays.includes(value) ? 'btn-primary' : 'btn-outline'}`}>
                        {label}
                      </button>
                    ))}
                  </div>
                  {regSchedule.selectedDays.length > 0 && (
                    <>
                      <div style={{ marginBottom: '0.5rem' }}>
                        <small className="text-muted">
                          Applied to: {regSchedule.selectedDays.map(d => d.charAt(0) + d.slice(1).toLowerCase()).join(', ')}
                        </small>
                      </div>
                      <div className="form-row">
                        <div className="form-group">
                          <label className="form-label">Time In (Primary) *</label>
                          <input type="time" name="primaryTimeIn" className="form-control"
                            value={regSchedule.primaryTimeIn} onChange={handleRegSchedChange} required />
                        </div>
                        <div className="form-group">
                          <label className="form-label">Time Out (Primary) *</label>
                          <input type="time" name="primaryTimeOut" className="form-control"
                            value={regSchedule.primaryTimeOut} onChange={handleRegSchedChange} required />
                        </div>
                      </div>
                      <div className="form-row">
                        <div className="form-group">
                          <label className="form-label">Time In (Secondary) <span className="text-muted">— optional</span></label>
                          <input type="time" name="secondaryTimeIn" className="form-control"
                            value={regSchedule.secondaryTimeIn} onChange={handleRegSchedChange} />
                        </div>
                        <div className="form-group">
                          <label className="form-label">Time Out (Secondary) <span className="text-muted">— optional</span></label>
                          <input type="time" name="secondaryTimeOut" className="form-control"
                            value={regSchedule.secondaryTimeOut} onChange={handleRegSchedChange} />
                        </div>
                      </div>
                    </>
                  )}
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={closeReg}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={regSubmitting}>
                  {regSubmitting ? <><span className="btn-spinner" />Registering...</> : 'Register Scholar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Info Modal ────────────────────────────────────────────── */}
      {infoScholar && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeInfo()}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                {infoScholar.profilePhotoUrl ? (
                  <img
                    src={infoScholar.profilePhotoUrl}
                    alt="Profile"
                    onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                    style={{ width: 100, height: 100, borderRadius: '50%', objectFit: 'cover', border: '2px solid #e5e7eb', flexShrink: 0, display: 'block' }}
                  />
                ) : null}
                <div style={{
                  width: 60, height: 60, borderRadius: '50%', background: '#1e3a5f',
                  color: '#fff', fontSize: '1.25rem', fontWeight: 700, flexShrink: 0,
                  display: infoScholar.profilePhotoUrl ? 'none' : 'flex',
                  alignItems: 'center', justifyContent: 'center',
                }}>
                  {`${infoScholar.firstName?.[0]?.toUpperCase() ?? ''}${infoScholar.lastName?.[0]?.toUpperCase() ?? ''}`}
                </div>
                <div>
                  <h3 style={{ margin: 0 }}>{infoScholar.lastName}, {infoScholar.firstName}</h3>
                  <p style={{ margin: '2px 0 0', fontSize: '0.875rem', color: '#6b7280', fontWeight: 400 }}>
                    {infoScholar.schoolId}
                  </p>
                </div>
              </div>
              <button className="modal-close" onClick={closeInfo}>×</button>
            </div>
            <div className="modal-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem 1.5rem', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
                <div>
                  <span style={{ color: '#6b7280' }}>Department</span><br />
                  <strong>{infoScholar.departmentName || '—'}</strong>
                </div>
                <div>
                  <span style={{ color: '#6b7280' }}>Branch</span><br />
                  <strong>{infoScholar.branchName || '—'}</strong>
                </div>
                {infoScholar.personalGmail && (
                  <div>
                    <span style={{ color: '#6b7280' }}>Personal Gmail</span><br />
                    <strong>{infoScholar.personalGmail}</strong>
                  </div>
                )}
                <div>
                    <span style={{ color: '#6b7280' }}>School Email</span><br />
                    <strong>{infoScholar.email}</strong>
                  </div>
                <div>
                  <span style={{ color: '#6b7280' }}>Status</span><br />
                  <span className={`badge ${infoScholar.isActive ? 'badge-success' : 'badge-danger'}`}>
                    {infoScholar.isActive ? 'Active' : 'Inactive'}
                  </span>
                </div>
              </div>

              <h4 style={{ marginBottom: '0.75rem', color: '#374151' }}>Weekly Schedule</h4>
              {infoLoading ? (
                <div className="loading-state"><div className="spinner"></div></div>
              ) : infoSchedule && infoSchedule.length > 0 ? (
                <table className="table" style={{ fontSize: '0.875rem' }}>
                  <thead>
                    <tr>
                      <th>Day</th>
                      <th>Primary In</th>
                      <th>Primary Out</th>
                      <th>Secondary In</th>
                      <th>Secondary Out</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...infoSchedule]
                      .sort((a, b) => DAY_ORDER.indexOf(a.dayOfWeek) - DAY_ORDER.indexOf(b.dayOfWeek))
                      .map((entry, i) => (
                        <tr key={i}>
                          <td style={{ textTransform: 'capitalize' }}>
                            {entry.dayOfWeek.charAt(0) + entry.dayOfWeek.slice(1).toLowerCase()}
                          </td>
                          <td>{formatTime(entry.primaryTimeIn)}</td>
                          <td>{formatTime(entry.primaryTimeOut)}</td>
                          <td>{formatTime(entry.secondaryTimeIn)}</td>
                          <td>{formatTime(entry.secondaryTimeOut)}</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              ) : (
                <p style={{ color: '#9ca3af' }}>No schedule configured for this scholar.</p>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={closeInfo}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ────────────────────────────────────────────── */}
      {editScholar && editForm && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeEdit()}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <div>
                <h3 style={{ margin: 0 }}>Edit Scholar</h3>
                <p style={{ margin: '2px 0 0', fontSize: '0.875rem', color: '#6b7280', fontWeight: 400 }}>
                  {editScholar.schoolId}
                </p>
              </div>
              <button className="modal-close" onClick={closeEdit}>×</button>
            </div>
            <form onSubmit={handleEditSubmit}>
              <div className="modal-body">
                {editError && <div className="alert alert-error">{editError}</div>}
                <AvatarPicker
                  preview={editPhotoPreview}
                  initials={`${editForm?.firstName?.[0]?.toUpperCase() ?? ''}${editForm?.lastName?.[0]?.toUpperCase() ?? ''}`}
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
                      {departments.filter(d => d.isActive !== false).map(d => (
                        <option key={d.departmentId} value={d.departmentId}>{d.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label className="form-label">Branch</label>
                    <select name="branchId" className="form-control" value={editForm.branchId}
                      onChange={handleEditChange} disabled={!editForm.deptId}>
                      <option value="">Select branch</option>
                      {branchesFor(editForm.deptId).filter(b => b.isActive !== false).map(b => (
                        <option key={b.branchId} value={b.branchId}>{b.name}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div style={{ marginTop: '1rem' }}>
                  <label className="form-label">Duty Schedule</label>
                  {schedLoading ? (
                    <div className="loading-state"><div className="spinner"></div></div>
                  ) : editSchedule && (
                    <>
                      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '0.75rem' }}>
                        {DAYS.map(({ label, value }) => (
                          <button key={value} type="button" onClick={() => toggleEditDay(value)}
                            className={`btn btn-sm ${editSchedule.selectedDays.includes(value) ? 'btn-primary' : 'btn-outline'}`}>
                            {label}
                          </button>
                        ))}
                      </div>
                      {editSchedule.selectedDays.length > 0 && (
                        <>
                          <div style={{ marginBottom: '0.5rem' }}>
                            <small className="text-muted">
                              Applied to: {editSchedule.selectedDays.map(d => d.charAt(0) + d.slice(1).toLowerCase()).join(', ')}
                            </small>
                          </div>
                          <div className="form-row">
                            <div className="form-group">
                              <label className="form-label">Time In (Primary) *</label>
                              <input type="time" name="primaryTimeIn" className="form-control"
                                value={editSchedule.primaryTimeIn} onChange={handleEditSchedChange} required />
                            </div>
                            <div className="form-group">
                              <label className="form-label">Time Out (Primary) *</label>
                              <input type="time" name="primaryTimeOut" className="form-control"
                                value={editSchedule.primaryTimeOut} onChange={handleEditSchedChange} required />
                            </div>
                          </div>
                          <div className="form-row">
                            <div className="form-group">
                              <label className="form-label">Time In (Secondary) <span className="text-muted">— optional</span></label>
                              <input type="time" name="secondaryTimeIn" className="form-control"
                                value={editSchedule.secondaryTimeIn} onChange={handleEditSchedChange} />
                            </div>
                            <div className="form-group">
                              <label className="form-label">Time Out (Secondary) <span className="text-muted">— optional</span></label>
                              <input type="time" name="secondaryTimeOut" className="form-control"
                                value={editSchedule.secondaryTimeOut} onChange={handleEditSchedChange} />
                            </div>
                          </div>
                        </>
                      )}
                    </>
                  )}
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
      {/* ── Duty Summary Modal ───────────────────────────────────── */}
      {dutyScholar && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeDuty()}>
          <div className="modal modal-lg" style={{ maxWidth: '780px' }}>
            <div className="modal-header">
              <div>
                <h3 style={{ margin: 0 }}>Duty Overview</h3>
                <p style={{ margin: '2px 0 0', fontSize: '0.875rem', color: '#6b7280', fontWeight: 400 }}>
                  {dutyScholar.lastName}, {dutyScholar.firstName} · {dutyScholar.schoolId}
                </p>
              </div>
              <button className="modal-close" onClick={closeDuty}>×</button>
            </div>

            <div className="modal-body">
              {dutyLoading ? (
                <div className="loading-state"><div className="spinner"></div></div>
              ) : (
                <>
                  {/* ── Summary Cards ── */}
                  {dutySummary && (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.75rem', marginBottom: '1.5rem' }}>
                      {[
                        { label: 'Required Hours',  value: dutySummary.totalRequiredHours + ' hrs', color: '#6366f1' },
                        { label: 'Completed Hours', value: dutySummary.completedHours + ' hrs',     color: '#16a34a' },
                        { label: 'Remaining Hours', value: dutySummary.remainingHours + ' hrs',     color: '#dc2626' },
                        { label: 'Total Lates',     value: dutySummary.totalLates,                  color: '#d97706' },
                        { label: 'Missed Hours',    value: dutySummary.missedHours + ' hrs',        color: '#7c3aed' },
                      ].map(({ label, value, color }) => (
                        <div key={label} style={{
                          background: '#f9fafb',
                          border: `1px solid ${color}33`,
                          borderLeft: `3px solid ${color}`,
                          borderRadius: '8px',
                          padding: '0.75rem 1rem',
                        }}>
                          <div style={{ fontSize: '0.72rem', color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '4px' }}>{label}</div>
                          <div style={{ fontSize: '1.3rem', fontWeight: 700, color }}>{value}</div>
                        </div>
                      ))}
                    </div>
                  )}

                  {/* ── Duty History ── */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <h4 style={{ margin: 0, color: '#374151' }}>Duty History</h4>
                    <div style={{ display: 'flex', gap: '4px' }}>
                      {['ALL', 'REGULAR', 'MAKEUP', 'OVERTIME'].map(f => (
                        <button
                          key={f}
                          className={`btn btn-sm ${dutyFilter === f ? 'btn-primary' : 'btn-outline'}`}
                          onClick={() => setDutyFilter(f)}
                          style={{ fontSize: '0.75rem', padding: '2px 8px' }}
                        >
                          {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="table-responsive" style={{ maxHeight: '320px', overflowY: 'auto' }}>
                    {dutyHistory.length === 0 ? (
                      <p style={{ color: '#9ca3af', textAlign: 'center', padding: '1rem 0' }}>No duty records found.</p>
                    ) : (() => {
                      const rows = dutyFilter === 'ALL'
                        ? dutyHistory
                        : dutyHistory.filter(d => d.dutyType === dutyFilter);
                      if (rows.length === 0) return (
                        <p style={{ color: '#9ca3af', textAlign: 'center', padding: '1rem 0' }}>No records for this type.</p>
                      );
                      return (
                        <table className="table" style={{ fontSize: '0.8rem' }}>
                          <thead>
                            <tr>
                              <th>Date</th>
                              <th>Type</th>
                              <th>Attendance</th>
                              <th>Approval</th>
                              <th>Time In</th>
                              <th>Time Out</th>
                              <th>Hours</th>
                              <th>Remarks</th>
                            </tr>
                          </thead>
                          <tbody>
                            {rows.map(d => (
                              <tr key={d.dutyId}>
                                <td style={{ whiteSpace: 'nowrap' }}>{formatDate(d.dutyDate)}</td>
                                <td>
                                  <span style={{
                                    background: d.dutyType === 'REGULAR' ? '#e0e7ff' : d.dutyType === 'MAKEUP' ? '#fef3c7' : '#f3e8ff',
                                    color:      d.dutyType === 'REGULAR' ? '#4338ca' : d.dutyType === 'MAKEUP' ? '#92400e' : '#6b21a8',
                                    borderRadius: '4px', padding: '1px 6px', fontSize: '0.72rem', fontWeight: 600,
                                  }}>
                                    {d.dutyType}
                                  </span>
                                </td>
                                <td>
                                  <span className={`badge ${
                                    d.attendanceStatus === 'PRESENT' ? 'badge-success'
                                    : d.attendanceStatus === 'LATE'  ? 'badge-warning'
                                    : 'badge-danger'
                                  }`} style={{ fontSize: '0.7rem' }}>
                                    {d.attendanceStatus ?? '—'}
                                  </span>
                                </td>
                                <td>
                                  <span className={`badge ${
                                    d.approvalStatus === 'APPROVED' ? 'badge-success'
                                    : d.approvalStatus === 'PENDING' ? 'badge-warning'
                                    : 'badge-danger'
                                  }`} style={{ fontSize: '0.7rem' }}>
                                    {d.approvalStatus}
                                  </span>
                                </td>
                                <td>{formatTime(d.timeIn)}</td>
                                <td>{formatTime(d.timeOut)}</td>
                                <td>{calcHours(d.timeIn, d.timeOut)}</td>
                                <td style={{ maxWidth: '140px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                                    title={d.remarks ?? ''}>
                                  {d.remarks ?? '—'}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      );
                    })()}
                  </div>
                </>
              )}

              {/* ── Reminder feedback ── */}
              {reminderMsg === 'success' && (
                <div className="alert alert-success" style={{ marginTop: '1rem' }}>
                  Reminder email sent successfully.
                  <button className="alert-close" onClick={() => setReminderMsg('')}>×</button>
                </div>
              )}
              {reminderMsg.startsWith('error:') && (
                <div className="alert alert-error" style={{ marginTop: '1rem' }}>
                  {reminderMsg.slice(6)}
                  <button className="alert-close" onClick={() => setReminderMsg('')}>×</button>
                </div>
              )}
            </div>

            <div className="modal-footer" style={{ justifyContent: 'space-between' }}>
              <button
                className="btn btn-primary"
                onClick={sendReminder}
                disabled={reminderSending || dutyLoading}
                style={{ background: '#7c3aed', borderColor: '#7c3aed' }}
              >
                {reminderSending ? <><span className="btn-spinner" /> Sending...</> : '✉ Send Duty Reminder Email'}
              </button>
              <button className="btn btn-ghost" onClick={closeDuty}>Close</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
