import XCTest

/// Smoke tests for CI (Codemagic / simulator). Extend with `@testable import` and real cases when needed.
final class iosAppTests: XCTestCase {

    func test_ciPipelinePlaceholder() {
        XCTAssertTrue(true, "Keeps the iosAppTests target non-empty for xcodebuild test")
    }
}
