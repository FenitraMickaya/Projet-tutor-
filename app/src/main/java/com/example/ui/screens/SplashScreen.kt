package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }
    val opacity = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        // Animate scale and opacity in parallel
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(300)
        )
    }
    
    LaunchedEffect(Unit) {
        opacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
        // Hold splash for 2 seconds
        delay(2000)
        onNavigateNext()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Rich background radial/linear gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scale.value * pulseScale)
                    .size(110.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Logo StaffFlow",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "StaffFlow",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale.value)
            )

            Text(
                text = "Gestion de Présence & Paie",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // Footer branding
        Text(
            text = "Ariary Edition",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 24.dp)
        )
    }
}
