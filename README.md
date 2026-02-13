

```markdown
# SDPD CS #08: Parsing Internet Resources 📱

This Android application demonstrates how to **fetch** raw data from the internet and **parse** it into usable Kotlin objects. It uses industry-standard libraries (**Retrofit** & **Moshi**) and modern UI tools (**Jetpack Compose**).

---

## 📚 Core Concepts

### 1. What is Parsing?
When an Android app communicates with a server, the server does not send Kotlin objects (like `Int`, `String`, or `List`). [cite_start]Instead, it sends **raw structured text**, usually in **JSON** (JavaScript Object Notation) format[cite: 11, 14].

[cite_start]**Parsing** is the conversion process where this raw text is mapped into strongly typed Kotlin data classes that your app can actually use in its logic and UI[cite: 35].

> **Why is this important?**
> [cite_start]* **Type Safety:** Ensures "id" is treated as an Integer and "title" as a String[cite: 73].
> [cite_start]* **Null Safety:** Handles missing or null values from the server without crashing[cite: 74].
> [cite_start]* **Architecture:** Separates raw data handling from your UI logic[cite: 76].

### 2. The Tools Used
* **Retrofit:** A type-safe HTTP client for Android. [cite_start]It handles the network connection to the server[cite: 146].
* **Moshi:** A modern JSON library for Android. [cite_start]It handles the conversion (parsing) of JSON into Kotlin objects[cite: 147].
* [cite_start]**Coroutines:** Used to run network tasks on a background thread, preventing the app from freezing (ANR)[cite: 197, 198].

---

## 🏗️ Architecture Flow

[cite_start]The application follows a clean 4-step data flow[cite: 320]:

1.  **App Request:** The UI asks for data (e.g., "Get all Posts").
2.  [cite_start]**Retrofit:** Sends an HTTP `GET` request to the Server[cite: 321].
3.  [cite_start]**Server:** Returns a raw `JSON` response[cite: 322].
4.  [cite_start]**Moshi:** Automatically converts that `JSON` → `Kotlin Data Class`[cite: 326].
5.  **UI:** Displays the clean Kotlin objects to the user.

---

## 🛠️ Project Setup

### 1. Dependencies (`build.gradle.kts`)
To make this work, we added these external libraries to the module-level `build.gradle` file:

```kotlin
dependencies {
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // JSON Converter
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    // Moshi (The Parser)
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
}

```

### 2. Permissions (`AndroidManifest.xml`)

We must explicitly ask the Android OS for permission to use the internet.

```xml
<uses-permission android:name="android.permission.INTERNET" />

```

---

## 💻 Code Structure

### Step 1: The Data Model

We define a Kotlin `data class` that matches the structure of the JSON we expect.

**JSON from Server:**

```json
{
  "id": 1,
  "title": "sunt aut facere...",
  "body": "quia et suscipit..."
}

```

**Kotlin Code:**

```kotlin
data class Post(
    val id: Int,      // Matches JSON "id"
    val title: String, // Matches JSON "title"
    val body: String  // Matches JSON "body"
)

```

### Step 2: The API Contract

We create an interface to define *how* we talk to the server.

```kotlin
interface ApiService {
    @GET("posts") // Specifies the endpoint: /posts
    suspend fun getPosts(): List<Post>
}

```

### Step 3: Retrofit Instance (Singleton)

We create a single object that configures Retrofit and Moshi. This saves memory by ensuring we don't create multiple network clients.

```kotlin
object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("[https://jsonplaceholder.typicode.com/](https://jsonplaceholder.typicode.com/)")
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Connects Moshi to Retrofit
            .build()
            .create(ApiService::class.java)
    }
}

```

### Step 4: The UI (Pagination)

We use **Jetpack Compose** to display the data.

* **`LazyColumn`:** Efficiently renders the list.
* **Pagination:** Instead of showing all 100 items at once, we split them into pages (6 items per page) for better usability.
* 
**Coroutines (`scope.launch`):** All network calls happen inside a coroutine to keep the UI smooth.



---

## 🚀 How to Run

1. Clone this repository.
2. Open in **Android Studio**.
3. Let Gradle sync (it will download Retrofit and Moshi).
4. Run on an Emulator or Physical Device (ensure it has internet access).
5. Click **"Start Download"** to fetch the data.
6. Use **"Next"** and **"Prev"** buttons to navigate through the parsed objects.

```

```
