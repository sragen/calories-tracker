"use client"

import { useEffect, useState, useCallback } from "react"
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
import { MoreHorizontal } from "lucide-react"

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

  const loadUsers = useCallback(async (p: number) => {
    setLoading(true)
    try {
      const res = await api.get<PageResponse>(`/api/admin/users?page=${p}&size=20&sort=createdAt,desc`)
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load users")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadUsers(page) }, [page, loadUsers])

  async function updateStatus(id: number, status: string) {
    try {
      await api.patch(`/api/admin/users/${id}/status`, { status })
      toast.success(`User ${status.toLowerCase()}`)
      loadUsers(page)
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
