// Import Bhubaneswar events to Firestore
// Run: node importEvents.js

const admin = require('firebase-admin');

// Initialize Firebase Admin
// Make sure you have your serviceAccountKey.json in the same folder! 📂
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// 📅 Bhubaneswar Events Data - Late Feb & March 2026
// Current Context: It is Feb 17, 2026 (Tuesday)

const events = [
  // THIS FRIDAY - Feb 20, 2026
  {
    id: "ekamra-utsav-feb20",
    title: "Ekamra Utsav: Sufi Night",
    description: "Kick off the weekend with a soulful evening at the Ekamra Utsav. Featuring renowned Sufi vocalists performing against the backdrop of ancient carvings. A magical blend of history and melody.",
    startAt: 1771593000000, // Feb 20, 2026 6:30 PM IST
    endAt: 1771607400000,   // Feb 20, 2026 10:30 PM IST
    areaName: "IDCO Exhibition Ground",
    city: "Bhubaneswar",
    latitude: 20.2961,
    longitude: 85.8245,
    imageUrl: "https://images.unsplash.com/photo-1514525253440-b393452e8d26?w=800", // Live music vibe
    category: "Music",
    tags: ["Concert", "Sufi", "Cultural", "Nightlife", "Open Air"],
    isApproved: true
  },

  // THIS SATURDAY - Feb 21, 2026
  {
    id: "yogini-heritage-tour-feb21",
    title: "Mystic Chausathi Yogini Tour",
    description: "A guided sunrise tour to the mysterious Chausathi Yogini temple in Hirapur (outskirts of BBS). Learn about the Tantric heritage of Odisha. Includes AC transport and Odia breakfast.",
    startAt: 1771635600000, // Feb 21, 2026 6:00 AM IST
    endAt: 1771650000000,   // Feb 21, 2026 10:00 AM IST
    areaName: "Hirapur (Pickup at Master Canteen)",
    city: "Bhubaneswar",
    latitude: 20.2285,
    longitude: 85.8770,
    imageUrl: "https://images.unsplash.com/photo-1621258217260-64215354506c?w=800", // Stone temple texture
    category: "Heritage",
    tags: ["History", "Guided Tour", "Photography", "Morning", "Bus Tour"],
    isApproved: true
  },

  // THIS SUNDAY - Feb 22, 2026
  {
    id: "patha-utsav-janpath-feb22",
    title: "Patha Utsav (Street Fest)",
    description: "Janpath transforms into a vehicle-free zone! Join thousands of locals for cycling, skating, rangoli making, and Zumba on the street. The happiest way to start your Sunday.",
    startAt: 1771720200000, // Feb 22, 2026 5:30 AM IST
    endAt: 1771736400000,   // Feb 22, 2026 10:00 AM IST
    areaName: "Janpath Road",
    city: "Bhubaneswar",
    latitude: 20.2783,
    longitude: 85.8422,
    imageUrl: "https://images.unsplash.com/photo-1552308995-2baac1ad5490?w=800", // Street activity
    category: "Community",
    tags: ["Fitness", "Kids", "Street Event", "Free", "Morning", "Zumba"],
    isApproved: true
  },

  // NEXT WEEKEND - Feb 27, 2026
  {
    id: "crafts-bazaar-feb27",
    title: "National Crafts Bazaar",
    description: "End of season sale! Artisans from 20 states showcasing terracotta, dhokra casting, and handlooms. Great place to buy home decor and authentic Sambalpuri textiles.",
    startAt: 1772190000000, // Feb 27, 2026 4:00 PM IST
    endAt: 1772208000000,   // Feb 27, 2026 9:00 PM IST
    areaName: "Janata Maidan",
    city: "Bhubaneswar",
    latitude: 20.2980,
    longitude: 85.8235,
    imageUrl: "https://images.unsplash.com/photo-1606293926075-69a00dbfde81?w=800", // Handicrafts
    category: "Shopping",
    tags: ["Shopping", "Handicrafts", "Market", "Fair", "Family"],
    isApproved: true
  },

  // BIG FESTIVAL - March 3, 2026
  {
    id: "dola-purnima-holi-mar03",
    title: "Dola Purnima & Holi Gathering",
    description: "Celebrate the Festival of Colors! Join the grand Dola Jatra procession in Old Town followed by organic color play. Thandai, music, and pure joy.",
    startAt: 1772506800000, // Mar 03, 2026 8:00 AM IST
    endAt: 1772521200000,   // Mar 03, 2026 12:00 PM IST
    areaName: "Old Town (Near Lingaraj)",
    city: "Bhubaneswar",
    latitude: 20.2367,
    longitude: 85.8352,
    imageUrl: "https://images.unsplash.com/photo-1520182103507-6c2e36506a72?w=800", // Holi colors
    category: "Festival",
    tags: ["Holi", "Colors", "Traditional", "Fun", "Photography", "Major Event"],
    isApproved: true
  }
];

// Import function
async function importEvents() {
  console.log(`🚀 Starting import of ${events.length} events for Feb/Mar 2026...\n`);

  let successCount = 0;

  for (const event of events) {
    try {
      // Using .set() with {merge: true} prevents overwriting if you run it twice casually
      await db.collection('events').doc(event.id).set(event, { merge: true });
      console.log(`✅ [Imported] ${event.title}`);
      successCount++;
    } catch (error) {
      console.error(`❌ [Failed] ${event.title}`);
      console.error(`   Error: ${error.message}`);
    }
  }

  console.log(`\n🎉 All done! ${successCount}/${events.length} events are live in Firestore.`);
  console.log(`📅 Date check: The next event is on Feb 20th.`);
  process.exit(0);
}

// Run import
importEvents();