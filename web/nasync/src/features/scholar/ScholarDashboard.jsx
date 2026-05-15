import { useEffect, useState } from 'react';
import { useAuth } from '../../features/auth/AuthContext';
import Layout from '../../shared/components/Layout';
import { scholarDutyApi } from '../../shared/api';
import { useNavigate } from 'react-router-dom';
import '../../shared/styles/scholar.css';
 
export default function ScholarDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
 
  const [summary, setSummary] = useState(null);
  const [openDuty, setOpenDuty] = useState(null);
  const [hasRegularToday, setHasRegularToday] = useState(false);
  const [dutyType, setDutyType] = useState('REGULAR');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
 
  const showMsg = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 4000);
  };
 
  const loadData = async () => {
    setLoading(true);
    try {
      const [sumRes, dutiesRes] = await Promise.all([
        scholarDutyApi.getSummary(),
        scholarDutyApi.getMyDuties(),
      ]);
      setSummary(sumRes.data);
 
      const today = new Date().toISOString().slice(0, 10);
      const todayDuties = (dutiesRes.data || []).filter(
        d => d.dutyDate === today
      );
 
      const open = todayDuties.find(d => !d.timeOut);
      setOpenDuty(open || null);
 
      const regularDone = todayDuties.some(
        d => d.dutyType === 'REGULAR' && d.approvalStatus !== 'REJECTED'
      );
      setHasRegularToday(regularDone);
 
      if (regularDone && dutyType === 'REGULAR') {
        setDutyType('MAKEUP');
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };
 
  useEffect(() => { loadData(); }, []);
 
  const handleClockIn = async () => {
    setActionLoading(true);
      try {
          const res = await scholarDutyApi.clockIn(dutyType);
          const d = res.data;
          showMsg(
              `Clocked in at ${formatTo12Hour(d.timeIn)} — Status: ${d.status} (${d.dutyType})`,
              'success'
          );
          loadData();
      } catch (e) {
          const errorMsg = e.response?.data?.error || 'Clock-in failed.';
          
          if (errorMsg.includes('ABSENT') || errorMsg.includes('absent')) {
              showMsg(errorMsg, 'error');
              loadData();
          } else {
              showMsg(errorMsg, 'error');
          }
      } finally {
          setActionLoading(false);
      }
  };
 
  const handleClockOut = async () => {
    setActionLoading(true);
    try {
      const res = await scholarDutyApi.clockOut();
      showMsg(`Clocked out at ${formatTo12Hour(res.data.timeOut)}. Pending department head review.`);
      loadData();
    } catch (e) {
      showMsg(e.response?.data?.error || 'Clock-out failed.', 'error');
    } finally {
      setActionLoading(false);
    }
  };
 
  const greeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 18) return 'Good afternoon';
    return 'Good evening';
  };
 
  const statusColor = s => {
    if (s === 'LATE') return 'status-late';
    if (s === 'PRESENT') return 'status-present';
    return 'status-pending';
  };

  const formatTo12Hour = (timeString) => {
    if (!timeString) return '—';
      let [hours, minutes] = timeString.split(':');
      hours = parseInt(hours);
      const ampm = hours >= 12 ? 'PM' : 'AM';
      const hour12 = hours % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
  };
 
  return (
    <Layout pageTitle="Scholar Dashboard">
      <div className="scholar-dashboard">
 
        <div className="welcome-section">
          <h1>{greeting()}, {user?.firstName}!</h1>
          <p className="text-muted">
            {new Date().toLocaleDateString('en-PH', {
              weekday: 'long', year: 'numeric',
              month: 'long', day: 'numeric'
            })}
          </p>
        </div>
 
        {message.text && (
          <div className={`scholar-alert scholar-alert-${message.type}`}>
            <span>{message.text}</span>
            <button onClick={() => setMessage({ text: '', type: '' })}>×</button>
          </div>
        )}
 
        <div className="duty-clock-card">
          <div className="duty-clock-header">
            <div>
              <div className="duty-clock-label">TODAY'S DUTY</div>
              <div className="duty-clock-shift">
                {user?.shift || 'Morning'} Shift
                {user?.expectedTimeIn && user?.expectedTimeOut && (
                  <span className="duty-clock-time">
                    &nbsp;·&nbsp;
                    {formatTo12Hour(user.expectedTimeIn)} – {formatTo12Hour(user.expectedTimeOut)}
                  </span>
                )}
              </div>
            </div>
 
            {openDuty ? (
              <div className="clock-action-area">
                <div className="open-duty-badge">
                  <span className={`status-dot ${statusColor(openDuty.status)}`}></span>
                  <span>
                    {openDuty.dutyType} · In at <strong>{formatTo12Hour(openDuty.timeIn)}</strong>
                  </span>
                </div>
                <button
                  className="btn btn-danger clock-btn"
                  onClick={handleClockOut}
                  disabled={actionLoading}
                >
                  {actionLoading ? (
                    <><span className="btn-spinner"></span> Processing…</>
                  ) : '⏹ Clock Out'}
                </button>
              </div>
            ) : (
              <div className="clock-action-area">
                <div className="duty-type-row">
                  <label className="duty-type-label">Duty Type</label>
                  <div className="duty-type-chips">
                    {['REGULAR', 'MAKEUP', 'OVERTIME'].map(type => {
                      const disabled =
                        type === 'REGULAR' && hasRegularToday;
                      return (
                        <button
                          key={type}
                          type="button"
                          className={`duty-chip ${dutyType === type ? 'duty-chip-active' : ''} ${disabled ? 'duty-chip-disabled' : ''}`}
                          onClick={() => !disabled && setDutyType(type)}
                          disabled={disabled}
                          title={disabled ? 'Already completed Regular duty today' : ''}
                        >
                          {type}
                          {disabled && <span className="chip-done">✓</span>}
                        </button>
                      );
                    })}
                  </div>
                </div>
                {(dutyType === 'MAKEUP' || dutyType === 'OVERTIME') && (
                  <p className="min-hour-hint">
                    ⏱ Minimum 1 hour required before you can clock out.
                  </p>
                )}
                <button
                  className="btn btn-primary clock-btn"
                  onClick={handleClockIn}
                  disabled={actionLoading}
                >
                  {actionLoading ? (
                    <><span className="btn-spinner"></span> Processing…</>
                  ) : '▶ Clock In'}
                </button>
              </div>
            )}
          </div>
        </div>
 
        {!loading && summary && (
          <div className="stats-grid" style={{ marginTop: '1.25rem' }}>
            <div className="stat-card accent-navy">
              <span className="stat-label">Present This Semester</span>
              <span className="stat-value">{summary.presentCount ?? '—'}</span>
              <span className="stat-sub">Approved + Present</span>
            </div>
            <div className="stat-card accent-gold">
              <span className="stat-label">Late Count</span>
              <span className="stat-value">{summary.lateCount ?? '—'}</span>
              <span className="stat-sub">3 lates = 1 absent</span>
            </div>
            <div className="stat-card accent-navy">
              <span className="stat-label">Absent Count</span>
              <span className="stat-value">{summary.absentCount ?? '—'}</span>
              <span className="stat-sub">Auto-flagged by system</span>
            </div>
            <div className="stat-card accent-gold">
              <span className="stat-label">Makeup Hours Remaining</span>
              <span className="stat-value">
                {summary.makeupHoursRemaining ?? '—'}
              </span>
              <span className="stat-sub">
                Owed: {summary.makeupHoursOwed ?? 0} hrs ·
                Done: {summary.makeupHoursRendered ?? 0} hrs
              </span>
            </div>
          </div>
        )}
 
        <div className="card" style={{ marginTop: '1.25rem' }}>
          <div className="card-body">
            <h3>Quick Access</h3>
            <div className="quick-actions-grid">
              <button className="quick-action-btn btn-outline"
                onClick={() => navigate('/scholar/duties')}>
                <div className="quick-action-label">My Duty Records</div>
                <div className="quick-action-desc">
                  View all duty sessions — present, late, absent, pending
                </div>
              </button>
            </div>
          </div>
        </div>
 
      </div>
    </Layout>
  );
}
