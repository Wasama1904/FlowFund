💰 FlowFund - Personal Budgeting Application
Module: OPSC6311 | Group Project | Android Development

---

<img width="261" height="298" alt="Picture1" src="https://github.com/user-attachments/assets/b972bf7b-63b2-468f-b98f-02e2a7654d2d" />

FlowFund is an Android-based personal finance management application designed to help users track income and expenses, manage budgets, create savings goals, organize spending categories, earn achievement badges, and visualize financial data through intuitive charts and analytics.

---

📱DASHBOARD

<img width="366" height="759" alt="Screenshot 2026-06-15 174616" src="https://github.com/user-attachments/assets/5be324c6-2c3f-47e7-8a23-4af7066528f7" />
<img width="366" height="758" alt="Screenshot 2026-06-15 174635" src="https://github.com/user-attachments/assets/1b28d794-d4e2-4281-88e8-53d34301a287" />

---

TRANSACTIONS ------------------------------------------------------------------------------------------GOALS

<img width="366" height="759" alt="Screenshot 2026-06-15 174652" src="https://github.com/user-attachments/assets/76d8f4bc-b45b-45ff-8742-32d655b6beb7" />
<img width="366" height="757" alt="Screenshot 2026-06-15 174727" src="https://github.com/user-attachments/assets/05c34007-ee1d-4c1c-81c4-8b6e0d26888f" />

---

BUDGETS

<img width="368" height="757" alt="Screenshot 2026-06-15 174707" src="https://github.com/user-attachments/assets/1dae29da-3bbd-4537-b36a-1cff1a79c53b" />

---

👥 Group Members

ST10457568 – Tinotenda Marumahoko

ST10454395 – Cameron Reece Johannes

ST10451742 – Wasama Makolo

ST10438423 – Liam Frans

---

🎬 Demonstration Video

Youtube Link: https://youtu.be/6AmjEgPPVMs?si=IZssGe81FrhMGCvR

---

🔍 Research Foundation

System design was informed by analysis of industry-leading apps including Mint, YNAB, and Wallet by BudgetBakers. 

<img width="1536" height="1024" alt="ChatGPT Image Jun 14, 2026, 09_31_01 PM" src="https://github.com/user-attachments/assets/87fb67c6-7739-4f6d-80ba-1fb01b6b94a1" />

Key insights implemented:

- Automatic expense tracking & categorization
- Category-based budgeting with visual feedback
- Visual reports & spending insights
- Goal tracking with progress indicators
- Budget alerts & notifications
- Multi-currency support concepts
- Shared budgeting framework

---

✨ Core Features


- Authentication: Secure user registration and login
- Transaction Management: Add, edit, delete, and view income/expenses
- Budget System: Create custom budgets and monitor spending vs limits
- Financial Goals: Set, track, and achieve savings targets
- Smart Categories: Color-coded expense categories for easy organization
- Analytics Dashboard: Charts and graphs for financial insights
- Achievement Badges: Gamified rewards for financial milestones
- Personalization: Dark mode, currency preferences, custom insights
- Offline First: All data stored locally for privacy and speed

---

🏗️ System Architecture

FlowFund implements MVVM architecture for clean separation of concerns and maintainability.

- Layer
- Component
- Responsibility
- Database
- FlowFundDatabase
- Room database - central local storage
- Repository
- FlowFundRepository
- Single source of truth for data access
- ViewModel
- AuthViewModel
- Authentication and session logic
- ViewModel
- DashboardViewModel
- Analytics, summaries, chart data
- ViewModel
- BudgetViewModel
- Budget calculations & badge awards
- ViewModel
- TransactionViewModel
- Transaction CRUD operations
- UI
- Activities/Fragments
- User interface and navigation
- Main Modules

---

📈 Goals Module
- Savings goal creation and tracking

Categories Module 
- Custom spending category management

Settings Module 
- User preferences, themes, currency

MainActivity 
- Navigation hub with bottom navigation

---

🛠️ Tech Stack

- Language: Kotlin
- IDE: Android Studio
- Architecture: MVVM + Repository Pattern
- Database: Room Database
- UI: Material Design 3, RecyclerView, Fragments
- Data Storage: SharedPreferences for settings
- Visualization: MPAndroidChart / Charts Library
- Async: Kotlin Coroutines + LiveData/Flow

---

🚀 Getting Started

APK Download Link:

- https://github.com/Wasama1904/FlowFund/releases/download/flowfund/flowfund-v1.0.0.apk

Installation From Github:

- Clone the repository
- Bash
- git clone https://github.com/Wasama1904/FlowFund.git
- Open project in Android Studio
- Sync Gradle and build project
- Run on emulator or physical device
- Default Login

---

🎯 Future Enhancements

- Cloud sync with Firebase
- Bank API integration for auto-import
- Export reports to PDF/CSV
- Multi-currency conversion
- Shared budgets for families
- Bill reminders and recurring transactions

---

📄 Conclusion

FlowFund combines research-driven design with practical MVVM implementation to deliver a user-friendly personal finance solution. By focusing on visual feedback, gamification through badges, and clear financial insights, the app encourages responsible spending and supports long-term financial goals.

---

📜 License

This project is submitted for academic purposes as part of OPSC6311 POE at IIE Varsity College.


---
