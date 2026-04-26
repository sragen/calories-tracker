"use client"

import { usePathname } from "next/navigation"
import { ChevronRight, Home } from "lucide-react"

const routeLabels: Record<string, string> = {
  "/": "Dashboard",
  "/users": "Users",
  "/configs": "Config & Flags",
}

export function Topbar() {
  const pathname = usePathname()
  const label = routeLabels[pathname] ?? "Page"
  const isHome = pathname === "/"

  return (
    <header className="sticky top-0 z-10 flex h-16 items-center gap-3 border-b border-border bg-background/80 backdrop-blur-sm px-6">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Home className="h-3.5 w-3.5" />
        {!isHome && (
          <>
            <ChevronRight className="h-3 w-3 opacity-50" />
            <span className="font-medium text-foreground">{label}</span>
          </>
        )}
        {isHome && <span className="font-medium text-foreground">Dashboard</span>}
      </div>
    </header>
  )
}
