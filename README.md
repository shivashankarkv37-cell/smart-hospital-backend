# 🏥 Smart Hospital Management System – Backend

A backend application for managing hospital operations, including patient registration, doctor management, department management, and appointment scheduling.

Built using **Java, Spring Boot, Spring Security, JWT Authentication, Spring Data JPA, and MySQL**, this project provides REST APIs for secure and efficient hospital management.

## 🚀 Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot | Backend application framework |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Dependency management |
| REST APIs | Client-server communication |
| Git & GitHub | Version control |

## ✨ Features

### 1. Authentication & Authorization
- User registration and login.
- JWT-based authentication.
- Password encryption.
- Role-Based Access Control (RBAC).
- Secure API endpoints.

### 2. Patient Management
- Patient registration.
- Patient information management.
- Retrieve patient details.
- Secure patient-related operations.

### 3. Doctor Management
- Add and manage doctors.
- Retrieve doctor information.
- Associate doctors with departments.

### 4. Department Management
- Create and manage hospital departments.
- Retrieve department information.
- Organize doctors based on departments.

### 5. Appointment Management
- Book appointments.
- Manage appointment details.
- Cancel appointments.
- Check-in patients.
- Track consultation status.
- Complete appointments.

### 6. Appointment Status Tracking

Appointments support the following statuses:

- `BOOKED`
- `CANCELLED`
- `CHECKED_IN`
- `IN_CONSULTATION`
- `COMPLETED`

## 🏗️ Project Architecture

The backend follows a layered architecture to maintain separation of concerns and improve maintainability.

```text
smart-hospital-backend/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/smart_hospital_backend/
│       │       │
│       │       ├── controller/
│       │       ├── service/
│       │       ├── repository/
│       │       ├── entity/
│       │       ├── dto/
│       │       ├── config/
│       │       ├── security/
│       │       └── SmartHospitalBackendApplication.java
│       │
│       └── resources/
│           └── application.properties
│
├── pom.xml
└── README.md
```

**Architecture responsibilities:**

- Controller: Handles incoming HTTP requests.
- Service: Contains business logic.
- Repository: Communicates with the database.
- Entity: Represents database tables.
- DTO: Transfers data between application layers.
- Security: Handles authentication and authorization.
- Config: Manages application configurations.

## ⚙️ Getting Started

Follow these instructions to run the backend locally.

### Prerequisites

Make sure you have installed:

- Java JDK 21
- Maven
- MySQL Server
- IntelliJ IDEA or VS Code
- Postman (for API testing)

### 1. Clone the Repository

```bash
git clone https://github.com/shivashankarkv37-cell/Smart_Hospital.git
```

### 2. Navigate to Backend Directory

```bash
cd Smart_Hospital/smart-hospital-backend
```

Adjust the directory if your backend folder has a different name.

### 3. Create MySQL Database

Open MySQL and execute:

```sql
CREATE DATABASE smart_hospital;
```

### 4. Configure Database

Update your `application.properties` file:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_hospital
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

server.port=8080
```

Configure your database credentials as environment variables.

Also configure your JWT secret using a Base64-encoded secret of at least 256 bits.

### 5. Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or run the main application class from IntelliJ IDEA.

### 6. Verify Backend

Once the application starts successfully, test:

```http
GET http://localhost:8080/api/health
```

Expected response:

```text
Smart Hospital Backend is running!
```

## 🔐 Security

The application uses Spring Security and JWT for securing REST APIs.

Security implementation includes:

- Stateless authentication.
- JWT token validation.
- Password hashing.
- Role-based access control.
- Protected API endpoints.

Supported roles:

| Role | Responsibility |
|---|---|
| ADMIN | Administrative operations |
| DOCTOR | Doctor-related operations |
| PATIENT | Patient-related operations |
| RECEPTIONIST | Appointment and patient operations |

## 🔌 API Modules

The backend exposes REST API endpoints for:

| Module | Endpoint |
|---|---|
| Authentication | `/api/auth/login` |
| Patient Management | `/api/patients` |
| Doctor Management | `/api/doctors` |
| Department Management | `/api/departments` |
| Appointment Management | `/api/appointments` |
| Health Check | `/api/health` |

*Refer to the controller implementations for supported HTTP methods, request bodies, and authorization requirements.*

## 🧪 API Testing

Use Postman to test the REST APIs.

Testing includes:

- User registration and login.
- JWT token generation and validation.
- Protected endpoint access.
- Role-based authorization.
- Patient and doctor management.
- Appointment booking and status updates.

## 🎯 Learning Outcomes

Through this project, I gained practical experience in:

- Developing RESTful APIs using Spring Boot.
- Implementing JWT authentication using Spring Security.
- Understanding Role-Based Access Control.
- Integrating MySQL with Spring Data JPA and Hibernate.
- Implementing layered backend architecture.
- Managing application entities and relationships.
- Handling real-world hospital management workflows.

## 🔮 Future Improvements

- Refresh token implementation.
- Email notifications for appointments.
- Docker-based deployment.
- Redis caching.
- API documentation using Swagger/OpenAPI.
- Improved logging and exception handling.

## 👨‍💻 Developer

**Shivashankar KV**

BCA Student | Java Backend Developer

- GitHub: [@shivashankarkv37-cell](https://github.com/shivashankarkv37-cell)

---

⭐ If you find this project interesting, feel free to explore the repository.
