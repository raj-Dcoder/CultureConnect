package com.rajveer.cultureconnect.features.explore

/**
 * Hardcoded explore data for Bhubaneswar.
 * Why hardcoded? Places, food spots, and highlights rarely change.
 * This keeps the app fast, offline-friendly, and free (no Firestore reads).
 */

data class ExploreItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val location: String,
    val tags: List<String> = emptyList()
)

// ───────────────────────────────────────────
// 📸 City Highlights — Iconic Bhubaneswar
// ───────────────────────────────────────────
val cityHighlights = listOf(
    ExploreItem(
        id = "highlight-lingaraj",
        title = "Lingaraj Temple",
        description = "The largest and most important temple in Bhubaneswar, dating back to 1000 AD. A masterpiece of Kalinga architecture dedicated to Lord Shiva.",
        imageUrl = "https://images.unsplash.com/photo-1626621331169-5f34be280ed9?w=800",
        location = "Old Town, Bhubaneswar",
        tags = listOf("Heritage", "Temple", "Must Visit")
    ),
    ExploreItem(
        id = "highlight-dhauli",
        title = "Dhauli Shanti Stupa",
        description = "Historic peace pagoda built on the site where Emperor Ashoka embraced Buddhism after the Kalinga War. Panoramic views of the Daya River valley.",
        imageUrl = "https://images.unsplash.com/photo-1590766940554-634509a31e4d?w=800",
        location = "Dhauli Hills, Bhubaneswar",
        tags = listOf("Heritage", "Peace", "Viewpoint")
    ),
    ExploreItem(
        id = "highlight-nandankanan",
        title = "Nandankanan Zoological Park",
        description = "Home to the rare white tiger! One of India's premier zoos with a botanical garden spanning 400 hectares. Perfect for families.",
        imageUrl = "https://images.unsplash.com/photo-1551316679-9c6ae9dec224?w=800",
        location = "Nandankanan Road, Bhubaneswar",
        tags = listOf("Nature", "Family", "Wildlife")
    ),
    ExploreItem(
        id = "highlight-udayagiri",
        title = "Udayagiri & Khandagiri Caves",
        description = "Ancient Jain rock-cut caves dating back to 2nd century BCE. Exquisite carvings depicting courtly scenes, elephants, and warriors.",
        imageUrl = "https://images.unsplash.com/photo-1600011250309-4e5e2b743598?w=800",
        location = "Khandagiri, Bhubaneswar",
        tags = listOf("Heritage", "Caves", "History")
    )
)

// ───────────────────────────────────────────
// 🏛️ Places to Visit
// ───────────────────────────────────────────
val placesToVisit = listOf(
    ExploreItem(
        id = "place-mukteshwar",
        title = "Mukteshwar Temple",
        description = "Known as the 'Gem of Odisha Architecture'. The ornate torana (archway) is the finest example of Kalinga decorative art.",
        imageUrl = "https://images.unsplash.com/photo-1585135497273-1a86d9d2fecc?w=800",
        location = "Old Town, Bhubaneswar",
        tags = listOf("Temple", "Architecture", "Photography")
    ),
    ExploreItem(
        id = "place-rajarani",
        title = "Rajarani Temple",
        description = "Famous for its stunning sandstone carvings of nymphs and couples. Often called the 'Love Temple' of Bhubaneswar.",
        imageUrl = "https://images.unsplash.com/photo-1580407196238-dac33f57c410?w=800",
        location = "Tankapani Road, Bhubaneswar",
        tags = listOf("Temple", "Art", "Romantic")
    ),
    ExploreItem(
        id = "place-state-museum",
        title = "Odisha State Museum",
        description = "Houses rare manuscripts, tribal artifacts, ancient coins, and Pattachitra paintings. A deep dive into Odisha's rich history and culture.",
        imageUrl = "https://images.unsplash.com/photo-1554907984-15263bfd63bd?w=800",
        location = "Lewis Road, Bhubaneswar",
        tags = listOf("Museum", "History", "Art")
    ),
    ExploreItem(
        id = "place-ekamra-kanan",
        title = "Ekamra Kanan Botanical Garden",
        description = "Lush 200-acre botanical garden with a cactus house, Japanese garden, and rose garden. Perfect for morning walks and picnics.",
        imageUrl = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=800",
        location = "Nayapalli, Bhubaneswar",
        tags = listOf("Nature", "Garden", "Peaceful")
    ),
    ExploreItem(
        id = "place-bindusagar",
        title = "Bindu Sagar Lake",
        description = "Sacred lake surrounded by ancient temples in Old Town. Believed to contain water from every holy river in India.",
        imageUrl = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800",
        location = "Old Town, Bhubaneswar",
        tags = listOf("Lake", "Sacred", "Scenic")
    )
)

