package com.example.diwali.model

import androidx.compose.ui.graphics.Color

enum class CrackerType {
    SUTLI_BOMB,
    ROCKET,
    PHOOLJHADI,
    CHAKRI,
    ANAR,
    PENCIL_WHIP,
    PAPER_BOMB,
    LADI,
    SNAKE_TABLET,
    COLOR_SPARKLER,
    AERIAL_SHELL,
    WHIZ_WHEEL,
    ELECTRIC_FOUNTAIN,
    SKY_SHOTS;

    companion object {
        val LAXMI_BOMB = PAPER_BOMB
    }
}

data class CrackerItem(
    val id: String,
    val type: CrackerType,
    val name: String,
    val nameGujarati: String = name, // Backwards compatibility if referenced
    val nameEnglish: String = name,
    val category: String,
    val description: String,
    val price: Int,
    val isUnlockedByDefault: Boolean = false,
    val rewardCoins: Int = 10,
    val boomStars: Int = 3,
    val badgeText: String = "Popular",
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color
)

object CrackerLevelConfig {
    const val TOTAL_LEVELS_PER_CRACKER = 25

    fun getLevelRewardCoins(baseReward: Int, level: Int): Int {
        val safeBase = if (baseReward <= 0) 10 else baseReward
        return safeBase + ((level.coerceAtLeast(1) - 1) * 5)
    }

    fun getLevelUnlockCost(baseReward: Int, level: Int): Int {
        if (level <= 1) return 0
        val previousReward = getLevelRewardCoins(baseReward, level - 1)
        return previousReward * 3
    }

    fun getLevelUnlockCost(level: Int): Int {
        return getLevelUnlockCost(10, level)
    }

    fun getLevelScale(level: Int): Float {
        val clamped = level.coerceIn(1, TOTAL_LEVELS_PER_CRACKER)
        return 0.85f + (clamped - 1) * (0.80f / (TOTAL_LEVELS_PER_CRACKER - 1))
    }

    fun getLevelParticleMultiplier(level: Int): Float {
        val clamped = level.coerceIn(1, TOTAL_LEVELS_PER_CRACKER)
        return 1.0f + (clamped - 1) * (1.8f / (TOTAL_LEVELS_PER_CRACKER - 1))
    }
}

