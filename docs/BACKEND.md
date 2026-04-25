# Backend Guide

Spring Boot 3.x dengan Kotlin. REST API yang auto-generate Swagger docs dan punya generic CRUD pattern.

## Struktur Folder

```
backend/
├── src/main/kotlin/com/company/app/
│   ├── Application.kt
│   │
│   ├── common/                          # Jangan diubah kecuali sangat perlu
│   │   ├── auth/
│   │   │   ├── JwtFilter.kt            # JWT request filter
│   │   │   ├── JwtService.kt           # Token generate/validate
│   │   │   └── AuthController.kt       # POST /api/auth/login, /register, /refresh
│   │   ├── crud/
│   │   │   ├── BaseEntity.kt           # id, createdAt, updatedAt
│   │   │   ├── BaseRepository.kt       # JpaRepository + findAllActive()
│   │   │   ├── BaseService.kt          # findAll, findById, create, update, delete
│   │   │   └── BaseController.kt       # REST endpoints generik
│   │   ├── file/
│   │   │   └── FileController.kt       # POST /api/files/upload
│   │   ├── audit/
│   │   │   └── AuditableEntity.kt      # createdBy, updatedBy, deletedAt
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.kt
│   │       └── AppException.kt
│   │
│   ├── config/
│   │   ├── SecurityConfig.kt           # Spring Security + JWT
│   │   ├── OpenApiConfig.kt            # Swagger UI config
│   │   ├── StorageConfig.kt            # S3/MinIO config
│   │   └── CorsConfig.kt
│   │
│   └── modules/                        # ← AREA KERJA UTAMA PER PRODUK
│       └── user/                       # Contoh module — jadikan referensi
│           ├── User.kt
│           ├── UserDto.kt
│           ├── UserRepository.kt
│           ├── UserService.kt
│           └── UserController.kt
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-local.yml.example   # Copy ke application-local.yml
│   └── db/migration/
│       ├── V1__init.sql                # Users, roles, files table
│       └── V2__example.sql             # Contoh migration
│
├── docker-compose.yml
└── Dockerfile
```

## Pattern: Menambah Domain Module Baru

Contoh: menambah module `Product`.

### 1. Buat Entity

```kotlin
// modules/product/Product.kt
@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column
    var description: String? = null,

    @Column(nullable = false)
    var isActive: Boolean = true
) : BaseEntity()   // ← extends BaseEntity (id, createdAt, updatedAt otomatis)
```

### 2. Buat DTO

```kotlin
// modules/product/ProductDto.kt
data class CreateProductRequest(
    @field:NotBlank val name: String,
    @field:Positive val price: BigDecimal,
    val description: String? = null
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val description: String?,
    val createdAt: LocalDateTime
)
```

### 3. Buat Repository

```kotlin
// modules/product/ProductRepository.kt
@Repository
interface ProductRepository : BaseRepository<Product, Long> {
    // Tambah query custom di sini jika perlu
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>
}
```

### 4. Buat Service

```kotlin
// modules/product/ProductService.kt
@Service
class ProductService(
    private val repository: ProductRepository
) : BaseService<Product, CreateProductRequest>(repository) {

    override fun toEntity(dto: CreateProductRequest) = Product(
        name = dto.name,
        price = dto.price,
        description = dto.description
    )

    override fun updateEntity(entity: Product, dto: CreateProductRequest) {
        entity.name = dto.name
        entity.price = dto.price
        entity.description = dto.description
    }
}
```

### 5. Buat Controller

```kotlin
// modules/product/ProductController.kt
@RestController
@RequestMapping("/api/products")
class ProductController(service: ProductService)
    : BaseController<Product, CreateProductRequest>(service)
// Selesai — GET/POST/PUT/DELETE sudah ada
```

### 6. Buat Migration SQL

```sql
-- db/migration/V3__create_products.sql
CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(15,2) NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMP
);
```

Flyway otomatis menjalankan migration saat `bootRun`.

## Endpoints yang Tersedia (dari BaseController)

| Method | Path | Deskripsi |
|---|---|---|
| `GET` | `/api/{resource}?page=0&size=20` | List dengan pagination |
| `GET` | `/api/{resource}/{id}` | Detail by ID |
| `POST` | `/api/{resource}` | Create baru |
| `PUT` | `/api/{resource}/{id}` | Update by ID |
| `DELETE` | `/api/{resource}/{id}` | Soft delete by ID |

## Auth Endpoints

| Method | Path | Akses | Deskripsi |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | End user daftar mandiri (role=USER) |
| `POST` | `/api/auth/login` | Public | Login → return JWT + refresh token |
| `POST` | `/api/auth/refresh` | Auth | Refresh access token |
| `GET` | `/api/auth/me` | Auth | Profile user yang sedang login |
| `PUT` | `/api/auth/me` | Auth | Update profile sendiri |

