"use client"

import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { Users, TrendingUp, BookOpen, Star, Download } from "lucide-react"

interface Overview {
  totalUsers: number
  dau: number
  mau: number
  totalLogs: number
  activePremium: number
  totalRevenueIdr: number
}

interface RevenuePoint { date: string; revenueIdr: number }
interface TopFood { id: number; name: string; logCount: number }

function StatCard({
  label, value, sub, icon: Icon, iconClass, bgClass,
}: { label: string; value: string | number; sub?: string; icon: React.ElementType; iconClass: string; bgClass: string }) {
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

function BarChart({ data, maxValue, color, labelKey, valueKey }: {
  data: Record<string, string | number>[]
  maxValue: number
  color: string
  labelKey: string
  valueKey: string
}) {
  if (!data.length) return <p className="text-sm text-muted-foreground py-8 text-center">No data</p>
  return (
    <div className="flex items-end gap-1 h-40">
      {data.map((item, i) => {
        const val = Number(item[valueKey])
        const pct = maxValue > 0 ? (val / maxValue) * 100 : 0
        return (
          <div key={i} className="flex-1 flex flex-col items-center gap-1 group relative">
            <div
              className={`w-full rounded-t-sm transition-all ${color}`}
              style={{ height: `${Math.max(pct, 1)}%` }}
            />
            <div className="absolute bottom-full mb-1 hidden group-hover:block bg-popover border border-border rounded px-2 py-1 text-xs whitespace-nowrap z-10">
              {String(item[labelKey])}: {val.toLocaleString()}
            </div>
          </div>
        )
      })}
    </div>
  )
}

function formatIdr(v: number) {
  if (v >= 1_000_000) return `Rp ${(v / 1_000_000).toFixed(1)}jt`
  if (v >= 1_000) return `Rp ${(v / 1_000).toFixed(0)}rb`
  return `Rp ${v}`
}

async function downloadCsv(type: string) {
  const BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"
  const token = localStorage.getItem("access_token")
  const res = await fetch(`${BASE}/api/admin/analytics/export?type=${type}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok) { toast.error("Export failed"); return }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement("a")
  a.href = url
  a.download = `analytics-${type}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export default function AnalyticsPage() {
  const [overview, setOverview] = useState<Overview | null>(null)
  const [revenue, setRevenue] = useState<RevenuePoint[]>([])
  const [topFoods, setTopFoods] = useState<TopFood[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.get<Overview>("/api/admin/analytics/overview"),
      api.get<RevenuePoint[]>("/api/admin/analytics/revenue?days=30"),
      api.get<TopFood[]>("/api/admin/analytics/top-foods?limit=10"),
    ]).then(([ov, rev, tf]) => {
      setOverview(ov)
      setRevenue(rev)
      setTopFoods(tf)
    }).catch(e => toast.error(e instanceof Error ? e.message : "Failed to load analytics"))
    .finally(() => setLoading(false))
  }, [])

  const maxRevenue = revenue.reduce((m, r) => Math.max(m, r.revenueIdr), 0)
  const maxLogCount = topFoods.reduce((m, f) => Math.max(m, f.logCount), 0)

  return (
    <div className="space-y-8">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Analytics</h1>
          <p className="text-sm text-muted-foreground mt-1">Real-time overview of your application</p>
        </div>
        <div className="flex gap-2">
          {(["overview", "revenue", "top-foods"] as const).map(type => (
            <button
              key={type}
              onClick={() => downloadCsv(type)}
              className="inline-flex items-center gap-1.5 rounded-lg border border-border bg-card px-3 py-1.5 text-xs font-medium text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
            >
              <Download className="h-3 w-3" />
              {type}
            </button>
          ))}
        </div>
      </div>

      {/* Overview cards */}
      <div className="grid grid-cols-2 xl:grid-cols-3 gap-4">
        <StatCard label="Total Users"     value={loading ? "—" : (overview?.totalUsers ?? 0).toLocaleString()}
          sub="Registered accounts"       icon={Users}      bgClass="bg-indigo-500/15"  iconClass="text-indigo-400" />
        <StatCard label="DAU"             value={loading ? "—" : (overview?.dau ?? 0).toLocaleString()}
          sub="Active today"              icon={TrendingUp} bgClass="bg-sky-500/15"     iconClass="text-sky-400" />
        <StatCard label="MAU"             value={loading ? "—" : (overview?.mau ?? 0).toLocaleString()}
          sub="Active last 30 days"       icon={TrendingUp} bgClass="bg-purple-500/15"  iconClass="text-purple-400" />
        <StatCard label="Total Logs"      value={loading ? "—" : (overview?.totalLogs ?? 0).toLocaleString()}
          sub="Meal log entries"          icon={BookOpen}   bgClass="bg-emerald-500/15" iconClass="text-emerald-400" />
        <StatCard label="Active Premium"  value={loading ? "—" : (overview?.activePremium ?? 0).toLocaleString()}
          sub="Premium subscribers"       icon={Star}       bgClass="bg-amber-500/15"   iconClass="text-amber-400" />
        <StatCard label="Total Revenue"   value={loading ? "—" : formatIdr(overview?.totalRevenueIdr ?? 0)}
          sub="Lifetime revenue"          icon={TrendingUp} bgClass="bg-rose-500/15"    iconClass="text-rose-400" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Revenue chart */}
        <div className="rounded-xl border border-border bg-card p-5 space-y-4">
          <div>
            <h2 className="text-sm font-semibold">Revenue — Last 30 Days</h2>
            <p className="text-xs text-muted-foreground">Daily revenue from subscriptions</p>
          </div>
          {loading ? (
            <div className="h-40 flex items-center justify-center">
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            </div>
          ) : (
            <>
              <BarChart
                data={revenue.map(r => ({ date: r.date.slice(5), revenueIdr: r.revenueIdr }))}
                maxValue={maxRevenue}
                color="bg-emerald-500"
                labelKey="date"
                valueKey="revenueIdr"
              />
              <div className="flex justify-between text-xs text-muted-foreground">
                <span>{revenue[0]?.date?.slice(5) ?? ""}</span>
                <span>{revenue[revenue.length - 1]?.date?.slice(5) ?? ""}</span>
              </div>
            </>
          )}
        </div>

        {/* Top foods chart */}
        <div className="rounded-xl border border-border bg-card p-5 space-y-4">
          <div>
            <h2 className="text-sm font-semibold">Top 10 Foods</h2>
            <p className="text-xs text-muted-foreground">Most logged food items</p>
          </div>
          {loading ? (
            <div className="h-40 flex items-center justify-center">
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            </div>
          ) : (
            <div className="space-y-2 max-h-48 overflow-y-auto">
              {topFoods.map((food, i) => (
                <div key={food.id} className="flex items-center gap-3">
                  <span className="text-xs text-muted-foreground w-4 shrink-0">{i + 1}</span>
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center mb-0.5">
                      <span className="text-sm truncate">{food.name}</span>
                      <span className="text-xs text-muted-foreground ml-2 shrink-0">{food.logCount.toLocaleString()}×</span>
                    </div>
                    <div className="h-1.5 rounded-full bg-muted overflow-hidden">
                      <div
                        className="h-full rounded-full bg-indigo-500"
                        style={{ width: `${maxLogCount > 0 ? (food.logCount / maxLogCount) * 100 : 0}%` }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
