import { cn } from "../utils/format";

const styles: Record<string, string> = {
  PLANNING: "bg-slate-100 text-slate-700",
  ACTIVE: "bg-emerald-100 text-emerald-700",
  ON_HOLD: "bg-amber-100 text-amber-700",
  COMPLETED: "bg-blue-100 text-blue-700",
  ARCHIVED: "bg-slate-200 text-slate-500",
  TODO: "bg-slate-100 text-slate-700",
  IN_PROGRESS: "bg-indigo-100 text-indigo-700",
  BLOCKED: "bg-rose-100 text-rose-700",
  DONE: "bg-emerald-100 text-emerald-700",
  LOW: "bg-slate-100 text-slate-600",
  MEDIUM: "bg-sky-100 text-sky-700",
  HIGH: "bg-orange-100 text-orange-700",
  URGENT: "bg-rose-100 text-rose-700",
  ADMIN: "bg-violet-100 text-violet-700",
  PROJECT_MANAGER: "bg-blue-100 text-blue-700",
  TEAM_MEMBER: "bg-teal-100 text-teal-700",
  VIEWER: "bg-slate-100 text-slate-600",
  MANAGER: "bg-blue-100 text-blue-700",
  MEMBER: "bg-teal-100 text-teal-700",
};

export function Badge({ value }: { value: string }) {
  return (
    <span className={cn("inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold", styles[value] || "bg-slate-100")}>
      {value.replaceAll("_", " ")}
    </span>
  );
}
