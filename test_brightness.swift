import Foundation
import CoreGraphics

@_silgen_name("DisplayServicesSetBrightness")
func DisplayServicesSetBrightness(_ display: CGDirectDisplayID, _ brightness: Float) -> Int32

DisplayServicesSetBrightness(CGMainDisplayID(), 0.5)
print("done")
