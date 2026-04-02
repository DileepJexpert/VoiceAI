package com.voiceai.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.voiceai.app.domain.model.Tag

@Composable
fun TagChip(
    tag: Tag,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = parseTagColor(tag.color)
    val contentColor = if (isColorDark(backgroundColor)) Color.White else Color.Black

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                } else {
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                }
            )
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

private fun parseTagColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color(0xFF6C5CE7) // Fallback to purple
    }
}

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
