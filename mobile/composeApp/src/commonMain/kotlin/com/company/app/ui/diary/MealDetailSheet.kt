package com.company.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.MealLogEntry
import com.company.app.ui.components.CalSnapFoodPhoto
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailSheet(
    entry: MealLogEntry,
    mealType: String,
    isDeleting: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CalSnapColors.Background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = CalSnapSpacing.sm)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(CalSnapColors.Divider),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CalSnapSpacing.screenPad)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
        ) {
            // Hero photo (full-width, 240dp tall)
            CalSnapFoodPhoto(
                name = entry.foodItem.name,
                imageUrl = entry.aiScanPhotoUrl,
                modifier = Modifier.fillMaxWidth(),
                size = 240.dp,
                cornerRadius = CalSnapRadius.lg,
            )

            // Title
            Text(
                text = entry.foodItem.name,
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Meal type chip + portion
            Row(
                horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MealTypeChip(mealType)
                Text(
                    text = "${entry.quantityG.toInt()}g",
                    fontSize = 14.sp,
                    color = CalSnapColors.Muted,
                    fontWeight = FontWeight.W500,
                )
            }

            // Macro grid: 4 columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CalSnapRadius.md))
                    .background(CalSnapColors.SurfaceAlt)
                    .padding(vertical = CalSnapSpacing.md, horizontal = CalSnapSpacing.sm),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MacroCell("Cal", "${entry.calories.toInt()}")
                MacroCell("Protein", "${entry.proteinG.toInt()}g")
                MacroCell("Carbs", "${entry.carbsG.toInt()}g")
                MacroCell("Fat", "${entry.fatG.toInt()}g")
            }

            // Delete button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CalSnapRadius.md))
                    .background(CalSnapColors.BadSoft)
                    .clickable(
                        enabled = !isDeleting,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete,
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = CalSnapColors.Bad,
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalSnapIcon(name = "trash", size = 18.dp, color = CalSnapColors.Bad, strokeWidth = 2f)
                        Text(
                            text = "Delete this meal",
                            color = CalSnapColors.Bad,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W600,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTypeChip(mealType: String) {
    val label = mealType.lowercase().replaceFirstChar { it.uppercase() }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CalSnapRadius.pill))
            .background(CalSnapColors.Accent)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            color = CalSnapColors.OnAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
private fun MacroCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = CalSnapColors.Muted,
            fontWeight = FontWeight.W500,
        )
    }
}
