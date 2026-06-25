import { useState } from 'react';
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
  { to: '/profile', label: 'Profile' },
];

const SCHOLAR_NAV = [
  { to: '/scholar', label: 'Dashboard' },
  { to: '/scholar/duties', label: 'My Duties' },
  { to: '/profile', label: 'Profile' },
];

const DEPTHEAD_NAV = [
  { to: '/depthead', label: 'Dashboard' },
  { to: '/depthead/pending', label: 'Pending Duties' },
  { to: '/depthead/scholars', label: 'My Scholars' },
  { to: '/profile', label: 'Profile' },
];

export default function Sidebar({ isOpen, onClose }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [logoutLoading, setLogoutLoading] = useState(false);

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

  const handleLogoutConfirm = async () => {
    setLogoutLoading(true);
    try {
      await logout();
      navigate('/login');
    } finally {
      setLogoutLoading(false);
      setShowLogoutModal(false);
    }
  };
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
          <div style={{ flexShrink: 0 }}>
            {user?.profilePhotoUrl ? (
              <img
                src={user.profilePhotoUrl}
                alt="Profile"
                onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
                style={{ width: 64, height: 64, borderRadius: '50%', objectFit: 'cover', border: '2px solid #e5e7eb', display: 'block' }}
              />
            ) : null}
            <div style={{
              width: 64, height: 64, borderRadius: '50%', background: '#1e3a5f',
              color: '#fff', fontSize: '1.5rem', fontWeight: 700,
              display: user?.profilePhotoUrl ? 'none' : 'flex',
              alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}>
              {user?.firstName?.[0]?.toUpperCase()}{user?.lastName?.[0]?.toUpperCase() || '?'}
            </div>
          </div>
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
          <button onClick={() => setShowLogoutModal(true)} className="sidebar-logout">
            ↩ Logout
          </button>
        </div>
      </aside>

      {showLogoutModal && (
        <div className="modal-overlay">
          <div className="modal" style={{ maxWidth: 400 }}>
            <div className="modal-header">
              <h3>Log Out</h3>
            </div>
            <div className="modal-body">
              <p>Are you sure you want to log out?</p>
            </div>
            <div className="modal-footer">
              <button
                className="btn btn-secondary"
                onClick={() => setShowLogoutModal(false)}
                disabled={logoutLoading}
              >
                Cancel
              </button>
              <button
                className="btn btn-danger"
                onClick={handleLogoutConfirm}
                disabled={logoutLoading}
              >
                {logoutLoading ? 'Logging out…' : 'Log Out'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
