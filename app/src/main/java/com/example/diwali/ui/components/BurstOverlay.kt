package com.example.diwali.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Spark(
    val angle: Double,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun BurstOverlay(
    cracker: CrackerItem,
    earnedCoins: Int,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val textOffset = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0.5f) }

    val sparks = remember {
        val rand = Random(System.currentTimeMillis())
        val palette = listOf(
            GoldYellow,
            cracker.primaryColor,
            cracker.secondaryColor,
            Color.White,
            FestiveOrange,
            Color(0xFF00E5FF),
            Color(0xFFFF4081)
        )
        List(55) {
            Spark(
                angle = rand.nextDouble() * 2 * Math.PI,
                speed = 140f + rand.nextFloat() * 300f,
                radius = 3f + rand.nextFloat() * 4.5f,
                color = palette[rand.nextInt(palette.size)]
            )
        }
    }

    LaunchedEffect(cracker) {
        // Flash fades quickly
        flashAlpha.animateTo(0f, animationSpec = tween(220, easing = LinearEasing))
    }

    LaunchedEffect(cracker) {
        textOffset.animateTo(-160f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(cracker) {
        progress.animateTo(1f, animationSpec = tween(1300, easing = FastOutSlowInEasing))
        onAnimationEnd()
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Flash burst background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(flashAlpha.value)
                .background(Color.White)
        )

        // Particle Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2 - 40f)
            val p = progress.value
            val currentAlpha = (1f - p).coerceIn(0f, 1f)

            sparks.forEach { spark ->
                val dist = spark.speed * p
                val x = (center.x + dist * cos(spark.angle)).toFloat()
                val y = (center.y + dist * sin(spark.angle)).toFloat()

                drawCircle(
                    color = spark.color.copy(alpha = currentAlpha),
                    radius = spark.radius * (1f - p * 0.4f),
                    center = Offset(x, y)
                )
            }
        }

        // Floating Celebratory Badge ("+15 COINS! 🪙🎉")
        val p = progress.value
        val textAlpha = (1f - p * 0.8f).coerceIn(0f, 1f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { IntOffset(0, textOffset.value.toInt()) }
                .scale(1f + p * 0.3f)
                .alpha(textAlpha)
        ) {
            Text(
                text = "DHAMAKA!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = GoldYellow
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE6000000))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "+$earnedCoins COINS!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldLight
                )
            }
        }
    }
}
