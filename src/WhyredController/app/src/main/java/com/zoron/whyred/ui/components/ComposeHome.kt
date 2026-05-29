package com.zoron.whyred.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoron.whyred.ui.theme.ZoronTheme

@Composable
fun PowerStateHeroCard(
    stateText: String,
    profileText: String,
    cpuInfo: String,
    isDetecting: Boolean
) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "POWER STATE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isDetecting) Color.Gray else MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stateText,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Mode: $profileText", style = MaterialTheme.typography.bodyLarge)
            Text(text = "CPU Governor: $cpuInfo", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ZoronModeGrid(
    onModeClick: (String) -> Unit
) {
    Column {
        Text(
            text = "ZORON-X MODES",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            ModeCard(
                modifier = Modifier.weight(1f),
                icon = "⚡",
                title = "Balanced",
                desc = "Smart daily",
                onClick = { onModeClick("balanced") }
            )
            ModeCard(
                modifier = Modifier.weight(1f),
                icon = "🔋",
                title = "Deep",
                desc = "Max battery",
                onClick = { onModeClick("deep") }
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ModeCard(
                modifier = Modifier.weight(1f),
                icon = "❄️",
                title = "Hibernate",
                desc = "Extreme standby",
                onClick = { onModeClick("hibernation") }
            )
            ModeCard(
                modifier = Modifier.weight(1f),
                icon = "🚀",
                title = "Burst",
                desc = "Race-to-idle",
                onClick = { onModeClick("burst") }
            )
        }
        ModeCard(
            modifier = Modifier.fillMaxWidth(),
            icon = "🌙",
            title = "Nightwatch",
            desc = "Ultra deep overnight preservation",
            onClick = { onModeClick("nightwatch") }
        )
    }
}

@Composable
fun ModeCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    BentoCard(modifier = modifier.clickable { onClick() }.height(160.dp)) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(text = icon, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = desc, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
