# Android MOH Supervision

Android client starter for the CHW supervision backend sync APIs.

## What is included

- Kotlin Android app scaffold
- Retrofit client for:
  - `POST /api/mobile/sync`
  - `GET /api/mobile/sync`
  - `GET /api/mobile/sync/state`
- Simple UI to test push, pull, and sync-state calls

## Location

This app lives in a separate folder/repo:

- `android-moh-supervision/`

It is intentionally separate from the backend source.

## Open in Android Studio

1. Open the `android-moh-supervision` folder in Android Studio.
2. Let Gradle sync.
3. Run on emulator/device.

## Backend connection notes

- For local emulator to local backend:
  - use `http://10.0.2.2:3000`
- For physical device:
  - use your machine LAN IP and ensure firewall/network access.

## Authentication

The app uses Basic Auth against backend credentials (`username/password`).

## Next implementation step

Replace the sample generated push payload in `SyncRepository` with locally stored offline records from Room/SQLite queue.
