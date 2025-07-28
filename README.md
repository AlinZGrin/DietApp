# 🥗 DietApp - Smart Nutrition & Fitness Tracker

A comprehensive, modern Android application for tracking diet, nutrition, fitness goals, and maintaining a healthy lifestyle. Built with cutting-edge Android development practices and Firebase integration.

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)
![License](https://img.shields.io/badge/License-MIT-red.svg)

## 🌟 Features

### 📱 Core Features
- **🔐 User Authentication**: Secure Firebase Auth with email/password and Google Sign-In
- **🍎 Food Logging**: Comprehensive food tracking with detailed nutritional analysis
- **📊 Calorie Tracking**: Monitor daily caloric intake and expenditure
- **🔬 Nutrition Analysis**: Detailed macro and micronutrient breakdown
- **⚖️ Weight Tracking**: Log and visualize weight changes over time
- **🎯 Smart Goal Setting**: AI-powered personalized health and fitness goals
- **📈 Progress Visualization**: Beautiful charts and analytics

### 🚀 Advanced Features
- **📷 Barcode Scanning**: Quick food entry using CameraX and ML Kit
- **💧 Water Intake Tracking**: Monitor daily hydration with smart reminders
- **🏃 Exercise Integration**: Connect with fitness apps and health platforms
- **📋 Meal Planning**: Plan meals and generate shopping lists
- **👥 Social Features**: Share progress and join community challenges
- **🤖 Smart Recommendations**: AI-powered food and exercise suggestions
- **📱 Offline Support**: Full functionality without internet connection
- **🌍 Imperial/Metric Units**: Support for both unit systems with smart conversion

## 🛠️ Technology Stack

- **Platform**: Android (SDK 24+)
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Repository Pattern
- **Local Database**: Room with type converters
- **Cloud Database**: Firebase Firestore with real-time sync
- **Authentication**: Firebase Auth
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Camera & ML**: CameraX + ML Kit for barcode scanning
- **Charts**: MPAndroidChart for data visualization
- **Image Processing**: Coil for efficient image loading

## 📁 Project Architecture

```
DietApp/
├── 📱 android-app/                    # Android application
│   ├── app/
│   │   ├── src/main/java/com/dietapp/
│   │   │   ├── 🗃️ data/              # Data layer
│   │   │   │   ├── dao/              # Room DAOs
│   │   │   │   ├── entities/         # Room entities
│   │   │   │   ├── repository/       # Repository implementations
│   │   │   │   └── database/         # Database configuration
│   │   │   ├── 🔧 di/                # Dependency injection
│   │   │   ├── 🎨 ui/                # UI layer
│   │   │   │   ├── screens/          # Compose screens
│   │   │   │   ├── components/       # Reusable components
│   │   │   │   ├── viewmodels/       # ViewModels
│   │   │   │   ├── scanner/          # Camera & barcode scanning
│   │   │   │   └── theme/            # Material 3 theming
│   │   │   ├── 📐 utils/             # Utility classes
│   │   │   ├── 🧭 navigation/        # Navigation setup
│   │   │   └── 🔐 auth/              # Authentication
│   │   └── build.gradle              # App dependencies
│   ├── build.gradle                  # Project configuration
│   └── gradle.properties             # Build properties
├── 🔧 .vscode/                       # VS Code workspace
├── 📝 .github/                       # GitHub workflows & templates
└── 📖 README.md                      # This file
```

## 🚀 Getting Started

### ✅ Prerequisites

- **Android Studio**: Flamingo (2022.2.1) or later
- **Java**: JDK 17 or later
- **Android SDK**: API 24 (Android 7.0) minimum
- **Firebase Project**: For authentication and cloud features
- **USDA API Key**: For comprehensive food database access

### 📦 Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/DietApp.git
   cd DietApp
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to and select the `android-app` folder

3. **Firebase Setup**:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name `com.dietapp`
   - Download `google-services.json`
   - Place it in `android-app/app/` directory
   - Enable Authentication (Email/Password, Google)
   - Enable Firestore Database
   - Enable Cloud Storage

4. **API Configuration**:
   Create `android-app/local.properties` and add:
   ```properties
   USDA_API_KEY=your_usda_api_key_here
   ```

5. **Build and Run**:
   ```bash
   cd android-app
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 🔑 Key Features in Detail

### 🔐 Authentication System
- Firebase Auth integration with multiple providers
- Secure session management with automatic token refresh
- Google Sign-In for quick registration
- Profile management with cloud sync

### 🍎 Advanced Food Logging
- **Barcode Scanning**: Real-time camera scanning with ML Kit
- **USDA Database**: Access to 300,000+ foods with complete nutrition data
- **Custom Foods**: Add personal recipes and custom items
- **Meal Categories**: Organize by breakfast, lunch, dinner, and snacks
- **Portion Control**: Multiple serving size options and measurements

### 📊 Smart Analytics
- **Progress Charts**: Weight trends, calorie intake, macro distribution
- **Goal Tracking**: Visual progress toward weight and nutrition goals
- **Insights**: AI-powered recommendations based on eating patterns
- **Export Data**: CSV export for detailed analysis

### 🎯 Intelligent Goal Setting
- **BMR/TDEE Calculation**: Scientifically accurate metabolic rate estimation
- **Activity Level Integration**: Customized targets based on lifestyle
- **Macro Distribution**: Optimal protein/carb/fat ratios for goals
- **Timeline Tracking**: Realistic goal timelines with progress milestones

## 🔧 Configuration

### Environment Variables
```properties
# local.properties
USDA_API_KEY=your_api_key
FIREBASE_PROJECT_ID=your_project_id
```

### Firebase Collections Structure
```
users/
  └── {userId}/
      ├── profile (UserProfile)
      ├── goals (Goal[])
      ├── foodLogs (FoodLog[])
      ├── weightEntries (WeightEntry[])
      └── waterIntake (WaterIntake[])
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/AmazingFeature`
3. **Commit** your changes: `git commit -m 'Add some AmazingFeature'`
4. **Push** to the branch: `git push origin feature/AmazingFeature`
5. **Submit** a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write comprehensive unit tests
- Update documentation for new features
- Use meaningful commit messages
- Ensure backward compatibility

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support & Community

- **Issues**: [GitHub Issues](https://github.com/yourusername/DietApp/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/DietApp/discussions)

## 🙏 Acknowledgments

- **USDA FoodData Central** - Comprehensive nutrition database
- **Firebase Team** - Backend infrastructure and services
- **Android Jetpack** - Modern Android development components
- **ML Kit** - On-device machine learning capabilities
- **Material Design** - Beautiful and intuitive UI/UX guidelines

## 🗺️ Roadmap

- [ ] 🍽️ Meal planning with weekly menu generation
- [ ] 🤝 Social features and community challenges
- [ ] ⌚ Wear OS companion app
- [ ] 🔗 Integration with popular fitness trackers
- [ ] 🌐 Web dashboard for detailed analytics
- [ ] 🤖 Advanced AI nutritionist chatbot
- [ ] 📱 iOS version development

---

**Made with ❤️ for healthy living**
