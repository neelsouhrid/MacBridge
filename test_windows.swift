import Cocoa

let options = CGWindowListOption(arrayLiteral: .excludeDesktopElements, .optionOnScreenOnly)
guard let windowListInfo = CGWindowListCopyWindowInfo(options, kCGNullWindowID) as? [[String: Any]] else {
    print("Failed to get window list")
    exit(1)
}

for info in windowListInfo {
    guard let ownerName = info[kCGWindowOwnerName as String] as? String,
          let layer = info[kCGWindowLayer as String] as? Int, layer == 0 else {
        continue
    }
    
    let windowName = info[kCGWindowName as String] as? String ?? ""
    if ownerName == "Safari" || ownerName.lowercased().contains("antigravity") {
        print("App: \(ownerName), Window: \(windowName)")
    }
}
