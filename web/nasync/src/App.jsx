import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import AdminDashboard from './pages/admin/AdminDashboard';
import ScholarManagement from './pages/admin/ScholarManagement';
import DepartmentManagement from './pages/admin/DepartmentManagement';
import BranchManagement from './pages/admin/BranchManagement';
import DeptHeadManagement from './pages/admin/DeptHeadManagement';
import UsersPage from './pages/admin/UsersPage';
import ScholarDashboard from './pages/scholar/ScholarDashboard';
import './styles/global.css';

const Unauthorized = () => (
  <div className="unauthorized-container">
    <div className="unauthorized-card">
      <h1>403</h1>
      <h2>Unauthorized Access</h2>
      <p>You don't have permission to access this page.</p>
      <a href="/login" className="btn btn-primary">Go to Login</a>
    </div>
  </div>
);

const ProtectedLogin = () => {
  const { isAuthenticated, user } = useAuth();
  
  if (isAuthenticated) {
    if (user?.role === 'ADMIN') {
      return <Navigate to="/admin" replace />;
    } else if (user?.role === 'SCHOLAR') {
      return <Navigate to="/scholar" replace />;
    } else if (user?.role === 'DEPARTMENT_HEAD') {
      return <Navigate to="/depthead" replace />;
    }
    return <Navigate to="/" replace />;
  }
  
  return <Login />;
};

const RoleBasedRedirect = () => {
  const { user, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  } else if (user?.role === 'SCHOLAR') {
    return <Navigate to="/scholar" replace />;
  } else if (user?.role === 'DEPARTMENT_HEAD') {
    return <Navigate to="/depthead" replace />;
  }

  return <Navigate to="/login" replace />;
};

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<ProtectedLogin />} />
      
      <Route path="/unauthorized" element={<Unauthorized />} />
      
      <Route path="/" element={<RoleBasedRedirect />} />

      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/scholars"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <ScholarManagement />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/dept-heads"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <DeptHeadManagement />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/departments"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <DepartmentManagement />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/branches"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <BranchManagement />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <UsersPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/scholar"
        element={
          <ProtectedRoute allowedRoles={['SCHOLAR']}>
            <ScholarDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/scholar/dashboard"
        element={
          <ProtectedRoute allowedRoles={['SCHOLAR']}>
            <ScholarDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/scholar/history"
        element={
          <ProtectedRoute allowedRoles={['SCHOLAR']}>
            <div>Duty History (Coming Soon)</div>
          </ProtectedRoute>
        }
      />
      <Route
        path="/scholar/profile"
        element={
          <ProtectedRoute allowedRoles={['SCHOLAR']}>
            <div>My Profile (Coming Soon)</div>
          </ProtectedRoute>
        }
      />

      <Route
        path="/depthead"
        element={
          <ProtectedRoute allowedRoles={['DEPARTMENT_HEAD']}>
            <div>Department Head Dashboard (Coming Soon)</div>
          </ProtectedRoute>
        }
      />
      <Route
        path="/depthead/pending"
        element={
          <ProtectedRoute allowedRoles={['DEPARTMENT_HEAD']}>
            <div>Pending Duties (Coming Soon)</div>
          </ProtectedRoute>
        }
      />
      <Route
        path="/depthead/scholars"
        element={
          <ProtectedRoute allowedRoles={['DEPARTMENT_HEAD']}>
            <div>My Scholars (Coming Soon)</div>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<RoleBasedRedirect />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  );
}

export default App;