"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { Users, BookOpen, Star, TrendingUp } from "lucide-react"
import { api } from "@/lib/api"

interface Overview {
  totalUsers: number
  dau: number
  mau: number
  totalLogs: number
  activePremium: number
  totalRevenueIdr: number
}

function StatCard({
  label, value, sub, icon: Icon, iconClass, bgClass, href,
}: {
  label: string; value: string | number; sub?: string
  icon: React.ElementType; iconClass: string; bgClass: string; href?: string
}) {
  const content = (
    <div className="rounded-xl border border-border bg-card p-5 flex items-start gap-4 transition-colors hover:bg-card/80">
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
  return href ? <Link href={href}>{content}</Link> : content
}

function formatIdr(v: number) {
  if (v >= 1_000_000) return `Rp ${(v / 1_000_000).toFixed(1)}jt`
  if (v >= 1_000) return `Rp ${(v / 1_000).toFixed(0)}rb`
  return `Rp ${v}`
}

export default function DashboardPage() {
  const [data, setData] = useState<Overview | null>(null)

  useEffect(() => {
    api.get<Overview>("/api/admin/analytics/overview")
      .then(setData)
      .catch(() => {})
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
          value={loading ? "—" : (data.totalUsers).toLocaleString()}
          icon={Users}
          bgClass="bg-indigo-500/15"
          iconClass="text-indigo-400"
          sub="Registered accounts"
          href="/users"
        />
        <StatCard
          label="Daily Active"
          value={loading ? "—" : (data.dau).toLocaleString()}
          icon={TrendingUp}
          bgClass="bg-sky-500/15"
          iconClass="text-sky-400"
          sub="Users active today"
        />
        <StatCard
          label="Total Meal Logs"
          value={loading ? "—" : (data.totalLogs).toLocaleString()}
          icon={BookOpen}
          bgClass="bg-emerald-500/15"
          iconClass="text-emerald-400"
          sub="Diary entries"
          href="/analytics"
        />
        <StatCard
          label="Revenue"
          value={loading ? "—" : formatIdr(data.totalRevenueIdr)}
          icon={Star}
          bgClass="bg-amber-500/15"
          iconClass="text-amber-400"
          sub={loading ? "" : `${data.activePremium} active premium`}
          href="/subscriptions"
        />
      </div>

      <div className="rounded-xl border border-border bg-card p-5">
        <p className="text-sm text-muted-foreground text-center py-8">
          View detailed charts on the{" "}
          <Link href="/analytics" className="text-primary hover:underline">Analytics page →</Link>
        </p>
      </div>
    </div>
  )
}
