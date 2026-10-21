# Day 5: Queue management

## Runtime configuration

The application keeps JWT validation enabled. Configure a strong Base64 key (at least 32 decoded bytes) and database credentials before starting the app. In PowerShell, generate a development key with:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:DB_USERNAME = '<your MySQL username>'
$env:DB_PASSWORD = '<your MySQL password>'
.\mvnw.cmd spring-boot:run
```

The JWT key is required on every start. Set `DB_URL` too if MySQL is not at the configured local URL.

## Database and transitions

Hibernate creates/updates `queue_entries` through the project's existing `ddl-auto=update` setting. Each row references one appointment and one doctor. `appointment_id` is unique, and `(doctor_id, queue_date, token_number)` is unique. The doctor reference on the queue row is intentionally duplicated from the appointment so MySQL can enforce the per-doctor/day token uniqueness rule.

Tokens restart at `A001` for each doctor each date. The appointment row lock prevents a duplicate check-in; a doctor row lock serializes token allocation for that doctor's queue across server instances. The unique indexes are the final database guard. A check-in only succeeds for a `BOOKED` appointment scheduled for today's server local date. It moves the appointment to `CHECKED_IN`; start moves both records to `IN_CONSULTATION`; complete moves both to `COMPLETED`. Appointments already checked in cannot use the existing booked-only cancellation path.

The queue position is one plus the number of `WAITING` entries ahead for that doctor and date, ordered by check-in timestamp and row ID. An entry in consultation returns `queuePosition: null`. A completed entry is no longer active, so `/my-status` returns `404` when the patient has no active entry today. Active queue results exclude completed and cancelled records.

## Doctor and patient identity mapping

Patients are matched to the authenticated account by normalized email, following the existing appointment ownership model. Doctors need a persisted `doctors.user_id` association to a `users.id` account before doctor-scoped APIs will work. Existing doctor records have no such association by default; an administrator must link the correct account during provisioning, for example:

```sql
UPDATE doctors d
JOIN users u ON u.email = 'doctor@example.com'
SET d.user_id = u.id
WHERE d.id = 12;
```

The check-in, queue viewing, and consultation APIs use roles already stored for the authenticated user. Public registration remains patient-only; receptionist, doctor, and admin accounts must be provisioned through the application's trusted administrative process.

## Endpoints

Send `Authorization: Bearer <accessToken>` on every endpoint below.

| Method and path | Roles | Result |
| --- | --- | --- |
| `POST /api/queue/check-in/{appointmentId}` | `RECEPTIONIST`, `ADMIN` | Creates a token; returns `201` |
| `GET /api/queue/current` | `DOCTOR`, `RECEPTIONIST`, `ADMIN` | Doctor sees their own queue; reception/admin see all active queues today |
| `GET /api/queue/my-status` | `PATIENT` | Returns only the authenticated patient's active entry for today |
| `PATCH /api/queue/{queueEntryId}/start` | Assigned `DOCTOR`, `ADMIN` | Starts consultation; returns `200` |
| `PATCH /api/queue/{queueEntryId}/complete` | Assigned `DOCTOR`, `ADMIN` | Completes consultation; returns `200` |

The check-in request has no body. Example success response:

```json
{
  "id": 7,
  "tokenNumber": "A001",
  "appointmentId": 24,
  "patientName": "Example Patient",
  "doctorId": 12,
  "doctorName": "Dr. Example",
  "departmentName": "Cardiology",
  "queueDate": "2026-10-19",
  "status": "WAITING",
  "queuePosition": 1,
  "checkedInAt": "2026-10-19T09:30:00",
  "consultationStartedAt": null,
  "consultationCompletedAt": null
}
```

Invalid state transitions, duplicate check-in, and token constraint conflicts return `409`. Missing appointments/queue entries return `404`; missing authentication returns `401`; role or resource ownership failures return `403`.

## Postman workflow

1. Sign in as a provisioned receptionist/admin with `POST /api/auth/login` and copy `accessToken`.
2. Create a `BOOKED` appointment whose date is today and note its ID.
3. Send `POST {{baseUrl}}/api/queue/check-in/{{appointmentId}}` with the bearer token; expect `201` and `WAITING`.
4. Send `GET {{baseUrl}}/api/queue/current` as receptionist/admin. A doctor token sees only their linked doctor's queue.
5. Sign in as the linked patient and send `GET {{baseUrl}}/api/queue/my-status`; expect their token and position. Other patients cannot be selected by an ID parameter.
6. Sign in as the assigned doctor and send `PATCH {{baseUrl}}/api/queue/{{queueEntryId}}/start`, then `PATCH {{baseUrl}}/api/queue/{{queueEntryId}}/complete`; expect the appointment and queue statuses to advance together.
7. Repeat check-in or try an invalid transition; expect `409`.
