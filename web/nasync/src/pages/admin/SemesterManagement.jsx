import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { semesterApi } from '../../api';
import '../../styles/admin.css';
 
const DEFAULT_FORM = { label: '', startDate: '', endDate: '' };
 
export default function SemesterManagement() {
  const [semesters, setSemesters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
 
  const load = async () => {
    setLoading(true);
    try {
      const res = await semesterApi.getAll();
      setSemesters(res.data || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };
 
  useEffect(() => { load(); }, []);
 
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    if (!form.label || !form.startDate || !form.endDate) {
      setError('All fields are required.'); return;
    }
    setSubmitting(true);
    try {
      await semesterApi.create({
        label: form.label,
        startDate: form.startDate,
        endDate: form.endDate,
      });
      setSuccess('Semester created successfully.');
      setShowModal(false); setForm(DEFAULT_FORM); load();
      setTimeout(() => setSuccess(''), 3000);
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to create semester.');
    } finally { setSubmitting(false); }
  };
 
  const handleActivate = async (id) => {
    setActionLoading(true);
    try {
      await semesterApi.activate(id);
      setSuccess('Semester activated successfully.');
      load();
      setTimeout(() => setSuccess(''), 3000);
    } catch (e) { 
      setError(e.response?.data?.error || 'Failed to activate.');
      setTimeout(() => setError(''), 3000);
    } finally { setActionLoading(false); }
  };

  const handleDeactivate = async (id, label) => {
    if (!confirm(`Are you sure you want to end "${label}"? This will deactivate the semester.`)) {
      return;
    }
    setActionLoading(true);
    try {
      await semesterApi.deactivate(id);
      setSuccess(`Semester "${label}" has been ended.`);
      load();
      setTimeout(() => setSuccess(''), 3000);
    } catch (e) { 
      setError(e.response?.data?.error || 'Failed to deactivate semester.');
      setTimeout(() => setError(''), 3000);
    } finally { setActionLoading(false); }
  };
 
  return (
    <Layout pageTitle="Semester Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Semesters</h1>
            <p className="text-muted">{semesters.length} semester(s)</p>
          </div>
          <button className="btn btn-primary" onClick={() => {
            setShowModal(true); setForm(DEFAULT_FORM); setError('');
          }}>
            + Create Semester
          </button>
        </div>
 
        {success && (
          <div className="alert alert-success">
            {success}
            <button className="alert-close" onClick={() => setSuccess('')}>×</button>
          </div>
        )}
        {error && (
          <div className="alert alert-error">
            {error}
            <button className="alert-close" onClick={() => setError('')}>×</button>
          </div>
        )}
 
        <div className="card">
          <div className="card-body">
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : semesters.length === 0 ? (
              <div className="empty-state">
                <p>No semesters yet.</p>
              </div>
            ) : (
              <table className="table">
                <thead>
                  <tr>
                    <th>Label</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {semesters.map(s => (
                    <tr key={s.semesterId}>
                      <td><strong>{s.label}</strong></td>
                      <td>{s.startDate}</td>
                      <td>{s.endDate}</td>
                      <td>
                        <span className={
                          `badge ${s.active ? 'badge-success' : 'badge-muted'}`
                        }>
                          {s.active ? 'Active' : 'Ended'}
                        </span>
                      </td>
                      <td>
                        <div className="semester-actions">
                          {!s.active ? (
                            <button
                              className="btn btn-sm btn-success"
                              onClick={() => handleActivate(s.semesterId)}
                              disabled={actionLoading}
                              title="Activate this semester"
                            >
                              Activate
                            </button>
                          ) : (
                            <button
                              className="btn btn-sm btn-warning"
                              onClick={() => handleDeactivate(s.semesterId, s.label)}
                              disabled={actionLoading}
                              title="End this semester"
                            >
                              End Semester
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {showModal && (
          <div className="modal-overlay"
               onClick={(e) => e.target === e.currentTarget && setShowModal(false)}>
            <div className="modal">
              <div className="modal-header">
                <h3>Create Semester</h3>
                <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  {error && <div className="alert alert-error">{error}</div>}
                  <div className="form-group">
                    <label className="form-label">Label *</label>
                    <input type="text" className="form-control"
                      placeholder="e.g. 2nd Semester 2025-2026"
                      value={form.label}
                      onChange={e => setForm(p => ({ ...p, label: e.target.value }))} />
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">Start Date *</label>
                      <input type="date" className="form-control"
                        value={form.startDate}
                        onChange={e => setForm(p => ({ ...p, startDate: e.target.value }))} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">End Date *</label>
                      <input type="date" className="form-control"
                        value={form.endDate}
                        onChange={e => setForm(p => ({ ...p, endDate: e.target.value }))} />
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost"
                    onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Creating...' : 'Create Semester'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}