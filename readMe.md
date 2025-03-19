# Pulse Services - Dockerized Spring Boot Backend

This repository contains the backend for **Pulse Services**, built with **Spring Boot** and **MySQL**, and containerized using **Docker Compose**.

---

## 🚀 Getting Started

### **1️⃣ Prerequisites**
Ensure you have the following installed:
- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/)
- (Optional) [Expo](https://expo.dev/) if working with the mobile app

---

## **2️⃣ Running the Application**

### **🏃 Start the Application**
Run the following command to **build and start the services**:

```docker-compose up --build -d```

### **🏃 Stopping the Application**

### 🛑 Stop Application (Keep Data)
To stop the application while keeping the database data, run:
```docker-compose down```

To stop the application and delete all database data, run:
```docker-compose down -v```

## Restarting and Managing Containers
### Restart the Backend (Spring Boot Only)
```docker-compose restart pulse-services```

## ♻️ Restart the Entire Application
```docker-compose down && docker-compose up --build -d```

## 🔍 View Logs in Real-Time
```docker logs pulse-services -f```

## 🔄 Rebuild Everything from Scratch
```aiignore
docker-compose down -v
docker system prune -a
docker-compose up --build -d
```

## 6️⃣ Accessing the Database
### To enter the MySQL database running inside Docker, use:
```docker exec -it mysql-db mysql -u pulse -p```

##  Running the Mobile App (Expo)
```expo start```

## 🛠️ Troubleshooting
### If you encounter any build issues, try removing old images and rebuilding:

```
docker-compose down -v
docker system prune -a
docker-compose up --build -d
```

## 🐘 Database Connection Issues
### If MySQL fails to start, check logs:
```docker logs mysql-db```

## Then restart MySQL:
```docker-compose restart mysql-db```

## Dependencies
- "Spring Security" for Spring Boot Starter Security.
- "Spring Web" for Spring Boot Starter Web.
- "Spring Data JPA" for Spring Boot Starter Data JPA.
- "MySQL Driver" and specify the version (8.0.33) for MySQL Connector/J.
- "Lombok" for Project Lombok.

## Database Local
Uses a local mysql database to connect to this spring application

spring JPA configuration is configured to automatically create tables on application start up

### How to start mysql and stop mysql locally

How to start from CLI
```agsl
./gradlew bootRun
```

Start

```
mysql -u root -p
```

Password for mysql
```agsl
new_password
```

login mysql CLI

```
brew services start mysql
```

Stop

```
brew services stop mysql
```

logout

```
exit;
```

### Command to grant privileges to pulse datapase
```
CREATE USER 'pulse'@'localhost' IDENTIFIED BY 'pulse';
GRANT ALL PRIVILEGES ON pulse.* TO 'pulse'@'localhost';
FLUSH PRIVILEGES;

```