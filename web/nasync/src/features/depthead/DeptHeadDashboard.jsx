import { useEffect, useState } from 'react';
import { useAuth } from '../../features/auth/AuthContext';
import Layout from '../../components/Layout';
import { deptHeadApi } from '../../api';
import { useNavigate } from 'react-router-dom';
import '../../styles/admin.css';
 
export default function DeptHeadDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);
 
  useEffect(() => {
    deptHeadApi.getPendingDuties()
      .then(r => setPending(r.data || []))
      .catch(e => console.error(e))
      .finally(() => setLoading(false));
  }, []);
 
  return (
    <Layout pageTitle="Department Head Dashboard">
      <div className="admin-dashboard">
        <div className="welcome-section">
          <h1>Welcome, {user?.firstName}!</h1>
          <p className="text-muted">
            Department Head —{' '}
            {new Date().toLocaleDateString('en-PH', {
              weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
            })}
          </p>
        </div>
 
        <div className="stats-grid">
          <div className="stat-card accent-navy">
            <span className="stat-label">Pending Approvals</span>
            <span className="stat-value">{loading ? '—' : pending.length}</span>
            <span className="stat-sub">Awaiting your action</span>
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
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
