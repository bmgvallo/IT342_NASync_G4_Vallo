import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import Sidebar from './Sidebar';
import Toast from './Toast';
import '../styles/Layout.css';

export default function Layout({ children, pageTitle }) {
  const { user } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [showToast, setShowToast] = useState(false);

  useEffect(() => {
    const shouldShowToast = sessionStorage.getItem('showLoginToast');
    if (shouldShowToast === 'true') {
      setShowToast(true);
      sessionStorage.removeItem('showLoginToast');
      
      const timer = setTimeout(() => {
        setShowToast(false);
      }, 3000);
      
      return () => clearTimeout(timer);
    }
  }, []);

  const toggleSidebar = () => setSidebarOpen(!sidebarOpen);

  return (
    <div className="layout-root">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      
      <main className="layout-main">
        {showToast && (
          <Toast 
            message="Login Successful." 
            type="success" 
            onClose={() => setShowToast(false)}
          />
        )}

        <div className="topbar">
          <button className="topbar-menu-btn" onClick={toggleSidebar}>
            ☰
          </button>
          <div className="topbar-title">{pageTitle || 'Dashboard'}</div>
          <div className="topbar-right">
            <span className="topbar-school-id">{user?.schoolId}</span>
          </div>
        </div>

        <div className="layout-content">
          {children}
        </div>
      </main>
    </div>
  );
}