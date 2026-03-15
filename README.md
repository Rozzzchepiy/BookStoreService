# Book Service Store

A robust, secure, and scalable backend application for managing a bookstore. Built with Java and Spring Boot, this project follows the **MVC architectural pattern** while exposing a strictly **stateless** secured by JWT. 

## 🚀 Key Features

* **Stateless Authentication & Security:** Implemented JWT-based authentication. The application is completely stateless, ensuring high scalability. Secure communication is enforced via **HTTPS/SSL**.
* **Advanced User Management:** * User registration with strict payload **validation** (Hibernate Validator).
    * Secure **password recovery** flow.
    * Admin capabilities to manage access, including the ability to **block/ban users**.
* **Dynamic Advanced Search:** Implemented **Spring Data JPA Specifications** to allow complex, dynamic querying and filtering of books without hardcoding multiple repository methods.
* **Global Exception Handling:** Centralized error handling using `@ControllerAdvice` to provide consistent, user-friendly, and localized JSON error responses across the entire API.
* **Localization (i18n):** Support for multiple languages for validation messages and exceptions.
* **Structured Logging:** Configured application-wide logging to track requests, errors, and system events effectively.
* **Environment Configuration:** Clean separation of environments using `application.properties` (or YAML).
* **Database Seeding:** Automated population of initial test data (roles, admin accounts, sample books) using `data.sql` on startup.

## 🛠 Tech Stack

* **Language:** Java 
* **Framework:** Spring Boot (Spring Web MVC)
* **Security:** Spring Security, JWT (JSON Web Tokens)
* **Persistence:** Spring Data JPA, Hibernate
* **Database:** Relational Database (SQL)
* **Validation:** Spring Boot Validation

## ⚙️ Architecture & Design Decisions

Although the project utilizes `spring-boot-starter-web` (Spring MVC), it is designed strictly as a backend REST API. 
* Views are completely decoupled (ready for any frontend like React, Angular, or a mobile app).
* Session management in Spring Security is set to `STATELESS`, meaning every request is independently authenticated via the Authorization header (Bearer Token).

## 🚦 Getting Started

### Prerequisites
* Java 17 or higher
* Maven / Gradle
* A running SQL Database instance
