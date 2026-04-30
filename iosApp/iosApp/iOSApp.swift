import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            // ─────────────────────────────────────────────────────────────
            // Flavor switch:
            // - MPP_UI flag (Other Swift Flags = -D MPP_UI)  -> Compose Multiplatform
            // - default                                     -> Native SwiftUI
            //
            // Define two build configurations / schemes (e.g. "iosApp-MPP"
            // and "iosApp-Native") and add `-D MPP_UI` only to the MPP one.
            // ─────────────────────────────────────────────────────────────
            #if MPP_UI
            ContentView()
            #else
            NativeRootView()
            #endif
        }
    }
}
