import Foundation
import Swifter
import AppKit

// MARK: - HTTP Server (Swifter-based)
class MacBridgeHTTPServer {

    private let server = HttpServer()
    private let appMonitor = AppMonitor()
    private let port: UInt16

    init(port: UInt16 = 5001) {
        self.port = port
        setupRoutes()
    }

    // MARK: - Start / Stop

    func start() {
        do {
            try server.start(port, forceIPv4: true)
            print("[MacBridge] Server started on port \(port)")
            Task { @MainActor in
                CommandLog.shared.isServerRunning = true
                CommandLog.shared.serverPort = port
            }
        } catch {
            print("[MacBridge] Server failed to start: \(error)")
            Task { @MainActor in
                CommandLog.shared.isServerRunning = false
            }
        }
    }

    func stop() {
        server.stop()
        Task { @MainActor in
            CommandLog.shared.isServerRunning = false
        }
    }

    // MARK: - Route Setup

    private func setupRoutes() {

        // ── GET /ping ──────────────────────────────────────────────
        server["/ping"] = { _ in
            self.logCommand("/ping")
            return .ok(.json(["status": "ok", "app": "MacBridge"] as AnyObject))
        }

        // ── GET /apps ──────────────────────────────────────────────
        server["/apps"] = { _ in
            let apps = self.appMonitor.getRunningAppsJSON()
            self.logCommand("/apps", params: "\(apps.count) apps")
            return self.jsonResponse(apps)
        }

        // ── GET /switch?app=Name&window=Title ──────────────────────
        server["/switch"] = { request in
            let params = self.queryDict(request)
            guard let appName = params["app"] else {
                return .badRequest(.text("Missing 'app' parameter"))
            }

            let windowTitle = params["window"]
            let paramStr = "app=\(appName)" + (windowTitle.map { "&window=\($0)" } ?? "")

            let success = AppleScriptRunner.switchToApp(name: appName, window: windowTitle)

            self.logCommand("/switch", params: paramStr, success: success)

            if success {
                return .ok(.json([
                    "status": "ok",
                    "switched_to": appName,
                    "window": windowTitle ?? "main"
                ] as AnyObject))
            } else {
                return self.errorResponse("Failed to switch to \(appName)")
            }
        }

        // ── GET /volume?level=70 ───────────────────────────────────
        server["/volume"] = { request in
            let params = self.queryDict(request)
            guard let levelStr = params["level"], let level = Int(levelStr) else {
                return .badRequest(.text("Missing or invalid 'level' parameter (0-100)"))
            }

            let success = AppleScriptRunner.setVolume(level)
            self.logCommand("/volume", params: "level=\(level)", success: success)

            if success {
                return .ok(.json(["status": "ok", "volume": level] as AnyObject))
            } else {
                return self.errorResponse("Failed to set volume")
            }
        }

        // ── GET /brightness?level=0.5 ──────────────────────────────
        server["/brightness"] = { request in
            let params = self.queryDict(request)
            guard let levelStr = params["level"], let level = Double(levelStr) else {
                return .badRequest(.text("Missing or invalid 'level' parameter (0.0-1.0)"))
            }

            let result = BrightnessVolume.setBrightness(level)
            self.logCommand("/brightness", params: "level=\(level)", success: result.success)

            if result.success {
                return .ok(.json(["status": "ok", "brightness": level] as AnyObject))
            } else {
                return self.errorResponse(result.message)
            }
        }

        // ── GET /mute ──────────────────────────────────────────────
        server["/mute"] = { _ in
            let success = AppleScriptRunner.toggleMute()
            let muted = AppleScriptRunner.isMuted()
            self.logCommand("/mute", params: muted ? "muted" : "unmuted", success: success)

            return .ok(.json([
                "status": "ok",
                "muted": muted
            ] as AnyObject))
        }

        // ── GET /sleep ─────────────────────────────────────────────
        server["/sleep"] = { _ in
            self.logCommand("/sleep")
            DispatchQueue.global().asyncAfter(deadline: .now() + 0.5) {
                _ = AppleScriptRunner.sleep()
            }
            return .ok(.json(["status": "ok", "action": "sleep"] as AnyObject))
        }

        // ── GET /quit?app=AppName ──────────────────────────────────
        server["/quit"] = { request in
            let params = self.queryDict(request)
            guard let appName = params["app"] else {
                return .badRequest(.text("Missing 'app' parameter"))
            }

            let workspace = NSWorkspace.shared
            let apps = workspace.runningApplications.filter { $0.localizedName?.caseInsensitiveCompare(appName) == .orderedSame }

            guard let app = apps.first else {
                self.logCommand("/quit", params: "app=\(appName)", success: false)
                return .raw(404, "Not Found", ["Content-Type": "application/json"]) { writer in
                    let body: [String: Any] = ["status": "error", "message": "App not found"]
                    if let data = try? JSONSerialization.data(withJSONObject: body) {
                        try writer.write(data)
                    }
                }
            }

            app.terminate()
            DispatchQueue.global().asyncAfter(deadline: .now() + 2.0) {
                if !app.isTerminated {
                    app.forceTerminate()
                }
            }

            self.logCommand("/quit", params: "app=\(appName)", success: true)
            return .ok(.json(["status": "ok", "message": "Quit \(appName)"] as AnyObject))
        }

        // ── GET /launch?app=AppName ────────────────────────────────
        server["/launch"] = { request in
            let params = self.queryDict(request)
            guard let appName = params["app"] else {
                return .badRequest(.text("Missing 'app' parameter"))
            }

            let result = BrightnessVolume.shell("/usr/bin/open", "-a", appName)
            let success = result.status == 0

            self.logCommand("/launch", params: "app=\(appName)", success: success)

            if success {
                return .ok(.json(["status": "ok", "message": "Launched \(appName)"] as AnyObject))
            } else {
                return self.errorResponse("Failed to launch \(appName): \(result.output)")
            }
        }

        // ── GET /search?q=Query ────────────────────────────────────
        server["/search"] = { request in
            let params = self.queryDict(request)
            let query = params["q"]?.lowercased() ?? ""

            let directories = [
                "/Applications",
                "/Applications/Utilities",
                "/System/Applications",
                "/System/Applications/Utilities",
                FileManager.default.homeDirectoryForCurrentUser.appendingPathComponent("Applications").path
            ]

            var appNames = Set<String>()
            let fm = FileManager.default

            for dir in directories {
                guard let contents = try? fm.contentsOfDirectory(atPath: dir) else { continue }
                for item in contents {
                    if item.hasSuffix(".app") {
                        let name = item.replacingOccurrences(of: ".app", with: "")
                        if query.isEmpty || name.lowercased().contains(query) {
                            appNames.insert(name)
                        }
                    }
                }
            }

            let sortedApps = Array(appNames).sorted()
            self.logCommand("/search", params: "q=\(query)", success: true)

            // Return plain JSON string array
            do {
                let data = try JSONSerialization.data(withJSONObject: sortedApps)
                return .raw(200, "OK", ["Content-Type": "application/json"]) { writer in
                    try writer.write(data)
                }
            } catch {
                return self.errorResponse("Failed to serialize apps")
            }
        }
    }

    // MARK: - Helpers

    private func queryDict(_ request: HttpRequest) -> [String: String] {
        Dictionary(uniqueKeysWithValues: request.queryParams.map {
            ($0.0, $0.1.removingPercentEncoding ?? $0.1)
        })
    }

    private func jsonResponse(_ object: Any) -> HttpResponse {
        do {
            let data = try JSONSerialization.data(withJSONObject: object, options: .prettyPrinted)
            return .raw(200, "OK", ["Content-Type": "application/json"]) { writer in
                try writer.write(data)
            }
        } catch {
            return .internalServerError
        }
    }

    private func errorResponse(_ message: String) -> HttpResponse {
        return .raw(500, "Internal Server Error", ["Content-Type": "application/json"]) { writer in
            let body: [String: Any] = ["status": "error", "message": message]
            if let data = try? JSONSerialization.data(withJSONObject: body) {
                try writer.write(data)
            }
        }
    }

    private func logCommand(_ endpoint: String, params: String = "", success: Bool = true) {
        Task { @MainActor in
            CommandLog.shared.addEntry(
                endpoint: endpoint,
                params: params,
                status: success ? "✓" : "✗"
            )
        }
    }
}
