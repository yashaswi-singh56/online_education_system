import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="navbar">
      <span className="brand">Course Portal</span>
      <div className="nav-links">
        {user ? (
          <>
            <NavLink to="/" end>Dashboard</NavLink>
            <NavLink to="/courses">Courses</NavLink>
            <NavLink to="/profile">Profile</NavLink>
            {user.role === "ADMIN" && <NavLink to="/admin">Admin</NavLink>}
            <span className="user-name">{user.name}</span>
            <button className="btn btn-secondary btn-small" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login">Login</NavLink>
            <NavLink to="/register">Register</NavLink>
          </>
        )}
      </div>
    </nav>
  );
}