# 🌿 PlantSense AI

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF.svg?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2024%2B-3DDC84.svg?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%20Design-3-7F52FF.svg?style=flat-square&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean-blue?style=flat-square)](https://en.wikipedia.org/wiki/Multitier_architecture)
[![MVVM](https://img.shields.io/badge/Pattern-MVVM-blue?style=flat-square)](https://developer.android.com/topic/libraries/architecture/viewmodel)
[![Dagger Hilt](https://img.shields.io/badge/DI-Dagger%20Hilt-3DDC84?style=flat-square)](https://developer.android.com/training/dependency-injection/hilt-android)
[![Room Database](https://img.shields.io/badge/Database-Room-orange?style=flat-square)](https://developer.android.com/training/data-storage/room)
[![Gemini AI](https://img.shields.io/badge/AI-Gemini%203.5-orange.svg?style=flat-square&logo=google&logoColor=white)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)

# 🌿 Introduction

**PlantSense AI** is an Android application designed to help users identify plant species and diagnose leaf health issues. By capturing images of plants or leaves, the app leverages Google's Gemini AI to retrieve botanical classifications, customized care guides, and detailed pathology reports.

The project demonstrates modern Android development patterns, showing how to integrate offline-first capabilities with advanced cloud AI. It serves as a showcase of production-ready architecture, clean code practices, and the seamless integration of hardware features like CameraX with background database caching and synchronization.

### 💡 Why this Project?

This repository showcases the implementation of a modern, production-grade Android codebase that solves real-world engineering challenges:

- **AI Integration**: Demonstrates how to integrate large language models (Google Gemini AI) into mobile clients with strict response structures and fail-safe parsing mechanisms.
- **Modern Android**: Utilizes the modern Android development stack, including Jetpack Compose, Material 3, and Kotlin Coroutines/Flow for reactive UI and state flow management.
- **Clean Architecture & MVVM**: Implements a highly testable and maintainable structure with strict layer separation, facilitating clean domain logic independent of frameworks.
- **Production-Ready Practices**: Showcases practical engineering strategies such as background data workers (WorkManager), secure hardware access (CameraX), local file sandboxing, and robust local caching (Room/DataStore).

---

# 📸 Screenshots

| Home | Camera | Result | History |
|:---:|:---:|:---:|:---:|
| ![Home](screenshots/home.png) | ![Camera](screenshots/camera.png) | ![Result](screenshots/result.png) | ![History](screenshots/history.png) |

---

# 🌟 Features

### 🌿 AI Features
- **Multimodal Plant Identification**: Analyzes plant images using Google Gemini AI to retrieve botanical classifications, common names, and temperature/watering/light care rules.
- **Leaf Pathology Diagnostics**: Detects crop and plant diseases from leaf imagery and provides detailed descriptions of causes, symptoms, treatments, and preventative steps.

### 📷 Camera Features
- **Dynamic Camera Viewfinder**: Utilizes AndroidX CameraX to provide a high-performance viewfinder with customizable bounding boxes.
- **Torch & Lens Control**: Supports direct hardware control to toggle device flash/torch and switch between front and back camera lenses.
- **Local Gallery Picker**: Seamlessly integrates with the system photo picker to import local images from the device gallery.

### 💾 Offline Features
- **Local History Database**: Persists scan history, care reports, and diagnostic results using a Room database for instant offline access.
- **Image Sandboxing**: Automatically saves scanned photos to the application's internal files directory, ensuring scan results retain valid image references without relying on ephemeral content URIs.
- **Fallback Configuration**: Allows custom user API keys via Jetpack DataStore, with automatic fallback to pre-configured developer keys defined in build configurations.

### 🎨 UI & UX
- **Material 3 Interface**: Implements a nature-inspired design language utilizing dynamic Material 3 color palettes and styling.
- **Glassmorphic Navigation**: Features a floating bottom navigation bar with translucent cards and micro-animations for active state transitions.
- **Adaptive Theme**: Provides full support for dark and light UI themes with unified spacing and typography.

### ⚙️ Developer Features
- **Clean Architecture & MVVM**: Enforces strict separation of concerns, decoupling presentation code from business logic and database mechanisms.
- **Type-Safe Compose Navigation**: Uses compile-time safe routing parameters via the Navigation Compose library.
- **Automated Caching Policies**: Runs periodic background database and file-system cleanup tasks using Jetpack WorkManager.

---

# 🏗️ Architecture

The codebase is structured according to **Clean Architecture** principles and **MVVM** design patterns. This separation of concerns ensures that business logic remains entirely decoupled from database schemas and visual frameworks, facilitating high testability and maintainability.

```
       +---------------------------------------------------+
       |            Presentation Layer (MVVM)              |
       |  - UI: Jetpack Compose (Material 3 Screens)       |
       |  - State Management: ViewModels (StateFlow)       |
       +-------------------------+-------------------------+
                                 |
                                 v
       +---------------------------------------------------+
       |                 Domain Layer                      |
       |  - Use Cases (IdentifyPlant, DetectDisease, etc.) |
       |  - Domain Models (PlantResult, ScanHistoryItem)   |
       |  - Repository Contracts                           |
       +-------------------------+-------------------------+
                                 |
                                 v
       +---------------------------------------------------+
       |                  Data Layer                       |
       |  - Room DB (scan_history persistence)             |
       |  - Jetpack DataStore (API credentials)            |
       |  - Retrofit GeminiApiService (Multimodal REST API)|
       |  - Repository Implementations                     |
       +---------------------------------------------------+
```

### Layer Breakdown

- **Presentation Layer (MVVM)**: Handles the UI rendering and user interactions using Jetpack Compose and Material 3. ViewModels manage and expose UI states as reactive `StateFlow` streams, keeping the UI thin and logic-less.
- **Domain Layer**: The core of the application containing pure business logic. It defines the business entities, single-responsibility Use Cases (e.g., plant identification, disease detection), and repository interfaces. It is entirely independent of Android frameworks, UI libraries, and database modules.
- **Data Layer**: Responsible for data operations. It implements repository contracts defined in the Domain layer and coordinates data from local databases (Room), preferences (DataStore), and network services (Retrofit REST API). It maps remote and database data models to domain-specific entities.

---

# 🧠 How AI Works

PlantSense AI utilizes the Google Gemini API to analyze visual inputs and return structured, reliable data. The integration ensures high dependability through strict response formatting:

1. **Multimodal Analysis**: The application accepts image inputs captured via CameraX or selected from the system gallery, processing them alongside specialized prompts directed to the Gemini API.
2. **Structured JSON Output**: To prevent parsing exceptions and ensure UI stability, the app enforces a predefined response format. The AI model is constrained to return standardized JSON representations containing plant metadata (e.g., botanical classification, confidence scores, and watering/temperature requirements).
3. **Pathology Verification**: When diagnosing plant disease, the app leverages visual model classification to assess leaf symptoms and map active ailments, extracting treatment plans only when a disease is identified.

---

# 🛠️ Tech Stack

| Category | Technology |
| --- | --- |
| **Language** | Kotlin, Coroutines, StateFlow |
| **UI** | Jetpack Compose, Material 3, Navigation Compose |
| **Architecture** | Clean Architecture, MVVM |
| **DI** | Dagger Hilt |
| **Database** | Room SQLite |
| **Networking** | Retrofit, OkHttp |
| **AI** | Google Gemini AI (Gemini 3.5 Flash REST API) |
| **Camera** | CameraX |
| **Image Loading** | Coil |
| **Storage** | Jetpack DataStore Preferences |
| **Testing** | JUnit 4, MockK, Turbine, Coroutines Test |
| **Background Work** | Jetpack WorkManager |

---

# 📂 Project Structure

```
app/src/main/java/com/plantsense/ai/
├── core/                  # Shared utilities and configurations
│   ├── di/                # Core level dependency injection modules
│   ├── error/             # Exception definitions and custom error logic
│   ├── network/           # General HTTP clients and interceptors
│   ├── theme/             # Material 3 colors, shapes, and typography
│   └── utils/             # Helper classes and extension functions
├── data/                  # Implementation of data storage and APIs
│   ├── datastore/         # Jetpack DataStore preferences (credentials, settings)
│   ├── local/             # Room DB definitions, entities, and DAOs
│   ├── remote/            # Retrofit REST API services and data transfer objects (DTOs)
│   └── repository/        # Data repository implementations coordinating network and storage
├── di/                    # Core application-level Dagger Hilt dependency modules
├── domain/                # Enterprise business logic and interfaces
│   ├── model/             # Pure Kotlin data representations used across the app
│   ├── repository/        # Domain repository contracts (interfaces)
│   └── usecase/           # Single-responsibility use cases representing actions
├── presentation/          # Jetpack Compose UI screens, components, and ViewModels
│   ├── camera/            # CameraX viewfinder screen and capture logic
│   ├── common/            # Reusable UI widgets, loaders, and glassmorphic designs
│   ├── disease/           # Disease diagnostics details and pathological displays
│   ├── history/           # Local scan history list and management
│   ├── home/              # Main dashboard screen showing shortcuts and analytics
│   ├── identification/    # Plant identification results and care reports
│   ├── navigation/        # Type-safe Navigation Compose routes and transitions
│   ├── profile/           # User configuration and preferences passport
│   └── splash/            # Launch screen animation and initialization
└── worker/                # Background tasks managed via Jetpack WorkManager
```

---

# 🚀 Setup

Follow these steps to configure and build the application locally.

### Prerequisites

* **IDE**: Android Studio Ladybug (2024.1.3) or newer
* **Android SDK**: targetSdk 36, minSdk 24
* **Gemini API Key**: Obtain an API key from the [Google AI Studio](https://aistudio.google.com/) console

### Project Configuration

1. In the project's root directory, locate or create a `gradle.properties` file.
2. Add your Gemini developer key as follows:
   ```properties
   G_API_KEY=your_gemini_api_key_here
   ```
   > [!NOTE]
   > Do not wrap the API key value in quotation marks. The Gradle build script handles proper quotes and type formatting during code generation.

### Compilation & Build

To compile the debug APK using Gradle, run:
```bash
./gradlew assembleDebug
```

To run the local unit test assertions, run:
```bash
./gradlew testDebugUnitTest
```

---

# 🧪 Testing

The codebase includes structured unit tests to verify behavior and prevent regressions across all architectural layers.

- **Use Cases**: Validates pure business rules (such as `IdentifyPlantUseCaseTest`) by isolating domain behaviors and mocking external repository dependencies with MockK.
- **Repositories**: Verifies data synchronization pipelines and mappings between remote DTOs, database entities, and clean domain models (such as `PlantRepositoryImplTest`).
- **ViewModels**: Asserts UI state transitions (loading, success, failure) utilizing custom Coroutine test dispatchers and the **Turbine** library for reactive `StateFlow` assertions.

---

# ⚡ Performance

To ensure a smooth user experience and efficient resource usage, the application incorporates several performance-minded engineering practices:

- **Reactive StateFlow**: Exposes UI state using `StateFlow` collected in a lifecycle-aware manner (`collectAsStateWithLifecycle`), preventing background resource leakage and redundant updates.
- **Non-blocking Coroutines**: Offloads database access, network calls, and file I/O operations to background thread pools using `Dispatchers.IO` and `Dispatchers.Default` to prevent main-thread blocking and frame drops.
- **Compose Recomposition Optimization**: Uses immutable domain objects and stable UI state wrappers to minimize recompositions, ensuring smooth scrolling and responsive transitions.
- **WorkManager Offloading**: Delegates maintenance work, such as file cache cleanups, to WorkManager. This runs tasks during optimal device conditions (e.g. system idle) and ensures execution reliability.
- **Repository Pattern & Caching**: Employs local repositories to manage caching logic, avoiding redundant network requests to Gemini AI by serving previously resolved queries from the Room database.

---

# 🔒 Security

Security and key management follow official Android development guidelines to protect user credentials:

- **External Key Storage**: Sensitive API credentials, such as the Gemini developer key, are defined in the local `gradle.properties` (which is git-ignored) and injected at compilation time.
- **BuildConfig Generation**: The compiler binds the injected key into a secure, obfuscated-ready `BuildConfig` field, keeping secrets out of the codebase files.
- **HTTPS Enforcement**: All communications with Google's Gemini AI and remote services are strictly routed through encrypted HTTPS protocols with TLS verification.
- **Zero Committed Secrets**: The repository includes templates (`gradle.properties.example`) to guide developers without committing actual production keys or operational secrets.

---

# 🗺️ Roadmap

Future enhancements planned for the application include:

- [ ] **Plant Reminders**: Add local notifications and reminders for watering, fertilizing, and general care schedules.
- [ ] **Weather-Aware Care**: Integrate local weather forecasts to recommend adjustments in outdoor watering routines.
- [ ] **Cloud Synchronization**: Enable optional secure user accounts to backup and sync scan histories across multiple devices.
- [ ] **AI Chat Assistant**: Implement a conversational chat interface for real-time botanical care advice.
- [ ] **Plant Comparison**: Provide side-by-side comparison features for growth tracking and health comparisons.
- [ ] **Home Screen Widgets**: Build interactive Android home screen widgets displaying current plant health statuses and care schedules.
- [ ] **Wear OS Support**: Create a companion Wear OS app for quick care alerts and step-by-step watering reminders.
- [ ] **Offline ML Diagnostics**: Bundle lightweight on-device TensorFlow Lite models for basic plant identification without requiring network connectivity.

---

# 📄 License

```
Copyright 2026 PlantSense AI Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

# 🤝 Acknowledgements

- **Google Gemini AI**: For providing natural language and visual reasoning models.
- **Android Jetpack & Compose Teams**: For the modern Android components and declarative UI systems.
- **Open Source Community**: For the libraries that empower modern Kotlin development, including Coil, Retrofit, and MockK.

# bharathelper
