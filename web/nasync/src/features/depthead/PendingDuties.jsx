import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { deptHeadApi } from '../../shared/api';
import '../../shared/styles/admin.css';
 
const STATUS_BADGE = {
  PENDING: 'badge-info',
  APPROVED: 'badge-success',
  REJECTED: 'badge-danger',
};

const ATTENDANCE_BADGE = {
  PRESENT: 'badge-success',
  LATE: 'badge-warning',
  ABSENT: 'badge-danger',
};
 
export default function PendingDuties() {
  const [pendingDuties, setPendingDuties] = useState([]);
  const [historyDuties, setHistoryDuties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState(null);
  const [remarks, setRemarks] = useState('');
  const [modalType, setModalType] = useState(null);
  const [message, setMessage] = useState({ text: '', type: '' });
 
  const showMsg = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 3500);
  };

  const formatTo12Hour = (timeString) => {
    if (!timeString) return '—';
    let [hours, minutes] = timeString.split(':');
    hours = parseInt(hours);
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const hour12 = hours % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
  };
 
  const load = () => {
    setLoading(true);
    deptHeadApi.getAllDuties()
      .then(r => {
        const allDuties = r.data || [];
        const pending = allDuties.filter(d => d.approvalStatus === 'PENDING');
        const history = allDuties.filter(d => d.approvalStatus !== 'PENDING');
        setPendingDuties(pending);
        setHistoryDuties(history);
      })
      .catch(e => console.error(e))
      .finally(() => setLoading(false));
  };
 
  useEffect(() => { load(); }, []);
 
  const openModal = (id, type) => {
    setActionId(id); setModalType(type); setRemarks('');
  };
  const closeModal = () => { setActionId(null); setModalType(null); setRemarks(''); };
 
  const handleConfirm = async () => {
    try {
      if (modalType === 'approve') {
        await deptHeadApi.approveDuty(actionId, remarks);
        showMsg('Duty approved successfully.');
      } else {
        await deptHeadApi.rejectDuty(actionId, remarks);
        showMsg('Duty rejected.', 'error');
      }
      closeModal(); load();
    } catch (e) {
      showMsg(e.response?.data?.error || 'Action failed.', 'error');
      closeModal();
    }
  };
 
  return (
    <Layout pageTitle="All Duties">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>All Duties</h1>
            <p className="text-muted">
              {pendingDuties.length} pending · {historyDuties.length} reviewed · {pendingDuties.length + historyDuties.length} total
            </p>
          </div>
        </div>
 
        {message.text && (
          <div className={`alert alert-${message.type}`}>{message.text}</div>
        )}
 
        <div className="card">
          <div className="card-header">
            <h3>Pending Approvals</h3>
          </div>
          <div className="card-body">
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : pendingDuties.length === 0 ? (
              <div className="empty-state">
                <p>No pending duties — all caught up!</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Scholar</th>
                      <th>School ID</th>
                      <th>Branch</th>
                      <th>Date</th>
                      <th>Type</th>
                      <th>Time In</th>
                      <th>Time Out</th>
                      <th>Attendance</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingDuties.map(d => (
                      <tr key={d.dutyId}>
                        <td>{d.scholarName}</td>
                        <td><code>{d.scholarSchoolId}</code></td>
                        <td>{d.branchName || '—'}</td>
                        <td>{d.dutyDate}</td>
                        <td>{d.dutyType}</td>
                        <td>{formatTo12Hour(d.timeIn)}</td>
                        <td>{formatTo12Hour(d.timeOut)}</td>
                        <td>
                          <span className={`badge ${ATTENDANCE_BADGE[d.attendanceStatus] || 'badge-muted'}`}>
                            {d.attendanceStatus || '—'}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${STATUS_BADGE[d.approvalStatus] || 'badge-muted'}`}>
                            {d.approvalStatus || 'PENDING'}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '4px' }}>
                            <button className="btn btn-sm btn-primary"
                              onClick={() => openModal(d.dutyId, 'approve')}>
                              Approve
                            </button>
                            <button className="btn btn-sm btn-ghost"
                              onClick={() => openModal(d.dutyId, 'reject')}>
                              Reject
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="card" style={{ marginTop: '24px' }}>
          <div className="card-header">
            <h3>History (Approved / Rejected)</h3>
          </div>
          <div className="card-body">
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : historyDuties.length === 0 ? (
              <div className="empty-state">
                <p>No duty history yet.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Scholar</th>
                      <th>School ID</th>
                      <th>Date</th>
                      <th>Type</th>
                      <th>Time In</th>
                      <th>Time Out</th>
                      <th>Attendance</th>
                      <th>Approval Status</th>
                      <th>Approved By</th>
                      <th>Remarks</th>
                    </tr>
                  </thead>
                  <tbody>
                    {historyDuties.map(d => (
                      <tr key={d.dutyId}>
                        <td>{d.scholarName}</td>
                        <td><code>{d.scholarSchoolId}</code></td>
                        <td>{d.dutyDate}</td>
                        <td>{d.dutyType}</td>
                        <td>{formatTo12Hour(d.timeIn)}</td>
                        <td>{formatTo12Hour(d.timeOut)}</td>
                        <td>
                          <span className={`badge ${ATTENDANCE_BADGE[d.attendanceStatus] || 'badge-muted'}`}>
                            {d.attendanceStatus || '—'}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${STATUS_BADGE[d.approvalStatus] || 'badge-muted'}`}>
                            {d.approvalStatus || '—'}
                          </span>
                        </td>
                        <td>{d.approvedByName || '—'}</td>
                        <td style={{ maxWidth: '250px', wordBreak: 'break-word' }}>
                          {d.remarks || '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {actionId && (
          <div className="modal-overlay"
               onClick={(e) => e.target === e.currentTarget && closeModal()}>
            <div className="modal">
              <div className="modal-header">
                <h3>{modalType === 'approve' ? 'Approve Duty' : 'Reject Duty'}</h3>
                <button className="modal-close" onClick={closeModal}>×</button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label">
                    Remarks {modalType === 'reject' && <span className="required">*</span>}
                  </label>
                  <textarea className="form-control" rows={3}
                    value={remarks}
                    onChange={e => setRemarks(e.target.value)}
                    placeholder={modalType === 'reject' ? "Please provide a reason for rejection..." : "Add a note for the scholar (optional)..."}
                    required={modalType === 'reject'}
                  />
                  {modalType === 'reject' && (
                    <small className="form-hint text-danger">Remarks are required for rejection</small>
                  )}
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-ghost" onClick={closeModal}>Cancel</button>
                <button
                  className={`btn ${modalType === 'approve' ? 'btn-primary' : 'btn-danger'}`}
                  onClick={handleConfirm}
                  disabled={modalType === 'reject' && !remarks.trim()}
                >
                  Confirm {modalType === 'approve' ? 'Approval' : 'Rejection'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}