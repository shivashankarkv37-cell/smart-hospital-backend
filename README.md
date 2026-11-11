# Smart Hospital Management System — Backend

Backend REST API for the Smart Hospital Management System, built using Java and Spring Boot.

## Tech Stack

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* MySQL
* Maven

## Day 1 Features

* Spring Boot project setup
* Health-check REST API
* MySQL database configuration
* Initial frontend–backend integration

## Project Structure

```text
src/main/java/com/example/smarthospital/
├── controller/
├── service/
├── repository/
├── entity/
└── SmartHospitalApplication.java

src/main/resources/
└── application.properties
```

## Database Setup

Create the database in MySQL:

```sql
CREATE DATABASE smart_hospital;
```

Configure `application.properties`:

```properties
spring.application.name=smart-hospital-backend
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/smart_hospital
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
```

## Run the Application

```bash
mvn spring-boot:run
```

## Health Check API

**Endpoint:** `GET /api/health`

**URL:** `http://localhost:8080/api/health`

**Response:**

```text
Smart Hospital Backend is running!
```

## Current Status

* [x] Spring Boot project setup
* [x] Health-check API
* [ ] MySQL connection verification
* [ ] Database schema design
* [ ] JPA entity implementation
* [ ] Authentication APIs
