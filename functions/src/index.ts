import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import axios from "axios";

// Initialize Firebase Admin SDK
admin.initializeApp();

/**
 * Request data from Android app
 */
interface FareRequest {
    originLat: number;      // Pickup latitude
    originLng: number;      // Pickup longitude
    destLat: number;        // Destination latitude
    destLng: number;        // Destination longitude
}

/**
 * Single provider's fare estimate
 */
interface FareEstimate {
    provider: string;       // "Ola Mini", "Uber Go", etc.
    category: string;       // "Economy", "Premium", etc.
    estimatedFare: number;  // Calculated fare in INR
    currency: string;       // "INR"
    distance: string;       // "5.2 km"
    duration: string;       // "15 mins"
    deepLink: string;       // URL to open provider app
}

/**
 * Complete response to Android app
 */
interface FareResponse {
    providers: FareEstimate[];  // Array of all provider estimates
    distance: {
        value: number;    // Distance in meters
        text: string;     // "5.2 km"
    };
    duration: {
        value: number;    // Duration in seconds
        text: string;     // "15 mins"
    };
}

/**
 * Provider pricing configuration
 */
interface ProviderConfig {
    name: string;
    category: string;
    baseFare: number;      // Base charge in INR
    perKmRate: number;     // Rate per kilometer
    perMinRate: number;    // Rate per minute
}

/**
 * Pricing configurations for all providers
 * Based on publicly available rate cards (simplified)
 */
const PROVIDERS: ProviderConfig[] = [
    // Ola providers
    {
        name: "Ola Mini",
        category: "Economy",
        baseFare: 40,
        perKmRate: 12,
        perMinRate: 2,
    },
    {
        name: "Ola Prime Sedan",
        category: "Premium",
        baseFare: 60,
        perKmRate: 15,
        perMinRate: 2.5,
    },
    {
        name: "Ola Auto",
        category: "Auto",
        baseFare: 25,
        perKmRate: 10,
        perMinRate: 1.5,
    },

    // Uber providers
    {
        name: "Uber Go",
        category: "Economy",
        baseFare: 45,
        perKmRate: 13,
        perMinRate: 2,
    },
    {
        name: "Uber Premier",
        category: "Premium",
        baseFare: 70,
        perKmRate: 18,
        perMinRate: 3,
    },

    // Rapido providers
    {
        name: "Rapido Bike",
        category: "Bike",
        baseFare: 15,
        perKmRate: 8,
        perMinRate: 1,
    },
    {
        name: "Rapido Auto",
        category: "Auto",
        baseFare: 20,
        perKmRate: 9,
        perMinRate: 1.2,
    },
];

/**
 * Call Google Directions API to get distance and duration
 * 
 * @param originLat - Pickup latitude
 * @param originLng - Pickup longitude
 * @param destLat - Destination latitude
 * @param destLng - Destination longitude
 * @returns Distance and duration information
 */
async function getDirections(
    originLat: number,
    originLng: number,
    destLat: number,
    destLng: number
): Promise<{ distance: { value: number; text: string }; duration: { value: number; text: string } }> {

    // Get API key from Firebase configuration
    const apiKey = functions.config().google?.directions_api_key;

    console.log("🔑 API Key configured:", apiKey ? "YES" : "NO");
    console.log("📍 Calling Directions API with:", { originLat, originLng, destLat, destLng });

    if (!apiKey) {
        console.error("❌ API key not found in Firebase config");
        throw new Error("GOOGLE_DIRECTIONS_API_KEY not configured");
    }

    // Build the API URL
    const url = "https://maps.googleapis.com/maps/api/directions/json";

    try {
        // Make the API call
        const response = await axios.get(url, {
            params: {
                origin: `${originLat},${originLng}`,
                destination: `${destLat},${destLng}`,
                mode: "driving",
                key: apiKey,
            },
        });

        console.log("📡 Directions API response status:", response.data.status);

        // Check if API call was successful
        if (response.data.status !== "OK") {
            console.error("❌ Directions API error:", response.data.status, response.data.error_message);
            throw new Error(`Directions API error: ${response.data.status} - ${response.data.error_message || 'Unknown error'}`);
        }

        // Extract the first route (usually the best route)
        const route = response.data.routes[0];
        if (!route) {
            console.error("❌ No route found in response");
            throw new Error("No route found");
        }

        // Get distance and duration from the route
        const leg = route.legs[0];

        console.log("✅ Route found:", leg.distance.text, leg.duration.text);

        return {
            distance: {
                value: leg.distance.value,  // Distance in meters
                text: leg.distance.text,    // "18.5 km"
            },
            duration: {
                value: leg.duration.value,  // Duration in seconds
                text: leg.duration.text,    // "30 mins"
            },
        };
    } catch (error: any) {
        console.error("❌ Error calling Directions API:", error.message);
        if (error.response) {
            console.error("API Response:", error.response.data);
        }
        throw error;
    }
}

