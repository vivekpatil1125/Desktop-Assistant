# ✦ Desktop Assistant (AI-Powered)

A modern **AI-powered Desktop Assistant** built using **Java (Swing)** that can perform system tasks, understand voice commands, automate actions, and respond intelligently using AI.

---

## 🚀 Features

### 🧠 AI Capabilities
- Integrated with **Gemini AI** for intelligent responses
- Natural language command processing
- AI fallback when no local feature matches

---

### 🎤 Voice Assistant
- Voice input using **Wit.ai Speech API**
- Converts speech → text → executes commands
- Optional **Text-to-Speech (TTS)** using PowerShell + System.Speech

---

### 💻 System Features
- Open / close applications
- Control system functions:
  - Volume up/down
  - Mute sound
  - Lock screen
- Display system information

---

### 📁 File Management
- Search files across system
- Open file location automatically
- Create / delete / rename files & folders
- Open files directly

---

### ⚡ Quick Commands (Sidebar)
- Time & Date
- Clipboard history
- Web search
- YouTube search
- File search with input prompt

---

### 📊 Smart Features
- Command history tracking
- App usage tracking
- Background threaded execution (no UI freezing)

---

### 🎨 Modern UI
- Custom dark-themed UI
- Chat-style interface
- Sidebar quick commands
- Animated microphone feedback

---

## 🛠️ Tech Stack

### 🔹 Core
- Java (Swing)
- Java Sound API
- Multithreading

### 🔹 APIs
- Wit.ai (Speech Recognition)
- Gemini API (AI responses)

### 🔹 Libraries
- Apache HttpClient (API calls)
- Gson (JSON parsing)

---

## 📂 Project Structure

```
org.example
│── Main.java
│
└── features
    │── Feature.java
    │── AppLaunchFeature.java
    │── FileSearchFeature.java
    │── SystemControlFeature.java
    │── GeminiFeature.java
    │── TextToSpeech.java
    │── UsageTracker.java
```

---

## ⚙️ Setup Instructions

### 1. Clone Repository
```
git clone https://github.com/your-username/desktop-assistant.git
cd desktop-assistant
```

---

### 2. Add API Keys

Create a `.env` file:

```
WIT_AI_TOKEN=your_wit_ai_key
GEMINI_API_KEY=your_gemini_key
```

---

### 3. Run Project

```
mvn clean install
mvn exec:java
```

OR run `Main.java` directly in IntelliJ / Eclipse.

---

## 🔐 Environment Variables

Access keys using:

```
AppConfig.WIT_AI_TOKEN
AppConfig.GEMINI_API_KEY
```

---

## 🧠 How It Works

1. User enters text or voice command  
2. Input is processed in background thread  
3. System checks features:
   - If matched → execute locally  
   - Else → send to Gemini AI  
4. Response is displayed (and optionally spoken)

---

## 🔊 Text-to-Speech

Uses Windows PowerShell + System.Speech

Flow:
```
Java → PowerShell → SpeechSynthesizer → Audio Output
```

---

## 🧵 Multi-threading

Threads are used for:
- Voice recording
- API calls
- File searching
- Command execution

Ensures smooth and responsive UI.

---

## 📌 Future Improvements

- Cross-platform voice engine
- Offline AI support
- Smart suggestions
- Plugin system
- Mobile integration

---

## 💡 Use Cases

- Personal assistant
- Productivity automation
- Hands-free system control
- AI chatbot interface

---

## 👨‍💻 Author

Vivek Patil  
MCA Student

---

## ⭐ Support

If you like this project:
- Star ⭐ the repository
- Fork 🍴 it
- Contribute 💡

---

## 📜 License

For educational and personal use.
