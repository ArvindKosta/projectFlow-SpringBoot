import { FormEvent, useEffect, useState } from "react";
import { api, getApiError } from "../api/client";
import { Badge } from "../components/Badge";
import { Spinner } from "../components/Spinner";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import type { GlobalRole, PageResponse, User } from "../types";

export function UsersPage() {
  const { user } = useAuth();
  const toast = useToast();
  const [data, setData] = useState<PageResponse<User> | null>(null);
  const [q, setQ] = useState("");
  const [form, setForm] = useState({ name: "", email: "", password: "", globalRole: "TEAM_MEMBER" as GlobalRole });

  const load = async () => {
    const { data } = await api.get<PageResponse<User>>("/api/users", { params: { q, size: 20 } });
    setData(data);
  };

  useEffect(() => {
    load().catch((err) => toast.push(getApiError(err).message, "error"));
  }, []);

  if (!data) return <Spinner />;
  const isAdmin = user?.globalRole === "ADMIN";

  const create = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api.post("/api/users", { ...form, active: true });
      toast.push("User created", "success");
      setForm({ name: "", email: "", password: "", globalRole: "TEAM_MEMBER" });
      await load();
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Users</h1>
      <form className="flex gap-2" onSubmit={(e) => { e.preventDefault(); load().catch((err) => toast.push(getApiError(err).message, "error")); }}>
        <input placeholder="Search name or email" value={q} onChange={(e) => setQ(e.target.value)} />
        <button className="rounded-lg bg-slate-900 px-4 text-white">Search</button>
      </form>
      {isAdmin && (
        <form className="grid gap-2 rounded-2xl bg-white p-5 shadow-sm md:grid-cols-5" onSubmit={create}>
          <input placeholder="Name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <input placeholder="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          <input placeholder="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required minLength={8} />
          <select value={form.globalRole} onChange={(e) => setForm({ ...form, globalRole: e.target.value as GlobalRole })}>
            {["ADMIN", "PROJECT_MANAGER", "TEAM_MEMBER", "VIEWER"].map((r) => <option key={r}>{r}</option>)}
          </select>
          <button className="rounded-lg bg-brand-600 text-white">Create user</button>
        </form>
      )}
      <div className="overflow-x-auto rounded-2xl bg-white p-5 shadow-sm">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b text-slate-500">
              <th className="py-2">Name</th><th>Email</th><th>Role</th><th>Status</th><th></th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((item) => (
              <tr key={item.id} className="border-b">
                <td className="py-3">{item.name}</td>
                <td>{item.email}</td>
                <td><Badge value={item.globalRole} /></td>
                <td>{item.active ? "Active" : "Inactive"}</td>
                <td>
                  {isAdmin && (
                    <button
                      className="text-brand-600"
                      onClick={async () => {
                        try {
                          await api.patch(`/api/users/${item.id}`, { active: !item.active });
                          await load();
                        } catch (err) {
                          toast.push(getApiError(err).message, "error");
                        }
                      }}
                    >
                      {item.active ? "Deactivate" : "Activate"}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
