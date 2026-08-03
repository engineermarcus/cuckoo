# Cuckoo

A personal daily-schedule alarm app for Android. Cuckoo fires a loud, alarm-style
notification for each task in a fixed daily routine, and lets you act on it —
**Start / Done / Skip** — right from the notification, no need to open the app.
Every action is logged so you can see later whether a task was actually done,
skipped, or missed entirely.

The task list itself is intentionally hard-coded. The only thing editable from
inside the app is *when* a task fires — not what the tasks are, not how many
there are. This is deliberate: it stops the routine from being edited away
mid-day. To add, rename, or remove a task, you edit the code and push.

## Download

Every push to `main` triggers `.github/workflows/build-apk.yml`, which builds
a signed debug APK and publishes it as a GitHub Release.

**[Releases →](https://github.com/engineermarcus/cuckoo/releases)**
Download `app-debug.apk`, install it (allow install from unknown sources on
first install). New releases install as an update over the old one — your
schedule and history are preserved, since every build is signed with the same
committed `app/debug.keystore` and `versionCode` auto-increments from the
Actions run number.

---

## Project layout

```
app/src/main/java/com/cuckoo/app/
├── ScheduleItem.kt               # data class: one task (id, label, hour, minute, enabled)
├── ScheduleRepository.kt         # the hard-coded task list lives here + SharedPrefs persistence
├── TaskEvent.kt                  # data class: one completion record (scheduleId, date, status)
├── EventRepository.kt            # reads/writes TaskEvent history to SharedPrefs
├── AlarmScheduler.kt             # schedules/cancels Android AlarmManager alarms per task
├── AlarmReceiver.kt              # fires when an alarm goes off: shows notification, plays sound, reschedules for tomorrow
├── NotificationHelper.kt         # builds the notification UI, incl. Start/Done/Skip action buttons
├── NotificationActionReceiver.kt # handles taps on Start/Done/Skip, writes to EventRepository
├── BootReceiver.kt               # re-registers all alarms after a phone reboot
├── MainActivity.kt               # the task list screen (time picker, enable/disable toggle)
└── ScheduleAdapter.kt            # RecyclerView adapter for the task list
```

---

## Changing the schedule (the only supported way)

Open `app/src/main/java/com/cuckoo/app/ScheduleRepository.kt`. The task list is
the `defaultItems()` function:

```kotlin
private fun defaultItems(): List<ScheduleItem> = listOf(
    ScheduleItem(1, "Math", 13, 0),
    ScheduleItem(2, "Chores", 14, 0),
    ScheduleItem(3, "C++", 16, 0),
    ScheduleItem(4, "Kotlin", 17, 45),
    ScheduleItem(5, "Electronics", 19, 30),
    ScheduleItem(6, "Electromagnetism", 21, 30),
    ScheduleItem(7, "Review / buffer", 23, 0)
)
```

`ScheduleItem(id, label, hour, minute, enabled = true)` — hour is 24h format.

- **Add a task**: append a new `ScheduleItem(...)` with a unique `id` (one higher
  than the current max).
- **Rename a task**: change the `label` string.
- **Remove a task**: delete its line.
- **Change default time**: change `hour`/`minute` — note this only affects
  *first install*; once installed, the app persists whatever time the user set
  via the in-app time picker, stored in SharedPreferences (`cuckoo_prefs` →
  `schedule_items`), and `defaultItems()` is never read again after that.

Commit, push to `main`, the Action builds and releases the new APK automatically.

If you actually want end users to be able to add/rename/delete tasks from the
UI, that means re-exposing the "add" and "delete" flows that currently exist in
`MainActivity.kt` (`showAddDialog()`, `deleteItem()`) — right now those are
intentionally disconnected from the UI to keep the task list fixed. That's a
one-line call to re-wire in `ScheduleAdapter`'s `onDelete` and the FAB's
`onClickListener`, if you want it back.

---

## How an alarm actually fires — code path

1. `AlarmScheduler.schedule(context, item)` sets an exact `AlarmManager` alarm
   for `item.hour:item.minute`, using `PendingIntent.getBroadcast` targeting
   `AlarmReceiver`:

   ```kotlin
   val pi = pendingIntent(context, item)
   alarmManager.setExactAndAllowWhileIdle(
       AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi
   )
   ```

2. When it fires, `AlarmReceiver.onReceive()` shows the notification, plays a
   sound, and immediately reschedules the *same* item for 24h later:

   ```kotlin
   NotificationHelper.show(context, id, label)
   playSound(context)
   val item = ScheduleRepository.getItems(context).find { it.id == id }
   if (item != null && item.enabled) {
       AlarmScheduler.schedule(context, item)
   }
   ```

3. `NotificationHelper.show()` attaches three actions, each a `PendingIntent`
   pointing at `NotificationActionReceiver` with a different `action` string:

   ```kotlin
   .addAction(0, "Start", startIntent)
   .addAction(0, "Done", doneIntent)
   .addAction(0, "Skip", skipIntent)
   ```

4. Tapping one calls `NotificationActionReceiver.onReceive()`, which maps the
   action to an `EventStatus` and writes it via `EventRepository.record()`:

   ```kotlin
   val status = when (intent.action) {
       ACTION_START -> EventStatus.STARTED
       ACTION_DONE  -> EventStatus.DONE
       ACTION_SKIP  -> EventStatus.SKIPPED
       else -> return
   }
   EventRepository.record(context, id, status)
   ```

## Reading task history programmatically

`EventRepository` (`app/src/main/java/com/cuckoo/app/EventRepository.kt`) is
the single source of truth for completion history, keyed by `scheduleId` +
`dateEpochDay` (one entry per task per calendar day):

```kotlin
// All events ever recorded
EventRepository.getAll(context): List<TaskEvent>

// History for one task, oldest first
EventRepository.forSchedule(context, scheduleId): List<TaskEvent>

// Every event recorded for a specific day
EventRepository.forDay(context, dateEpochDay): List<TaskEvent>

// Look up a single task/day pair (e.g. "did I do Electromagnetism today?")
EventRepository.eventFor(context, scheduleId, dateEpochDay): TaskEvent?
```

`TaskEvent` (`app/src/main/java/com/cuckoo/app/TaskEvent.kt`):

```kotlin
enum class EventStatus { STARTED, DONE, SKIPPED, MISSED }

data class TaskEvent(
    val scheduleId: Int,
    val dateEpochDay: Long,
    var status: EventStatus,
    val timestamp: Long = System.currentTimeMillis()
)
```

Both `ScheduleRepository` and `EventRepository` store JSON via `Gson` in the
same `SharedPreferences` file (`cuckoo_prefs`), under keys `schedule_items` and
`task_events` respectively — no database, no external dependency.

---

## Permissions used (`AndroidManifest.xml`)

| Permission | Why |
|---|---|
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | fire alarms at the exact minute, not batched |
| `POST_NOTIFICATIONS` | show the alarm notification (required from Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | re-register all alarms after a reboot, via `BootReceiver` |
| `VIBRATE` | notification vibration pattern |
| `WAKE_LOCK` / `FOREGROUND_SERVICE` | keep the alarm sound playing reliably |

## Build pipeline

`.github/workflows/build-apk.yml` — runs on every push to `main` or manual
dispatch. Builds `assembleDebug`, tags the release `v1.0.<run number>`, and
uploads `app/build/outputs/apk/debug/app-debug.apk` as both a workflow
artifact and a GitHub Release asset. No local Android Studio setup is required
to ship a new build — pushing to `main` is the entire deploy pipeline.
