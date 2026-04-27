"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { api, FoodCategory, FoodItem } from "@/lib/api"
import { toast } from "sonner"

interface FoodFormData {
  name: string
  nameEn: string
  categoryId: string
  caloriesPer100g: string
  proteinPer100g: string
  carbsPer100g: string
  fatPer100g: string
  fiberPer100g: string
  defaultServingG: string
  servingDescription: string
  barcode: string
}

const EMPTY_FORM: FoodFormData = {
  name: "",
  nameEn: "",
  categoryId: "",
  caloriesPer100g: "",
  proteinPer100g: "0",
  carbsPer100g: "0",
  fatPer100g: "0",
  fiberPer100g: "",
  defaultServingG: "100",
  servingDescription: "",
  barcode: "",
}

interface FoodFormProps {
  food?: FoodItem
  onSuccess?: () => void
}

export function FoodForm({ food, onSuccess }: FoodFormProps) {
  const router = useRouter()
  const [form, setForm] = useState<FoodFormData>(
    food
      ? {
          name: food.name,
          nameEn: food.nameEn ?? "",
          categoryId: food.categoryId ? String(food.categoryId) : "",
          caloriesPer100g: String(food.caloriesPer100g),
          proteinPer100g: String(food.proteinPer100g),
          carbsPer100g: String(food.carbsPer100g),
          fatPer100g: String(food.fatPer100g),
          fiberPer100g: food.fiberPer100g != null ? String(food.fiberPer100g) : "",
          defaultServingG: String(food.defaultServingG),
          servingDescription: food.servingDescription ?? "",
          barcode: food.barcode ?? "",
        }
      : EMPTY_FORM
  )
  const [categories, setCategories] = useState<FoodCategory[]>([])
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get<FoodCategory[]>("/api/foods/categories").then(setCategories).catch(() => {})
  }, [])

  function set(field: keyof FoodFormData, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!form.name.trim()) { toast.error("Name is required"); return }
    if (!form.caloriesPer100g) { toast.error("Calories is required"); return }

    const body = {
      name: form.name.trim(),
      nameEn: form.nameEn.trim() || null,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      caloriesPer100g: Number(form.caloriesPer100g),
      proteinPer100g: Number(form.proteinPer100g),
      carbsPer100g: Number(form.carbsPer100g),
      fatPer100g: Number(form.fatPer100g),
      fiberPer100g: form.fiberPer100g ? Number(form.fiberPer100g) : null,
      defaultServingG: Number(form.defaultServingG) || 100,
      servingDescription: form.servingDescription.trim() || null,
      barcode: form.barcode.trim() || null,
    }

    setSaving(true)
    try {
      if (food) {
        await api.put(`/api/admin/foods/${food.id}`, body)
        toast.success("Food updated")
      } else {
        await api.post("/api/admin/foods", body)
        toast.success("Food created")
      }
      onSuccess?.()
      router.push("/foods")
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to save")
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6 max-w-2xl">
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="name">Name (Indonesian) *</Label>
          <Input id="name" value={form.name} onChange={(e) => set("name", e.target.value)} placeholder="Nasi Putih" required />
        </div>
        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="nameEn">Name (English)</Label>
          <Input id="nameEn" value={form.nameEn} onChange={(e) => set("nameEn", e.target.value)} placeholder="Steamed White Rice" />
        </div>
        <div className="col-span-2 space-y-1.5">
          <Label htmlFor="category">Category</Label>
          <select
            id="category"
            value={form.categoryId}
            onChange={(e) => set("categoryId", e.target.value)}
            className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          >
            <option value="">— Select Category —</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </div>
      </div>

      <div>
        <p className="text-sm font-medium mb-3">Nutrition per 100g</p>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-1.5">
            <Label htmlFor="calories">Calories (kcal) *</Label>
            <Input id="calories" type="number" step="0.1" min="0" value={form.caloriesPer100g} onChange={(e) => set("caloriesPer100g", e.target.value)} required />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="protein">Protein (g)</Label>
            <Input id="protein" type="number" step="0.1" min="0" value={form.proteinPer100g} onChange={(e) => set("proteinPer100g", e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="carbs">Carbs (g)</Label>
            <Input id="carbs" type="number" step="0.1" min="0" value={form.carbsPer100g} onChange={(e) => set("carbsPer100g", e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="fat">Fat (g)</Label>
            <Input id="fat" type="number" step="0.1" min="0" value={form.fatPer100g} onChange={(e) => set("fatPer100g", e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="fiber">Fiber (g)</Label>
            <Input id="fiber" type="number" step="0.1" min="0" value={form.fiberPer100g} onChange={(e) => set("fiberPer100g", e.target.value)} />
          </div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="serving">Default Serving (g)</Label>
          <Input id="serving" type="number" step="0.1" min="1" value={form.defaultServingG} onChange={(e) => set("defaultServingG", e.target.value)} />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="servingDesc">Serving Description</Label>
          <Input id="servingDesc" value={form.servingDescription} onChange={(e) => set("servingDescription", e.target.value)} placeholder="1 bowl (200g)" />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="barcode">Barcode</Label>
          <Input id="barcode" value={form.barcode} onChange={(e) => set("barcode", e.target.value)} placeholder="8991234567890" />
        </div>
      </div>

      <div className="flex gap-3">
        <Button type="submit" disabled={saving}>
          {saving ? "Saving..." : food ? "Update Food" : "Create Food"}
        </Button>
        <Button type="button" variant="outline" onClick={() => router.push("/foods")}>
          Cancel
        </Button>
      </div>
    </form>
  )
}
