# MyStoryApp

A full-featured Android application for creating and sharing photo stories, developed as the final submission project for [Dicoding](https://www.dicoding.com)'s "Belajar Pengembangan Aplikasi Android Intermediate" course.



<p align="center">
<img width="3840" height="2160" alt="Modern_App_Portfolio_Mockup_Presentation_upscaled" src="https://github.com/user-attachments/assets/b7e94be7-0836-4a27-9c25-7fa33f8ecc90" />
</p>


## 📖 Overview

MyStoryApp is a social storytelling platform that enables users to create, share, and discover photo stories. Built with modern Android development principles, the app demonstrates advanced concepts including robust architecture, efficient data handling, location-based services, and comprehensive testing strategies.

## 🎓 Academic Context

This project represents the **final submission** for **Dicoding's "Belajar Pengembangan Aplikasi Android Intermediate"** course, showcasing mastery of intermediate-level Android development concepts:

- Advanced architecture patterns (Repository + MVVM)
- Efficient data pagination with Paging 3
- Location-based services integration
- Comprehensive testing methodologies
- Modern Android development best practices

## ✨ Key Features

### 🔐 Secure Authentication
- **User Registration & Login**: Dedicated authentication pages with form validation
- **Smart Password Validation**: Custom EditText with real-time error checking (minimum 8 characters requirement)
- **Session Management**: Secure token storage using DataStore with persistent login state
- **Auto-Routing**: Intelligent navigation based on authentication status
- **Secure Logout**: Complete session clearing with token removal

### 📚 Story Management
- **Dynamic Story Feed**: Paginated list displaying user stories with photos and names
- **Detailed Story View**: Comprehensive story details with full descriptions and user information
- **Story Creation**: Multi-source photo upload (camera/gallery) with rich text descriptions
- **Real-time Updates**: Newly created stories appear instantly at the top of the feed

### 📍 Location Features
- **GPS Integration**: Optional location tagging using LocationServices and FusedLocationProviderClient
- **Interactive Maps**: Dedicated map view showing geotagged stories with markers
- **Current Location**: Real-time user position display on map
- **Address Resolution**: Geocoder integration for location-to-address conversion
- **Location Filtering**: API parameter `location=1` for location-based story retrieval

### 🎨 Enhanced User Experience
- **Shared Element Transitions**: Smooth animations between screens
  - Splash to login transition
  - Story list to detail view with photo highlighting
- **Bottom Navigation**: Easy access to Story List, Map, and About sections
- **Responsive Design**: Optimized interface for various screen sizes

### ⚡ Performance & Data Management
- **Paging 3 Integration**: Efficient data loading with Remote Mediator
- **Local Synchronization**: Room database keeps local data in sync with remote API
- **Smooth Scrolling**: Optimized list performance for large datasets
- **Offline Capability**: Local data persistence for improved user experience

## 🧪 Comprehensive Testing

### Unit Testing
Robust ViewModel testing for Paging data scenarios:
- **Data Integrity**: Validates non-null data and expected item counts
- **Content Accuracy**: Confirms correct first item retrieval
- **Edge Case Handling**: Ensures proper behavior with empty datasets

### UI Testing
- **End-to-End Testing**: Espresso-based comprehensive UI validation
- **User Flow Testing**: Critical path verification for authentication and story creation
- **Interface Consistency**: Reliable UI behavior across different scenarios

## 🏗️ Technical Implementation

### Architecture & Patterns
- **Repository Pattern**: Clean data access layer abstraction
- **MVVM Architecture**: Clear separation between UI and business logic
- **Manual Dependency Injection**: Manual DI implementation for dependency management

### Core Technologies

#### UI & Framework
- **UI Framework**: Traditional Android View system
- **Language**: Kotlin for modern, expressive development
- **Navigation**: Bottom Navigation for intuitive app section access

#### Data Management
- **Networking**: Retrofit for reliable API communication
- **Local Database**: Room for efficient SQLite operations
- **Preferences**: DataStore for secure key-value storage
- **Reactive Programming**: LiveData for responsive UI updates

#### Location & Mapping
- **Location Services**: LocationServices and FusedLocationProviderClient for GPS functionality
- **Geocoding**: Geocoder for address resolution
- **Maps Integration**: Google Maps API with SupportMapFragment

#### Concurrency
- **Asynchronous Programming**: Kotlin Coroutines for background operations
- **Data Loading**: Paging 3 with Remote Mediator for optimized performance

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Google Maps API key
- Internet connection for story synchronization

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/HighOverseer/Story_App.git
cd MyStoryApp
```

2. **Configure API Keys**
```bash
# Add your Google Maps API key to local.properties
MAPS_API_KEY=your_google_maps_api_key_here
```

3. **Open in Android Studio**
```bash
# Import the project and sync Gradle files
```

4. **Build and run**
```bash
# Deploy to your Android device or emulator
```

## 📱 User Experience Flow

1. **Onboarding**: Smooth splash screen with automatic authentication routing
2. **Authentication**: Secure login/registration with real-time validation
3. **Story Discovery**: Browse infinite-scroll story feed with rich media
4. **Content Creation**: Capture moments with integrated camera or gallery selection
5. **Location Sharing**: Optional GPS tagging for location-based story discovery
6. **Map Exploration**: Visual discovery of stories through interactive mapping

## 🎯 Academic Learning Objectives Met

✅ **Advanced Architecture**: Repository pattern and MVVM implementation  
✅ **Data Pagination**: Paging 3 with Remote Mediator integration  
✅ **Location Services**: GPS and mapping functionality  
✅ **Testing Strategies**: Comprehensive unit and UI testing  
✅ **Modern UI**: Shared element transitions and custom components  
✅ **Data Persistence**: Room database and DataStore integration  
✅ **Network Programming**: Retrofit API integration with error handling  


## 🙏 Acknowledgments

- **Dicoding Indonesia** for providing comprehensive intermediate Android curriculum
- **Android Developer Community** for excellent libraries and development resources
- **Open Source Contributors** whose libraries made this project possible

---
