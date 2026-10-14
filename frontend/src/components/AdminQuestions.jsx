import { useState } from "react";
import { apiFetch } from "../api";

const emptyForm = {
  questionText: "",
  optionA: "",
  optionB: "",
  optionC: "",
  optionD: "",
  correctOption: "A",
};

export default function AdminQuestions({ courses }) {
  const [courseId, setCourseId] = useState("");
  const [questions, setQuestions] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");

  const loadQuestions = (id) => {
    if (!id) {
      setQuestions([]);
      return;
    }
    apiFetch(`/api/admin/courses/${id}/questions`)
      .then((response) => response.json())
      .then((data) => setQuestions(data));
  };

  const handleSelect = (event) => {
    setCourseId(event.target.value);
    setError("");
    loadQuestions(event.target.value);
  };

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const handleAdd = async (event) => {
    event.preventDefault();
    const response = await apiFetch(`/api/admin/courses/${courseId}/questions`, {
      method: "POST",
      body: JSON.stringify(form),
    });
    if (response.ok) {
      setForm(emptyForm);
      setError("");
      loadQuestions(courseId);
    } else {
      setError(await response.text());
    }
  };

  const handleDelete = (questionId) => {
    apiFetch(`/api/admin/questions/${questionId}`, { method: "DELETE" }).then(() =>
      loadQuestions(courseId)
    );
  };

  return (
    <section className="card">
      <h2>Manage Quiz Questions</h2>
      <select value={courseId} onChange={handleSelect}>
        <option value="">Select a course</option>
        {courses.map((course) => (
          <option key={course.id} value={course.id}>{course.title}</option>
        ))}
      </select>

      {courseId && (
        <>
          {error && <p className="alert alert-error" style={{ marginTop: 16 }}>{error}</p>}
          <form className="form" style={{ marginTop: 16 }} onSubmit={handleAdd}>
            <input name="questionText" placeholder="Question" value={form.questionText}
              onChange={handleChange} required />
            <input name="optionA" placeholder="Option A" value={form.optionA}
              onChange={handleChange} required />
            <input name="optionB" placeholder="Option B" value={form.optionB}
              onChange={handleChange} required />
            <input name="optionC" placeholder="Option C" value={form.optionC}
              onChange={handleChange} required />
            <input name="optionD" placeholder="Option D" value={form.optionD}
              onChange={handleChange} required />
            <select name="correctOption" value={form.correctOption} onChange={handleChange}>
              <option value="A">Correct: A</option>
              <option value="B">Correct: B</option>
              <option value="C">Correct: C</option>
              <option value="D">Correct: D</option>
            </select>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">Add Question</button>
            </div>
          </form>

          {questions.length === 0 ? (
            <p className="empty">No questions yet.</p>
          ) : (
            <ul className="lesson-list">
              {questions.map((q, index) => (
                <li className="lesson-item" key={q.id}>
                  <span>
                    {index + 1}. {q.questionText} <span className="badge">Answer: {q.correctOption}</span>
                  </span>
                  <button className="btn btn-delete btn-small" onClick={() => handleDelete(q.id)}>
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