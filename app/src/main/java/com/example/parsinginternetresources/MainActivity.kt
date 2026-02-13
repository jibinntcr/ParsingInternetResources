package com.example.parsinginternetresources

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parsinginternetresources.ui.theme.ParsingInternetResourcesTheme
// --- NETWORK LIBRARIES ---
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

/**
 * ====================================================================
 * SECTION 1: DATA MODEL
 * ====================================================================
 * This Data Class represents the structure of the JSON object we expect
 * from the server.
 *
 * JSON Example: { "id": 1, "title": "...", "body": "..." }
 * Kotlin Mapping:
 * - 'id' maps to 'val id: Int'
 * - 'title' maps to 'val title: String'
 */
data class Post(
    val id: Int,
    val title: String,
    val body: String
)

/**
 * ====================================================================
 * SECTION 2: API INTERFACE (The Contract)
 * ====================================================================
 * This interface defines HOW we talk to the server.
 * Retrofit uses this to generate the network code automatically.
 */
interface ApiService {
    // @GET("posts") tells Retrofit to add "/posts" to the base URL.
    // 'suspend' keyword allows this function to pause and resume, preventing
    // the UI from freezing while waiting for the internet response.
    @GET("posts")
    suspend fun getPosts(): List<Post>
}

/**
 * ====================================================================
 * SECTION 3: NETWORK CLIENT SETUP (Singleton)
 * ====================================================================
 * We use a 'Singleton' (object) to ensure we only create ONE instance
 * of Retrofit, which saves memory and resources.
 */
object RetrofitInstance {
    // 1. Setup Moshi: The library that converts JSON text -> Kotlin Objects
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // essential for Kotlin data classes
        .build()

    // 2. Setup Retrofit: The library that handles the HTTP connection
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/") // The Server URL
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Attach Moshi
            .build()
            .create(ApiService::class.java) // Create the implementation of our interface
    }
}

/**
 * ====================================================================
 * SECTION 4: USER INTERFACE (PAGINATED VIEW)
 * ====================================================================
 */

// BITS Pilani Theme Colors
val BitsBlue = Color(0xFF2B2B88)
val BitsGold = Color(0xFFE99B2D)
val LightGold = Color(0xFFFFF8E1)
val LightGreen = Color(0xFFF0F4C3)
val LightGrey = Color(0xFFF5F5F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParsingInternetResourcesTheme {
                // Scaffold provides a standard layout structure with TopBar and BottomBar
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { HeaderSection() },
                    bottomBar = { FooterSection() }
                ) { innerPadding ->
                    // Pass the padding from Scaffold to our screen content
                    PaginatedParsingScreen(paddingValues = innerPadding)
                }
            }
        }
    }
}

@Composable
fun PaginatedParsingScreen(paddingValues: PaddingValues) {
    // --- STATE MANAGEMENT ---
    // 'allPosts': Holds ALL 100 posts downloaded from the internet.
    var allPosts by remember { mutableStateOf<List<Post>>(emptyList()) }

    // Pagination Controls
    var currentPage by remember { mutableIntStateOf(0) } // Starts at page 0
    val itemsPerPage = 6 // Only show 6 items per screen so it fits nicely

    // Status & Loading State
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Ready to Download Data") }
    val scope = rememberCoroutineScope()

    // --- PAGINATION LOGIC ---
    // Calculate total pages: (100 items / 6 per page) = ~17 pages
    val totalPages = if (allPosts.isEmpty()) 0 else (allPosts.size + itemsPerPage - 1) / itemsPerPage

    // Slice the big list to get ONLY the items for the current page
    val currentDisplayList = allPosts
        .drop(currentPage * itemsPerPage)
        .take(itemsPerPage)

    // --- MAIN LAYOUT ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues) // Respect Header/Footer padding
            .padding(16.dp), // Internal screen padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. EDUCATIONAL CARDS (Only show if we haven't downloaded data yet)
        if (allPosts.isEmpty() && !isLoading) {
            EducationalCard(
                title = "Concept: JSON Parsing",
                description = "We will fetch raw text from 'jsonplaceholder.typicode.com/posts' and convert it into Kotlin Objects using Retrofit & Moshi."
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeMappingCard()
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. STATUS & DOWNLOAD BUTTON
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.labelMedium,
            color = if (isLoading) BitsGold else Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Show button only if data is empty (Start Screen)
        if (allPosts.isEmpty() && !isLoading) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        statusMessage = "Downloading 100 items from Server..."
                        delay(1000) // Artificial delay for demo effect
                        try {
                            // --- NETWORK CALL ---
                            allPosts = RetrofitInstance.api.getPosts()
                            statusMessage = "Success! Loaded ${allPosts.size} Posts."
                            currentPage = 0 // Reset to first page
                        } catch (e: Exception) {
                            statusMessage = "Error: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BitsBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Download")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. LOADING SPINNER
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BitsGold)
            }
        }

        // 4. THE DATA LIST (PAGINATED)
        if (currentDisplayList.isNotEmpty()) {

            // Educational Header for the List
            Text(
                text = "▼ Parsed Kotlin Objects (Page ${currentPage + 1})",
                style = MaterialTheme.typography.titleSmall,
                color = BitsBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Efficient List View
            LazyColumn(
                modifier = Modifier.weight(1f), // Takes up all remaining space
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentDisplayList) { post ->
                    PostItem(post)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. PAGINATION CONTROLS (Next / Prev Buttons)
            PaginationControls(
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevious = { if (currentPage > 0) currentPage-- },
                onNext = { if (currentPage < totalPages - 1) currentPage++ }
            )
        }
    }
}

/**
 * ====================================================================
 * HELPER UI COMPONENTS
 * ====================================================================
 */

@Composable
fun EducationalCard(title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightGold),
        border = BorderStroke(1.dp, BitsGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BitsBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = BitsBlue)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CodeMappingCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("JSON to Kotlin Mapping:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
            Text("JSON \"id\"    ->  val id: Int", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Text("JSON \"title\" ->  val title: String", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PREVIOUS BUTTON
        Button(
            onClick = onPrevious,
            enabled = currentPage > 0, // Disable if on first page
            colors = ButtonDefaults.buttonColors(containerColor = BitsBlue)
        ) {
            Text("< Prev")
        }

        // PAGE INDICATOR
        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // NEXT BUTTON
        Button(
            onClick = onNext,
            enabled = currentPage < totalPages - 1, // Disable if on last page
            colors = ButtonDefaults.buttonColors(containerColor = BitsBlue)
        ) {
            Text("Next >")
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ID Circle
            Surface(
                shape = RoundedCornerShape(50),
                color = BitsBlue,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "${post.id}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title Text
            Text(
                text = post.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1, // Keep it clean (1 line only)
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// --- HEADER ---
@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BitsBlue)
            .statusBarsPadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("SDPD CS #08", color = BitsGold, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

// --- FOOTER ---
@Composable
fun FooterSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BitsBlue)
            .navigationBarsPadding()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("WILP BITS PILANI", color = BitsGold, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}