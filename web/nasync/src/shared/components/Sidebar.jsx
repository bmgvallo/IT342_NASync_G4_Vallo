import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/AuthContext';
import "../styles/Sidebar.css";
 
const ADMIN_NAV = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/scholars', label: 'Scholars' },
  { to: '/admin/dept-heads', label: 'Dept Heads' },
  { to: '/admin/departments', label: 'Departments' },
  { to: '/admin/branches', label: 'Branches' },
  { to: '/admin/semesters', label: 'Semesters' },
  { to: '/admin/duty-days', label: 'Duty Days' },
];
 
const SCHOLAR_NAV = [
  { to: '/scholar', label: 'Dashboard' },
  { to: '/scholar/duties', label: 'My Duties' },
];
 
const DEPTHEAD_NAV = [
  { to: '/depthead', label: 'Dashboard' },
  { to: '/depthead/pending', label: 'Pending Duties' },
  { to: '/depthead/scholars', label: 'My Scholars' },
];
 
export default function Sidebar({ isOpen, onClose }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
 
  if (!user) return null;
 
  const navItems =
    user.role === 'ADMIN' ? ADMIN_NAV :
    user.role === 'SCHOLAR' ? SCHOLAR_NAV :
    DEPTHEAD_NAV;
 
  const roleLabel =
    user.role === 'ADMIN' ? 'OAS Administrator' :
    user.role === 'SCHOLAR' ? 'NAS Scholar' :
    'Department Head';
 
  const initials =
    `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();
 
  const handleLogout = async () => { await logout(); navigate('/login'); };
  const handleNavClick = () => { if (window.innerWidth <= 900) onClose(); };
 
  return (
    <>
      {isOpen && <div className="sidebar-overlay" onClick={onClose} />}
      <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <span className="sidebar-logo-text">NASync</span>
          </div>
        </div>
 
        <div className="sidebar-user-card">
          <div className="sidebar-avatar">{initials || '?'}</div>
          <div className="sidebar-user-info">
            <div className="sidebar-user-name">
              {user.firstName} {user.lastName}
            </div>
            <div className="sidebar-user-role">{roleLabel}</div>
          </div>
        </div>
 
        <nav className="sidebar-nav">
          {navItems.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/admin' || item.to === '/scholar' || item.to === '/depthead'}
              className={({ isActive }) =>
                `sidebar-nav-item ${isActive ? 'active' : ''}`
              }
              onClick={handleNavClick}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
 
        <div className="sidebar-footer">
          <button onClick={handleLogout} className="sidebar-logout">
            ↩ Logout
          </button>
        </div>
      </aside>
    </>
  );
}
