import AppIntents
import Foundation

struct KBBISearchIntent: AppIntent {
    static var title: LocalizedStringResource = "Search KBBI"
    static var openAppWhenRun = true

    func perform() async throws -> some IntentResult {
        .result(opensIntent: OpenURLIntent(URL(string: "kbbi://action/search")!))
    }
}

struct KBBIRandomWordIntent: AppIntent {
    static var title: LocalizedStringResource = "Open a random word"
    static var openAppWhenRun = true

    func perform() async throws -> some IntentResult {
        .result(opensIntent: OpenURLIntent(URL(string: "kbbi://action/random")!))
    }
}

struct KBBIBookmarksIntent: AppIntent {
    static var title: LocalizedStringResource = "Open saved words"
    static var openAppWhenRun = true

    func perform() async throws -> some IntentResult {
        .result(opensIntent: OpenURLIntent(URL(string: "kbbi://bookmarks")!))
    }
}

struct KBBIProverbsIntent: AppIntent {
    static var title: LocalizedStringResource = "Open proverbs"
    static var openAppWhenRun = true

    func perform() async throws -> some IntentResult {
        .result(opensIntent: OpenURLIntent(URL(string: "kbbi://proverb")!))
    }
}

struct KBBIAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        return [
            AppShortcut(intent: KBBISearchIntent(), phrases: ["Search in \(.applicationName)"], shortTitle: "Search", systemImageName: "magnifyingglass"),
            AppShortcut(intent: KBBIBookmarksIntent(), phrases: ["Open saved words in \(.applicationName)"], shortTitle: "Saved words", systemImageName: "bookmark"),
            AppShortcut(intent: KBBIProverbsIntent(), phrases: ["Open proverbs in \(.applicationName)"], shortTitle: "Proverbs", systemImageName: "text.book.closed"),
            AppShortcut(intent: KBBIRandomWordIntent(), phrases: ["Open a random word in \(.applicationName)"], shortTitle: "Random word", systemImageName: "shuffle")
        ]
    }
}
