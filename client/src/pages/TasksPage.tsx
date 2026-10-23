import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, getApiError } from "../api/client";
import { Badge } from "../components/Badge";
import { EmptyState, Spinner } from "../components/Spinner";
import { useToast } from "../context/ToastContext";
import type { PageResponse, Project, TaskItem } from "../types";

export function TasksPage() {
  const toast = useToast();
  const [tasks, setTasks] = useState<TaskItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await api.get<PageResponse<Project>>("/api/projects", { params: { size: 50 } });
        const pages = await Promise.all(
          data.content.map((project) => api.get<PageResponse<TaskItem>>(`/api/projects/${project.id}/tasks`, { params: { size: 20 } })),
        );
        setTasks(pages.flatMap((page) => page.data.content));
      } catch (err) {
        toast.push(getApiError(err).message, "error");
      } finally {
        setLoading(false);
      }
    })();
  }, [toast]);

  if (loading) return <Spinner />;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Tasks</h1>
      {tasks.length === 0 ? (
        <EmptyState title="No tasks" body="Tasks from your projects will show here." />
      ) : (
        <div className="overflow-x-auto rounded-2xl bg-white p-5 shadow-sm">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b text-slate-500">
                <th className="py-2">Title</th><th>Project</th><th>Status</th><th>Priority</th><th></th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr key={task.id} className="border-b">
                  <td className="py-3 font-medium">{task.title} {task.overdue && <span className="text-rose-600">overdue</span>}</td>
                  <td>{task.projectName}</td>
                  <td><Badge value={task.status} /></td>
                  <td><Badge value={task.priority} /></td>
                  <td><Link className="text-brand-600" to={`/tasks/${task.id}`}>Open</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
