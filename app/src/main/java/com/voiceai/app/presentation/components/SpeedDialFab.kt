package com.voiceai.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.voiceai.app.presentation.theme.Coral
import com.voiceai.app.presentation.theme.Teal

private val Blue = Color(0xFF4A90D9)
private val Orange = Color(0xFFFF9F43)
private val Purple = Color(0xFF6C5CE7)

@Composable
fun SpeedDialFab(
    onRecordClick: () -> Unit,
    onScanClick: () -> Unit,
    onBusinessCardClick: () -> Unit = {},
    onReceiptClick: () -> Unit = {},
    onQRClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "fab_rotation"
    )

    data class MiniFabItem(
        val icon: ImageVector,
        val label: String,
        val color: Color,
        val contentDescription: String,
        val onClick: () -> Unit
    )

    val miniFabs = listOf(
        MiniFabItem(Icons.Filled.QrCode2, "Scan QR Code", Purple, "Scan QR Code", onQRClick),
        MiniFabItem(Icons.Filled.Receipt, "Scan Receipt", Orange, "Scan Receipt", onReceiptClick),
        MiniFabItem(Icons.Filled.ContactPage, "Scan Business Card", Blue, "Scan Business Card", onBusinessCardClick),
        MiniFabItem(Icons.Filled.Description, "Scan Document", Teal, "Scan Document", onScanClick),
        MiniFabItem(Icons.Filled.Mic, "Record", Coral, "Record", onRecordClick),
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        miniFabs.forEachIndexed { index, item ->
            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 200,
                        delayMillis = index * 40
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        delayMillis = index * 40
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 150)
                ) + fadeOut(animationSpec = tween(durationMillis = 150))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    SmallFloatingActionButton(
                        onClick = {
                            expanded = false
                            item.onClick()
                        },
                        containerColor = item.color,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription
                        )
                    }
                }
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = if (expanded) "Close" else "Create",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}
