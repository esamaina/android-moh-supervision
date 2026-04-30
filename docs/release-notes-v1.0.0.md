# SST Android Release Notes - v1.0.0

## Release Date

2026-04-30

## Highlights

- Delivered full offline-first supervision workflow on Android from login to sync.
- Added schema-aware questionnaire updates to keep mobile aligned with backend changes.
- Implemented conflict-safe sync queue handling with retry and recovery controls.
- Added native support for capturing and syncing all questionnaire pillars across levels and respondents.
- Improved sync observability, dashboard/reporting coverage, and admin user-management flows.

## Core Capabilities Included

- Authentication and session handling for field use.
- Multi-section native supervision form (Location, Respondent, Assessment).
- Native action plan editing linked to supervision records.
- Supervision records list with filter, status toggle, delete, sync selected/all.
- Offline Room persistence for drafts, queue, and secure session state.
- Background sync with WorkManager and explicit conflict policy handling.
- Device-aware sync identity and sync-state tracking support.
- Questionnaire schema version check and coverage guard before submit.
- Dynamic pillar question upsert UI plus full raw all-pillar payload sync.

## Backend/Mobile Integration Delivered

- Mobile sync push/pull endpoints with role-aware access control.
- Device sync state endpoint and per-device timestamp tracking.
- Mobile questionnaire schema endpoint for update detection.
- Mobile user-management APIs for admin operations.

## Quality and Delivery

- Unit tests for analytics, mapping, payload normalization, and conflict policy logic.
- Instrumentation tests for landing/login/menu/records/form and schema guard behavior.
- CI workflows for Android build/test and release pipeline readiness.

## Operational Notes

- Release tag: `v1.0.0`
- Android repository branch: `main`
- Backend support branch: `uat`
- Build verified in Android Studio environment.

## Recommended Post-Release Smoke Checks

- Login -> create supervision draft -> add pillar question -> save -> reopen.
- Submit with valid schema coverage and verify sync success.
- Force schema coverage gap and verify submit guard blocks with clear message.
- Sync selected/all from records and verify queue health updates.
- Validate reports/dashboard summaries with local offline records.
