export type GlobalRole = "ADMIN" | "PROJECT_MANAGER" | "TEAM_MEMBER" | "VIEWER";
export type ProjectRole = "MANAGER" | "MEMBER" | "VIEWER";
export type ProjectStatus = "PLANNING" | "ACTIVE" | "ON_HOLD" | "COMPLETED" | "ARCHIVED";
export type TaskStatus = "TODO" | "IN_PROGRESS" | "BLOCKED" | "DONE";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";

export interface User {
  id: number;
  name: string;
  email: string;
  globalRole: GlobalRole;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  status: ProjectStatus;
  startDate?: string | null;
  dueDate?: string | null;
  owner: User;
  createdAt: string;
  updatedAt: string;
  taskCount: number;
  completedTaskCount: number;
  progressPercent: number;
}

export interface Member {
  id: number;
  user: User;
  projectRole: ProjectRole;
  joinedAt: string;
}

export interface TaskItem {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate?: string | null;
  projectId: number;
  projectName: string;
  assignee?: User | null;
  createdBy: User;
  version: number;
  overdue: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CommentItem {
  id: number;
  content: string;
  taskId: number;
  author: User;
  createdAt: string;
  updatedAt: string;
}

export interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  overdueTasks: number;
  recentProjects: Project[];
  recentTasks: TaskItem[];
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
}
