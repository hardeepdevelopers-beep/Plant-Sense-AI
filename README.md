# PlantSense AI

A smart botanical companion app developed in Jetpack Compose that uses the Google Gemini API to identify plants and diagnose plant diseases.

## Getting Started

### API Configuration

To keep secrets secure, local API keys are not committed to Git. Follow these steps to configure your API key:

1. Copy `gradle.properties.example` to `gradle.properties`:
   ```bash
   cp gradle.properties.example gradle.properties
   ```
2. Open `gradle.properties` and replace `YOUR_API_KEY_HERE` with your actual Google Gemini API key:
   ```properties
   G_API_KEY=your_actual_gemini_api_key_here
   ```