// ───────────────────────────────────────────
// 🍜 Food Spots
// ───────────────────────────────────────────
val foodSpots = listOf(
    ExploreItem(
        id = "food-dalma",
        title = "Dalma Restaurant",
        description = "Authentic Odia thali with Dalma, Pakhala Bhata, Saga Bhaja, and Chhena Poda. The taste of Odisha on one plate!",
        imageUrl = "https://images.unsplash.com/photo-1567337710282-00832b415979?w=800",
        location = "Saheed Nagar, Bhubaneswar",
        tags = listOf("Odia Cuisine", "Thali", "Vegetarian")
    ),
    ExploreItem(
        id = "food-manek-chowk",
        title = "Unit 1 Street Food Hub",
        description = "Bhubaneswar's famous street food corner! Try Dahibara Aloodum, Gupchup (Panipuri), Cuttack Chaat, and Bara Ghuguni.",
        imageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=800",
        location = "Unit 1, Master Canteen Square",
        tags = listOf("Street Food", "Budget", "Must Try")
    ),
    ExploreItem(
        id = "food-chhena-poda",
        title = "Pahala Rasagola Shops",
        description = "Stop at Pahala on NH16 for the world-famous Odisha Rasagola and Chhena Poda (burnt cheese cake). A roadside legend!",
        imageUrl = "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=800",
        location = "Pahala, NH16",
        tags = listOf("Sweets", "Iconic", "Dessert")
    ),
    ExploreItem(
        id = "food-wildgrass",
        title = "Wildgrass Restaurant",
        description = "Upscale Odia dining with tribal-inspired ambiance. Try the bamboo chicken, patra poda fish, and mandia pitha. Great for special occasions.",
        imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
        location = "Jaydev Vihar, Bhubaneswar",
        tags = listOf("Fine Dining", "Odia Cuisine", "Premium")
    )
)

// ───────────────────────────────────────────
// 🗺️ Experiences
// ───────────────────────────────────────────
val experiences = listOf(
    ExploreItem(
        id = "exp-temple-walk",
        title = "Old Town Temple Walking Tour",
        description = "Walk through 1000+ years of history! Visit Lingaraj, Mukteshwar, Rajarani, and Parasurameswara temples with expert commentary on Kalinga architecture.",
        imageUrl = "https://images.unsplash.com/photo-1569949381669-ecf31ae8e613?w=800",
        location = "Old Town, Bhubaneswar",
        tags = listOf("Walking Tour", "3 Hours", "Heritage")
    ),
    ExploreItem(
        id = "exp-pattachitra",
        title = "Pattachitra Painting Workshop",
        description = "Learn the 3000-year-old scroll painting art from master artists of Raghurajpur. Take home your own Pattachitra masterpiece!",
        imageUrl = "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800",
        location = "Raghurajpur Heritage Village",
        tags = listOf("Workshop", "Art", "Hands-on")
    ),
    ExploreItem(
        id = "exp-tribal-village",
        title = "Tribal Village Visit",
        description = "Experience the indigenous lifestyle of Odisha's tribal communities. Authentic food, traditional dance, and craft demonstrations.",
        imageUrl = "https://images.unsplash.com/photo-1570026517541-cc96e91fda25?w=800",
        location = "Outskirts of Bhubaneswar",
        tags = listOf("Cultural", "Full Day", "Unique")
    )
)
