# Day 6 dashboards

All protected calls use `Authorization: Bearer <accessToken>` from `POST /api/auth/login`.

## Receptionist workflow

Search by a partial name, email, phone, or numeric patient ID:

```http
GET {{baseUrl}}/api/receptionist/patients/search?query=rahul&page=0&size=20
```

Read one safe patient summary:

```http
GET {{baseUrl}}/api/receptionist/patients/12
```

Book for an existing patient using the existing appointment request fields:

```http
POST {{baseUrl}}/api/receptionist/appointments
Content-Type: application/json

{
  "patientId": 12,
  "doctorId": 5,
  "appointmentDate": "2026-10-25",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "reason": "General consultation"
}
```

Check in through the existing queue endpoint, which returns the generated token:

```http
POST {{baseUrl}}/api/queue/check-in/42
```

View the active queue through the existing endpoint:

```http
GET {{baseUrl}}/api/queue/current
```

Receptionist routes accept `RECEPTIONIST` and `ADMIN`. Check-in and current queue reuse the same role rules on the original queue API.

## Admin workflow

Departments:

```http
GET {{baseUrl}}/api/admin/departments
GET {{baseUrl}}/api/admin/departments/2
POST {{baseUrl}}/api/admin/departments
Content-Type: application/json

{"name":"Neurology","description":"Nervous system care"}
```

```http
PUT {{baseUrl}}/api/admin/departments/2
Content-Type: application/json

{"name":"Neurology","description":"Updated department description"}
```

Doctors:

```http
GET {{baseUrl}}/api/admin/doctors
GET {{baseUrl}}/api/admin/doctors/5
POST {{baseUrl}}/api/admin/doctors
Content-Type: application/json

{"name":"Dr. Example","specialization":"Cardiology","departmentId":2,"userId":31}
```

`userId` is optional. If supplied, it must refer to an existing `DOCTOR` account not already linked to a doctor profile. No password is accepted or changed here.

```http
PUT {{baseUrl}}/api/admin/doctors/5
Content-Type: application/json

{"name":"Dr. Example","specialization":"Interventional Cardiology","departmentId":2}
```

Doctor activation is not provided because the current Doctor model has no activation field. Appointments can be searched and paged:

```http
GET {{baseUrl}}/api/admin/appointments?date=2026-10-25&doctorId=5&status=BOOKED&page=0&size=20
GET {{baseUrl}}/api/admin/appointments/42
```

All `/api/admin/**` routes require `ADMIN`. Admin appointment responses include a patient ID, display name, and email plus doctor, time, status, and reason. Patient search responses omit the password hash.

## Status codes

- `201 Created`: receptionist appointment or admin record created.
- `200 OK`: read or update succeeded.
- `400 Bad Request`: invalid or missing fields/parameters.
- `401 Unauthorized`: missing or invalid JWT.
- `403 Forbidden`: valid JWT without the required role.
- `404 Not Found`: patient, doctor, department, or appointment does not exist.
- `409 Conflict`: appointment time overlap or duplicate department/account association.

## Database and startup

JPA `ddl-auto=update` adds the nullable `appointments.created_by_user_id` field for audit attribution. Existing records remain valid. This implementation adds no doctor activation column and no deletion behavior.

Before launching, provide `JWT_SECRET` as Base64 for at least 32 decoded bytes, plus `DB_USERNAME` and `DB_PASSWORD`. Never put their actual values in source control. Existing public `GET /api/doctors` and `GET /api/departments` and existing queue routes are preserved.
