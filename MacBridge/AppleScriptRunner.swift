import Foundation

// MARK: - AppleScript Execution Wrapper
struct AppleScriptRunner {

    /// Runs an arbitrary AppleScript string and returns the result descriptor
    @discardableResult
    static func run(_ source: String) -> (success: Bool, output: String?) {
        var error: NSDictionary?
        let script = NSAppleScript(source: source)
        let result = script?.executeAndReturnError(&error)

        if let error = error {
            let message = error[NSAppleScript.errorMessage] as? String ?? "Unknown AppleScript error"
            print("[AppleScript Error] \(message)")
            return (false, message)
        }
        return (true, result?.stringValue)
    }

    /// Switches to a specific app, optionally focusing a named window
    static func switchToApp(name: String, window: String? = nil) -> Bool {
        var script: String

        if let window = window {
            script = """
            tell application "System Events"
                tell process "\(name)"
                    set frontmost to true
                    try
                        perform action "AXRaise" of window "\(window)"
                    end try
                end tell
            end tell
            tell application "\(name)" to activate
            """
        } else {
            script = """
            tell application "\(name)" to activate
            """
        }

        return run(script).success
    }

    /// Returns window titles for a given app via System Events
    static func getWindowTitles(forApp appName: String) -> [String] {
        let script = """
        tell application "System Events"
            tell process "\(appName)"
                try
                    set winNames to (name of every window whose name is not missing value and name is not "")
                    set output to ""
                    repeat with w in winNames
                        if output is not "" then set output to output & "|||"
                        set output to output & w
                    end repeat
                    return output
                on error
                    return ""
                end try
            end tell
        end tell
        """
        let result = run(script)
        guard let output = result.output, !output.isEmpty else { return [] }
        return output.components(separatedBy: "|||").filter { !$0.isEmpty }
    }

    /// Puts the Mac to sleep
    static func sleep() -> Bool {
        let process = Process()
        process.executableURL = URL(fileURLWithPath: "/usr/bin/pmset")
        process.arguments = ["sleepnow"]
        do {
            try process.run()
            return true
        } catch {
            return false
        }
    }

    /// Sets the system output volume (0–100)
    static func setVolume(_ level: Int) -> Bool {
        let clamped = max(0, min(100, level))
        let script = "set volume output volume \(clamped)"
        return run(script).success
    }

    /// Toggles mute on/off
    static func toggleMute() -> Bool {
        let script = """
        set currentMute to output muted of (get volume settings)
        if currentMute then
            set volume without output muted
        else
            set volume with output muted
        end if
        """
        return run(script).success
    }

    /// Gets current mute state
    static func isMuted() -> Bool {
        let script = "return output muted of (get volume settings)"
        let result = run(script)
        return result.output == "true"
    }
}
