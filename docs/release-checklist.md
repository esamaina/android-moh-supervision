# SST Android Release Checklist

## Build And Signing
- Ensure Gradle sync succeeds.
- Set signing environment variables:
  - `SST_KEYSTORE_PATH`
  - `SST_KEYSTORE_PASSWORD`
  - `SST_KEY_ALIAS`
  - `SST_KEY_PASSWORD`
- Run:
  - `./gradlew clean assembleRelease`
- Confirm output APK is generated with `SST` naming.

## Offline Functionality Validation
- Create supervision records offline.
- Edit saved records and action plans offline.
- Mark records complete/incomplete.
- Validate records list filtering and status toggle.

## Sync Validation
- Trigger manual sync and verify successful push.
- Verify failed sync items move to `failed` and can retry.
- Verify periodic sync worker triggers when network is available.

## Security Validation
- Confirm credentials are stored via secure storage.
- Confirm logout clears session locally.

## Regression Validation
- Landing -> Login -> Menu navigation works.
- Supervision form, records, action plan screens load and save correctly.
- Reports, dashboard, and user management native modules load.
- Web fallback links open correctly for transition paths.
