import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { apiFetch } from "../api";

export default function Quiz() {
  const { id } = useParams();
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const loadQuiz = () => {
    setResult(null);
    setAnswers({});
    apiFetch(`/api/quiz/${id}`).then(async (response) => {
      if (response.ok) {
        setQuestions(await response.json());
      } else {
        setError(await response.text());
      }
    });
  };

  useEffect(() => {
    loadQuiz();
  }, [id]);

  const handleSelect = (questionId, option) => {
    setAnswers({ ...answers, [questionId]: option });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const response = await apiFetch(`/api/quiz/${id}/submit`, {
      method: "POST",
      body: JSON.stringify(answers),
    });
    if (response.ok) {
      setResult(await response.json());
      window.scrollTo({ top: 0, behavior: "smooth" });
    } else {
      setError(await response.text());
    }
  };

  if (error) {
    return (
      <div>
        <Link to={`/courses/${id}`} className="back-link">← Back to course</Link>
        <p className="alert alert-error">{error}</p>
      </div>
    );
  }

  if (result) {
    return (
      <div>
        <Link to={`/courses/${id}`} className="back-link">← Back to course</Link>
        <div className={`card result-banner ${result.passed ? "result-pass" : "result-fail"}`}>
          <div className="result-score">{result.score} / {result.total}</div>
          <p className="result-percent">{result.percent}%</p>
          <h2>{result.passed ? "Congratulations, you passed! 🎉" : "Not passed this time"}</h2>
          <p>Pass mark is 60%.</p>
          <div className="card-actions result-actions">
            <button className="btn btn-primary" onClick={loadQuiz}>Retake Quiz</button>
            <Link className="btn btn-secondary" to={`/courses/${id}`}>Back to course</Link>
          </div>
        </div>

        <div className="card">
          <h2>Review</h2>
          {result.review.map((item, index) => (
            <div
              key={item.questionId}
              className={`review-item ${item.correct ? "review-correct" : "review-wrong"}`}
            >
              <p className="question-title">{index + 1}. {item.questionText}</p>
              <p>Your answer: <strong>{item.yourAnswer || "Not answered"}</strong></p>
              {!item.correct && <p>Correct answer: <strong>{item.correctAnswer}</strong></p>}
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div>
      <Link to={`/courses/${id}`} className="back-link">← Back to course</Link>
      <h1 className="page-title">Quiz</h1>
      {questions.length === 0 ? (
        <div className="card"><p className="empty">No questions have been added yet.</p></div>
      ) : (
        <form onSubmit={handleSubmit}>
          {questions.map((q, index) => (
            <div className="card" key={q.id}>
              <p className="question-title">{index + 1}. {q.questionText}</p>
              {[["A", q.optionA], ["B", q.optionB], ["C", q.optionC], ["D", q.optionD]].map(
                ([letter, text]) => (
                  <label
                    key={letter}
                    className={`option-label ${answers[q.id] === letter ? "option-selected" : ""}`}
                  >
                    <input
                      type="radio"
                      name={`question-${q.id}`}
                      checked={answers[q.id] === letter}
                      onChange={() => handleSelect(q.id, letter)}
                    />
                    <span>{letter}. {text}</span>
                  </label>
                )
              )}
            </div>
          ))}
          <button
            type="submit"
            className="btn btn-primary"
            disabled={Object.keys(answers).length !== questions.length}
          >
            Submit Quiz
          </button>
        </form>
      )}
    </div>
  );
}