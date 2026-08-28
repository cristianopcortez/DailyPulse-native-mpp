import XCTest

/// Lightweight XCUITest smoke for CI. Uses the same mock launch flags as ArticlesScreenUITests
/// so the simulator never depends on a live BFF.
final class iosAppUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func test_appLaunchesAndReachesForeground() throws {
        let app = DailyPulseUITestLaunch.application(scenario: "success")
        app.launch()

        XCTAssertTrue(
            app.wait(for: .runningForeground, timeout: 20),
            "App should reach foreground without crashing."
        )

        let screenshot = XCUIScreen.main.screenshot()
        let attachment = XCTAttachment(screenshot: screenshot)
        attachment.name = "App_Launched_Successfully"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
