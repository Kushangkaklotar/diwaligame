package com.example.diwali.model

import androidx.compose.ui.graphics.Color

enum class CrackerType {
    CHAKRI,
    ANAR,
    PHOOLJHADI,
    SUTLI_BOMB,
    ROCKET
}

data class CrackerItem(
    val id: String,
    val type: CrackerType,
    val nameGujarati: String,
    val nameEnglish: String,
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

object CrackerRepository {
    val crackers = listOf(
        CrackerItem(
            id = "chakri",
            type = CrackerType.CHAKRI,
            nameGujarati = "ચકરી (Ground Spinner)",
            nameEnglish = "Spinning Chakri",
            category = "🌀 Ground Spinner",
            description = "જમીન પર ગોળ ગોળ ફરીને સોનેરી સ્પાર્કલ્સ ઉડાડે!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 10,
            boomStars = 3,
            badgeText = "Kids Special 🌀",
            primaryColor = Color(0xFFFF9800),
            secondaryColor = Color(0xFFFFD54F),
            glowColor = Color(0xFFFFE082)
        ),
        CrackerItem(
            id = "anar",
            type = CrackerType.ANAR,
            nameGujarati = "કોઠી / અનાર (Flower Pot)",
            nameEnglish = "Sparkling Anar",
            category = "🌺 Sparkling Fountain",
            description = "ઉંચા ઉંચા ચમકતા રંગબેરંગી તણખાઓનો મસ્ત ફુવારો!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 15,
            boomStars = 4,
            badgeText = "Festive Classic 🪔",
            primaryColor = Color(0xFFE91E63),
            secondaryColor = Color(0xFFFF5722),
            glowColor = Color(0xFFFF8A80)
        ),
        CrackerItem(
            id = "phooljhadi",
            type = CrackerType.PHOOLJHADI,
            nameGujarati = "ફૂલઝરી (Sparklers)",
            nameEnglish = "Magic Sparkler",
            category = "✨ Hand Sparkler",
            description = "બાળકો માટે એકદમ સેફ અને ચમકતા રંગબેરંગી તારાઓ!",
            price = 0,
            isUnlockedByDefault = true,
            rewardCoins = 10,
            boomStars = 2,
            badgeText = "100% Safe ✨",
            primaryColor = Color(0xFF00BCD4),
            secondaryColor = Color(0xFF4CAF50),
            glowColor = Color(0xFF80DEEA)
        ),
        CrackerItem(
            id = "sutli_bomb",
            type = CrackerType.SUTLI_BOMB,
            nameGujarati = "સુતળી બોમ્બ (Sutli Bomb)",
            nameEnglish = "Green Sutli Bomb",
            category = "💣 Mega Blaster",
            description = "મોટો ધમાકો અને ધરતી ધ્રુજાવી દે તેવો જબરદસ્ત અવાજ!",
            price = 50,
            isUnlockedByDefault = false,
            rewardCoins = 25,
            boomStars = 5,
            badgeText = "Super Boom 💥",
            primaryColor = Color(0xFF2E7D32),
            secondaryColor = Color(0xFF66BB6A),
            glowColor = Color(0xFFA5D6A7)
        ),
        CrackerItem(
            id = "rocket",
            type = CrackerType.ROCKET,
            nameGujarati = "આકાશ રોકેટ (Sky Rocket)",
            nameEnglish = "Cosmic Sky Rocket",
            category = "🚀 Sky Blaster",
            description = "સીધું આકાશમાં ઊડીને રાતના અંધારામાં રંગોની રોશની ફેલાવે!",
            price = 100,
            isUnlockedByDefault = false,
            rewardCoins = 40,
            boomStars = 5,
            badgeText = "Sky Blaster 🚀",
            primaryColor = Color(0xFF7B1FA2),
            secondaryColor = Color(0xFFBA68C8),
            glowColor = Color(0xFFE1BEE7)
        )
    )
}
