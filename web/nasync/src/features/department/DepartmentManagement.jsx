import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { adminApi } from '../../shared/api';
import '../../shared/styles/admin.css';

export default function DepartmentManagement() {
  const [departments, setDepartments] = useState([]);
  const [allBranches, setAllBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('active');
  const [showModal, setShowModal] = useState(false);
  const [editingDept, setEditingDept] = useState(null);
  const [formName, setFormName] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [infoDept, setInfoDept] = useState(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const [deptsRes, branchesRes] = await Promise.all([
        adminApi.getDepartments(),
        adminApi.getBranches()
      ]);
      setDepartments(deptsRes.data?.data || deptsRes.data || []);
      setAllBranches(branchesRes.data?.data || branchesRes.data || []);
    } catch (err) {
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const activeDepts = departments.filter(d => d.isActive !== false);
  const deactivatedDepts = departments.filter(d => d.isActive === false);
  const displayed = activeTab === 'active' ? activeDepts : deactivatedDepts;

  const openCreateModal = () => {
    setEditingDept(null);
    setFormName('');
    setError('');
    setShowModal(true);
  };

  const openEditModal = (dept) => {
    setEditingDept(dept);
    setFormName(dept.name);
    setError('');
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditingDept(null);
    setFormName('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formName.trim()) {
      setError('Department name is required');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      if (editingDept) {
        await adminApi.updateDepartment(editingDept.departmentId, formName.trim());
      } else {
        await adminApi.createDepartment(formName.trim());
      }
      closeModal();
      loadData();
    } catch (err) {
      setError(err.response?.data?.error || err.message || 'Failed to save department');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (dept) => {
    if (!confirm(`Deactivate department "${dept.name}"? It can be reactivated later.`)) return;
    try {
      await adminApi.deleteDepartment(dept.departmentId);
      loadData();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to deactivate department.');
    }
  };

  const handleReactivate = async (dept) => {
    try {
      await adminApi.reactivateDepartment(dept.departmentId);
      loadData();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to reactivate department.');
    }
  };

  const deptBranches = infoDept
    ? allBranches.filter(b => b.deptId === infoDept.departmentId)
    : [];

  return (
    <Layout pageTitle="Department Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Departments</h1>
            <p className="text-muted">
              {activeDepts.length} active · {deactivatedDepts.length} deactivated
            </p>
          </div>
          <button className="btn btn-primary" onClick={openCreateModal}>
            + Add Department
          </button>
        </div>

        <div style={{ display: 'flex', gap: '8px', marginBottom: '1rem' }}>
          <button
            className={`btn ${activeTab === 'active' ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setActiveTab('active')}
          >
            Active ({activeDepts.length})
          </button>
          <button
            className={`btn ${activeTab === 'deactivated' ? 'btn-primary' : 'btn-outline'}`}
            onClick={() => setActiveTab('deactivated')}
          >
            Deactivated ({deactivatedDepts.length})
          </button>
        </div>

        <div className="card">
          <div className="table-responsive">
            {loading ? (
              <div className="loading-state"><div className="spinner"></div></div>
            ) : displayed.length === 0 ? (
              <div className="empty-state">
                <p>{activeTab === 'active' ? 'No active departments' : 'No deactivated departments'}</p>
              </div>
            ) : (
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {displayed.map(dept => (
                    <tr key={dept.departmentId}>
                      <td className="text-muted">{dept.departmentId}</td>
                      <td>{dept.name}</td>
                      <td className="text-muted">
                        {dept.createdAt ? new Date(dept.createdAt).toLocaleDateString() : '—'}
                      </td>
                      <td>
                        <div className="table-actions">
                          <button
                            className="btn btn-sm btn-ghost"
                            title="View branches"
                            onClick={() => setInfoDept(dept)}
                          >
                            Info
                          </button>
                          {activeTab === 'active' ? (
                            <>
                              <button
                                className="btn btn-sm btn-secondary"
                                onClick={() => openEditModal(dept)}
                              >
                                🖍
                              </button>
                              <button
                                className="btn btn-sm btn-danger"
                                onClick={() => handleDeactivate(dept)}
                              >
                                Deactivate
                              </button>
                            </>
                          ) : (
                            <button
                              className="btn btn-sm btn-primary"
                              onClick={() => handleReactivate(dept)}
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

        {/* Department Info Modal */}
        {infoDept && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setInfoDept(null)}>
            <div className="modal">
              <div className="modal-header">
                <h3>Branches under {infoDept.name}</h3>
                <button className="modal-close" onClick={() => setInfoDept(null)}>×</button>
              </div>
              <div className="modal-body">
                {deptBranches.length === 0 ? (
                  <p className="text-muted">No branches assigned to this department.</p>
                ) : (
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Branch Name</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {deptBranches.map(b => (
                        <tr key={b.branchId}>
                          <td>{b.name}</td>
                          <td>
                            <span style={{
                              color: b.isActive !== false ? 'var(--color-success, green)' : 'var(--color-error, #c00)',
                              fontWeight: 500
                            }}>
                              {b.isActive !== false ? 'Active' : 'Deactivated'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
              <div className="modal-footer">
                <button className="btn btn-ghost" onClick={() => setInfoDept(null)}>Close</button>
              </div>
            </div>
          </div>
        )}

        {/* Create / Edit Modal */}
        {showModal && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeModal()}>
            <div className="modal">
              <div className="modal-header">
                <h3>{editingDept ? 'Edit Department' : 'Add Department'}</h3>
                <button className="modal-close" onClick={closeModal}>×</button>
              </div>
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  {error && <div className="alert alert-error">{error}</div>}
                  <div className="form-group">
                    <label className="form-label">Department Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      placeholder="e.g. Office of Academic Scholarships"
                      autoFocus
                      required
                    />
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
