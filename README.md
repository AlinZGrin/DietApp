# Diet App - Android Kotlin Application

A comprehensive Android application for weight monitoring, nutrition tracking, and healthy eating habits built with Kotlin and Jetpack Compose.

## 🍎 Overview

This project contains a full-featured Android application implementing all functional requirements for diet and nutrition tracking with Firebase backend integration.

**Key Features:**

- **Weight Tracking**: Manual logging, trend analysis, goal setting with Google Fit integration
- **Meal Suggestions**: AI-powered recommendations based on dietary preferences and goals
- **Food Logging**: Barcode scanning, comprehensive nutrition database, daily summaries
- **Progress Analytics**: Interactive charts, weekly reports, goal achievement tracking
- **Cloud Sync**: Real-time data synchronization with Firebase backend
- **Push Notifications**: Meal reminders, achievements, and progress updates
- **Security**: GDPR-compliant data handling, secure Firebase authentication

**Technology Stack:**

- Kotlin + Jetpack Compose
- MVVM Architecture with Hilt DI
- Room Database + Firebase Firestore
- Firebase Authentication & Cloud Messaging
- ML Kit for barcode scanning
- Material Design 3

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest stable)
- Android SDK API 24+
- Firebase project setup

### Setup

```bash
cd android-app
# Configure Firebase (see android-app/README.md)
./gradlew assembleDebug
```

### Features Implemented

- ✅ User registration and profile management (FR1.1-1.3)
- ✅ Weight tracking with trend analysis (FR2.1-2.5)
- ✅ Calorie and nutrition goal setting (FR3.1-3.3)
- ✅ Personalized meal suggestions (FR4.1-4.5)
- ✅ Food logging with barcode scanning (FR5.1-5.4)
- ✅ Progress feedback and analytics (FR6.1-6.3)
- ✅ Health app integrations (FR7.1-7.2)
- ✅ Firebase cloud sync and authentication (FR8.1-8.3)
- ✅ Push notifications and reminders (FR9.1-9.3)
- ✅ Data export and sharing (FR10.1-10.3)

## 📋 Functional Requirements Status

All functional requirements from the specification have been implemented:

| Requirement | Status      | Implementation                             |
| ----------- | ----------- | ------------------------------------------ |
| FR1.1-1.3   | ✅ Complete | User profile management with Firebase Auth |
| FR2.1-2.5   | ✅ Complete | Weight tracking with Room database         |
| FR3.1-3.3   | ✅ Complete | BMR/TDEE calculations with custom goals    |
| FR4.1-4.5   | ✅ Complete | Smart meal suggestions with filtering      |
| FR5.1-5.4   | ✅ Complete | Food logging with ML Kit barcode scanning  |
| FR6.1-6.3   | ✅ Complete | Progress analytics and health warnings     |
| FR7.1-7.2   | ✅ Complete | Google Fit and Health Connect integration  |
| FR8.1-8.3   | ✅ Complete | Secure authentication and GDPR compliance  |
| FR9.1-9.2   | ✅ Complete | Configurable push notifications            |
| FR10.1-10.3 | ✅ Complete | Firebase-based content management          |

## 🛠️ Development Tools

### VS Code Configuration

- **Tasks**: Pre-configured build and run tasks
- **Debug**: Launch configurations for Python and Node.js
- **Extensions**: Recommended extensions for multi-language development
- **Settings**: Optimized workspace settings

### Available Commands

Access via Command Palette (Ctrl+Shift+P) → "Tasks: Run Task":

- **Run Python Hello World** - Execute Python example
- **Run JavaScript Hello World** - Execute Node.js example
- **Run Python Tests** - Execute test suite
- **Format Python Code** - Code formatting with Black
- **Lint Python Code** - Code analysis with Pylint

## 📱 Android App Architecture

```
android-app/
├── app/src/main/java/com/dietapp/
│   ├── data/              # Data layer (models, database, repositories)
│   ├── di/                # Dependency injection
│   ├── ui/                # UI layer (screens, components, navigation)
│   └── DietApp.kt         # Application class
├── build.gradle          # Build configuration
└── README.md             # Detailed Android documentation
```

## 🔒 Security & Privacy

- **Data Encryption**: All personal data encrypted at rest
- **Authentication**: Multi-provider secure authentication
- **Compliance**: GDPR and HIPAA considerations
- **Privacy**: Minimal data collection with user consent
- **Security**: Regular security audits and updates

## 🧪 Testing

### Android Testing

- **Unit Tests**: Repository and ViewModel testing
- **Integration Tests**: Database and API testing
- **UI Tests**: Compose UI testing with semantics
- **Performance Tests**: Memory and battery optimization

### General Testing

- **Python**: pytest with coverage reporting
- **JavaScript**: Jest testing framework
- **Code Quality**: ESLint, Prettier, Black formatters

## 📚 Documentation

- **`android-app/README.md`** - Comprehensive Android development guide
- **`docs/getting-started.md`** - General development setup
- **`.github/copilot-instructions.md`** - Coding standards and guidelines

## 🤝 Contributing

1. Follow the coding standards in `.github/copilot-instructions.md`
2. Write tests for new functionality
3. Update documentation for significant changes
4. Use conventional commit messages
5. Submit pull requests for review

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Ready for Development**: This workspace provides everything needed to develop, test, and deploy a comprehensive diet and nutrition tracking application with modern development practices and industry-standard security measures.
