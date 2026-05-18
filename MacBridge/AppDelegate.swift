import Cocoa

// MARK: - App Delegate
class AppDelegate: NSObject, NSApplicationDelegate {

    private var httpServer: MacBridgeHTTPServer?

    func applicationDidFinishLaunching(_ notification: Notification) {
        requestPermissions()
        startServer()
    }

    func applicationWillTerminate(_ notification: Notification) {
        httpServer?.stop()
    }

    // MARK: - Permissions

    private func requestPermissions() {
        let options: NSDictionary = [
            kAXTrustedCheckOptionPrompt.takeUnretainedValue() as String: true
        ]
        let trusted = AXIsProcessTrustedWithOptions(options)

        if !trusted {
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                let alert = NSAlert()
                alert.messageText = "Accessibility Permission Required"
                alert.informativeText = """
                MacBridge needs Accessibility permission to:
                • Read window titles from running apps
                • Switch between app windows
                • Control system volume and brightness

                Please grant access in System Settings → Privacy & Security → Accessibility.
                """
                alert.alertStyle = .warning
                alert.icon = NSImage(systemSymbolName: "lock.shield", accessibilityDescription: nil)
                alert.addButton(withTitle: "Open System Settings")
                alert.addButton(withTitle: "Continue Anyway")

                if alert.runModal() == .alertFirstButtonReturn {
                    if let url = URL(string: "x-apple.systempreferences:com.apple.preference.security?Privacy_Accessibility") {
                        NSWorkspace.shared.open(url)
                    }
                }
            }
        }
    }

    // MARK: - Server

    private func startServer() {
        httpServer = MacBridgeHTTPServer()
        httpServer?.start()
    }
}
