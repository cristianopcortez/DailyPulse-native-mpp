import XCTest

/// Shared launch wiring for iosAppUITests. Must stay in this target (not the app)
/// because XCUIApplication is configured from the test process.
enum DailyPulseUITestLaunch {

    static func application(scenario: String = "success") -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments = [
            "-ui-testing",
            "-ui-testing-scenario",
            scenario
        ]
        app.launchEnvironment["UI_TESTING"] = "1"
        app.launchEnvironment["UI_TESTING_SCENARIO"] = scenario
        return app
    }
}

extension XCUIApplication {

    /// Finds nodes by label/value/identifier substring so assertions work for
    /// both SwiftUI Text and Compose Multiplatform accessibility trees.
    func elementContaining(_ text: String) -> XCUIElement {
        let predicate = NSPredicate(
            format: "label CONTAINS[c] %@ OR value CONTAINS[c] %@ OR identifier CONTAINS[c] %@",
            text,
            text,
            text
        )
        return descendants(matching: .any).matching(predicate).firstMatch
    }

    @discardableResult
    func waitForText(
        _ text: String,
        timeout: TimeInterval = 15,
        allowSwipe: Bool = false
    ) -> XCUIElement {
        let element = elementContaining(text)
        let initialWait = allowSwipe ? min(4.0, timeout) : timeout
        if element.waitForExistence(timeout: initialWait) {
            return element
        }
        guard allowSwipe else { return element }

        let deadline = Date().addingTimeInterval(timeout)
        while Date() < deadline && !element.exists {
            swipeUp()
            _ = element.waitForExistence(timeout: 1.0)
        }
        return element
    }
}