/**
 * Calculate fare for a single provider
 * 
 * @param provider - Provider configuration (rates)
 * @param distanceMeters - Distance in meters
 * @param durationSeconds - Duration in seconds
 * @param originLat - Origin latitude (for deep link)
 * @param originLng - Origin longitude (for deep link)
 * @param destLat - Destination latitude (for deep link)
 * @param destLng - Destination longitude (for deep link)
 * @returns Fare estimate for this provider
 */
function calculateFare(
    provider: ProviderConfig,
    distanceMeters: number,
    durationSeconds: number,
    originLat: number,
    originLng: number,
    destLat: number,
    destLng: number
): FareEstimate {
    // Convert meters to kilometers
    const distanceKm = distanceMeters / 1000;

    // Convert seconds to minutes
    const durationMin = durationSeconds / 60;

    // Apply the fare formula
    const fare =
        provider.baseFare +
        (distanceKm * provider.perKmRate) +
        (durationMin * provider.perMinRate);

    // Round to nearest rupee
    const estimatedFare = Math.round(fare);

    // Generate deep link for the provider
    const deepLink = generateDeepLink(
        provider.name,
        originLat,
        originLng,
        destLat,
        destLng
    );

    return {
        provider: provider.name,
        category: provider.category,
        estimatedFare: estimatedFare,
        currency: "INR",
        distance: `${distanceKm.toFixed(1)} km`,
        duration: `${Math.round(durationMin)} mins`,
        deepLink: deepLink,
    };
}

/**
 * Generate deep link to open provider app
 * 
 * @param providerName - Name of the provider
 * @param originLat - Pickup latitude
 * @param originLng - Pickup longitude
 * @param destLat - Destination latitude
 * @param destLng - Destination longitude
 * @returns Deep link URL
 */
function generateDeepLink(
    providerName: string,
    originLat: number,
    originLng: number,
    destLat: number,
    destLng: number
): string {
    // Determine which provider and generate appropriate deep link
    if (providerName.startsWith("Ola")) {
        // Ola deep link format - correct scheme
        return `olacabs://app/launch?lat=${originLat}&lng=${originLng}&drop_lat=${destLat}&drop_lng=${destLng}`;
    } else if (providerName.startsWith("Uber")) {
        // Uber deep link format
        return `uber://?action=setPickup&pickup[latitude]=${originLat}&pickup[longitude]=${originLng}&dropoff[latitude]=${destLat}&dropoff[longitude]=${destLng}`;
    } else if (providerName.startsWith("Rapido")) {
        // Rapido HTTPS link - works whether app is installed or not
        return `https://rapido.bike/ride/share?pickup_lat=${originLat}&pickup_lng=${originLng}&drop_lat=${destLat}&drop_lng=${destLng}`;
    } else {
        // Fallback - return empty string
        return "";
    }
}

/**
 * Main Cloud Function - Calculate ride fares
 * 
 * This is the HTTP callable function that Android app will invoke
 * 
 * @param data - Request data from Android (origin and destination coordinates)
 * @param context - Firebase context (authentication, etc.)
 * @returns Fare estimates for all providers
 */
export const calculateFares = functions.https.onCall(
    async (data: FareRequest, context): Promise<FareResponse> => {

        // Log the request (helpful for debugging)
        console.log("🚀 calculateFares called with:", data);

        // Validate input data
        if (!data.originLat || !data.originLng || !data.destLat || !data.destLng) {
            console.error("❌ Missing required coordinates");
            throw new functions.https.HttpsError(
                "invalid-argument",
                "Missing required coordinates"
            );
        }

        try {
            // Step 1: Get distance and duration from Directions API
            console.log("📍 Calling Directions API...");
            const directions = await getDirections(
                data.originLat,
                data.originLng,
                data.destLat,
                data.destLng
            );

            console.log(`✅ Route found: ${directions.distance.text}, ${directions.duration.text}`);

            // Step 2: Calculate fares for all providers
            console.log("💰 Calculating fares for all providers...");
            const fareEstimates: FareEstimate[] = PROVIDERS.map((provider) =>
                calculateFare(
                    provider,
                    directions.distance.value,
                    directions.duration.value,
                    data.originLat,
                    data.originLng,
                    data.destLat,
                    data.destLng
                )
            );

            // Step 3: Sort by price (cheapest first)
            fareEstimates.sort((a, b) => a.estimatedFare - b.estimatedFare);

            console.log(`✅ Calculated ${fareEstimates.length} fare estimates`);

            // Step 4: Return the response
            return {
                providers: fareEstimates,
                distance: directions.distance,
                duration: directions.duration,
            };

        } catch (error: any) {
            // Log the error
            console.error("❌ Error calculating fares:", error.message);
            console.error("Stack:", error.stack);

            // Throw a user-friendly error
            throw new functions.https.HttpsError(
                "internal",
                `Failed to calculate fares: ${error.message}`
            );
        }
    }
);