## User Management Endpoints

| Method | Path | Akses | Deskripsi |
|---|---|---|---|
| `GET` | `/api/admin/users` | ADMIN+ | List semua user dengan pagination |
| `GET` | `/api/admin/users/{id}` | ADMIN+ | Detail user |
| `POST` | `/api/admin/users` | ADMIN+ | Buat user Staff/Operator baru |
| `PUT` | `/api/admin/users/{id}` | ADMIN+ | Update data user |
| `PUT` | `/api/admin/users/{id}/status` | ADMIN+ | Ubah status (ACTIVE/INACTIVE/SUSPENDED) |
| `PUT` | `/api/admin/users/{id}/role` | SUPER_ADMIN | Ubah role user |

User type dan alur registrasinya:
- **END_USER** → self-register via `/api/auth/register`, role otomatis `USER`
- **STAFF / OPERATOR** → dibuat oleh ADMIN via `/api/admin/users`, tidak bisa self-register
- **SUPER_ADMIN** → di-seed via SQL saat pertama deploy, tidak bisa dibuat via API

## RBAC — Role & Permission

Template menggunakan role-based access dengan permission per modul yang disimpan di DB.

**4 Role default:**

| Role | Deskripsi |
|---|---|
| `SUPER_ADMIN` | Akses penuh semua modul + manajemen role |
| `ADMIN` | Akses semua modul, tidak bisa delete dan ubah role |
| `STAFF` | Read-only ke modul yang diizinkan |
| `USER` | Hanya mobile app, tidak bisa akses admin panel |

**Cara protect endpoint dengan RBAC:**

```kotlin
@RestController
@RequestMapping("/api/admin/products")
class ProductAdminController(val service: ProductService) {

    @GetMapping
    @RequiresPermission(module = "PRODUCTS", action = READ)
    fun list(pageable: Pageable) = service.findAll(pageable)

    @PostMapping
    @RequiresPermission(module = "PRODUCTS", action = WRITE)
    fun create(@RequestBody dto: CreateProductRequest) = service.create(dto)

    @DeleteMapping("/{id}")
    @RequiresPermission(module = "PRODUCTS", action = DELETE)
    fun delete(@PathVariable id: Long) = service.delete(id)
}
```

**Cara daftarkan modul baru ke permission system:**

Cukup insert ke tabel `role_permissions` via Flyway migration:

```sql
-- V4__add_products_permission.sql
INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'PRODUCTS', true,  true,  true),
    ('ADMIN',       'PRODUCTS', true,  true,  false),
    ('STAFF',       'PRODUCTS', true,  false, false);
```

Tidak perlu ubah kode — permission otomatis di-load dari DB.

## Remote Config & Feature Flag Endpoints

| Method | Path | Akses | Deskripsi |
|---|---|---|---|
| `GET` | `/api/config` | Public | Semua config aktif (untuk mobile fetch saat launch) |
| `GET` | `/api/admin/config` | ADMIN+ | List semua config termasuk yang nonaktif |
| `PUT` | `/api/admin/config/{key}` | ADMIN+ | Update value atau toggle on/off |

**Response `/api/config` (yang di-fetch mobile):**

```json
{
  "maintenance_mode": false,
  "min_app_version": "1.0.0",
  "force_update": false,
  "push_notification": true,
  "promo_banner_url": "https://cdn.example.com/promo.jpg",
  "max_retry_login": 5
}
```

Config default tersedia di `V2__seed_app_configs.sql`. Tambah config baru via migration SQL atau langsung dari admin panel.

## File Upload

```bash
# Upload file
POST /api/files/upload
Content-Type: multipart/form-data

# Response
{ "url": "https://storage.example.com/files/uuid.jpg" }
```

## Konfigurasi per Produk

Edit `application-local.yml` (tidak di-commit):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nama_produk_db
    username: postgres
    password: postgres

app:
  name: "Nama Produk Anda"
  jwt:
    secret: "ganti-dengan-secret-panjang-dan-aman"
    expiration-ms: 86400000   # 24 jam

storage:
  type: minio                 # minio | s3
  endpoint: http://localhost:9000
  bucket: nama-produk-files
```

## Swagger UI

Otomatis tersedia di `http://localhost:8080/swagger-ui.html` setelah `bootRun`.
Semua endpoint ter-dokumentasi tanpa setup tambahan.

## Menjalankan Tests

```bash
./gradlew test                    # Unit + integration tests
./gradlew test --tests "*.UserServiceTest"  # Test spesifik
```
