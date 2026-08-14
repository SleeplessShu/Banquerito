package com.sleeplessdog.banquerito.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.bracket
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import org.jetbrains.compose.resources.painterResource

data class BottomNavItem(
    val route: String,
    val icon: @Composable () -> Unit,
)

@Composable
fun bottomNavItems(currentRoute: String?) = listOf(
    BottomNavItem("accounts") {
        BottomNavIconWithLabel(
            icon = AppIcons.bank(),
            label = "Счета",
            isSelected = currentRoute == "accounts",
        )
    },
    BottomNavItem("operations") {
        BottomNavIconWithLabel(
            icon = AppIcons.strategy(),
            label = "Планы",
            isSelected = currentRoute == "operations",
        )
    },
    BottomNavItem("taxes") {
        BottomNavIconWithLabel(
            icon = AppIcons.wallet(),
            label = "Налоги",
            isSelected = currentRoute == "taxes",
        )
    },
    BottomNavItem("consultant") {
        BottomNavIconWithLabel(
            icon = AppIcons.agent(),
            label = "Ассистент",
            isSelected = currentRoute == "consultant",
        )
    },
    BottomNavItem("settings") {
        BottomNavIconWithLabel(
            icon = AppIcons.settings(),
            label = "Настройки",
            isSelected = currentRoute == "settings",
        )
    },
)

@Composable
fun BottomNavIconWithLabel(
    icon: Painter,
    label: String,
    isSelected: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if(isSelected) {MaterialTheme.colorScheme.onPrimary} else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if(isSelected) {MaterialTheme.colorScheme.onPrimary} else {
                MaterialTheme.colorScheme.primaryContainer
            },
                    maxLines = 1,
        )
    }
}

@Composable
fun BottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = bottomNavItems(currentRoute)
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0xFF2A2A2A),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(14.dp))
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    val itemWidth = maxWidth / items.size
                    val bracketWidth = itemWidth / 2

                    val bracketHeight = 32.dp
                    // Анимированная позиция рамки
                    val bracketOffset by animateDpAsState(
                        targetValue = itemWidth * selectedIndex,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "bracket_offset",
                    )

                    // Рамка — едет под иконками
                    androidx.compose.foundation.Image(
                        painter = painterResource(Res.drawable.bracket),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .width(bracketWidth)
                            .height(bracketHeight)
                            .offset(x = bracketOffset + itemWidth / 4,
                                y = 4.dp),
                    )

                    // Иконки поверх рамки
                    Row(modifier = Modifier.fillMaxWidth()) {
                        items.forEach { item ->
                            val interactionSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(
                                            bounded = true,
                                            radius = 32.dp,
                                        ),
                                    ) {
                                        navController.navigate(item.route) {
                                            popUpTo("accounts") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                item.icon()
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }}
}