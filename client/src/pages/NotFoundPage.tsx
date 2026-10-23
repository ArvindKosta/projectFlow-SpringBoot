import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <h1 className="text-3xl font-semibold">Page not found</h1>
      <p className="mt-2 text-slate-500">The page you requested does not exist.</p>
      <Link className="mt-4 text-brand-600" to="/">Return to dashboard</Link>
    </div>
  );
}
