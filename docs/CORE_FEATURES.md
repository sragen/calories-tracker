# Core Features

Tiga fitur yang selalu ada di setiap produk yang dibangun dari template ini.

---

## 1. User Management

### Tipe User

| Role | Cara Dibuat | Bisa Login Admin Panel | Bisa Login Mobile |
|---|---|---|---|
| `SUPER_ADMIN` | Seed SQL saat deploy | Ya (akses penuh) | Opsional |
| `ADMIN` | Dibuat oleh SUPER_ADMIN | Ya | Opsional |
| `STAFF` | Dibuat oleh ADMIN | Ya (akses terbatas) | Opsional |
| `USER` | Self-register via mobile | Tidak | Ya |

### Status User

| Status | Arti |
|---|---|
| `ACTIVE` | Dapat login dan menggunakan app |
| `INACTIVE` | Akun dinonaktifkan sementara |
| `SUSPENDED` | Akun diblokir karena pelanggaran |

INACTIVE dan SUSPENDED tidak bisa login — API return `403 Forbidden`.

### API User Management

```
# End user self-register (mobile)
POST /api/auth/register
Body: { email, password, name, phone? }

# Login semua tipe user
POST /api/auth/login
Body: { email, password }
Response: { accessToken, refreshToken, expiresIn }

# Refresh token (sebelum access token expired)
POST /api/auth/refresh
Body: { refreshToken }

# Profile user yang sedang login
GET  /api/auth/me
PUT  /api/auth/me   Body: { name, phone, avatarUrl }

# Admin: kelola user (butuh role ADMIN atau SUPER_ADMIN)
GET  /api/admin/users             # list + pagination
POST /api/admin/users             # buat staff/operator
GET  /api/admin/users/{id}
PUT  /api/admin/users/{id}
PUT  /api/admin/users/{id}/status   Body: { status: "SUSPENDED" }
PUT  /api/admin/users/{id}/role     Body: { role: "ADMIN" }  # SUPER_ADMIN only
```

### FCM Token (Push Notification)

Saat user login dari mobile, kirim FCM token ke backend:

```
PUT /api/auth/me
Body: { fcmToken: "firebase-token-string" }
```

Backend menyimpan per user dan menggunakannya untuk kirim push notification.

---

## 2. Admin Management (RBAC)

### Konsep

Setiap endpoint admin dilindungi oleh kombinasi **module** + **action**:
- **Module**: area fitur (USERS, PRODUCTS, ORDERS, CONFIG, REPORTS, …)
- **Action**: READ | WRITE | DELETE

Permission tersimpan di DB — bisa dikonfigurasi tanpa ubah kode.

### Permission Default

| Role | Module | READ | WRITE | DELETE |
|---|---|---|---|---|
| SUPER_ADMIN | Semua modul | ✅ | ✅ | ✅ |
| ADMIN | USERS | ✅ | ✅ | ❌ |
| ADMIN | CONFIG | ✅ | ✅ | ❌ |
| ADMIN | REPORTS | ✅ | ❌ | ❌ |
| STAFF | USERS | ✅ | ❌ | ❌ |
| STAFF | Modul lain | ❌ | ❌ | ❌ |

### Cara Kerja di Backend

Annotation `@RequiresPermission` dicek oleh Spring AOP sebelum method dijalankan:

```kotlin
@GetMapping
@RequiresPermission(module = "USERS", action = READ)   // ADMIN + STAFF bisa
fun listUsers(): Page<UserResponse>

@DeleteMapping("/{id}")
@RequiresPermission(module = "USERS", action = DELETE) // hanya SUPER_ADMIN
fun deleteUser(@PathVariable id: Long)
```

Jika user tidak punya permission → `403 Forbidden` dengan response:
```json
{ "error": "FORBIDDEN", "message": "Insufficient permission: USERS:DELETE" }
```

### Cara Tambah Modul Baru ke Permission System

1. Buat migration SQL:
```sql
-- V5__add_reports_permission.sql
INSERT INTO role_permissions (role, module, can_read, can_write, can_delete) VALUES
    ('SUPER_ADMIN', 'REPORTS', true,  true,  true),
    ('ADMIN',       'REPORTS', true,  false, false),
    ('STAFF',       'REPORTS', false, false, false);
```

2. Gunakan annotation di controller baru:
```kotlin
@RequiresPermission(module = "REPORTS", action = READ)
```

Selesai — tidak ada kode lain yang perlu diubah.

