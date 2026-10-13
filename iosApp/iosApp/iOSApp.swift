import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        installFirebaseReporting()
        KoinInitIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
