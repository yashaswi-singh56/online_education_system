import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../AuthContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const message = location.state ? location.state.message : "";

  const handleSubmit = async (event) => {
    event.preventDefault();
    const loggedInUser = await login(email, password);
    if (loggedInUser) {
      navigate(loggedInUser.role === "ADMIN" ? "/admin" : "/");
    } else {
      setError("Invalid email or password");
    }
  };

  return (
    <div className="card auth-card">
      <h2>Login</h2>
      {message && <p className="alert alert-success">{message}</p>}
      {error && <p className="alert alert-error">{error}</p>}
      <form className="stack-form" onSubmit={handleSubmit}>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit" className="btn btn-primary">Login</button>
      </form>
      <p className="auth-link">
        New student? <Link to="/register">Create an account</Link>
      </p>
    </div>
  );
}