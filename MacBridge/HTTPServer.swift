import Foundation
import Swifter

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
