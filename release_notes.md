# Zoron Changelog

## v4.5.12
- **Analytics**: Implemented advanced bucketing compression and added Math stats (Mean, Variance, StdDev) while strictly fixing viewport rendering bugs.
- **Documentation**: Implemented an all-new Wiki-style split-pane UX with Table of Contents auto-highlighting. Added specific facts for Gaming, Battery, and Video Modes.
- **What's New**: Implemented date parsing and explicit empty states for seamless changelog rendering.

## v4.5.11
- **Analytics Fix**: Refactored Vico chart async model loading to fix empty model crashes permanently and handle invalid rendering gracefully.
- **UI Redesign**: Transformed Settings changelog into a dedicated 'What's New' screen parsed dynamically from OTA metadata.
- **Documentation**: Overhauled 'Learn Hub' with 100% factual data retrieved from backend script reverse-engineering (Fastpath, Intent Engine, Rule Engine, etc.), adding a new Table of Contents for rapid navigation.


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

