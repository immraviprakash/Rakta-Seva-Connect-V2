package com.raktaseva.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BloodGroupBadge(
    group: String,
    modifier: Modifier = Modifier,
    size: Int = 42
) {
    val radius = (size / 3.5).dp
    Box(
        modifier = modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(radius)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = group,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.8).sp
        )
    }
}
