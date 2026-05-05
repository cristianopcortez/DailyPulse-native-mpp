import XCTest

/// Lightweight XCUITest smoke for CI: no text/button queries (works for both CMP and SwiftUI entry points).
final class iosAppUITests: XCTestCase {

    override func setUpWithError() throws {
        // Stop the test immediately if a failure occurs.
        continueAfterFailure = false
    }

    func test_appLaunchesAndReachesForeground() throws {
        let app = XCUIApplication()
        app.launch()

        // 1. Verify if the application successfully reached the foreground.
        XCTAssertTrue(
            app.wait(for: .runningForeground, timeout: 20),
            "App should reach foreground without crashing."
        )

        // 2. Capture a screenshot of the main screen.
        let screenshot = XCUIScreen.main.screenshot()

        // 3. Create an attachment to store the screenshot in the .xcresult bundle.
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = "App_Launched_Successfully"

        // CRITICAL: .keepAlways ensures the attachment is preserved even if the test passes.
        // By default, Xcode deletes attachments for successful tests to save space.
        attachment.lifetime = .keepAlways

        add(attachment)
    }
}
