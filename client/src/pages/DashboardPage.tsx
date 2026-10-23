import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, getApiError } from "../api/client";
import { Badge } from "../components/Badge";
import { EmptyState, Spinner } from "../components/Spinner";
import type { DashboardStats } from "../types";
import { formatDate } from "../utils/format";

export function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get<DashboardStats>("/api/dashboard/stats")
      .then((res) => setStats(res.data))
      .catch((err) => setError(getApiError(err).message));
  }, []);

  if (!stats && !error) return <Spinner />;
  if (error) return <p className="text-rose-600">{error}</p>;
  if (!stats) return null;

  const cards = [
    ["Projects", stats.totalProjects],
    ["Active projects", stats.activeProjects],
    ["Tasks", stats.totalTasks],
    ["Completed", stats.completedTasks],
    ["Pending", stats.pendingTasks],
    ["Overdue", stats.overdueTasks],
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Dashboard</h1>
        <p className="text-sm text-slate-500">Role-aware totals for projects you can access.</p>
      </div>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-6">
        {cards.map(([label, value]) => (
          <div key={String(label)} className="rounded-2xl bg-white p-4 shadow-sm">
            <p className="text-sm text-slate-500">{label}</p>
            <p className="mt-2 text-2xl font-semibold">{value}</p>
          </div>
        ))}
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <section className="rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="mb-4 font-semibold">Recent projects</h2>
          {stats.recentProjects.length === 0 ? (
            <EmptyState title="No projects yet" body="Create a project to get started." />
          ) : (
            <div className="space-y-3">
              {stats.recentProjects.map((project) => (
                <Link key={project.id} to={`/projects/${project.id}`} className="block rounded-xl border p-3 hover:bg-slate-50">
                  <div className="flex items-center justify-between">
                    <p className="font-medium">{project.name}</p>
                    <Badge value={project.status} />
                  </div>
                  <p className="mt-1 text-sm text-slate-500">{project.progressPercent}% complete</p>
                </Link>
              ))}
            </div>
          )}
        </section>
        <section className="rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="mb-4 font-semibold">Recent tasks</h2>
          {stats.recentTasks.length === 0 ? (
            <EmptyState title="No tasks yet" body="Tasks from your projects will appear here." />
          ) : (
            <div className="space-y-3">
              {stats.recentTasks.map((task) => (
                <Link key={task.id} to={`/tasks/${task.id}`} className="block rounded-xl border p-3 hover:bg-slate-50">
                  <div className="flex items-center justify-between">
                    <p className="font-medium">{task.title}</p>
                    <Badge value={task.status} />
                  </div>
                  <p className="mt-1 text-sm text-slate-500">
                    {task.projectName} · due {formatDate(task.dueDate)}
                  </p>
                </Link>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
