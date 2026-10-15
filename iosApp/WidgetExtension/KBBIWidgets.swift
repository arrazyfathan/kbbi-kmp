import SwiftUI
import WidgetKit
import WidgetCore

private let appGroup = "group.com.arrazyfathan.kbbi"
private let snapshotName = "widget-snapshot.json"

private enum KBBIWidgetKind: String, CaseIterable {
    case wordOfDay = "word-of-day"
    case savedWord = "saved-word"
    case quickSearch = "quick-search"

    var title: String {
        switch self {
        case .wordOfDay: return NSLocalizedString("Word of the day", comment: "Widget name")
        case .savedWord: return NSLocalizedString("Saved word", comment: "Widget name")
        case .quickSearch: return NSLocalizedString("Quick search", comment: "Widget name")
        }
    }
}

private struct SavedWidgetData: Decodable {
    let schemaVersion: Int
    let revision: Int64
    let languageTag: String
    let catalogWords: [String]
    let savedWords: [String]
}

private struct KBBIEntry: TimelineEntry {
    let date: Date
    let kind: KBBIWidgetKind
    let word: String?
    let language: String
}

private struct KBBIProvider: TimelineProvider {
    let kind: KBBIWidgetKind

    func placeholder(in context: Context) -> KBBIEntry {
        KBBIEntry(date: .now, kind: kind, word: "bahasa", language: "id")
    }

    func getSnapshot(in context: Context, completion: @escaping (KBBIEntry) -> Void) {
        completion(entry(for: .now))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<KBBIEntry>) -> Void) {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: .now)
        let entries = (0..<8).compactMap { offset -> KBBIEntry? in
            guard let date = calendar.date(byAdding: .day, value: offset, to: today) else { return nil }
            return entry(for: date)
        }
        let refresh = calendar.date(byAdding: .day, value: 1, to: today) ?? .now.addingTimeInterval(86_400)
        completion(Timeline(entries: entries, policy: .after(refresh)))
    }

    private func entry(for date: Date) -> KBBIEntry {
        let snapshot = loadSnapshot()
        let calendar = Calendar.current
        let year = calendar.component(.year, from: date)
        let day = calendar.ordinality(of: .day, in: .year, for: date) ?? 1
        let words = kind == .savedWord ? (snapshot?.savedWords ?? []) : loadCatalog()
        let selected: String?
        if kind == .wordOfDay {
            selected = WidgetDailyItemSelector.shared.selectWordOfDay(words: words, year: Int32(year), dayOfYear: Int32(day))
        } else if kind == .savedWord {
            selected = WidgetDailyItemSelector.shared.selectSavedWord(words: words, year: Int32(year), dayOfYear: Int32(day))
        } else {
            selected = nil
        }
        return KBBIEntry(date: date, kind: kind, word: selected, language: snapshot?.languageTag ?? Locale.current.language.languageCode?.identifier ?? "en")
    }

    private func loadSnapshot() -> SavedWidgetData? {
        guard let url = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroup)?.appendingPathComponent(snapshotName),
              let data = try? Data(contentsOf: url), data.count <= 2_000_000,
              let snapshot = try? JSONDecoder().decode(SavedWidgetData.self, from: data),
              snapshot.schemaVersion == 1,
              snapshot.catalogWords.count <= 100_000,
              snapshot.savedWords.count <= 100_000,
              (snapshot.catalogWords + snapshot.savedWords).allSatisfy({ word in
                  word.count <= 256 && !word.unicodeScalars.contains(where: CharacterSet.controlCharacters.contains)
              }) else { return nil }
        return snapshot
    }

    private func loadCatalog() -> [String] {
        guard let url = Bundle.main.url(forResource: "entries", withExtension: "json"),
              let data = try? Data(contentsOf: url), data.count <= 2_000_000,
              let words = try? JSONDecoder().decode([String].self, from: data), words.count <= 100_000 else { return [] }
        return words
    }
}

private struct KBBIWidgetView: View {
    let entry: KBBIEntry

    private var deepLink: URL {
        guard let word = entry.word,
              let encoded = word.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed.subtracting(CharacterSet(charactersIn: "/"))),
              let url = URL(string: "kbbi://widget/\(entry.kind.rawValue)/\(encoded)") else {
            return URL(string: "kbbi://widget/\(entry.kind.rawValue)")!
        }
        return url
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(entry.kind.title.uppercased()).font(.caption.weight(.semibold)).opacity(0.78)
            Spacer(minLength: 0)
            if entry.kind == .quickSearch {
                Label(NSLocalizedString("Search KBBI", comment: "Widget action"), systemImage: "magnifyingglass")
                    .font(.title3.weight(.semibold))
            } else if let word = entry.word {
                Text(word).font(.title2.weight(.bold)).lineLimit(2).minimumScaleFactor(0.72)
            } else {
                Text(entry.kind == .savedWord ? NSLocalizedString("No saved words", comment: "Widget empty state") : NSLocalizedString("Word list unavailable", comment: "Widget unavailable state"))
                    .font(.callout)
            }
            Spacer(minLength: 0)
            Text(entry.date.formatted(date: .abbreviated, time: .omitted)).font(.caption2).opacity(0.7)
        }
        .padding()
        .containerBackground(entry.kind == .savedWord ? Color(red: 1, green: 0.94, blue: 0.72) : Color(red: 0.19, green: 0.24, blue: 0.62), for: .widget)
        .foregroundStyle(entry.kind == .savedWord ? Color(red: 0.19, green: 0.15, blue: 0.04) : .white)
        .widgetURL(deepLink)
        .accessibilityElement(children: .combine)
    }
}

private struct KBBIWidget: Widget {
    let kind: KBBIWidgetKind

    init() {
        kind = .wordOfDay
    }

    init(kind: KBBIWidgetKind) {
        self.kind = kind
    }

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "com.arrazyfathan.kbbi.widget.\(kind.rawValue)", provider: KBBIProvider(kind: kind)) { entry in
            KBBIWidgetView(entry: entry)
        }
        .configurationDisplayName(kind.title)
        .description(NSLocalizedString("KBBI glanceable word and search actions", comment: "Widget description"))
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

@main
struct KBBIWidgetBundle: WidgetBundle {
    var body: some Widget {
        KBBIWidget(kind: .wordOfDay)
        KBBIWidget(kind: .savedWord)
        KBBIWidget(kind: .quickSearch)
    }
}
