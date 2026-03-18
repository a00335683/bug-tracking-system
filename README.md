# Bug Tracking & Issue Reporting System

This project is a full-stack web application developed for the Web Technologies module.

The system allows teams to manage projects and track software issues from reporting to resolution.


## Setup Instructions

To run this project locally, make sure the following are installed:

- Java (JDK 21)
- Maven
- MySQL (via MySQL Workbench)

### Steps to run the project

1. Clone the repository:
   git clone <your-repo-link>

2. Open the project in your IDE (e.g., IntelliJ)

3. Configure the database in `application.yml`:
   - Set your MySQL username and password
   - Ensure MySQL is running
   - Create the required database if not already created

4. Run the Spring Boot application:
   mvn spring-boot:run

5. Open the application in your browser:
   http://localhost:8081

## Technologies Used
- Spring Boot (REST API)
- MySQL
- JavaScript / jQuery
- Bootstrap
- JWT Authentication

## API Documentation
Postman Collection: [Download API Collection](./bug-tracking-api.postman_collection.json)

This collection contains all API endpoints used in the system.

Import it into Postman to test the API.

### Default Test Users

The following accounts can be used to test the system:

- Admin → username: admin / password: admin123  
- Tester → username: tester1 / password: tester123  
- Developer → username: dev1 / password: dev123  

(Note: These are sample accounts for local testing only.)

## User Manual
Part 1: [User Manual Part 1](User_Manual_Part1.pdf)  
Part 2: [User Manual Part 2](User_Manual_Part2.pdf)

The user manual with screenshots explains how to use the system for:

- Administrator
- Tester
- Developer
