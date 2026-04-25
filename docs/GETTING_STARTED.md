# Getting Started

Setup lingkungan development dari nol sampai semua service jalan.

## Prerequisites

| Tool | Versi Minimum | Install |
|---|---|---|
| JDK | 21 | `brew install openjdk@21` |
| Docker Desktop | Latest | [docker.com](https://docker.com) |
| Node.js | 20 LTS | `brew install node` |
| Android Studio | Hedgehog+ | [developer.android.com](https://developer.android.com/studio) |
| Xcode | 15+ | App Store (Mac only) |

## Setup Pertama Kali

### 1. Clone template

```bash
git clone <repo-url> nama-produk-baru
cd nama-produk-baru

# Hapus git history template, mulai fresh
rm -rf .git
git init && git add . && git commit -m "initial: from app-template"
```

### 2. Konfigurasi backend

```bash
cd backend
cp src/main/resources/application-local.yml.example \
   src/main/resources/application-local.yml

# Edit application-local.yml sesuai kebutuhan
# (nama database, JWT secret, dll)
```

### 3. Jalankan infrastruktur

```bash
# Dari root monorepo
docker-compose up -d

# Verifikasi container berjalan
docker-compose ps

# Output yang diharapkan:
# NAME                STATUS
# app-postgres        Up
# app-minio           Up
```

### 4. Jalankan backend

```bash
cd backend
./gradlew bootRun

# Tunggu sampai muncul:
# Started ApplicationKt in X.XXX seconds
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### 5. Jalankan admin panel

```bash
cd admin
npm install
npm run dev

# Admin panel: http://localhost:3000
```

### 6. Jalankan Android

1. Buka Android Studio
2. File → Open → pilih folder `mobile/`
3. Tunggu Gradle sync selesai
4. Pilih `androidApp` di run configuration
5. Jalankan di emulator atau device

### 7. Jalankan iOS (Mac only)

```bash
cd mobile/iosApp
xcodebuild -scheme iosApp -sdk iphonesimulator build

# Atau buka di Xcode:
open iosApp/iosApp.xcodeproj
# Pilih simulator → Cmd+R
```

## Verifikasi Setup

Setelah semua jalan, test endpoint ini:

```bash
# Health check
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123","name":"Admin"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password123"}'
# Expected: {"token":"eyJ..."}
```

## Troubleshooting

### Backend gagal start: "Port 8080 already in use"
```bash
lsof -ti:8080 | xargs kill -9
```

### Database connection error
```bash
# Pastikan PostgreSQL container jalan
docker-compose ps app-postgres

# Restart jika perlu
docker-compose restart app-postgres
```

### Gradle build error di Android Studio
```bash
# Dari terminal
cd mobile
./gradlew clean
./gradlew :androidApp:assembleDebug
```

### iOS: "No such module 'shared'"
```bash
cd mobile
./gradlew :shared-kmm:embedAndSignAppleFrameworkForXcode
# Lalu rebuild di Xcode
```

### Admin panel: "Cannot connect to API"
Pastikan `NEXT_PUBLIC_API_URL` di `admin/config/app.config.ts` mengarah ke backend yang jalan.

## Ports

| Service | Port | URL |
|---|---|---|
| Backend API | 8080 | http://localhost:8080 |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| Admin Panel | 3000 | http://localhost:3000 |
| PostgreSQL | 5432 | localhost:5432 |
| MinIO Console | 9001 | http://localhost:9001 |
| MinIO API | 9000 | http://localhost:9000 |
