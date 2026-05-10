import SwiftUI
import composeApp

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            // .container ignores notch + home indicator; keyboard insets still apply.
            ContentView()
                .ignoresSafeArea(.container, edges: .all)
        }
    }
}
