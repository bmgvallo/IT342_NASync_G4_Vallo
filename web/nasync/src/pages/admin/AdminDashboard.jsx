import { useAuth } from '../../contexts/AuthContext';
import Layout from '../../components/Layout';
import { useNavigate } from 'react-router-dom';
import '../../styles/admin.css';

export default function AdminDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const stats = [
    { label: 'Total Scholars', value: '—', sub: 'Active this semester', accent: 'accent-navy' },
    { label: 'Total Dept Heads', value: '—', sub: 'Across all branches', accent: 'accent-gold' },
    { label: 'Active Users', value: '—', sub: 'Currently active', accent: 'accent-navy' },
    { label: 'Pending Actions', value: '—', sub: 'Require attention', accent: 'accent-gold' },
  ];

  const quickActions = [
    { label: 'Register New Scholar', desc: 'Add a NAS scholar', page: 'scholars', color: 'btn-outline' },
    { label: 'Manage Departments', desc: 'View and configure departments', page: 'departments', color: 'btn-outline' },
    { label: 'Manage Branches', desc: 'Configure branches per department', page: 'branches', color: 'btn-outline' },
    { label: 'Register Dept Head', desc: 'Add department head', page: 'dept-heads', color: 'btn-outline' },
    { label: 'View All Users', desc: 'Browse and manage user accounts', page: 'users', color: 'btn-outline' },
  ];

  const handleQuickAction = (page) => {
    navigate(`/admin/${page}`);
  };

  return (
    <Layout pageTitle="Admin Dashboard">
      <div className="admin-dashboard">
        <div className="welcome-section">
          <h1>Welcome back, {user?.firstName}</h1>
          <p className="text-muted">
            OAS Admin Dashboard — {new Date().toLocaleDateString('en-PH', { 
              weekday: 'long', 
              year: 'numeric', 
              month: 'long', 
              day: 'numeric' 
            })}
          </p>
        </div>

        <div className="stats-grid">
          {stats.map((stat, index) => (
            <div key={index} className={`stat-card ${stat.accent}`}>
              <span className="stat-label">{stat.label}</span>
              <span className="stat-value">{stat.value}</span>
              <span className="stat-sub">{stat.sub}</span>
            </div>
          ))}
        </div>

        <div className="card">
          <div className="card-header">
            <h2>Quick Actions</h2>
          </div>
          <div className="quick-actions-grid">
            {quickActions.map((action, index) => (
              <button
                key={index}
                className={`quick-action-btn ${action.color}`}
                onClick={() => handleQuickAction(action.page)}
              >
                <div className="quick-action-label">{action.label}</div>
                <div className="quick-action-desc">{action.desc}</div>
              </button>
            ))}
          </div>
        </div>
      </div>
    </Layout>
  );
}