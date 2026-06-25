import { useEffect, useState } from 'react';
import { useAuth } from '../../features/auth/AuthContext';
import Layout from '../../shared/components/Layout';
import { deptHeadApi, authApi } from '../../shared/api';
import { useNavigate } from 'react-router-dom';
import '../../shared/styles/admin.css';
 
export default function DeptHeadDashboard() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [pending, setPending] = useState([]);
  const [scholars, setScholars] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      deptHeadApi.getPendingDuties().catch(() => ({ data: [] })),
      deptHeadApi.getScholars().catch(() => ({ data: [] })),
      authApi.getMe().catch(() => ({ data: null })),
    ]).then(([pendingRes, scholarsRes, meRes]) => {
      setPending(pendingRes.data || []);
      setScholars(scholarsRes.data || []);
      if (meRes.data) updateUser(meRes.data);
    }).finally(() => setLoading(false));
  }, []);
 
  return (
    <Layout pageTitle="Department Head Dashboard">
      <div className="admin-dashboard">
        <div className="welcome-section" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div>
            <h1 style={{ margin: 0 }}>Welcome, {user?.firstName}!</h1>
            <p className="text-muted" style={{ margin: 0 }}>
              Department Head —{' '}
              {new Date().toLocaleDateString('en-PH', {
                weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
              })}
            </p>
          </div>
        </div>
 
        <div className="stats-grid">
          <div className="stat-card accent-navy">
            <span className="stat-label">Pending Approvals</span>
            <span className="stat-value">{loading ? '—' : pending.length}</span>
            <span className="stat-sub">Awaiting your action</span>
          </div>
          <div className="stat-card accent-navy">
            <span className="stat-label">Total Scholars</span>
            <span className="stat-value">{loading ? '—' : scholars.length}</span>
            <span className="stat-sub">In your branch</span>
          </div>
        </div>
 
        <div className="card">
          <div className="card-body">
            <h3>Quick Access</h3>
            <div className="quick-actions-grid">
              <button className="quick-action-btn btn-outline"
                onClick={() => navigate('/depthead/pending')}>
                <div className="quick-action-label">Pending Duties</div>
                <div className="quick-action-desc">
                  Review and approve or reject scholar submissions
                </div>
              </button>
              <button className="quick-action-btn btn-outline"
                onClick={() => navigate('/depthead/scholars')}>
                <div className="quick-action-label">Manage Scholars</div>
                <div className="quick-action-desc">
                  View and manage scholar information
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
