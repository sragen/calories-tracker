"use client"

import { useEffect, useState, useCallback } from "react"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import { api, UserSubscriptionResponse } from "@/lib/api"
import { toast } from "sonner"

const STATUS_CLASSES: Record<string, string> = {
  ACTIVE:    "bg-emerald-500/15 text-emerald-400 border-emerald-500/20",
  PENDING:   "bg-amber-500/15 text-amber-400 border-amber-500/20",
  EXPIRED:   "bg-zinc-500/15 text-zinc-400 border-zinc-500/20",
  CANCELLED: "bg-red-500/15 text-red-400 border-red-500/20",
}

function StatusBadge({ status }: { status: string }) {
  return (
    <span className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${STATUS_CLASSES[status] ?? STATUS_CLASSES.PENDING}`}>
      {status}
    </span>
  )
}

function formatIdr(amount: number) {
  return new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 }).format(amount)
}

const STATUS_FILTERS = ["ALL", "ACTIVE", "PENDING", "EXPIRED", "CANCELLED"] as const

export default function SubscriptionsPage() {
  const [data, setData] = useState<UserSubscriptionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState<string>("ALL")
  const [page, setPage] = useState(0)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.get<UserSubscriptionResponse[]>("/api/admin/subscriptions")
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load subscriptions")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = statusFilter === "ALL" ? data : data.filter(s => s.status === statusFilter)
  const pageSize = 20
  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize))
  const paged = filtered.slice(page * pageSize, (page + 1) * pageSize)

  const columns: ColumnDef<UserSubscriptionResponse>[] = [
    {
      id: "plan",
      header: "Plan",
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.plan.name}</p>
          <p className="text-xs text-muted-foreground">{formatIdr(row.original.plan.priceIdr)}</p>
        </div>
      ),
    },
    {
      id: "status",
      header: "Status",
      cell: ({ row }) => <StatusBadge status={row.original.status} />,
    },
    {
      id: "started",
      header: "Started",
      cell: ({ row }) => row.original.startedAt
        ? new Date(row.original.startedAt).toLocaleDateString("id-ID")
        : "—",
    },
    {
      id: "expires",
      header: "Expires",
      cell: ({ row }) => row.original.expiresAt
        ? new Date(row.original.expiresAt).toLocaleDateString("id-ID")
        : "—",
    },
    {
      id: "payment",
      header: "Payment ID",
      cell: ({ row }) => (
        <span className="text-xs font-mono text-muted-foreground">
          {row.original.paymentId ?? "—"}
        </span>
      ),
    },
    {
      id: "created",
      header: "Created",
      cell: ({ row }) => new Date(row.original.createdAt).toLocaleDateString("id-ID"),
    },
  ]

  const activeCount = data.filter(s => s.status === "ACTIVE").length

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Subscriptions</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage user premium subscriptions
          </p>
        </div>
        {activeCount > 0 && (
          <span className="inline-flex items-center rounded-full bg-emerald-500/15 border border-emerald-500/20 px-3 py-1 text-sm font-medium text-emerald-400">
            {activeCount} active
          </span>
        )}
      </div>

      {/* Status filter tabs */}
      <div className="flex gap-1 flex-wrap">
        {STATUS_FILTERS.map(s => (
          <button
            key={s}
            onClick={() => { setStatusFilter(s); setPage(0) }}
            className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
              statusFilter === s
                ? "bg-primary text-primary-foreground"
                : "text-muted-foreground hover:text-foreground hover:bg-muted"
            }`}
          >
            {s} {s !== "ALL" && `(${data.filter(x => x.status === s).length})`}
          </button>
        ))}
      </div>

      <DataTable
        columns={columns}
        data={paged}
        pageIndex={page}
        pageCount={pageCount}
        onPageChange={setPage}
        isLoading={loading}
      />
    </div>
  )
}
