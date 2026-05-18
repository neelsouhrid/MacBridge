import Foundation
import CoreGraphics

// MARK: - Brightness & Volume Shell Wrappers
struct BrightnessVolume {

    /// Sets display brightness (0.0 – 1.0) using the `brightness` CLI tool
    /// User must install via: brew install brightness
    static func setBrightness(_ level: Double) -> (success: Bool, message: String) {
        let clamped = Float(max(0.0, min(1.0, level)))
        
        // Use Apple's private DisplayServices framework dynamically to set brightness natively
        // This completely bypasses the need for Homebrew, m1ddc, or the brightness CLI
        let handle = dlopen("/System/Library/PrivateFrameworks/DisplayServices.framework/DisplayServices", RTLD_NOW)
        if let handle = handle {
            let sym = dlsym(handle, "DisplayServicesSetBrightness")
            if let sym = sym {
                typealias SetBrightnessFunc = @convention(c) (CGDirectDisplayID, Float) -> Int32
                let setBrightness = unsafeBitCast(sym, to: SetBrightnessFunc.self)
                
                var displayCount: UInt32 = 0
                var activeDisplays = [CGDirectDisplayID](repeating: 0, count: 10)
                let error = CGGetActiveDisplayList(10, &activeDisplays, &displayCount)
                
                if error == .success {
                    for i in 0..<Int(displayCount) {
                        _ = setBrightness(activeDisplays[i], clamped)
                    }
                }
                
                dlclose(handle)
                return (true, "Brightness set natively to \(Int(clamped * 100))%")
            }
            dlclose(handle)
        }
        
        // Fallback for extremely old Macs (unlikely needed, but safe)
        _ = shell("/usr/local/bin/brightness", String(format: "%.2f", clamped))
        
        return (true, "Brightness set to \(Int(clamped * 100))%")
    }

    /// Gets current volume level (0–100)
    static func getVolume() -> Int {
        let script = "output volume of (get volume settings)"
        let result = AppleScriptRunner.run(script)
        if let output = result.output, let vol = Int(output) {
            return vol
        }
        return -1
    }

    // MARK: - Shell Helper

    /// Executes a shell command and returns (stdout, exit status)
    static func shell(_ command: String, _ arguments: String...) -> (output: String, status: Int32) {
        let process = Process()
        let pipe = Pipe()

        process.executableURL = URL(fileURLWithPath: command)
        process.arguments = arguments
        process.standardOutput = pipe
        process.standardError = pipe

        do {
            try process.run()
            process.waitUntilExit()
        } catch {
            return ("Error: \(error.localizedDescription)", -1)
        }

        let data = pipe.fileHandleForReading.readDataToEndOfFile()
        let output = String(data: data, encoding: .utf8) ?? ""
        return (output.trimmingCharacters(in: .whitespacesAndNewlines), process.terminationStatus)
    }

    /// Executes a shell command string via /bin/zsh
    static func shellCommand(_ command: String) -> (output: String, status: Int32) {
        let process = Process()
        let pipe = Pipe()

        process.executableURL = URL(fileURLWithPath: "/bin/zsh")
        process.arguments = ["-c", command]
        process.standardOutput = pipe
        process.standardError = pipe

        do {
            try process.run()
            process.waitUntilExit()
        } catch {
            return ("Error: \(error.localizedDescription)", -1)
        }

        let data = pipe.fileHandleForReading.readDataToEndOfFile()
        let output = String(data: data, encoding: .utf8) ?? ""
        return (output.trimmingCharacters(in: .whitespacesAndNewlines), process.terminationStatus)
    }
}
