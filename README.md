# LaundrySystem 🧺

An Android application designed to efficiently manage laundry services. This application aims to streamline operations for laundry businesses, from order management to staff and expense tracking.

## Badges 🛡️

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](<link_to_build_status>)
[![Version](https://img.shields.io/badge/version-2.0.0-blue)](<link_to_version>)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellowgreen)](<link_to_license>)

## Description ✨

The LaundrySystem is a comprehensive Android application built with Kotlin, designed to handle all aspects of laundry service operations. It provides a robust platform for managing customer orders, tracking expenses, maintaining a price list, managing staff, and logging system activities. The application utilizes Firebase for backend services, ensuring real-time data synchronization and scalability.

## Table of Contents 📖

- [Features](#features-star)
- [Tech Stack](#tech-stack-technologies)
- [Installation](#installation--setup-rocket)
- [Usage](#usage-guide-manual)
- [Project Structure](#project-structure-folder)
- [Contributing](#contributing-handshake)
- [License](#license-key)
- [Important Links](#important-links-link)
- [Footer](#footer-tools)

## Features 🚀

*   **User Authentication:** Secure user registration, login, and password recovery with security questions.
*   **Role-Based Access:** Differentiates between Admin and other user roles for access control.
*   **Order Management:** Create, view, update, and manage customer laundry orders with status tracking (Pending, Cleaning, Drying, Delivery, Completed).
*   **Service Item Management:** Add, edit, and delete laundry services with associated prices and units of measure.
*   **Expense Tracking:** Log and manage daily expenses with categorization and notes.
*   **Staff Management:** View, register, and manage staff details, including salary, role, and verification status.
*   **Real-time Dashboard:** Provides an overview of today's revenue, order count, expenses, and ongoing orders.
*   **Invoice Generation:** Automatically generates and prints PDF invoices for orders.
*   **Logging:** Records system activities, including order creation, updates, and deletions, for audit purposes.
*   **Profile Picture Upload:** Allows users to upload and manage their profile pictures.
*   **Biometric Authentication:** Supports fingerprint-based login for enhanced security.
*   **Theming:** Implements both light and dark theme options.
*   **Form Validation:** Robust input validation for a seamless user experience.

## Tech Stack 💻

*   **Language:** Kotlin
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **UI Toolkit:** Jetpack Compose
*   **Backend:** Firebase Firestore
*   **Image Handling:** Cloudinary (for image uploads)
*   **PDF Generation:** Android PdfDocument API
*   **QR Code Generation:** ZXing Library

## Installation & Setup 🛠️

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Unknown1648/LaundrySystem.git
    cd LaundrySystem
    ```

2.  **Set up Firebase:**
    *   Create a Firebase project in the Firebase console.
    *   Add your Android app to the Firebase project and download the `google-services.json` file.
    *   Place the `google-services.json` file in the `app/` directory of your project.
    *   Configure Firestore and Cloudinary (if using custom image upload) in your Firebase project settings and update the application accordingly.

3.  **Open in Android Studio:**
    *   Open the project in Android Studio.
    *   Allow Android Studio to sync Gradle files.

4.  **Run the application:**
    *   Connect an Android device or start an emulator.
    *   Build and run the application from Android Studio.

## Usage Guide 📖

Upon launching the app, the `InitialScreen` checks if an admin user already exists. If not, it directs the user to the `AdminRegistrationScreen` to set up the initial administrator account. Otherwise, it navigates to the `Login` screen.

### First-Time Setup (Admin Registration) 🧑‍💻

1.  Open the app.
2.  You will be redirected to the Admin Registration screen.
3.  Fill in the `Full Name`, `Username`, and `Password` for the administrator.
4.  Tap 'Register'. The admin account will be created, and you'll be redirected to the login screen.

### Logging In 🔑

1.  Enter your `Username` and `Password`.
2.  Tap the 'Login' button.
3.  If login is successful, you'll be directed to the `Dashboard`.

### Core Features in Action 🌟

*   **Dashboard:** Provides a quick overview of daily revenue, orders, expenses, and ongoing orders. Access `Quick Actions` to create orders, log expenses, or manage services.
    *   *Example:* View today's performance metrics at a glance.
*   **Orders:** Manage all customer orders. Filter by status, search by customer name, create new orders, and update order statuses.
    *   *Example:* Add a new order, select services, set customer details, and mark it as paid.
*   **Expenses:** Log daily expenses, categorize them, and add notes.
    *   *Example:* Record the purchase of laundry detergent under the 'Utilities' category.
*   **Price List:** Manage the pricing for different laundry services.
    *   *Example:* Add a new service like 'Ironing' with its price and unit of measure.
*   **Staff Management:** View staff details, add new employees, and manage their roles and salaries.
    *   *Example:* Add a new cleaner to the system and assign them a role.
*   **Settings:** Update your profile, change your password, enable/disable biometric authentication, and view app information.
    *   *Example:* Navigate to 'Change Password' to update your login credentials.

### Password Recovery 🔐

1.  On the Login screen, tap 'Forgot Password?'.
2.  Enter your `Username`.
3.  Tap 'Load Security Questions'.
4.  Answer at least two of the security questions correctly.
5.  Enter and confirm your `New Password`.
6.  Tap 'Reset Password'.

### Setting Recovery Questions ❓

*   After successful login, you might be prompted to set up recovery questions if not already done.
*   Navigate to `Settings` -> `Edit Profile` to set or update your recovery questions.

## Project Structure 📂

```
LaundrySystem/
├── app/
│   ├── build.gradle.kts
│   ├── gradle.properties
│   ├── gradle/
│   │   └── wrapper/
│   │       ├── gradle-wrapper.jar
│   │       └── gradle-wrapper.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── settings.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/sparrow/laundrysys/
│           │   ├── MainActivity.kt
│           │   ├── classes/
│           │   │   ├── AuthViewModel.kt
│           │   │   ├── DialogState.kt
│           │   │   ├── Expense.kt
│           │   │   ├── ForgotPasswordState.kt
│           │   │   ├── Logs.kt
│           │   │   ├── Order.kt
│           │   │   ├── PdfDocumentAdapter.kt
│           │   │   ├── PriceItem.kt
│           │   │   └── User.kt
│           │   ├── ui/
│           │   │   ├── components/
│           │   │   │   └── NavDrawerContent.kt
│           │   │   ├── screens/
│           │   │   │   ├── AdminRegistrationScreen.kt
│           │   │   │   ├── ChangePassword.kt
│           │   │   │   ├── Dashboard.kt
│           │   │   │   ├── Expenses.kt
│           │   │   │   ├── ForgotPassword.kt
│           │   │   │   ├── InitialScreen.kt
│           │   │   │   ├── Login.kt
│           │   │   │   ├── Logs.kt
│           │   │   │   ├── Orders.kt
│           │   │   │   ├── PriceList.kt
│           │   │   │   ├── PrivacyPolicy.kt
│           │   │   │   ├── Register.kt
│           │   │   │   ├── Settings.kt
│           │   │   │   ├── SetupQuestions.kt
│           │   │   │   ├── Staff.kt
│           │   │   │   ├── TermsOfService.kt
│           │   │   │   └── ... (other screens)
│           │   │   ├── theme/
│           │   │   │   ├── Color.kt
│           │   │   │   └── Theme.kt
│           │   │   │   └── Type.kt
│           │   │   └── utilities/
│           │   │       ├── Navigation.kt
│           │   │       ├── changePassword.kt
│           │   │       └── generateInvoice.kt
│           │   │       └── printPdf.kt
│           ├── res/
│           │   ├── mipmap-hdpi/
│           │   ├── mipmap-mdpi/
│           │   ├── mipmap-xhdpi/
│           │   ├── mipmap-xxhdpi/
│           │   ├── mipmap-xxxhdpi/
│           │   ├── values/
│           │   │   ├── colors.xml
│           │   │   ├── ic_launcher_background.xml
│           │   │   ├── strings.xml
│           │   │   └── themes.xml
│           │   └── xml/
│           └── ...
├── .idea/
└── README.md
```

## Contributing 🤝

Contributions are welcome! Please feel free to:

*   **Fork the repository**
*   **Create a new branch** (`git checkout -b feature/YourFeature`)
*   **Make your changes**
*   **Commit your changes** (`git commit -m 'Add some YourFeature'`)
*   **Push to the branch** (`git push origin feature/YourFeature`)
*   **Open a Pull Request**

Please ensure your code adheres to the project's coding standards and includes relevant tests.

## License 📜

This project is licensed under the MIT License - see the [LICENSE](<link_to_license>) file for details.

## Important Links 🔗

*   **Repository:** [LaundrySystem](https://github.com/Unknown1648/LaundrySystem)

## Footer 💻

This project was developed as part of the LaundrySystem Android application.

- **Repository:** [LaundrySystem](https://github.com/Unknown1648/LaundrySystem)
- **Author:** Unknown1648
- **Contact:** <unknown1648@example.com>

Feel free to **fork**, **star ⭐**, and **raise issues** if you encounter any problems or have suggestions!


---
**<p align="center">Generated by [ReadmeCodeGen](https://www.readmecodegen.com/)</p>**