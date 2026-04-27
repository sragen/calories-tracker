"use client"

import { useEffect, useState, useCallback } from "react"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetDescription,
  SheetFooter,
} from "@/components/ui/sheet"
import { whitelistApi, userSearchApi, WhitelistEntry } from "@/lib/api"
import { toast } from "sonner"
import { Plus, Trash2 } from "lucide-react"

export default function WhitelistPage() {
  const [data, setData] = useState<{ content: WhitelistEntry[]; page: { totalPages: number; number: number } } | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)

  // Add form state
  const [searchEmail, setSearchEmail] = useState("")
  const [searchResults, setSearchResults] = useState<{ id: number; name: string; email: string | null }[]>([])
  const [selectedUser, setSelectedUser] = useState<{ id: number; name: string; email: string | null } | null>(null)
  const [note, setNote] = useState("")
  const [searching, setSearching] = useState(false)
  const [adding, setAdding] = useState(false)

  const load = useCallback(async (p: number) => {
    setLoading(true)
    try {
      const res = await whitelistApi.list(p)
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load whitelist")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load(page) }, [page, load])

  async function handleSearch() {
    if (!searchEmail.trim()) return
    setSearching(true)
    setSelectedUser(null)
    try {
      const res = await userSearchApi.search(searchEmail.trim())
      setSearchResults(res.content)
      if (res.content.length === 0) toast.info("No users found")
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Search failed")
    } finally {
      setSearching(false)
    }
  }

  async function handleAdd() {
    if (!selectedUser) return
    setAdding(true)
    try {
      await whitelistApi.add(selectedUser.id, note.trim() || undefined)
      toast.success(`${selectedUser.name} added to whitelist`)
      setOpen(false)
      resetForm()
      load(0)
      setPage(0)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to add to whitelist")
    } finally {
      setAdding(false)
    }
  }

  async function handleRemove(userId: number, name: string) {
    try {
      await whitelistApi.remove(userId)
      toast.success(`${name} removed from whitelist`)
      load(page)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to remove")
    }
  }

  function resetForm() {
    setSearchEmail("")
    setSearchResults([])
    setSelectedUser(null)
    setNote("")
  }

  const columns: ColumnDef<WhitelistEntry>[] = [
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
      accessorKey: "note",
      header: "Note",
      cell: ({ row }) => (
        <span className="text-sm text-muted-foreground">{row.original.note ?? "—"}</span>
      ),
    },
    {
      accessorKey: "addedByName",
      header: "Added By",
      cell: ({ row }) => <span className="text-sm">{row.original.addedByName}</span>,
    },
    {
      accessorKey: "createdAt",
      header: "Added At",
      cell: ({ row }) => new Date(row.original.createdAt).toLocaleDateString(),
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <Button
          variant="ghost"
          size="icon-sm"
          className="text-destructive hover:text-destructive hover:bg-destructive/10"
          onClick={() => handleRemove(row.original.userId, row.original.userName)}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Premium Whitelist</h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            Users with permanent premium access regardless of subscription status
          </p>
        </div>
        <Button onClick={() => { resetForm(); setOpen(true) }}>
          <Plus className="h-4 w-4 mr-2" />
          Add User
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        pageIndex={page}
        pageCount={data?.page.totalPages ?? 1}
        onPageChange={setPage}
        isLoading={loading}
      />

      <Sheet open={open} onOpenChange={setOpen}>
        <SheetContent side="right">
          <SheetHeader>
            <SheetTitle>Add to Whitelist</SheetTitle>
            <SheetDescription>
              Search for a user by email and grant permanent premium access.
            </SheetDescription>
          </SheetHeader>

          <div className="flex-1 overflow-y-auto px-4 space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email-search">Search by Email</Label>
              <div className="flex gap-2">
                <Input
                  id="email-search"
                  placeholder="user@example.com"
                  value={searchEmail}
                  onChange={(e) => setSearchEmail(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                />
                <Button variant="outline" onClick={handleSearch} disabled={searching}>
                  {searching ? "..." : "Search"}
                </Button>
              </div>
            </div>

            {searchResults.length > 0 && (
              <div className="space-y-1.5">
                <Label>Select User</Label>
                {searchResults.map((u) => (
                  <button
                    key={u.id}
                    onClick={() => setSelectedUser(u)}
                    className={`w-full text-left rounded-lg border px-3 py-2.5 text-sm transition-colors ${
                      selectedUser?.id === u.id
                        ? "border-primary bg-primary/10 text-foreground"
                        : "border-border hover:bg-muted"
                    }`}
                  >
                    <p className="font-medium">{u.name}</p>
                    <p className="text-xs text-muted-foreground">{u.email ?? "—"}</p>
                  </button>
                ))}
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="note">Note (optional)</Label>
              <Input
                id="note"
                placeholder="e.g. Internal tester, Owner account"
                value={note}
                onChange={(e) => setNote(e.target.value)}
              />
            </div>
          </div>

          <SheetFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
            <Button onClick={handleAdd} disabled={!selectedUser || adding}>
              {adding ? "Adding..." : "Add to Whitelist"}
            </Button>
          </SheetFooter>
        </SheetContent>
      </Sheet>
    </div>
  )
}
