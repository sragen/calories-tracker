package com.company.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.theme.CalSnapColors
import com.company.app.ui.theme.CalSnapType

/**
 * Linear macro progress bar used below the calorie ring on the Home screen.
 *
 * @param label "PROTEIN" / "CARBS" / "FAT" — rendered uppercase
 * @param current grams consumed
 * @param target grams target
 * @param color fill color (use CalSnapColors.Protein / .Carb / .Fat)
 */
@Composable
fun CalSnapMacroBar(
    label: String,
    current: Float,
    target: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val progress = if (target > 0f) (current / target).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = CalSnapType.Label,
            color = CalSnapColors.Muted,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = current.toInt().toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.W600,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.4).sp,
            )
            Text(
                text = "/${target.toInt()}g",
                fontSize = 11.sp,
                color = CalSnapColors.Mute2,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CalSnapColors.Divider),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}
