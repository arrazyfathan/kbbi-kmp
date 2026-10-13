import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import FirebasePerformance
import Foundation
import Shared

private func appleReportingBuildInfo() -> AppBuildInfo {
    #if DEBUG
    let buildType = "debug"
    #else
    let buildType = "release"
    #endif
    return AppBuildInfo(
        flavor: Bundle.main.object(forInfoDictionaryKey: "KBBIEnvironment") as? String ?? "development",
        buildType: buildType, versionName: Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0.0.0")
}

func installFirebaseReporting() {
    IosReporting.shared.install(backend: NoOpReportingBackend(buildInfo: appleReportingBuildInfo()))
    guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist"),
        let options = FirebaseOptions(contentsOfFile: path)
    else { return }
    // Preferences apply immediately after Koin starts. Collection defaults off in Info.plist.
    if FirebaseApp.app() == nil { FirebaseApp.configure(options: options) }
    Performance.sharedInstance().isInstrumentationEnabled = false
    IosReporting.shared.install(backend: AppleFirebaseReportingBackend())
}
private final class AppleFirebaseReportingBackend: NSObject, ReportingBackend {
    let capabilities = ReportingCapabilities(analytics: true, crashes: true, performance: true)
    var buildInfo: AppBuildInfo { appleReportingBuildInfo() }
    func collection(crashes: Bool, analytics: Bool, performance: Bool) {
        Analytics.setAnalyticsCollectionEnabled(analytics)
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(crashes)
        if !crashes { Crashlytics.crashlytics().deleteUnsentReports() }
        Performance.sharedInstance().isDataCollectionEnabled = performance
        Performance.sharedInstance().isInstrumentationEnabled = false
    }
    func event(name: String, parameters: [String: String]) { Analytics.logEvent(name, parameters: parameters) }
    func breadcrumb(message: String) { Crashlytics.crashlytics().log(message) }
    func key(key: String, value: String) { Crashlytics.crashlytics().setCustomValue(value, forKey: key) }
    func nonFatal(operation: String, originalType: String, context: [String: String]) {
        let sdk = Crashlytics.crashlytics()
        sdk.setCustomValue(operation, forKey: "operation")
        context.forEach { sdk.setCustomValue($0.value, forKey: $0.key) }
        // Original exception text, cause, content and credentials never cross the bridge.
        sdk.record(error: NSError(domain: "KBBI.Reporting", code: 1, userInfo: [NSLocalizedDescriptionKey: "\(operation) failed (\(originalType))"]))
    }
    func startNetwork(url: String, method: String) -> any NetworkPerformanceTrace {
        let methods: [String: HTTPMethod] = ["GET": .get, "POST": .post, "PUT": .put, "PATCH": .patch, "DELETE": .delete, "HEAD": .head, "OPTIONS": .options]
        guard let metricURL = URL(string: url),
            let metric = HTTPMetric(url: metricURL, httpMethod: methods[method] ?? .get)
        else {
            return NoOpNetworkPerformanceTrace.shared
        }
        metric.start()
        return AppleNetworkTrace(metric: metric)
    }
}
private final class AppleNetworkTrace: NSObject, NetworkPerformanceTrace {
    private let metric: HTTPMetric
    private let lock = NSLock()
    private var stopped = false
    init(metric: HTTPMetric) { self.metric = metric }
    func response(statusCode: Int32, contentType: String?, payloadSizeBytes: KotlinLong?) {
        lock.lock()
        defer { lock.unlock() }
        guard !stopped else { return }
        metric.responseCode = Int(statusCode)
        metric.responseContentType = contentType
        if let size = payloadSizeBytes { metric.responsePayloadSize = Int(size.int64Value) }
    }
    func stop() {
        lock.lock()
        defer { lock.unlock() }
        guard !stopped else { return }
        stopped = true
        metric.stop()
    }
}
