import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { scholarDutyApi } from '../../shared/api';
import '../../shared/styles/scholar.css';

const ATTENDANCE_BADGE = {
  PRESENT: 'badge-success',
  LATE: 'badge-warning',
  ABSENT: 'badge-danger',
};

const APPROVAL_BADGE = {
  PENDING: 'badge-info',
  APPROVED: 'badge-success',
  REJECTED: 'badge-danger',
};

const formatTo12Hour = (timeString) => {
    if (!timeString) return '—';
    let [hours, minutes] = timeString.split(':');
    hours = parseInt(hours);
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const hour12 = hours % 12 || 12;
    return `${hour12}:${minutes} ${ampm}`;
};
 
export default function ScholarMyDuties() {
  const [duties, setDuties] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [dutyTypeFilter, setDutyTypeFilter] = useState('ALL');
  const [attendanceFilter, setAttendanceFilter] = useState('ALL');
  const [approvalFilter, setApprovalFilter] = useState('ALL');
 
  useEffect(() => {
    scholarDutyApi.getMyDuties()
      .then(r => setDuties(r.data || []))
      .catch(e => console.error(e))
      .finally(() => setLoading(false));
  }, []);
 
  const filtered = duties.filter(d => {
    if (dutyTypeFilter !== 'ALL' && d.dutyType !== dutyTypeFilter) return false;
    
    if (attendanceFilter !== 'ALL' && d.attendanceStatus !== attendanceFilter) return false;
    
    if (approvalFilter !== 'ALL' && d.approvalStatus !== approvalFilter) return false;
    
    return true;
  });

  const totalCount = duties.length;
  const regularCount = duties.filter(d => d.dutyType === 'REGULAR').length;
  const makeupCount = duties.filter(d => d.dutyType === 'MAKEUP').length;
  const overtimeCount = duties.filter(d => d.dutyType === 'OVERTIME').length;
  const presentCount = duties.filter(d => d.attendanceStatus === 'PRESENT').length;
  const lateCount = duties.filter(d => d.attendanceStatus === 'LATE').length;
  const absentCount = duties.filter(d => d.attendanceStatus === 'ABSENT').length;
  const pendingCount = duties.filter(d => d.approvalStatus === 'PENDING').length;
  const approvedCount = duties.filter(d => d.approvalStatus === 'APPROVED').length;
  const rejectedCount = duties.filter(d => d.approvalStatus === 'REJECTED').length;

  const hasActiveFilters = dutyTypeFilter !== 'ALL' || attendanceFilter !== 'ALL' || approvalFilter !== 'ALL';
 
  return (
    <Layout pageTitle="My Duties">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>My Duties</h1>
            <p className="text-muted">
              {filtered.length} of {totalCount} record(s)
            </p>
          </div>
        </div>
 
        <div className="card">
          <div className="card-body">
            
            <div className="dropdown-filters">
              <div className="filter-dropdown">
                <label className="filter-label">Duty Type</label>
                <select 
                  className="filter-select"
                  value={dutyTypeFilter}
                  onChange={(e) => setDutyTypeFilter(e.target.value)}
                >
                  <option value="ALL">All</option>
                  <option value="REGULAR">Regular</option>
                  <option value="MAKEUP">Makeup</option>
                  <option value="OVERTIME">Overtime</option>
                </select>
              </div>

              <div className="filter-dropdown">
                <label className="filter-label">Attendance Status</label>
                <select 
                  className="filter-select"
                  value={attendanceFilter}
                  onChange={(e) => setAttendanceFilter(e.target.value)}
                >
                  <option value="ALL">All</option>
                  <option value="PRESENT">Present</option>
                  <option value="LATE">Late</option>
                  <option value="ABSENT">Absent</option>
                </select>
              </div>

              <div className="filter-dropdown">
                <label className="filter-label">Approval Status</label>
                <select 
                  className="filter-select"
                  value={approvalFilter}
                  onChange={(e) => setApprovalFilter(e.target.value)}
                >
                  <option value="ALL">All</option>
                  <option value="PENDING">Pending</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                </select>
              </div>
            </div>

            {hasActiveFilters && (
              <div className="clear-filters">
                <button 
                  className="btn btn-sm btn-ghost"
                  onClick={() => {
                    setDutyTypeFilter('ALL');
                    setAttendanceFilter('ALL');
                    setApprovalFilter('ALL');
                  }}>
                  Clear All Filters
                </button>
              </div>
            )}
 
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : filtered.length === 0 ? (
              <div className="empty-state">
                <p>No duty records found.</p>
              </div>
            ) : (
              <div className="table-responsive" style={{ marginTop: '20px' }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Type</th>
                      <th>Time In</th>
                      <th>Time Out</th>
                      <th>Attendance</th>
                      <th>Approval Status</th>
                      <th>Remarks</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map(d => (
                      <tr key={d.dutyId}>
                        <td>{d.dutyDate}</td>
                        <td>
                          <span className={`badge ${d.dutyType === 'REGULAR' ? 'badge-info' : d.dutyType === 'MAKEUP' ? 'badge-gold' : 'badge-maroon'}`}>
                            {d.dutyType}
                          </span>
                        </td>
                        <td>{formatTo12Hour(d.timeIn)}</td>
                        <td>{formatTo12Hour(d.timeOut)}</td>
                        <td>
                          <span className={`badge ${ATTENDANCE_BADGE[d.attendanceStatus] || 'badge-muted'}`}>
                            {d.attendanceStatus || '—'}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${APPROVAL_BADGE[d.approvalStatus] || 'badge-muted'}`}>
                            {d.approvalStatus || 'PENDING'}
                          </span>
                        </td>
                        <td style={{ maxWidth: '200px', wordBreak: 'break-word' }}>
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
      </div>
    </Layout>
  );
}