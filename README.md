💰 FlowFund - Personal Budgeting Application
Module: OPSC6311 | Group Project | Android Development

FlowFund is an Android-based personal finance management application designed to help users track income and expenses, manage budgets, create savings goals, organize spending categories, earn achievement badges, and visualize financial data through intuitive charts and analytics.

📱 Demo
Dashboard

Transactions

Goals

Budgets

Add screenshot

Add screenshot

Add screenshot

Add screenshot

👥 Group Members
ST10457568 – Tinotenda Marumahoko
ST10454395 – Cameron Reece Johannes
ST10451742 – Wasama Makolo
ST10438423 – Liam Frans
🔍 Research Foundation
System design was informed by analysis of industry-leading apps including Mint, YNAB, and Wallet by BudgetBakers. Key insights implemented:

Automatic expense tracking & categorization
Category-based budgeting with visual feedback
Visual reports & spending insights
Goal tracking with progress indicators
Budget alerts & notifications
Multi-currency support concepts
Shared budgeting framework
✨ Core Features
Authentication: Secure user registration and login
Transaction Management: Add, edit, delete, and view income/expenses
Budget System: Create custom budgets and monitor spending vs limits
Financial Goals: Set, track, and achieve savings targets
Smart Categories: Color-coded expense categories for easy organization
Analytics Dashboard: Charts and graphs for financial insights
Achievement Badges: Gamified rewards for financial milestones
Personalization: Dark mode, currency preferences, custom insights
Offline First: All data stored locally for privacy and speed
🏗️ System Architecture
FlowFund implements MVVM architecture for clean separation of concerns and maintainability.

Layer

Component

Responsibility

Database

FlowFundDatabase

Room database - central local storage

Repository

FlowFundRepository

Single source of truth for data access

ViewModel

AuthViewModel

Authentication and session logic

ViewModel

DashboardViewModel

Analytics, summaries, chart data

ViewModel

BudgetViewModel

Budget calculations & badge awards

ViewModel

TransactionViewModel

Transaction CRUD operations

UI

Activities/Fragments

User interface and navigation

Main Modules

Goals Module - Savings goal creation and tracking
Categories Module - Custom spending category management
Settings Module - User preferences, themes, currency
MainActivity - Navigation hub with bottom navigation
🛠️ Tech Stack
Language: Kotlin
IDE: Android Studio
Architecture: MVVM + Repository Pattern
Database: Room Database
UI: Material Design 3, RecyclerView, Fragments
Data Storage: SharedPreferences for settings
Visualization: MPAndroidChart / Charts Library
Async: Kotlin Coroutines + LiveData/Flow
🚀 Getting Started
Prerequisites
Android Studio Hedgehog | 2023.1.1 or newer
Minimum SDK: 24 [Android 7.0]
Target SDK: 34 [Android 14]
Installation
Clone the repository
Bash
git clone https://github.com/yourusername/FlowFund.git
Open project in Android Studio
Sync Gradle and build project
Run on emulator or physical device
Default Login
For testing purposes: test@flowfund.com / Test1234

📂 Project Structure
Code
app/
├── data/
│   ├── database/          # Room entities, DAOs, database
│   └── repository/        # FlowFundRepository
├── ui/
│   ├── auth/             # Login & registration
│   ├── dashboard/        # Main dashboard & charts
│   ├── transactions/     # Add/edit transactions
│   ├── budgets/          # Budget management
│   ├── goals/            # Savings goals
│   └── settings/         # App preferences
├── viewmodel/            # All ViewModels
└── utils/                # Helpers, extensions

8 lines hidden
🎯 Future Enhancements
Cloud sync with Firebase
Bank API integration for auto-import
Export reports to PDF/CSV
Multi-currency conversion
Shared budgets for families
Bill reminders and recurring transactions
📄 Conclusion
FlowFund combines research-driven design with practical MVVM implementation to deliver a user-friendly personal finance solution. By focusing on visual feedback, gamification through badges, and clear financial insights, the app encourages responsible spending and supports long-term financial goals.

📜 License
This project is submitted for academic purposes as part of OPSC6311 POE at IIE Varsity College.
