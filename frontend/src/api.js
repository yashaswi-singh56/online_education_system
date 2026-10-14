export const API = "http://localhost:8080";

export function getAuth() {
  const saved = sessionStorage.getItem("auth");
  return saved ? JSON.parse(saved) : null;
}

export function saveAuth(auth) {
  sessionStorage.setItem("auth", JSON.stringify(auth));
}

export function clearAuth() {
  sessionStorage.removeItem("auth");
}

export async function apiFetch(path, options = {}) {
  const auth = getAuth();
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (auth) {
    headers.Authorization = `Bearer ${auth.token}`;
  }

  const response = await fetch(API + path, { ...options, headers });

  // Token expire ho gaya ya galat hai: logout karke login page par bhejo
  if (response.status === 401 && auth) {
    clearAuth();
    window.location.href = "/login";
  }
  return response;
}