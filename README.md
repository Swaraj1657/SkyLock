# ☁️ SkyLock — Secure Cloud Storage & File Sharing

> A high-performance, self-hosted cloud storage solution built with Spring Boot — designed for large-scale file management, hierarchical organization, and seamless collaboration.

🔗 **Live Demo:** [skylock.dpdns.org](https://skylock.dpdns.org)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Screenshots](#-screenshots)
- [Getting Started](#-getting-started)
- [Configuration Reference](#-configuration-reference)
- [API Overview](#-api-overview)
- [Performance Notes](#-performance-notes)
- [Security Considerations](#-security-considerations)
- [Roadmap](#-roadmap)

---

## 🧭 Overview

SkyLock is a full-stack cloud storage platform that provides users with a private, secure space to upload, organize, and share files. It supports chunked uploads for multi-gigabyte files, hierarchical folder management, JWT-based authentication, and SMTP-based email flows — all wrapped in a Glassmorphism-styled frontend built with Thymeleaf and vanilla JavaScript.

The backend is designed around efficiency: file data is streamed directly to disk using Java NIO's `FileChannel`, keeping memory consumption low even under heavy upload loads.

---

## ✨ Key Features

### 🔐 Security & Identity
- **JWT Authentication** — Stateless session management using signed JSON Web Tokens (JJWT library).
- **Spring Security Integration** — Full protection of API and page routes; role-based access enforcement.
- **Email Verification & Notifications** — Account actions and sharing events trigger emails via Brevo SMTP relay.
- **Secure Password Handling** — Passwords are hashed and stored securely using Spring Security's encoding pipeline.

### 📁 File & Folder Management
- **Hierarchical Directory Structures** — Create, rename, move, and delete nested folders to organize content any way you like.
- **Intelligent File Type Detection** — Files are automatically categorized (images, documents, videos, archives, etc.) with contextual UI icons.
- **Real-Time Storage Quotas** — Per-user storage usage is tracked dynamically and checked against configurable maximum limits.
- **File Sharing** — Share files and folders with other registered users, with access managed server-side.

### 📤 High-Performance Uploads
- **Chunked Upload System** — Large files are split into segments on the client side and streamed independently to the server, enabling reliable uploads over unstable connections.
- **Zero-Copy Merging via NIO** — Once all chunks arrive, `FileChannel.transferTo` is used to reconstruct the original file at the filesystem level — no heap buffering, maximum I/O throughput.
- **Resumable-Friendly Design** — Each chunk is an independent unit, laying the groundwork for full resumable upload support.
- **Configurable Size Limits** — Both per-file and per-request limits are configurable (currently set to 1 GB).

### 🖥️ Frontend
- **Thymeleaf Server-Side Rendering** — Pages are rendered server-side, enabling fast initial load and SEO-friendly output.
- **Modular ES6 JavaScript** — Frontend logic is organized into clean, reusable modules.
- **Glassmorphism Design** — A modern, frosted-glass aesthetic built entirely in vanilla CSS3 — no frontend framework required.
- **Dashboard Search & Filtering** — Quick-access search for files and folders directly within the user dashboard.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.10 |
| Security | Spring Security 6, JJWT |
| Database | MySQL 8.0 (metadata & user relations) |
| File Storage | Local filesystem via Java NIO `FileChannel` |
| Frontend | Thymeleaf, Modular ES6 JS, Vanilla CSS3 |
| Email | Brevo SMTP Relay via Spring Mail |
| Build Tool | Maven 3.6+ |
| Deployment | Cloudflare (DNS & proxy) |

---

## 🏗️ Architecture

### Chunked Upload Pipeline

SkyLock uses a two-phase upload model to handle large files reliably and efficiently:

```
Client                          Server
  │                                │
  ├── Chunk 1 ──────────────────► TempDir / {uploadId} / chunk_0
  ├── Chunk 2 ──────────────────► TempDir / {uploadId} / chunk_1
  ├── Chunk N ──────────────────► TempDir / {uploadId} / chunk_N-1
  │                                │
  └── Merge Request ────────────► ChunkUploadService
                                   │
                                   ├── Validates all chunks present
                                   ├── FileChannel.transferTo() → final file
                                   └── Persists metadata to MySQL (transactional)
```

**Key design decisions:**

- **Temp directory isolation** — Each upload session gets its own directory, preventing conflicts between concurrent uploads by the same user.
- **NIO Zero-Copy** — `FileChannel.transferTo` delegates data movement to the OS kernel where possible, bypassing Java heap entirely for the merge step.
- **Transactional writes** — Database records are only committed after the physical file is verified on disk, ensuring consistency between the filesystem and the database.

### Project Structure

```
src/
└── main/
    ├── java/com/skylock/
    │   ├── controller/        # MVC controllers (Auth, File, Folder, Share)
    │   ├── service/           # Business logic (ChunkUploadService, FileService, etc.)
    │   ├── repository/        # Spring Data JPA repositories
    │   ├── model/             # JPA entities (User, FileEntity, FolderEntity, etc.)
    │   ├── dto/               # Request/Response data transfer objects
    │   ├── security/          # JWT filter, UserDetailsService, SecurityConfig
    │   └── config/            # App configuration beans
    └── resources/
        ├── templates/         # Thymeleaf HTML templates
        ├── static/            # CSS, JS modules, icons
        └── application.properties
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK ([Adoptium](https://adoptium.net/) recommended)
- Maven 3.6+
- MySQL 8.0 Server

### 1. Clone the Repository

```bash
git clone https://github.com/Swaraj1657/SkyLock.git
cd SkyLock
```

### 2. Create the Database

```sql
CREATE DATABASE skylock;
```

### 3. Create Storage Directories

SkyLock stores uploaded files on the local filesystem. Create the two required directories before starting the application:

```bash
# Windows (PowerShell)
New-Item -ItemType Directory -Path "C:\SkyLock_Cloud\temp" -Force

# Linux / macOS
mkdir -p /var/skylock/storage/temp
```

> **Note:** Update `file.storage.path` and `file.temp.path` in `application.properties` to match your chosen paths.

### 4. Configure the Application

Copy the template below into `src/main/resources/application.properties` and fill in your values:

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

# --- JWT (use a strong 32+ character secret) ---
app.jwt.secret=${JWT_SECRET:ReplaceThisWithASecureRandomSecret!!!}

# --- File Storage ---
file.storage.path=/your/storage/path/
file.temp.path=/your/storage/path/temp

# --- Upload Limits ---
spring.servlet.multipart.max-file-size=1000MB
spring.servlet.multipart.max-request-size=1000MB

# --- Email (Brevo / any SMTP relay) ---
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=YOUR_SMTP_USERNAME
spring.mail.password=YOUR_SMTP_API_KEY
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 5. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

---

## ⚙️ Configuration Reference

| Property | Description | Default |
|---|---|---|
| `spring.datasource.url` | MySQL connection URL | `jdbc:mysql://127.0.0.1:3306/skylock` |
| `app.jwt.secret` | JWT signing secret (min. 32 chars) | Set via `JWT_SECRET` env var |
| `file.storage.path` | Root directory for stored files | *(required)* |
| `file.temp.path` | Temp directory for upload chunks | *(required)* |
| `spring.servlet.multipart.max-file-size` | Maximum individual file size | `1000MB` |
| `spring.servlet.multipart.max-request-size` | Maximum total request size | `1000MB` |
| `spring.jpa.hibernate.ddl-auto` | Schema strategy (`update` / `validate`) | `update` |

---

## 📊 Performance Notes

- **Upload throughput** is bounded by client bandwidth and local disk I/O. The `FileChannel` merge step typically completes in under a second even for multi-GB files on modern hardware.
- **Memory footprint during uploads** remains minimal regardless of file size — data is streamed directly to disk rather than buffered in the JVM heap.
- **Concurrent uploads** are isolated per session, meaning multiple users uploading simultaneously do not interfere with each other's temp directories.

---

## 🔒 Security Considerations

- **Never commit `application.properties`** with real credentials. Use environment variables (e.g., `${JWT_SECRET}`) or a secrets manager for sensitive values.
- **JWT secrets** should be randomly generated strings of at least 64 characters for production deployments.
- **Database credentials** should use a restricted MySQL user with `SELECT`, `INSERT`, `UPDATE`, `DELETE` privileges only — not `root`.
- **Storage paths** should be outside the web root and inaccessible via direct HTTP requests.
- **HTTPS** is strongly recommended in production. Cloudflare's proxy (as used at `skylock.dpdns.org`) provides this automatically.

---

## 🗺️ Roadmap

- [ ] Resumable upload recovery (persist chunk state across sessions)
- [ ] Public shareable links with optional expiry and password protection
- [ ] Admin dashboard for user and quota management
- [ ] Object storage backend (S3-compatible) as an alternative to local filesystem
- [ ] Two-factor authentication (TOTP)
- [ ] Activity audit log per user

---

## 👤 Author

**Swaraj**
- GitHub: [@Swaraj1657](https://github.com/Swaraj1657)
- Live: [skylock.dpdns.org](https://skylock.dpdns.org)

---

*Built with Spring Boot, secured with JWT, deployed on Cloudflare.*
