Backend services for pulse prototype

## Dependencies
- "Spring Security" for Spring Boot Starter Security.
- "Spring Web" for Spring Boot Starter Web.
- "Spring Data JPA" for Spring Boot Starter Data JPA.
- "MySQL Driver" and specify the version (8.0.33) for MySQL Connector/J.
- "Lombok" for Project Lombok.

## Database
Uses a local mysql database to connect to this spring application

spring JPA configuration is configured to automatically create tables on application start up

### How to start mysql and stop mysql locally

Start

```
mysql -u root -p
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