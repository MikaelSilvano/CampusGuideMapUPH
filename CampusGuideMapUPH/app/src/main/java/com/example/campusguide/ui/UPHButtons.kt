package com.example.campusguide.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val UPH_Navy = Color(0xFF16224C)
private val UPH_Red  = Color(0xFFE31E2E)
private val UPH_White = Color(0xFFFFFFFF)
private val UPH_Orange = Color(0xFFF58A0A)

val routeColor = Color(0xFFA64AEF)

@Composable
fun UPHPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = UPH_Navy,
            contentColor   = UPH_White,
            disabledContainerColor = UPH_Navy.copy(alpha = 0.35f),
            disabledContentColor   = UPH_White.copy(alpha = 0.7f)
        )
    ) { content() }
}

@Composable
fun UPHSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        border = BorderStroke(1.dp, UPH_Navy),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = UPH_Navy,
            disabledContentColor = UPH_Navy.copy(alpha = 0.35f)
        )
    ) { content() }
}
