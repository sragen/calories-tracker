"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Users, Settings, LayoutDashboard, LogOut, Zap } from "lucide-react"
import { cn } from "@/lib/utils"
import { clearAuthCookie } from "@/lib/auth-cookie"

const navItems = [
  { href: "/", label: "Dashboard", icon: LayoutDashboard },
  { href: "/users", label: "Users", icon: Users },
  { href: "/configs", label: "Config & Flags", icon: Settings },
]

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()

  function handleLogout() {
    clearAuthCookie()
    router.replace("/login")
  }

  return (
    <aside className="flex flex-col w-60 shrink-0 h-screen sticky top-0 bg-sidebar border-r border-sidebar-border">
      {/* Logo */}
      <div className="flex h-16 items-center gap-3 px-5 border-b border-sidebar-border">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
          <Zap className="h-4 w-4 text-primary-foreground" />
        </div>
        <div>
          <p className="text-sm font-semibold text-sidebar-foreground">App Template</p>
          <p className="text-xs text-sidebar-foreground/50">Admin Panel</p>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 p-3 space-y-0.5">
        <p className="px-3 pb-2 pt-1 text-xs font-medium uppercase tracking-wider text-sidebar-foreground/40">
          Menu
        </p>
        {navItems.map(({ href, label, icon: Icon }) => {
          const isActive = pathname === href
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-150",
                isActive
                  ? "bg-primary text-primary-foreground shadow-sm"
                  : "text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-foreground"
              )}
            >
              <Icon className="h-4 w-4 shrink-0" />
              {label}
            </Link>
          )
        })}
      </nav>

      {/* Sign Out */}
      <div className="p-3 border-t border-sidebar-border">
        <button
          onClick={handleLogout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-sidebar-foreground/60 hover:bg-sidebar-accent hover:text-sidebar-foreground transition-all duration-150"
        >
          <LogOut className="h-4 w-4 shrink-0" />
          Sign Out
        </button>
      </div>
    </aside>
  )
}
