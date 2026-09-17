# MedLocal

A hyperlocal pharmacy marketplace web app.

## Tech Stack

- Backend: Java 21 + Spring Boot
- Database: PostgreSQL with PostGIS extension
- Caching: Redis
- Frontend: React (Create React App) + Tailwind
- Auth: Spring Security + JWT
- Real-time: WebSocket or SSE
- AI: Claude API
- Payments: Razorpay/Stripe (test mode)
- Containerization: Docker + docker-compose

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 21
- Node.js (for frontend)

### Backend

1. Start the database and cache:
   ```
   docker-compose up -d
   ```

2. Build and run the backend:
   ```
   cd backend
   mvn spring-boot:run
   ```

### Frontend

1. Install dependencies:
   ```
   cd frontend
   npm install
   ```

2. Start the development server:
   ```
   npm start
   ```

## API Documentation

(To be added)

## Features

(To be added)