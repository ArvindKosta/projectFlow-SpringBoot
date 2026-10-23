import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { CheckSquare, FolderKanban, LayoutDashboard, LogOut, Menu, UserRound, Users, X } from "lucide-react";
import { useState } from "react";
import { useAuth } from "../context/AuthContext";

const links = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/projects", label: "Projects", icon: FolderKanban },
  { to: "/tasks", label: "Tasks", icon: CheckSquare },
  { to: "/users", label: "Users", icon: Users, roles: ["ADMIN", "PROJECT_MANAGER"] },
  { to: "/profile", label: "Profile", icon: UserRound },
];

export function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const visible = links.filter((link) => !link.roles || (user && link.roles.includes(user.globalRole)));

  return (
    <div className="min-h-screen bg-slate-50 lg:grid lg:grid-cols-[260px_1fr]">
      <aside className={`fixed inset-y-0 z-30 w-64 bg-slate-900 p-5 text-white lg:static ${open ? "block" : "hidden lg:block"}`}>
        <div className="mb-8 flex items-center justify-between">
          <div>
            <p className="text-lg font-bold">ProjectFlow</p>
            <p className="text-xs text-slate-400">Collaborative delivery</p>
          </div>
          <button className="lg:hidden" onClick={() => setOpen(false)} type="button">
            <X size={18} />
          </button>
        </div>
        <nav className="space-y-1">
          {visible.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === "/"}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm ${isActive ? "bg-white/10 text-white" : "text-slate-300 hover:bg-white/5"}`
              }
            >
              <link.icon size={16} />
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div>
        <header className="flex items-center justify-between border-b bg-white px-4 py-3">
          <button className="rounded-lg border p-2 lg:hidden" onClick={() => setOpen(true)} type="button">
            <Menu size={18} />
          </button>
          <div className="ml-auto flex items-center gap-3 text-sm">
            <div className="text-right">
              <p className="font-medium">{user?.name}</p>
              <p className="text-xs text-slate-500">{user?.globalRole.replaceAll("_", " ")}</p>
            </div>
            <button
              className="rounded-lg border px-3 py-2"
              type="button"
              onClick={async () => {
                await logout();
                navigate("/login");
              }}
            >
              <LogOut size={16} />
            </button>
          </div>
        </header>
        <main className="p-4 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
