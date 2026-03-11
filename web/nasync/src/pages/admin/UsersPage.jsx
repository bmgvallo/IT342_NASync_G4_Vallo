import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { adminApi } from '../../api';
import '../../styles/admin.css';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filterRole, setFilterRole] = useState('ALL');

  const loadData = async () => {
    setLoading(true);
    try {
      const [usersRes, deptsRes] = await Promise.all([
        adminApi.getAllUsers(),
        adminApi.getDepartments()
      ]);
      
      setUsers(usersRes.data?.data || usersRes.data || []);
      setDepartments(deptsRes.data?.data || deptsRes.data || []);
    } catch (err) {
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredUsers = users.filter(user => {
    const q = search.toLowerCase();
    const matchSearch = !q || 
      user.schoolId?.toLowerCase().includes(q) ||
      user.firstName?.toLowerCase().includes(q) ||
      user.lastName?.toLowerCase().includes(q) ||
      user.email?.toLowerCase().includes(q);
    
    const matchRole = filterRole === 'ALL' || user.role === filterRole;
    
    return matchSearch && matchRole;
  });

  const getRoleBadge = (role) => {
    if (role === 'ADMIN') return <span className="badge badge-maroon">Admin</span>;
    if (role === 'DEPARTMENT_HEAD') return <span className="badge badge-gold">Dept Head</span>;
    return <span className="badge badge-info">Scholar</span>;
  };

  const toggleActive = async (userId) => {
    try {
      await adminApi.toggleUserActive(userId);
      loadData();
    } catch (err) {
      alert('Failed to update user status');
    }
  };

  return (
    <Layout pageTitle="User Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>All Users</h1>
            <p className="text-muted">{users.length} total users</p>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="toolbar">
              <input
                type="text"
                className="form-control search-input"
                placeholder="Search by name, ID, or email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <select
                className="form-control filter-select"
                value={filterRole}
                onChange={(e) => setFilterRole(e.target.value)}
              >
                <option value="ALL">All Roles</option>
                <option value="SCHOLAR">Scholars</option>
                <option value="DEPARTMENT_HEAD">Dept Heads</option>
                <option value="ADMIN">Admins</option>
              </select>
              <span className="text-muted result-count">
                {filteredUsers.length} result{filteredUsers.length !== 1 ? 's' : ''}
              </span>
            </div>

            <div className="table-responsive">
              {loading ? (
                <div className="loading-state">
                  <div className="spinner"></div>
                </div>
              ) : filteredUsers.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">👥</div>
                  <p>No users found</p>
                </div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>School ID</th>
                      <th>Name</th>
                      <th>Role</th>
                      <th>Email</th>
                      <th>Department</th>
                      <th>Branch</th>
                      <th>Shift</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map(user => (
                      <tr key={user.userId}>
                        <td><code>{user.schoolId}</code></td>
                        <td>{user.lastName}, {user.firstName}</td>
                        <td>{getRoleBadge(user.role)}</td>
                        <td>{user.email}</td>
                        <td>{user.departmentName || user.deptName || '—'}</td>
                        <td>{user.branchName || '—'}</td>
                        <td>{user.shift || '—'}</td>
                        <td>
                          <span className={`badge ${user.active ? 'badge-success' : 'badge-muted'}`}>
                            {user.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          {user.role !== 'ADMIN' && (
                            <button
                              className={`btn btn-sm ${user.active ? 'btn-ghost' : 'btn-success'}`}
                              onClick={() => toggleActive(user.userId)}
                            >
                              {user.active ? 'Deactivate' : 'Activate'}
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}