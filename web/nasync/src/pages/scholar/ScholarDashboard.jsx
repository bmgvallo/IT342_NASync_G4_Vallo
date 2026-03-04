import { useAuth } from '../../contexts/AuthContext';
import Layout from '../../components/Layout';
import '../../styles/scholar.css';

export default function ScholarDashboard() {
  const { user } = useAuth();

  return (
    <Layout pageTitle="Scholar Dashboard">
      <div className="scholar-dashboard">
        <div className="welcome-card">
          <h1>Welcome, {user?.firstName}!</h1>
          <p className="text-muted">NAS Scholar Dashboard</p>
        </div>
      </div>
    </Layout>
  );
}