import XCTest

/// UI tests for ArticlesScreen, equivalent to Android ArticlesScreenTest / ArticlesScreenErrorTest.
///
/// The app process is started with `-ui-testing` so iOSApp.swift injects TestBffConfig +
/// an in-process GraphQL MockEngine before Koin (XCUITest cannot call Kotlin from this process).
final class ArticlesScreenUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func test_articlesScreen_displaysArticlesFromMock() throws {
        let app = DailyPulseUITestLaunch.application(scenario: "success")
        app.launch()

        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 20))

        let screen = app.descendants(matching: .any)["articles_screen"]
        XCTAssertTrue(
            screen.waitForExistence(timeout: 10) || app.staticTexts["Articles"].waitForExistence(timeout: 10),
            "Articles screen (identifier or title) should appear."
        )

        let techGiants = app.waitForText("Tech Giants", timeout: 15)
        XCTAssertTrue(techGiants.exists, "Primary mocked article title should be visible.")

        let accuracy = app.waitForText("95% accuracy", timeout: 10)
        XCTAssertTrue(accuracy.exists, "Primary mocked article description should be visible.")

        attachScreenshot(named: "Articles_List_Loaded")
    }

    func test_articlesScreen_revealsSecondaryArticleWithScrollTolerance() throws {
        let app = DailyPulseUITestLaunch.application(scenario: "success")
        app.launch()

        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 20))

        let quantum = app.waitForText("Quantum Computing", timeout: 18, allowSwipe: true)
        XCTAssertTrue(
            quantum.exists,
            "Secondary mocked article should exist after scrolling (simulator layout tolerant)."
        )

        attachScreenshot(named: "Articles_Secondary_Visible")
    }

    func test_articlesScreen_displaysBackendError() throws {
        let app = DailyPulseUITestLaunch.application(scenario: "error")
        app.launch()

        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 20))

        let error = app.waitForText("error", timeout: 15)
        XCTAssertTrue(
            error.exists || app.descendants(matching: .any)["articles_error"].waitForExistence(timeout: 5),
            "Backend failure should surface an error message."
        )

        attachScreenshot(named: "Articles_Backend_Error")
    }

    private func attachScreenshot(named name: String) {
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
