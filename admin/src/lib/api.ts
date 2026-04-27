const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"

function getToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("access_token")
}

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken()
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (res.status === 401) {
    localStorage.removeItem("access_token")
    window.location.href = "/login"
    throw new Error("Unauthorized")
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }))
    throw new Error(err.message ?? "Request failed")
  }

  if (res.status === 204) return undefined as T
  return res.json()
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PATCH", body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
}

// ── Types ─────────────────────────────────────────────────────────────────────

export interface FoodCategory {
  id: number
  name: string
  nameEn: string | null
  icon: string | null
  sortOrder: number
}

export interface FoodItem {
  id: number
  name: string
  nameEn: string | null
  categoryId: number | null
  categoryName: string | null
  caloriesPer100g: number
  proteinPer100g: number
  carbsPer100g: number
  fatPer100g: number
  fiberPer100g: number | null
  defaultServingG: number
  servingDescription: string | null
  barcode: string | null
  source: string
  isVerified: boolean
  isActive: boolean
  createdAt: string
}

export interface FoodPage {
  content: FoodItem[]
  page: { totalPages: number; number: number; totalElements: number }
}

export interface AdminSubscription {
  subscriptionId: number
  userId: number
  userName: string
  userEmail: string | null
  planName: string
  status: string
  platform: string
  currentPeriodEnd: string | null
  createdAt: string
}

export interface WhitelistEntry {
  id: number
  userId: number
  userName: string
  userEmail: string | null
  note: string | null
  addedByName: string
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  page: { totalPages: number; number: number; totalElements: number }
}

export const subscriptionApi = {
  list: (page: number, status?: string, platform?: string) => {
    const params = new URLSearchParams({ page: String(page), size: "20", sort: "createdAt,desc" })
    if (status) params.set("status", status)
    if (platform) params.set("platform", platform)
    return request<PageResponse<AdminSubscription>>(`/api/admin/subscriptions?${params}`)
  },
}

export const whitelistApi = {
  list: (page: number) =>
    request<PageResponse<WhitelistEntry>>(`/api/admin/whitelist?page=${page}&size=20`),
  add: (userId: number, note?: string) =>
    request<WhitelistEntry>("/api/admin/whitelist", {
      method: "POST",
      body: JSON.stringify({ userId, note: note || null }),
    }),
  remove: (userId: number) =>
    request<void>(`/api/admin/whitelist/${userId}`, { method: "DELETE" }),
}

export const userSearchApi = {
  search: (email: string) =>
    request<PageResponse<{ id: number; name: string; email: string | null; role: string }>>(
      `/api/admin/users?page=0&size=5&email=${encodeURIComponent(email)}`
    ),
}
