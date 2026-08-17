import Foundation
import Capacitor

@objc(NativeCallPlugin)
public class NativeCallPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NativeCallPlugin"
    public let jsName = "NativeCall"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stopRinging", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setRingtone", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "checkFullScreenIntentPermission", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "requestFullScreenIntentPermission", returnType: CAPPluginReturnPromise)
    ]

    @objc func initialize(_ call: CAPPluginCall) {
        call.unavailable(NativeCall.unimplementedMessage)
    }

    @objc func stopRinging(_ call: CAPPluginCall) {
        call.unavailable(NativeCall.unimplementedMessage)
    }

    @objc func setRingtone(_ call: CAPPluginCall) {
        call.unavailable(NativeCall.unimplementedMessage)
    }

    @objc func checkFullScreenIntentPermission(_ call: CAPPluginCall) {
        call.resolve(["granted": false])
    }

    @objc func requestFullScreenIntentPermission(_ call: CAPPluginCall) {
        call.unavailable(NativeCall.unimplementedMessage)
    }
}
