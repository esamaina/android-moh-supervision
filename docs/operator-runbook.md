# SST Android Operator Runbook

## Objective
Provide field officers with offline-first end-to-end supervision capability and delayed sync when network is restored.

## Core Workflow
1. Login and verify base URL.
2. Capture supervision records offline.
3. Add/update action plans and due dates.
4. Manage supervision record status (complete/incomplete).
5. Sync selected or all records when connectivity is available.

## Troubleshooting

### Login issues
- Verify base URL is reachable.
- Confirm username/password are valid backend credentials.

### Sync failures
- Open Sync Center and check health counters.
- Retry `Sync Selected` from Supervision Records.
- Ensure internet connectivity is active.

### Missing records
- Check local filters in Supervision Records (`all/completed/incomplete`).
- Use refresh action in records/history screen.

## Data Safety Notes
- Records are saved locally before sync.
- Credentials are held in secure local storage.
- Logout clears local session state.

## Escalation Data To Capture
- Device ID used
- Time of failed operation
- Record ID (first 8 chars visible in list)
- Screenshot of sync status and error text
