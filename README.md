# Order Manager API

A simple Spring Boot REST API to manage orders and stock. Users can create orders for items, and the system automatically allocates available stock to fulfill orders. Notifications are sent by email when an order is complete.

---

## Features

- CRUD operations for **Users**, **Items**, **Orders**, **Stock Movements**
- Automatic order fulfillment based on stock availability.
- Automatic allocation of new stock movements to pending orders.
- Email notifications when orders are completed.
- Logs all operations including stock movements, completed orders, and errors.

---

## Requirements

- **Java 8**
- **Maven 3.8+**
- **PostgreSQL 15+** (or compatible)
- **Docker & Docker Compose** (optional)
- **SMTP email account** (e.g., Gmail with app password)

---

## Order api collection
- api/collection/OrderApi

---

## Environment Variables

Before running the application, set the email credentials in application.properties file

```bash
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

---

### Building the Project
To build the project locally:
```sh
mvn clean install
mvn spring-boot:run 
```

---

### Running PostgreSQL with Docker

You can run a local PostgreSQL instance using Docker:
```sh
docker-compose up -d
```
