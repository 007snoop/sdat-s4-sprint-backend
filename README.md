# SDAT S4 Sprint API - Backend Documentation

Welcome to the backend API for the SDAT S4 Sprint Project. This is a RESTful Java Spring Boot API hosted live at:

```
https://sdat-s4-sprint-backend.onrender.com/
```

---

## Core Entities

* **City**: `id`, `name`, `province`, `population`
* **Airport**: `id`, `name`, `code`, `city`
* **Passenger**: `id`, `firstName`, `lastName`, `phoneNumber`, `city`
* **Aircraft**: `id`, `type`, `airlineName`, `numberOfPassengers`

---

## Active Endpoints

> All endpoints are live and return JSON

### Cities

| Method | Endpoint       | Description             |
|--------|----------------|-------------------------|
| GET    | `/cities`      | List all cities         |
| GET    | `/cities/{id}` | Get a city by ID        |
| POST   | `/cities`      | Create a city           |
| PUT    | `/cities/{id}` | Replace a city          |
| PATCH  | `/cities/{id}` | Partially update a city |
| DELETE | `/cities/{id}` | Delete a city           |

**POST example:**

```json
{
  "name": "St. John's",
  "province": "NL",
  "population": 100000
}
```

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

**POST example:**

```
POST /airports?cityId=1
```

```json
{
  "name": "YYT Airport",
  "code": "YYT"
}
```

---

### Passengers

| Method | Endpoint                      | Description                  |
|--------|-------------------------------|------------------------------|
| GET    | `/passengers`                 | List all passengers          |
| GET    | `/passengers/{id}`            | Get a passenger by ID        |
| POST   | `/passengers?cityId={cityId}` | Create passenger in a city   |
| PUT    | `/passengers/{id}`            | Replace a passenger          |
| PATCH  | `/passengers/{id}`            | Partially update a passenger |
| DELETE | `/passengers/{id}`            | Delete a passenger           |

**POST example:**

```
POST /passengers?cityId=2
```

```json
{
  "firstName": "Colin",
  "lastName": "Smith",
  "phoneNumber": "7091234567"
}
```

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

**POST example:**

```json
{
  "type": "Boeing 737",
  "airlineName": "Air Canada",
  "numberOfPassengers": 180
}
```

---

## Relationships (Coming Soon)

| Relationship              | Planned Endpoint Example                |
|---------------------------|-----------------------------------------|
| Add aircraft to passenger | `POST /passengers/{pid}/aircraft/{aid}` |
| Add airport to aircraft   | `POST /aircraft/{aid}/airports/{apid}`  |

Let me know if you need these wired up.

---

## Reminder

All updates (PATCH/PUT) require the `id` in the URL.
Avoid using `POST` to update — it will create duplicates.

---

```
Built by Colin | Java, Spring Boot, MySQL | Deployed on Render
```
