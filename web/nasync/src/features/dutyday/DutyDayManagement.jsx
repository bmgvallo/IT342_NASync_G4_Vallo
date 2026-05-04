import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { semesterApi, dutyDayApi } from '../../api';
import '../../styles/admin.css';
 
const TYPE_COLORS = {
  HOLIDAY: 'badge-danger',
  SUSPENDED: 'badge-gold',
  WFH: 'badge-info',
};
 
export default function DutyDayManagement() {
  const [semesters, setSemesters] = useState([]);
  const [selectedSemId, setSelectedSemId] = useState('');
  const [dutyDays, setDutyDays] = useState([]);
  const [form, setForm] = useState({ dayDate: '', dayType: 'HOLIDAY', description: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
 
  useEffect(() => {
    semesterApi.getAll().then(r => {
      setSemesters(r.data || []);
      const active = (r.data || []).find(s => s.active);
      if (active) setSelectedSemId(String(active.semesterId));
    });
  }, []);
 
  useEffect(() => {
    if (!selectedSemId) return;
    loadDutyDays();
  }, [selectedSemId]);
 
  const loadDutyDays = async () => {
    setLoading(true);
    try {
      const res = await dutyDayApi.getBySemester(selectedSemId);
      setDutyDays(res.data || []);
    } catch (e) { console.error(e); }
    finally { setLoading(false); }
  };
 
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    if (!selectedSemId || !form.dayDate || !form.dayType) {
      setError('Semester, date, and type are required.'); return;
    }
    try {
      await dutyDayApi.create({
        semesterId: Number(selectedSemId),
        dayDate: form.dayDate,
        dayType: form.dayType,
        description: form.description || null,
      });
      setSuccess('Duty day marked successfully.');
      setForm({ dayDate: '', dayType: 'HOLIDAY', description: '' });
      loadDutyDays();
    } catch (e) {
      setError(e.response?.data?.error || 'Failed to mark duty day.');
    }
  };
 
  const handleDelete = async (id) => {
    if (!window.confirm('Remove this duty day?')) return;
    try {
      await dutyDayApi.delete(id);
      setSuccess('Duty day removed.');
      loadDutyDays();
    } catch (e) { alert('Failed to delete.'); }
  };
 
  return (
    <Layout pageTitle="Duty Day Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Duty Days</h1>
            <p className="text-muted">Mark holidays, suspended days, and WFH dates</p>
          </div>
        </div>
 
        {success && (
          <div className="alert alert-success">
            {success}
            <button className="alert-close" onClick={() => setSuccess('')}>x</button>
          </div>
        )}
 
        <div className="card">
          <div className="card-body">
            <h3>Add Duty Day</h3>
            {error && <div className="alert alert-error">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Semester *</label>
                  <select className="form-control"
                    value={selectedSemId}
                    onChange={e => setSelectedSemId(e.target.value)} required>
                    <option value="">Select semester</option>
                    {semesters.map(s => (
                      <option key={s.semesterId} value={s.semesterId}>
                        {s.label}{s.active ? ' (Active)' : ''}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Date *</label>
                  <input type="date" className="form-control"
                    value={form.dayDate}
                    onChange={e => setForm(p => ({ ...p, dayDate: e.target.value }))} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Type *</label>
                  <select className="form-control"
                    value={form.dayType}
                    onChange={e => setForm(p => ({ ...p, dayType: e.target.value }))}>
                    <option value="HOLIDAY">Holiday</option>
                    <option value="SUSPENDED">Suspended</option>
                    <option value="WFH">WFH</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Description (optional)</label>
                  <input type="text" className="form-control"
                    placeholder="e.g. Independence Day"
                    value={form.description}
                    onChange={e => setForm(p => ({ ...p, description: e.target.value }))} />
                </div>
              </div>
              <button type="submit" className="btn btn-primary">+ Mark Day</button>
            </form>
          </div>
        </div>
 
        <div className="card" style={{ marginTop: '16px' }}>
          <div className="card-body">
            <h3>Marked Days</h3>
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : dutyDays.length === 0 ? (
              <div className="empty-state">
                <p>No special days marked yet.</p>
              </div>
            ) : (
              <table className="table">
                <thead>
                  <tr><th>Date</th><th>Type</th><th>Description</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {dutyDays.sort((a,b) => a.dayDate.localeCompare(b.dayDate)).map(d => (
                    <tr key={d.dutyDayId}>
                      <td>{d.dayDate}</td>
                      <td>
                        <span className={`badge ${TYPE_COLORS[d.dayType] || 'badge-muted'}`}>
                          {d.dayType}
                        </span>
                      </td>
                      <td>{d.description || '—'}</td>
                      <td>
                        <button className="btn btn-sm btn-ghost"
                          onClick={() => handleDelete(d.dutyDayId)}>
                          Remove
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}
