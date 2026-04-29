package com.company.app.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun rememberHapticFeedback(): HapticFeedback {
    return remember {
        object : HapticFeedback {
            override fun light() {
                UIImpactFeedbackGenerator(
                    style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
                ).impactOccurred()
            }

            override fun medium() {
                UIImpactFeedbackGenerator(
                    style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
                ).impactOccurred()
            }

            override fun selection() {
                UISelectionFeedbackGenerator().selectionChanged()
            }

            override fun success() {
                UINotificationFeedbackGenerator().notificationOccurred(
                    UINotificationFeedbackType.UINotificationFeedbackTypeSuccess
                )
            }

            override fun error() {
                UINotificationFeedbackGenerator().notificationOccurred(
                    UINotificationFeedbackType.UINotificationFeedbackTypeError
                )
            }
        }
    }
}
