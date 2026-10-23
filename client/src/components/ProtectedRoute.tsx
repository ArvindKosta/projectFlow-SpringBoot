import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Spinner } from "./Spinner";
import type { GlobalRole } from "../types";

export function ProtectedRoute({ roles }: { roles?: GlobalRole[] }) {
  const { user, loading } = useAuth();
  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Spinner />
      </div>
    );
  }
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.globalRole)) return <Navigate to="/" replace />;
  return <Outlet />;
}