object CrackerRepository {
    val crackers = listOf(
        // 1. Sutli Bomb
        CrackerItem(
            id = "sutli_bomb",
            type = CrackerType.SUTLI_BOMB,
            name = "Sutli Bomb",
            category = "Mega Blaster",
            description = "Massive explosive blast with earth-shaking sound and smoke!",
            price = 50,
            isUnlockedByDefault = false,
            rewardCoins = 25,
            boomStars = 5,
            badgeText = "Super Boom",
            primaryColor = Color(0xFF2E7D32),
            secondaryColor = Color(0xFF66BB6A),
            glowColor = Color(0xFFA5D6A7)
        ),
        // 2. Sky Rocket
        CrackerItem(
            id = "rocket",
            type = CrackerType.ROCKET,
            name = "Sky Rocket",
            category = "Sky Blaster",
            description = "Shoots straight into the starry sky and bursts into bright colors!",
            price = 100,
            isUnlockedByDefault = false,
            rewardCoins = 40,
            boomStars = 5,
            badgeText = "Sky Blaster",
            primaryColor = Color(0xFF7B1FA2),
            secondaryColor = Color(0xFFBA68C8),
            glowColor = Color(0xFFE1BEE7)
        ),
        // 3. Sky Shots 7-Cake (NEW CATEGORY)
        CrackerItem(
            id = "sky_shots",
            type = CrackerType.SKY_SHOTS,
            name = "Sky Shots 7-Cake",
            category = "Sky Shots",
            description = "Multi-tube aerial battery firing 7 rhythmic bursts high into the stratosphere!",
            price = 120,
            isUnlockedByDefault = false,
            rewardCoins = 45,
            boomStars = 5,
            badgeText = "7-Shots Cake",
            primaryColor = Color(0xFF4A148C),
            secondaryColor = Color(0xFFFFAB00),
            glowColor = Color(0xFFEA80FC)
        ),
        // 4. Classic Fuljhadi
        CrackerItem(
            id = "phooljhadi",
            type = CrackerType.PHOOLJHADI,
            name = "Classic Sparkler",
            category = "Hand Sparkler",
            description = "Safe handheld sparkler with twinkling golden sparks!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 10,
            boomStars = 2,
            badgeText = "100% Safe",
            primaryColor = Color(0xFF00BCD4),
            secondaryColor = Color(0xFF4CAF50),
            glowColor = Color(0xFF80DEEA)
        ),
        // 4. Chakri
        CrackerItem(
            id = "chakri",
            type = CrackerType.CHAKRI,
            name = "Ground Chakri",
            category = "Ground Spinner",
            description = "Spins rapidly on the ground casting a fiery golden wheel!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 10,
            boomStars = 3,
            badgeText = "Kids Special",
            primaryColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFFFFD54F),
            glowColor = Color(0xFFFFE082)
        ),
        // 5. Anar / Flower Pot
        CrackerItem(
            id = "anar",
            type = CrackerType.ANAR,
            name = "Flower Pot (Anar)",
            category = "Sparkling Fountain",
            description = "Shoots a high shimmering fountain of golden sparks!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 15,
            boomStars = 4,
            badgeText = "Festive Classic",
            primaryColor = Color(0xFFE91E63),
            secondaryColor = Color(0xFFFF5722),
            glowColor = Color(0xFFFF8A80)
        ),
        // 6. Magic Whip / Pencil
        CrackerItem(
            id = "pencil_whip",
            type = CrackerType.PENCIL_WHIP,
            name = "Magic Whip (Pencil)",
            category = "Crackling Wand",
            description = "Crackling wand that pops with colorful flashing sparks!",
            price = 30,
            isUnlockedByDefault = false,
            rewardCoins = 15,
            boomStars = 3,
            badgeText = "Fun Crackle",
            primaryColor = Color(0xFF8E24AA),
            secondaryColor = Color(0xFFFFD600),
            glowColor = Color(0xFFEA80FC)
        ),
        // 7. Paper Bomb (formerly Laxmi Bomb)
        CrackerItem(
            id = "laxmi_bomb",
            type = CrackerType.PAPER_BOMB,
            name = "Paper Bomb",
            category = "Paper Blaster",
            description = "Crisp loud paper blast with red confetti and bright flash!",
            price = 60,
            isUnlockedByDefault = false,
            rewardCoins = 25,
            boomStars = 4,
            badgeText = "Crisp Bang",
            primaryColor = Color(0xFFD50000),
            secondaryColor = Color(0xFFFFD54F),
            glowColor = Color(0xFFFF8A80)
        ),
        // 8. 1000 Ladi (Series Crackers)
        CrackerItem(
            id = "ladi",
            type = CrackerType.LADI,
            name = "1000 Series Crackers",
            category = "Rapid Chain",
            description = "Rapid machine-gun series pops in non-stop succession!",
            price = 120,
            isUnlockedByDefault = false,
            rewardCoins = 50,
            boomStars = 5,
            badgeText = "Mega Chain",
            primaryColor = Color(0xFFC62828),
            secondaryColor = Color(0xFFFFEB3B),
            glowColor = Color(0xFFFF8A65)
        ),
        // 9. Snake Tablet / Saap ni Goli
        CrackerItem(
            id = "snake_tablet",
            type = CrackerType.SNAKE_TABLET,
            name = "Snake Tablets",
            category = "Magical Ash",
            description = "Lights up into a magical black snake rising from the ash!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 10,
            boomStars = 2,
            badgeText = "Kids Favorite",
            primaryColor = Color(0xFF37474F),
            secondaryColor = Color(0xFFFF7043),
            glowColor = Color(0xFF90A4AE)
        ),
        // 10. Colorful Fuljhadi
        CrackerItem(
            id = "color_sparkler",
            type = CrackerType.COLOR_SPARKLER,
            name = "Rainbow Sparkler",
            category = "Neon Sparkler",
            description = "Vibrant neon sparks in emerald green, ruby red, and blue!",
            price = 40,
            isUnlockedByDefault = false,
            rewardCoins = 20,
            boomStars = 3,
            badgeText = "Multi Color",
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFFFF1744),
            glowColor = Color(0xFF69F0AE)
        ),
        // 11. Aerial Shell / Sky Blast
        CrackerItem(
            id = "aerial_shell",
            type = CrackerType.AERIAL_SHELL,
            name = "Sky Blast Mortar",
            category = "Grand Mortar",
            description = "Heavy cannon shot bursting into a colossal sky peony!",
            price = 200,
            isUnlockedByDefault = false,
            rewardCoins = 60,
            boomStars = 5,
            badgeText = "Grand Finale",
            primaryColor = Color(0xFF1565C0),
            secondaryColor = Color(0xFFFFD600),
            glowColor = Color(0xFF82B1FF)
        ),
        // 12. Whiz Wheel Ground Chakri
        CrackerItem(
            id = "whiz_wheel",
            type = CrackerType.WHIZ_WHEEL,
            name = "Whiz Wheel",
            category = "Flying Spinner",
            description = "Whistling spinner that spins on the ground then flies upward!",
            price = 80,
            isUnlockedByDefault = false,
            rewardCoins = 30,
            boomStars = 4,
            badgeText = "Whistling",
            primaryColor = Color(0xFF00B0FF),
            secondaryColor = Color(0xFFFFAB00),
            glowColor = Color(0xFF80D8FF)
        ),
        // 13. Electric Fountain / Cold Pyro
        CrackerItem(
            id = "electric_fountain",
            type = CrackerType.ELECTRIC_FOUNTAIN,
            name = "Electric Fountain",
            category = "Cold Pyro Geyser",
            description = "Safe cold-pyro silver geyser with crystal sparkles!",
            price = 150,
            isUnlockedByDefault = false,
            rewardCoins = 45,
            boomStars = 5,
            badgeText = "100% Smoke Free",
            primaryColor = Color(0xFFECEFF1),
            secondaryColor = Color(0xFF00E5FF),
            glowColor = Color(0xFFFFFFFF)
        )
    )

    fun getCrackerById(id: String): CrackerItem? = crackers.firstOrNull { it.id == id }
}
