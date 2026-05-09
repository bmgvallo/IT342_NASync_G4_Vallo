import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { deptHeadApi } from '../../shared/api';
import '../../shared/styles/admin.css';
 
export default function DeptHeadScholars() {
  const [scholars, setScholars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
 
  useEffect(() => {
    deptHeadApi.getScholars()
      .then(r => setScholars(r.data || []))
      .catch(e => console.error(e))
      .finally(() => setLoading(false));
  }, []);
 
  const filtered = scholars.filter(s => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (
      s.firstName?.toLowerCase().includes(q) ||
      s.lastName?.toLowerCase().includes(q) ||
      s.schoolId?.toLowerCase().includes(q)
    );
  });

  const formatTo12Hour = (timeString) => {
    if (!timeString) return '—';
    let [hours, minutes] = timeString.split(':');
    hours = parseInt(hours);
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const hour12 = hours % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
  };
 
  return (
    <Layout pageTitle="My Scholars">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>My Scholars</h1>
            <p className="text-muted">
              {scholars.length} scholar(s) in your branch
            </p>
          </div>
        </div>
 
        <div className="card">
          <div className="card-body">
            <div className="search-bar" style={{ marginBottom: '1rem' }}>
              <input
                type="text"
                className="form-control"
                placeholder="Search by name or school ID..."
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
 
            {loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
              </div>
            ) : filtered.length === 0 ? (
              <div className="empty-state">
                <p>No scholars in your branch.</p>
              </div>
            ) : (
              <table className="table">
                <thead>
                  <tr>
                    <th>School ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Shift</th>
                    <th>Expected In</th>
                    <th>Expected Out</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map(s => (
                    <tr key={s.userId}>
                      <td><code>{s.schoolId}</code></td>
                      <td>{s.lastName}, {s.firstName}</td>
                      <td>{s.email}</td>
                      <td>{s.shift || '—'}</td>
                      <td>{formatTo12Hour(s.expectedTimeIn) || '—'}</td>
                      <td>{formatTo12Hour(s.expectedTimeOut) || '—'}</td>
                      <td>
                        <span className={
                          `badge ${s.active ? 'badge-success' : 'badge-muted'}`
                        }>
                          {s.active ? 'Active' : 'Inactive'}
                        </span>
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
