"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { api } from "@/lib/api"
import { Users, Settings } from "lucide-react"

interface PageData {
  totalUsers: number
  activeConfigs: number
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

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold">Dashboard</h1>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{data?.totalUsers ?? "—"}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">Config Entries</CardTitle>
            <Settings className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{data?.activeConfigs ?? "—"}</p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
