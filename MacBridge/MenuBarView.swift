import SwiftUI
import ServiceManagement

// MARK: - Menu Bar Popover View
struct MenuBarView: View {
    @EnvironmentObject var commandLog: CommandLog
    @AppStorage("ServerPort") private var savedPort: Int = 5001
    @State private var localIP: String = "Detecting..."
    @State private var launchAtLogin = false
    @State private var isHovering = false
    @State private var portString: String = ""

    var body: some View {
        VStack(spacing: 0) {
            headerSection
            Divider().opacity(0.3)
            connectionSection
            Divider().opacity(0.3)
            logSection
            Divider().opacity(0.3)
            controlsSection
        }
        .frame(width: 360)
        .background(.ultraThinMaterial)
        .onAppear {
            localIP = NetworkInfo.getLocalIPAddress() ?? "Not connected"
            launchAtLogin = SMAppService.mainApp.status == .enabled
            portString = "\(savedPort)"
        }
    }

    // MARK: - Header
    private var headerSection: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "667EEA"), Color(hex: "764BA2")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 36, height: 36)

                Image(systemName: "antenna.radiowaves.left.and.right")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("MacBridge")
                    .font(.system(size: 15, weight: .bold, design: .rounded))
                Text("Remote Mac Control")
                    .font(.system(size: 11))
                    .foregroundColor(.secondary)
            }

            Spacer()

            HStack(spacing: 6) {
                Circle()
                    .fill(commandLog.isServerRunning ? Color.green : Color.red)
                    .frame(width: 8, height: 8)
                    .shadow(
                        color: (commandLog.isServerRunning ? Color.green : Color.red).opacity(0.6),
                        radius: 4
                    )

                Text(commandLog.isServerRunning ? "Active" : "Offline")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(commandLog.isServerRunning ? .green : .red)
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(
                Capsule()
                    .fill((commandLog.isServerRunning ? Color.green : Color.red).opacity(0.1))
            )
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }

    // MARK: - Connection Info
    private var connectionSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label("Connection", systemImage: "network")
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(.secondary)

            HStack(spacing: 0) {
                infoCard(
                    icon: "wifi",
                    title: "IP Address",
                    value: localIP,
                    color: Color(hex: "667EEA")
                )

                Divider().frame(height: 36).opacity(0.3)

                editablePortCard
            }
            .background(Color.primary.opacity(0.04))
            .cornerRadius(8)

            Text("http://\(localIP):\(commandLog.serverPort)")
                .font(.system(size: 11, design: .monospaced))
                .foregroundColor(.secondary)
                .textSelection(.enabled)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    private func infoCard(icon: String, title: String, value: String, color: Color) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 12))
                .foregroundColor(color)

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 9, weight: .medium))
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.system(size: 12, weight: .semibold, design: .monospaced))
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
    }

    private var editablePortCard: some View {
        HStack(spacing: 8) {
            Image(systemName: "network")
                .font(.system(size: 12))
                .foregroundColor(Color(hex: "764BA2"))

            VStack(alignment: .leading, spacing: 1) {
                Text("Port (Enter to apply)")
                    .font(.system(size: 9, weight: .medium))
                    .foregroundColor(.secondary)
                
                TextField("Port", text: $portString)
                    .font(.system(size: 12, weight: .semibold, design: .monospaced))
                    .textFieldStyle(.plain)
                    .onSubmit {
                        if let newPort = Int(portString), newPort > 1024, newPort <= 65535 {
                            savedPort = newPort
                            NotificationCenter.default.post(name: NSNotification.Name("RestartServerNotification"), object: nil)
                        } else {
                            portString = "\(savedPort)" // revert if invalid
                        }
                    }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
    }

    // MARK: - Command Log
    private var logSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Label("Recent Commands", systemImage: "list.bullet.rectangle")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(.secondary)

                Spacer()

                Text("\(commandLog.entries.count)")
                    .font(.system(size: 10, weight: .bold, design: .rounded))
                    .foregroundColor(.white)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Capsule().fill(Color(hex: "667EEA")))
            }

            if commandLog.entries.isEmpty {
                HStack {
                    Spacer()
                    VStack(spacing: 6) {
                        Image(systemName: "tray")
                            .font(.system(size: 20))
                            .foregroundColor(.secondary.opacity(0.5))
                        Text("No commands yet")
                            .font(.system(size: 11))
                            .foregroundColor(.secondary.opacity(0.6))
                    }
                    .padding(.vertical, 12)
                    Spacer()
                }
            } else {
                VStack(spacing: 4) {
                    ForEach(commandLog.entries) { entry in
                        logRow(entry)
                    }
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    private func logRow(_ entry: CommandEntry) -> some View {
        HStack(spacing: 8) {
            Text(entry.status)
                .font(.system(size: 10))

            Text(entry.endpoint)
                .font(.system(size: 11, weight: .semibold, design: .monospaced))
                .foregroundColor(Color(hex: "667EEA"))

            if !entry.params.isEmpty {
                Text(entry.params)
                    .font(.system(size: 10, design: .monospaced))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                    .truncationMode(.tail)
            }

            Spacer()

            Text(entry.formattedTime)
                .font(.system(size: 9, design: .monospaced))
                .foregroundColor(.secondary.opacity(0.7))
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 5)
        .background(Color.primary.opacity(0.03))
        .cornerRadius(6)
    }

    // MARK: - Controls & Footer
    private var controlsSection: some View {
        VStack(spacing: 8) {
            Toggle(isOn: $launchAtLogin) {
                Label("Launch at Login", systemImage: "arrow.clockwise")
                    .font(.system(size: 12, weight: .medium))
            }
            .toggleStyle(.switch)
            .controlSize(.small)
            .onChange(of: launchAtLogin) { _, newValue in
                setLaunchAtLogin(newValue)
            }

            HStack(spacing: 8) {
                Button {
                    localIP = NetworkInfo.getLocalIPAddress() ?? "Not connected"
                } label: {
                    Label("Refresh IP", systemImage: "arrow.triangle.2.circlepath")
                        .font(.system(size: 11, weight: .medium))
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .controlSize(.small)

                Button(role: .destructive) {
                    NSApp.terminate(nil)
                } label: {
                    Label("Quit", systemImage: "power")
                        .font(.system(size: 11, weight: .medium))
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                .controlSize(.small)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    // MARK: - Launch at Login
    private func setLaunchAtLogin(_ enabled: Bool) {
        do {
            if enabled {
                try SMAppService.mainApp.register()
            } else {
                try SMAppService.mainApp.unregister()
            }
        } catch {
            print("[MacBridge] Launch at login error: \(error)")
            launchAtLogin = SMAppService.mainApp.status == .enabled
        }
    }
}

// MARK: - Color Hex Extension
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b, a: UInt64
        switch hex.count {
        case 6:
            (r, g, b, a) = (int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF, 255)
        case 8:
            (r, g, b, a) = (int >> 24 & 0xFF, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (r, g, b, a) = (128, 128, 128, 255)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
