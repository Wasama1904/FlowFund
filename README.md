FlowFund Project 
Project: FlowFund Personal Budgeting Application
Module: OPSC6311 POE

Group participants
ST10457568 [TINOTENDA MARUMAHOKO]
ST10454395 [CAMERON REECE JOHANNES]
ST10451742 [WASAMA MAKOLO]
ST10438423[LIAM FRANS]

Project Overview
FlowFund is an Android-based personal finance management application designed to help users track income and expenses, manage budgets, create savings goals, organize spending categories, earn achievement badges, and visualize financial data.

Research Foundation
The system design was informed by research on Mint, YNAB, and Wallet by BudgetBakers. Key features identified for inclusion were automatic expense tracking, category-based budgeting, visual reports, goal tracking, spending insights, alerts, multi-currency support, and shared budgeting concepts.

Core Features
•	User registration and login authentication
•	Transaction management (add, edit, delete, view)
•	Budget creation and monitoring
•	Financial goal tracking
•	Expense categories with colour coding
•	Dashboard with charts and analytics
•	Achievement badge system
•	Dark mode and currency preferences
•	Personalized financial insights

System Architecture
The application follows the MVVM architecture pattern. Room Database is used for local storage, Repositories manage data access, ViewModels handle business logic, and Activities/Fragments provide the user interface.

Main Components
•	FlowFundDatabase - central Room database
•	FlowFundRepository - data access layer
•	AuthViewModel - authentication logic
•	DashboardViewModel - analytics and summaries
•	BudgetViewModel - budget calculations and badge awards
•	TransactionViewModel - transaction management
•	Goals Module - savings goal tracking
•	Categories Module - spending category management
•	Settings Module - user preferences
•	MainActivity - application navigation hub

Technology Stack
Android Studio, Kotlin, Room Database, RecyclerView, SharedPreferences, Material Design Components, Charts and Graphing Libraries.

Conclusion
FlowFund combines research findings and practical implementation to provide a user-friendly personal finance solution that encourages responsible spending and supports long-term financial goals.
