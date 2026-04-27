import Link from "next/link"
import { ChevronRight } from "lucide-react"
import { FoodForm } from "@/components/food-form"

export default function CreateFoodPage() {
  return (
    <div className="space-y-6">
      <div>
        <nav className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
          <Link href="/foods" className="hover:text-foreground transition-colors">Food Database</Link>
          <ChevronRight className="h-3 w-3" />
          <span className="text-foreground">Create Food</span>
        </nav>
        <h1 className="text-2xl font-bold tracking-tight">Add Food Item</h1>
      </div>

      <FoodForm />
    </div>
  )
}
