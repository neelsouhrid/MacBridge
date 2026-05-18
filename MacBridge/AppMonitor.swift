import Cocoa
import Foundation

// MARK: - App Info Model
struct AppInfo {
    let name: String
    let windows: [String]

    func toDict() -> [String: Any] {
        return ["name": name, "windows": windows]
    }
}

// MARK: - Running Apps & Windows Monitor
class AppMonitor {

    /// Returns all visible (regular) running apps with their window titles
    func getRunningApps() -> [AppInfo] {
        let workspace = NSWorkspace.shared
        let apps = workspace.runningApplications.filter {
            $0.activationPolicy == .regular && $0.localizedName != nil
        }

        var result: [AppInfo] = []

        for app in apps {
            guard let name = app.localizedName else { continue }
            let windows = AppleScriptRunner.getWindowTitles(forApp: name)
            result.append(AppInfo(name: name, windows: windows))
        }

        return result
    }

    /// Returns running apps as a JSON-serializable array
    func getRunningAppsJSON() -> [[String: Any]] {
        return getRunningApps().map { $0.toDict() }
    }
}
