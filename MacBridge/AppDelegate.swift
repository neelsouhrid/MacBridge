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
        let savedPort = UserDefaults.standard.integer(forKey: "ServerPort")
        let portToUse: UInt16 = savedPort > 0 ? UInt16(savedPort) : 5001
        
        httpServer = MacBridgeHTTPServer(port: portToUse)
        httpServer?.start()
        
        NotificationCenter.default.addObserver(forName: NSNotification.Name("RestartServerNotification"), object: nil, queue: .main) { [weak self] _ in
            self?.restartServer()
        }
    }
    
    private func restartServer() {
        httpServer?.stop()
        let savedPort = UserDefaults.standard.integer(forKey: "ServerPort")
        let portToUse: UInt16 = savedPort > 0 ? UInt16(savedPort) : 5001
        httpServer = MacBridgeHTTPServer(port: portToUse)
        httpServer?.start()
    }
}
