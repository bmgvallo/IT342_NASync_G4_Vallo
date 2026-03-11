import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { adminApi } from '../../api';
import '../../styles/admin.css';

const DEFAULT_FORM = {
  name: '',
  deptId: ''
};

export default function BranchManagement() {
  const [branches, setBranches] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingBranch, setEditingBranch] = useState(null);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

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

  useEffect(() => {
    loadData();
  }, []);

  const openCreateModal = () => {
    setEditingBranch(null);
    setForm(DEFAULT_FORM);
    setError('');
    setShowModal(true);
  };

  const openEditModal = (branch) => {
    setEditingBranch(branch);
    setForm({
      name: branch.name,
      deptId: String(branch.deptId || branch.departmentId || '')
    });
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
      const msg = err.response?.data?.error || err.message || 'Failed to save branch';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (branch) => {
    if (!confirm(`Delete branch "${branch.name}"? This may affect associated users.`)) {
      return;
    }

    try {
      await adminApi.deleteBranch(branch.branchId);
      loadData();
    } catch (err) {
      alert('Failed to delete branch. It may have associated users.');
    }
  };

  return (
    <Layout pageTitle="Branch Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Branches</h1>
            <p className="text-muted">{branches.length} branches</p>
          </div>
          <button className="btn btn-primary" onClick={openCreateModal}>
            + Add Branch
          </button>
        </div>

        <div className="card">
          <div className="table-responsive">
            {loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
              </div>
            ) : branches.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">🌿</div>
                <p>No branches yet</p>
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
                  {branches.map(branch => {
                    const dept = departments.find(d => 
                      d.departmentId === (branch.deptId || branch.departmentId)
                    );
                    
                    return (
                      <tr key={branch.branchId}>
                        <td className="text-muted">{branch.branchId}</td>
                        <td>{branch.name}</td>
                        <td>{dept?.name || '—'}</td>
                        <td className="text-muted">
                          {branch.createdAt ? new Date(branch.createdAt).toLocaleDateString() : '—'}
                        </td>
                        <td>
                          <div className="table-actions">
                            <button
                              className="btn btn-sm btn-secondary"
                              onClick={() => openEditModal(branch)}
                            >
                              Edit
                            </button>
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => handleDelete(branch)}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Modal */}
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
                      {departments.map(dept => (
                        <option key={dept.departmentId} value={dept.departmentId}>
                          {dept.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={closeModal}>
                    Cancel
                  </button>
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