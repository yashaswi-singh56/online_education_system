import { useState, useEffect } from "react";
import { apiFetch } from "../api";
import AdminLessons from "../components/AdminLessons";
import AdminQuestions from "../components/AdminQuestions";

const emptyForm = { title: "", description: "", category: "" };

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [courses, setCourses] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);

  const loadUsers = () => {
    apiFetch("/api/admin/users")
      .then((response) => response.json())
      .then((data) => setUsers(data));
  };

  const loadCourses = () => {
    apiFetch("/api/courses")
      .then((response) => response.json())
      .then((data) => setCourses(data));
  };

  useEffect(() => {
    loadUsers();
    loadCourses();
  }, []);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const path = editingId ? `/api/courses/${editingId}` : "/api/courses";
    apiFetch(path, {
      method: editingId ? "PUT" : "POST",
      body: JSON.stringify(form),
    }).then(() => {
      resetForm();
      loadCourses();
    });
  };

  const handleEditCourse = (course) => {
    setEditingId(course.id);
    setForm({
      title: course.title,
      description: course.description,
      category: course.category,
    });
  };

  const handleDeleteCourse = (id) => {
    if (window.confirm("Delete this course? Enrollments will also be removed.")) {
      apiFetch(`/api/courses/${id}`, { method: "DELETE" }).then(() => loadCourses());
    }
  };

  const handleDeleteUser = (id) => {
    if (window.confirm("Delete this student?")) {
      apiFetch(`/api/admin/users/${id}`, { method: "DELETE" }).then(() => loadUsers());
    }
  };

  return (
    <div>
      <h1 className="page-title">Admin Panel</h1>

      <section className="card">
        <h2>{editingId ? "Edit Course" : "Add New Course"}</h2>
        <form className="form" onSubmit={handleSubmit}>
          <input
            name="title"
            placeholder="Course title"
            value={form.title}
            onChange={handleChange}
            required
          />
          <input
            name="category"
            placeholder="Category"
            value={form.category}
            onChange={handleChange}
            required
          />
          <input
            name="description"
            placeholder="Description"
            value={form.description}
            onChange={handleChange}
            required
          />
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editingId ? "Update Course" : "Add Course"}
            </button>
            {editingId && (
              <button type="button" className="btn btn-secondary" onClick={resetForm}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="card">
        <h2>Courses ({courses.length})</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Category</th>
                <th className="actions-col">Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.map((course) => (
                <tr key={course.id}>
                  <td>{course.id}</td>
                  <td>{course.title}</td>
                  <td><span className="badge">{course.category}</span></td>
                  <td className="actions-col">
                    <button className="btn btn-edit" onClick={() => handleEditCourse(course)}>
                      Edit
                    </button>
                    <button className="btn btn-delete" onClick={() => handleDeleteCourse(course.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
            <AdminQuestions courses={courses} />
      <AdminLessons courses={courses} />
      <section className="card">
        <h2>Users ({users.length})</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th className="actions-col">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td><span className="badge">{u.role}</span></td>
                  <td className="actions-col">
                    {u.role !== "ADMIN" && (
                      <button className="btn btn-delete" onClick={() => handleDeleteUser(u.id)}>
                        Delete
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}