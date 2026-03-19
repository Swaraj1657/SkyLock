<div align="center">

<img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
<img src="https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
<img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
<img src="https://img.shields.io/badge/Cloudflare-Deployed-F38020?style=for-the-badge&logo=cloudflare&logoColor=white" />

<br /><br />

# ☁️ SkyLock

### *Secure Cloud Storage. Built for Scale.*

**SkyLock** is a high-performance, self-hosted cloud storage platform built with Spring Boot. It supports multi-gigabyte chunked uploads, hierarchical folder management, JWT-based authentication, file sharing, and SMTP-powered email workflows — all wrapped in a sleek Glassmorphism-styled frontend.

<br />

[![Live Demo](https://img.shields.io/badge/🌐%20Live%20Demo-skylock.dpdns.org-0A84FF?style=for-the-badge)](https://skylock.dpdns.org)
&nbsp;&nbsp;
[![GitHub Repo](https://img.shields.io/badge/GitHub-SkyLock-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Swaraj1657/SkyLock)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Configuration Reference](#-configuration-reference)
- [Performance Notes](#-performance-notes)
- [Security Considerations](#-security-considerations)
- [Roadmap](#-roadmap)
- [Author](#-author)

---

## 🧭 Overview

SkyLock provides users with a private, secure space to upload, organize, and share files — deployed and running live at [skylock.dpdns.org](https://skylock.dpdns.org).

The backend is engineered for efficiency: file data is streamed directly to disk using Java NIO's `FileChannel`, keeping JVM heap memory consumption minimal even under heavy concurrent upload loads. Database records are only persisted after physical file verification, ensuring consistency between the filesystem and MySQL at all times.

The frontend is built entirely with Thymeleaf server-side rendering and modular ES6 JavaScript — no heavyweight frontend framework required — delivering fast initial page loads with a modern Glassmorphism aesthetic.

---

## ✨ Key Features

### 🔐 Security & Identity
**JWT Authentication** provides stateless session management using signed JSON Web Tokens (JJWT 0.12.6). Spring Security 6 enforces full protection of all API and page routes with role-based access control. Passwords are hashed through Spring Security's encoding pipeline and email verification is required for new accounts.

### 📁 File & Folder Management
Create, rename, move, and delete deeply nested folders to organize content exactly how you want. Files are automatically categorized by type (images, documents, videos, archives, etc.) with contextual UI icons. Per-user storage quotas are tracked dynamically and enforced against configurable limits. File and folder sharing between registered users is managed server-side.

### 📤 High-Performance Chunked Uploads
Large files are split into segments client-side and streamed independently to the server — enabling reliable uploads even over unstable connections. Once all chunks arrive, Java NIO's `FileChannel.transferTo` reconstructs the original file at the OS level with zero JVM heap buffering. Each chunk is an independent unit, laying the groundwork for full resumable upload recovery. Both per-file and per-request size limits are configurable (currently 1 GB).

### 🖥️ Frontend
Server-side rendering via Thymeleaf delivers fast initial loads and clean HTML output. Frontend logic is organized into reusable ES6 modules. The Glassmorphism UI is built in pure CSS3 — no React, Vue, or Angular. Dashboard search and filtering let users find files and folders instantly.

### 📧 Email Notifications
Account verification, sharing invitations, and other events trigger transactional emails via Brevo SMTP relay through Spring Mail.

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.5.10 |
| **Security** | Spring Security 6 + JJWT | 0.12.6 |
| **ORM** | Spring Data JPA + Hibernate | — |
| **Database** | MySQL | 8.0 |
| **File I/O** | Java NIO `FileChannel` | — |
| **Frontend** | Thymeleaf + Modular ES6 JS + Vanilla CSS3 | — |
| **Email** | Spring Mail + Brevo SMTP | — |
| **Utilities** | Lombok | — |
| **Build Tool** | Maven | 3.6+ |
| **Deployment** | Cloudflare (DNS & proxy + HTTPS) | — |

---

## 🏗️ Architecture

### Chunked Upload Pipeline

SkyLock uses a two-phase upload model to handle large files reliably without memory pressure:

```
Client                              Server
  │                                    │
  ├── Chunk 0 ────────────────────►  TempDir/{uploadId}/chunk_0
  ├── Chunk 1 ────────────────────►  TempDir/{uploadId}/chunk_1
  ├── Chunk N ────────────────────►  TempDir/{uploadId}/chunk_N-1
  │                                    │
  └── Merge Request ───────────────►  ChunkUploadService
                                        │
                                        ├── Validates all chunks present
                                        ├── FileChannel.transferTo() → final file
                                        └── Persists metadata to MySQL (transactional)
```

**Key design decisions:**

- **Temp directory isolation** — Each upload session gets its own directory, preventing conflicts between concurrent uploads by the same or different users.
- **NIO Zero-Copy** — `FileChannel.transferTo` delegates data movement to the OS kernel where possible, bypassing the Java heap entirely during the merge step.
- **Transactional consistency** — Database records are committed only after the physical file is verified on disk, keeping the filesystem and database in sync at all times.

---

## 📁 Project Structure

```
SkyLock/
├── src/
│   └── main/
│       ├── java/com/example/swaraj/
│       │   ├── controller/        # MVC controllers (Auth, File, Folder, Share)
│       │   ├── service/           # Business logic (ChunkUploadService, FileService, etc.)
│       │   ├── repository/        # Spring Data JPA repositories
│       │   ├── model/             # JPA entities (User, FileEntity, FolderEntity, etc.)
│       │   ├── dto/               # Request / Response data transfer objects
│       │   ├── security/          # JWT filter, UserDetailsService, SecurityConfig
│       │   └── config/            # Application configuration beans
│       └── resources/
│           ├── templates/         # Thymeleaf HTML templates
│           ├── static/            # CSS, JS modules, icons
│           └── application.properties
├── .mvn/wrapper/                  # Maven wrapper (no local Maven install required)
├── mvnw / mvnw.cmd                # Maven wrapper scripts
└── pom.xml                        # Dependencies & build config
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21 JDK** — [Adoptium](https://adoptium.net/) recommended
- **Maven 3.6+** — or use the included `mvnw` wrapper (no install needed)
- **MySQL 8.0** — running locally or on a remote server

### 1. Clone the Repository

```bash
git clone https://github.com/Swaraj1657/SkyLock.git
cd SkyLock
```

### 2. Create the Database

```sql
CREATE DATABASE skylock;
```

> Use a dedicated MySQL user with limited privileges — avoid `root` in production. See [Security Considerations](#-security-considerations).

### 3. Create Storage Directories

SkyLock stores uploaded files on the local filesystem. Create the required directories before starting:

```bash
# Linux / macOS
mkdir -p /var/skylock/storage/temp

# Windows (PowerShell)
New-Item -ItemType Directory -Path "C:\SkyLock_Cloud\temp" -Force
```

> Update `file.storage.path` and `file.temp.path` in `application.properties` to match your chosen paths.

### 4. Configure the Application

Edit `src/main/resources/application.properties` with your values:

```properties
spring.application.name=SkyLock

# --- Database ---
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/skylock
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# --- Server ---
server.address=0.0.0.0
server.port=8080

# --- JPA ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# --- JWT (use a strong 64+ character secret in production) ---
app.jwt.secret=${JWT_SECRET:ReplaceThisWithASecureRandomSecret!!!}

# --- File Storage ---
file.storage.path=/your/storage/path/
file.temp.path=/your/storage/path/temp/

# --- Upload Limits ---
spring.servlet.multipart.max-file-size=1000MB
spring.servlet.multipart.max-request-size=1000MB

# --- Email (Brevo SMTP or any compatible relay) ---
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=YOUR_SMTP_USERNAME
spring.mail.password=YOUR_SMTP_API_KEY
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 5. Build & Run

```bash
# Using Maven wrapper (no Maven installation required)
./mvnw clean install
./mvnw spring-boot:run

# Or with a local Maven installation
mvn clean install
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

---

## ⚙️ Configuration Reference

| Property | Description | Default |
|---|---|---|
| `spring.datasource.url` | MySQL connection URL | `jdbc:mysql://127.0.0.1:3306/skylock` |
| `app.jwt.secret` | JWT signing secret — minimum 32 chars, 64+ recommended | Set via `JWT_SECRET` env var |
| `file.storage.path` | Root directory for stored files | *(required)* |
| `file.temp.path` | Temp directory for upload chunk staging | *(required)* |
| `spring.servlet.multipart.max-file-size` | Maximum individual file size | `1000MB` |
| `spring.servlet.multipart.max-request-size` | Maximum total HTTP request size | `1000MB` |
| `spring.jpa.hibernate.ddl-auto` | Schema strategy — use `validate` in production | `update` |

---

## 📊 Performance Notes

- **Upload throughput** is bounded by client bandwidth and local disk I/O. The `FileChannel` merge step typically completes in under a second even for multi-GB files on modern hardware.
- **Memory footprint during uploads** stays minimal regardless of file size — data is streamed directly to disk rather than buffered in the JVM heap.
- **Concurrent uploads** are session-isolated, meaning multiple users uploading simultaneously do not interfere with each other's temp directories.
- **Language breakdown:** Java (35.6%), JavaScript (28.1%), CSS (19.4%), HTML (16.9%) — a well-balanced full-stack split.

---

## 🔒 Security Considerations

- **Never commit `application.properties`** with real credentials. Use environment variables (e.g., `${JWT_SECRET}`) or a secrets manager for all sensitive values.
- **JWT secrets** should be randomly generated strings of at least 64 characters for production deployments.
- **Database user** should have `SELECT`, `INSERT`, `UPDATE`, `DELETE` privileges only — never use `root` in production.
- **Storage paths** should sit outside the web root and be inaccessible via direct HTTP.
- **HTTPS** is strongly recommended in production. The live deployment at `skylock.dpdns.org` uses Cloudflare's proxy to handle TLS automatically.
- Switch `spring.jpa.hibernate.ddl-auto` from `update` to `validate` in production to prevent accidental schema modifications.

---

## 🗺️ Roadmap

- [ ] Resumable upload recovery — persist chunk state across browser sessions
- [ ] Public shareable links with optional expiry dates and password protection
- [ ] Admin dashboard for user management, quota overrides, and storage monitoring
- [ ] Object storage backend (S3-compatible) as an alternative to local filesystem
- [ ] Two-factor authentication (TOTP / authenticator app)
- [ ] Per-user activity and audit logs

---

## 👤 Author

**Swaraj**

- GitHub: [@Swaraj1657](https://github.com/Swaraj1657)
- Email: [swaraj1675@gmail.com](mailto:swaraj1675@gmail.com)
- Live: [skylock.dpdns.org](https://skylock.dpdns.org)

---

<div align="center">

Built with Spring Boot · Secured with JWT · Deployed on Cloudflare

**[⭐ Star this repo](https://github.com/Swaraj1657/SkyLock)** if you find it useful!

</div>
