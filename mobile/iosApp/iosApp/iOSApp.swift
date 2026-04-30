import SwiftUI
import composeApp

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.startKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
