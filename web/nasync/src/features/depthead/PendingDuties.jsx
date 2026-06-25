import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { deptHeadApi, semesterApi } from '../../shared/api';
import { formatDate, formatTime } from '../../shared/utils/formatters';
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
  const [activeSemester, setActiveSemester] = useState(undefined);
  const [editDecisionId, setEditDecisionId] = useState(null);
  const [editDecisionCurrent, setEditDecisionCurrent] = useState(null);
  const [editDecisionNew, setEditDecisionNew] = useState(null);
  const [editDecisionRemarks, setEditDecisionRemarks] = useState('');
  const [confirming, setConfirming] = useState(false);

  const showMsg = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 3500);
  };

 
  const todayStr = (() => {
    const n = new Date();
    return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, '0')}-${String(n.getDate()).padStart(2, '0')}`;
  })();

  const load = (showSpinner = true) => {
    if (showSpinner) setLoading(true);
    Promise.all([
      deptHeadApi.getPendingDuties(),
      deptHeadApi.getAllDuties(),
      semesterApi.getActive().catch(() => ({ data: null })),
    ])
      .then(([pendingRes, allRes, semRes]) => {
        setPendingDuties(pendingRes.data || []);
        const allDuties = allRes.data || [];
        setHistoryDuties(allDuties.filter(d => d.approvalStatus !== 'PENDING'));
        setActiveSemester(semRes.data?.semesterId ? semRes.data : null);
      })
      .catch(e => console.error(e))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    const interval = setInterval(() => load(false), 15_000);
    return () => clearInterval(interval);
  }, []);
 
  const openModal = (id, type) => {
    setActionId(id); setModalType(type); setRemarks('');
  };
  const closeModal = () => { setActionId(null); setModalType(null); setRemarks(''); };

  const openEditDecision = (duty) => {
    setEditDecisionId(duty.dutyId);
    setEditDecisionCurrent(duty.approvalStatus);
    setEditDecisionNew(duty.approvalStatus === 'APPROVED' ? 'REJECTED' : 'APPROVED');
    setEditDecisionRemarks('');
  };
  const closeEditDecision = () => {
    setEditDecisionId(null); setEditDecisionCurrent(null);
    setEditDecisionNew(null); setEditDecisionRemarks('');
  };
  const handleEditDecisionConfirm = async () => {
    setConfirming(true);
    try {
      await deptHeadApi.changeDutyDecision(editDecisionId, editDecisionNew, editDecisionRemarks);
      showMsg(
        editDecisionNew === 'APPROVED' ? 'Decision updated — duty approved.' : 'Decision updated — duty rejected.',
        editDecisionNew === 'APPROVED' ? 'success' : 'error'
      );
      closeEditDecision(); load();
    } catch (e) {
      showMsg(e.response?.data?.error || 'Failed to update decision.', 'error');
      closeEditDecision();
    } finally {
      setConfirming(false);
    }
  };

  const handleConfirm = async () => {
    setConfirming(true);
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
    } finally {
      setConfirming(false);
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
 
        {activeSemester === null && (
          <div className="alert alert-error">
            No active semester. Duty workflows are currently paused.
          </div>
        )}

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
                        <td>{formatDate(d.dutyDate)}</td>
                        <td>{d.dutyType}</td>
                        <td>{formatTime(d.timeIn)}</td>
                        <td>{formatTime(d.timeOut)}</td>
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
                              onClick={() => openModal(d.dutyId, 'approve')}
                              disabled={!d.timeOut}
                              title={!d.timeOut ? 'Waiting for scholar to clock out' : ''}>
                              Approve
                            </button>
                            <button className="btn btn-sm btn-ghost"
                              onClick={() => openModal(d.dutyId, 'reject')}
                              disabled={!d.timeOut}
                              title={!d.timeOut ? 'Waiting for scholar to clock out' : ''}>
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

        {/* Absent Today — auto-approved absent records, not pending */}
        {!loading && (() => {
          const absentToday = historyDuties.filter(
            d => d.attendanceStatus === 'ABSENT' && d.dutyDate === todayStr
          );
          if (absentToday.length === 0) return null;
          return (
            <div className="card" style={{ marginTop: '24px' }}>
              <div className="card-header">
                <h3>Absent Today ({absentToday.length})</h3>
              </div>
              <div className="card-body">
                <div className="table-responsive">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Scholar</th>
                        <th>School ID</th>
                        <th>Branch</th>
                        <th>Remarks</th>
                      </tr>
                    </thead>
                    <tbody>
                      {absentToday.map(d => (
                        <tr key={d.dutyId}>
                          <td>{d.scholarName}</td>
                          <td><code>{d.scholarSchoolId}</code></td>
                          <td>{d.branchName || '—'}</td>
                          <td style={{ maxWidth: '300px', wordBreak: 'break-word' }}>
                            {d.remarks || '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          );
        })()}

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
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {historyDuties.map(d => (
                      <tr key={d.dutyId}>
                        <td>{d.scholarName}</td>
                        <td><code>{d.scholarSchoolId}</code></td>
                        <td>{formatDate(d.dutyDate)}</td>
                        <td>{d.dutyType}</td>
                        <td>{formatTime(d.timeIn)}</td>
                        <td>{formatTime(d.timeOut)}</td>
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
                        <td>
                          <button
                            className="btn btn-sm btn-ghost"
                            onClick={() => openEditDecision(d)}
                            title="Edit decision"
                          >
                            🖍
                          </button>
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
                  disabled={confirming || (modalType === 'reject' && !remarks.trim())}
                >
                  {confirming
                    ? <><span className="btn-spinner" />{modalType === 'approve' ? 'Approving...' : 'Rejecting...'}</>
                    : `Confirm ${modalType === 'approve' ? 'Approval' : 'Rejection'}`}
                </button>
              </div>
            </div>
          </div>
        )}

        {editDecisionId && (
          <div className="modal-overlay"
               onClick={(e) => e.target === e.currentTarget && closeEditDecision()}>
            <div className="modal">
              <div className="modal-header">
                <h3>Edit Decision</h3>
                <button className="modal-close" onClick={closeEditDecision}>×</button>
              </div>
              <div className="modal-body">
                <p style={{ marginBottom: '0.75rem' }}>
                  Current status: <span className={`badge ${STATUS_BADGE[editDecisionCurrent] || 'badge-muted'}`}>{editDecisionCurrent}</span>
                </p>
                <div className="form-group">
                  <label className="form-label">Change to</label>
                  <div style={{ display: 'flex', gap: '8px', marginBottom: '0.75rem' }}>
                    {['APPROVED', 'REJECTED'].map(s => (
                      <button
                        key={s}
                        type="button"
                        className={`btn btn-sm ${editDecisionNew === s ? (s === 'APPROVED' ? 'btn-primary' : 'btn-danger') : 'btn-ghost'}`}
                        onClick={() => setEditDecisionNew(s)}
                      >
                        {s}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">
                    Remarks {editDecisionNew === 'REJECTED' && <span className="required">*</span>}
                  </label>
                  <textarea className="form-control" rows={3}
                    value={editDecisionRemarks}
                    onChange={e => setEditDecisionRemarks(e.target.value)}
                    placeholder={editDecisionNew === 'REJECTED' ? "Please provide a reason for rejection..." : "Add a note for the scholar (optional)..."}
                  />
                  {editDecisionNew === 'REJECTED' && (
                    <small className="form-hint text-danger">Remarks are required for rejection</small>
                  )}
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-ghost" onClick={closeEditDecision}>Cancel</button>
                <button
                  className={`btn ${editDecisionNew === 'APPROVED' ? 'btn-primary' : 'btn-danger'}`}
                  onClick={handleEditDecisionConfirm}
                  disabled={confirming || editDecisionNew === editDecisionCurrent || (editDecisionNew === 'REJECTED' && !editDecisionRemarks.trim())}
                >
                  {confirming
                    ? <><span className="btn-spinner" />{editDecisionNew === 'APPROVED' ? 'Approving...' : 'Rejecting...'}</>
                    : `Confirm ${editDecisionNew === 'APPROVED' ? 'Approval' : 'Rejection'}`}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}