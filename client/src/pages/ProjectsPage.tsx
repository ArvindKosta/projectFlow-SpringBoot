import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, getApiError } from "../api/client";
import { Badge } from "../components/Badge";
import { EmptyState, Spinner } from "../components/Spinner";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import type { PageResponse, Project, ProjectStatus } from "../types";

export function ProjectsPage() {
  const { user } = useAuth();
  const toast = useToast();
  const [data, setData] = useState<PageResponse<Project> | null>(null);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(true);
  const canCreate = user?.globalRole === "ADMIN" || user?.globalRole === "PROJECT_MANAGER";
  const [form, setForm] = useState({ name: "", description: "", status: "ACTIVE" as ProjectStatus, dueDate: "" });

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await api.get<PageResponse<Project>>("/api/projects", { params: { q, status: status || undefined } });
      setData(data);
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const create = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api.post("/api/projects", { ...form, dueDate: form.dueDate || null });
      toast.push("Project created", "success");
      setForm({ name: "", description: "", status: "ACTIVE", dueDate: "" });
      await load();
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold">Projects</h1>
          <p className="text-sm text-slate-500">Search, filter, and open a project workspace.</p>
        </div>
        <form className="flex gap-2" onSubmit={(e) => { e.preventDefault(); void load(); }}>
          <input placeholder="Search" value={q} onChange={(e) => setQ(e.target.value)} />
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">All statuses</option>
            {["PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"].map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
          <button className="rounded-lg bg-slate-900 px-4 py-2 text-sm text-white">Filter</button>
        </form>
      </div>
      {canCreate && (
        <form className="grid gap-3 rounded-2xl bg-white p-5 shadow-sm md:grid-cols-4" onSubmit={create}>
          <input placeholder="Project name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <input placeholder="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
          <button className="rounded-lg bg-brand-600 text-white">Create project</button>
        </form>
      )}
      {loading ? (
        <Spinner />
      ) : !data || data.content.length === 0 ? (
        <EmptyState title="No projects found" body="Adjust filters or create a new project." />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {data.content.map((project) => (
            <Link key={project.id} to={`/projects/${project.id}`} className="rounded-2xl bg-white p-5 shadow-sm hover:shadow-md">
              <div className="flex items-start justify-between">
                <h3 className="font-semibold">{project.name}</h3>
                <Badge value={project.status} />
              </div>
              <p className="mt-2 line-clamp-2 text-sm text-slate-500">{project.description || "No description"}</p>
              <div className="mt-4 h-2 rounded-full bg-slate-100">
                <div className="h-2 rounded-full bg-brand-600" style={{ width: `${project.progressPercent}%` }} />
              </div>
              <p className="mt-2 text-xs text-slate-500">
                {project.completedTaskCount}/{project.taskCount} tasks · {project.progressPercent}%
              </p>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
