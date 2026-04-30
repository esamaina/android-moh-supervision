# Android MOH Supervision (SST)

Offline-first Android client for CHW supervision with native workflows and sync recovery.

## What is included

- Kenyan-themed SST app shell (landing, login, role-aware menu)
- Native supervision workflow (offline capture, edit, status updates)
- Native action plan workflow linked to records
- Native supervision records list with filters and status toggles
- Sync center (push/pull/state + sync health)
- Background sync scheduler (WorkManager)
- Secure session persistence
- Native module placeholders with web fallback for transition:
  - reports
  - dashboard
  - user management

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

- Current transport auth is backend Basic Auth.
- Credentials/session are persisted using secure local storage path.

## Build

```bash
./gradlew assembleDebug
```

For release signing, set:

- `SST_KEYSTORE_PATH`
- `SST_KEYSTORE_PASSWORD`
- `SST_KEY_ALIAS`
- `SST_KEY_PASSWORD`

Then:

```bash
./gradlew clean assembleRelease
```

## Operations Docs

- `docs/release-checklist.md`
- `docs/operator-runbook.md`
