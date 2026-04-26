"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { ToggleLeft, ToggleRight, Save } from "lucide-react"

interface Config {
  id: number
  key: string
  value: string
  type: string
  label: string | null
  description: string | null
  isActive: boolean
}

export default function ConfigsPage() {
  const [configs, setConfigs] = useState<Config[]>([])
  const [editValues, setEditValues] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)

  async function loadConfigs() {
    try {
      const data = await api.get<Config[]>("/api/admin/configs")
      setConfigs(data)
      const initial: Record<string, string> = {}
      data.forEach((c) => { initial[c.key] = c.value })
      setEditValues(initial)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load configs")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadConfigs() }, [])

  async function handleToggle(key: string) {
    try {
      const updated = await api.post<Config>(`/api/admin/configs/${key}/toggle`)
      setConfigs((prev) => prev.map((c) => (c.key === key ? updated : c)))
      setEditValues((prev) => ({ ...prev, [key]: updated.value }))
      toast.success(`${key} toggled to ${updated.value}`)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to toggle")
    }
  }

  async function handleSave(key: string) {
    try {
      const updated = await api.put<Config>(`/api/admin/configs/${key}`, {
        value: editValues[key],
      })
      setConfigs((prev) => prev.map((c) => (c.key === key ? updated : c)))
      toast.success(`${key} updated`)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to save")
    }
  }

  if (loading) {
    return <div className="text-muted-foreground">Loading configs…</div>
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold tracking-tight">Config & Feature Flags</h1>

      <div className="grid gap-4">
        {configs.map((config) => (
          <Card key={config.key}>
            <CardHeader className="pb-2">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <CardTitle className="text-sm font-semibold">
                    {config.label ?? config.key}
                  </CardTitle>
                  {config.description && (
                    <CardDescription className="text-xs mt-1">
                      {config.description}
                    </CardDescription>
                  )}
                </div>
                <Badge variant="outline" className="text-xs shrink-0">
                  {config.type}
                </Badge>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-3">
                {config.type === "BOOLEAN" ? (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="gap-2"
                    onClick={() => handleToggle(config.key)}
                  >
                    {config.value === "true" ? (
                      <ToggleRight className="h-5 w-5 text-primary" />
                    ) : (
                      <ToggleLeft className="h-5 w-5 text-muted-foreground" />
                    )}
                    <span className={config.value === "true" ? "text-primary" : "text-muted-foreground"}>
                      {config.value === "true" ? "Enabled" : "Disabled"}
                    </span>
                  </Button>
                ) : (
                  <div className="flex flex-1 items-center gap-2">
                    <Input
                      value={editValues[config.key] ?? ""}
                      onChange={(e) =>
                        setEditValues((prev) => ({ ...prev, [config.key]: e.target.value }))
                      }
                      className="max-w-xs h-8 text-sm"
                    />
                    <Button
                      size="sm"
                      variant="outline"
                      className="gap-1"
                      onClick={() => handleSave(config.key)}
                      disabled={editValues[config.key] === config.value}
                    >
                      <Save className="h-3 w-3" />
                      Save
                    </Button>
                  </div>
                )}
                <code className="ml-auto text-xs text-muted-foreground font-mono">{config.key}</code>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
