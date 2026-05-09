import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './features/auth/AuthContext';
import ProtectedRoute from './shared/components/ProtectedRoute';
import Login from './features/auth/Login';
import AdminDashboard from './features/admin/AdminDashboard';
import ScholarManagement from './features/admin/ScholarManagement';
import DepartmentManagement from './features/department/DepartmentManagement';
import BranchManagement from './features/branch/BranchManagement';
import DeptHeadManagement from './features/admin/DeptHeadManagement';
import UsersPage from './features/admin/UsersPage';
import ScholarDashboard from './features/scholar/ScholarDashboard';
import './shared/styles/global.css';
import AuthCallback from './features/auth/AuthCallback';
import SemesterManagement from './features/semester/SemesterManagement';
import DutyDayManagement from './features/dutyday/DutyDayManagement';
import ScholarMyDuties from './features/scholar/ScholarMyDuties';
import DeptHeadDashboard from './features/depthead/DeptHeadDashboard';
import PendingDuties from './features/depthead/PendingDuties';
import DeptHeadScholars from './features/depthead/DeptHeadScholars';


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
      <Route path="/auth/callback" element={<AuthCallback />} />
 
      <Route path="/admin" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
      } />
      <Route path="/admin/scholars" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><ScholarManagement /></ProtectedRoute>
      } />
      <Route path="/admin/dept-heads" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><DeptHeadManagement /></ProtectedRoute>
      } />
      <Route path="/admin/departments" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><DepartmentManagement /></ProtectedRoute>
      } />
      <Route path="/admin/branches" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><BranchManagement /></ProtectedRoute>
      } />
      <Route path="/admin/semesters" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><SemesterManagement /></ProtectedRoute>
      } />
      <Route path="/admin/duty-days" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><DutyDayManagement /></ProtectedRoute>
      } />
      <Route path="/admin/users" element={
        <ProtectedRoute allowedRoles={['ADMIN']}><UsersPage /></ProtectedRoute>
      } />
 
      <Route path="/scholar" element={
        <ProtectedRoute allowedRoles={['SCHOLAR']}><ScholarDashboard /></ProtectedRoute>
      } />
      <Route path="/scholar/dashboard" element={
        <ProtectedRoute allowedRoles={['SCHOLAR']}><ScholarDashboard /></ProtectedRoute>
      } />
      <Route path="/scholar/duties" element={
        <ProtectedRoute allowedRoles={['SCHOLAR']}><ScholarMyDuties /></ProtectedRoute>
      } />
 
      <Route path="/depthead" element={
        <ProtectedRoute allowedRoles={['DEPARTMENT_HEAD']}>
          <DeptHeadDashboard />
        </ProtectedRoute>
      } />
      <Route path="/depthead/pending" element={
        <ProtectedRoute allowedRoles={['DEPARTMENT_HEAD']}>
          <PendingDuties />
        </ProtectedRoute>
      } />
      <Route path="/depthead/scholars" element={
        <ProtectedRoute allowedRoles={['DEPARTMENT_HEAD']}>
          <DeptHeadScholars />
        </ProtectedRoute>
      } />

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