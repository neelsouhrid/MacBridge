import Foundation
import CoreGraphics

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
            print("Found \(displayCount) displays")
            for i in 0..<Int(displayCount) {
                let display = activeDisplays[i]
                print("Setting brightness for display \(display)")
                _ = setBrightness(display, 0.5)
            }
        }
    }
    dlclose(handle)
}
