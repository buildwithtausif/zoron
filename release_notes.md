# Zoron Changelog

## v4.5.10
- **Analytics Fix**: Resolved a crash in the Analytics tab caused by the charting library rejecting identical timestamps when processing battery telemetry.


## v4.5.9
- **UI Modernization**: Redesigned 'About Modes' into a full documentation experience with comprehensive details for 10 missing mode sections.
- **What's New Fix**: Resolved 'What's New' page showing empty placeholders by fetching and displaying live OTA metadata history.
- **Navigation**: Reverted floating pill navigation to a fixed bottom bar with a translucent frosted-glass effect.

## v4.5.4
- **Backend Optimizations**: Fixed engine validation logic to prevent false FAILs for zRAM and I/O schedulers. Added support for fallback schedulers (`mq-deadline`, `kyber`, `bfq`).
- **Engine FastPath**: Reduced redundant engine reapplication loops by utilizing FastPath for burst optimizations.
- **Compose Migration Complete**: Migrated the Rule Editor and OTA Updater from legacy views directly into Jetpack Compose.
- **Analytics Exporter**: Added data export capabilities, allowing users to save CSV logs and process reports directly to the `Downloads/Zoron` directory.
- **Governor Tuning**: Added GPU Governor selection via the UI with detailed mode documentation outlining hardware impacts.

