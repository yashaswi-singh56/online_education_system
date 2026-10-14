import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { apiFetch } from "../api";
import { useAuth } from "../AuthContext";

export default function Dashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);

  const load = () => {
    apiFetch("/api/dashboard")
      .then((response) => response.json())
      .then((result) => setData(result));
  };

  useEffect(() => {
    load();
  }, []);

  const handleUnenroll = (courseId) => {
    apiFetch(`/api/enrollments/${courseId}`, { method: "DELETE" }).then(() => load());
  };

  if (!data) {
    return <p>Loading...</p>;
  }

  return (
    <div>
      <h1 className="page-title">Welcome, {user.name}!</h1>

      <div className="stats-grid">
        <div className="card stat">
          <span className="stat-number">{data.enrolledCount}</span>
          <span>Enrolled Courses</span>
        </div>
        <div className="card stat">
          <span className="stat-number">{data.completedCount}</span>
          <span>Completed</span>
        </div>
        <div className="card stat">
          <span className="stat-number">{data.averageProgress}%</span>
          <span>Average Progress</span>
        </div>
      </div>

      <h2 className="section-title">My Courses</h2>
      {data.courses.length === 0 ? (
        <div className="card">
          <p className="empty">
            You have not enrolled in any course yet. <Link to="/courses">Browse courses</Link>
          </p>
        </div>
      ) : (
        <div className="course-grid">
          {data.courses.map((course) => (
            <div className="card course-card" key={course.courseId}>
              <span className="badge">{course.category}</span>
              <h3>{course.title}</h3>
              <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${course.percent}%` }}></div>
              </div>
              <p className="progress-text">
                {course.completedLessons}/{course.totalLessons} lessons · {course.percent}%
              </p>
              <div className="card-actions">
                <Link className="btn btn-primary btn-small" to={`/courses/${course.courseId}`}>
                  Continue
                </Link>
                <button className="btn btn-delete btn-small" onClick={() => handleUnenroll(course.courseId)}>
                  Unenroll
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}