# 🩸 Rakta-Seva Connect

> A modern real-time blood donation and emergency coordination platform built using Kotlin, Jetpack Compose, Firebase, and Gemini AI.

---

## 📌 Overview

Rakta-Seva Connect is an Android-based healthcare coordination application designed to help users quickly connect with compatible blood donors during emergencies.

The application focuses on:

* Real-time emergency blood request coordination
* Smart donor discovery
* Instant request acceptance workflows
* Modern responsive UI/UX
* AI-powered assistance and message generation
* Reliable Firebase-backed synchronization

The platform was developed as a production-style internship project using modern Android development practices.

---

# ✨ Features

## 🚨 Emergency Blood Request System

* Create emergency blood requests instantly
* Real-time request updates using Firestore listeners
* Live donor response tracking
* Request cancellation and archive support
* Optimistic UI updates for smooth interactions

## 🩸 Donor Discovery

* Find compatible blood donors
* Blood group filtering
* Eligibility tracking
* Privacy-safe contact sharing
* Realtime donor coordination

## 🔔 Live Notification System

* Instant acceptance notifications
* Realtime alert synchronization
* Read/unread state tracking
* Notification persistence

## 🤖 Gemini AI Integration

* AI Chat Assistant
* AI-powered emergency message generation
* Natural conversational responses
* Google Gemini API integration

## 🎨 Modern UI/UX

* Jetpack Compose + Material 3
* Adaptive Light/Dark/System themes
* Discord-inspired dark mode palette
* Responsive layouts
* Smooth Compose animations
* Adaptive launcher icons and splash screen

## ⚡ Performance & Stability

* Stable Compose state management
* Optimized recompositions
* Firebase realtime synchronization
* Lifecycle-safe listeners
* Clean architecture practices

---

# 🛠️ Tech Stack

| Category       | Technology                        |
| -------------- | --------------------------------- |
| Language       | Kotlin                            |
| UI Framework   | Jetpack Compose                   |
| Design System  | Material 3                        |
| Backend        | Firebase                          |
| Authentication | Firebase Auth                     |
| Database       | Firebase Firestore                |
| AI Integration | Google Gemini API                 |
| Image Loading  | Coil                              |
| Architecture   | Compose State + Firebase Realtime |
| IDE            | Android Studio                    |

---

# 📱 Application Screens

* Splash / Welcome Screen
* Authentication System
* Home Dashboard
* Emergency Requests
* Donor Discovery
* Notifications
* User Profile
* Settings & Theme Control
* AI Chat Assistant
* AI Message Generator

---

# 🔄 Realtime Coordination Workflow

Rakta-Seva Connect uses Firebase Firestore realtime snapshot listeners to synchronize emergency coordination across users instantly.

### Flow:

1. User creates emergency request
2. Compatible donors receive live request updates
3. Donor accepts request
4. Requester receives realtime notification
5. Contact coordination begins securely
6. Request state updates instantly across devices

---

# 🧠 AI Features

The application integrates Google Gemini AI to assist users with:

* Emergency response assistance
* Smart blood request message generation
* Conversational healthcare guidance
* Natural language interaction

The AI system is implemented securely using BuildConfig-based API key injection and GitHub-safe local configuration handling.

---

# 🔐 Security & Privacy

* API keys excluded from Git tracking
* Secure local configuration management
* Privacy-safe donor contact visibility
* Firebase-backed authentication
* Lifecycle-safe realtime listeners

---

# 🌗 Theme System

The app supports:

* Light Mode
* Dark Mode
* Follow System Mode

Theme preferences persist automatically and dynamically react to device theme changes.

---

# 🚀 Setup Instructions

## 1. Clone Repository

```bash
git clone <your-repository-url>
```

## 2. Open in Android Studio

Open the project folder in Android Studio.

## 3. Configure Firebase

* Add your `google-services.json`
* Enable Firebase Authentication
* Enable Firebase Firestore

## 4. Configure Gemini API

Add your Gemini API key inside `local.properties`:

```properties
GEMINI_API_KEY=YOUR_API_KEY_HERE
```

## 5. Build Project

```bash
./gradlew assembleDebug
```

---

# 📦 Release Build

To generate a release APK:

```bash
Build → Generate Signed Bundle / APK
```

---

# 📈 Future Improvements

Potential future enhancements:

* Push notification integration
* Hospital verification system
* GPS-based donor radius filtering
* Blood donation history tracking
* Cloud Functions automation
* Advanced AI healthcare assistance
* Admin dashboard
* Multi-language support

---

# 👨‍💻 Developer

**Ravi Prakash K**

Android Developer | UI/UX Enthusiast | Firebase & Compose Developer

---

# 📄 License

This project was developed for educational, internship, and portfolio purposes.

---

# ❤️ Rakta-Seva Connect

> Connecting donors. Saving lives.
