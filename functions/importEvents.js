// Import Bhubaneswar events to Firestore
// Run: node importEvents.js

const admin = require('firebase-admin');

// Initialize Firebase Admin
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Bhubaneswar Events Data - February 2026 Upcoming Events
const events = [
  // TODAY - Feb 13, 2026
  {
    id: "odissi-concert-tonight-feb13",
    title: "Odissi Dance Concert - Tonight!",
    description: "Renowned Odissi dancer Sujata Mohapatra performs at Rabindra Mandap tonight! Experience the grace and beauty of Odisha's classical dance form. Special performance includes rare pieces from Geeta Govinda. Tickets available at the venue.",
    startAt: 1770989400000, // Feb 13, 2026 7:00 PM IST
    endAt: 1770996600000,  // Feb 13, 2026 9:00 PM IST
    areaName: "Rabindra Mandap",
    city: "Bhubaneswar",
    latitude: 20.2961,
    longitude: 85.8245,
    imageUrl: "https://images.unsplash.com/photo-1588596917736-ce8f7a143b8d?w=800",
    category: "Cultural",
    tags: ["Cultural", "Dance", "Music", "Evening", "Live Performance", "Traditional"],
    isApproved: true
  },
  
  // THIS WEEKEND - Feb 15, 2026 (Saturday)
  {
    id: "ekamra-haat-weekend-feb15",
    title: "Ekamra Haat Weekend Market",
    description: "Odisha's premier handicrafts and food bazaar! Shop authentic Pattachitra paintings, silver filigree jewelry, handloom sarees. Try local delicacies: Chhena Poda, Pakhala Bhata, Rasagola. Open-air market with live folk music performances.",
    startAt: 1771129800000, // Feb 15, 2026 10:00 AM IST
    endAt: 1771165800000,  // Feb 15, 2026 8:00 PM IST
    areaName: "Ekamra Haat, Kalpana Square",
    city: "Bhubaneswar",
    latitude: 20.2961,
    longitude: 85.8245,
    imageUrl: "https://images.unsplash.com/photo-1567696911980-2eed69a46042?w=800",
    category: "Food",
    tags: ["Food", "Shopping", "Handicrafts", "Weekend", "Family Friendly", "Local Experience"],
    isApproved: true
  },

  // THIS WEEKEND - Feb 16, 2026 (Sunday)
  {
    id: "temple-heritage-walk-feb16",
    title: "Bhubaneswar Temple Heritage Walk",
    description: "Guided walking tour of ancient temples in Old Town. Visit Mukteshwar, Rajarani, and Parasurameswara temples. Expert guide explains Kalinga architecture and history. Complimentary breakfast at local eatery. Perfect Sunday morning activity!",
    startAt: 1771205400000, // Feb 16, 2026 7:00 AM IST
    endAt: 1771216800000,  // Feb 16, 2026 10:10 AM IST
    areaName: "Old Town, Near Bindu Sagar",
    city: "Bhubaneswar",
    latitude: 20.2403,
    longitude: 85.8330,
    imageUrl: "https://images.unsplash.com/photo-1580407196238-dac33f57c410?w=800",
    category: "Cultural",
    tags: ["Heritage", "Temple", "Walking Tour", "Morning", "Tourist Friendly", "Photography"],
    isApproved: true
  },

  // NEXT WEEK - Feb 20, 2026 (Friday)
  {
    id: "food-festival-feb20",
    title: "Bhubaneswar Street Food Festival",
    description: "Celebrate Odisha's culinary heritage! 50+ stalls serving traditional dishes: Dahibara Aloodum, Cuttack Thunka Puri, Bhubaneswar Chhena Poda, and more. Live cooking demonstrations, food competitions, and cultural performances. 3-day food extravaganza!",
    startAt: 1771590600000, // Feb 20, 2026 6:00 PM IST
    endAt: 1771878600000,  // Feb 23, 2026 6:00 PM IST
    areaName: "IDCO Exhibition Ground",
    city: "Bhubaneswar",
    latitude: 20.2961,
    longitude: 85.8245,
    imageUrl: "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800",
    category: "Food",
    tags: ["Food", "Festival", "Street Food", "Family Friendly", "Live Performance", "Weekend"],
    isApproved: true
  },

  // NEXT WEEK - Feb 22, 2026 (Sunday)
  {
    id: "music-festival-feb22",
    title: "Odisha Folk Music Festival",
    description: "Experience authentic Sambalpuri, Desia, and tribal folk music! Artists from across Odisha perform traditional songs and dances. Special performances by Padma Shri awardees. Open-air concert under the stars. Free entry!",
    startAt: 1771763400000, // Feb 22, 2026 6:00 PM IST
    endAt: 1771777800000,  // Feb 22, 2026 10:00 PM IST
    areaName: "Nandankanan Road Amphitheatre",
    city: "Bhubaneswar",
    latitude: 20.3974,
    longitude: 85.8172,
    imageUrl: "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800",
    category: "Music",
    tags: ["Music", "Folk", "Cultural", "Free Entry", "Evening", "Traditional"],
    isApproved: true
  },

  // LATER THIS MONTH - Feb 25, 2026
  {
    id: "art-exhibition-feb25",
    title: "Pattachitra Art Exhibition & Workshop",
    description: "Explore Odisha's ancient scroll painting art form! Exhibition showcasing works by master artists from Raghurajpur village. Live demonstrations and hands-on workshops. Learn to paint your own Pattachitra! Art for sale, materials provided for workshops.",
    startAt: 1772007000000, // Feb 25, 2026 10:00 AM IST
    endAt: 1772213400000,  // Feb 27, 2026 7:30 PM IST
    areaName: "State Museum, Bhubaneswar",
    city: "Bhubaneswar",
    latitude: 20.2645,
    longitude: 85.8339,
    imageUrl: "https://images.unsplash.com/photo-1547891654-e66ed7ebb968?w=800",
    category: "Cultural",
    tags: ["Art", "Workshop", "Cultural", "Educational", "Family Friendly", "Traditional"],
    isApproved: true
  },

  // END OF MONTH - Feb 28, 2026
  {
    id: "lingaraj-festival-feb28",
    title: "Lingaraj Temple Annual Festival",
    description: "Sacred annual celebration at the iconic 1000-year-old Lingaraj Temple. Traditional rituals, devotional music, and cultural performances. Free prasad distribution. One of the most important religious events in Bhubaneswar. All are welcome!",
    startAt: 1772238600000, // Feb 28, 2026 6:00 AM IST
    endAt: 1772289000000,  // Feb 28, 2026 8:00 PM IST
    areaName: "Lingaraj Temple, Old Town",
    city: "Bhubaneswar",
    latitude: 20.2367,
    longitude: 85.8352,
    imageUrl: "https://images.unsplash.com/photo-1548690596-3e8f8d1f5f56?w=800",
    category: "Festival",
    tags: ["Festival", "Religious", "Temple", "Cultural", "Traditional", "Heritage"],
    isApproved: true
  },

  // MARCH PREVIEW - Mar 1, 2026
  {
    id: "tribal-heritage-festival-mar1",
    title: "Odisha Tribal Heritage Festival",
    description: "Celebrate the rich culture of Odisha's 62 tribal communities! Traditional Sambalpuri dance, tribal art exhibitions, indigenous cuisine, and folk music. Interactive workshops on tribal crafts and weaving. 3-day cultural extravaganza!",
    startAt: 1772346600000, // Mar 1, 2026 12:00 PM IST
    endAt: 1772605800000,  // Mar 4, 2026 12:00 PM IST
    areaName: "Kalinga Stadium Complex",
    city: "Bhubaneswar",
    latitude: 20.2822,
    longitude: 85.8253,
    imageUrl: "https://images.unsplash.com/photo-1570026517541-cc96e91fda25?w=800",
    category: "Cultural",
    tags: ["Cultural", "Traditional", "Art", "Music", "Food", "Family Friendly", "Festival"],
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
