import UIKit
import UniformTypeIdentifiers

private let appGroup = "group.com.arrazyfathan.kbbi"

final class ShareViewController: UIViewController {
    private let titleLabel = UILabel()
    private let termLabel = UILabel()
    private let resultView = UITextView()
    private let statusLabel = UILabel()
    private let retryButton = UIButton(type: .system)
    private let keepButton = UIButton(type: .system)
    private var selectedText = ""
    private var lookupTask: URLSessionDataTask?
    private var lookupWord = ""

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        buildView()
        loadSharedText()
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        lookupTask?.cancel()
    }

    private func buildView() {
        titleLabel.text = "KBBI"
        titleLabel.font = .preferredFont(forTextStyle: .largeTitle).withWeight(.bold)
        termLabel.font = .preferredFont(forTextStyle: .title2).withWeight(.semibold)
        resultView.font = .preferredFont(forTextStyle: .body)
        resultView.isEditable = false
        resultView.layer.cornerRadius = 12
        resultView.backgroundColor = .secondarySystemBackground
        statusLabel.font = .preferredFont(forTextStyle: .footnote)
        statusLabel.textColor = .secondaryLabel
        keepButton.setTitle("Keep for next KBBI opening", for: .normal)
        keepButton.titleLabel?.font = .preferredFont(forTextStyle: .headline)
        keepButton.addTarget(self, action: #selector(keepForNextOpen), for: .touchUpInside)

        retryButton.setTitle("Try again", for: .normal)
        retryButton.isHidden = true
        retryButton.addTarget(self, action: #selector(retryLookup), for: .touchUpInside)
        let stack = UIStackView(arrangedSubviews: [titleLabel, termLabel, statusLabel, resultView, retryButton, keepButton])
        stack.axis = .vertical
        stack.spacing = 14
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 20),
            stack.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -20),
            stack.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 18),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -18),
            resultView.heightAnchor.constraint(greaterThanOrEqualToConstant: 180)
        ])
    }

    private func loadSharedText() {
        let items = extensionContext?.inputItems as? [NSExtensionItem] ?? []
        guard let provider = items.flatMap({ $0.attachments ?? [] }).first(where: { $0.hasItemConformingToTypeIdentifier(UTType.plainText.identifier) }) else {
            statusLabel.text = "Share plain text to look up a word."
            keepButton.isEnabled = false
            return
        }
        provider.loadItem(forTypeIdentifier: UTType.plainText.identifier, options: nil) { [weak self] item, _ in
            let text = (item as? String) ?? (item as? NSAttributedString)?.string ?? ""
            DispatchQueue.main.async {
                guard let self else { return }
                self.selectedText = String(text.prefix(4096))
                self.lookupWord = Self.firstWord(self.selectedText)
                guard !self.lookupWord.isEmpty else {
                    self.statusLabel.text = "No searchable word was found."
                    self.keepButton.isEnabled = false
                    return
                }
                self.termLabel.text = self.lookupWord
                self.lookup()
            }
        }
    }

    private func lookup() {
        guard let sharedDefaults = UserDefaults(suiteName: appGroup),
              let base = sharedDefaults.string(forKey: "apiBaseUrl"),
              var components = URLComponents(string: base) else {
            statusLabel.text = "Open KBBI once before using the share extension."
            return
        }
        let cache = UserDefaults.standard
        let cacheKey = "lookup.\(lookupWord)"
        if let cached = cache.string(forKey: cacheKey) {
            recordCachedWord(lookupWord, in: cache)
            resultView.text = cached
            statusLabel.text = "Saved lookup"
            return
        }
        var pathComponentSet = CharacterSet.urlPathAllowed
        pathComponentSet.remove(charactersIn: "/?#%")
        let encodedWord = lookupWord.addingPercentEncoding(withAllowedCharacters: pathComponentSet) ?? ""
        guard !encodedWord.isEmpty else { statusLabel.text = "The lookup service is unavailable."; return }
        components.percentEncodedPath = "/" + components.path.trimmingCharacters(in: CharacterSet(charactersIn: "/")) + "/search/\(encodedWord)"
        guard let url = components.url else {
            statusLabel.text = "The lookup service is unavailable."
            return
        }
        statusLabel.text = "Looking up…"
        var request = URLRequest(url: url, timeoutInterval: 15)
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        let visitor = sharedDefaults.string(forKey: "shareVisitorId") ?? UUID().uuidString.lowercased()
        sharedDefaults.set(visitor, forKey: "shareVisitorId")
        request.setValue(visitor, forHTTPHeaderField: "x-visitor-id")
        lookupTask = URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            let result = Self.decodeLookup(data: data, response: response, error: error)
            DispatchQueue.main.async {
                guard let self else { return }
                self.statusLabel.text = result.status
                self.resultView.text = result.text
                self.retryButton.isHidden = result.status == "Found" || result.status == "Not found"
                if result.status == "Found" {
                    cache.set(result.text, forKey: cacheKey)
                    self.recordCachedWord(self.lookupWord, in: cache)
                }
            }
        }
        lookupTask?.resume()
    }

    private static func decodeLookup(data: Data?, response: URLResponse?, error: Error?) -> (status: String, text: String) {
        guard error == nil, let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode),
              let data, data.count <= 512 * 1024, let root = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              root["success"] as? Bool == true, let payload = root["data"] as? [String: Any],
              let entries = payload["entries"] as? [[String: Any]], !entries.isEmpty else {
            if let http = response as? HTTPURLResponse, http.statusCode == 404 { return ("Not found", "No dictionary entry was found for this word.") }
            return ("Lookup failed", "Check your connection and try again.")
        }
        let definitions = entries.flatMap { entry -> [String] in
            let headword = entry["headword"] as? String ?? ""
            let meanings = entry["definitions"] as? [[String: Any]] ?? []
            return meanings.compactMap { meaning in
                guard let description = meaning["description"] as? String else { return nil }
                let wordClass = meaning["wordClass"] as? String ?? ""
                return "\(headword)\(wordClass.isEmpty ? "" : " (\(wordClass))")\n\(description)"
            }
        }
        return definitions.isEmpty ? ("Not found", "No dictionary entry was found for this word.") : ("Found", definitions.joined(separator: "\n\n"))
    }

    private func recordCachedWord(_ word: String, in cache: UserDefaults) {
        var words = cache.stringArray(forKey: "lookupCacheWords") ?? []
        words.removeAll { $0 == word }
        words.append(word)
        while words.count > 20 {
            cache.removeObject(forKey: "lookup.\(words.removeFirst())")
        }
        cache.set(words, forKey: "lookupCacheWords")
    }

    private static func firstWord(_ text: String) -> String {
        guard text.count <= 4096 else { return "" }
        let candidate = text.trimmingCharacters(in: .whitespacesAndNewlines).split(whereSeparator: \.isWhitespace).first.map(String.init) ?? ""
        let allowed = CharacterSet.letters.union(.decimalDigits)
        let scalars = Array(candidate.unicodeScalars)
        let start = scalars.prefix { !allowed.contains($0) && $0 != "-" }.count
        let end = scalars.reversed().prefix { !allowed.contains($0) && $0 != "-" }.count
        let word = String(String.UnicodeScalarView(scalars.dropFirst(start).dropLast(end))).lowercased()
        guard word.count <= 256, !word.isEmpty, word != ".", word != "..", word.rangeOfCharacter(from: CharacterSet(charactersIn: "/\\?#")) == nil else { return "" }
        return word
    }

    @objc private func keepForNextOpen() {
        guard let defaults = UserDefaults(suiteName: appGroup), !selectedText.isEmpty else { return }
        defaults.set(selectedText, forKey: "pendingShareText")
        defaults.set(UUID().uuidString, forKey: "pendingShareId")
        statusLabel.text = "This lookup will open when you next open KBBI."
        extensionContext?.completeRequest(returningItems: nil)
    }

    @objc private func retryLookup() {
        lookupTask?.cancel()
        lookup()
    }
}

private extension UIFont {
    func withWeight(_ weight: UIFont.Weight) -> UIFont {
        UIFont.systemFont(ofSize: pointSize, weight: weight)
    }
}
