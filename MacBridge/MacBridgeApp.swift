import SwiftUI

// MARK: - App Entry Point
@main
struct MacBridgeApp: App {
    @NSApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        MenuBarExtra {
            MenuBarView()
                .environmentObject(CommandLog.shared)
        } label: {
            Label("MacBridge", systemImage: "antenna.radiowaves.left.and.right")
        }
        .menuBarExtraStyle(.window)
    }
}
