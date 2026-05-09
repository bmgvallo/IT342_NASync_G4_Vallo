import { useEffect, useState } from 'react';
import Layout from '../../shared/components/Layout';
import { adminApi } from '../../shared/api';
import '../../shared/styles/admin.css';

export default function DepartmentManagement() {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingDept, setEditingDept] = useState(null);
  const [formName, setFormName] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadDepartments = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getDepartments();
      setDepartments(res.data?.data || res.data || []);
    } catch (err) {
      console.error('Error loading departments:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDepartments();
  }, []);

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
      loadDepartments();
    } catch (err) {
      const msg = err.response?.data?.error || err.message || 'Failed to save department';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (dept) => {
    if (!confirm(`Delete department "${dept.name}"? This may affect related branches and users.`)) {
      return;
    }

    try {
      await adminApi.deleteDepartment(dept.departmentId);
      loadDepartments();
    } catch (err) {
      alert('Failed to delete department. It may have associated branches or users.');
    }
  };

  return (
    <Layout pageTitle="Department Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Departments</h1>
            <p className="text-muted">{departments.length} departments</p>
          </div>
          <button className="btn btn-primary" onClick={openCreateModal}>
            + Add Department
          </button>
        </div>

        <div className="card">
          <div className="table-responsive">
            {loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
              </div>
            ) : departments.length === 0 ? (
              <div className="empty-state">
                <p>No departments yet</p>
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
                  {departments.map(dept => (
                    <tr key={dept.departmentId}>
                      <td className="text-muted">{dept.departmentId}</td>
                      <td>{dept.name}</td>
                      <td className="text-muted">
                        {dept.createdAt ? new Date(dept.createdAt).toLocaleDateString() : '—'}
                      </td>
                      <td>
                        <div className="table-actions">
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => openEditModal(dept)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-sm btn-danger"
                            onClick={() => handleDelete(dept)}
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

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