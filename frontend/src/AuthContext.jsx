import { createContext, useContext, useState } from "react";
import { API, getAuth, saveAuth, clearAuth } from "./api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const auth = getAuth();
    return auth ? auth.user : null;
  });

  const login = async (email, password) => {
    const response = await fetch(API + "/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!response.ok) {
      return null;
    }
    const data = await response.json();
    saveAuth({ token: data.token, user: data.user });
    setUser(data.user);
    return data.user;
  };

  const logout = () => {
    clearAuth();
    setUser(null);
  };

  const updateUser = (data) => {
    saveAuth({ ...getAuth(), user: data });
    setUser(data);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}