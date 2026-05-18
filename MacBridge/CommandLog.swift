import Foundation
import SwiftUI
import Combine
// MARK: - Command Entry Model
struct CommandEntry: Identifiable {
    let id = UUID()
    let timestamp: Date
    let endpoint: String
    let params: String
    let status: String

    var formattedTime: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm:ss"
        return formatter.string(from: timestamp)
    }
}

// MARK: - Shared Command Log (Observable)
@MainActor
class CommandLog: ObservableObject {
    static let shared = CommandLog()

    @Published var entries: [CommandEntry] = []
    @Published var isServerRunning = false
    @Published var serverPort: UInt16 = 5001

    private init() {}

    func addEntry(endpoint: String, params: String = "", status: String = "✓") {
        let entry = CommandEntry(
            timestamp: Date(),
            endpoint: endpoint,
            params: params,
            status: status
        )
        entries.insert(entry, at: 0)
        if entries.count > 5 {
            entries = Array(entries.prefix(5))
        }
    }
}
