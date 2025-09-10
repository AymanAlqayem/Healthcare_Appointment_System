# 🏥 Smart Healthcare Appointment System

A **Spring Boot 3** application designed to streamline healthcare management processes.  
This system enables hospitals to efficiently manage **patients, doctors, appointments, prescriptions, and medical records** with robust security and modern architectural patterns.

---

## 🚀 Key Features
- 🔐 **Role-Based Access Control** – Admin, Doctor, and Patient roles  
- 👨‍⚕️ **Doctor Management** – Add, update, and remove doctors with specialty-based search  
- 🧑‍🤝‍🧑 **Patient Management** – Registration and profile updates  
- 📅 **Appointment System** – Booking, cancellation, completion with double-booking prevention  
- 📑 **Medical Records** – Prescriptions and history stored in **MongoDB**  
- 📝 **Comprehensive Logging** – Using Spring AOP for critical operations  
- ⚡ **Caching** – Frequently accessed doctor data (Ehcache, Hibernate 1st/2nd level cache)  
- 🌐 **RESTful APIs** – With validation and error handling  

---

## 🛠 Technology Stack
- **Framework:** Spring Boot `3.5.5`  
- **Security:** Spring Security with JWT  
- **Databases:**  
  - Relational: MySQL/PostgreSQL with JPA/Hibernate  
  - NoSQL: MongoDB (prescriptions & medical records)  
- **Caching:** Hibernate + Ehcache  
- **AOP:** Spring AOP  
- **Testing:** JUnit 5 + Mockito  
- **Documentation:** OpenAPI/Swagger  
- **Build Tool:** Maven  

---

## ⚙️ Installation & Setup

### Prerequisites
- ☕ Java 17+  
- 📦 Maven 3.6+  
- 🗄 MySQL/PostgreSQL  
- 🍃 MongoDB  
- 🔗 Git  

### Steps
```bash
# Clone the repository
git clone <repository-url>
cd Healthcare_Appointment_System

# Configure databases (MySQL/Postgres + MongoDB) in
# src/main/resources/application.properties

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
