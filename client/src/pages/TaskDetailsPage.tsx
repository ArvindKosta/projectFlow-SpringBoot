import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, getApiError, isConflict } from "../api/client";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { Spinner } from "../components/Spinner";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import type { CommentItem, TaskItem } from "../types";
import { formatDateTime } from "../utils/format";

export function TaskDetailsPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [task, setTask] = useState<TaskItem | null>(null);
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [comment, setComment] = useState("");
  const [confirm, setConfirm] = useState(false);
  const [conflict, setConflict] = useState(false);

  const load = async () => {
    const [{ data: t }, { data: c }] = await Promise.all([
      api.get<TaskItem>(`/api/tasks/${id}`),
      api.get<CommentItem[]>(`/api/tasks/${id}/comments`),
    ]);
    setTask(t);
    setComments(c);
    setConflict(false);
  };

  useEffect(() => {
    load().catch((err) => toast.push(getApiError(err).message, "error"));
  }, [id]);

  if (!task || !user) return <Spinner />;

  const save = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const { data } = await api.patch<TaskItem>(`/api/tasks/${task.id}`, {
        version: task.version,
        title: task.title,
        description: task.description,
        status: task.status,
        priority: task.priority,
        dueDate: task.dueDate,
        assigneeId: task.assignee?.id,
      });
      setTask(data);
      toast.push("Task updated", "success");
    } catch (err) {
      if (isConflict(err)) {
        setConflict(true);
        toast.push("This task changed since you loaded it. Reloading the latest version.", "error");
        await load();
      } else {
        toast.push(getApiError(err).message, "error");
      }
    }
  };

  const addComment = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const { data } = await api.post<CommentItem>(`/api/tasks/${task.id}/comments`, { content: comment });
      setComments((current) => [...current, data]);
      setComment("");
    } catch (err) {
      toast.push(getApiError(err).message, "error");
    }
  };

  return (
    <div className="space-y-6">
      <Link to={`/projects/${task.projectId}`} className="text-sm text-brand-600">Back to {task.projectName}</Link>
      {conflict && (
        <div className="rounded-xl bg-amber-50 p-4 text-amber-800">
          A newer version of this task was loaded after a concurrent update. Review the latest fields before saving again.
        </div>
      )}
      <form className="grid gap-3 rounded-2xl bg-white p-5 shadow-sm md:grid-cols-2" onSubmit={save}>
        <div className="md:col-span-2">
          <label>Title</label>
          <input value={task.title} onChange={(e) => setTask({ ...task, title: e.target.value })} />
        </div>
        <div>
          <label>Status</label>
          <select value={task.status} onChange={(e) => setTask({ ...task, status: e.target.value as TaskItem["status"] })}>
            {["TODO", "IN_PROGRESS", "BLOCKED", "DONE"].map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
        <div>
          <label>Priority</label>
          <select value={task.priority} onChange={(e) => setTask({ ...task, priority: e.target.value as TaskItem["priority"] })}>
            {["LOW", "MEDIUM", "HIGH", "URGENT"].map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
        <div className="md:col-span-2">
          <label>Description</label>
          <textarea value={task.description || ""} onChange={(e) => setTask({ ...task, description: e.target.value })} />
        </div>
        <p className="text-sm text-slate-500">Version {task.version} · {task.overdue ? "Overdue" : "On track"}</p>
        <div className="flex justify-end gap-2">
          <button type="button" className="rounded-lg border px-4 py-2" onClick={() => setConfirm(true)}>Delete</button>
          <button className="rounded-lg bg-brand-600 px-4 py-2 text-white">Save task</button>
        </div>
      </form>

      <section className="rounded-2xl bg-white p-5 shadow-sm">
        <h2 className="mb-4 font-semibold">Comments</h2>
        <div className="space-y-3">
          {comments.map((item) => (
            <div key={item.id} className="rounded-xl border p-3">
              <div className="flex items-center justify-between text-sm">
                <p className="font-medium">{item.author.name}</p>
                <p className="text-slate-500">{formatDateTime(item.createdAt)}</p>
              </div>
              <p className="mt-2 text-sm">{item.content}</p>
              {(item.author.id === user.id || user.globalRole === "ADMIN") && (
                <button
                  className="mt-2 text-xs text-rose-600"
                  onClick={async () => {
                    try {
                      await api.delete(`/api/comments/${item.id}`);
                      setComments((current) => current.filter((c) => c.id !== item.id));
                    } catch (err) {
                      toast.push(getApiError(err).message, "error");
                    }
                  }}
                >
                  Delete
                </button>
              )}
            </div>
          ))}
        </div>
        <form className="mt-4 flex gap-2" onSubmit={addComment}>
          <input value={comment} onChange={(e) => setComment(e.target.value)} placeholder="Write a comment" required />
          <button className="rounded-lg bg-slate-900 px-4 text-white">Post</button>
        </form>
      </section>

      <ConfirmDialog
        open={confirm}
        title="Delete task?"
        body="This cannot be undone."
        onCancel={() => setConfirm(false)}
        onConfirm={async () => {
          try {
            await api.delete(`/api/tasks/${task.id}`);
            navigate(`/projects/${task.projectId}`);
          } catch (err) {
            toast.push(getApiError(err).message, "error");
          }
        }}
      />
    </div>
  );
}
