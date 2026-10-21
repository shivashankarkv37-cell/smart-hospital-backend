# CareBridge hospital FAQ assistant

## Current implementation

`POST /api/assistant/chat` uses a deterministic rule-based FAQ. There is no LLM provider configured or called, and replies include `mode: "RULE_BASED"`. This keeps the endpoint usable without model credentials, network access, or an installed Ollama model. The assistant has no repository, database, appointment, or queue service dependencies and cannot perform actions.

The assistant is enabled by default. Set `ASSISTANT_ENABLED=false` to disable it; requests then return HTTP 503. No API key, model name, or timeout is needed in rule-based mode.

## API

```http
POST /api/assistant/chat
Content-Type: application/json
```

```json
{"message":"How can I book an appointment?"}
```

```json
{
  "reply":"Sign in as a patient, browse the doctor directory, and submit the appointment form. The booking API is POST /api/appointments and checks ownership, time validity, and schedule conflicts. This assistant does not book or confirm appointments.",
  "mode":"RULE_BASED"
}
```

Messages must contain 1 to 1000 non-whitespace characters. Invalid requests use the existing API validation error format (400). A disabled assistant returns the existing API error format (503). The general FAQ route is public, like the existing public directory routes. JWT authentication and role checks on appointment, queue, and administrative APIs remain in force.

FAQ rules cover patient registration, login, appointment booking and cancellation, doctor and department directories, queue tokens, application navigation, and medical advice limitations. Unsupported or hospital-policy questions receive a response that directs the user to hospital staff; the assistant does not invent hours, fees, availability, or policies.

## Medical and privacy limits

The assistant does not diagnose, prescribe, or recommend individual treatment. Medical advice requests are redirected to a qualified healthcare professional. Possible emergencies are directed to local emergency services or the nearest emergency department. The assistant never receives patient records, credentials, or appointment details and never claims it completed a booking or cancellation.

This is not a substitute for clinical advice. Keyword rules are intentionally limited and may not recognize every phrasing. General FAQ mode has no rate limiter in this backend; add request rate limiting at the gateway or application edge before exposing it to untrusted high-volume traffic.

## Configuration and running

Spring Boot reads `ASSISTANT_ENABLED` from the process environment. The local `.env` file is not loaded automatically; use the documented PowerShell importer in [AUTHENTICATION.md](AUTHENTICATION.md), or enter the variable in the IntelliJ run configuration. The rule-based implementation does not need Ollama or hosted-provider setup. Spring AI has not been added, so there is no LLM mode to configure or availability claim to make.

Run the backend with Java 21 and the project wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Run the assistant and regression tests with:

```powershell
.\mvnw.cmd test
```

The backend still requires the configured MySQL credentials and a valid Base64 `JWT_SECRET` containing at least 32 random bytes, as documented in `AUTHENTICATION.md`.
