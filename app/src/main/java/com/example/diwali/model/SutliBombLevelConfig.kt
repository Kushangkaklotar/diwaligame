package com.example.diwali.model

import androidx.compose.ui.graphics.Color

/**
 * Detailed 25-Level specification for Sutli Bomb across 5 Phases.
 */
data class SutliLevelDetail(
    val level: Int,
    val name: String,
    val phaseName: String,
    val description: String,
    val scaleFactor: Float,
    val bodyColor: Color,
    val twineColor: Color,
    val widthDp: Float,
    val heightDp: Float,
    val isSpherical: Boolean = false,
    val isElongated: Boolean = false,
    val isWide: Boolean = false,
    val hasRocketCone: Boolean = false,
    val hasChemicalMark: Boolean = false,
    val hasSideLadi: Boolean = false,
    val isRoxStyle: Boolean = false,
    val isGoldStyle: Boolean = false,
    val isMultiFuse: Boolean = false,
    val isNuclearStyle: Boolean = false,
    val isBossMega: Boolean = false,
    val soundProfile: SutliSoundProfile,
    val explosionProfile: SutliExplosionProfile
)

enum class SutliSoundProfile {
    LIGHT_POP,          // Level 1: soft pop
    SOFT_BANG,          // Level 2: small pop
    FIZZ_THEN_POP,      // Level 3: hiss/fizz then pop
    SHARP_TADAK,        // Level 4: snappy sharp crack
    NORMAL_BOOM,        // Level 5: milestone normal blast
    INTENSE_BANG,       // Level 6: loud sharp blast
    HEAVY_BASS,         // Level 7: low-frequency heavy bass
    DEEP_ECHO,          // Level 8: hollow echoing boom
    HEAVY_DHAM,         // Level 9: deep thud "Dham"
    MID_SHOCKWAVE,      // Level 10: classic mid-range
    CHEMICAL_CRACKLE,   // Level 11: crackling sparks
    HISS_THEN_BLAST,    // Level 12: hiss beforehand
    EARTH_RUMBLE,       // Level 13: earth-shaking rumble
    DEEP_BLACK_BOOM,    // Level 14: muffled heavy boom
    ROCKET_HYBRID,      // Level 15: whistle into boom
    EAR_PIERCING_BANG,  // Level 16: deafening loud bang
    WIDE_BARREL_BLAST,  // Level 17: omnidirectional blast
    ROX_DEMOLITION,     // Level 18: industrial demolition
    GIANT_THUNDER,      // Level 19: mammoth thunderclap
    GOLDEN_MAJESTIC,    // Level 20: golden sparkling boom
    TRIPLE_POP_BANG,    // Level 21: 3 pre-bursts then main blast
    NUCLEAR_APOCALYPSE, // Level 22: nuclear atmospheric blast
    SUB_BASS_QUAKE,     // Level 23: violent sub-bass quake
    PRE_BOSS_DUAL,      // Level 24: double-gunpowder acoustic
    MEGA_BOOM_BOSS      // Level 25: slow-mo countdown + Brahmand Mega-Boom!
}

data class SutliExplosionProfile(
    val flashIntensity: Float,          // 0.2f to 1.0f
    val flashDurationMs: Long = 250L,
    val screenShakePx: Float,           // 4f to 50f
    val shakeDurationMs: Long = 400L,
    val shockwaveMaxRadius: Float,      // 180f to 600f
    val shockwaveColor: Color = Color(0xFFFFD54F),
    val smokeColor: Color = Color(0xFF9E9E9E),
    val smokeCount: Int = 20,
    val fireCount: Int = 30,
    val sparkCount: Int = 40,
    val hasSmokeRing: Boolean = false,
    val hasGoldenRing: Boolean = false,
    val hasDustKickup: Boolean = false,
    val hasChemicalSparks: Boolean = false,
    val hasAerialFlares: Boolean = false,
    val hasWhiteoutFlash: Boolean = false,
    val hasFinalFireworks: Boolean = false,
    val scorchRadius: Float = 40f
)

object SutliBombLevelConfig {

