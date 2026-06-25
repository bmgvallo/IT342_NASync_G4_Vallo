import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { adminApi } from '../../shared/api';
import '../../shared/styles/admin.css';

const DEFAULT_FORM = { name: '', deptId: '' };

export default function BranchManagement() {
  const [branches, setBranches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('active');
  const [showModal, setShowModal] = useState(false);
  const [editingBranch, setEditingBranch] = useState(null);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [infoBranch, setInfoBranch] = useState(null);
  const [infoUsers, setInfoUsers] = useState([]);
  const [infoLoading, setInfoLoading] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [branchesRes, deptsRes] = await Promise.all([
        adminApi.getBranches(),
        adminApi.getDepartments()
      ]);
      setBranches(branchesRes.data?.data || branchesRes.data || []);
      setDepartments(deptsRes.data?.data || deptsRes.data || []);
    } catch (err) {
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const activeBranches = branches.filter(b => b.isActive !== false);
  const deactivatedBranches = branches.filter(b => b.isActive === false);
  const displayed = activeTab === 'active' ? activeBranches : deactivatedBranches;

  const openInfoModal = async (branch) => {
    setInfoBranch(branch);
    setInfoUsers([]);
    setInfoLoading(true);
    try {
      const res = await adminApi.getAllUsers();
      const all = res.data || [];
      setInfoUsers(all.filter(u => u.branchId === branch.branchId));
    } catch {
      setInfoUsers([]);
    } finally {
      setInfoLoading(false);
    }
  };

  const closeInfoModal = () => {
    setInfoBranch(null);
    setInfoUsers([]);
  };

  const openCreateModal = () => {
    setEditingBranch(null);
    setForm(DEFAULT_FORM);
    setError('');
    setShowModal(true);
  };

  const openEditModal = (branch) => {
    setEditingBranch(branch);
    setForm({ name: branch.name, deptId: String(branch.deptId || '') });
    setError('');
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingBranch(null);
    setForm(DEFAULT_FORM);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.deptId) {
      setError('Branch name and department are required');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      if (editingBranch) {
        await adminApi.updateBranch(editingBranch.branchId, form.name.trim(), Number(form.deptId));
      } else {
        await adminApi.createBranch(form.name.trim(), Number(form.deptId));
      }
      closeModal();
      loadData();
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to save branch');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (branch) => {
    if (!confirm(`Deactivate branch "${branch.name}"? It can be reactivated later.`)) return;
    try {
      await adminApi.deleteBranch(branch.branchId);
      loadData();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to deactivate branch.');
    }
  };

  const handleReactivate = async (branch) => {
    try {
      await adminApi.reactivateBranch(branch.branchId);
      loadData();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to reactivate branch.');
    }
  };

  const infoDeptHead = infoUsers.find(u => u.role === 'DEPARTMENT_HEAD');
  const infoScholars = infoUsers.filter(u => u.role === 'SCHOLAR');

  return (
    <Layout pageTitle="Branch Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Branches</h1>
            <p className="text-muted">
              {activeBranches.length} active · {deactivatedBranches.length} deactivated
            </p>
          </div>
          <button className="btn btn-primary" onClick={openCreateModal}>+ Add Branch</button>
        </div>

        <div style={{ display: 'flex', gap: '8px', marginBottom: '1rem' }}>
          <button
            className={`btn ${activeTab === 'active' ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setActiveTab('active')}
          >
            Active ({activeBranches.length})
          </button>
          <button
            className={`btn ${activeTab === 'deactivated' ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setActiveTab('deactivated')}
          >
            Deactivated ({deactivatedBranches.length})
          </button>
        </div>

        <div className="card">
          <div className="table-responsive">
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : displayed.length === 0 ? (
              <div className="empty-state">
                <p>{activeTab === 'active' ? 'No active branches' : 'No deactivated branches'}</p>
              </div>
            ) : (
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Branch Name</th>
                    <th>Department</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {displayed.map(branch => (
                    <tr key={branch.branchId}>
                      <td className="text-muted">{branch.branchId}</td>
                      <td>{branch.name}</td>
                      <td>{branch.departmentName || '—'}</td>
                      <td className="text-muted">
                        {branch.createdAt ? new Date(branch.createdAt).toLocaleDateString() : '—'}
                      </td>
                      <td>
                        <div className="table-actions">
                          <button
                            className="btn btn-sm btn-ghost"
                            title="View dept head and scholars"
                            onClick={() => openInfoModal(branch)}
                          >
                            Info
                          </button>
                          {activeTab === 'active' ? (
                            <>
                              <button
                                className="btn btn-sm btn-secondary"
                                onClick={() => openEditModal(branch)}
                              >
                                🖍
                              </button>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleDeactivate(branch)}
                              >
                                Deactivate
                              </button>
                            </>
                          ) : (
                            <button
                              className="btn btn-sm btn-primary"
                              onClick={() => handleReactivate(branch)}
                            >
                              Reactivate
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Branch Info Modal */}
        {infoBranch && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeInfoModal()}>
            <div className="modal" style={{ maxWidth: '520px' }}>
              <div className="modal-header">
                <h3>{infoBranch.name} — Branch Details</h3>
                <button className="modal-close" onClick={closeInfoModal}>×</button>
              </div>
              <div className="modal-body">
                {infoLoading ? (
                  <div className="loading-state"><div className="spinner"></div></div>
                ) : (
                  <>
                    <div style={{ marginBottom: '1rem' }}>
                      <strong>Department Head</strong>
                      {infoDeptHead ? (
                        <p style={{ marginTop: '0.25rem' }}>
                          {infoDeptHead.firstName} {infoDeptHead.lastName}
                          <span className="text-muted" style={{ marginLeft: '8px' }}>
                            ({infoDeptHead.schoolId})
                          </span>
                        </p>
                      ) : (
                        <p className="text-muted" style={{ marginTop: '0.25rem' }}>No department head assigned</p>
                      )}
                    </div>

                    <div>
                      <strong>Scholars ({infoScholars.length})</strong>
                      {infoScholars.length === 0 ? (
                        <p className="text-muted" style={{ marginTop: '0.25rem' }}>No scholars assigned</p>
                      ) : (
                        <table className="table" style={{ marginTop: '0.5rem' }}>
                          <thead>
                            <tr>
                              <th>Name</th>
                              <th>School ID</th>
                              <th>Status</th>
                            </tr>
                          </thead>
                          <tbody>
                            {infoScholars.map(s => (
                              <tr key={s.userId}>
                                <td>{s.firstName} {s.lastName}</td>
                                <td className="text-muted">{s.schoolId}</td>
                                <td>
                                  <span style={{
                                    color: s.isActive ? 'var(--color-success, green)' : 'var(--color-error, #c00)',
                                    fontWeight: 500
                                  }}>
                                    {s.isActive ? 'Active' : 'Inactive'}
                                  </span>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                    </div>
                  </>
                )}
              </div>
              <div className="modal-footer">
                <button className="btn btn-ghost" onClick={closeInfoModal}>Close</button>
              </div>
            </div>
          </div>
        )}

        {/* Create / Edit Modal */}
        {showModal && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeModal()}>
            <div className="modal">
              <div className="modal-header">
                <h3>{editingBranch ? 'Edit Branch' : 'Add Branch'}</h3>
                <button className="modal-close" onClick={closeModal}>×</button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  {error && <div className="alert alert-error">{error}</div>}
                  <div className="form-group">
                    <label className="form-label">Branch Name *</label>
                    <input
                      type="text"
                      name="name"
                      className="form-control"
                      value={form.name}
                      onChange={handleChange}
                      placeholder="e.g. IT Department"
                      autoFocus
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Department *</label>
                    <select
                      name="deptId"
                      className="form-control"
                      value={form.deptId}
                      onChange={handleChange}
                      required
                    >
                      <option value="">Select department</option>
                      {departments.filter(d => d.isActive !== false).map(dept => (
                        <option key={dept.departmentId} value={dept.departmentId}>
                          {dept.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={closeModal}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Saving...' : 'Save'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
