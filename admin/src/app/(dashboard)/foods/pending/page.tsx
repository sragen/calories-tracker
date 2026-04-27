"use client"

import { useEffect, useState, useCallback } from "react"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import { api, FoodItem, FoodPage } from "@/lib/api"
import { toast } from "sonner"
import { CheckCircle, XCircle } from "lucide-react"

export default function PendingFoodsPage() {
  const [data, setData] = useState<FoodPage | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [processingId, setProcessingId] = useState<number | null>(null)

  const loadPending = useCallback(async (p: number) => {
    setLoading(true)
    try {
      const res = await api.get<FoodPage>(`/api/admin/foods/pending?page=${p}&size=20`)
      setData(res)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to load")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadPending(page) }, [page, loadPending])

  async function handleVerify(id: number) {
    setProcessingId(id)
    try {
      await api.post(`/api/admin/foods/${id}/verify`)
      toast.success("Food verified and published")
      loadPending(page)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to verify")
    } finally {
      setProcessingId(null)
    }
  }

  async function handleReject(id: number) {
    if (!confirm("Reject and delete this submission?")) return
    setProcessingId(id)
    try {
      await api.post(`/api/admin/foods/${id}/reject`)
      toast.success("Submission rejected")
      loadPending(page)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to reject")
    } finally {
      setProcessingId(null)
    }
  }

  const columns: ColumnDef<FoodItem>[] = [
    {
      accessorKey: "name",
      header: "Food Name",
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.name}</p>
          {row.original.nameEn && <p className="text-xs text-muted-foreground">{row.original.nameEn}</p>}
          {row.original.barcode && (
            <p className="text-xs font-mono text-muted-foreground">Barcode: {row.original.barcode}</p>
          )}
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
      cell: ({ row }) => (
        <span className="text-xs font-medium text-amber-400">{row.original.source}</span>
      ),
    },
    {
      accessorKey: "createdAt",
      header: "Submitted",
      cell: ({ row }) => new Date(row.original.createdAt).toLocaleDateString(),
    },
    {
      id: "actions",
      header: "Actions",
      cell: ({ row }) => {
        const food = row.original
        const isProcessing = processingId === food.id
        return (
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleVerify(food.id)}
              disabled={isProcessing}
              className="text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/10"
            >
              <CheckCircle className="h-3.5 w-3.5 mr-1" />
              Approve
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => handleReject(food.id)}
              disabled={isProcessing}
              className="text-destructive border-destructive/30 hover:bg-destructive/10"
            >
              <XCircle className="h-3.5 w-3.5 mr-1" />
              Reject
            </Button>
          </div>
        )
      },
    },
  ]

  const total = data?.page.totalElements ?? 0

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Pending Review</h1>
          <p className="text-sm text-muted-foreground mt-1">
            User-submitted foods awaiting verification
          </p>
        </div>
        {total > 0 && (
          <span className="inline-flex items-center rounded-full bg-amber-500/15 border border-amber-500/20 px-3 py-1 text-sm font-medium text-amber-400">
            {total} pending
          </span>
        )}
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
