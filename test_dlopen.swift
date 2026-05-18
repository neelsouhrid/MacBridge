import Foundation
import CoreGraphics

let handle = dlopen("/System/Library/PrivateFrameworks/DisplayServices.framework/DisplayServices", RTLD_NOW)
if handle != nil {
    let sym = dlsym(handle, "DisplayServicesSetBrightness")
    if sym != nil {
        typealias SetBrightnessFunc = @convention(c) (CGDirectDisplayID, Float) -> Int32
        let setBrightness = unsafeBitCast(sym, to: SetBrightnessFunc.self)
        setBrightness(CGMainDisplayID(), 0.8)
        print("Brightness set via dlopen")
    } else {
        print("Symbol not found")
    }
    dlclose(handle)
} else {
    print("Framework not found")
}
