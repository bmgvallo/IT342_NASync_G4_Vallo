import { useEffect, useState } from 'react';
import { useAuth } from '../../features/auth/AuthContext';
import Layout from '../../shared/components/Layout';
import { scholarDutyApi, semesterApi, authApi } from '../../shared/api';
import { useNavigate } from 'react-router-dom';
import { formatTime } from '../../shared/utils/formatters';
import '../../shared/styles/scholar.css';

export default function ScholarDashboard() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();

  const [summary, setSummary] = useState(null);
  const [openDuty, setOpenDuty] = useState(null);
  const [hasRegularToday, setHasRegularToday] = useState(false);
  const [absentToday, setAbsentToday] = useState(false);
  const [dutyType, setDutyType] = useState('REGULAR');
  const [todaySchedule, setTodaySchedule] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [activeSemester, setActiveSemester] = useState(undefined);

  const showMsg = (text, type = 'success') => {
    setMessage({ text, type });
    setTimeout(() => setMessage({ text: '', type: '' }), 4000);
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const [sumRes, dutiesRes, semRes, meRes, schedRes] = await Promise.all([
        scholarDutyApi.getSummary(),
        scholarDutyApi.getMyDuties(),
        semesterApi.getActive().catch(() => ({ data: null })),
        authApi.getMe().catch(() => ({ data: null })),
        scholarDutyApi.getTodaySchedule().catch(() => ({ data: null })),
      ]);
      setSummary(sumRes.data);
      setActiveSemester(semRes.data?.semesterId ? semRes.data : null);
      if (meRes.data) updateUser(meRes.data);
      setTodaySchedule(schedRes.data && Object.keys(schedRes.data).length ? schedRes.data : null);

      const _now = new Date();
      const today = `${_now.getFullYear()}-${String(_now.getMonth() + 1).padStart(2, '0')}-${String(_now.getDate()).padStart(2, '0')}`;
      const todayDuties = (dutiesRes.data || []).filter(
        d => d.dutyDate === today
      );

      // only duties with a timeIn count as open — absent records have no timeIn
      const open = todayDuties.find(d => d.timeIn && !d.timeOut);
      setOpenDuty(open || null);

      // detect absent record created by backend on clock-in past the threshold
      const isAbsentToday = todayDuties.some(d => d.attendanceStatus === 'ABSENT');
      setAbsentToday(isAbsentToday);

      // REGULAR chip disabled when: absent, open regular, or both shifts completed (2+)
      const hasOpenRegular = todayDuties.some(
        d => d.dutyType === 'REGULAR' && d.approvalStatus !== 'REJECTED' && d.timeIn && !d.timeOut
      );
      const completedRegularCount = todayDuties.filter(
        d => d.dutyType === 'REGULAR' && d.approvalStatus !== 'REJECTED' && d.timeOut
      ).length;
      const regularDone = isAbsentToday || hasOpenRegular || completedRegularCount >= 2;
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

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 30 * 1000);
    return () => clearInterval(interval);
  }, []);

  const handleClockIn = async () => {
    setActionLoading(true);
    try {
      const res = await scholarDutyApi.clockIn(dutyType);
      const d = res.data;
      showMsg(
        `Clocked in at ${formatTime(d.timeIn)} — Status: ${d.status} (${d.dutyType})`,
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
      showMsg(`Clocked out at ${formatTime(res.data.timeOut)}. Pending department head review.`);
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


  const effectiveExpectedTimeOut = openDuty?.expectedTimeOut || user?.expectedTimeOut;
  const clockOutAllowed = !effectiveExpectedTimeOut || (() => {
    const [h, m] = effectiveExpectedTimeOut.split(':').map(Number);
    const expectedOut = new Date();
    expectedOut.setHours(h, m, 0, 0);
    return new Date() >= expectedOut;
  })();

  return (
    <Layout pageTitle="Scholar Dashboard">
      <div className="scholar-dashboard">

        <div className="welcome-section" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div>
            <h1 style={{ margin: 0 }}>{greeting()}, {user?.firstName}!</h1>
            <p className="text-muted" style={{ margin: 0 }}>
              {new Date().toLocaleDateString('en-PH', {
                weekday: 'long', year: 'numeric',
                month: 'long', day: 'numeric'
              })}
            </p>
            {(user?.departmentName || user?.branchName) && (
              <p className="text-muted" style={{ margin: 0, fontSize: '0.85rem' }}>
                {[user?.departmentName, user?.branchName].filter(Boolean).join(' · ')}
              </p>
            )}
          </div>
        </div>

        {message.text && (
          <div className={`scholar-alert scholar-alert-${message.type}`}>
            <span>{message.text}</span>
            <button onClick={() => setMessage({ text: '', type: '' })}>×</button>
          </div>
        )}

        {activeSemester === null && (
          <div className="scholar-alert scholar-alert-error">
            <span>No active semester is currently available. Duty recording is temporarily disabled.</span>
          </div>
        )}

        {absentToday && (
          <div className="scholar-alert scholar-alert-error">
            <span>You have been marked <strong>Absent</strong> for today's duty. Please contact your Department Head to arrange a makeup schedule.</span>
          </div>
        )}

        <div className="duty-clock-card">
          <div className="duty-clock-header">
            <div>
              <div className="duty-clock-label">TODAY'S DUTY</div>

              <div className="duty-clock-shift">
                {(() => {
                  if (todaySchedule?.primaryTimeIn && todaySchedule?.primaryTimeOut) {
                    return (
                      <div className="duty-shifts">
                        <div>
                          <strong>Shift 1:</strong>{' '}
                          {formatTime(todaySchedule.primaryTimeIn)} –{' '}
                          {formatTime(todaySchedule.primaryTimeOut)}
                        </div>

                        {todaySchedule.secondaryTimeIn &&
                          todaySchedule.secondaryTimeOut && (
                            <div>
                              <strong>Shift 2:</strong>{' '}
                              {formatTime(todaySchedule.secondaryTimeIn)} –{' '}
                              {formatTime(todaySchedule.secondaryTimeOut)}
                            </div>
                          )}
                      </div>
                    );
                  }

                  if (user?.expectedTimeIn && user?.expectedTimeOut) {
                    return (
                      <div>
                        {formatTime(user.expectedTimeIn)} –{' '}
                        {formatTime(user.expectedTimeOut)}
                      </div>
                    );
                  }

                  return '—';
                })()}
              </div>
            </div>

            {openDuty ? (
              <div className="clock-action-area">
                <div className="open-duty-badge">
                  <span className={`status-dot ${statusColor(openDuty.status)}`}></span>
                  <span>
                    {openDuty.dutyType} · In at <strong>{formatTime(openDuty.timeIn)}</strong>
                  </span>
                </div>
                <button
                  className="btn btn-danger clock-btn"
                  onClick={handleClockOut}
                  disabled={actionLoading || activeSemester === null || !clockOutAllowed}
                  title={!clockOutAllowed ? `Clock-out available after ${formatTime(effectiveExpectedTimeOut)}` : ''}
                >
                  {actionLoading ? (
                    <><span className="btn-spinner"></span> Processing…</>
                  ) : '⏹ Clock Out'}
                </button>
                {!clockOutAllowed && (
                  <p className="min-hour-hint">Clock-out available after {formatTime(effectiveExpectedTimeOut)}</p>
                )}
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
                  disabled={actionLoading || activeSemester === null}
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
              <button className="quick-action-btn btn-outline"
                onClick={() => navigate('/change-password')}>
                <div className="quick-action-label">Change Password</div>
                <div className="quick-action-desc">
                  Update your account password
                </div>
              </button>
            </div>
          </div>
        </div>

      </div>
    </Layout>
  );
}
