import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { adminApi } from '../../api';
import '../../styles/admin.css';

const DEFAULT_FORM = {
  schoolId: '',
  firstName: '',
  lastName: '',
  email: '',
  personalGmail: '',
  deptId: '',
  branchId: '', // Add branch field
  role: 'DEPARTMENT_HEAD'
};

export default function DeptHeadManagement() {
  const [deptHeads, setDeptHeads] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(DEFAULT_FORM);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [search, setSearch] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const [deptHeadsRes, deptsRes, branchesRes] = await Promise.all([
        adminApi.getDepartmentHeads(),
        adminApi.getDepartments(),
        adminApi.getBranches()
      ]);
      
      setDeptHeads(deptHeadsRes.data?.data || deptHeadsRes.data || []);
      setDepartments(deptsRes.data?.data || deptsRes.data || []);
      setBranches(branchesRes.data?.data || branchesRes.data || []);
    } catch (err) {
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredBranches = branches.filter(b => 
    b.deptId === Number(form.deptId) || b.departmentId === Number(form.deptId)
  );

  const filteredDeptHeads = deptHeads.filter(dh => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (
      dh.firstName?.toLowerCase().includes(q) ||
      dh.lastName?.toLowerCase().includes(q) ||
      dh.schoolId?.toLowerCase().includes(q) ||
      dh.email?.toLowerCase().includes(q)
    );
  });

  const openModal = () => {
    setForm(DEFAULT_FORM);
    setError('');
    setSuccess('');
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setForm(DEFAULT_FORM);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: value,
      ...(name === 'deptId' ? { branchId: '' } : {})
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!form.schoolId || !form.firstName || !form.lastName || !form.email || !form.deptId || !form.branchId) {
      setError('Please fill in all required fields.');
      return;
    }

    setSubmitting(true);
    try {
      await adminApi.registerUser({
        schoolId: form.schoolId,
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        personalGmail: form.personalGmail || undefined,
        password: form.schoolId,
        role: 'DEPARTMENT_HEAD',
        deptId: Number(form.deptId),
        branchId: Number(form.branchId), // Include branch ID
      });

      setSuccess(`Department Head ${form.firstName} ${form.lastName} registered successfully.`);
      closeModal();
      loadData();
    } catch (err) {
      const msg = err.response?.data?.error || err.message || 'Registration failed';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
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
    <Layout pageTitle="Department Head Management">
      <div className="admin-page">
        <div className="page-header">
          <div>
            <h1>Department Heads</h1>
            <p className="text-muted">{deptHeads.length} department heads</p>
          </div>
          <button className="btn btn-primary" onClick={openModal}>
            + Register Dept Head
          </button>
        </div>

        {success && (
          <div className="alert alert-success">
            {success}
            <button className="alert-close" onClick={() => setSuccess('')}>×</button>
          </div>
        )}

        <div className="card">
          <div className="card-body">
            <div className="search-bar">
              <input
                type="text"
                className="form-control"
                placeholder="Search by name or school ID..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            <div className="table-responsive">
              {loading ? (
                <div className="loading-state">
                  <div className="spinner"></div>
                </div>
              ) : filteredDeptHeads.length === 0 ? (
                <div className="empty-state">
                  <div className="empty-icon">🏛️</div>
                  <p>No department heads found</p>
                </div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>School ID</th>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Department</th>
                      <th>Branch</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredDeptHeads.map(dh => (
                      <tr key={dh.userId}>
                        <td><code>{dh.schoolId}</code></td>
                        <td>{dh.lastName}, {dh.firstName}</td>
                        <td>{dh.email}</td>
                        <td>{dh.departmentName || dh.deptName || '—'}</td>
                        <td>{dh.branchName || '—'}</td>
                        <td>
                          <span className={`badge ${dh.active ? 'badge-success' : 'badge-danger'}`}>
                            {dh.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <button
                            className={`btn btn-sm ${dh.active ? 'btn-ghost' : 'btn-outline'}`}
                            onClick={() => toggleActive(dh.userId)}
                          >
                            {dh.active ? 'Deactivate' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>

        {/* Register Modal */}
        {showModal && (
          <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && closeModal()}>
            <div className="modal modal-lg">
              <div className="modal-header">
                <h3>Register Department Head</h3>
                <button className="modal-close" onClick={closeModal}>×</button>
              </div>
              
              <form onSubmit={handleSubmit}>
                <div className="modal-body">
                  {error && <div className="alert alert-error">{error}</div>}
                  
                  <div className="alert alert-info">
                    <strong>Default password</strong> will be set to the user's <strong>School ID</strong>.
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">School ID *</label>
                      <input
                        type="text"
                        name="schoolId"
                        className="form-control"
                        value={form.schoolId}
                        onChange={handleChange}
                        placeholder="e.g. 21-0001"
                        required
                      />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">First Name *</label>
                      <input
                        type="text"
                        name="firstName"
                        className="form-control"
                        value={form.firstName}
                        onChange={handleChange}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Last Name *</label>
                      <input
                        type="text"
                        name="lastName"
                        className="form-control"
                        value={form.lastName}
                        onChange={handleChange}
                        required
                      />
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label">CIT Email *</label>
                      <input
                        type="email"
                        name="email"
                        className="form-control"
                        value={form.email}
                        onChange={handleChange}
                        placeholder="name@cit.edu"
                        required
                      />
                    </div>
                    <div className="form-group">
                      <label className="form-label">Personal Gmail</label>
                      <input
                        type="email"
                        name="personalGmail"
                        className="form-control"
                        value={form.personalGmail}
                        onChange={handleChange}
                        placeholder="For Google login"
                      />
                    </div>
                  </div>

                  <div className="form-row">
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
                    <div className="form-group">
                      <label className="form-label">Branch *</label>
                      <select
                        name="branchId"
                        className="form-control"
                        value={form.branchId}
                        onChange={handleChange}
                        disabled={!form.deptId}
                        required
                      >
                        <option value="">Select branch</option>
                        {filteredBranches.map(branch => (
                          <option key={branch.branchId} value={branch.branchId}>
                            {branch.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-ghost" onClick={closeModal}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? 'Registering...' : 'Register Dept Head'}
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