interface Props {
  open: boolean;
  title: string;
  body: string;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmDialog({ open, title, body, onCancel, onConfirm }: Props) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <h3 className="text-lg font-semibold">{title}</h3>
        <p className="mt-2 text-sm text-slate-600">{body}</p>
        <div className="mt-6 flex justify-end gap-2">
          <button className="rounded-lg border px-4 py-2 text-sm" onClick={onCancel} type="button">
            Cancel
          </button>
          <button className="rounded-lg bg-rose-600 px-4 py-2 text-sm text-white" onClick={onConfirm} type="button">
            Confirm
          </button>
        </div>
      </div>
    </div>
  );
}
