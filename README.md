# PHONEFIX

**PHONEFIX — By PaliaAPK HUB**  
**Scan • Diagnose • Fix**

A native Android phone diagnostics and guided repair assistant by **SHANPALIA**.

## Build

This project is configured for Codemagic signed Android release builds.

- Workflow: `phonefix-android`
- Output: signed release APK
- Codemagic Android keystore reference: `paliaapk-release`
- Application ID: `com.paliaapk.phonefix`

The keystore itself is intentionally **not** stored in this repository. Codemagic injects the configured signing identity at build time.
