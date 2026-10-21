import Shared
import SwiftUI
import UIKit
import FirebaseMessaging
import UserNotifications
import WidgetKit

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(KBBIAppDelegate.self) private var appDelegate
    @Environment(\.scenePhase) private var scenePhase
    init() {
        installFirebaseReporting()
        IosCampaignCoordinator.shared.install()
        KoinInitIosKt.doInitKoinIos()
        UserDefaults(suiteName: "group.com.arrazyfathan.kbbi")?.set(
            ExternalEntryConfigurationKt.getShareExtensionApiBaseUrl(),
            forKey: "apiBaseUrl"
        )
        WidgetSnapshotBridge.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    IosIncomingLaunchRequests.shared.publish(uri: url.absoluteString)
                }
                .onAppear(perform: consumeDeferredShare)
                .onChange(of: scenePhase) { _, phase in
                    if phase == .active {
                        Task { await IosCampaignCoordinator.shared.reconcile() }
                        consumeDeferredShare()
                        WidgetSnapshotBridge.reconcile()
                    }
                }
        }
    }
}

private enum WidgetSnapshotBridge {
    static func start() {
        NotificationCenter.default.addObserver(
            forName: Notification.Name("com.arrazyfathan.kbbi.widgetSnapshotUpdated"),
            object: nil,
            queue: .main
        ) { notification in
            guard let encoded = notification.object as? String,
                  let data = encoded.data(using: .utf8),
                  data.count <= 2_000_000,
                  let container = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: "group.com.arrazyfathan.kbbi") else { return }
            let destination = container.appendingPathComponent("widget-snapshot.json")
            do {
                try data.write(to: destination, options: .atomic)
                WidgetCenter.shared.reloadTimelines(ofKind: "com.arrazyfathan.kbbi.widget.word-of-day")
                WidgetCenter.shared.reloadTimelines(ofKind: "com.arrazyfathan.kbbi.widget.saved-word")
            } catch {
                NSLog("KBBI widget snapshot publish failed: %@", String(describing: error))
            }
        }
    }

    static func reconcile() {
        WidgetCenter.shared.reloadTimelines(ofKind: "com.arrazyfathan.kbbi.widget.word-of-day")
        WidgetCenter.shared.reloadTimelines(ofKind: "com.arrazyfathan.kbbi.widget.saved-word")
    }
}

private func consumeDeferredShare() {
    guard let defaults = UserDefaults(suiteName: "group.com.arrazyfathan.kbbi"),
          let text = defaults.string(forKey: "pendingShareText") else { return }
    IosIncomingLaunchRequests.shared.publishText(sharedText: text)
}

final class KBBIAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        return true
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
        Task { await IosCampaignCoordinator.shared.reconcile() }
    }

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        NSLog("KBBI APNs registration failed: %@", String(describing: error))
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        IosCampaignCoordinator.shared.didUpdateFCMToken(fcmToken)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        guard let campaign = parsedCampaign(from: notification.request.content.userInfo) else {
            completionHandler([])
            return
        }
        let defaults = UserDefaults.standard
        let enabled = campaign.topic == "app_updates"
            ? (defaults.object(forKey: "kbbi.ui.update_notifications") as? Bool ?? true)
            : defaults.bool(forKey: "kbbi.ui.campaign_notifications")
        guard enabled else {
            completionHandler([])
            return
        }
        completionHandler([.banner, .list, .sound])
    }

    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(
            name: "Default Configuration",
            sessionRole: connectingSceneSession.role
        )
        configuration.delegateClass = KBBISceneDelegate.self
        return configuration
    }

    // Retain this callback for any non-scene lifecycle path. Scene-based launches
    // are handled by KBBISceneDelegate below.
    func application(
        _ application: UIApplication,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        completionHandler(publishShortcut(shortcutItem))
    }
}

final class KBBISceneDelegate: UIResponder, UIWindowSceneDelegate {
    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        if let shortcutItem = connectionOptions.shortcutItem {
            _ = publishShortcut(shortcutItem)
        }
    }

    func windowScene(
        _ windowScene: UIWindowScene,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        completionHandler(publishShortcut(shortcutItem))
    }
}

private func publishShortcut(_ shortcutItem: UIApplicationShortcutItem) -> Bool {
    let route: String
    switch shortcutItem.type {
    case "com.arrazyfathan.kbbi.search": route = "kbbi://action/search"
    case "com.arrazyfathan.kbbi.bookmarks": route = "kbbi://bookmarks"
    case "com.arrazyfathan.kbbi.proverbs": route = "kbbi://proverb"
    case "com.arrazyfathan.kbbi.random": route = "kbbi://action/random"
    default:
        return false
    }
    IosIncomingLaunchRequests.shared.publish(uri: route)
    return true
}
extension KBBIAppDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        if let campaign = parsedCampaign(from: response.notification.request.content.userInfo) {
            if let releaseId = campaign.releaseId, let version = campaign.versionName {
                let prefix = "kbbi.push.update.\(releaseId)."
                UserDefaults.standard.set(version, forKey: prefix + "version")
                UserDefaults.standard.set(campaign.title, forKey: prefix + "title")
                UserDefaults.standard.set(campaign.body, forKey: prefix + "body")
            }
            IosIncomingLaunchRequests.shared.publish(uri: campaign.route, requestId: "push-\(campaign.deliveryId)")
        }
        completionHandler()
    }
}

private func parsedCampaign(from userInfo: [AnyHashable: Any]) -> IosParsedCampaignPush? {
    let data = Dictionary(uniqueKeysWithValues: userInfo.compactMap { key, value -> (String, String)? in
        guard let key = key as? String, let value = value as? String else { return nil }
        return (key, value)
    })
    return IosCampaignPayloadParser.shared.parse(
        data: data,
        nowEpochMillis: Date().timeIntervalSince1970 * 1_000,
    )
}
