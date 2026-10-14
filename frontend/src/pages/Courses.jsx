import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "../api";

export default function Courses() {
  const [courses, setCourses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [enrolledIds, setEnrolledIds] = useState([]);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [message, setMessage] = useState("");

  const loadCourses = () => {
    const params = new URLSearchParams({ search, category });
    apiFetch(`/api/courses?${params}`)
      .then((response) => response.json())
      .then((data) => setCourses(data));
  };

  const loadEnrolled = () => {
    apiFetch("/api/enrollments/my")
      .then((response) => response.json())
      .then((data) => setEnrolledIds(data.map((course) => course.id)));
  };

  useEffect(() => {
    apiFetch("/api/courses/categories")
      .then((response) => response.json())
      .then((data) => setCategories(data));
    loadEnrolled();
  }, []);

  useEffect(() => {
    loadCourses();
  }, [search, category]);

  const handleEnroll = async (courseId) => {
    const response = await apiFetch(`/api/enrollments/${courseId}`, { method: "POST" });
    setMessage(response.ok ? "Enrolled successfully!" : await response.text());
    loadEnrolled();
  };

  return (
    <div>
      <h1 className="page-title">Available Courses</h1>

      <div className="search-bar">
        <input
          placeholder="Search by course name..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>

      {message && <p className="alert alert-success">{message}</p>}

      {courses.length === 0 ? (
        <div className="card"><p className="empty">No courses found.</p></div>
      ) : (
        <div className="course-grid">
          {courses.map((course) => (
            <div className="card course-card" key={course.id}>
              <span className="badge">{course.category}</span>
              <h3>{course.title}</h3>
              <p>{course.description}</p>
              <div className="card-actions">
                {enrolledIds.includes(course.id) ? (
                  <button className="btn btn-secondary btn-small" disabled>Enrolled ✓</button>
                ) : (
                  <button className="btn btn-primary btn-small" onClick={() => handleEnroll(course.id)}>
                    Enroll
                  </button>
                )}
                <Link className="btn btn-secondary btn-small" to={`/courses/${course.id}`}>
                  View lessons
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}