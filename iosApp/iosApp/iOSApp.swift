import Shared
import SwiftUI
import UIKit
import UserNotifications

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(KBBIAppDelegate.self) private var appDelegate
    @Environment(\.scenePhase) private var scenePhase
    init() {
        installFirebaseReporting()
        KoinInitIosKt.doInitKoinIos()
        UserDefaults(suiteName: "group.com.arrazyfathan.kbbi")?.set(
            ExternalEntryConfigurationKt.getShareExtensionApiBaseUrl(),
            forKey: "apiBaseUrl"
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    IosIncomingLaunchRequests.shared.publish(uri: url.absoluteString)
                }
                .onAppear(perform: consumeDeferredShare)
                .onChange(of: scenePhase) { _, phase in
                    if phase == .active { consumeDeferredShare() }
                }
        }
    }
}

private func consumeDeferredShare() {
    guard let defaults = UserDefaults(suiteName: "group.com.arrazyfathan.kbbi"),
          let text = defaults.string(forKey: "pendingShareText") else { return }
    IosIncomingLaunchRequests.shared.publishText(sharedText: text)
}

final class KBBIAppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
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
        if let uri = response.notification.request.content.userInfo["url"] as? String {
            IosIncomingLaunchRequests.shared.publish(uri: uri)
        }
        completionHandler()
    }
}
