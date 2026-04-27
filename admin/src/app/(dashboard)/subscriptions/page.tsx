"use client"

import { useEffect, useState, useCallback } from "react"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { subscriptionApi, AdminSubscription } from "@/lib/api"
import { toast } from "sonner"
import { ChevronDown } from "lucide-react"

const statusBadge: Record<string, string> = {
  TRIAL: "bg-violet-500/15 text-violet-400 border-violet-500/20",
  ACTIVE: "bg-emerald-500/15 text-emerald-400 border-emerald-500/20",
  PAST_DUE: "bg-amber-500/15 text-amber-400 border-amber-500/20",
  EXPIRED: "bg-zinc-500/15 text-zinc-400 border-zinc-500/20",
  CANCELLED: "bg-rose-500/15 text-rose-400 border-rose-500/20",
}

const platformBadge: Record<string, string> = {
  GOOGLE_PLAY: "bg-blue-500/15 text-blue-400 border-blue-500/20",
  APP_STORE: "bg-sky-500/15 text-sky-400 border-sky-500/20",
}

function ColorBadge({ label, className }: { label: string; className: string }) {
  return (
    <span className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${className}`}>
      {label}
    </span>
  )
}

const STATUS_OPTIONS = ["", "TRIAL", "ACTIVE", "PAST_DUE", "EXPIRED", "CANCELLED"]
const PLATFORM_OPTIONS = ["", "GOOGLE_PLAY", "APP_STORE"]

export default function SubscriptionsPage() {
  const [data, setData] = useState<{ content: AdminSubscription[]; page: { totalPages: number; number: number; totalElements: number } } | null>(null)
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState("")
  const [platform, setPlatform] = useState("")
  const [loading, setLoading] = useState(true)

  const load = useCallback(async (p: number, s: string, pl: string) => {
    setLoading(true)
    try {
      const res = await subscriptionApi.list(p, s || undefined, pl || undefined)
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load subscriptions")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load(page, status, platform) }, [page, status, platform, load])

  function handleStatus(s: string) {
    setStatus(s)
    setPage(0)
  }

  function handlePlatform(pl: string) {
    setPlatform(pl)
    setPage(0)
  }

  const columns: ColumnDef<AdminSubscription>[] = [
    {
      accessorKey: "userName",
      header: "User",
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.userName}</p>
          <p className="text-xs text-muted-foreground">{row.original.userEmail ?? "—"}</p>
        </div>
      ),
    },
    {
      accessorKey: "planName",
      header: "Plan",
      cell: ({ row }) => <span className="text-sm">{row.original.planName}</span>,
    },
    {
      accessorKey: "status",
      header: "Status",
      cell: ({ row }) => (
        <ColorBadge
          label={row.original.status}
          className={statusBadge[row.original.status] ?? "bg-zinc-500/15 text-zinc-400 border-zinc-500/20"}
        />
      ),
    },
    {
      accessorKey: "platform",
      header: "Platform",
      cell: ({ row }) => (
        <ColorBadge
          label={row.original.platform}
          className={platformBadge[row.original.platform] ?? "bg-zinc-500/15 text-zinc-400 border-zinc-500/20"}
        />
      ),
    },
    {
      accessorKey: "currentPeriodEnd",
      header: "Expires",
      cell: ({ row }) =>
        row.original.currentPeriodEnd
          ? new Date(row.original.currentPeriodEnd).toLocaleDateString()
          : "—",
    },
    {
      accessorKey: "createdAt",
      header: "Started",
      cell: ({ row }) => new Date(row.original.createdAt).toLocaleDateString(),
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Subscriptions</h1>
          {data && (
            <p className="text-sm text-muted-foreground mt-0.5">
              {data.page.totalElements} total
            </p>
          )}
        </div>
        <div className="flex items-center gap-2">
          <DropdownMenu>
            <DropdownMenuTrigger className="flex items-center gap-1.5 rounded-lg border border-border bg-muted/40 px-3 py-2 text-sm font-medium hover:bg-muted transition-colors">
              {status || "All Statuses"}
              <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {STATUS_OPTIONS.map((s) => (
                <DropdownMenuItem key={s || "all"} onClick={() => handleStatus(s)}>
                  {s || "All Statuses"}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>

          <DropdownMenu>
            <DropdownMenuTrigger className="flex items-center gap-1.5 rounded-lg border border-border bg-muted/40 px-3 py-2 text-sm font-medium hover:bg-muted transition-colors">
              {platform || "All Platforms"}
              <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {PLATFORM_OPTIONS.map((pl) => (
                <DropdownMenuItem key={pl || "all"} onClick={() => handlePlatform(pl)}>
                  {pl || "All Platforms"}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
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
