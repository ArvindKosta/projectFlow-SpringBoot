import { FormEvent, useState } from "react";
import { Navigate } from "react-router-dom";
import { getApiError } from "../api/client";
import { useAuth } from "../context/AuthContext";

export function LoginPage() {
  const { user, login, register } = useAuth();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("admin@projectflow.dev");
  const [password, setPassword] = useState("Admin123!");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      if (mode === "login") await login(email, password);
      else await register(name, email, password);
    } catch (err) {
      setError(getApiError(err).message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-900 p-4">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-3xl bg-white shadow-2xl lg:grid-cols-2">
        <div className="hidden bg-gradient-to-br from-brand-700 to-slate-900 p-10 text-white lg:block">
          <p className="text-sm uppercase tracking-[0.2em] text-blue-200">ProjectFlow</p>
          <h1 className="mt-6 text-4xl font-semibold">Plan work. Assign owners. Ship together.</h1>
          <p className="mt-4 text-blue-100">
            Projects, memberships, tasks, comments, and optimistic locking for concurrent updates.
          </p>
        </div>
        <form className="space-y-4 p-8" onSubmit={onSubmit}>
          <h2 className="text-2xl font-semibold">{mode === "login" ? "Sign in" : "Create an account"}</h2>
          <p className="text-sm text-slate-500">
            Public registration always creates a TEAM_MEMBER. Development demo accounts are documented in the README.
          </p>
          {mode === "register" && (
            <div>
              <label>Name</label>
              <input value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
          )}
          <div>
            <label>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div>
            <label>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} />
          </div>
          {error && <p className="rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}
          <button className="w-full rounded-lg bg-brand-600 py-2.5 text-white disabled:opacity-60" disabled={busy}>
            {busy ? "Please wait..." : mode === "login" ? "Sign in" : "Register"}
          </button>
          <button type="button" className="w-full text-sm text-brand-600" onClick={() => setMode(mode === "login" ? "register" : "login")}>
            {mode === "login" ? "Need an account? Register" : "Already have an account? Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}
