# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CreatorLink is a B2B SaaS platform for tracking affiliate/influencer marketing campaign performance through unique tracking links. Advertisers can monitor creator/influencer performance by tracking clicks across campaigns.

## Repository Structure

```
creatorlink/
├── BE/                    # Spring Boot backend (Java 17)
└── FE/                    # React frontend (Vite)
```

## Build & Run Commands

### Backend (BE/)
```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Run single test class
./gradlew test --tests "com.jung.creatorlink.SomeTestClass"

# Run single test method
./gradlew test --tests "com.jung.creatorlink.SomeTestClass.someMethod"
```

### Frontend (FE/)
```bash
# Install dependencies
npm install

# Development server (opens browser automatically)
npm run dev

# Build for production
npm run build

# Lint
npm run lint

# Preview production build
npm run preview
```

## Architecture

### Backend Architecture

**Package Structure**: `com.jung.creatorlink`
```
├── config/           # Security (CORS, auth), WebConfig (file uploads)
├── controller/       # REST endpoints (organized by domain)
├── service/          # Business logic (@Transactional)
├── repository/       # Spring Data JPA repositories
├── domain/           # JPA entities with rich methods
├── dto/              # Request/Response objects
└── common/exception/ # GlobalExceptionHandler, custom exceptions
```

**Core Domain Entities**:
- **User**: Advertiser account
- **Campaign**: Marketing campaign with landing URL, date range, state (UPCOMING/RUNNING/ENDED)
- **Creator**: Influencer/creator linked to an advertiser
- **Channel**: Social media platform (INSTAGRAM, YOUTUBE, BLOG) with placement type
- **TrackingLink**: Unique slug-based tracking URL (/t/{slug})
- **ClickLog**: Click event with timestamp, IP, userAgent, referer

**Key Patterns**:
- Soft delete via `Status` enum (ACTIVE/INACTIVE) - use `deactivate()` method
- All queries should filter by `status = ACTIVE` unless explicitly including inactive
- Transactions on service layer, read-only for queries
- Slug generation: 8-char alphanumeric with retry on collision

### Frontend Architecture

```
FE/src/
├── api/              # Axios API modules (campaigns.js, creators.js, etc.)
├── contexts/         # React Context (AuthContext)
├── components/       # Reusable components (Layout, ProtectedRoute)
└── pages/            # Route page components
```

**Stack**: React 19, React Router 7, Tailwind CSS 4, Recharts, Axios

**Vite Proxy**: `/api`, `/t`, `/uploads` → `http://localhost:8080`

## Database

- MySQL 8 on `localhost:3306/creatorlink`
- JPA with `ddl-auto=update` (schema auto-managed)
- File uploads stored locally at configured `app.upload.dir`

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Key API Endpoints

```
POST/GET/PUT/DELETE  /api/campaigns
POST/GET/DELETE      /api/tracking-links
POST/GET/PUT/DELETE  /api/creators
POST/GET/PUT/DELETE  /api/channels
GET                  /api/stats/*
GET                  /t/{slug}           # Public redirect (records click)
POST                 /api/upload/image   # Image upload
```

## Development Notes

- **Authentication**: Currently all routes permit all requests (JWT planned). Include `advertiserId` in requests.
- **Error Handling**: `ResourceNotFoundException` → 404, `IllegalArgumentException` → 400, `IllegalStateException` → 409
- **Naming Convention**: Entity (singular) → Repository/Service/Controller (Entity + suffix)
- **Timestamps**: `createdAt`/`updatedAt` set in service layer
