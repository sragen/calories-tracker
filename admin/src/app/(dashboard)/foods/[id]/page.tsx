"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { ChevronRight } from "lucide-react"
import { FoodForm } from "@/components/food-form"
import { api, FoodItem } from "@/lib/api"

export default function EditFoodPage({ params }: { params: { id: string } }) {
  const [food, setFood] = useState<FoodItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.get<FoodItem>(`/api/admin/foods/${params.id}`)
      .then(setFood)
      .catch((e) => setError(e instanceof Error ? e.message : "Not found"))
      .finally(() => setLoading(false))
  }, [params.id])

  return (
    <div className="space-y-6">
      <div>
        <nav className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
          <Link href="/foods" className="hover:text-foreground transition-colors">Food Database</Link>
          <ChevronRight className="h-3 w-3" />
          <span className="text-foreground">Edit Food</span>
        </nav>
        <h1 className="text-2xl font-bold tracking-tight">Edit Food Item</h1>
      </div>

      {loading && <p className="text-muted-foreground">Loading...</p>}
      {error && <p className="text-destructive">{error}</p>}
      {food && <FoodForm food={food} />}
    </div>
  )
}
