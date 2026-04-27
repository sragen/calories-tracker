"use client"

import { useState, useRef } from "react"
import Link from "next/link"
import { ChevronRight, Upload, Download } from "lucide-react"
import { Button } from "@/components/ui/button"
import { toast } from "sonner"

interface ImportResult {
  imported: number
  skipped: number
  errors: string[]
}

const CSV_TEMPLATE = `name,nameEn,categoryId,caloriesPer100g,proteinPer100g,carbsPer100g,fatPer100g,fiberPer100g,defaultServingG,barcode
Nasi Putih,White Rice,1,175,3.1,38.9,0.3,,100,
Ayam Goreng,Fried Chicken,2,260,27.3,0,14.9,,100,
Tempe Goreng,Fried Tempeh,2,227,17.0,13.4,12.4,,100,
`

export default function ImportFoodsPage() {
  const [result, setResult] = useState<ImportResult | null>(null)
  const [uploading, setUploading] = useState(false)
  const [previewRows, setPreviewRows] = useState<string[][]>([])
  const fileRef = useRef<HTMLInputElement>(null)

  function downloadTemplate() {
    const blob = new Blob([CSV_TEMPLATE], { type: "text/csv" })
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = "tkpi_template.csv"
    a.click()
    URL.revokeObjectURL(url)
  }

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return

    const reader = new FileReader()
    reader.onload = (ev) => {
      const text = ev.target?.result as string
      const rows = text.split("\n").slice(0, 6).map((r) => r.split(",").map((c) => c.trim()))
      setPreviewRows(rows)
    }
    reader.readAsText(file)
  }

  async function handleUpload() {
    const file = fileRef.current?.files?.[0]
    if (!file) { toast.error("Select a CSV file first"); return }

    const token = typeof window !== "undefined" ? localStorage.getItem("access_token") : null
    const formData = new FormData()
    formData.append("file", file)

    setUploading(true)
    setResult(null)
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"}/api/admin/foods/import-csv`,
        {
          method: "POST",
          headers: token ? { Authorization: `Bearer ${token}` } : {},
          body: formData,
        }
      )
      if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }))
        throw new Error(err.message)
      }
      const data: ImportResult = await res.json()
      setResult(data)
      toast.success(`Imported ${data.imported} foods`)
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Import failed")
    } finally {
      setUploading(false)
    }
  }

  const headers = previewRows[0] ?? []
  const dataRows = previewRows.slice(1)

  return (
    <div className="space-y-6 max-w-3xl">
      <div>
        <nav className="flex items-center gap-1 text-sm text-muted-foreground mb-1">
          <Link href="/foods" className="hover:text-foreground transition-colors">Food Database</Link>
          <ChevronRight className="h-3 w-3" />
          <span className="text-foreground">Import CSV</span>
        </nav>
        <h1 className="text-2xl font-bold tracking-tight">Import Foods from CSV</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Bulk import food items from a CSV file (TKPI format)
        </p>
      </div>

      {/* Template download */}
      <div className="rounded-lg border border-border p-4 space-y-3">
        <h2 className="text-sm font-semibold">CSV Format</h2>
        <p className="text-xs text-muted-foreground">
          Columns: name, nameEn, categoryId, caloriesPer100g, proteinPer100g, carbsPer100g, fatPer100g, fiberPer100g, defaultServingG, barcode
        </p>
        <p className="text-xs text-muted-foreground">
          CategoryId: 1=Nasi & Sereal, 2=Lauk Pauk, 3=Sayuran, 4=Buah, 5=Minuman, 6=Snack, 7=Fast Food, 8=Kemasan, 9=Olahan Susu, 10=Masakan Indonesia, 11=Lainnya
        </p>
        <Button variant="outline" size="sm" onClick={downloadTemplate}>
          <Download className="h-3.5 w-3.5 mr-2" />
          Download Template
        </Button>
      </div>

      {/* File upload */}
      <div className="rounded-lg border border-dashed border-border p-8 space-y-4 text-center">
        <Upload className="h-8 w-8 mx-auto text-muted-foreground" />
        <div>
          <p className="text-sm font-medium">Choose CSV file</p>
          <p className="text-xs text-muted-foreground">Only .csv files, max 10MB</p>
        </div>
        <input
          ref={fileRef}
          type="file"
          accept=".csv"
          onChange={handleFileChange}
          className="block mx-auto text-sm text-muted-foreground file:mr-4 file:py-1.5 file:px-3 file:rounded-md file:border file:border-border file:bg-muted file:text-sm file:font-medium hover:file:bg-muted/80"
        />
      </div>

      {/* Preview */}
      {previewRows.length > 0 && (
        <div className="space-y-2">
          <h2 className="text-sm font-semibold">Preview (first 5 rows)</h2>
          <div className="overflow-x-auto rounded-lg border border-border">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b border-border bg-muted/50">
                  {headers.map((h, i) => (
                    <th key={i} className="px-3 py-2 text-left font-medium text-muted-foreground">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {dataRows.filter(r => r.some(c => c)).map((row, ri) => (
                  <tr key={ri} className="border-b border-border last:border-0">
                    {row.map((cell, ci) => (
                      <td key={ci} className="px-3 py-2">{cell || "—"}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Button onClick={handleUpload} disabled={uploading} className="w-full">
        {uploading ? "Importing..." : "Import Foods"}
      </Button>

      {/* Result */}
      {result && (
        <div className="rounded-lg border border-border p-4 space-y-3">
          <h2 className="text-sm font-semibold">Import Result</h2>
          <div className="grid grid-cols-2 gap-4">
            <div className="text-center rounded-md bg-emerald-500/10 border border-emerald-500/20 p-3">
              <p className="text-2xl font-bold text-emerald-400">{result.imported}</p>
              <p className="text-xs text-muted-foreground">Imported</p>
            </div>
            <div className="text-center rounded-md bg-amber-500/10 border border-amber-500/20 p-3">
              <p className="text-2xl font-bold text-amber-400">{result.skipped}</p>
              <p className="text-xs text-muted-foreground">Skipped</p>
            </div>
          </div>
          {result.errors.length > 0 && (
            <div>
              <p className="text-xs font-medium text-destructive mb-1">Errors ({result.errors.length})</p>
              <ul className="space-y-1">
                {result.errors.slice(0, 10).map((err, i) => (
                  <li key={i} className="text-xs text-muted-foreground font-mono">{err}</li>
                ))}
                {result.errors.length > 10 && (
                  <li className="text-xs text-muted-foreground">...and {result.errors.length - 10} more</li>
                )}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
