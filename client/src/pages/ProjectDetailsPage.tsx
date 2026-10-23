import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, getApiError } from "../api/client";
import { Badge } from "../components/Badge";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { EmptyState, Spinner } from "../components/Spinner";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import type { Member, PageResponse, Project, ProjectRole, TaskItem, TaskPriority, User } from "../types";

export function ProjectDetailsPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [project, setProject] = useState<Project | null>(null);
  const [members, setMembers] = useState<Member[]>([]);
  const [tasks, setTasks] = useState<TaskItem[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [confirm, setConfirm] = useState(false);
  const [memberForm, setMemberForm] = useState({ userId: "", projectRole: "MEMBER" });
  const [taskForm, setTaskForm] = useState({ title: "", description: "", priority: "MEDIUM" as TaskPriority, dueDate: "", assigneeId: "" });
  const [taskQuery, setTaskQuery] = useState({ q: "", status: "" });

  const load = async () => {
    const [{ data: p }, { data: m }, { data: t }] = await Promise.all([
      api.get<Project>(`/api/projects/${id}`),
      api.get<Member[]>(`/api/projects/${id}/members`),
      api.get<PageResponse<TaskItem>>(`/api/projects/${id}/tasks`, { params: { q: taskQuery.q || undefined, status: taskQuery.status || undefined } }),
    ]);
    setProject(p);
    setMembers(m);
    setTasks(t.content);
  };

  useEffect(() => {
    load().catch((err) => toast.push(getApiError(err).message, "error"));
  }, [id]);

  useEffect(() => {
    if (user?.globalRole === "ADMIN" || user?.globalRole === "PROJECT_MANAGER") {
      api.get<PageResponse<User>>("/api/users", { params: { size: 100 } }).then((res) => setUsers(res.data.content)).catch(() => undefined);
    }
  }, [user]);

  if (!project) return <Spinner />;

  const canManage = user?.globalRole === "ADMIN" || project.owner.id === user?.id ||
    members.some((m) => m.user.id === user?.id && m.projectRole === "MANAGER");

  const saveProject = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const { data } = await api.patch<Project>(`/api/projects/${project.id}`, project);
      setProject(data);
      toast.push("Project updated", "success");
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  const addMember = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api.post(`/api/projects/${project.id}/members`, {
        userId: Number(memberForm.userId),
        projectRole: memberForm.projectRole,
      });
      toast.push("Member added", "success");
      await load();
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  const createTask = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api.post(`/api/projects/${project.id}/tasks`, {
        ...taskForm,
        assigneeId: taskForm.assigneeId ? Number(taskForm.assigneeId) : null,
        dueDate: taskForm.dueDate || null,
      });
      toast.push("Task created", "success");
      setTaskForm({ title: "", description: "", priority: "MEDIUM", dueDate: "", assigneeId: "" });
      await load();
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <Link to="/projects" className="text-sm text-brand-600">Back to projects</Link>
          <h1 className="text-2xl font-semibold">{project.name}</h1>
        </div>
        {canManage && (
          <button className="rounded-lg bg-rose-600 px-4 py-2 text-sm text-white" onClick={() => setConfirm(true)}>Delete</button>
        )}
      </div>

      <form className="grid gap-3 rounded-2xl bg-white p-5 shadow-sm md:grid-cols-2" onSubmit={saveProject}>
        <div>
          <label>Name</label>
          <input value={project.name} onChange={(e) => setProject({ ...project, name: e.target.value })} disabled={!canManage} />
        </div>
        <div>
          <label>Status</label>
          <select value={project.status} onChange={(e) => setProject({ ...project, status: e.target.value as Project["status"] })} disabled={!canManage}>
            {["PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"].map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
        <div className="md:col-span-2">
          <label>Description</label>
          <textarea value={project.description || ""} onChange={(e) => setProject({ ...project, description: e.target.value })} disabled={!canManage} />
        </div>
        {canManage && <button className="rounded-lg bg-brand-600 px-4 py-2 text-white">Save project</button>}
      </form>

      <section className="rounded-2xl bg-white p-5 shadow-sm">
        <h2 className="mb-4 font-semibold">Members</h2>
        {canManage && (
          <form className="mb-4 grid gap-2 md:grid-cols-3" onSubmit={addMember}>
            <select value={memberForm.userId} onChange={(e) => setMemberForm({ ...memberForm, userId: e.target.value })} required>
              <option value="">Select user</option>
              {users.map((u) => <option key={u.id} value={u.id}>{u.name} ({u.email})</option>)}
            </select>
            <select value={memberForm.projectRole} onChange={(e) => setMemberForm({ ...memberForm, projectRole: e.target.value })}>
              {["MANAGER", "MEMBER", "VIEWER"].map((r) => <option key={r}>{r}</option>)}
            </select>
            <button className="rounded-lg bg-slate-900 text-white">Add member</button>
          </form>
        )}
        <div className="space-y-2">
          {members.map((m) => (
            <div key={m.id} className="flex items-center justify-between rounded-lg border p-3">
              <div>
                <p className="font-medium">{m.user.name}</p>
                <p className="text-xs text-slate-500">{m.user.email}</p>
              </div>
              <div className="flex items-center gap-2">
                {canManage ? (
                  <select
                    value={m.projectRole}
                    onChange={async (e) => {
                      try {
                        await api.patch(`/api/projects/${project.id}/members/${m.id}`, { projectRole: e.target.value as ProjectRole });
                        await load();
                      } catch (err) {
                        toast.push(getApiError(err).message, "error");
                      }
                    }}
                  >
                    {["MANAGER", "MEMBER", "VIEWER"].map((r) => <option key={r}>{r}</option>)}
                  </select>
                ) : (
                  <Badge value={m.projectRole} />
                )}
                {canManage && m.user.id !== project.owner.id && (
                  <button className="text-sm text-rose-600" onClick={async () => {
                    try {
                      await api.delete(`/api/projects/${project.id}/members/${m.id}`);
                      await load();
                    } catch (err) {
                      toast.push(getApiError(err).message, "error");
                    }
                  }}>Remove</button>
                )}
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-2xl bg-white p-5 shadow-sm">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
          <h2 className="font-semibold">Tasks</h2>
          <form className="flex gap-2" onSubmit={(e) => { e.preventDefault(); load().catch((err) => toast.push(getApiError(err).message, "error")); }}>
            <input placeholder="Search tasks" value={taskQuery.q} onChange={(e) => setTaskQuery({ ...taskQuery, q: e.target.value })} />
            <select value={taskQuery.status} onChange={(e) => setTaskQuery({ ...taskQuery, status: e.target.value })}>
              <option value="">All</option>
              {["TODO", "IN_PROGRESS", "BLOCKED", "DONE"].map((s) => <option key={s}>{s}</option>)}
            </select>
            <button className="rounded-lg border px-3">Filter</button>
          </form>
        </div>
        {canManage && (
          <form className="mb-4 grid gap-2 md:grid-cols-5" onSubmit={createTask}>
            <input placeholder="Title" value={taskForm.title} onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })} required />
            <input placeholder="Description" value={taskForm.description} onChange={(e) => setTaskForm({ ...taskForm, description: e.target.value })} />
            <select value={taskForm.priority} onChange={(e) => setTaskForm({ ...taskForm, priority: e.target.value as TaskPriority })}>
              {["LOW", "MEDIUM", "HIGH", "URGENT"].map((p) => <option key={p}>{p}</option>)}
            </select>
            <select value={taskForm.assigneeId} onChange={(e) => setTaskForm({ ...taskForm, assigneeId: e.target.value })}>
              <option value="">Unassigned</option>
              {members.map((m) => <option key={m.user.id} value={m.user.id}>{m.user.name}</option>)}
            </select>
            <button className="rounded-lg bg-brand-600 text-white">Add task</button>
          </form>
        )}
        {tasks.length === 0 ? (
          <EmptyState title="No tasks" body="Create a task or change filters." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b text-slate-500">
                  <th className="py-2">Title</th><th>Status</th><th>Priority</th><th>Assignee</th><th></th>
                </tr>
              </thead>
              <tbody>
                {tasks.map((task) => (
                  <tr key={task.id} className="border-b">
                    <td className="py-3 font-medium">{task.title} {task.overdue && <span className="text-rose-600">overdue</span>}</td>
                    <td><Badge value={task.status} /></td>
                    <td><Badge value={task.priority} /></td>
                    <td>{task.assignee?.name || "Unassigned"}</td>
                    <td><Link className="text-brand-600" to={`/tasks/${task.id}`}>Open</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <ConfirmDialog
        open={confirm}
        title="Delete project?"
        body="This removes the project, its memberships, and its tasks."
        onCancel={() => setConfirm(false)}
        onConfirm={async () => {
          try {
            await api.delete(`/api/projects/${project.id}`);
            navigate("/projects");
          } catch (err) {
            toast.push(getApiError(err).message, "error");
          }
        }}
      />
    </div>
  );
}
