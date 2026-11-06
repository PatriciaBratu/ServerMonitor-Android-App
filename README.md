# Secure Android Server Monitor

## Project Overview
A native Android application built entirely with **Jetpack Compose** that provides real-time monitoring of server statuses (UP/DOWN). Designed with an offline-first architecture, the application transforms a mobile device into a portable diagnostic tool. The project heavily emphasizes data security, utilizing **SQLCipher** for military-grade database encryption, and leverages Android's **Storage Access Framework (SAF)** for dynamic, decoupled configuration management.

## Key Features & Functionalities

* **Dynamic Configuration Import (SAF):** 
  Users can seamlessly load server configurations from a `.json` file. By implementing Android's Storage Access Framework (`ActivityResultContracts.OpenDocument`) and `Gson` stream parsing, the app can securely read configurations from any physical location—internal storage, SD cards, or external USB drives via OTG—without requiring invasive storage permissions.
* **Data Security:** 
  To prevent unauthorized access to connection logs, all monitoring history is stored in a local SQLite database that is encrypted on-the-fly using **SQLCipher (AES-256)**. The `.db` file remains completely encrypted on the disk within the app's isolated Sandbox (`/data/data/`), accessible only via strict programmatic passphrase authentication (`PRAGMA key`).
* **Asynchronous Health Checks:** 
  Server pinging and connection validation run continuously in the background on dedicated execution threads (Coroutines/Threads), ensuring the UI remains highly responsive and fluid during network timeouts.
* **Multi-Channel Alert System:** 
  * **System Notifications:** Triggers high-priority local Android notifications (utilizing Android 8+ `NotificationChannel`) to instantly alert the user when a server goes down.
  * **External Webhooks:** Automatically dispatches critical downtime alerts to a dedicated **Discord** channel for team visibility.
* **Modern & Reactive UI:** 
  The user interface is fully declarative, built with Jetpack Compose and Material Design principles, featuring real-time state observation to reflect server health instantly.

## Tech Stack & Architecture

* **Framework:** Android SDK (Native)
* **UI Toolkit:** Jetpack Compose
* **Language:** Kotlin / Java
* **Database & Security:** SQLite, SQLCipher (`net.sqlcipher.database.SQLiteDatabase`)
* **Data Parsing:** Google Gson (InputStreamReader for memory-efficient parsing)
* **Core Components:** `ContentResolver`, `NotificationManager`, Background Threads
* **Architecture Highlights:** Offline-first, UI State Management, Page-level memory encryption, decoupled storage access.
