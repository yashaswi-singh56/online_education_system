import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { apiFetch } from "../api";

export default function CourseDetail() {
  const { id } = useParams();
  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [enrolled, setEnrolled] = useState(false);
  const [status, setStatus] = useState(null);
  const [message, setMessage] = useState("");

  const loadLessons = () => {
    apiFetch(`/api/courses/${id}/lessons`)
      .then((response) => response.json())
      .then((data) => setLessons(data));
  };

  const loadStatus = () => {
    apiFetch(`/api/quiz/${id}/status`)
      .then((response) => response.json())
      .then((data) => setStatus(data));
  };

  useEffect(() => {
    apiFetch("/api/courses")
      .then((response) => response.json())
      .then((list) => setCourse(list.find((c) => String(c.id) === id)));
    apiFetch("/api/enrollments/my")
      .then((response) => response.json())
      .then((list) => setEnrolled(list.some((c) => String(c.id) === id)));
    loadLessons();
    loadStatus();
  }, [id]);

  const handleToggle = async (lessonId) => {
    const response = await apiFetch(`/api/lessons/${lessonId}/toggle`, { method: "POST" });
    setMessage(response.ok ? "" : await response.text());
    loadLessons();
    loadStatus();
  };

  const handleCertificate = async () => {
    const response = await apiFetch(`/api/certificate/${id}`);
    if (!response.ok) {
      setMessage(await response.text());
      return;
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `certificate-${course.title}.pdf`;
    link.click();
    URL.revokeObjectURL(url);
  };

  if (!course) {
    return <p>Loading...</p>;
  }

  const doneCount = lessons.filter((lesson) => lesson.completed).length;
  const percent = lessons.length === 0 ? 0 : Math.round((doneCount * 100) / lessons.length);

  return (
    <div>
      <Link to="/courses" className="back-link">← Back to courses</Link>
      <div className="card">
        <span className="badge">{course.category}</span>
        <h1 className="page-title">{course.title}</h1>
        <p>{course.description}</p>

        {enrolled ? (
          <>
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${percent}%` }}></div>
            </div>
            <p className="progress-text">{doneCount}/{lessons.length} lessons · {percent}%</p>
          </>
        ) : (
          <p className="alert alert-error">Enroll in this course to track your progress.</p>
        )}
      </div>

      <div className="card">
        <h2>Lessons</h2>
        {message && <p className="alert alert-error">{message}</p>}
        {lessons.length === 0 ? (
          <p className="empty">No lessons added yet.</p>
        ) : (
          <ul className="lesson-list">
            {lessons.map((lesson) => (
              <li className="lesson-item" key={lesson.id}>
                <input
                  type="checkbox"
                  checked={lesson.completed}
                  disabled={!enrolled}
                  onChange={() => handleToggle(lesson.id)}
                />
                <span className={lesson.completed ? "lesson-done" : ""}>{lesson.title}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      {status && (
        <div className="card">
          <h2>Quiz &amp; Certificate</h2>
          <ul className="checklist">
            <li className={status.lessonsTotal > 0 && status.lessonsDone === status.lessonsTotal ? "check-done" : ""}>
              {status.lessonsTotal > 0 && status.lessonsDone === status.lessonsTotal ? "✓" : "○"} Complete all lessons
              ({status.lessonsDone}/{status.lessonsTotal})
            </li>
            <li className={status.quizPassed ? "check-done" : ""}>
              {status.quizPassed ? "✓" : "○"} Pass the quiz with 60% or more
              {status.bestPercent > 0 && ` (best score: ${status.bestPercent}%)`}
            </li>
          </ul>
          <div className="card-actions">
            {enrolled && status.questionCount > 0 && (
              <Link className="btn btn-primary" to={`/courses/${id}/quiz`}>
                {status.bestPercent > 0 ? "Retake Quiz" : "Take Quiz"}
              </Link>
            )}
            <button
              className="btn btn-secondary"
              disabled={!status.certificateReady}
              onClick={handleCertificate}
            >
              Download Certificate
            </button>
          </div>
          {enrolled && status.questionCount === 0 && (
            <p className="progress-text">The quiz for this course has not been added yet.</p>
          )}
        </div>
      )}
    </div>
  );
}