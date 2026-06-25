import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { deptHeadApi } from '../../shared/api';
import { formatDate, formatTime } from '../../shared/utils/formatters';
import '../../shared/styles/admin.css';

const DAY_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

export default function DeptHeadScholars() {
  const [scholars, setScholars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedScholar, setSelectedScholar] = useState(null);
  const [details, setDetails] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);

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

  const openDetails = async (scholar) => {
    setSelectedScholar(scholar);
    setDetails(null);
    setDetailsLoading(true);
    try {
      const res = await deptHeadApi.getScholarDetails(scholar.schoolId);
      setDetails(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setDetailsLoading(false);
    }
  };

  const closeModal = () => {
    setSelectedScholar(null);
    setDetails(null);
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

            <div className="table-responsive">
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
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map(s => (
                      <tr key={s.userId}>
                        <td><code>{s.schoolId}</code></td>
                        <td>{s.lastName}, {s.firstName}</td>
                        <td>{s.email}</td>
                        <td>
                          <span className={`badge ${s.isActive ? 'badge-success' : 'badge-danger'}`}>
                            {s.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-outline"
                            onClick={() => openDetails(s)}
                            title="View Scholar Details"
                          >
                            ℹ
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
      </div>

      {selectedScholar && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <div>
                <h3 style={{ margin: 0 }}>
                  {selectedScholar.lastName}, {selectedScholar.firstName}
                </h3>
                <p style={{ margin: '2px 0 0', fontSize: '0.875rem', color: '#6b7280', fontWeight: 400 }}>
                  {selectedScholar.schoolId} &nbsp;·&nbsp; {selectedScholar.email}
                </p>
              </div>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>

            <div className="modal-body">
              {detailsLoading ? (
                <div className="loading-state"><div className="spinner"></div></div>
              ) : details ? (
                <>
                  <section style={{ marginBottom: '1.5rem' }}>
                    <h4 style={{ marginBottom: '0.75rem', color: '#374151' }}>Weekly Schedule</h4>
                    {details.schedule && details.schedule.length > 0 ? (
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
                          {[...details.schedule]
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
                  </section>

                  <section>
                    <h4 style={{ marginBottom: '0.75rem', color: '#374151' }}>Duty History</h4>
                    {details.dutyHistory && details.dutyHistory.length > 0 ? (
                      <table className="table" style={{ fontSize: '0.875rem' }}>
                        <thead>
                          <tr>
                            <th>Date</th>
                            <th>Type</th>
                            <th>Time In</th>
                            <th>Time Out</th>
                            <th>Attendance</th>
                            <th>Approval</th>
                          </tr>
                        </thead>
                        <tbody>
                          {details.dutyHistory.map((d, i) => (
                            <tr key={i}>
                              <td>{formatDate(d.dutyDate)}</td>
                              <td>{d.dutyType}</td>
                              <td>{formatTime(d.timeIn)}</td>
                              <td>{formatTime(d.timeOut)}</td>
                              <td>
                                <span className={`badge ${
                                  d.attendanceStatus === 'PRESENT' ? 'badge-success' :
                                  d.attendanceStatus === 'LATE' ? 'badge-warning' :
                                  d.attendanceStatus === 'ABSENT' ? 'badge-danger' : 'badge-muted'
                                }`}>
                                  {d.attendanceStatus || '—'}
                                </span>
                              </td>
                              <td>
                                <span className={`badge ${
                                  d.approvalStatus === 'APPROVED' ? 'badge-success' :
                                  d.approvalStatus === 'REJECTED' ? 'badge-danger' : 'badge-muted'
                                }`}>
                                  {d.approvalStatus || '—'}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    ) : (
                      <p style={{ color: '#9ca3af' }}>No duty records found.</p>
                    )}
                  </section>
                </>
              ) : (
                <p style={{ color: '#9ca3af' }}>Failed to load scholar details.</p>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={closeModal}>Close</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