### Cara Kerja di Admin Panel

Sidebar dan tombol aksi otomatis menyesuaikan dengan permission user yang login:

```
SUPER_ADMIN melihat:          STAFF melihat:
├── Dashboard                 ├── Dashboard
├── Users ●                   └── Users (read only)
├── Products ●
├── Orders ●
├── Config ●
└── Reports (read only)

● = bisa create/edit/delete
```

Tombol "Tambah", "Edit", "Hapus" disembunyikan jika user tidak punya permission.

### API RBAC

```
# Roles
GET /api/admin/roles                # list semua role
GET /api/admin/roles/{role}/permissions  # permission per role

# Update permission (SUPER_ADMIN only)
PUT /api/admin/roles/{role}/permissions/{module}
Body: { canRead: true, canWrite: false, canDelete: false }
```

---

## 3. Feature Flag & Remote Config

### Konsep

Admin toggle config dari admin panel → mobile fetch saat launch → fitur aktif/nonaktif tanpa perlu deploy ulang app.

```
Admin Panel                     Mobile App
    │                               │
    │ Toggle "maintenance_mode=ON"  │ Launch app
    │                               │ GET /api/config
    ▼                               ▼
  DB update                    { maintenance_mode: true }
                                    │
                               Tampilkan halaman maintenance
```

### Config yang Tersedia (Default)

| Key | Type | Default | Fungsi |
|---|---|---|---|
| `maintenance_mode` | BOOLEAN | false | Tampilkan halaman maintenance, blokir semua aksi |
| `force_update` | BOOLEAN | false | Paksa user update app sebelum bisa digunakan |
| `min_app_version` | STRING | "1.0.0" | Versi minimum app yang diizinkan |
| `push_notification` | BOOLEAN | true | Toggle fitur push notification di app |
| `promo_banner_url` | STRING | "" | URL gambar banner promo, kosong = tidak tampil |
| `max_retry_login` | NUMBER | 5 | Percobaan login sebelum akun dikunci |

### Cara Tambah Config Baru

Via migration SQL:
```sql
-- V6__add_new_config.sql
INSERT INTO app_configs (key, value, type, label, description) VALUES
    ('checkout_v2_enabled', 'false', 'BOOLEAN',
     'Checkout Flow V2', 'Aktifkan UI checkout yang baru');
```

Config langsung muncul di admin panel toggle list.

### Integrasi Mobile (KMM)

```kotlin
// shared-kmm: AppConfig model
data class AppConfig(
    val maintenanceMode: Boolean,
    val forceUpdate: Boolean,
    val minAppVersion: String,
    val pushNotification: Boolean,
    val promoBannerUrl: String?,
    val maxRetryLogin: Int,
    // Tambah field baru sesuai key yang ditambahkan di DB
)

// ViewModel — panggil di startup
class AppViewModel(private val repo: ConfigRepository) : BaseViewModel() {
    val config = MutableStateFlow<AppConfig?>(null)

    fun loadConfig() = launch {
        repo.fetchConfig().onSuccess { config.value = it }
    }
}

// Root composable — cek config sebelum tampilkan app
@Composable
fun App(appViewModel: AppViewModel = koinViewModel()) {
    val config by appViewModel.config.collectAsState()

    LaunchedEffect(Unit) { appViewModel.loadConfig() }

    when {
        config == null               -> LoadingScreen()
        config!!.maintenanceMode     -> MaintenanceScreen()
        config!!.forceUpdate         -> ForceUpdateScreen(config!!.minAppVersion)
        else                         -> AppNavigation()
    }
}
```

### Cara Pakai Feature Flag di Screen

```kotlin
@Composable
fun HomeScreen(appViewModel: AppViewModel = koinViewModel()) {
    val config by appViewModel.config.collectAsState()

    Column {
        // Banner promo hanya tampil jika URL tidak kosong
        config?.promoBannerUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            PromoBanner(imageUrl = url)
        }

        // Konten utama
        MainContent()
    }
}
```

### API Config

```
# Mobile fetch (public, tidak butuh auth)
GET /api/config
Response: flat JSON semua config aktif

# Admin panel
GET /api/admin/config
Response: list config lengkap dengan label, description, type

PUT /api/admin/config/{key}
Body: { value: "true" }       # untuk BOOLEAN: "true"/"false"
      { value: "2.0.0" }      # untuk STRING
      { value: "10" }         # untuk NUMBER
```
