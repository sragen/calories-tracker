"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Users, Settings, LayoutDashboard, LogOut } from "lucide-react"
import { cn } from "@/lib/utils"
import { clearAuthCookie } from "@/lib/auth-cookie"
import { Button } from "@/components/ui/button"

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
    <aside className="flex flex-col w-56 shrink-0 border-r bg-background h-screen sticky top-0">
      <div className="flex h-14 items-center border-b px-4">
        <span className="font-semibold text-sm">App Template Admin</span>
      </div>

      <nav className="flex-1 p-2 space-y-1">
        {navItems.map(({ href, label, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            className={cn(
              "flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors",
              pathname === href
                ? "bg-primary text-primary-foreground"
                : "text-muted-foreground hover:bg-muted hover:text-foreground"
            )}
          >
            <Icon className="h-4 w-4" />
            {label}
          </Link>
        ))}
      </nav>

      <div className="p-2 border-t">
        <Button
          variant="ghost"
          size="sm"
          className="w-full justify-start gap-3 text-muted-foreground"
          onClick={handleLogout}
        >
          <LogOut className="h-4 w-4" />
          Logout
        </Button>
      </div>
    </aside>
  )
}
