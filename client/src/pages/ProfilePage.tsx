import { FormEvent, useState } from "react";
import { api, getApiError } from "../api/client";
import { Badge } from "../components/Badge";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import type { User } from "../types";

export function ProfilePage() {
  const { user, setUser, logout } = useAuth();
  const toast = useToast();
  const [name, setName] = useState(user?.name || "");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  if (!user) return null;

  const save = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const { data } = await api.patch<User>("/api/auth/me", {
        name,
        currentPassword: currentPassword || undefined,
        newPassword: newPassword || undefined,
      });
      setUser(data);
      setCurrentPassword("");
      setNewPassword("");
      toast.push("Profile updated", "success");
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  return (
    <div className="max-w-xl space-y-6">
      <h1 className="text-2xl font-semibold">Profile</h1>
      <div className="rounded-2xl bg-white p-5 shadow-sm">
        <p className="text-sm text-slate-500">{user.email}</p>
        <div className="mt-2"><Badge value={user.globalRole} /></div>
      </div>
      <form className="space-y-3 rounded-2xl bg-white p-5 shadow-sm" onSubmit={save}>
        <div>
          <label>Name</label>
          <input value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div>
          <label>Current password</label>
          <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
        </div>
        <div>
          <label>New password</label>
          <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} minLength={8} />
        </div>
        <button className="rounded-lg bg-brand-600 px-4 py-2 text-white">Save</button>
      </form>
      <button className="text-sm text-rose-600" onClick={() => logout()}>Sign out</button>
    </div>
  );
}
