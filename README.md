
# SDAT S4 Sprint API - Backend Documentation

Welcome to the backend API for the SDAT S4 Sprint Project.  
This is a RESTful Java Spring Boot API hosted on **AWS EC2**, with images stored in **Amazon ECR**, and IAM roles handling secure access.  

Live API (EC2 public IP or domain):  
```

http\://3.139.58.109:8080/api

````

---

## 🚀 AWS Deployment Architecture

- **Spring Boot Backend** is built into a Docker image.
- **Amazon ECR (Elastic Container Registry):** stores the built Docker image (`airportsim-backend`).
- **GitHub Actions CI/CD:** on each push, the workflow builds the image and pushes to ECR.
- **Amazon EC2 (Elastic Compute Cloud):** runs the container using `docker run` or `docker-compose`.
- **IAM Role/Policy:** grants EC2 permission to pull images from ECR securely.
- **MySQL Database:** hosted on AWS RDS or locally inside EC2 (configurable via environment variables).

**Flow:**
1. Push to GitHub → GitHub Actions builds Docker image.
2. Push image to **ECR**.
3. EC2 pulls the new image and runs container.
4. API is exposed on EC2 at `:8080/api/...`.

---

## 🛠 Core Entities

* **City**: `id`, `name`, `province`, `population`
* **Airport**: `id`, `name`, `portId`, `city`
* **Passenger**: `id`, `firstName`, `lastName`, `phoneNumber`, `city`
* **Aircraft**: `id`, `type`, `airlineName`, `numOfPassengers`
* **Flight**: `id`, `airline`, `flightNumber`, `departureAirport`, `arrivalAirport`, `scheduledDeparture`, `scheduledArrival`, `status`, `distanceKm`, `durationMinutes`
* **Booking**: `id`, `bookingRef`, `status`, `seatNumber`, `fareClass`, `flight`, `passenger`

---

## 📡 Endpoints

### Cities

| Method | Endpoint       | Description             |
|--------|----------------|-------------------------|
| GET    | `/cities`      | List all cities         |
| GET    | `/cities/{id}` | Get a city by ID        |
| POST   | `/cities`      | Create a city           |
| PUT    | `/cities/{id}` | Replace a city          |
| PATCH  | `/cities/{id}` | Partially update a city |
| DELETE | `/cities/{id}` | Delete a city           |

---

### Airports

| Method | Endpoint                    | Description                 |
|--------|-----------------------------|-----------------------------|
| GET    | `/airports`                 | List all airports           |
| GET    | `/airports/{id}`            | Get an airport by ID        |
| POST   | `/airports?cityId={cityId}` | Create airport for a city   |
| PUT    | `/airports/{id}`            | Replace an airport          |
| PATCH  | `/airports/{id}`            | Partially update an airport |
| DELETE | `/airports/{id}`            | Delete an airport           |

---

### Passengers

| Method | Endpoint                                          | Description                            |
|--------|---------------------------------------------------|----------------------------------------|
| GET    | `/passengers`                                     | List all passengers                    |
| GET    | `/passengers/{id}`                                | Get a passenger by ID                  |
| GET    | `/passengers/{id}/aircraft`                       | List aircraft a passenger has flown on |
| POST   | `/passengers?cityId={cityId}`                     | Create passenger in a city             |
| PUT    | `/passengers/{id}`                                | Replace a passenger                    |
| PATCH  | `/passengers/{id}`                                | Partially update a passenger           |
| DELETE | `/passengers/{id}`                                | Delete a passenger                     |
| PUT    | `/passengers/{passengerId}/aircraft/{aircraftId}` | Assign aircraft to passenger           |

---

### Aircraft

| Method | Endpoint         | Description                  |
|--------|------------------|------------------------------|
| GET    | `/aircraft`      | List all aircraft            |
| GET    | `/aircraft/{id}` | Get an aircraft by ID        |
| POST   | `/aircraft`      | Create an aircraft           |
| PUT    | `/aircraft/{id}` | Replace an aircraft          |
| PATCH  | `/aircraft/{id}` | Partially update an aircraft |
| DELETE | `/aircraft/{id}` | Delete an aircraft           |

---

### Flights

| Method | Endpoint                       | Description                            |
|--------|--------------------------------|----------------------------------------|
| GET    | `/flights`                     | List flights (default 200 limit)       |
| GET    | `/flights/page?page=0&size=50` | Paginated flights                      |
| GET    | `/flights/count`               | Count flights                          |
| GET    | `/flights/{id}/departure-airport` | Get flight’s departure airport       |
| GET    | `/flights/{id}/arrival-airport`   | Get flight’s arrival airport         |

---

### Bookings (Passenger ↔ Flight)

| Method | Endpoint                                | Description                                    |
|--------|-----------------------------------------|------------------------------------------------|
| POST   | `/bookings`                             | Create booking (link passenger to flight)      |
| GET    | `/bookings/{id}`                        | Get booking by ID                              |
| GET    | `/bookings/by-passenger/{passengerId}`  | Raw list of bookings for a passenger           |
| GET    | `/bookings/by-flight/{flightId}`        | Raw list of bookings for a flight              |
| PATCH  | `/bookings/{id}/status`                 | Update booking status (CONFIRMED, CANCELLED…)  |
| DELETE | `/bookings/{id}`                        | Cancel booking (sets status = CANCELLED)       |
| GET    | `/bookings/by-flight/{id}/manifest`     | Passenger manifest for a flight (with seats, times, airport names) |
| GET    | `/bookings/by-passenger/{id}/itinerary` | Passenger itinerary (flights with times, airports, status) |

---

## 🧑‍💻 Example Usage

**Create a booking:**
```http
POST /api/bookings
Content-Type: application/json

{
  "flightId": 1,
  "passengerId": 2,
  "seatNumber": "12A",
  "fareClass": "ECONOMY"
}
````

**Flight manifest:**

```
GET /api/bookings/by-flight/1/manifest
```

**Passenger itinerary:**

```
GET /api/bookings/by-passenger/2/itinerary
```

---

## 🔒 IAM & Security

* **IAM Role:** EC2 instance role allows pulling private images from ECR without static credentials.
* **ECR Repository Policy:** grants push/pull to CI/CD GitHub Action role and pull to EC2 instance role.
* **Secrets:** Database credentials managed via **AWS Secrets Manager** or environment variables.
* **CORS:** configured in `application.properties` to allow trusted frontend origins.

---

## ✅ Notes

* All responses are JSON.
* `PATCH` and `PUT` require the `id` in the path.
* `POST` is only for **create**, not update.
* Hibernate auto-creates tables (`spring.jpa.hibernate.ddl-auto=update`).

---

Built with Java 17, Spring Boot, MySQL
Deployed on AWS (EC2 + ECR + IAM + Docker)



