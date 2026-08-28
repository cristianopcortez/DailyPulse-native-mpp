import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        Self.configureUiTestingIfNeeded()
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

    /// XCUITest launches the app in a separate process. Launch arguments/environment
    /// are the only way to inject TestBffConfig + in-process GraphQL mocks before UI.
    private static func configureUiTestingIfNeeded() {
        let arguments = ProcessInfo.processInfo.arguments
        let environment = ProcessInfo.processInfo.environment
        let uiTesting = arguments.contains("-ui-testing") || environment["UI_TESTING"] == "1"
        guard uiTesting else { return }

        var scenario = environment["UI_TESTING_SCENARIO"] ?? "success"
        if let index = arguments.firstIndex(of: "-ui-testing-scenario"),
           arguments.indices.contains(index + 1) {
            scenario = arguments[index + 1]
        }

        TestBffConfig.shared.setOverride(url: "http://127.0.0.1:9")
        TestBffConfig.shared.setUiTestScenario(scenario: scenario)
    }
}
