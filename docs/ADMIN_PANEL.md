# Admin Panel Guide

Next.js 15 dengan Shadcn/UI dan Tanstack Table. Admin panel developer-facing untuk manage data produk via CRUD.

## Struktur Folder

```
admin/
├── src/
│   ├── app/
│   │   ├── layout.tsx                   # Root layout
│   │   ├── (auth)/
│   │   │   └── login/
│   │   │       └── page.tsx             # Halaman login
│   │   └── (dashboard)/
│   │       ├── layout.tsx               # Sidebar + Header
│   │       ├── page.tsx                 # Dashboard home
│   │       └── users/                   # Contoh CRUD — jadikan referensi
│   │           ├── columns.tsx          # Definisi kolom tabel
│   │           ├── page.tsx             # List page
│   │           ├── create/page.tsx      # Form tambah
│   │           └── [id]/
│   │               └── edit/page.tsx    # Form edit
│   │
│   ├── components/
│   │   ├── ui/                          # Shadcn components (jangan edit manual)
│   │   ├── data-table/
│   │   │   ├── DataTable.tsx            # Reusable table component
│   │   │   ├── DataTableToolbar.tsx     # Search + filter bar
│   │   │   ├── DataTablePagination.tsx  # Pagination
│   │   │   └── DataTableRowActions.tsx  # Dropdown aksi per baris
│   │   ├── forms/
│   │   │   └── FormField.tsx            # Labeled input wrapper
│   │   └── layout/
│   │       ├── AppSidebar.tsx           # Sidebar navigasi
│   │       └── AppHeader.tsx            # Header + user dropdown
│   │
│   ├── hooks/
│   │   └── use-resource.ts              # Generic CRUD hook
│   │
│   ├── lib/
│   │   ├── api-client.ts                # Axios instance
│   │   └── utils.ts
│   │
│   ├── config/
│   │   ├── app.config.ts                # ← GANTI INI PER PRODUK BARU
│   │   └── nav.config.ts                # ← DEFINISI MENU SIDEBAR
│   │
│   └── types/
│       └── api.ts                       # ApiResponse<T>, Pageable, dll
│
└── package.json
```

## Pattern: Menambah CRUD Baru

Contoh: menambah halaman CRUD untuk `Product`.

### 1. Tambah menu di sidebar

```typescript
// config/nav.config.ts
import { Package, Users } from "lucide-react"

export const navItems = [
  { label: "Users",    href: "/users",    icon: Users    },
  { label: "Products", href: "/products", icon: Package  }, // ← tambah ini
]
```

### 2. Definisi kolom tabel

```typescript
// app/(dashboard)/products/columns.tsx
import { ColumnDef } from "@tanstack/react-table"
import { DataTableRowActions } from "@/components/data-table/DataTableRowActions"

export type Product = {
  id: number
  name: string
  price: number
  createdAt: string
}

export const columns: ColumnDef<Product>[] = [
  {
    accessorKey: "name",
    header: "Nama Produk",
  },
  {
    accessorKey: "price",
    header: "Harga",
    cell: ({ row }) => `Rp ${row.getValue<number>("price").toLocaleString("id")}`,
  },
  {
    accessorKey: "createdAt",
    header: "Dibuat",
    cell: ({ row }) => new Date(row.getValue("createdAt")).toLocaleDateString("id"),
  },
  {
    id: "actions",
    cell: ({ row }) => (
      <DataTableRowActions
        row={row}
        resource="products"     // ← nama resource = path API + path halaman
      />
    ),
  },
]
```

### 3. Halaman list

```typescript
// app/(dashboard)/products/page.tsx
"use client"
import { DataTable } from "@/components/data-table/DataTable"
import { columns } from "./columns"
import { useResource } from "@/hooks/use-resource"

export default function ProductsPage() {
  const { data, isLoading } = useResource<Product[]>("products")

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Products</h1>
      </div>
      <DataTable columns={columns} data={data ?? []} isLoading={isLoading} />
    </div>
  )
}
```

### 4. Form create/edit

```typescript
// app/(dashboard)/products/create/page.tsx
"use client"
import { useResource } from "@/hooks/use-resource"
import { FormField } from "@/components/forms/FormField"
import { Button } from "@/components/ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"

const schema = z.object({
  name:  z.string().min(1, "Nama wajib diisi"),
  price: z.number().positive("Harga harus lebih dari 0"),
})
type FormData = z.infer<typeof schema>

export default function CreateProductPage() {
  const { create } = useResource("products")
  const form = useForm<FormData>({ resolver: zodResolver(schema) })

  return (
    <form onSubmit={form.handleSubmit(create)} className="space-y-4 max-w-md">
      <FormField label="Nama Produk" error={form.formState.errors.name?.message}>
        <input {...form.register("name")} className="input" />
      </FormField>
      <FormField label="Harga" error={form.formState.errors.price?.message}>
        <input {...form.register("price", { valueAsNumber: true })} type="number" className="input" />
      </FormField>
      <Button type="submit">Simpan</Button>
    </form>
  )
}
```

Halaman edit (`[id]/edit/page.tsx`) identik, bedanya menggunakan `update` dari `useResource`.

## useResource Hook

Generic hook untuk semua operasi CRUD ke backend:

```typescript
// hooks/use-resource.ts
const { 
  data,      // data dari API
  isLoading, // loading state
  create,    // POST /api/{resource}
  update,    // PUT /api/{resource}/{id}
  remove,    // DELETE /api/{resource}/{id}
} = useResource<Product>("products")
```

Hook ini otomatis:
- Attach JWT token ke setiap request
- Handle error response dari backend
- Refresh data setelah create/update/delete
- Pagination via query params

## Konfigurasi per Produk

```typescript
// config/app.config.ts  ← ganti ini saat clone
export const appConfig = {
  name: "Nama Produk Anda",
  apiUrl: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  logoUrl: "/logo.svg",               // taruh di public/logo.svg
}
```

## Menambah Shadcn Component Baru

```bash
npx shadcn@latest add dialog
npx shadcn@latest add select
npx shadcn@latest add badge
```

Component akan di-copy ke `src/components/ui/` — tidak ada runtime dependency ke shadcn.

## Build & Deploy

```bash
npm run dev        # Development (localhost:3000)
npm run build      # Build production
npm run start      # Jalankan production build

# Deploy ke Vercel
vercel --prod

# Atau export static
npm run build && npm run export
# Upload folder out/ ke S3/Nginx
```
