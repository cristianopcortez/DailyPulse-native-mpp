import XCTest

/// Lightweight XCUITest smoke for CI: no text/button queries (works for both CMP and SwiftUI entry points).
final class iosAppUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func test_appLaunchesAndReachesForeground() throws {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(
            app.wait(for: .runningForeground, timeout: 20),
            "App should reach foreground without crashing."
        )
    }
}
