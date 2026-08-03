# Cuckoo

A personal daily-schedule alarm app for Android. Cuckoo fires a loud alarm-style
notification for each task in your day (study blocks, chores, whatever you define),
and lets you act on it — Start, Done, or Skip — right from the notification, no need
to open the app.

Built for a fixed personal routine: the tasks themselves are permanent (edit the code
to change what they are), but each task's time can be moved right from the app.

## Download

Prebuilt APKs are published automatically on every push to `main`.

**[Go to Releases](https://github.com/engineermarcus/cuckoo/releases)** → download
the latest `app-debug.apk` → install on your phone (allow install from unknown sources).

On first launch: grant notification permission, and grant "exact alarm" permission
if prompted.

## Features

- Daily recurring alarms with sound + high-priority notification, one per task
- **Start / Done / Skip** actions right on the notification — no need to open the app
- Task completion history logged per day, per task
- Auto re-schedules itself for the next day after each alarm fires
- Survives phone reboots (schedule reloads automatically)
- Editable time per task via tap-to-pick time; task list itself is fixed in code

## Editing schedule

- Tap an item to change its time.
- Tap the switch to enable/disable a task.
- Task names and the set of tasks are fixed in code (see `ScheduleRepository.kt`) —
  this is intentional, so the routine can't accidentally be edited away mid-day.

Default schedule is preloaded: Math, Chores, C++, Kotlin, Electronics,
Electromagnetism, Review / buffer.

## Setup (for forking/building your own copy)

1. Create a new GitHub repo, push this project (root = repo root).
2. Go to the Actions tab, run "Build Cuckoo APK" (or just push to `main`).
3. Check the "Releases" section of your repo — the APK is attached there.
4. Download `app-debug.apk`, install on your phone.

## Updating the app (no conflicts)

- Every build signs with the same committed `app/debug.keystore` — same signature
  every time, so new APKs install as an **update**, not a conflicting new app.
- `versionCode` auto-increments from the GitHub Actions run number, so Android
  always sees the new APK as newer.
- Just install the newest release APK over the old one directly — no need to
  uninstall, your saved schedule and history stay intact.
- Only uninstall manually if you ever change the `applicationId` or regenerate
  `debug.keystore`.
