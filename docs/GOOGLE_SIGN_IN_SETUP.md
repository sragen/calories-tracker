# Google Sign-In Setup

Backend, Android, and iOS pieces are wired up. To make Google Sign-In actually
work end-to-end you need to provision OAuth client IDs in Google Cloud Console
and drop two values into the project.

## 1. Google Cloud Console

1. Open [console.cloud.google.com](https://console.cloud.google.com), pick (or
   create) the CalSnap project.
2. Enable the **Google Identity / Google Sign-In** API (look up
   "Identity Toolkit API" — it auto-enables the right scopes).
3. Configure the **OAuth consent screen**: app name `CalSnap`, your support
   email, optional logo, and the `email` + `profile` scopes.
4. Create three OAuth 2.0 Client IDs under **APIs & Services → Credentials**:

   | Type | Why we need it | Notes |
   |---|---|---|
   | **Web** | Backend uses this as the `aud` it accepts and Android Credential Manager uses it as the `serverClientId`. | Authorized redirect URIs can stay empty — we only verify ID tokens. |
   | **Android** | Lets the device obtain tokens minted for our app. | Package name `com.company.app`. SHA-1 fingerprint from `./gradlew :androidApp:signingReport` (debug + release). |
   | **iOS**     | Same purpose for the iOS bundle. | Bundle ID `com.adikurniawan.calsnap` (matches `iosApp/iosApp.xcodeproj`). Save the **iOS URL scheme** Google generates — you'll need it for `Info.plist`. |

## 2. Backend (Spring Boot)

Set the comma-separated list of accepted client IDs (Web + Android + iOS — the
backend accepts a token whose `aud` matches *any* entry):

```bash
# /opt/calsnap/.env on the server
APP_AUTH_GOOGLE_CLIENT_IDS=WEB_CLIENT_ID.apps.googleusercontent.com,IOS_CLIENT_ID.apps.googleusercontent.com,ANDROID_CLIENT_ID.apps.googleusercontent.com
```

Then redeploy (`./deploy.sh`). The verifier reads `app.auth.google.client-ids`
via `application.yml` / `SPRING_APPLICATION_*` mapping. Add the property to
`backend/src/main/resources/application.yml`:

```yaml
app:
  auth:
    google:
      client-ids: ${APP_AUTH_GOOGLE_CLIENT_IDS:}
```

## 3. Android

Replace the placeholder in
`mobile/androidApp/src/androidMain/res/values/strings.xml`:

```xml
<string name="google_web_client_id" translatable="false">
    WEB_CLIENT_ID.apps.googleusercontent.com
</string>
```

Use the **Web** client ID (not Android) — Credential Manager mints a token
whose `aud` is the Web client, which is what the backend accepts.

That's it for Android. Credential Manager handles the UI; no extra
permissions or manifest changes are needed.

## 4. iOS

iOS needs the **GoogleSignIn-iOS** SDK and a URL scheme.

### a. Add the SPM package

In Xcode:

- File → Add Package Dependencies… → URL
  `https://github.com/google/GoogleSignIn-iOS`
- Add `GoogleSignIn` product to the `iosApp` target.

### b. Info.plist URL scheme

Open `mobile/iosApp/iosApp/Info.plist` and add:

```xml
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLSchemes</key>
    <array>
      <!-- Reversed iOS client ID, e.g. com.googleusercontent.apps.123-abcdef -->
      <string>YOUR_REVERSED_IOS_CLIENT_ID</string>
    </array>
  </dict>
</array>
<key>GIDClientID</key>
<string>IOS_CLIENT_ID.apps.googleusercontent.com</string>
```

Google's Cloud Console gives you the reversed scheme on the iOS client detail
page — copy it verbatim.

### c. Swift adapter

Add this file to the `iosApp` target (e.g. `iosApp/iosApp/GoogleSignInBridge.swift`):

```swift
import GoogleSignIn
import composeApp
import UIKit

final class GoogleSignInBridgeImpl: NSObject, IosGoogleSignInBridge {
    func signIn(onResult: @escaping (String?, String?) -> Void) {
        guard
            let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
            let root = scene.windows.first?.rootViewController
        else {
            onResult(nil, "No root view controller available")
            return
        }
        GIDSignIn.sharedInstance.signIn(withPresenting: root) { result, error in
            if let error = error {
                let nsErr = error as NSError
                if nsErr.code == GIDSignInError.canceled.rawValue {
                    onResult(nil, "cancelled")
                } else {
                    onResult(nil, error.localizedDescription)
                }
                return
            }
            guard let idToken = result?.user.idToken?.tokenString else {
                onResult(nil, "No ID token returned")
                return
            }
            onResult(idToken, nil)
        }
    }
}
```

### d. Register the bridge at app startup

Edit `iosApp/iosApp/iOSApp.swift`:

```swift
import SwiftUI
import composeApp
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startKoinIos()
        GoogleSignInKt.registerGoogleSignInBridge(b: GoogleSignInBridgeImpl())
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
```

After this the iOS Google button works without any further code changes.

## 5. Verify

- `POST https://api.adikur.com/api/auth/google` with a real ID token from a
  device → expect 200 + `{ accessToken, refreshToken, isNewUser }`.
- Without configured client IDs the backend returns 503
  `GOOGLE_AUTH_NOT_CONFIGURED`. Without configured Android string the button
  surfaces an in-app error and does not crash.
