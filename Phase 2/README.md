# Booking System API

This repository contains the backend API documentation for a Booking System, consisting of User Service, Booking Service, and Wallet Service.

## User Service

### Create User
- **Endpoint:** `POST /users`
- **Request JSON payload:** `{"name": String, "email": String}`
- **Response:** 
  - HTTP 201 (Created) with JSON response `{"id": Integer, "name": String, "email": String}`
  - HTTP 400 (Bad Request) if user with the given email already exists.

### Get User Details
- **Endpoint:** `GET /users/{user_id}`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `{"id": Integer, "name": String, "email": String}`
  - HTTP 404 (Not Found) if user doesn't exist.

### Delete User
- **Endpoint:** `DELETE /users/{user_id}`
- **Response:** 
  - HTTP 200 (OK) on successful deletion
  - HTTP 404 (Not Found) if user doesn't exist.

### Delete All Users
- **Endpoint:** `DELETE /users`
- **Response:** HTTP 200 (OK)

## Booking Service

### Get Theatres
- **Endpoint:** `GET /theatres`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `[{ "id": Integer, "name": String, "location": String }]`
  - Empty list if no theatres are available.

### Get Shows by Theatre
- **Endpoint:** `GET /shows/theatres/{theatre_id}`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `[{ "id": Integer, "theatre_id": Integer, "title": String, "price": Integer, "seats_available": Integer }]`
  - HTTP 404 (Not Found) if theatre doesn't exist.

### Get Show Details
- **Endpoint:** `GET /shows/{show_id}`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `{"id": Integer, "theatre_id": Integer, "title": String, "price": Integer, "seats_available": Integer}`
  - HTTP 404 (Not Found) if show doesn't exist.

### Get User Bookings
- **Endpoint:** `GET /bookings/users/{user_id}`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `[{ "id": Integer, "show_id": Integer, "user_id": Integer, "seats_booked": Integer }]`
  - Empty list if user has no bookings.

### Create Booking
- **Endpoint:** `POST /bookings`
- **Request JSON payload:** `{"show_id": Integer, "user_id": Integer, "seats_booked": Integer}`
- **Response:** 
  - HTTP 200 (OK) on successful booking
  - HTTP 400 (Bad Request) for various error conditions.

### Delete User Bookings
- **Endpoint:** `DELETE /bookings/users/{user_id}`
- **Response:** 
  - HTTP 200 (OK) if user had bookings
  - HTTP 404 (Not Found) if user had no bookings.

### Delete User Show Bookings
- **Endpoint:** `DELETE /bookings/users/{user_id}/shows/{show_id}`
- **Response:** 
  - HTTP 200 (OK) if user had bookings for the specified show
  - HTTP 404 (Not Found) if user had no bookings for the show.

### Delete All Bookings
- **Endpoint:** `DELETE /bookings`
- **Response:** HTTP 200 (OK)

## Wallet Service

### Get Wallet Details
- **Endpoint:** `GET /wallets/{user_id}`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `{"user_id": Integer, "balance": Integer}`
  - HTTP 404 (Not Found) if user has no wallet.

### Debit/Credit Wallet
- **Endpoint:** `PUT /wallets/{user_id}`
- **Request JSON payload:** `{"action": "debit"/"credit", "amount": Integer}`
- **Response:** 
  - HTTP 200 (OK) with JSON payload `{"user_id": Integer, "balance": Integer}`
  - HTTP 400 (Bad Request) for insufficient balance during debit.

### Delete Wallet
- **Endpoint:** `DELETE /wallets/{user_id}`
- **Response:** 
  - HTTP 200 (OK) on successful deletion
  - HTTP 404 (Not Found) if user has no wallet.

### Delete All Wallets
- **Endpoint:** `DELETE /wallets`
- **Response:** HTTP 200 (OK)
