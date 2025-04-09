# Hotel Booking Management System

A comprehensive hotel booking management application featuring a modern JavaFX UI, robust SQLite database backend, and modular architecture. This project demonstrates best practices in Java development, database design, and user interface engineering.

## Tech Stack

- **Java 17+**
- **JavaFX** (UI framework)
- **SQLite** (Embedded database)
- **Maven** (Dependency management)
- **FXML** (UI layout)
- **CSS** (UI styling)

## Key Components

### 1. Database Layer

- **hotel_schema.sql**: Defines all tables, relationships, and initial data for the hotel system.
- **DatabaseUtil.java**: Handles database connections, queries, and transactions.
- **hotel.db**: SQLite database file storing all persistent data.

### 2. Model Layer

- **Admin, Guest, Room, Reservation, Billing, Feedback**: Java classes representing core entities, mapped to database tables.
- **RoomType.java**: Enum for room categories.

### 3. Controller Layer

- **AdminController**: Admin authentication and management.
- **MainController**: Main application logic and navigation.
- **ReservationController**: Handles reservation creation, modification, and status updates.
- **FeedbackController**: Manages guest feedback and ratings.
- **HelpAndInformationController**: Provides help and information to users.

### 4. Utilities

- **Helper.java**: General utility functions.
- **LoggerUtil.java**: Logging and error reporting.
- **ReservationHelper.java**: Reservation-specific utilities.

### 5. UI Layer

- **FXML Files**: Define layouts for admin, reservation, feedback, help, and welcome screens.
- **CSS Stylesheet**: Customizes the look and feel of the application.
- **Images**: Visual assets for rooms and UI backgrounds.

## Features

- **Admin Login**: Secure authentication for hotel staff.
- **Guest Management**: Add, view, and manage guest profiles.
- **Room Inventory**: Categorized rooms (Single, Double, Deluxe, Penthouse) with availability tracking.
- **Reservation System**: Book, modify, and cancel reservations; map rooms to reservations.
- **Billing**: Automated bill generation with discounts and tax calculations.
- **Feedback Collection**: Guests can rate and comment on their stay.
- **Help & Information**: In-app guidance for users.

## Relationships & Flow

- **Admins** manage hotel operations via the admin panel.
- **Guests** are registered and can make reservations.
- **Rooms** are categorized and mapped to reservations.
- **Reservations** link guests to rooms for specific dates.
- **Billing** is generated per reservation, factoring in discounts and taxes.
- **Feedback** is collected post-stay, linked to both guest and reservation.

## Getting Started

- Clone the repository.
- Open in IntelliJ IDEA or any Java IDE with JavaFX support.
- Run `HotelBookingApplication.java` to launch the app.
