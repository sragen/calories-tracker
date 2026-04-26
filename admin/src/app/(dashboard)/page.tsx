"use client"

import { useEffect, useState } from "react"
import { Users, Settings, TrendingUp, Activity } from "lucide-react"
import { api } from "@/lib/api"

interface PageData {
  totalUsers: number
  activeConfigs: number
}

function StatCard({
  label,
  value,
  icon: Icon,
  iconClass,
  bgClass,
  sub,
}: {
  label: string
  value: string | number
  icon: React.ElementType
  iconClass: string
  bgClass: string
  sub?: string
}) {
  return (
    <div className="rounded-xl border border-border bg-card p-5 flex items-start gap-4">
      <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-lg ${bgClass}`}>
        <Icon className={`h-5 w-5 ${iconClass}`} />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-sm text-muted-foreground truncate">{label}</p>
        <p className="mt-1 text-2xl font-bold tracking-tight">{value}</p>
        {sub && <p className="mt-0.5 text-xs text-muted-foreground">{sub}</p>}
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const [data, setData] = useState<PageData | null>(null)

  useEffect(() => {
    Promise.all([
      api.get<{ page: { totalElements: number } }>("/api/admin/users"),
      api.get<Array<unknown>>("/api/admin/configs"),
    ]).then(([users, configs]) => {
      setData({
        totalUsers: users.page.totalElements,
        activeConfigs: configs.length,
      })
    }).catch(() => {})
  }, [])

  const loading = data === null

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Dashboard</h1>
        <p className="mt-1 text-sm text-muted-foreground">Overview of your application</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          label="Total Users"
          value={loading ? "—" : data.totalUsers}
          icon={Users}
          bgClass="bg-indigo-500/15"
          iconClass="text-indigo-400"
          sub="Registered accounts"
        />
        <StatCard
          label="Config Entries"
          value={loading ? "—" : data.activeConfigs}
          icon={Settings}
          bgClass="bg-emerald-500/15"
          iconClass="text-emerald-400"
          sub="Active configurations"
        />
        <StatCard
          label="Uptime"
          value="99.9%"
          icon={TrendingUp}
          bgClass="bg-amber-500/15"
          iconClass="text-amber-400"
          sub="Last 30 days"
        />
        <StatCard
          label="System"
          value="Healthy"
          icon={Activity}
          bgClass="bg-sky-500/15"
          iconClass="text-sky-400"
          sub="All services running"
        />
      </div>
    </div>
  )
}
