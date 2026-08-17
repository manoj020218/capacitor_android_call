import Foundation

/**
 * iOS is out of scope for this plugin (see PLAN.md §7) — a real incoming-call
 * experience here means CallKit, which is its own deep integration with
 * different primitives (PKPushRegistry, CXProvider) and deserves its own
 * planning pass rather than a bolt-on. Every method throws so a host app
 * finds out immediately in development, not silently in production.
 */
@objc public class NativeCall: NSObject {
    static let unimplementedMessage = "capacitor-native-call does not support iOS yet — see PLAN.md §7. Guard calls with Capacitor.getPlatform() === 'android'."
}
