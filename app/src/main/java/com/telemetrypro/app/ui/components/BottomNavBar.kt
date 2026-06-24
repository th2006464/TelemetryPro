package com.telemetrypro.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telemetrypro.app.R
import com.telemetrypro.app.ui.theme.*

@Composable
fun BottomNavBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavTab(stringResource(R.string.nav_dashboard), "dashboard"),
        NavTab(stringResource(R.string.nav_skyview), "explore"),
        NavTab(stringResource(R.string.nav_trends), "trending_up"),
        NavTab(stringResource(R.string.nav_settings), "settings")
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(SurfaceContainerLowest)
            .border(width = 1.dp, color = OutlineVariant)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val textColor by animateColorAsState(
                targetValue = if (isSelected) PrimaryFixedDim else OnSurfaceVariant,
                label = "nav_color"
            )
            val bgColor = if (isSelected) PrimaryContainer.copy(alpha = 0.1f)
                else Color.Transparent

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .then(
                        if (isSelected) Modifier
                            .background(bgColor, RoundedCornerShape(12.dp))
                            else Modifier
                    )
            ) {
                Text(
                    text = tab.icon,
                    style = LabelCaps,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = tab.label,
                    style = LabelCaps,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

private data class NavTab(val label: String, val icon: String)
