import axios, { AxiosError } from "axios";
import type { ApiError } from "../types";

const TOKEN_KEY = "pf_token";

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null) {
  if (token) sessionStorage.setItem(TOKEN_KEY, token);
  else sessionStorage.removeItem(TOKEN_KEY);
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "",
  withCredentials: true,
});

api.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status;
    const url = String(error.config?.url || "");
    const authProbe = url.includes("/api/auth/login") || url.includes("/api/auth/register") || url.includes("/api/auth/me");
    if (status === 401 && !authProbe && typeof window !== "undefined" && window.location.pathname !== "/login") {
      setStoredToken(null);
      window.location.assign("/login");
    }
    return Promise.reject(error);
  },
);

export function getApiError(error: unknown): ApiError {
  const axiosError = error as AxiosError<ApiError>;
  if (axiosError.response?.data?.message) {
    return axiosError.response.data;
  }
  return {
    timestamp: new Date().toISOString(),
    status: axiosError.response?.status ?? 0,
    error: "NETWORK",
    code: "NETWORK",
    message: "Unable to reach the server. Confirm the API is running.",
    path: "",
  };
}

export function isConflict(error: unknown) {
  return getApiError(error).code === "STALE_TASK_VERSION" || getApiError(error).status === 409;
}
