import sys
import objc
from Foundation import NSBundle

# Load the private DisplayServices framework
bundle = NSBundle.bundleWithPath_('/System/Library/PrivateFrameworks/DisplayServices.framework')
if not bundle:
    print("Failed to load DisplayServices")
    sys.exit(1)
bundle.load()

DisplayServicesSetBrightness = bundle.classNamed_('DisplayServicesSetBrightness')
print(DisplayServicesSetBrightness)
