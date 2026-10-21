import FirebaseCore
import FirebaseMessaging
import Foundation
import Security
import UIKit
import UserNotifications
import Shared

@MainActor
final class IosCampaignCoordinator {
    static let shared = IosCampaignCoordinator()
    static let reconcileNotification = Notification.Name("com.arrazyfathan.kbbi.campaigns.reconcile")

    private var observer: NSObjectProtocol?
    private var reconciliation: Task<Void, Never>?
    private var reconcileAgain = false

    private init() {}

    func install() {
        observer = NotificationCenter.default.addObserver(
            forName: Self.reconcileNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            Task { @MainActor in await self?.reconcile() }
        }
        Task { await reconcile() }
    }

    func didUpdateFCMToken(_ token: String?) {
        guard let token, !token.isEmpty else { return }
        UserDefaults.standard.set(token, forKey: "kbbi.push.fcmToken")
        Task { await reconcile() }
    }

    func reconcile() async {
        if reconciliation != nil {
            reconcileAgain = true
            return
        }
        let task = Task { @MainActor in
            defer {
                reconciliation = nil
                if reconcileAgain {
                    reconcileAgain = false
                    Task { await reconcile() }
                }
            }
            guard FirebaseApp.app() != nil else { return }
            let settings = UserDefaults.standard
            let campaignsEnabled = settings.bool(forKey: "kbbi.ui.campaign_notifications")
            let updatesEnabled = (settings.object(forKey: "kbbi.ui.update_notifications") as? Bool) ?? true
            let identity = InstallationCredentials.loadOrCreate()
            guard campaignsEnabled || updatesEnabled || identity.registered else { return }
            let authorization = await UNUserNotificationCenter.current().notificationSettings()
            let authorized = authorization.authorizationStatus == .authorized || authorization.authorizationStatus == .provisional
            if authorized && (campaignsEnabled || updatesEnabled) { UIApplication.shared.registerForRemoteNotifications() }
            do {
                guard let token = settings.string(forKey: "kbbi.push.fcmToken"), !token.isEmpty else { return }
                for attempt in 0..<3 {
                    do {
                        try await register(
                            token: token,
                            campaignsEnabled: campaignsEnabled && authorized,
                            updatesEnabled: updatesEnabled && authorized,
                            credentials: identity
                        )
                        return
                    } catch {
                        if attempt == 2 { throw error }
                        try? await Task.sleep(nanoseconds: UInt64(1 << attempt) * 1_000_000_000)
                    }
                }
            } catch {
                NSLog("KBBI push installation reconciliation failed: %@", String(describing: error))
            }
        }
        reconciliation = task
        await task.value
    }

    private func register(token: String, campaignsEnabled: Bool, updatesEnabled: Bool, credentials: InstallationCredentials) async throws {
        guard let base = URL(string: ExternalEntryConfigurationKt.getShareExtensionApiBaseUrl()),
              let url = URL(string: "/api/v1/push/installations", relativeTo: base)?.absoluteURL else { return }
        let defaults = UserDefaults.standard
        let revision = max(0, defaults.integer(forKey: "kbbi.push.revision")) + 1
        defaults.set(revision, forKey: "kbbi.push.revision")
        let environment = (Bundle.main.object(forInfoDictionaryKey: "KBBIEnvironment") as? String) ?? "development"
        let language = (defaults.string(forKey: "kbbi.ui.language") ?? Locale.preferredLanguages.first ?? "en").lowercased()
        let locale = language.hasPrefix("id") ? "id" : "en"
        let appVersion = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0.0.0"
        let versionCode = Int(Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "0") ?? 1
        let body: [String: Any] = [
            "id": credentials.id,
            "secret": credentials.secret,
            "fcm_token": token,
            "environment": environment,
            "platform": "ios",
            "locale": locale,
            "app_version": appVersion,
            "version_code": max(1, versionCode),
            "categories": ["campaigns": campaignsEnabled, "updates": updatesEnabled],
            "revision": revision,
        ]
        var request = URLRequest(url: url)
        request.httpMethod = credentials.registered ? "PUT" : "POST"
        if credentials.registered { request.url = url.appendingPathComponent(credentials.id) }
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        var (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw URLError(.badServerResponse) }
        // A lost response after a successful POST is recovered by a secret-authenticated PUT.
        if http.statusCode == 409 && !credentials.registered {
            request.httpMethod = "PUT"
            request.url = url.appendingPathComponent(credentials.id)
            (data, response) = try await URLSession.shared.data(for: request)
            guard let retry = response as? HTTPURLResponse, (200..<300).contains(retry.statusCode) else {
                throw URLError(.badServerResponse)
            }
            InstallationCredentials.markRegistered()
            return
        }
        guard (200..<300).contains(http.statusCode) else {
            let description = String(data: data.prefix(512), encoding: .utf8) ?? ""
            throw NSError(domain: "KBBI.Push", code: http.statusCode, userInfo: [NSLocalizedDescriptionKey: description])
        }
        InstallationCredentials.markRegistered()
    }
}

private struct InstallationCredentials {
    let id: String
    let secret: String
    let registered: Bool

    static func loadOrCreate() -> InstallationCredentials {
        if let data = read(),
           let values = try? JSONSerialization.jsonObject(with: data) as? [String: String],
           let id = values["id"], let secret = values["secret"] {
            return InstallationCredentials(id: id, secret: secret, registered: UserDefaults.standard.bool(forKey: "kbbi.push.registered"))
        }
        let id = UUID().uuidString.lowercased()
        let secret = Data((0..<32).map { _ in UInt8.random(in: 0...255) }).base64EncodedString()
            .replacingOccurrences(of: "+", with: "-").replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
        let encoded = (try? JSONSerialization.data(withJSONObject: ["id": id, "secret": secret])) ?? Data()
        save(encoded)
        return InstallationCredentials(id: id, secret: secret, registered: false)
    }

    static func markRegistered() { UserDefaults.standard.set(true, forKey: "kbbi.push.registered") }
    private static let service = "com.arrazyfathan.kbbi.push-installation"
    private static func read() -> Data? {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword, kSecAttrService as String: service,
                                    kSecAttrAccount as String: "installation", kSecReturnData as String: true]
        var result: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess else { return nil }
        return result as? Data
    }
    private static func save(_ data: Data) {
        let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword, kSecAttrService as String: service,
                                    kSecAttrAccount as String: "installation"]
        let attributes: [String: Any] = [kSecValueData as String: data, kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly]
        if SecItemUpdate(query as CFDictionary, attributes as CFDictionary) != errSecSuccess {
            var insertion = query; attributes.forEach { insertion[$0.key] = $0.value }
            SecItemAdd(insertion as CFDictionary, nil)
        }
    }
}
