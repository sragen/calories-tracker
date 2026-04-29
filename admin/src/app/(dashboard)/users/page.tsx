"use client"

import { useEffect, useState, useCallback, useRef } from "react"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { MoreHorizontal, Search, X } from "lucide-react"

interface User {
  id: number
  name: string
  email: string | null
  phone: string | null
  role: string
  status: string
  createdAt: string
}

interface PageResponse {
  content: User[]
  page: { totalPages: number; number: number }
}

const roleBadge: Record<string, string> = {
  SUPER_ADMIN: "bg-rose-500/15 text-rose-400 border-rose-500/20",
  ADMIN: "bg-indigo-500/15 text-indigo-400 border-indigo-500/20",
  STAFF: "bg-amber-500/15 text-amber-400 border-amber-500/20",
  USER: "bg-zinc-500/15 text-zinc-400 border-zinc-500/20",
}

const statusBadge: Record<string, string> = {
  ACTIVE: "bg-emerald-500/15 text-emerald-400 border-emerald-500/20",
  INACTIVE: "bg-zinc-500/15 text-zinc-400 border-zinc-500/20",
  SUSPENDED: "bg-rose-500/15 text-rose-400 border-rose-500/20",
}

function ColorBadge({ label, className }: { label: string; className: string }) {
  return (
    <span className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${className}`}>
      {label}
    </span>
  )
}

export default function UsersPage() {
  const [data, setData] = useState<PageResponse | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState("")
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const loadUsers = useCallback(async (p: number, q: string) => {
    setLoading(true)
    try {
      const params = new URLSearchParams({ page: String(p), size: "20", sort: "createdAt,desc" })
      if (q.trim()) params.set("q", q.trim())
      const res = await api.get<PageResponse>(`/api/admin/users?${params}`)
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load users")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadUsers(page, query) }, [page, loadUsers, query])

  function handleSearch(value: string) {
    setQuery(value)
    setPage(0)
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => loadUsers(0, value), 400)
  }

  function clearSearch() {
    setQuery("")
    setPage(0)
    loadUsers(0, "")
  }

  async function updateStatus(id: number, status: string) {
    try {
      await api.patch(`/api/admin/users/${id}/status`, { status })
      toast.success(`User ${status.toLowerCase()}`)
      loadUsers(page, query)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to update")
    }
  }

  const columns: ColumnDef<User>[] = [
    {
      accessorKey: "name",
      header: "Name",
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.name}</p>
          <p className="text-xs text-muted-foreground">{row.original.email ?? row.original.phone}</p>
        </div>
      ),
    },
    {
      accessorKey: "role",
      header: "Role",
      cell: ({ row }) => (
        <ColorBadge
          label={row.original.role}
          className={roleBadge[row.original.role] ?? roleBadge.USER}
        />
      ),
    },
    {
      accessorKey: "status",
      header: "Status",
      cell: ({ row }) => (
        <ColorBadge
          label={row.original.status}
          className={statusBadge[row.original.status] ?? statusBadge.INACTIVE}
        />
      ),
    },
    {
      accessorKey: "createdAt",
      header: "Created",
      cell: ({ row }) => new Date(row.original.createdAt).toLocaleDateString(),
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => {
        const user = row.original
        const isActive = user.status === "ACTIVE"
        return (
          <DropdownMenu>
            <DropdownMenuTrigger className="flex h-8 w-8 items-center justify-center rounded-md border border-transparent hover:border-border hover:bg-muted">
              <MoreHorizontal className="h-4 w-4" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => updateStatus(user.id, isActive ? "INACTIVE" : "ACTIVE")}>
                {isActive ? "Deactivate" : "Activate"}
              </DropdownMenuItem>
              <DropdownMenuItem
                className="text-destructive"
                onClick={() => updateStatus(user.id, "SUSPENDED")}
              >
                Suspend
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">Users</h1>
        <span className="text-sm text-muted-foreground">
          {data?.page.totalPages ? `Page ${page + 1} of ${data.page.totalPages}` : ""}
        </span>
      </div>

      <div className="relative w-full max-w-sm">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
        <input
          type="text"
          value={query}
          onChange={e => handleSearch(e.target.value)}
          placeholder="Search by name or email…"
          className="w-full rounded-lg border border-border bg-card pl-9 pr-9 py-2 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50"
        />
        {query && (
          <button onClick={clearSearch} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        )}
      </div>

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        pageIndex={page}
        pageCount={data?.page.totalPages ?? 1}
        onPageChange={setPage}
        isLoading={loading}
      />
    </div>
  )
}
