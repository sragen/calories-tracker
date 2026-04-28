"use client"

import { useEffect, useState, useCallback } from "react"
import Link from "next/link"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import { Button, buttonVariants } from "@/components/ui/button"
import { cn } from "@/lib/utils"
import { Input } from "@/components/ui/input"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { api, FoodItem, FoodPage, FoodCategory } from "@/lib/api"
import { toast } from "sonner"
import { MoreHorizontal, Plus, Search, Upload } from "lucide-react"

function VerifiedBadge({ verified }: { verified: boolean }) {
  return (
    <span className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${verified ? "bg-emerald-500/15 text-emerald-400 border-emerald-500/20" : "bg-zinc-500/15 text-zinc-400 border-zinc-500/20"}`}>
      {verified ? "Verified" : "Unverified"}
    </span>
  )
}

function SourceBadge({ source }: { source: string }) {
  const classes: Record<string, string> = {
    ADMIN: "bg-indigo-500/15 text-indigo-400 border-indigo-500/20",
    USER: "bg-amber-500/15 text-amber-400 border-amber-500/20",
    OPEN_FOOD_FACTS: "bg-blue-500/15 text-blue-400 border-blue-500/20",
    TKPI: "bg-purple-500/15 text-purple-400 border-purple-500/20",
  }
  return (
    <span className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${classes[source] ?? classes.ADMIN}`}>
      {source}
    </span>
  )
}

export default function FoodsPage() {
  const [data, setData] = useState<FoodPage | null>(null)
  const [page, setPage] = useState(0)
  const [query, setQuery] = useState("")
  const [searchInput, setSearchInput] = useState("")
  const [categories, setCategories] = useState<FoodCategory[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<FoodCategory[]>("/api/foods/categories").then(setCategories).catch(() => {})
  }, [])

  const loadFoods = useCallback(async (p: number, q: string) => {
    setLoading(true)
    try {
      const params = new URLSearchParams({ page: String(p), size: "20", sort: "createdAt,desc" })
      if (q) params.set("q", q)
      const res = await api.get<FoodPage>(`/api/admin/foods?${params}`)
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load foods")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadFoods(page, query) }, [page, query, loadFoods])

  function handleSearch(e: React.FormEvent) {
    e.preventDefault()
    setPage(0)
    setQuery(searchInput)
  }

  async function toggleVerify(id: number, verified: boolean) {
    try {
      if (verified) {
        // Unverify by updating via PUT with isVerified=false
        const food = data?.content.find((f) => f.id === id)
        if (!food) return
        await api.put(`/api/admin/foods/${id}`, { ...food, isVerified: false })
      } else {
        await api.post(`/api/admin/foods/${id}/verify`)
      }
      toast.success(verified ? "Unverified" : "Verified")
      loadFoods(page, query)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to update")
    }
  }

  async function deleteFood(id: number) {
    if (!confirm("Delete this food item?")) return
    try {
      await api.delete(`/api/admin/foods/${id}`)
      toast.success("Deleted")
      loadFoods(page, query)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to delete")
    }
  }

  const columns: ColumnDef<FoodItem>[] = [
    {
      accessorKey: "name",
      header: "Name",
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.name}</p>
          {row.original.nameEn && <p className="text-xs text-muted-foreground">{row.original.nameEn}</p>}
        </div>
      ),
    },
    {
      accessorKey: "categoryName",
      header: "Category",
      cell: ({ row }) => row.original.categoryName ?? "—",
    },
    {
      accessorKey: "caloriesPer100g",
      header: "kcal/100g",
      cell: ({ row }) => `${row.original.caloriesPer100g} kcal`,
    },
    {
      id: "macros",
      header: "Macros",
      cell: ({ row }) => {
        const f = row.original
        return (
          <p className="text-xs text-muted-foreground whitespace-nowrap">
            P:{f.proteinPer100g}g C:{f.carbsPer100g}g F:{f.fatPer100g}g
          </p>
        )
      },
    },
    {
      accessorKey: "source",
      header: "Source",
      cell: ({ row }) => <SourceBadge source={row.original.source} />,
    },
    {
      accessorKey: "isVerified",
      header: "Status",
      cell: ({ row }) => <VerifiedBadge verified={row.original.isVerified} />,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => {
        const food = row.original
        return (
          <DropdownMenu>
            <DropdownMenuTrigger className="flex h-8 w-8 items-center justify-center rounded-md border border-transparent hover:border-border hover:bg-muted">
              <MoreHorizontal className="h-4 w-4" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>
                <Link href={`/foods/${food.id}`} className="w-full">Edit</Link>
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => toggleVerify(food.id, food.isVerified)}>
                {food.isVerified ? "Unverify" : "Verify"}
              </DropdownMenuItem>
              <DropdownMenuItem className="text-destructive" onClick={() => deleteFood(food.id)}>
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )
      },
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold tracking-tight">Food Database</h1>
        <div className="flex gap-2">
          <Link href="/foods/import" className={cn(buttonVariants({ variant: "outline" }))}>
            <Upload className="mr-2 h-4 w-4" />
            Import CSV
          </Link>
          <Link href="/foods/create" className={cn(buttonVariants())}>
            <Plus className="mr-2 h-4 w-4" />
            Add Food
          </Link>
        </div>
      </div>

      <div className="flex flex-wrap gap-3">
        <form onSubmit={handleSearch} className="flex gap-2">
          <Input
            placeholder="Search foods..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="w-64"
          />
          <Button type="submit" variant="outline" size="icon">
            <Search className="h-4 w-4" />
          </Button>
        </form>
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
