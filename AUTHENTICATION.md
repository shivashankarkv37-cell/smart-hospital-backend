# JWT authentication

## Local configuration

Copy the root `.env.example` to `.env` and replace its placeholders with your local values. The `.env` file is ignored by Git. Spring Boot does **not** load `.env` automatically; import its entries into the process environment before launching, or enter them in your IDE run configuration. Do not use `.env.example` values as real credentials.

In PowerShell, load a plain `NAME=value` `.env` file into the current shell and launch the backend:

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
    $name, $value = $_ -split '=', 2
    Set-Item -Path "Env:$($name.Trim())" -Value $value.Trim()
}
.\mvnw.cmd spring-boot:run
```

The application uses these variables:

```text
DB_URL=jdbc:mysql://localhost:3306/smart_hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
JWT_SECRET=<base64-encoded key containing at least 32 random bytes>
JWT_ACCESS_TOKEN_EXPIRATION_SECONDS=900
FRONTEND_ORIGIN=http://localhost:5173
```

`DB_URL` defaults to the local MySQL URL shown above; `DB_USERNAME` and `DB_PASSWORD` are required. Set the variables in IntelliJ under **Run | Edit Configurations | Environment variables** for the Spring Boot run configuration. IntelliJ does not import `.env` automatically.

Create a signing key locally with OpenSSL, or with PowerShell using a cryptographic random generator:

```bash
openssl rand -base64 32
```

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Keep the generated value in your process environment or ignored `.env` file. Do not commit it or put it in frontend configuration. The app uses JJWT and a Base64 HMAC signing key; it fails at startup if `JWT_SECRET` is missing, invalid Base64, or decodes to fewer than 32 bytes, the HS256 minimum. Access tokens expire after 900 seconds by default. Configure another lifetime between 1 and 3600 seconds with `JWT_ACCESS_TOKEN_EXPIRATION_SECONDS` when needed.

Access-token lifetime must be between 1 and 3600 seconds. The signing key shown in tests is test-only. Production and developer environments must use their own secret.

## Login and registration

Register a patient user:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "Rahul Sharma",
  "email": "patient@example.com",
  "phone": "+919876543210",
  "password": "PatientPassword123"
}
```

The public registration service always assigns `PATIENT`. A `role` value other than `PATIENT` fails validation; clients cannot self-assign privileged roles. The existing app stores login accounts in `users` and patient profiles separately in `patients`, so a patient who will book appointments must also create a matching patient profile through `POST /api/patients` using the same email. These registration operations use separate password hashes; login uses the `/api/auth/register` account password.

Log in:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "patient@example.com",
  "password": "PatientPassword123"
}
```

Successful response:

```json
{
  "accessToken": "<signed JWT>",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": 1,
    "email": "patient@example.com",
    "role": "PATIENT"
  }
}
```

`GET /api/auth/me` returns the safe profile for the token subject. It takes no user ID from the client.

## Calling protected APIs from Postman or Axios

Copy `accessToken` from the login response and send it on protected requests:

```http
Authorization: Bearer <accessToken>
```

Postman: select **Bearer Token** in the Authorization tab and paste the token. Axios can set the same header in a request interceptor. The API uses header bearer tokens, not authentication cookies; browsers do not automatically attach this header cross-site, so CSRF protection is disabled for this stateless API. Reassess CSRF before moving tokens into cookies.

## Current authorization policy

- Public: health, login, user registration, patient registration, doctor listing, and department listing.
- `PATIENT`: appointment booking and own-appointment retrieval/cancellation, with owner checks in the appointment service.
- `ADMIN`: department creation and appointment cancellation.
- `ADMIN`: department creation.
- `DOCTOR` and `RECEPTIONIST`: roles are supported in the user model, but no role-specific endpoints exist yet.

There is no public privileged-user provisioning route. Assign `ADMIN`, `DOCTOR`, or `RECEPTIONIST` only through a trusted administrative provisioning process. Existing account records with the configured legacy Spring Security PBKDF2 format remain usable; after a successful login, their hash is upgraded to BCrypt. New account and patient-profile registrations use the same injected BCrypt encoder. No plaintext-password migration is performed. If legacy records may contain plaintext, audit them in a controlled database process and require resets for affected users; never print values or hash existing hashes blindly.
