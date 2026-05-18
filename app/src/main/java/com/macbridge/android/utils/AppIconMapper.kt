package com.macbridge.android.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps well-known macOS app names to Material Icons.
 * For apps without a specific icon, returns a generic computer icon.
 */
object AppIconMapper {

    private val iconMap: Map<String, ImageVector> = mapOf(
        // Browsers
        "Safari" to Icons.Filled.TravelExplore,
        "Google Chrome" to Icons.Filled.Language,
        "Firefox" to Icons.Filled.LocalFireDepartment,
        "Arc" to Icons.Filled.Rocket,
        "Microsoft Edge" to Icons.Filled.OpenInBrowser,

        // Dev Tools
        "Visual Studio Code" to Icons.Filled.Code,
        "Code" to Icons.Filled.Code,
        "VS Code" to Icons.Filled.Code,
        "Terminal" to Icons.Filled.Terminal,
        "iTerm2" to Icons.Filled.Terminal,
        "Xcode" to Icons.Filled.Build,
        "Android Studio" to Icons.Filled.PhoneAndroid,
        "IntelliJ IDEA" to Icons.Filled.DataObject,

        // Apple Apps
        "Finder" to Icons.Filled.Folder,
        "Notes" to Icons.Filled.StickyNote2,
        "Calendar" to Icons.Filled.CalendarMonth,
        "Mail" to Icons.Filled.Email,
        "Messages" to Icons.Filled.Chat,
        "FaceTime" to Icons.Filled.VideoCall,
        "Photos" to Icons.Filled.Photo,
        "Music" to Icons.Filled.MusicNote,
        "Maps" to Icons.Filled.Map,
        "Reminders" to Icons.Filled.Checklist,
        "Preview" to Icons.Filled.Image,
        "System Preferences" to Icons.Filled.Settings,
        "System Settings" to Icons.Filled.Settings,
        "App Store" to Icons.Filled.Store,
        "Books" to Icons.Filled.MenuBook,
        "Contacts" to Icons.Filled.Contacts,
        "Clock" to Icons.Filled.Schedule,
        "Calculator" to Icons.Filled.Calculate,
        "TextEdit" to Icons.Filled.EditNote,
        "Activity Monitor" to Icons.Filled.Monitor,
        "Disk Utility" to Icons.Filled.Storage,
        "Keychain Access" to Icons.Filled.Key,

        // Communication
        "Slack" to Icons.Filled.Tag,
        "Discord" to Icons.Filled.Headset,
        "Zoom" to Icons.Filled.Videocam,
        "Microsoft Teams" to Icons.Filled.Groups,
        "Telegram" to Icons.Filled.Send,
        "WhatsApp" to Icons.Filled.Phone,

        // Media
        "Spotify" to Icons.Filled.Audiotrack,
        "VLC" to Icons.Filled.PlayCircle,
        "QuickTime Player" to Icons.Filled.SlowMotionVideo,
        "IINA" to Icons.Filled.OndemandVideo,

        // Productivity
        "Notion" to Icons.Filled.Description,
        "Obsidian" to Icons.Filled.Diamond,
        "Microsoft Word" to Icons.Filled.Article,
        "Microsoft Excel" to Icons.Filled.TableChart,
        "Microsoft PowerPoint" to Icons.Filled.Slideshow,
        "Pages" to Icons.Filled.Article,
        "Numbers" to Icons.Filled.TableChart,
        "Keynote" to Icons.Filled.Slideshow,

        // AI
        "Claude" to Icons.Filled.AutoAwesome,
        "Gemini" to Icons.Filled.Stars,
        "ChatGPT" to Icons.Filled.SmartToy,

        // Design
        "Figma" to Icons.Filled.DesignServices,
        "Sketch" to Icons.Filled.Palette,
        "Adobe Photoshop" to Icons.Filled.PhotoFilter,
        "Adobe Illustrator" to Icons.Filled.Draw,
        "Canva" to Icons.Filled.ColorLens,

        // Utilities
        "1Password" to Icons.Filled.Password,
        "Docker" to Icons.Filled.ViewInAr,
        "Postman" to Icons.Filled.Api,
        "GitHub Desktop" to Icons.Filled.Hub,
    )

    /**
     * Returns an icon for the given app name.
     * Falls back to a generic computer icon for unknown apps.
     */
    fun getIcon(appName: String): ImageVector {
        return iconMap[appName] ?: Icons.Filled.DesktopMac
    }
}
