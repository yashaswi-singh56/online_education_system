import { useState } from "react";
import { apiFetch } from "../api";

export default function AdminLessons({ courses }) {
  const [courseId, setCourseId] = useState("");
  const [lessons, setLessons] = useState([]);
  const [title, setTitle] = useState("");

  const loadLessons = (id) => {
    if (!id) {
      setLessons([]);
      return;
    }
    apiFetch(`/api/courses/${id}/lessons`)
      .then((response) => response.json())
      .then((data) => setLessons(data));
  };

  const handleSelect = (event) => {
    setCourseId(event.target.value);
    loadLessons(event.target.value);
  };

  const handleAdd = (event) => {
    event.preventDefault();
    apiFetch(`/api/courses/${courseId}/lessons`, {
      method: "POST",
      body: JSON.stringify({ title }),
    }).then(() => {
      setTitle("");
      loadLessons(courseId);
    });
  };

  const handleDelete = (lessonId) => {
    apiFetch(`/api/lessons/${lessonId}`, { method: "DELETE" }).then(() => loadLessons(courseId));
  };

  return (
    <section className="card">
      <h2>Manage Lessons</h2>
      <select value={courseId} onChange={handleSelect}>
        <option value="">Select a course</option>
        {courses.map((course) => (
          <option key={course.id} value={course.id}>{course.title}</option>
        ))}
      </select>

      {courseId && (
        <>
          <form className="search-bar" onSubmit={handleAdd}>
            <input
              placeholder="New lesson title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
            <button type="submit" className="btn btn-primary">Add Lesson</button>
          </form>

          {lessons.length === 0 ? (
            <p className="empty">No lessons yet.</p>
          ) : (
            <ul className="lesson-list">
              {lessons.map((lesson) => (
                <li className="lesson-item" key={lesson.id}>
                  <span>{lesson.title}</span>
                  <button className="btn btn-delete btn-small" onClick={() => handleDelete(lesson.id)}>
                    Delete
                  </button>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </section>
  );
}