    val levels: List<SutliLevelDetail> = listOf(
        // ==========================================
        // PHASE 1: Beginner Levels (Level 1 to 5) - Simple & Safe
        // ==========================================
        SutliLevelDetail(
            level = 1,
            name = "Sado Shuruat",
            phaseName = "Phase 1: Beginner (Simple & Safe)",
            description = "Thin light brown paper sutli bomb with gentle pop sound and light white smoke.",
            scaleFactor = 0.65f,
            bodyColor = Color(0xFFBCAAA4), // light brown paper
            twineColor = Color(0xFFD7CCC8), // very light twine
            widthDp = 40f,
            heightDp = 52f,
            soundProfile = SutliSoundProfile.LIGHT_POP,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.35f,
                flashDurationMs = 180L,
                screenShakePx = 4f,
                shockwaveMaxRadius = 150f,
                smokeColor = Color(0xFFE0E0E0), // light white smoke
                smokeCount = 14,
                fireCount = 16,
                sparkCount = 20,
                scorchRadius = 22f
            )
        ),
        SutliLevelDetail(
            level = 2,
            name = "Light Twine Bomb",
            phaseName = "Phase 1: Beginner (Simple & Safe)",
            description = "Slightly larger paper with thin white twine criss-cross and gentle smoke.",
            scaleFactor = 0.74f,
            bodyColor = Color(0xFFA1887F),
            twineColor = Color(0xFFF5F5F5), // white sutli
            widthDp = 46f,
            heightDp = 58f,
            soundProfile = SutliSoundProfile.SOFT_BANG,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.45f,
                screenShakePx = 6f,
                shockwaveMaxRadius = 180f,
                smokeColor = Color(0xFFEEEEEE),
                smokeCount = 18,
                fireCount = 22,
                sparkCount = 28,
                scorchRadius = 28f
            )
        ),
        SutliLevelDetail(
            level = 3,
            name = "Dark Brown Fizz Bomb",
            phaseName = "Phase 1: Beginner (Simple & Safe)",
            description = "Elongated dark brown cylinder with characteristic fizzing fuse sound.",
            scaleFactor = 0.82f,
            bodyColor = Color(0xFF5D4037), // dark brown
            twineColor = Color(0xFF8D6E63),
            widthDp = 44f,
            heightDp = 70f,
            isElongated = true,
            soundProfile = SutliSoundProfile.FIZZ_THEN_POP,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.55f,
                screenShakePx = 9f,
                shockwaveMaxRadius = 210f,
                smokeColor = Color(0xFFBDBDBD),
                smokeCount = 24,
                fireCount = 28,
                sparkCount = 36,
                scorchRadius = 34f
            )
        ),
        SutliLevelDetail(
            level = 4,
            name = "Circular Tadak Bomb",
            phaseName = "Phase 1: Beginner (Simple & Safe)",
            description = "Spherical round twine-wound design with sharp snappy crack sound.",
            scaleFactor = 0.90f,
            bodyColor = Color(0xFF6D4C41),
            twineColor = Color(0xFFD7CCC8),
            widthDp = 56f,
            heightDp = 56f,
            isSpherical = true,
            soundProfile = SutliSoundProfile.SHARP_TADAK,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.65f,
                screenShakePx = 12f,
                shockwaveMaxRadius = 240f,
                smokeColor = Color(0xFF9E9E9E),
                smokeCount = 28,
                fireCount = 34,
                sparkCount = 48,
                scorchRadius = 40f
            )
        ),
        SutliLevelDetail(
            level = 5,
            name = "Milestone Ground Bomb",
            phaseName = "Phase 1: Beginner (Simple & Safe)",
            description = "First milestone! Heavy green jute bomb kicking up dust and normal blast sound.",
            scaleFactor = 1.0f,
            bodyColor = Color(0xFF2E7D32), // classic green
            twineColor = Color(0xFF1B5E20),
            widthDp = 60f,
            heightDp = 70f,
            soundProfile = SutliSoundProfile.NORMAL_BOOM,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.75f,
                screenShakePx = 15f,
                shockwaveMaxRadius = 280f,
                smokeColor = Color(0xFF8D6E63), // ground dust tint
                smokeCount = 36,
                fireCount = 40,
                sparkCount = 60,
                hasDustKickup = true,
                scorchRadius = 48f
            )
        ),

        // ==========================================
        // PHASE 2: Medium Levels (Level 6 to 12) - Power & Smoke Increase
        // ==========================================
        SutliLevelDetail(
            level = 6,
            name = "Red Flash Blaster",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Crimson red paper sutli bomb detonating with intense bright flash.",
            scaleFactor = 1.08f,
            bodyColor = Color(0xFFD50000), // intense red
            twineColor = Color(0xFFFFD54F), // gold twine
            widthDp = 64f,
            heightDp = 74f,
            soundProfile = SutliSoundProfile.INTENSE_BANG,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f, // intense flash!
                flashDurationMs = 350L,
                screenShakePx = 18f,
                shockwaveMaxRadius = 310f,
                shockwaveColor = Color(0xFFFF1744),
                smokeColor = Color(0xFF757575),
                smokeCount = 42,
                fireCount = 48,
                sparkCount = 70,
                scorchRadius = 55f
            )
        ),
        SutliLevelDetail(
            level = 7,
            name = "Double-Wrapped Heavy",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Double-wound dense jute mesh with enhanced low-end bass rumble.",
            scaleFactor = 1.15f,
            bodyColor = Color(0xFF1B5E20),
            twineColor = Color(0xFFE65100),
            widthDp = 68f,
            heightDp = 78f,
            soundProfile = SutliSoundProfile.HEAVY_BASS,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.85f,
                screenShakePx = 22f,
                shockwaveMaxRadius = 340f,
                smokeColor = Color(0xFF616161),
                smokeCount = 48,
                fireCount = 55,
                sparkCount = 80,
                scorchRadius = 62f
            )
        ),
        SutliLevelDetail(
            level = 8,
            name = "Smoke Ring Cylinder",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Long cylinder bomb creating a spectacular expanding volumetric smoke ring.",
            scaleFactor = 1.20f,
            bodyColor = Color(0xFF33691E),
            twineColor = Color(0xFFD7CCC8),
            widthDp = 54f,
            heightDp = 90f,
            isElongated = true,
            soundProfile = SutliSoundProfile.DEEP_ECHO,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.82f,
                screenShakePx = 24f,
                shockwaveMaxRadius = 360f,
                smokeColor = Color(0xFF424242),
                smokeCount = 60,
                fireCount = 50,
                sparkCount = 75,
                hasSmokeRing = true, // smoke ring!
                scorchRadius = 68f
            )
        ),
        SutliLevelDetail(
            level = 9,
            name = "Thick Jute Dham",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Rough burlap texture with heavy 'Dham' thud and dense billowing smoke.",
            scaleFactor = 1.26f,
            bodyColor = Color(0xFF4E342E), // rough brown
            twineColor = Color(0xFF3E2723),
            widthDp = 72f,
            heightDp = 82f,
            soundProfile = SutliSoundProfile.HEAVY_DHAM,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.88f,
                screenShakePx = 26f,
                shockwaveMaxRadius = 380f,
                smokeColor = Color(0xFF37474F),
                smokeCount = 68,
                fireCount = 62,
                sparkCount = 90,
                scorchRadius = 74f
            )
        ),
        SutliLevelDetail(
            level = 10,
            name = "Classic Mid-Range Shockwave",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Standard heavy bomb firing a visible shockwave ring and camera shake.",
            scaleFactor = 1.32f,
            bodyColor = Color(0xFF2E7D32),
            twineColor = Color(0xFFFFD54F),
            widthDp = 76f,
            heightDp = 86f,
            soundProfile = SutliSoundProfile.MID_SHOCKWAVE,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.90f,
                screenShakePx = 28f,
                shakeDurationMs = 500L,
                shockwaveMaxRadius = 420f,
                smokeColor = Color(0xFF424242),
                smokeCount = 72,
                fireCount = 70,
                sparkCount = 100,
                scorchRadius = 80f
            )
        ),
        SutliLevelDetail(
            level = 11,
            name = "Chemical Gunpowder Sparks",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Special chemical gunpowder casing spraying dense crackling sparks.",
            scaleFactor = 1.38f,
            bodyColor = Color(0xFF4A148C), // purple chemical casing
            twineColor = Color(0xFF00E676), // neon green twine
            widthDp = 78f,
            heightDp = 88f,
            hasChemicalMark = true,
            soundProfile = SutliSoundProfile.CHEMICAL_CRACKLE,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.92f,
                screenShakePx = 29f,
                shockwaveMaxRadius = 440f,
                shockwaveColor = Color(0xFF00E5FF),
                smokeColor = Color(0xFF311B92),
                smokeCount = 75,
                fireCount = 75,
                sparkCount = 130, // massive sparks!
                hasChemicalSparks = true,
                scorchRadius = 85f
            )
        ),
        SutliLevelDetail(
            level = 12,
            name = "Double Ladi Hiss Blaster",
            phaseName = "Phase 2: Medium (Power & Smoke Increase)",
            description = "Attached to twin red mini-crackers that hiss and crackle before main detonation.",
            scaleFactor = 1.44f,
            bodyColor = Color(0xFFC62828),
            twineColor = Color(0xFFFFD54F),
            widthDp = 80f,
            heightDp = 90f,
            hasSideLadi = true,
            soundProfile = SutliSoundProfile.HISS_THEN_BLAST,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.94f,
                screenShakePx = 31f,
                shockwaveMaxRadius = 460f,
                smokeColor = Color(0xFF37474F),
                smokeCount = 80,
                fireCount = 82,
                sparkCount = 120,
                scorchRadius = 90f
            )
        ),

        // ==========================================
        // PHASE 3: Advanced Levels (Level 13 to 18) - Loud & Heavy Explosions
        // ==========================================
        SutliLevelDetail(
            level = 13,
            name = "Ground Shaker Bomb",
            phaseName = "Phase 3: Advanced (Loud & Heavy Explosions)",
            description = "Extra-thick jute ropes causing an earth-tremor shake that vibrates the screen.",
            scaleFactor = 1.50f,
            bodyColor = Color(0xFF1B5E20),
            twineColor = Color(0xFF5D4037),
            widthDp = 84f,
            heightDp = 94f,
            soundProfile = SutliSoundProfile.EARTH_RUMBLE,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.95f,
                screenShakePx = 36f,
                shakeDurationMs = 800L, // long ground tremor
                shockwaveMaxRadius = 480f,
                smokeColor = Color(0xFF263238),
                smokeCount = 90,
                fireCount = 90,
                sparkCount = 130,
                hasDustKickup = true,
                scorchRadius = 96f
            )
        ),
        SutliLevelDetail(
            level = 14,
            name = "Black Smoke Heavy Bomb",
            phaseName = "Phase 3: Advanced (Loud & Heavy Explosions)",
            description = "Charcoal black body detonating with a heavy blast and dense pitch-black smoke cloud.",
            scaleFactor = 1.56f,
            bodyColor = Color(0xFF1A1A1A), // pitch black
            twineColor = Color(0xFF424242),
            widthDp = 86f,
            heightDp = 96f,
            soundProfile = SutliSoundProfile.DEEP_BLACK_BOOM,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.92f,
                screenShakePx = 34f,
                shockwaveMaxRadius = 500f,
                smokeColor = Color(0xFF101010), // pitch black smoke!
                smokeCount = 120,
                fireCount = 85,
                sparkCount = 110,
                scorchRadius = 104f
            )
        ),
        SutliLevelDetail(
            level = 15,
            name = "Hybrid Rocket Sutli",
            phaseName = "Phase 3: Advanced (Loud & Heavy Explosions)",
            description = "Rocket-finned hybrid bomb shooting high aerial spark fountains into the sky.",
            scaleFactor = 1.62f,
            bodyColor = Color(0xFF7B1FA2),
            twineColor = Color(0xFFFFAB00),
            widthDp = 76f,
            heightDp = 106f,
            hasRocketCone = true,
            soundProfile = SutliSoundProfile.ROCKET_HYBRID,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.96f,
                screenShakePx = 35f,
                shockwaveMaxRadius = 520f,
                shockwaveColor = Color(0xFFE040FB),
                smokeColor = Color(0xFF37474F),
                smokeCount = 85,
                fireCount = 95,
                sparkCount = 140,
                hasAerialFlares = true, // shoots upward sparks!
                scorchRadius = 110f
            )
        ),
        SutliLevelDetail(
            level = 16,
            name = "Super-Compressed Loud Bang",
            phaseName = "Phase 3: Advanced (Loud & Heavy Explosions)",
            description = "Super-compressed gunpowder casing with an ear-piercing, deafening loud crack.",
            scaleFactor = 1.68f,
            bodyColor = Color(0xFFB71C1C),
            twineColor = Color(0xFFB0BEC5), // steel twine
            widthDp = 90f,
            heightDp = 100f,
            soundProfile = SutliSoundProfile.EAR_PIERCING_BANG,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                flashDurationMs = 280L,
                screenShakePx = 38f,
                shockwaveMaxRadius = 540f,
                smokeColor = Color(0xFF455A64),
                smokeCount = 95,
                fireCount = 105,
                sparkCount = 150,
                scorchRadius = 118f
            )
        ),
        SutliLevelDetail(
            level = 17,
            name = "Wide Radial Cylinder",
            phaseName = "Phase 3: Advanced (Loud & Heavy Explosions)",
            description = "Wide squat demolition barrel spreading dense smoke clouds in all four quadrants.",
            scaleFactor = 1.74f,
            bodyColor = Color(0xFF2E7D32),
            twineColor = Color(0xFFFFD54F),
            widthDp = 104f,
            heightDp = 86f,
            isWide = true,
            soundProfile = SutliSoundProfile.WIDE_BARREL_BLAST,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.96f,
                screenShakePx = 38f,
                shockwaveMaxRadius = 560f,
                smokeColor = Color(0xFF37474F),
                smokeCount = 130, // 4 directions
                fireCount = 110,
                sparkCount = 150,
                scorchRadius = 125f
            )
        ),
        SutliLevelDetail(
            level = 18,
            name = "ROX Demolition Blaster",
            phaseName = "Phase 3: Advanced (Loud & Heavy Explosions)",
            description = "Industrial ROX demolition styling creating a colossal visual blast radius.",
            scaleFactor = 1.80f,
            bodyColor = Color(0xFFE65100),
            twineColor = Color(0xFF212121),
            widthDp = 96f,
            heightDp = 106f,
            isRoxStyle = true,
            soundProfile = SutliSoundProfile.ROX_DEMOLITION,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.98f,
                screenShakePx = 40f,
                shockwaveMaxRadius = 580f,
                smokeColor = Color(0xFF212121),
                smokeCount = 110,
                fireCount = 135, // huge fireball radius
                sparkCount = 160,
                scorchRadius = 135f
            )
        ),

        // ==========================================
        // PHASE 4: Expert & Boss Levels (Level 19 to 24) - Extreme Power
        // ==========================================
        SutliLevelDetail(
            level = 19,
            name = "Giant Sutli Mega",
            phaseName = "Phase 4: Expert (Extreme Power)",
            description = "Monumental giant bomb towering on the pedestal with double braided ropes.",
            scaleFactor = 1.90f,
            bodyColor = Color(0xFF1B5E20),
            twineColor = Color(0xFFFFD54F),
            widthDp = 108f,
            heightDp = 120f,
            soundProfile = SutliSoundProfile.GIANT_THUNDER,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                screenShakePx = 42f,
                shakeDurationMs = 700L,
                shockwaveMaxRadius = 600f,
                smokeColor = Color(0xFF263238),
                smokeCount = 120,
                fireCount = 140,
                sparkCount = 175,
                scorchRadius = 145f
            )
        ),
        SutliLevelDetail(
            level = 20,
            name = "Golden Sparks Ring",
            phaseName = "Phase 4: Expert (Extreme Power)",
            description = "Opulent golden paper bomb bursting into a magnificent expanding halo of golden stars.",
            scaleFactor = 1.98f,
            bodyColor = Color(0xFFFFD700), // metallic gold
            twineColor = Color(0xFFFFF9C4),
            widthDp = 112f,
            heightDp = 124f,
            isGoldStyle = true,
            soundProfile = SutliSoundProfile.GOLDEN_MAJESTIC,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                flashDurationMs = 450L,
                screenShakePx = 42f,
                shockwaveMaxRadius = 620f,
                shockwaveColor = Color(0xFFFFD700),
                smokeColor = Color(0xFFFFF8E1),
                smokeCount = 100,
                fireCount = 130,
                sparkCount = 200,
                hasGoldenRing = true, // golden ring!
                scorchRadius = 150f
            )
        ),
        SutliLevelDetail(
            level = 21,
            name = "Triple-Fuse Sequence",
            phaseName = "Phase 4: Expert (Extreme Power)",
            description = "Multi-fuse bomb detonating with 3 sequential pops leading into the main blast.",
            scaleFactor = 2.05f,
            bodyColor = Color(0xFF4A148C),
            twineColor = Color(0xFFFF5722),
            widthDp = 116f,
            heightDp = 128f,
            isMultiFuse = true,
            soundProfile = SutliSoundProfile.TRIPLE_POP_BANG,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                screenShakePx = 44f,
                shockwaveMaxRadius = 640f,
                smokeColor = Color(0xFF37474F),
                smokeCount = 130,
                fireCount = 150,
                sparkCount = 190,
                scorchRadius = 160f
            )
        ),
        SutliLevelDetail(
            level = 22,
            name = "Nuclear Flash Blast",
            phaseName = "Phase 4: Expert (Extreme Power)",
            description = "Nuclear hazard theme bomb producing a full-screen pure whiteout flash for a full second.",
            scaleFactor = 2.12f,
            bodyColor = Color(0xFFFFD600), // hazard yellow
            twineColor = Color(0xFF000000), // black hazard ropes
            widthDp = 120f,
            heightDp = 132f,
            isNuclearStyle = true,
            soundProfile = SutliSoundProfile.NUCLEAR_APOCALYPSE,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                flashDurationMs = 1000L, // 1 FULL SECOND WHITEOUT!
                screenShakePx = 46f,
                shakeDurationMs = 900L,
                shockwaveMaxRadius = 660f,
                shockwaveColor = Color.White,
                smokeColor = Color(0xFFECEFF1),
                smokeCount = 150,
                fireCount = 160,
                sparkCount = 210,
                hasWhiteoutFlash = true,
                scorchRadius = 170f
            )
        ),
        SutliLevelDetail(
            level = 23,
            name = "Massive Black Bass Rumble",
            phaseName = "Phase 4: Expert (Extreme Power)",
            description = "Colossal obsidian bomb with volcanic fissures, extreme sub-bass, and violent camera shake.",
            scaleFactor = 2.20f,
            bodyColor = Color(0xFF0D0D0D),
            twineColor = Color(0xFFD50000), // glowing red twine
            widthDp = 124f,
            heightDp = 136f,
            soundProfile = SutliSoundProfile.SUB_BASS_QUAKE,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 0.98f,
                screenShakePx = 50f, // intense 50px shake!
                shakeDurationMs = 1000L,
                shockwaveMaxRadius = 680f,
                smokeColor = Color(0xFF1A1A1A),
                smokeCount = 160,
                fireCount = 170,
                sparkCount = 220,
                scorchRadius = 180f
            )
        ),
        SutliLevelDetail(
            level = 24,
            name = "Dual-Layer Pre-Boss",
            phaseName = "Phase 4: Expert (Extreme Power)",
            description = "Armored double-layer core with unique dual-toned acoustic and twin shockwaves.",
            scaleFactor = 2.30f,
            bodyColor = Color(0xFFB71C1C),
            twineColor = Color(0xFFFFD54F),
            widthDp = 132f,
            heightDp = 144f,
            soundProfile = SutliSoundProfile.PRE_BOSS_DUAL,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                screenShakePx = 48f,
                shockwaveMaxRadius = 700f,
                smokeColor = Color(0xFF263238),
                smokeCount = 175,
                fireCount = 185,
                sparkCount = 240,
                scorchRadius = 190f
            )
        ),

        // ==========================================
        // PHASE 5: Level 25 - The Ultimate Mega Bomb (The Final Boss)
        // ==========================================
        SutliLevelDetail(
            level = 25,
            name = "Brahmand Sutli Bomb (FINAL BOSS)",
            phaseName = "Phase 5: The Ultimate Mega Bomb (The Final Boss)",
            description = "Supreme Mega Blast! Vishal giant with red & gold thick ropes, slow-mo tick, screen white flash, colossal shockwave, and multicolor fireworks clouds!",
            scaleFactor = 2.50f,
            bodyColor = Color(0xFF880E4F), // royal deep ruby
            twineColor = Color(0xFFFFD700), // thick golden & red ropes
            widthDp = 150f,
            heightDp = 165f,
            isBossMega = true,
            soundProfile = SutliSoundProfile.MEGA_BOOM_BOSS,
            explosionProfile = SutliExplosionProfile(
                flashIntensity = 1.0f,
                flashDurationMs = 1200L, // long blinding white flash
                screenShakePx = 55f, // ultimate camera shake
                shakeDurationMs = 1400L,
                shockwaveMaxRadius = 800f, // sweeps the entire display!
                shockwaveColor = Color(0xFFFFD700),
                smokeColor = Color(0xFF212121),
                smokeCount = 220,
                fireCount = 240,
                sparkCount = 320,
                hasWhiteoutFlash = true,
                hasFinalFireworks = true, // skyward multicolor bursts!
                hasDustKickup = true,
                scorchRadius = 220f
            )
        )
    )

    fun getDetail(level: Int): SutliLevelDetail {
        val clamped = level.coerceIn(1, 25)
        return levels[clamped - 1]
    }
}
