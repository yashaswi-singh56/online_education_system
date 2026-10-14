import { useState } from "react";
import { useAuth } from "../AuthContext";
import { apiFetch } from "../api";

export default function Profile() {
  const { user, updateUser } = useAuth();
  const [name, setName] = useState(user.name);
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (event) => {
    event.preventDefault();
    const response = await apiFetch("/api/auth/me", {
      method: "PUT",
      body: JSON.stringify({ name, password }),
    });
    if (response.ok) {
      const data = await response.json();
      updateUser(data, password);
      setPassword("");
      setMessage("Profile updated!");
    } else {
      setMessage("Update failed");
    }
  };

  return (
    <div className="card auth-card">
      <h2>My Profile</h2>
      {message && <p className="alert alert-success">{message}</p>}
      <p><strong>Email:</strong> {user.email}</p>
      <p><strong>Role:</strong> {user.role}</p>
      <form className="stack-form" onSubmit={handleSubmit}>
        <input
          placeholder="Full name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="New password (leave empty to keep current)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit" className="btn btn-primary">Save Changes</button>
      </form>
    </div>
  );
}