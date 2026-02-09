// Import Bhubaneswar events to Firestore
// Run: node importEvents.js

const admin = require('firebase-admin');

// Initialize Firebase Admin
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Bhubaneswar Events Data
const events = [
  {
    id: "konark-dance-festival-2026",
    title: "Konark Dance Festival 2026",
    description: "Witness India's finest classical dancers perform Odissi, Bharatanatyam, and Kathak against the stunning backdrop of Konark Sun Temple. 5-day festival celebrating Indian heritage with artists from across India. Open-air performances under the stars!",
    startAt: 1733068800000,
    endAt: 1733500800000,
    areaName: "Konark Sun Temple",
    city: "Bhubaneswar",
    latitude: 19.8876,
    longitude: 86.0945,
    imageUrl: "https://images.unsplash.com/photo-1588596917736-ce8f7a143b8d?w=800",
    category: "Cultural",
    tags: ["Cultural", "Dance", "Heritage", "Tourist Friendly", "Family Friendly", "Temple"],
    isApproved: true
  },
  {
    id: "lingaraj-shivaratri-2026",
    title: "Maha Shivaratri at Lingaraj Temple",
    description: "Experience the grandest Shivaratri celebration in Odisha at the 1000-year-old Lingaraj Temple. All-night prayers, traditional rituals, cultural performances, and free prasad. One of the most sacred temples in India, attracting devotees from across the world.",
    startAt: 1740758400000,
    endAt: 1740844800000,
    areaName: "Lingaraj Temple, Old Town",
    city: "Bhubaneswar",
    latitude: 20.2367,
    longitude: 85.8352,
    imageUrl: "https://images.unsplash.com/photo-1548690596-3e8f8d1f5f56?w=800",
    category: "Festival",
    tags: ["Festival", "Religious", "Cultural", "Heritage", "Temple", "Traditional"],
    isApproved: true
  },
  {
    id: "ekamra-haat-weekend-market",
    title: "Ekamra Haat Weekend Market",
    description: "Odisha's premier handicrafts and food bazaar! Shop authentic Pattachitra paintings, silver filigree jewelry, handloom sarees. Try local delicacies: Chhena Poda, Pakhala Bhata, Rasagola. Open-air market with live folk music and dance performances every weekend.",
    startAt: 1739635200000,
    endAt: 1739656800000,
    areaName: "Ekamra Haat, Kalpana Square",
    city: "Bhubaneswar",
    latitude: 20.2961,
    longitude: 85.8245,
    imageUrl: "https://images.unsplash.com/photo-1567696911980-2eed69a46042?w=800",
    category: "Food",
    tags: ["Food", "Shopping", "Handicrafts", "Tourist Friendly", "Family Friendly", "Local Experience"],
    isApproved: true
  },
  {
    id: "tribal-heritage-festival-march",
    title: "Odisha Tribal Heritage Festival",
    description: "Celebrate the rich tribal culture of Odisha! Experience traditional Sambalpuri dance, tribal art exhibitions, indigenous cuisine, and folk music from 62 tribal communities. Interactive workshops on tribal crafts and weaving. Perfect for cultural enthusiasts!",
    startAt: 1741176600000,
    endAt: 1741435800000,
    areaName: "IDCO Exhibition Ground",
    city: "Bhubaneswar",
    latitude: 20.2961,
    longitude: 85.8245,
    imageUrl: "https://images.unsplash.com/photo-1570026517541-cc96e91fda25?w=800",
    category: "Cultural",
    tags: ["Cultural", "Traditional", "Art", "Music", "Food", "Family Friendly", "Educational"],
    isApproved: true
  },
  {
    id: "temple-heritage-walk-weekly",
    title: "Bhubaneswar Temple Heritage Walk",
    description: "Guided walking tour of ancient temples in Old Town Bhubaneswar. Visit 5 architectural marvels including Mukteshwar, Rajarani, and Parasurameswara temples. Expert guide explains Kalinga architecture, history, and mythology. Complimentary breakfast at a local eatery. Every Sunday morning!",
    startAt: 1739757000000,
    endAt: 1739768400000,
    areaName: "Old Town, Near Bindu Sagar",
    city: "Bhubaneswar",
    latitude: 20.2403,
    longitude: 85.8330,
    imageUrl: "https://images.unsplash.com/photo-1580407196238-dac33f57c410?w=800",
    category: "Cultural",
    tags: ["Heritage", "Temple", "Walking Tour", "Educational", "Morning", "Tourist Friendly", "Photography"],
    isApproved: true
  }
];

// Import function
async function importEvents() {
  console.log('🚀 Starting import of', events.length, 'events...\n');

  for (const event of events) {
    try {
      await db.collection('events').doc(event.id).set(event);
      console.log('✅', event.title);
    } catch (error) {
      console.error('❌ Failed to import:', event.title);
      console.error('   Error:', error.message);
    }
  }

  console.log('\n🎉 Import complete!');
  process.exit(0);
}

// Run import
importEvents();
