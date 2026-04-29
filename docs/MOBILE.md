# Mobile Guide

Kotlin Multiplatform (KMM) + Compose Multiplatform. Satu codebase untuk Android dan iOS.

## UI Design Docs

| Document | Description |
|---|---|
| [MOBILE_UI_DESIGN.md](./MOBILE_UI_DESIGN.md) | Full design spec — tokens, components, screens, animations, haptics |
| [MOBILE_UI_PHASES.md](./MOBILE_UI_PHASES.md) | Phased implementation plan — Phase 0–7 with BE gaps and acceptance criteria |

## Struktur Folder

```
mobile/
├── shared-kmm/                          # Kotlin Multiplatform module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/company/app/
│       │   ├── data/
│       │   │   ├── remote/
│       │   │   │   ├── ApiClient.kt     # Ktor HttpClient (configured)
│       │   │   │   └── ApiService.kt    # Interface endpoint API
│       │   │   ├── local/
│       │   │   │   └── AppDatabase.kt   # SQLDelight (offline cache)
│       │   │   └── repository/
│       │   │       └── BaseRepository.kt
│       │   ├── domain/
│       │   │   ├── model/               # Data models (shared)
│       │   │   │   └── User.kt          # Contoh model
│       │   │   └── usecase/             # Business logic (platform-agnostic)
│       │   └── presentation/
│       │       └── BaseViewModel.kt     # StateFlow-based ViewModel
│       │
│       ├── androidMain/kotlin/          # Android-specific implementations
│       └── iosMain/kotlin/              # iOS-specific implementations
│
├── composeApp/                          # Compose Multiplatform shared UI
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/com/company/app/
│       ├── theme/
│       │   ├── AppTheme.kt              # MaterialTheme wrapper
│       │   ├── Color.kt                 # ← GANTI WARNA PER PRODUK
│       │   └── Typography.kt
│       ├── navigation/
│       │   └── AppNavigation.kt         # NavHost + routes
│       ├── screens/
│       │   ├── auth/
│       │   │   └── LoginScreen.kt       # Contoh screen — clone pattern ini
│       │   ├── home/
│       │   │   └── HomeScreen.kt
│       │   └── common/
│       │       ├── LoadingScreen.kt
│       │       └── ErrorScreen.kt
│       └── components/                  # Reusable UI components
│           ├── AppButton.kt
│           ├── AppTextField.kt
│           └── AppTopBar.kt
│
├── androidApp/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/com/company/app/
│           └── MainActivity.kt          # Entry point Android
│
└── iosApp/
    └── iosApp/
        ├── ContentView.swift            # Entry point iOS
        └── Info.plist
```

## Pattern: Menambah Screen Baru

Contoh: menambah screen `ProductListScreen`.

### 1. Tambah model di shared-kmm

```kotlin
// shared-kmm/src/commonMain/.../domain/model/Product.kt
data class Product(
    val id: Long,
    val name: String,
    val price: Double,
    val imageUrl: String?
)
```

### 2. Tambah endpoint di ApiService

```kotlin
// shared-kmm/src/commonMain/.../data/remote/ApiService.kt
interface ApiService {
    suspend fun getProducts(page: Int = 0, size: Int = 20): ApiResponse<List<Product>>
    suspend fun getProductById(id: Long): Product
}
```

### 3. Buat Repository

```kotlin
// shared-kmm/src/commonMain/.../data/repository/ProductRepository.kt
class ProductRepository(private val api: ApiService) {
    suspend fun getProducts(): Result<List<Product>> = runCatching {
        api.getProducts().data
    }
}
```

### 4. Buat ViewModel (di shared-kmm)

```kotlin
// shared-kmm/src/commonMain/.../presentation/ProductViewModel.kt
class ProductViewModel(
    private val repository: ProductRepository
) : BaseViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProducts() {
        launch {
            _isLoading.value = true
            repository.getProducts()
                .onSuccess { _products.value = it }
                .onFailure { /* handle error */ }
            _isLoading.value = false
        }
    }
}
```

### 5. Buat Screen di composeApp

```kotlin
// composeApp/src/commonMain/.../screens/product/ProductListScreen.kt
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = koinViewModel(),
    onProductClick: (Long) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProducts() }

    if (isLoading) {
        LoadingScreen()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product = product, onClick = { onProductClick(product.id) })
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Rp ${product.price.toLong()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

### 6. Tambah route di Navigation

```kotlin
// composeApp/src/commonMain/.../navigation/AppNavigation.kt
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home")          { HomeScreen(navController) }
        composable("products")      { ProductListScreen(onProductClick = { id ->
            navController.navigate("products/$id")
        }) }
        composable("products/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLong() ?: return@composable
            ProductDetailScreen(productId = id)
        }
    }
}
```

## Theming per Produk

Ganti branding hanya di 2 file:

```kotlin
// composeApp/src/commonMain/.../theme/Color.kt
val PrimaryColor = Color(0xFF1976D2)        // ← Ganti warna brand
val SecondaryColor = Color(0xFF0D47A1)
val BackgroundColor = Color(0xFFFFFFFF)
val SurfaceColor = Color(0xFFF5F5F5)
```

```kotlin
// composeApp/src/commonMain/.../theme/AppTheme.kt
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            background = BackgroundColor,
            surface = SurfaceColor,
        ),
        typography = AppTypography,   // ← Definisikan font di Typography.kt
        content = content
    )
}
```

## Dependency Injection (Koin)

```kotlin
// shared-kmm: module DI
val appModule = module {
    single { ApiClient.create() }
    single { ApiServiceImpl(get()) }
    single { ProductRepository(get()) }
    viewModel { ProductViewModel(get()) }
}

// androidApp: MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin { modules(appModule) }
        setContent { AppTheme { AppNavigation() } }
    }
}
```

## Build

```bash
# Android APK
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:assembleRelease

# iOS (dari Xcode atau command line)
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator

# Shared KMM module saja
./gradlew :shared-kmm:build
```

## Catatan iOS Setup

1. Buka `iosApp/iosApp.xcodeproj` di Xcode
2. Ganti Bundle Identifier: `com.company.namaapp`
3. Set signing team di Signing & Capabilities
4. Jalankan di simulator atau device

KMM framework otomatis di-build dan di-link ke Xcode project via `embedAndSignAppleFrameworkForXcode` Gradle task.
